package com.breakersoft.plow.util;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.UUID;
import java.util.regex.Pattern;

import org.slf4j.Logger;

import com.breakersoft.plow.Defaults;

public final class PlowUtils {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(PlowUtils.class);

    public static int getReservedRam(int totalRam) {
        return Math.min((int) (totalRam * Defaults.NODE_RESERVE_MEMORY_PERCENT), Defaults.NODE_RESERVE_MEMORY_MAX);
    }

    public static boolean isUuid(String s) {
        try {
            UUID.fromString(s);
            return true;
        }
        catch (IllegalArgumentException e) {

        }
        return false;
    }

    public static boolean isValid(Collection<?> c) {
        if (c == null) {
            return false;
        }
        return !c.isEmpty();
    }

    public static boolean isValid(String s) {
        if (s == null) {
            return false;
        }
        return !s.isEmpty();
    }

    public static final Pattern ALPHA_NUM = Pattern.compile("[\\w\\.\\-]+");

    public static void alpahNumCheck(String str, String errMsg) {

        if (str == null) {
            throw new IllegalArgumentException(errMsg);
        }

        if (str.isEmpty()) {
            throw new IllegalArgumentException(errMsg);
        }

        if (!ALPHA_NUM.matcher(str).matches()) {
            throw new IllegalArgumentException(errMsg);
        }
    }

    public static String bytesToMb(long bytes) {
        return String.format("%0.2fmb", bytes / 1024.0 / 1024.0 / 1024.0);
    }

    /**
     * Return true if the str is not null and not empty.
     * @param str
     * @return
     */
    public static String checkEmpty(String str) {
        if (str == null) {
            throw new NullPointerException("Expecting not null string");
        }
        if (str.isEmpty()) {
            throw new IllegalArgumentException("Expecting non-empty string.");
        }
        return str;
    }

    /**
     * Uniquify a collection of strings while maintaining order.
     * @param c
     * @return
     */
    public static String[] uniquify(Collection<String> c) {
        if (c == null) {
            return null;
        }
        return new LinkedHashSet<String>(c).toArray(new String[] {});
    }

    public static void logTime(long startTime, String message) {
        final double time = (System.currentTimeMillis() - startTime) / 1000.0;
        if (time > 1) {
            logger.warn("LONGTIME: {} took {}", message, (System.currentTimeMillis() - startTime) / 1000.0);
        } else {
            logger.trace("TIME: {} took {}", message, (System.currentTimeMillis() - startTime) / 1000.0);
        }
    }

    public static void logTime(long startTime, String message, boolean info) {
        final double time = (System.currentTimeMillis() - startTime) / 1000.0;
        if (time > 1) {
            logger.warn("LONGTIME: {} took {}", message, (System.currentTimeMillis() - startTime) / 1000.0);
        } else {
            logger.info("TIME: {} took {}", message, (System.currentTimeMillis() - startTime) / 1000.0);
        }
    }
}
