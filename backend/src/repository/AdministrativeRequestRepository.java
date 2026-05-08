package repository;

import database.DatabaseConnection;
import enums.RequestStatus;
import enums.RequestType;
import model.AdministrativeRequest;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AdministrativeRequestRepository {

    // -------------------------------------------------------------------------
    // Save a new request
    // -------------------------------------------------------------------------
    public AdministrativeRequest save(AdministrativeRequest req) throws SQLException {
        String sql = """
            INSERT INTO administrative_requests
                (student_id, type, description, status, submission_date, refusal_reason)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, req.getStudentId());
            ps.setString(2, req.getType().name());
            ps.setString(3, req.getDescription());
            ps.setString(4, req.getStatus().name());
            ps.setDate(5, Date.valueOf(req.getSubmissionDate()));
            ps.setString(6, req.getRefusalReason());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) req.setRequestId(keys.getInt(1));
            }
        }
        return req;
    }

    // -------------------------------------------------------------------------
    // Find by id
    // -------------------------------------------------------------------------
    public AdministrativeRequest findById(int requestId) throws SQLException {
        String sql = """
            SELECT ar.request_id, ar.student_id, u.full_name AS student_name,
                   ar.type, ar.description, ar.status,
                   ar.submission_date, ar.refusal_reason
            FROM administrative_requests ar
            JOIN students s ON s.student_id = ar.student_id
            JOIN users u ON u.user_id = s.user_id
            WHERE ar.request_id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Student's own requests
    // -------------------------------------------------------------------------
    public List<AdministrativeRequest> findByStudentId(int studentId) throws SQLException {
        List<AdministrativeRequest> list = new ArrayList<>();
        String sql = """
            SELECT ar.request_id, ar.student_id, u.full_name AS student_name,
                   ar.type, ar.description, ar.status,
                   ar.submission_date, ar.refusal_reason
            FROM administrative_requests ar
            JOIN students s ON s.student_id = ar.student_id
            JOIN users u ON u.user_id = s.user_id
            WHERE ar.student_id = ?
            ORDER BY ar.submission_date DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // All requests (admin view)
    // -------------------------------------------------------------------------
    public List<AdministrativeRequest> findAll() throws SQLException {
        List<AdministrativeRequest> list = new ArrayList<>();
        String sql = """
            SELECT ar.request_id, ar.student_id, u.full_name AS student_name,
                   ar.type, ar.description, ar.status,
                   ar.submission_date, ar.refusal_reason
            FROM administrative_requests ar
            JOIN students s ON s.student_id = ar.student_id
            JOIN users u ON u.user_id = s.user_id
            ORDER BY ar.submission_date DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // Approve: set status = APPROVED
    // -------------------------------------------------------------------------
    public boolean approve(int requestId) throws SQLException {
        String sql = """
            UPDATE administrative_requests
            SET status = 'APPROVED', refusal_reason = NULL
            WHERE request_id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, requestId);
            return ps.executeUpdate() > 0;
        }
    }

    // -------------------------------------------------------------------------
    // Reject: set status = REJECTED + store reason
    // -------------------------------------------------------------------------
    public boolean reject(int requestId, String reason) throws SQLException {
        String sql = """
            UPDATE administrative_requests
            SET status = 'REJECTED', refusal_reason = ?
            WHERE request_id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, reason);
            ps.setInt(2, requestId);
            return ps.executeUpdate() > 0;
        }
    }

    // -------------------------------------------------------------------------
    // Map ResultSet row → AdministrativeRequest
    // -------------------------------------------------------------------------
    private AdministrativeRequest mapRow(ResultSet rs) throws SQLException {
        String studentName = null;
        try {
            studentName = rs.getString("student_name");
        } catch (SQLException ignored) {}

        return new AdministrativeRequest(
            rs.getInt("request_id"),
            rs.getInt("student_id"),
            studentName,
            RequestType.valueOf(rs.getString("type")),
            rs.getString("description"),
            RequestStatus.valueOf(rs.getString("status")),
            rs.getDate("submission_date").toLocalDate(),
            rs.getString("refusal_reason")
        );
    }
}
