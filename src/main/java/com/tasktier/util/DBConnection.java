package com.tasktier.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Thread-safe singleton utility for managing SQLite database connections.
 * Provides connections to the TaskTier database using the JDBC DriverManager.
 */
public final class DBConnection {

    private static final String DEFAULT_DB_URL = "jdbc:sqlite:tasktier.db";
    private static String dbUrl = DEFAULT_DB_URL;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            AppLogger.info(DBConnection.class, "SQLite JDBC Driver loaded successfully.");
        } catch (ClassNotFoundException exception) {
            AppLogger.error(DBConnection.class, "Failed to load SQLite JDBC driver", exception);
            throw new RuntimeException("SQLite JDBC driver not found on classpath", exception);
        }
    }

    private DBConnection() {
        // Prevent instantiation
    }

    /**
     * Retrieves an active JDBC Connection to the SQLite database.
     *
     * @return a valid Connection instance
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(dbUrl);
        // Enable foreign key constraints in SQLite
        try (var statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON;");
        }
        return connection;
    }

    /**
     * Sets the database connection URL (primarily used for configuration or testing).
     *
     * @param url new JDBC URL
     */
    public static synchronized void setDatabaseUrl(String url) {
        if (url != null && !url.trim().isEmpty()) {
            dbUrl = url;
            AppLogger.info(DBConnection.class, "Database URL set to: " + dbUrl);
        }
    }

    /**
     * Returns the active database connection URL.
     *
     * @return connection URL string
     */
    public static synchronized String getDatabaseUrl() {
        return dbUrl;
    }
}
