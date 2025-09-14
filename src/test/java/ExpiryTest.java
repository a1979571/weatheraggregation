import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public class ExpiryTest {

    @Test
    public void testExpiryRemovesOldEntries() {
        List<WeatherEntry> entries = new ArrayList<>();
        WeatherEntry oldEntry = new WeatherEntry();
        oldEntry.setId("OLD");
        oldEntry.setLastUpdatedEpoch(System.currentTimeMillis() - 1000*3600*25);
        entries.add(oldEntry);

        WeatherEntry newEntry = new WeatherEntry();
        newEntry.setId("NEW");
        newEntry.setLastUpdatedEpoch(System.currentTimeMillis());
        entries.add(newEntry);

        entries.removeIf(e -> System.currentTimeMillis() - e.getLastUpdatedEpoch() > 24*3600*1000);

        assertEquals(1, entries.size());
        assertEquals("NEW", entries.get(0).getId());
    }

    @Test
    public void testExpiryKeepsRecentEntries() {
        List<WeatherEntry> entries = new ArrayList<>();
        WeatherEntry recent = new WeatherEntry();
        recent.setId("RECENT");
        recent.setLastUpdatedEpoch(System.currentTimeMillis());
        entries.add(recent);

        entries.removeIf(e -> System.currentTimeMillis() - e.getLastUpdatedEpoch() > 24*3600*1000);

        assertEquals(1, entries.size());
        assertEquals("RECENT", entries.get(0).getId());
    }
}
