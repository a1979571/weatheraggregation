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

/**
 * AggregationServer.java
 *
 * A simple server that listens on a port for weather updates from clients.
 * Aggregates data, updates Lamport timestamps, and saves to feed file.
 * Supports expiry of old entries.
 */
public class AggregationServer {

    private final Path feedFile;
    public final List<WeatherEntry> weatherEntries;
    private final Gson gson;

    public AggregationServer(Path feedFile) throws IOException {
        this.feedFile = feedFile;

        this.gson = new GsonBuilder()
                .registerTypeAdapter(Instant.class, (JsonSerializer<Instant>) (src, typeOfSrc, context) ->
                        new JsonPrimitive(src.toString()))
                .registerTypeAdapter(Instant.class, (JsonDeserializer<Instant>) (json, typeOfT, context) ->
                        Instant.parse(json.getAsString()))
                .setPrettyPrinting()
                .create();

        this.weatherEntries = Collections.synchronizedList(new ArrayList<>());
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

    public void saveFeed() throws IOException {
        synchronized (weatherEntries) {
            try (FileWriter writer = new FileWriter(feedFile.toFile())) {
                gson.toJson(weatherEntries, writer);
            }
        }
        System.out.println("Saved " + weatherEntries.size() + " entries to " + feedFile);
    }

    // Update or add a weather entry
    public void aggregateEntry(WeatherEntry newEntry) {
        synchronized (weatherEntries) {
            int maxLamport = weatherEntries.stream().mapToInt(WeatherEntry::getLamportTime).max().orElse(0);
            newEntry.setLamportTime(maxLamport + 1);

            // Replace existing entry if id matches
            boolean replaced = false;
            for (int i = 0; i < weatherEntries.size(); i++) {
                if (weatherEntries.get(i).getId().equals(newEntry.getId())) {
                    weatherEntries.set(i, newEntry);
                    replaced = true;
                    break;
                }
            }
            if (!replaced) weatherEntries.add(newEntry);

            System.out.println("Aggregated entry: " + newEntry);
        }
    }

    /**
     * Remove entries older than `expiryMillis`
     */
    public void removeExpiredEntries(long expiryMillis) {
        synchronized (weatherEntries) {
            long now = System.currentTimeMillis();
            weatherEntries.removeIf(e -> now - e.getLastUpdatedEpoch() > expiryMillis);
        }
    }

    public void startServer(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("AggregationServer running on port " + port);

        while (true) {
            Socket client = serverSocket.accept();
            new Thread(() -> handleClient(client)).start();
        }
    }

    private void handleClient(Socket client) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()))) {

            String command = in.readLine();
            if (command == null) return;

            if (command.equalsIgnoreCase("GET")) {
                synchronized (weatherEntries) {
                    String json = gson.toJson(weatherEntries);
                    out.write(json);
                    out.write("\n");
                    out.flush();
                }
            } else {
                // Treat as weather update
                WeatherEntry entry = gson.fromJson(command, WeatherEntry.class);
                if (entry != null) aggregateEntry(entry);

                out.write("OK\n");
                out.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { client.close(); } catch (IOException ignored) {}
        }
    }

    public static void main(String[] args) throws IOException {
        Path feedPath = (args.length < 1) ?
                Path.of("C:\\Users\\91810\\IdeaProjects\\weatheraggregation\\src\\main\\java\\feed.txt") :
                Path.of(args[0]);

        AggregationServer server = new AggregationServer(feedPath);
        server.startServer(5000);
    }
}
