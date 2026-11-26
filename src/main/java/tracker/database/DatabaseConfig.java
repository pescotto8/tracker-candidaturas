package tracker.database;

import tracker.exception.TrackerException;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

/**
 * Datos de conexión a la base de datos.
 *
 * @param url URL JDBC de la base de datos
 * @param user usuario de la base de datos
 * @param password contraseña del usuario (puede estar vacía)
 */
public record DatabaseConfig(String url, String user, String password) {

    /** Archivo de configuración que se busca en el directorio de trabajo. */
    public static final String CONFIG_FILE = "config.properties";

    static final String ENV_URL = "IMPULSO_DB_URL";
    static final String ENV_USER = "IMPULSO_DB_USER";
    static final String ENV_PASSWORD = "IMPULSO_DB_PASSWORD";

    /**
     * Carga la configuración de {@code config.properties} y de las variables de entorno.
     *
     * @return configuración leída
     * @throws TrackerException si falta la URL o el usuario
     */
    public static DatabaseConfig load() {
        return load(Path.of(CONFIG_FILE), System.getenv());
    }

    /**
     * Carga la configuración de un archivo y de unas variables de entorno.
     * Las variables de entorno tienen prioridad sobre el archivo.
     *
     * @param file archivo de propiedades; puede no existir
     * @param environment variables de entorno
     * @return configuración leída
     * @throws TrackerException si falta la URL o el usuario, o si el archivo no se puede leer
     */
    static DatabaseConfig load(Path file, Map<String, String> environment) {
        Properties properties = readProperties(file);
        String url = firstNonBlank(environment.get(ENV_URL), properties.getProperty("db.url"));
        String user = firstNonBlank(environment.get(ENV_USER), properties.getProperty("db.user"));
        String password = firstNonBlank(environment.get(ENV_PASSWORD), properties.getProperty("db.password"));
        if (url == null || user == null) {
            throw new TrackerException("Falta la configuración de la base de datos. Copia "
                    + CONFIG_FILE + ".example como " + CONFIG_FILE + " o define " + ENV_URL + ", "
                    + ENV_USER + " e " + ENV_PASSWORD);
        }
        return new DatabaseConfig(url, user, password == null ? "" : password);
    }

    private static Properties readProperties(Path file) {
        Properties properties = new Properties();
        if (Files.isRegularFile(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                properties.load(reader);
            } catch (IOException e) {
                throw new TrackerException("No se pudo leer " + file + ": " + e.getMessage(), e);
            }
        }
        return properties;
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first.strip();
        }
        return second != null && !second.isBlank() ? second.strip() : null;
    }
}
