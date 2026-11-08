package com.Clinivora.repository;

import com.Clinivora.model.Patient;
import com.Clinivora.util.DateTimeUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PatientRepository {
    private static final String SELECT_COLS = """
            id, display_code, first_name, last_name, date_of_birth, phone, gender,
            national_id, address, emergency_contact, active, created_at, updated_at
            """;

    public Optional<Patient> findById(long id) throws SQLException {
        String sql = "SELECT " + SELECT_COLS + " FROM patient WHERE id = ?";
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

    public Optional<Patient> findByDisplayCode(String displayCode) throws SQLException {
        String sql = "SELECT " + SELECT_COLS + " FROM patient WHERE display_code = ?";
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setString(1, displayCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Patient> searchByName(String query, boolean includeInactive) throws SQLException {
        String sql = """
                SELECT %s FROM patient
                WHERE lower(first_name || ' ' || last_name) LIKE lower(?)
                %s
                ORDER BY last_name, first_name
                """.formatted(SELECT_COLS, includeInactive ? "" : "AND active = 1");
        List<Patient> patients = new ArrayList<>();
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setString(1, "%" + query + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    patients.add(map(rs));
                }
            }
        }
        return patients;
    }

    public List<Patient> findAll(boolean includeInactive) throws SQLException {
        String sql = "SELECT " + SELECT_COLS + " FROM patient "
                + (includeInactive ? "" : "WHERE active = 1 ")
                + "ORDER BY id DESC";
        List<Patient> patients = new ArrayList<>();
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                patients.add(map(rs));
            }
        }
        return patients;
    }

    public List<Patient> findAll() throws SQLException {
        return findAll(false);
    }

    public List<Patient> searchByName(String query) throws SQLException {
        return searchByName(query, false);
    }

    public long insert(Patient patient) throws SQLException {
        String sql = """
                INSERT INTO patient(display_code, first_name, last_name, date_of_birth, phone, gender,
                                    national_id, address, emergency_contact, active, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql, JdbcSupport.RETURN_GENERATED)) {
            ps.setString(1, patient.getDisplayCode());
            ps.setString(2, patient.getFirstName());
            ps.setString(3, patient.getLastName());
            JdbcSupport.setNullableString(ps, 4, DateTimeUtil.formatDate(patient.getDateOfBirth()));
            JdbcSupport.setNullableString(ps, 5, patient.getPhone());
            JdbcSupport.setNullableString(ps, 6, patient.getGender());
            JdbcSupport.setNullableString(ps, 7, patient.getNationalId());
            JdbcSupport.setNullableString(ps, 8, patient.getAddress());
            JdbcSupport.setNullableString(ps, 9, patient.getEmergencyContact());
            ps.setInt(10, patient.isActive() ? 1 : 0);
            ps.setString(11, DateTimeUtil.formatDateTime(patient.getCreatedAt()));
            ps.setString(12, DateTimeUtil.formatDateTime(patient.getUpdatedAt()));
            return JdbcSupport.executeInsert(ps);
        }
    }

    public void update(Patient patient) throws SQLException {
        String sql = """
                UPDATE patient
                SET first_name = ?, last_name = ?, date_of_birth = ?, phone = ?, gender = ?,
                    national_id = ?, address = ?, emergency_contact = ?, updated_at = ?
                WHERE id = ?
                """;
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setString(1, patient.getFirstName());
            ps.setString(2, patient.getLastName());
            JdbcSupport.setNullableString(ps, 3, DateTimeUtil.formatDate(patient.getDateOfBirth()));
            JdbcSupport.setNullableString(ps, 4, patient.getPhone());
            JdbcSupport.setNullableString(ps, 5, patient.getGender());
            JdbcSupport.setNullableString(ps, 6, patient.getNationalId());
            JdbcSupport.setNullableString(ps, 7, patient.getAddress());
            JdbcSupport.setNullableString(ps, 8, patient.getEmergencyContact());
            ps.setString(9, DateTimeUtil.formatDateTime(DateTimeUtil.now()));
            ps.setLong(10, patient.getId());
            ps.executeUpdate();
        }
    }

    public void setActive(long id, boolean active) throws SQLException {
        String sql = "UPDATE patient SET active = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setInt(1, active ? 1 : 0);
            ps.setString(2, DateTimeUtil.formatDateTime(DateTimeUtil.now()));
            ps.setLong(3, id);
            ps.executeUpdate();
        }
    }

    public void updateDisplayCode(long id, String displayCode) throws SQLException {
        String sql = "UPDATE patient SET display_code = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setString(1, displayCode);
            ps.setString(2, DateTimeUtil.formatDateTime(DateTimeUtil.now()));
            ps.setLong(3, id);
            ps.executeUpdate();
        }
    }

    public long count() throws SQLException {
        try (PreparedStatement ps = JdbcSupport.connection()
                .prepareStatement("SELECT COUNT(*) FROM patient WHERE active = 1");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private Patient map(ResultSet rs) throws SQLException {
        Patient patient = new Patient();
        patient.setId(rs.getLong("id"));
        patient.setDisplayCode(rs.getString("display_code"));
        patient.setFirstName(rs.getString("first_name"));
        patient.setLastName(rs.getString("last_name"));
        patient.setDateOfBirth(DateTimeUtil.parseDate(rs.getString("date_of_birth")));
        patient.setPhone(rs.getString("phone"));
        patient.setGender(rs.getString("gender"));
        patient.setNationalId(rs.getString("national_id"));
        patient.setAddress(rs.getString("address"));
        patient.setEmergencyContact(rs.getString("emergency_contact"));
        try {
            patient.setActive(rs.getInt("active") == 1);
        } catch (SQLException ex) {
            patient.setActive(true);
        }
        patient.setCreatedAt(DateTimeUtil.parseDateTime(rs.getString("created_at")));
        patient.setUpdatedAt(DateTimeUtil.parseDateTime(rs.getString("updated_at")));
        return patient;
    }
}
