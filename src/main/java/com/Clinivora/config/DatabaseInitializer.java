package com.Clinivora.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DatabaseInitializer {
    private DatabaseInitializer() {
    }

    public static void initialize() throws SQLException {
        Connection connection = DatabaseManager.getInstance().getConnection();
        // 1) Create missing tables / safe indexes from schema.sql
        // 2) Migrate older DBs (add patient.active / doctor.active, etc.)
        // Indexes that depend on migrated columns are created only in migrate().
        executeScript(connection, loadSchemaScript());
        migrate(connection);
    }

    /**
     * Additive migrations for existing databases. Never drops or wipes data.
     */
    public static void migrate(Connection connection) throws SQLException {
        addColumnIfMissing(connection, "patient", "active", "INTEGER NOT NULL DEFAULT 1");
        addColumnIfMissing(connection, "doctor", "active", "INTEGER NOT NULL DEFAULT 1");
        ensureCaseNoteTable(connection);
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE INDEX IF NOT EXISTS idx_patient_active ON patient(active)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_doctor_active ON doctor(active)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_case_note_case ON case_note(case_id)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_audit_action ON audit_log(action)");
        }
    }

    private static void ensureCaseNoteTable(Connection connection) throws SQLException {
        if (tableExists(connection, "case_note")) {
            return;
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE case_note (
                        id              INTEGER PRIMARY KEY AUTOINCREMENT,
                        case_id         INTEGER NOT NULL REFERENCES emergency_case(id),
                        body            TEXT    NOT NULL,
                        created_by      INTEGER REFERENCES app_user(id),
                        created_at      TEXT    NOT NULL
                    )
                    """);
        }
    }

    private static void addColumnIfMissing(Connection connection,
                                           String table,
                                           String column,
                                           String definition) throws SQLException {
        if (columnExists(connection, table, column)) {
            return;
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        }
    }

    private static boolean tableExists(Connection connection, String table) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(
                     "SELECT name FROM sqlite_master WHERE type='table' AND name='" + table + "'")) {
            return rs.next();
        }
    }

    private static boolean columnExists(Connection connection, String table, String column) throws SQLException {
        Set<String> columns = new HashSet<>();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) {
                columns.add(rs.getString("name").toLowerCase());
            }
        }
        return columns.contains(column.toLowerCase());
    }

    private static String loadSchemaScript() {
        try (InputStream in = DatabaseInitializer.class.getResourceAsStream(AppConfig.SCHEMA_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Schema resource missing: " + AppConfig.SCHEMA_RESOURCE);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append('\n');
                }
                return builder.toString();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load schema.sql", e);
        }
    }

    private static void executeScript(Connection connection, String script) throws SQLException {
        List<String> statements = splitStatements(script);
        try (Statement statement = connection.createStatement()) {
            for (String sql : statements) {
                if (sql.isBlank()) {
                    continue;
                }
                try {
                    statement.execute(sql);
                } catch (SQLException ex) {
                    // Older DBs skip CREATE TABLE IF NOT EXISTS, then CREATE INDEX on a
                    // newly-added column can fail before migrate() runs. Defer those.
                    String message = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
                    boolean indexOnMissingColumn = sql.toUpperCase().contains("CREATE INDEX")
                            && message.contains("no such column");
                    if (!indexOnMissingColumn) {
                        throw ex;
                    }
                }
            }
        }
    }

    private static List<String> splitStatements(String script) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String rawLine : script.split("\n")) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("--")) {
                continue;
            }
            current.append(line).append(' ');
            if (line.endsWith(";")) {
                String sql = current.toString().trim();
                if (sql.endsWith(";")) {
                    sql = sql.substring(0, sql.length() - 1).trim();
                }
                statements.add(sql);
                current.setLength(0);
            }
        }
        if (!current.isEmpty()) {
            statements.add(current.toString().trim());
        }
        return statements;
    }
}
