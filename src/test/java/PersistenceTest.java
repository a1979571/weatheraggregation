import assignment2.PersistenceManager;
import org.junit.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.Assert.*;

public class PersistenceTest {

    @Test
    public void testSaveAndLoadJson() throws Exception {
        Path temp = Files.createTempFile("testWeather", ".json");
        PersistenceManager pm = new PersistenceManager(temp);

        String json = "{\"id\":\"ID001\"}";
        pm.saveJson(json);

        String loaded = pm.loadJson();
        assertEquals(json, loaded);
    }
}
