import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.List;

public class GETClient {

    private final String serverHost = "localhost";
    private final int serverPort = 5000;
    private final Gson gson = new Gson();

    public void getWeather() {
        try (Socket socket = new Socket(serverHost, serverPort);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println("GET");

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }

            Type listType = new TypeToken<List<WeatherEntry>>() {}.getType();
            List<WeatherEntry> entries = gson.fromJson(sb.toString(), listType);

            for (WeatherEntry e : entries) {
                System.out.println(e);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new GETClient().getWeather();
    }
}
