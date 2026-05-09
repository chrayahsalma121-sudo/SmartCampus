package repository;

import database.DatabaseConnection;
import enums.UserRole;
import model.Student;

import java.sql.*;

/**
 * StudentRepository — queries the `students` table (joined with `users`).
 *
 * NOTE: Salma's services call isStudentValid(studentId) — this must compile
 * before her code can run.
 */
public class StudentRepository {

    // -------------------------------------------------------------------------
    // Find a Student by their userId — used in /api/auth/me and AuthFilter
    // -------------------------------------------------------------------------
    public Student findByUserId(int userId) throws SQLException {
        String sql = """
            SELECT u.user_id, u.full_name, u.email, u.password,
                   s.student_id, s.filiere, s.valid
            FROM users u
            JOIN students s ON s.user_id = u.user_id
            WHERE u.user_id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Return the student_id for a given user_id — used in AuthFilter
    // -------------------------------------------------------------------------
    public Integer findStudentIdByUserId(int userId) throws SQLException {
        String sql = "SELECT student_id FROM students WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("student_id");
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Check if a student's account is valid — CALLED BY SALMA'S SERVICES
    // -------------------------------------------------------------------------
    public boolean isStudentValid(int studentId) throws SQLException {
        String sql = "SELECT valid FROM students WHERE student_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBoolean("valid");
            }
        }
        return false; // not found → treat as invalid
    }

    // -------------------------------------------------------------------------
    // Create new student
    // -------------------------------------------------------------------------
    public void createStudent(int userId, String filiere) throws SQLException {
        String sql = "INSERT INTO students (user_id, filiere) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, filiere);
            ps.executeUpdate();
        }
    }

    // -------------------------------------------------------------------------
    // Map ResultSet row → Student
    // -------------------------------------------------------------------------
    private Student mapRow(ResultSet rs) throws SQLException {
        return new Student(
            rs.getInt("user_id"),
            rs.getString("full_name"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getInt("student_id"),
            rs.getString("filiere"),
            rs.getBoolean("valid")
        );
    }
}
