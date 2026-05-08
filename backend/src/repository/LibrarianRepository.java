package repository;

import database.DatabaseConnection;
import model.Librarian;

import java.sql.*;

/**
 * LibrarianRepository — queries the `librarians` table (joined with `users`).
 */
public class LibrarianRepository {

    // -------------------------------------------------------------------------
    // Find Librarian by their userId — used in /api/auth/me
    // -------------------------------------------------------------------------
    public Librarian findByUserId(int userId) throws SQLException {
        String sql = """
            SELECT u.user_id, u.full_name, u.email, u.password,
                   l.librarian_id
            FROM users u
            JOIN librarians l ON l.user_id = u.user_id
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
    // Return librarian_id for a given user_id — used in AuthFilter
    // -------------------------------------------------------------------------
    public Integer findLibrarianIdByUserId(int userId) throws SQLException {
        String sql = "SELECT librarian_id FROM librarians WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("librarian_id");
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Map ResultSet row → Librarian
    // -------------------------------------------------------------------------
    private Librarian mapRow(ResultSet rs) throws SQLException {
        return new Librarian(
            rs.getInt("user_id"),
            rs.getString("full_name"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getInt("librarian_id")
        );
    }
}
