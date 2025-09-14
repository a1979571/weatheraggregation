package assignment2;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class GETClient {
    private final String serverHost;
    private final int serverPort;

    public GETClient(String host, int port) {
        this.serverHost = host;
        this.serverPort = port;
    }

    public void getWeather() {
        int maxRetries = 3;
        int retryDelayMs = 2000;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try (Socket socket = new Socket(serverHost, serverPort);
                 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                // Send GET request
                String request = "GET /weather.json HTTP/1.1\r\n" +
                        "User-Agent: GETClient/1.0\r\n" +
                        "\r\n";
                out.write(request);
                out.flush();

                // Read status line
                String statusLine = in.readLine();
                if (statusLine == null || !statusLine.contains("200")) {
                    System.out.println("Failed: Server returned " + statusLine);
                    throw new IOException("Bad response");
                }

                // Read headers to find Content-Length
                int contentLength = 0;
                String line;
                while ((line = in.readLine()) != null && !line.trim().isEmpty()) {
                    if (line.toLowerCase().startsWith("content-length:")) {
                        contentLength = Integer.parseInt(line.split(":")[1].trim());
                    }
                }

                // Read JSON body
                char[] content = new char[contentLength];
                int totalRead = 0;
                while (totalRead < contentLength) {
                    int read = in.read(content, totalRead, contentLength - totalRead);
                    if (read == -1) break;
                    totalRead += read;
                }

                String json = new String(content, 0, totalRead);

                // Parse JSON array to List<WeatherEntry>
                List<Map<String, String>> list = JsonParser.parseArray(json);
                for (Map<String, String> map : list) {
                    WeatherEntry e = WeatherEntry.fromMap(map);
                    System.out.println("ID: " + e.getId() +
                            ", Name: " + e.getName() +
                            ", State: " + e.getState() +
                            ", Lat: " + e.getLat() +
                            ", Lon: " + e.getLon() +
                            ", Temp: " + e.getAirTemp() +
                            ", Lamport: " + e.getLamportTime());
                }

                return; // success

            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage() + ". Retry (" + attempt + "/" + maxRetries + ")");
            }

            try {
                Thread.sleep(retryDelayMs);
            } catch (InterruptedException ignored) {}
        }

        System.out.println("Failed to GET weather data after retries.");
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: GETClient <host> <port>");
            System.exit(1);
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        GETClient client = new GETClient(host, port);
        client.getWeather();
    }
}
