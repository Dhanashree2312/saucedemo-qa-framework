package com.qa.framework.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {

    private static final Properties properties = new Properties();
    static{try(InputStream input = ConfigManager.class.getResourceAsStream("/config/qa.properties"))

    {
        if (input == null) {
            throw new RuntimeException("Sorry, I'm unable to locate qa.properties file");
        }
        properties.load(input);
    }catch(
    IOException e){
        throw new RuntimeException(
                "Failed to load config.properties",
                e
        );

    }
}
    public static String getProperty(String key){
        String value = properties.getProperty(key);
        if(value ==null || value.isBlank()){
            throw new IllegalStateException(
                    "Missing or blank configuration property: " + key
            );        }
        return value.trim();
    }

    public static Boolean getBooleanProperty(String key){
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

}
