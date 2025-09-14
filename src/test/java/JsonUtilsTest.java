import assignment2.JsonUtils;
import assignment2.WeatherEntry;
import org.junit.Test;
import java.io.File;
import java.io.FileWriter;
import static org.junit.Assert.*;

public class JsonUtilsTest {

    @Test
    public void testToJsonAndFromJson() {
        WeatherEntry entry = new WeatherEntry();
        entry.setId("ID123");
        String json = JsonUtils.toJson(entry);

        WeatherEntry parsed = JsonUtils.fromJson(json);
        assertEquals("ID123", parsed.getId());
    }

    @Test
    public void testParseFeedFile() throws Exception {
        File temp = File.createTempFile("testFeed", ".txt");
        try (FileWriter writer = new FileWriter(temp)) {
            writer.write("id:ID999\nname:TestCity\nstate:TS\nlat:10.0\nlon:20.0\n");
        }

        WeatherEntry entry = JsonUtils.parseFeedFile(temp);
        assertNotNull(entry);
        assertEquals("ID999", entry.getId());
        assertEquals("TestCity", entry.getName());
        assertEquals(10.0, entry.getLat(), 0.001);
    }
}
