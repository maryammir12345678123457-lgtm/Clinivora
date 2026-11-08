package com.Clinivora.repository;

import com.Clinivora.model.AppUser;
import com.Clinivora.model.enums.UserRole;
import com.Clinivora.util.DateTimeUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AppUserRepository {

    public Optional<AppUser> findByUsername(String username) throws SQLException {
        String sql = """
                SELECT id, username, password_hash, salt, role, display_name, active, created_at, updated_at
                FROM app_user WHERE username = ?
                """;
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<AppUser> findById(long id) throws SQLException {
        String sql = """
                SELECT id, username, password_hash, salt, role, display_name, active, created_at, updated_at
                FROM app_user WHERE id = ?
                """;
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<AppUser> findAll() throws SQLException {
        String sql = """
                SELECT id, username, password_hash, salt, role, display_name, active, created_at, updated_at
                FROM app_user ORDER BY username
                """;
        List<AppUser> users = new ArrayList<>();
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(map(rs));
            }
        }
        return users;
    }

    public long insert(AppUser user) throws SQLException {
        String sql = """
                INSERT INTO app_user(username, password_hash, salt, role, display_name, active, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql, JdbcSupport.RETURN_GENERATED)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getSalt());
            ps.setString(4, user.getRole().name());
            ps.setString(5, user.getDisplayName());
            ps.setInt(6, user.isActive() ? 1 : 0);
            ps.setString(7, DateTimeUtil.formatDateTime(user.getCreatedAt()));
            ps.setString(8, DateTimeUtil.formatDateTime(user.getUpdatedAt()));
            return JdbcSupport.executeInsert(ps);
        }
    }

    public boolean existsByUsername(String username) throws SQLException {
        String sql = "SELECT 1 FROM app_user WHERE username = ? LIMIT 1";
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void updatePassword(long id, String passwordHash, String salt) throws SQLException {
        String sql = "UPDATE app_user SET password_hash = ?, salt = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setString(2, salt);
            ps.setString(3, DateTimeUtil.formatDateTime(DateTimeUtil.now()));
            ps.setLong(4, id);
            ps.executeUpdate();
        }
    }

    public void setActive(long id, boolean active) throws SQLException {
        String sql = "UPDATE app_user SET active = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setInt(1, active ? 1 : 0);
            ps.setString(2, DateTimeUtil.formatDateTime(DateTimeUtil.now()));
            ps.setLong(3, id);
            ps.executeUpdate();
        }
    }

    public void updateProfile(long id, String displayName, UserRole role) throws SQLException {
        String sql = "UPDATE app_user SET display_name = ?, role = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setString(1, displayName);
            ps.setString(2, role.name());
            ps.setString(3, DateTimeUtil.formatDateTime(DateTimeUtil.now()));
            ps.setLong(4, id);
            ps.executeUpdate();
        }
    }

    private AppUser map(ResultSet rs) throws SQLException {
        AppUser user = new AppUser();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setSalt(rs.getString("salt"));
        user.setRole(UserRole.fromString(rs.getString("role")));
        user.setDisplayName(rs.getString("display_name"));
        user.setActive(rs.getInt("active") == 1);
        user.setCreatedAt(DateTimeUtil.parseDateTime(rs.getString("created_at")));
        user.setUpdatedAt(DateTimeUtil.parseDateTime(rs.getString("updated_at")));
        return user;
    }
}
