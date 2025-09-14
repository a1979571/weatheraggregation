package assignment2;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * AggregationServer implementing socket-based REST-like API.
 * Supports GET and PUT /weather.json requests with HTTP-like response codes.
 * Manages Lamport clock, persistence, expiry of entries, and concurrency.
 */
public class AggregationServer {

    private final Path feedFile;
    public final List<WeatherEntry> weatherEntries;
    private final Gson gson;
    private final LamportClock lamportClock;
    public ServerSocket serverSocket;

    private static final long EXPIRY_MILLIS = 30_000;
    private static final int PORT_DEFAULT = 4567;

    public AggregationServer(Path feedFile) throws IOException {
        this.feedFile = feedFile;
        this.weatherEntries = Collections.synchronizedList(new ArrayList<>());
        this.lamportClock = new LamportClock();

        this.gson = new GsonBuilder()
                .registerTypeAdapter(Instant.class, (JsonSerializer<Instant>) (src, typeOfSrc, context) ->
                        new JsonPrimitive(src.toString()))
                .registerTypeAdapter(Instant.class, (JsonDeserializer<Instant>) (json, typeOfT, context) ->
                        Instant.parse(json.getAsString()))
                .setPrettyPrinting()
                .create();

        loadFeed();
    }

    private void loadFeed() throws IOException {
        if (!feedFile.toFile().exists()) {
            System.out.println("Feed file not found. Starting with empty list.");
            return;
        }

        try (FileReader reader = new FileReader(feedFile.toFile())) {
            Type listType = new TypeToken<List<WeatherEntry>>() {}.getType();
            List<WeatherEntry> loaded = gson.fromJson(reader, listType);
            if (loaded != null) weatherEntries.addAll(loaded);
        }
        System.out.println("Loaded " + weatherEntries.size() + " weather entries from " + feedFile);
    }

    public synchronized void saveFeed() throws IOException {
        try (FileWriter writer = new FileWriter(feedFile.toFile())) {
            gson.toJson(weatherEntries, writer);
        }
        System.out.println("Saved " + weatherEntries.size() + " entries to " + feedFile);
    }

    /**
     * Aggregate or update a weather entry.
     * Updates Lamport timestamp and last updated time.
     * @param newEntry entry to add or update
     * @return true if new entry added, false if replaced
     */
    public boolean aggregateEntry(WeatherEntry newEntry) {
        synchronized (weatherEntries) {
            lamportClock.tick();
            newEntry.setLamportTime(lamportClock.getTime());
            newEntry.refreshLastUpdated();

            for (int i = 0; i < weatherEntries.size(); i++) {
                if (weatherEntries.get(i).getId().equals(newEntry.getId())) {
                    weatherEntries.set(i, newEntry);
                    return false; // replaced existing
                }
            }
            weatherEntries.add(newEntry);
            return true; // new added
        }
    }

    /**
     * Remove entries older than EXPIRY_MILLIS (30 seconds).
     */
    public void removeExpiredEntries() {
        synchronized (weatherEntries) {
            long now = System.currentTimeMillis();
            boolean removed = weatherEntries.removeIf(e -> now - e.getLastUpdatedEpoch() > EXPIRY_MILLIS);
            if (removed) {
                try {
                    saveFeed();
                } catch (IOException e) {
                    System.err.println("Error saving feed after expiry cleanup: " + e.getMessage());
                }
            }
        }
    }

    public void startServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("AggregationServer running on port " + port);

        // Schedule expiry cleanup every 10 seconds
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
        try (BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
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

            // Read headers to find Content-Length
            int contentLength = 0;
            String line;
            while (!(line = in.readLine()).equals("")) {
                if (line.toLowerCase().startsWith("content-length:")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }
            }

            if (method.equalsIgnoreCase("GET") && path.equalsIgnoreCase("/weather.json")) {
                handleGet(out);
            } else if (method.equalsIgnoreCase("PUT") && path.equalsIgnoreCase("/weather.json")) {
                // Read exact contentLength chars
                char[] content = new char[contentLength];
                int read = in.read(content, 0, contentLength);
                if (read != contentLength) {
                    writeResponse(out, 400, "Incomplete PUT body");
                    return;
                }
                String payload = new String(content);
                handlePutPayload(payload, out);
            } else {
                writeResponse(out, 400, "Unsupported Method or Path");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { client.close(); } catch (IOException ignored) {}
        }
    }

    private void handlePutPayload(String payload, BufferedWriter out) throws IOException {
        if (payload.isEmpty()) {
            writeResponse(out, 204, "No Content");
            return;
        }

        try {
            WeatherEntry newEntry = gson.fromJson(payload, WeatherEntry.class);
            if (newEntry == null || newEntry.getId() == null || newEntry.getId().isEmpty()) {
                writeResponse(out, 500, "Invalid JSON or Missing ID");
                return;
            }
            boolean addedNew = aggregateEntry(newEntry);
            saveFeed();
            writeResponse(out, addedNew ? 201 : 200, "OK");
        } catch (JsonSyntaxException e) {
            writeResponse(out, 500, "Invalid JSON Format");
        }
    }

    private void handleGet(BufferedWriter out) throws IOException {
        synchronized (weatherEntries) {
            String json = gson.toJson(weatherEntries);
            writeResponse(out, 200, json);
        }
    }

    private void handlePut(BufferedReader in, BufferedWriter out) throws IOException {
        // Read JSON payload - for simplicity read until EOF or socket close
        StringBuilder payload = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            payload.append(line);
        }

        if (payload.length() == 0) {
            writeResponse(out, 204, "No Content");
            return;
        }

        try {
            WeatherEntry newEntry = gson.fromJson(payload.toString(), WeatherEntry.class);
            if (newEntry == null || newEntry.getId() == null || newEntry.getId().isEmpty()) {
                writeResponse(out, 500, "Invalid JSON or Missing ID");
                return;
            }
            boolean addedNew = aggregateEntry(newEntry);
            saveFeed();
            writeResponse(out, addedNew ? 201 : 200, "OK");
        } catch (JsonSyntaxException e) {
            writeResponse(out, 500, "Invalid JSON Format");
        }
    }

    private void writeResponse(BufferedWriter out, int statusCode, String body) throws IOException {
        String statusText;
        switch (statusCode) {
            case 200: statusText = "OK"; break;
            case 201: statusText = "Created"; break;
            case 204: statusText = "No Content"; break;
            case 400: statusText = "Bad Request"; break;
            case 500: statusText = "Internal Server Error"; break;
            default: statusText = "Error"; break;
        }
        out.write("HTTP/1.1 " + statusCode + " " + statusText + "\r\n");
        out.write("Content-Type: application/json\r\n");
        out.write("Content-Length: " + body.length() + "\r\n");
        out.write("\r\n");
        out.write(body);
        out.flush();
    }

    public static void main(String[] args) throws IOException {
        Path feedPath = (args.length < 1)
                ? Path.of("feed.json")
                : Path.of(args[0]);

        AggregationServer server = new AggregationServer(feedPath);
        server.startServer(args.length < 2 ? PORT_DEFAULT : Integer.parseInt(args[1]));
    }
}