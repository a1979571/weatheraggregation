package assignment2;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.List;

/**
 * GETClient
 *
 * Connects to AggregationServer to fetch weather entries.
 * Demonstrates Lamport clock awareness and JSON parsing.
 */
public class GETClient {

    private static final String HOST = "localhost";
    private static final int PORT = 4567;
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send GET request (HTTP-like)
            out.write("GET /weather HTTP/1.1\r\n");
            out.write("User-Agent: GETClient/1.0\r\n");
            out.write("\r\n");
            out.flush();

            // Read response headers (optional)
            String line;
            int contentLength = 0;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                if (line.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }
            }

            // Read response body
            char[] bodyChars = new char[contentLength];
            in.read(bodyChars, 0, contentLength);
            String body = new String(bodyChars);

            // Parse JSON into list of WeatherEntry
            Type listType = new TypeToken<List<WeatherEntry>>() {}.getType();
            List<WeatherEntry> entries = gson.fromJson(body, listType);

            // Print entries
            System.out.println("=== Weather Entries ===");
            for (WeatherEntry entry : entries) {
                System.out.printf("ID: %s | Location: %s | Condition: %s | Temp: %.1f°C | Lamport: %d%n",
                        entry.getId(), entry.getLocation(), entry.getCondition(),
                        entry.getTemperature(), entry.getLamportTime());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
