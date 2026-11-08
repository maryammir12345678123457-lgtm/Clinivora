package com.Clinivora.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseManager {
    private static volatile DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            Path dataDir = AppConfig.getDataDirectory();
            try {
                Files.createDirectories(dataDir);
            } catch (Exception e) {
                throw new SQLException("Unable to create data directory: " + dataDir, e);
            }
            connection = DriverManager.getConnection(AppConfig.getJdbcUrl());
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
            }
        }
        return connection;
    }

    public synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
                // no-op
            } finally {
                connection = null;
            }
        }
    }

    /** Test helper to reset singleton between tests. */
    public static synchronized void resetForTests() {
        if (instance != null) {
            instance.close();
        }
        instance = null;
    }
}
