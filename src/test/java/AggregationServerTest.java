import static org.junit.Assert.*;

import assignment2.AggregationServer;
import assignment2.WeatherEntry;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

public class AggregationServerTest {

    private AggregationServer server;

    @Before
    public void setUp() throws Exception {
        Path tempFile = Files.createTempFile("feed", ".json");
        server = new AggregationServer(tempFile);
    }

    @Test
    public void testAggregateNewEntry() {
        WeatherEntry entry = new WeatherEntry();
        entry.setId("ID1");
        entry.setName("City1");

        boolean added = server.aggregateEntry(entry);
        assertTrue(added);
        assertEquals(1, server.weatherEntries.size());
        assertEquals("ID1", server.weatherEntries.get(0).getId());
    }

    @Test
    public void testAggregateReplaceEntry() {
        WeatherEntry entry1 = new WeatherEntry();
        entry1.setId("ID1");
        entry1.setName("City1");

        WeatherEntry entry2 = new WeatherEntry();
        entry2.setId("ID1");
        entry2.setName("City2");

        server.aggregateEntry(entry1);
        boolean added = server.aggregateEntry(entry2);
        assertFalse(added);

        assertEquals(1, server.weatherEntries.size());
        assertEquals("City2", server.weatherEntries.get(0).getName());
    }

    @Test
    public void testRemoveExpiredEntries() throws InterruptedException {
        WeatherEntry entry = new WeatherEntry();
        entry.setId("ID1");
        entry.setName("City1");
        entry.refreshLastUpdated();

        server.aggregateEntry(entry);

        // Simulate expiry
        entry.setLastUpdatedEpoch(System.currentTimeMillis() - 31_000);

        server.removeExpiredEntries();

        assertTrue(server.weatherEntries.isEmpty());
    }
}