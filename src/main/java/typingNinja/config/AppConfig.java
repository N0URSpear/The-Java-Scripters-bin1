package typingNinja.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Centralised configuration loader that prefers environment variables
 * but falls back to packaged properties so the packaged app retains
 * its defaults.
 */
public final class AppConfig {
    private static final String RESOURCE_NAME = "/typingninja-config.properties";
    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream in = AppConfig.class.getResourceAsStream(RESOURCE_NAME)) {
            if (in != null) {
                PROPERTIES.load(in);
            } else {
                System.err.println("[Config] Resource not found: " + RESOURCE_NAME);
            }
        } catch (IOException e) {
            System.err.println("[Config] Failed to load " + RESOURCE_NAME + ": " + e.getMessage());
        }
    }

    private AppConfig() {
    }

    public static String get(String key) {
        String env = System.getenv(key);
        if (env != null) {
            env = env.trim();
            if (!env.isEmpty()) {
                return env;
            }
        }
        String prop = PROPERTIES.getProperty(key);
        return prop == null ? null : prop.trim();
    }

    public static String getOrDefault(String key, String defaultValue) {
        String value = get(key);
        return (value == null || value.isEmpty()) ? defaultValue : value;
    }

    public static int getInt(String key, int defaultValue) {
        String value = get(key);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            System.err.println("[Config] Invalid integer for key " + key + ": '" + value + "'");
            return defaultValue;
        }
    }
}

