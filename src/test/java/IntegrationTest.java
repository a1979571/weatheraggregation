import assignment2.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.*;

public class IntegrationTest {

    private AggregationServer server;
    private Thread serverThread;
    private Path tempFeed;

    @Before
    public void setUp() throws IOException {
        tempFeed = Files.createTempFile("feed", ".json");
        server = new AggregationServer(tempFeed);

        serverThread = new Thread(() -> {
            try {
                server.startServer(0); // 0 = random free port
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();

        // Wait for server to start
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
    }

    @After
    public void tearDown() throws IOException {
        if (serverThread.isAlive()) serverThread.interrupt();
        Files.deleteIfExists(tempFeed);
    }

    private int getServerPort() {
        return server.serverSocket.getLocalPort();
    }

    @Test
    public void testPutAndGet() {
        int port = getServerPort();

        WeatherEntry entry = new WeatherEntry();
        entry.setId("INT001");
        entry.setName("IntegrationCity");

        ContentServer cs = new ContentServer("localhost", port);
        cs.sendWeatherUpdate(entry);

        GETClient getClient = new GETClient("localhost", port);
        getClient.getWeather(); // prints to stdout

        // Verify directly from server list
        assertEquals(1, server.weatherEntries.size());
        assertEquals("IntegrationCity", server.weatherEntries.get(0).getName());
    }
}
