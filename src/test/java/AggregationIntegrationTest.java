import org.junit.Test;
import java.io.File;
import java.nio.file.Path;
import static org.junit.Assert.*;

public class AggregationIntegrationTest {

    @Test
    public void testAggregateEntryAddsNew() throws Exception {
        Path temp = new File("tempFeed.json").toPath();
        AggregationServer server = new AggregationServer(temp);

        WeatherEntry entry = new WeatherEntry();
        entry.setId("ID100");
        entry.setLastUpdatedEpoch(1000); // deterministic
        server.aggregateEntry(entry);

        assertEquals(1, server.weatherEntries.size());
        assertEquals("ID100", server.weatherEntries.get(0).getId());
        assertEquals(1000, server.weatherEntries.get(0).getLastUpdatedEpoch());
    }

    @Test
    public void testAggregateEntryUpdatesExisting() throws Exception {
        Path temp = new File("tempFeed.json").toPath();
        AggregationServer server = new AggregationServer(temp);

        WeatherEntry entry1 = new WeatherEntry();
        entry1.setId("ID200");
        entry1.setAirTemp(20.0);
        entry1.setLastUpdatedEpoch(1000);
        server.aggregateEntry(entry1);

        WeatherEntry entry2 = new WeatherEntry();
        entry2.setId("ID200");
        entry2.setAirTemp(22.5);
        entry2.setLastUpdatedEpoch(2000);
        server.aggregateEntry(entry2);

        assertEquals(1, server.weatherEntries.size());
        assertEquals(22.5, server.weatherEntries.get(0).getAirTemp(), 0.001);
        assertEquals(2000, server.weatherEntries.get(0).getLastUpdatedEpoch());
    }

    @Test
    public void testLastUpdatedPreserved() throws Exception {
        Path temp = new File("tempFeed.json").toPath();
        AggregationServer server = new AggregationServer(temp);

        WeatherEntry entry = new WeatherEntry();
        entry.setId("ID300");
        entry.setLastUpdatedEpoch(123456789);
        server.aggregateEntry(entry);

        assertEquals(123456789, server.weatherEntries.get(0).getLastUpdatedEpoch());
    }

    @Test
    public void testRemoveExpiredEntries() throws Exception {
        Path temp = new File("tempFeed.json").toPath();
        AggregationServer server = new AggregationServer(temp);

        WeatherEntry oldEntry = new WeatherEntry();
        oldEntry.setId("OLD");
        oldEntry.setLastUpdatedEpoch(System.currentTimeMillis() - 1000*3600*25); // older than 24h
        server.aggregateEntry(oldEntry);

        WeatherEntry recentEntry = new WeatherEntry();
        recentEntry.setId("NEW");
        recentEntry.setLastUpdatedEpoch(System.currentTimeMillis());
        server.aggregateEntry(recentEntry);

        server.removeExpiredEntries(24*3600*1000); // remove older than 24h
        assertEquals(1, server.weatherEntries.size());
        assertEquals("NEW", server.weatherEntries.get(0).getId());
    }
}
