import static org.junit.Assert.*;

import assignment2.ContentServer;
import assignment2.WeatherEntry;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WeatherEntryTest {

    @Test
    public void testParseFromFile() throws IOException {
        File tempFile = File.createTempFile("weather", ".txt");
        try (FileWriter fw = new FileWriter(tempFile)) {
            fw.write("id:IDS123\n");
            fw.write("name:TestCity\n");
            fw.write("state:TS\n");
            fw.write("lat:10.5\n");
            fw.write("lon:20.5\n");
            fw.write("air_temp:25.4\n");
        }

        WeatherEntry entry = ContentServer.parseFromFile(tempFile);

        assertEquals("IDS123", entry.getId());
        assertEquals("TestCity", entry.getName());
        assertEquals(10.5, entry.getLat(), 0.001);
        assertEquals(25.4, entry.getAirTemp(), 0.001);
    }
}