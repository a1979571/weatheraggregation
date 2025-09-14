package assignment2;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * PersistenceManager
 *
 * Handles safe reading and writing of JSON data to a file.
 * Ensures atomic writes to prevent data loss during crashes.
 */
public class PersistenceManager {

    private final Path targetFile;

    /**
     * Constructor.
     *
     * @param targetFile Path to the JSON file for persistence
     */
    public PersistenceManager(Path targetFile) {
        this.targetFile = targetFile;
    }

    /**
     * Save JSON string to the target file atomically.
     *
     * @param json JSON content to save
     * @throws IOException if the write fails
     */
    public void saveJson(String json) throws IOException {
        // Create temporary file in the same directory
        Path dir = targetFile.getParent() != null ? targetFile.getParent() : Paths.get(".");
        Path tmp = Files.createTempFile(dir, "weather", ".tmp");

        // Write JSON to temporary file
        Files.writeString(tmp, json, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);

        // Atomically move temporary file to target file
        try {
            Files.move(tmp, targetFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            // Fallback if atomic move is not supported
            Files.move(tmp, targetFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Load JSON content from the target file.
     *
     * @return JSON string, or null if the file does not exist
     * @throws IOException if reading fails
     */
    public String loadJson() throws IOException {
        if (!Files.exists(targetFile)) {
            return null;
        }
        return Files.readString(targetFile, StandardCharsets.UTF_8);
    }
}
