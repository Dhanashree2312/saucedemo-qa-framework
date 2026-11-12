package com.qa.framework.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

public class ConfigManager {

    private static final String ENVIRONMENT =
            System.getProperty("env", "qa")
                    .trim()
                    .toLowerCase(Locale.ROOT);

    private static final Properties frameworkProperties =
            loadProperties("/config/framework.properties");

    private static final Properties environmentProperties =
            loadProperties(
                    "/config/environments/" + ENVIRONMENT + ".properties"
            );

    private ConfigManager() {
        // Prevent object creation; all configuration methods are static.
    }

    private static Properties loadProperties(String path) {
        Properties loadedProperties = new Properties();

        try (InputStream input =
                     ConfigManager.class.getResourceAsStream(path)) {

            if (input == null) {
                throw new IllegalStateException(
                        "Unable to locate configuration file: " + path
                );
            }

            loadedProperties.load(input);
            return loadedProperties;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to load configuration file: " + path,
                    e
            );
        }
    }

    public static String getProperty(String key) {
        // Environment-specific values take priority.
        String value = environmentProperties.getProperty(key);

        // Fall back to shared framework settings.
        if (value == null || value.isBlank()) {
            value = frameworkProperties.getProperty(key);
        }

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing or blank configuration property: " + key
            );
        }

        return value.trim();
    }

    public static boolean getBooleanProperty(String key) {
        String value = getProperty(key);

        if (!value.equalsIgnoreCase("true")
                && !value.equalsIgnoreCase("false")) {
            throw new IllegalStateException(
                    "Configuration property '" + key
                            + "' must be true or false"
            );
        }

        return Boolean.parseBoolean(value);
    }

    public static int getIntProperty(String key) {
        String value = getProperty(key);

        try {
            int result = Integer.parseInt(value);

            if (result <= 0) {
                throw new IllegalStateException(
                        "Configuration property '" + key
                                + "' must be greater than zero"
                );
            }

            return result;

        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "Configuration property '" + key
                            + "' must be a valid integer",
                    e
            );
        }
    }

    public static String getBrowser() {
        String browser = System.getProperty("browser", "chromium")
                .trim()
                .toLowerCase(Locale.ROOT);

        if (!browser.equals("chromium")
                && !browser.equals("firefox")
                && !browser.equals("webkit")) {
            throw new IllegalArgumentException(
                    "Unsupported browser: " + browser
            );
        }

        return browser;
    }

    public static boolean isHeadless() {
        String headless = System.getProperty("headless", "true").trim();

        if (!headless.equalsIgnoreCase("true")
                && !headless.equalsIgnoreCase("false")) {
            throw new IllegalArgumentException(
                    "Headless value must be either true or false"
            );
        }

        return Boolean.parseBoolean(headless);
    }

    public static String getEnvironment() {
        return ENVIRONMENT;
    }
}
