package com.restfulbooker.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads framework configuration.
 *
 * <p>Lookup order for every key: environment variable {@code RB_<KEY>}
 * (dots replaced by underscores), JVM system property, then the property
 * files on the classpath ({@code config.local.properties} overrides
 * {@code config.properties}).
 */
public final class ConfigReader {

    private static final String ENV_PREFIX = "RB_";
    private static final Properties PROPERTIES = loadProperties();

    private ConfigReader() {
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        loadInto(properties, "config.properties", true);
        loadInto(properties, "config.local.properties", false);
        return properties;
    }

    private static void loadInto(Properties target, String fileName, boolean mandatory) {
        try (InputStream stream = ConfigReader.class.getClassLoader().getResourceAsStream(fileName)) {
            if (stream == null) {
                if (mandatory) {
                    throw new IllegalStateException(fileName + " was not found on the classpath");
                }
                return;
            }
            target.load(stream);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read " + fileName, e);
        }
    }

    /** Returns the value for the key, or an empty string when nothing is configured. */
    public static String get(String key) {
        String envValue = System.getenv(ENV_PREFIX + key.toUpperCase().replace('.', '_'));
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue.trim();
        }
        return PROPERTIES.getProperty(key, "").trim();
    }

    public static int getInt(String key) {
        String value = get(key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Configuration key '" + key + "' must be a number but was '" + value + "'", e);
        }
    }

    public static String getBaseUrl() {
        return get("base.url");
    }

    public static int getConnectionTimeout() {
        return getInt("connection.timeout");
    }

    public static int getResponseTimeout() {
        return getInt("response.timeout");
    }

    public static long getResponseTimeThreshold() {
        return getInt("response.time.threshold");
    }

    public static String getUsername() {
        return requireCredential("username", "RB_USERNAME");
    }

    public static String getPassword() {
        return requireCredential("password", "RB_PASSWORD");
    }

    private static String requireCredential(String key, String envName) {
        String value = get(key);
        if (value.isEmpty()) {
            throw new IllegalStateException("No '" + key + "' configured. Set the " + envName
                    + " environment variable (or add " + key + "=... to src/main/resources/config.local.properties). "
                    + "See the Configuration section of README.md.");
        }
        return value;
    }
}
