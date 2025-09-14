//import org.junit.Before;
//import org.junit.Test;
//import java.io.File;
//import java.nio.file.Path;
//import static org.junit.Assert.*;
//
//public class AggregationIntegrationTest {
//
//    private AggregationServer server;
//    private Path tempFile;
//
//    @Before
//    public void setup() throws Exception {
//        tempFile = new File("tempWeather.json").toPath();
//        server = new AggregationServer(4567, tempFile);
//        server.weatherEntries.clear(); // ensure clean start
//    }
//
//    @Test
//    public void testAggregateEntryAddsNew() {
//        WeatherEntry entry = new WeatherEntry();
//        entry.setId("ID100");
//        entry.setLastUpdatedEpoch(System.currentTimeMillis());
//        server.aggregateEntry(entry);
//
//        assertEquals(1, server.weatherEntries.size());
//        assertEquals("ID100", server.weatherEntries.get(0).getId());
//    }
//
//    @Test
//    public void testAggregateEntryUpdatesExisting() {
//        WeatherEntry entry1 = new WeatherEntry();
//        entry1.setId("ID200");
//        entry1.setAirTemp(20.0);
//        entry1.setLastUpdatedEpoch(1000);
//        server.aggregateEntry(entry1);
//
//        WeatherEntry entry2 = new WeatherEntry();
//        entry2.setId("ID200");
//        entry2.setAirTemp(22.5);
//        entry2.setLastUpdatedEpoch(2000);
//        server.aggregateEntry(entry2);
//
//        assertEquals(1, server.weatherEntries.size());
//        assertEquals(22.5, server.weatherEntries.get(0).getAirTemp(), 0.001);
//        assertEquals(2000, server.weatherEntries.get(0).getLastUpdatedEpoch());
//    }
//
//    @Test
//    public void testRemoveExpiredEntries() {
//        WeatherEntry oldEntry = new WeatherEntry();
//        oldEntry.setId("OLD");
//        oldEntry.setLastUpdatedEpoch(System.currentTimeMillis() - 31_000); // older than 30 sec
//        server.aggregateEntry(oldEntry);
//
//        WeatherEntry recentEntry = new WeatherEntry();
//        recentEntry.setId("NEW");
//        recentEntry.setLastUpdatedEpoch(System.currentTimeMillis());
//        server.aggregateEntry(recentEntry);
//
//        server.removeExpiredEntries(30_000);
//        assertEquals(1, server.weatherEntries.size());
//        assertEquals("NEW", server.weatherEntries.get(0).getId());
//    }
//
//    @Test
//    public void testLamportTimeIncrements() {
//        WeatherEntry entry1 = new WeatherEntry();
//        entry1.setId("L1");
//        server.aggregateEntry(entry1);
//
//        WeatherEntry entry2 = new WeatherEntry();
//        entry2.setId("L2");
//        server.aggregateEntry(entry2);
//
//        int t1 = server.weatherEntries.get(0).getLamportTime();
//        int t2 = server.weatherEntries.get(1).getLamportTime();
//        assertTrue(t2 > t1); // Lamport time must increase
//    }
//}
