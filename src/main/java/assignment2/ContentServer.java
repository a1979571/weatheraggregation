package assignment2;
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;

/**
 * ContentServer reads a weather data file in key:value format,
 * parses it into a WeatherEntry, and sends a PUT request to AggregationServer.
 * Uses proper HTTP headers, Content-Length, and flushes output.
 * Includes basic retry logic on connection failures.
 */
public class ContentServer {

    private final String serverHost;
    private final int serverPort;
    private final Gson gson = new Gson();

    public ContentServer(String host, int port) {
        this.serverHost = host;
        this.serverPort = port;
    }

    public void sendWeatherUpdate(WeatherEntry entry) {
        int maxRetries = 3;
        int retryDelayMs = 2000;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try (Socket socket = new Socket(serverHost, serverPort);
                 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                String json = gson.toJson(entry);
                String request = "PUT /weather.json HTTP/1.1\r\n" +
                        "User-Agent: ContentServer/1.0\r\n" +
                        "Content-Type: application/json\r\n" +
                        "Content-Length: " + json.getBytes("UTF-8").length + "\r\n" +
                        "\r\n" +
                        json;

                out.write(request);
                out.flush();

                // Read response status line
                String statusLine = in.readLine();
                System.out.println("Server response: " + statusLine);

                if (statusLine != null && (statusLine.contains("201") || statusLine.contains("200"))) {
                    System.out.println("Weather update sent successfully.");
                    return;
                } else {
                    System.out.println("Failed, retrying...");
                }

            } catch (IOException e) {
                System.out.println("Connection error: " + e.getMessage() + ". Retrying (" + attempt + "/" + maxRetries + ")");
            }

            try {
                Thread.sleep(retryDelayMs);
            } catch (InterruptedException ignored) {}
        }
        System.out.println("Failed to send weather update after retries.");
    }

    public static WeatherEntry parseFromFile(File file) throws IOException {
        WeatherEntry entry = new WeatherEntry();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                int idx = line.indexOf(':');
                if (idx < 0) continue;
                String key = line.substring(0, idx).trim();
                String val = line.substring(idx + 1).trim();

                switch (key) {
                    case "id": entry.setId(val); break;
                    case "name": entry.setName(val); break;
                    case "state": entry.setState(val); break;
                    case "time_zone": entry.setTimeZone(val); break;
                    case "lat": entry.setLat(Double.parseDouble(val)); break;
                    case "lon": entry.setLon(Double.parseDouble(val)); break;
                    case "local_date_time": entry.setLocalDateTime(val); break;
                    case "local_date_time_full": entry.setLocalDateTimeFull(val); break;
                    case "air_temp": entry.setAirTemp(Double.parseDouble(val)); break;
                    case "apparent_t": entry.setApparentTemp(Double.parseDouble(val)); break;
                    case "cloud": entry.setCloud(val); break;
                    case "dewpt": entry.setDewpt(Double.parseDouble(val)); break;
                    case "press": entry.setPress(Double.parseDouble(val)); break;
                    case "rel_hum": entry.setRelHum(Integer.parseInt(val)); break;
                    case "wind_dir": entry.setWindDir(val); break;
                    case "wind_spd_kmh": entry.setWindSpdKmh(Integer.parseInt(val)); break;
                    case "wind_spd_kt": entry.setWindSpdKt(Integer.parseInt(val)); break;
                    default: break;
                }
            }
        }
        return entry;
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: ContentServer <host> <port> <weather_data_file>");
            System.exit(1);
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String filename = args[2];

        ContentServer cs = new ContentServer(host, port);
        try {
            WeatherEntry entry = parseFromFile(new File(filename));
            if (entry.getId() == null || entry.getId().isEmpty()) {
                System.err.println("Invalid input file: Missing id field.");
                System.exit(1);
            }
            cs.sendWeatherUpdate(entry);
        } catch (IOException e) {
            System.err.println("Error reading input file: " + e.getMessage());
            System.exit(1);
        }
    }
}