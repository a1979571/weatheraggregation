import assignment2.*;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class ServerExpiryTest {

    private AggregationServer server;
    private Path tempFeed;

    @Before
    public void setUp() throws Exception {
        tempFeed = Files.createTempFile("feed", ".json");
        server = new AggregationServer(tempFeed);
    }

    @Test
    public void testAutomaticExpiry() throws InterruptedException {
        WeatherEntry oldEntry = new WeatherEntry();
        oldEntry.setId("OLD");
        oldEntry.refreshLastUpdated();
        server.aggregateEntry(oldEntry);

        // Force old timestamp
        oldEntry.setLastUpdatedEpoch(System.currentTimeMillis() - 31_000);

        // Schedule cleanup (simulate TimerTask)
        server.removeExpiredEntries();

        assertTrue(server.weatherEntries.isEmpty());
    }
}
