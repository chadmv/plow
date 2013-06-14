package com.breakersoft.plow;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PlowCfg {

    private static final Logger logger = LoggerFactory.getLogger(PlowCfg.class);

    private static final String PROPERTIES_FILE = "plow.properties";

    private final Properties props = new Properties();

    public PlowCfg() throws IOException {

        String override = System.getProperty("plow.cfg.path");
        if (override != null) {
            props.load(new FileInputStream(override));
        }
        else {
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            if (cl != null) {
                URL url = cl.getResource(PROPERTIES_FILE);
                if (null == url) {
                    url = cl.getResource("/" + PROPERTIES_FILE);
                }
                if (null != url) {
                    try {
                        InputStream in = url.openStream();
                        props.load(in);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
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
