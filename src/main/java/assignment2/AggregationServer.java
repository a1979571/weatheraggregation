package assignment2;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.*;

/**
 * AggregationServer implementing socket-based REST-like API.
 * Supports GET and PUT /weather.json requests with HTTP-like response codes.
 * Manages Lamport clock, persistence, expiry of entries, and concurrency.
 */
public class AggregationServer {

    private final Path feedFile;
    public final List<WeatherEntry> weatherEntries;
    private final LamportClock lamportClock;
    public ServerSocket serverSocket;

    private static final long EXPIRY_MILLIS = 30_000;
    private static final int PORT_DEFAULT = 4567;

    public AggregationServer(Path feedFile) throws IOException {
        this.feedFile = feedFile;
        this.weatherEntries = Collections.synchronizedList(new ArrayList<>());
        this.lamportClock = new LamportClock();
        loadFeed();
    }

    private void loadFeed() throws IOException {
        if (!feedFile.toFile().exists() || feedFile.toFile().length() == 0) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(feedFile.toFile()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            if (sb.isEmpty()) return;

            List<Map<String, String>> list = JsonParser.parseArray(sb.toString());
            for (Map<String, String> map : list) {
                WeatherEntry entry = WeatherEntry.fromMap(map);
                // Only add entries that have a valid id
                if (entry.getId() != null && !entry.getId().isEmpty()) {
                    weatherEntries.add(entry);
                }
            }
        }
        System.out.println("Loaded " + weatherEntries.size() + " weather entries.");
    }

    public synchronized void saveFeed() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(feedFile.toFile()))) {
            String json = JsonParser.toJsonArray(weatherEntries);
            writer.write(json);
        }
        System.out.println("Saved " + weatherEntries.size() + " entries.");
    }

    public boolean aggregateEntry(WeatherEntry newEntry) {
        synchronized (weatherEntries) {
            lamportClock.tick();
            newEntry.setLamportTime(lamportClock.getTime());
            newEntry.refreshLastUpdated();

            for (int i = 0; i < weatherEntries.size(); i++) {
                if (Objects.equals(weatherEntries.get(i).getId(), newEntry.getId())) {
                    weatherEntries.set(i, newEntry);
                    return false;
                }
            }
            weatherEntries.add(newEntry);
            return true;
        }
    }


    public void removeExpiredEntries() {
        synchronized (weatherEntries) {
            long now = System.currentTimeMillis();
            boolean removed = weatherEntries.removeIf(e -> now - e.getLastUpdatedEpoch() > EXPIRY_MILLIS);
            if (removed) {
                try {
                    saveFeed();
                } catch (IOException e) {
                    System.err.println("Error saving feed after cleanup: " + e.getMessage());
                }
            }
        }
    }

    public void startServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("AggregationServer running on port " + port);

        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                removeExpiredEntries();
            }
        }, 10000, 10000);

        while (true) {
            Socket client = serverSocket.accept();
            new Thread(() -> handleClient(client)).start();
        }
    }

    private void handleClient(Socket client) {
        try (client; BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()))) {

            String requestLine = in.readLine();
            if (requestLine == null) {
                writeResponse(out, 400, "Bad Request");
                return;
            }

            String[] parts = requestLine.split(" ");
            if (parts.length < 3) {
                writeResponse(out, 400, "Bad Request");
                return;
            }

            String method = parts[0];
            String path = parts[1];

            int contentLength = 0;
            String line;
            while (!(line = in.readLine()).isEmpty()) {
                if (line.toLowerCase().startsWith("content-length:")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }
            }

            if (method.equalsIgnoreCase("GET") && path.equalsIgnoreCase("/weather.json")) {
                handleGet(out);
            } else if (method.equalsIgnoreCase("PUT") && path.equalsIgnoreCase("/weather.json")) {
                char[] content = new char[contentLength];
                int read = in.read(content, 0, contentLength);
                if (read != contentLength) {
                    writeResponse(out, 400, "Incomplete PUT body");
                    return;
                }
                handlePutPayload(new String(content), out);
            } else {
                writeResponse(out, 400, "Unsupported Method or Path");
            }

        } catch (IOException ignored) {}
    }

    private void handlePutPayload(String payload, BufferedWriter out) throws IOException {
        if (payload.isEmpty()) {
            writeResponse(out, 204, "No Content");
            return;
        }
        try {
            WeatherEntry entry = WeatherEntry.fromMap(JsonParser.parseObject(payload));
            if (entry.getId() == null || entry.getId().isEmpty()) {
                writeResponse(out, 500, "Invalid JSON or Missing ID");
                return;
            }
            boolean addedNew = aggregateEntry(entry);
            saveFeed();
            writeResponse(out, addedNew ? 201 : 200, "OK");
        } catch (Exception e) {
            writeResponse(out, 500, "Invalid JSON Format");
        }
    }

    private void handleGet(BufferedWriter out) throws IOException {
        synchronized (weatherEntries) {
            String json = JsonParser.toJsonArray(weatherEntries);
            writeResponse(out, 200, json);
        }
    }

    private void writeResponse(BufferedWriter out, int statusCode, String body) throws IOException {
        String statusText = switch (statusCode) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 400 -> "Bad Request";
            case 500 -> "Internal Server Error";
            default -> "Error";
        };
        out.write("HTTP/1.1 " + statusCode + " " + statusText + "\r\n");
        out.write("Content-Type: application/json\r\n");
        out.write("Content-Length: " + body.length() + "\r\n");
        out.write("\r\n");
        out.write(body);
        out.flush();
    }

    public static void main(String[] args) throws IOException {
        Path feedPath = (args.length < 1) ? Path.of("feed.json") : Path.of(args[0]);
        AggregationServer server = new AggregationServer(feedPath);
        server.startServer(args.length < 2 ? PORT_DEFAULT : Integer.parseInt(args[1]));
    }
}
