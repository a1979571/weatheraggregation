//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.nio.file.Files;
//import java.nio.file.Path;
//
//import static org.junit.Assert.*;
//
//public class ContentServerTest {
//
//    private AggregationServer server;
//    private ContentServer contentServer;
//    private Path tempFile;
//
//    @Before
//    public void setUp() throws Exception {
//        // Setup a temporary aggregation server on random port
//        tempFile = Files.createTempFile("tempWeatherStore", ".json");
//        server = new AggregationServer(tempFile, 5003);
//
//        // Run server in a separate thread
//        new Thread(server::start).start();
//        Thread.sleep(500); // Give server time to start
//
//        contentServer = new ContentServer("localhost", 5003);
//    }
//
//    @After
//    public void tearDown() throws Exception {
//        Files.deleteIfExists(tempFile);
//    }
//
//    @Test
//    public void testSendWeatherUpdateAddsEntry() throws Exception {
//        WeatherEntry entry = new WeatherEntry();
//        entry.setId("CS100");
//        entry.setAirTemp(25.0);
//        entry.setLamportTime(1);
//        entry.refreshLastUpdated();
//
//        contentServer.sendWeatherUpdate(entry);
//
//        // Wait a bit to allow server to process
//        Thread.sleep(200);
//
//        // Aggregate server should have the entry
//        assertEquals(1, server.getWeatherEntries().size());
//        assertEquals("CS100", server.getWeatherEntries().get(0).getId());
//        assertEquals(25.0, server.getWeatherEntries().get(0).getAirTemp(), 0.001);
//    }
//
//    @Test
//    public void testSendMultipleUpdatesRespectsLamportTime() throws Exception {
//        WeatherEntry entry1 = new WeatherEntry();
//        entry1.setId("CS200");
//        entry1.setAirTemp(20.0);
//        entry1.setLamportTime(1);
//        entry1.refreshLastUpdated();
//
//        WeatherEntry entry2 = new WeatherEntry();
//        entry2.setId("CS200");
//        entry2.setAirTemp(22.0);
//        entry2.setLamportTime(5); // higher Lamport time
//        entry2.refreshLastUpdated();
//
//        contentServer.sendWeatherUpdate(entry1);
//        contentServer.sendWeatherUpdate(entry2);
//
//        Thread.sleep(200);
//
//        // Server should have updated entry2
//        assertEquals(1, server.getWeatherEntries().size());
//        WeatherEntry finalEntry = server.getWeatherEntries().get(0);
//        assertEquals(22.0, finalEntry.getAirTemp(), 0.001);
//        assertEquals(5, finalEntry.getLamportTime());
//    }
//}
