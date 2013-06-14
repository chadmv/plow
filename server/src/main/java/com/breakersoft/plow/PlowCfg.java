package com.breakersoft.plow;

import java.io.IOException;
import java.util.Properties;

import org.springframework.stereotype.Component;

@Component
public class PlowCfg {

    private static final String PROPERTIES_FILE = "plow.properties";

    private final Properties props = new Properties();

    public PlowCfg() throws IOException {
        props.load(getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE));
    }

    public String get(String key) {
        return props.getProperty(key);
    }

    public String get(String key, String defaultValue) {
        String value = props.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public int get(String key, int defaultValue) {
        String value = props.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Integer.parseInt(value.trim());
    }

    public boolean get(String key, boolean defaultValue) {
        String value = props.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }

    public long get(String key, long defaultValue) {
        String value = props.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Long.parseLong(value.trim());
    }
}
