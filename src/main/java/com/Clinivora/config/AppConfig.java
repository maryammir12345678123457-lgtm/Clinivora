package com.Clinivora.config;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class AppConfig {
    public static final String APP_NAME = "Clinivora";
    public static final String PACKAGE_ROOT = "com.Clinivora";
    public static final String DB_FILE_NAME = "Clinivora.db";
    public static final String SCHEMA_RESOURCE = "/db/schema.sql";

    private static Path dataDirectory = Paths.get("data");

    private AppConfig() {
    }

    public static Path getDataDirectory() {
        return dataDirectory;
    }

    public static void setDataDirectory(Path directory) {
        if (directory == null) {
            throw new IllegalArgumentException("Data directory cannot be null");
        }
        dataDirectory = directory;
    }

    public static Path getDatabasePath() {
        return dataDirectory.resolve(DB_FILE_NAME);
    }

    public static String getJdbcUrl() {
        return "jdbc:sqlite:" + getDatabasePath().toAbsolutePath();
    }
}
