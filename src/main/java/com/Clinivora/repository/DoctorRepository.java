package com.Clinivora.repository;

import com.Clinivora.model.Doctor;
import com.Clinivora.util.DateTimeUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DoctorRepository {
    private static final String SELECT_COLS =
            "id, user_id, display_code, full_name, specialty, available, active, created_at";

    public Optional<Doctor> findById(long id) throws SQLException {
        String sql = "SELECT " + SELECT_COLS + " FROM doctor WHERE id = ?";
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

    public Optional<Doctor> findByUserId(long userId) throws SQLException {
        String sql = "SELECT " + SELECT_COLS + " FROM doctor WHERE user_id = ?";
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Doctor> findAll(boolean includeInactive) throws SQLException {
        String sql = "SELECT " + SELECT_COLS + " FROM doctor "
                + (includeInactive ? "" : "WHERE active = 1 ")
                + "ORDER BY full_name";
        List<Doctor> doctors = new ArrayList<>();
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                doctors.add(map(rs));
            }
        }
        return doctors;
    }

    public List<Doctor> findAll() throws SQLException {
        return findAll(false);
    }

    public List<Doctor> findAvailable() throws SQLException {
        String sql = "SELECT " + SELECT_COLS
                + " FROM doctor WHERE available = 1 AND active = 1 ORDER BY full_name";
        List<Doctor> doctors = new ArrayList<>();
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                doctors.add(map(rs));
            }
        }
        return doctors;
    }

    public long insert(Doctor doctor) throws SQLException {
        String sql = """
                INSERT INTO doctor(user_id, display_code, full_name, specialty, available, active, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql, JdbcSupport.RETURN_GENERATED)) {
            JdbcSupport.setNullableLong(ps, 1, doctor.getUserId());
            ps.setString(2, doctor.getDisplayCode());
            ps.setString(3, doctor.getFullName());
            JdbcSupport.setNullableString(ps, 4, doctor.getSpecialty());
            ps.setInt(5, doctor.isAvailable() ? 1 : 0);
            ps.setInt(6, doctor.isActive() ? 1 : 0);
            ps.setString(7, DateTimeUtil.formatDateTime(doctor.getCreatedAt()));
            return JdbcSupport.executeInsert(ps);
        }
    }

    public void updateDisplayCode(long id, String displayCode) throws SQLException {
        String sql = "UPDATE doctor SET display_code = ? WHERE id = ?";
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setString(1, displayCode);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    public void setAvailable(long id, boolean available) throws SQLException {
        String sql = "UPDATE doctor SET available = ? WHERE id = ?";
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setInt(1, available ? 1 : 0);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    public void setActive(long id, boolean active) throws SQLException {
        String sql = "UPDATE doctor SET active = ? WHERE id = ?";
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setInt(1, active ? 1 : 0);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    private Doctor map(ResultSet rs) throws SQLException {
        Doctor doctor = new Doctor();
        doctor.setId(rs.getLong("id"));
        long userId = rs.getLong("user_id");
        doctor.setUserId(rs.wasNull() ? null : userId);
        doctor.setDisplayCode(rs.getString("display_code"));
        doctor.setFullName(rs.getString("full_name"));
        doctor.setSpecialty(rs.getString("specialty"));
        doctor.setAvailable(rs.getInt("available") == 1);
        try {
            doctor.setActive(rs.getInt("active") == 1);
        } catch (SQLException ex) {
            doctor.setActive(true);
        }
        doctor.setCreatedAt(DateTimeUtil.parseDateTime(rs.getString("created_at")));
        return doctor;
    }
}
