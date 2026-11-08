package com.Clinivora.repository;

import com.Clinivora.model.CaseNote;
import com.Clinivora.util.DateTimeUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CaseNoteRepository {

    public long insert(CaseNote note) throws SQLException {
        String sql = """
                INSERT INTO case_note(case_id, body, created_by, created_at)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql, JdbcSupport.RETURN_GENERATED)) {
            ps.setLong(1, note.getCaseId());
            ps.setString(2, note.getBody());
            JdbcSupport.setNullableLong(ps, 3, note.getCreatedBy());
            ps.setString(4, DateTimeUtil.formatDateTime(note.getCreatedAt()));
            return JdbcSupport.executeInsert(ps);
        }
    }

    public List<CaseNote> findByCaseId(long caseId) throws SQLException {
        String sql = """
                SELECT id, case_id, body, created_by, created_at
                FROM case_note WHERE case_id = ? ORDER BY created_at, id
                """;
        List<CaseNote> notes = new ArrayList<>();
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setLong(1, caseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    notes.add(map(rs));
                }
            }
        }
        return notes;
    }

    private CaseNote map(ResultSet rs) throws SQLException {
        CaseNote note = new CaseNote();
        note.setId(rs.getLong("id"));
        note.setCaseId(rs.getLong("case_id"));
        note.setBody(rs.getString("body"));
        long createdBy = rs.getLong("created_by");
        note.setCreatedBy(rs.wasNull() ? null : createdBy);
        note.setCreatedAt(DateTimeUtil.parseDateTime(rs.getString("created_at")));
        return note;
    }
}
