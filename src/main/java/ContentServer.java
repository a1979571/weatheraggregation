import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ContentServer {

    private final String serverHost = "localhost";
    private final int serverPort = 5000;

    public void sendWeatherUpdate(WeatherEntry entry) {
        try (Socket socket = new Socket(serverHost, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String json = JsonUtils.toJson(entry);
            out.println(json);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ContentServer cs = new ContentServer();

        WeatherEntry entry = new WeatherEntry();
        entry.setId("IDS60901");
        entry.setName("Adelaide (West Terrace / ngayirdapira)");
        entry.setState("SA");
        entry.setTimeZone("CST");
        entry.setLat(-34.9);
        entry.setLon(138.6);
        entry.setLocalDateTime("15/04:00pm");
        entry.setLocalDateTimeFull("20230715160000");
        entry.setAirTemp(13.3);
        entry.setApparentTemp(9.5);
        entry.setCloud("Partly cloudy");
        entry.setDewpt(5.7);
        entry.setPress(1023.9);
        entry.setRelHum(60);
        entry.setWindDir("S");
        entry.setWindSpdKmh(15);
        entry.setWindSpdKt(8);

        cs.sendWeatherUpdate(entry);
    }
}
