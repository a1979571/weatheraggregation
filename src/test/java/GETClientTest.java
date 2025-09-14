//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.List;
//
//import static org.junit.Assert.*;
//
//public class GETClientTest {
//
//    private AggregationServer server;
//    private Path tempFile;
//    private GETClient getClient;
//
//    @Before
//    public void setUp() throws Exception {
//        // Setup temporary aggregation server on port 5004
//        tempFile = Files.createTempFile("tempWeatherStore", ".json");
//        server = new AggregationServer(tempFile, 5004);
//
//        // Run server in separate thread
//        new Thread(server::start).start();
//        Thread.sleep(500);
//
//        // Add sample weather entry
//        WeatherEntry entry = new WeatherEntry();
//        entry.setId("GET100");
//        entry.setName("TestCity");
//        entry.setAirTemp(15.5);
//        entry.refreshLastUpdated();
//        server.aggregateEntry(entry);
//
//        getClient = new GETClient("localhost", 5004);
//    }
//
//    @After
//    public void tearDown() throws Exception {
//        Files.deleteIfExists(tempFile);
//    }
//
//    @Test
//    public void testGetWeatherReceivesEntries() {
//        List<WeatherEntry> entries = getClient.getWeatherEntries();
//
//        assertNotNull(entries);
//        assertEquals(1, entries.size());
//        WeatherEntry fetched = entries.get(0);
//        assertEquals("GET100", fetched.getId());
//        assertEquals("TestCity", fetched.getName());
//        assertEquals(15.5, fetched.getAirTemp(), 0.001);
//    }
//
//    @Test
//    public void testGetWeatherEmptyServer() throws Exception {
//        // Clear server entries
//        server.getWeatherEntries().clear();
//
//        List<WeatherEntry> entries = getClient.getWeatherEntries();
//        assertNotNull(entries);
//        assertEquals(0, entries.size());
//    }
//}
