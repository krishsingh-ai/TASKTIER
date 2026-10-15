package com.tasktier.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for date and time calculations and conversions.
 * Leverages the modern Java Time API (LocalDateTime, Duration).
 */
public final class DateUtil {

    public static final DateTimeFormatter DB_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    public static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private DateUtil() {
        // Prevent instantiation
    }

    /**
     * Formats a LocalDateTime into a database-compatible text format.
     *
     * @param dateTime the LocalDateTime to format
     * @return formatted string or null if input is null
     */
    public static String formatForDb(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DB_FORMATTER);
    }

    /**
     * Formats a LocalDateTime into a user-friendly display format.
     *
     * @param dateTime the LocalDateTime to format
     * @return formatted display string or empty string if input is null
     */
    public static String formatForDisplay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DISPLAY_FORMATTER);
    }

    /**
     * Parses a date-time string from either DB format, ISO format, or HTML5 datetime-local format.
     *
     * @param dateString date string to parse
     * @return parsed LocalDateTime or null if invalid/null
     */
    public static LocalDateTime parse(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        String trimmed = dateString.trim();
        try {
            if (trimmed.contains("T")) {
                if (trimmed.length() == 16) {
                    return LocalDateTime.parse(trimmed, INPUT_FORMATTER);
                }
                return LocalDateTime.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
            if (trimmed.length() == 19) {
                return LocalDateTime.parse(trimmed, DB_FORMATTER);
            }
            if (trimmed.length() == 10) {
                return java.time.LocalDate.parse(trimmed).atTime(23, 59, 0);
            }
            return LocalDateTime.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception exception) {
            AppLogger.warn(DateUtil.class, "Failed to parse date string: " + trimmed);
            return null;
        }
    }

    /**
     * Computes remaining hours until a deadline from the current moment.
     * Negative values indicate overdue.
     *
     * @param deadline the target deadline
     * @return hours remaining, or 999.0 if deadline is null
     */
    public static double hoursUntil(LocalDateTime deadline) {
        if (deadline == null) {
            return 999.0;
        }
        Duration duration = Duration.between(LocalDateTime.now(), deadline);
        return duration.toMinutes() / 60.0;
    }

    /**
     * Computes duration in minutes between two timestamps.
     *
     * @param start start timestamp
     * @param end end timestamp
     * @return elapsed minutes, or 0 if either is null
     */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return Math.max(0, Duration.between(start, end).toMinutes());
    }
}
