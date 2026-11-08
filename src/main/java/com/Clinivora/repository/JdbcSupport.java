package com.Clinivora.repository;

import com.Clinivora.config.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

final class JdbcSupport {
    private JdbcSupport() {
    }

    static Connection connection() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    static void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setLong(index, value);
        }
    }

    static void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    static void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value);
        }
    }

    static long executeInsert(PreparedStatement ps) throws SQLException {
        int updated = ps.executeUpdate();
        if (updated == 0) {
            throw new SQLException("Insert failed, no rows affected");
        }
        try (ResultSet keys = ps.getGeneratedKeys()) {
            if (keys.next()) {
                return keys.getLong(1);
            }
        }
        throw new SQLException("Insert failed, no generated key");
    }

    static int RETURN_GENERATED = Statement.RETURN_GENERATED_KEYS;
}
