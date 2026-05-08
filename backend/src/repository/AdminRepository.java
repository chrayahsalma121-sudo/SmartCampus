package repository;

import database.DatabaseConnection;
import model.Admin;

import java.sql.*;

/**
 * AdminRepository — queries the `admins` table (joined with `users`).
 */
public class AdminRepository {

    // -------------------------------------------------------------------------
    // Find Admin by their userId — used in /api/auth/me
    // -------------------------------------------------------------------------
    public Admin findByUserId(int userId) throws SQLException {
        String sql = """
            SELECT u.user_id, u.full_name, u.email, u.password,
                   a.admin_id
            FROM users u
            JOIN admins a ON a.user_id = u.user_id
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
    // Return admin_id for a given user_id — used in AuthFilter
    // -------------------------------------------------------------------------
    public Integer findAdminIdByUserId(int userId) throws SQLException {
        String sql = "SELECT admin_id FROM admins WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("admin_id");
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Map ResultSet row → Admin
    // -------------------------------------------------------------------------
    private Admin mapRow(ResultSet rs) throws SQLException {
        return new Admin(
            rs.getInt("user_id"),
            rs.getString("full_name"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getInt("admin_id")
        );
    }
}
