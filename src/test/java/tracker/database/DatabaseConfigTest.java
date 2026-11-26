package tracker.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tracker.exception.TrackerException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseConfigTest {

    @TempDir
    Path directory;

    private Path writeConfig(String content) throws IOException {
        Path file = directory.resolve("config.properties");
        Files.writeString(file, content);
        return file;
    }

    @Test
    void readsValuesFromFile() throws IOException {
        Path file = writeConfig("""
                db.url=jdbc:mysql://localhost:3306/impulso
                db.user=root
                db.password=secreto
                """);

        DatabaseConfig config = DatabaseConfig.load(file, Map.of());

        assertEquals("jdbc:mysql://localhost:3306/impulso", config.url());
        assertEquals("root", config.user());
        assertEquals("secreto", config.password());
    }

    @Test
    void environmentOverridesFile() throws IOException {
        Path file = writeConfig("""
                db.url=jdbc:mysql://localhost:3306/impulso
                db.user=root
                db.password=secreto
                """);
        Map<String, String> environment = Map.of(
                DatabaseConfig.ENV_URL, "jdbc:mysql://servidor:3306/otra",
                DatabaseConfig.ENV_PASSWORD, "otra");

        DatabaseConfig config = DatabaseConfig.load(file, environment);

        assertEquals("jdbc:mysql://servidor:3306/otra", config.url());
        assertEquals("root", config.user());
        assertEquals("otra", config.password());
    }

    @Test
    void worksWithoutFileWhenEnvironmentIsComplete() {
        Map<String, String> environment = Map.of(
                DatabaseConfig.ENV_URL, "jdbc:mysql://localhost:3306/impulso",
                DatabaseConfig.ENV_USER, "impulso");

        DatabaseConfig config = DatabaseConfig.load(directory.resolve("no-existe.properties"), environment);

        assertEquals("impulso", config.user());
        assertEquals("", config.password());
    }

    @Test
    void missingUrlOrUserFailsWithClearMessage() throws IOException {
        Path file = writeConfig("db.url=jdbc:mysql://localhost:3306/impulso\n");

        TrackerException error = assertThrows(TrackerException.class,
                () -> DatabaseConfig.load(file, Map.of()));

        assertTrue(error.getMessage().contains("config.properties"));
    }
}
