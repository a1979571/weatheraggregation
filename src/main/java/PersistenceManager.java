import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class PersistenceManager {

    private final Path targetFile;

    public PersistenceManager(Path targetFile) {
        this.targetFile = targetFile;
    }

    public void saveJson(String json) throws IOException {
        Path dir = targetFile.getParent() == null ? Paths.get(".") : targetFile.getParent();
        Path tmp = Files.createTempFile(dir, "weather", ".tmp");

        Files.write(tmp, json.getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING);

        try {
            Files.move(tmp, targetFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tmp, targetFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public String loadJson() throws IOException {
        if (!Files.exists(targetFile)) return null;
        byte[] bytes = Files.readAllBytes(targetFile);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
