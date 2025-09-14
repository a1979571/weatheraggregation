package assignment2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.Socket;

/**
 * ContentServer
 *
 * Reads a weather JSON file and sends it to the AggregationServer.
 *
 * Usage: java ContentServer <host:port> <json-feed-file>
 */
public class ContentServer {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java ContentServer <host:port> <json-feed-file>");
            return;
        }

        String hostPort = args[0];
        String[] parts = hostPort.split(":");
        if (parts.length != 2) {
            System.out.println("Invalid host:port format.");
            return;
        }

        String host = parts[0];
        int port = Integer.parseInt(parts[1]);
        File feedFile = new File(args[1]);

        try {
            // Read JSON directly from file
            String json = readJsonFile(feedFile);
            if (json == null || json.isEmpty()) {
                System.out.println("Feed file is empty or invalid.");
                return;
            }

            // Send JSON to AggregationServer
            try (Socket socket = new Socket(host, port);
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                 BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                // Send POST request (HTTP-like)
                writer.write("POST /weather HTTP/1.1\r\n");
                writer.write("User-Agent: ContentServer/1.0\r\n");
                writer.write("Content-Type: application/json\r\n");
                writer.write("Content-Length: " + json.getBytes().length + "\r\n");
                writer.write("\r\n");
                writer.write(json);
                writer.flush();

                // Read server response headers
                String responseLine;
                while ((responseLine = reader.readLine()) != null && !responseLine.isEmpty()) {
                    System.out.println(responseLine);
                }

                System.out.println("Weather entry sent successfully.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Read entire JSON content from file */
    private static String readJsonFile(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }
}
