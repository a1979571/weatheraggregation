import assignment2.WeatherEntry;
import org.junit.Test;
import java.time.Instant;
import static org.junit.Assert.*;

public class WeatherEntryTest {

    @Test
    public void testGettersSetters() {
        WeatherEntry entry = new WeatherEntry();
        entry.setId("ID1");
        entry.setName("CityA");
        entry.setAirTemp(25.5);
        entry.setApparentTemp(26.0);
        entry.setCloud(50);
        entry.setDewpt(15.0);
        entry.setPress(1012.0);
        entry.setRelHum(60.0);
        entry.setWindDir(180.0);
        entry.setWindSpdKmh(20.0);
        entry.setWindSpdKt(10.0);
        entry.setLamportTime(10);
        entry.refreshLastUpdated();

        assertEquals("ID1", entry.getId());
        assertEquals("CityA", entry.getName());
        assertEquals(25.5, entry.getAirTemp(), 0.001);
        assertEquals(26.0, entry.getApparentTemp(), 0.001);
        assertEquals(50, entry.getCloud());
        assertEquals(15.0, entry.getDewpt(), 0.001);
        assertEquals(1012.0, entry.getPress(), 0.001);
        assertEquals(60.0, entry.getRelHum(), 0.001);
        assertEquals(180.0, entry.getWindDir(), 0.001);
        assertEquals(20.0, entry.getWindSpdKmh(), 0.001);
        assertEquals(10.0, entry.getWindSpdKt(), 0.001);
        assertEquals(10, entry.getLamportTime());
        assertTrue(entry.getLastUpdatedEpoch() > 0);
    }

    @Test
    public void testLastUpdatedEpochSetterAndRefresh() {
        WeatherEntry entry = new WeatherEntry();
        long customTime = 123456789L;
        entry.setLastUpdatedEpoch(customTime);
        assertEquals(customTime, entry.getLastUpdatedEpoch());

        entry.refreshLastUpdated();
        long refreshedTime = entry.getLastUpdatedEpoch();
        long now = Instant.now().toEpochMilli();
        assertTrue(refreshedTime <= now && refreshedTime >= now - 1000); // refreshed recently
    }

    @Test
    public void testToStringContainsId() {
        WeatherEntry entry = new WeatherEntry();
        entry.setId("ID123");
        String str = entry.toString();
        assertTrue(str.contains("ID123"));
    }
}
