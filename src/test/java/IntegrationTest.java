import assignment2.AggregationServer;
import assignment2.ContentServer;
import assignment2.GETClient;
import assignment2.WeatherEntry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.*;

public class IntegrationTest {

    private AggregationServer server;
    private Path tempFeed;

    @Before
    public void setUp() throws Exception {
        tempFeed = Files.createTempFile("feed", ".txt");
        server = new AggregationServer(tempFeed);
        new Thread(() -> {
            try {
                server.startServer(4567);
            } catch (Exception ignored) {}
        }).start();

        // Small delay to let server start
        Thread.sleep(500);
    }

    @After
    public void tearDown() throws Exception {
        if (server != null) {
            server.serverSocket.close();
        }
    }

    @Test
    public void testContentServerPUTAndGET() throws Exception {
        // Create temp weather file
        File weatherFile = File.createTempFile("weather", ".txt");
        try (FileWriter fw = new FileWriter(weatherFile)) {
            fw.write("id:INT001\n");
            fw.write("name:IntegrationCity\n");
            fw.write("state:TS\n");
            fw.write("lat:12.5\n");
            fw.write("lon:45.0\n");
            fw.write("air_temp:28.5\n");
        }

        // Parse using Gson (false) or SimpleJsonParser (true)
        WeatherEntry entry = ContentServer.parseFromFile(weatherFile);

        // Send PUT to server
        ContentServer cs = new ContentServer("localhost", 4567);
        cs.sendWeatherUpdate(entry);

        // Small delay for server to process
        Thread.sleep(500);

        // GET from server
        GETClient client = new GETClient("localhost", 4567);
        client.getWeather();

        // Check that server contains the entry
        List<WeatherEntry> entries = server.weatherEntries;
        assertEquals(1, entries.size());
        assertEquals("INT001", entries.get(0).getId());
        assertEquals("IntegrationCity", entries.get(0).getName());
    }
}
