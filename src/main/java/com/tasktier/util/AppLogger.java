package com.tasktier.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centralized logging utility for the TaskTier application.
 * Ensures consistent application-wide logging without System.out.println.
 */
public final class AppLogger {

    private AppLogger() {
        // Prevent instantiation
    }

    /**
     * Obtains a Logger configured for the specified class.
     *
     * @param clazz the class requesting the logger
     * @return configured Logger instance
     */
    public static Logger getLogger(Class<?> clazz) {
        return Logger.getLogger(clazz.getName());
    }

    /**
     * Logs an informational message.
     *
     * @param clazz the source class
     * @param message the message to log
     */
    public static void info(Class<?> clazz, String message) {
        Logger.getLogger(clazz.getName()).log(Level.INFO, message);
    }

    /**
     * Logs a warning message.
     *
     * @param clazz the source class
     * @param message the warning message
     */
    public static void warn(Class<?> clazz, String message) {
        Logger.getLogger(clazz.getName()).log(Level.WARNING, message);
    }

    /**
     * Logs an error message and associated throwable.
     *
     * @param clazz the source class
     * @param message error description
     * @param throwable exception or error caught
     */
    public static void error(Class<?> clazz, String message, Throwable throwable) {
        Logger.getLogger(clazz.getName()).log(Level.SEVERE, message, throwable);
    }
}
