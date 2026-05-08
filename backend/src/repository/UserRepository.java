package repository;

import database.DatabaseConnection;
import enums.UserRole;
import model.User;

import java.sql.*;

/**
 * UserRepository — queries the `users` table.
 */
public class UserRepository {

    // -------------------------------------------------------------------------
    // Find user by email — used at login
    // -------------------------------------------------------------------------
    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT user_id, full_name, email, password, role FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Find user by id — used for /api/auth/me
    // -------------------------------------------------------------------------
    public User findById(int userId) throws SQLException {
        String sql = "SELECT user_id, full_name, email, password, role FROM users WHERE user_id = ?";

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
    // Map ResultSet row → User
    // -------------------------------------------------------------------------
    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("user_id"),
            rs.getString("full_name"),
            rs.getString("email"),
            rs.getString("password"),
            UserRole.valueOf(rs.getString("role"))
        );
    }
}
