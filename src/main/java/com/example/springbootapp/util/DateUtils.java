package com.example.springbootapp.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for date operations
 * This demonstrates the shared utilities structure
 */
public class DateUtils {
    
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Format current date time using default format
     * @return formatted current date time
     */
    public static String getCurrentDateTimeFormatted() {
        return LocalDateTime.now().format(DEFAULT_FORMATTER);
    }
    
    /**
     * Format given date time using default format
     * @param dateTime the date time to format
     * @return formatted date time
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DEFAULT_FORMATTER);
    }
    
    /**
     * Check if a date time is in the past
     * @param dateTime the date time to check
     * @return true if the date time is in the past
     */
    public static boolean isInPast(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.isBefore(LocalDateTime.now());
    }
    
    /**
     * Check if a date time is in the future
     * @param dateTime the date time to check
     * @return true if the date time is in the future
     */
    public static boolean isInFuture(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.isAfter(LocalDateTime.now());
    }
}
