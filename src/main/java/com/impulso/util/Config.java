package com.impulso.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Configuración externalizada. Orden de prioridad:
 * variable de entorno IMPULSO_* &gt; ./config.properties &gt; config.properties del classpath.
 */
public final class Config {
    private static final Properties PROPS = new Properties();

    static {
        Path local = Path.of("config.properties");
        try {
            if (Files.exists(local)) {
                try (InputStream in = Files.newInputStream(local)) {
                    PROPS.load(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
                }
            } else {
                try (InputStream in = Config.class.getResourceAsStream("/config.properties")) {
                    if (in != null) {
                        PROPS.load(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer config.properties", e);
        }
    }

    private Config() {
    }

    public static String get(String key, String def) {
        String env = System.getenv("IMPULSO_" + key.toUpperCase().replace('.', '_'));
        if (env != null && !env.isBlank()) {
            return env;
        }
        return PROPS.getProperty(key, def);
    }

    public static int getInt(String key, int def) {
        try {
            return Integer.parseInt(get(key, String.valueOf(def)).trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
