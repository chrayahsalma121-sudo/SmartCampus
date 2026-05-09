package repository;

import database.DatabaseConnection;
import enums.UserRole;
import model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
    // Create new user — used by admin
    // -------------------------------------------------------------------------
    public int createUser(String fullName, String email, String password, UserRole role) throws SQLException {
        String sql = "INSERT INTO users (full_name, email, password, role) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.setString(4, role.name());
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Failed to create user, no ID obtained.");
    }

    // -------------------------------------------------------------------------
    // Find all users (admin view)
    // -------------------------------------------------------------------------
    public List<User> findAll() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT user_id, full_name, email, password, role FROM users";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // Delete a user
    // -------------------------------------------------------------------------
    public boolean delete(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        }
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
