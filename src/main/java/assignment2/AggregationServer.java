package assignment2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * AggregationServer
 *
 * Central server that aggregates weather data from multiple ContentServers
 * and serves GET requests to clients.
 * Ensures consistency using Lamport clocks and handles 30-second expiry.
 */
public class AggregationServer {

    private static final int DEFAULT_PORT = 4567;
    private static final String STORAGE_FILE = "weather_store.json";

    private final Map<String, WeatherEntry> store = new HashMap<>();
    private final Map<String, Long> lastUpdate = new HashMap<>(); // track last contact
    private final ReentrantLock lock = new ReentrantLock();
    private final LamportClock lamportClock = new LamportClock();
    private final PersistenceManager persistenceManager = new PersistenceManager(Paths.get(STORAGE_FILE));
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length >= 1) {
            try { port = Integer.parseInt(args[0]); } catch (NumberFormatException ignored) {}
        }
        new AggregationServer().start(port);
    }

    private void start(int port) {
        loadStore();
        startExpiryCleaner();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Aggregation Server running on port " + port);

            while (true) {
                Socket client = serverSocket.accept();
                new Thread(() -> handleClient(client)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load persisted store if exists
    private void loadStore() {
        try {
            String json = persistenceManager.loadJson();
            if (json != null && !json.isEmpty()) {
                WeatherEntry[] entries = gson.fromJson(json, WeatherEntry[].class);
                for (WeatherEntry entry : entries) {
                    store.put(entry.getId(), entry);
                    lastUpdate.put(entry.getId(), System.currentTimeMillis());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load persisted store: " + e.getMessage());
        }
    }

    // Periodically remove stale entries older than 30 seconds
    private void startExpiryCleaner() {
        cleaner.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            lock.lock();
            try {
                Iterator<Map.Entry<String, Long>> it = lastUpdate.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, Long> entry = it.next();
                    if (now - entry.getValue() > 30_000) { // 30 seconds
                        store.remove(entry.getKey());
                        it.remove();
                        System.out.println("Expired: " + entry.getKey());
                    }
                }
                persistStore();
            } finally {
                lock.unlock();
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    private void handleClient(Socket client) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()))) {

            String firstLine = in.readLine();
            if (firstLine == null) return;

            if (firstLine.startsWith("GET /weather")) {
                sendWeatherEntries(out);
            } else if (firstLine.startsWith("POST /weather")) {
                StringBuilder body = new StringBuilder();
                String line;
                boolean isBody = false;
                while ((line = in.readLine()) != null) {
                    if (line.isEmpty()) {
                        isBody = true; // Headers done
                        continue;
                    }
                    if (isBody) body.append(line);
                }
                receiveWeatherEntry(body.toString(), out);
            } else {
                sendStatus(out, 400, "Bad Request");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendWeatherEntries(BufferedWriter out) throws IOException {
        lock.lock();
        try {
            String json = gson.toJson(store.values());
            out.write("HTTP/1.1 200 OK\r\n");
            out.write("Content-Length: " + json.length() + "\r\n");
            out.write("\r\n");
            out.write(json);
            out.flush();
        } finally {
            lock.unlock();
        }
    }

    private void receiveWeatherEntry(String json, BufferedWriter out) throws IOException {
        if (json == null || json.isEmpty()) {
            sendStatus(out, 204, "No Content");
            return;
        }

        lock.lock();
        try {
            WeatherEntry entry = gson.fromJson(json, WeatherEntry.class);
            if (entry == null || entry.getId() == null || entry.getId().isEmpty()) {
                sendStatus(out, 500, "Invalid JSON");
                return;
            }

            // Update Lamport clock
            lamportClock.update(entry.getLamportTime());
            entry.setLamportTime(lamportClock.tick());
            entry.setTimestamp(System.currentTimeMillis());

            boolean isNew = !store.containsKey(entry.getId());
            store.put(entry.getId(), entry);
            lastUpdate.put(entry.getId(), System.currentTimeMillis());
            persistStore();

            sendStatus(out, isNew ? 201 : 200, isNew ? "Created" : "OK");
            System.out.println((isNew ? "Created" : "Updated") + ": " + entry);

        } catch (Exception e) {
            sendStatus(out, 500, "Internal Server Error");
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    private void persistStore() {
        try {
            persistenceManager.saveJson(gson.toJson(store.values()));
        } catch (IOException e) {
            System.err.println("Failed to persist store: " + e.getMessage());
        }
    }

    private void sendStatus(BufferedWriter out, int code, String message) throws IOException {
        out.write("HTTP/1.1 " + code + " " + message + "\r\n");
        out.write("\r\n");
        out.flush();
    }
}
