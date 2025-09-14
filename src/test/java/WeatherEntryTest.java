import org.junit.Test;
import java.time.Instant;
import static org.junit.Assert.*;

public class WeatherEntryTest {

    @Test
    public void testGettersSetters() {
        WeatherEntry entry = new WeatherEntry();
        entry.setId("ID1");
        entry.setAirTemp(25.5);
        entry.setLamportTime(10);

        assertEquals("ID1", entry.getId());
        assertEquals(25.5, entry.getAirTemp(), 0.001);
        assertEquals(10, entry.getLamportTime());
    }

    @Test
    public void testLastUpdatedEpoch() {
        WeatherEntry entry = new WeatherEntry();
        long before = Instant.now().toEpochMilli();
        entry.refreshLastUpdated();
        long after = entry.getLastUpdatedEpoch();
        assertTrue(after >= before);

        // Test setter
        entry.setLastUpdatedEpoch(123456789L);
        assertEquals(123456789L, entry.getLastUpdatedEpoch());
    }
}
