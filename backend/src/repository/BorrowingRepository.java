package repository;

import database.DatabaseConnection;
import model.Borrowing;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * BorrowingRepository — queries the `borrowings` table.
 */
public class BorrowingRepository {

    // -------------------------------------------------------------------------
    // Save a new borrowing record
    // -------------------------------------------------------------------------
    public Borrowing save(Borrowing b) throws SQLException {
        String sql = """
            INSERT INTO borrowings (student_id, book_id, borrow_date, return_date, returned)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, b.getStudentId());
            ps.setInt(2, b.getBookId());
            ps.setDate(3, Date.valueOf(b.getBorrowDate()));
            ps.setDate(4, Date.valueOf(b.getReturnDate()));
            ps.setBoolean(5, b.isReturned());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) b.setBorrowingId(keys.getInt(1));
            }
        }
        return b;
    }

    // -------------------------------------------------------------------------
    // Find a borrowing by id
    // -------------------------------------------------------------------------
    public Borrowing findById(int borrowingId) throws SQLException {
        String sql = """
            SELECT borrowing_id, student_id, book_id, borrow_date, return_date, returned
            FROM borrowings WHERE borrowing_id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, borrowingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // All borrowings for a student (for my-borrowings list)
    // -------------------------------------------------------------------------
    public List<Borrowing> findAllByStudentId(int studentId) throws SQLException {
        List<Borrowing> list = new ArrayList<>();
        String sql = """
            SELECT borrowing_id, student_id, book_id, borrow_date, return_date, returned
            FROM borrowings
            WHERE student_id = ?
            ORDER BY borrow_date DESC
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
    // Count active (not yet returned) borrowings for a student — enforces the 3-book limit
    // -------------------------------------------------------------------------
    public int countActiveBorrowings(int studentId) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM borrowings
            WHERE student_id = ? AND returned = FALSE
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    // -------------------------------------------------------------------------
    // All borrowings with book and student details (librarian view)
    // -------------------------------------------------------------------------
    public List<Map<String, Object>> findAllDetailed() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = """
            SELECT br.borrowing_id, br.student_id, u.full_name AS student_name,
                   br.book_id, b.title, b.author,
                   br.borrow_date, br.return_date, br.returned
            FROM borrowings br
            JOIN books b ON b.book_id = br.book_id
            JOIN students s ON s.student_id = br.student_id
            JOIN users u ON u.user_id = s.user_id
            ORDER BY br.borrow_date DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("borrowingId", rs.getInt("borrowing_id"));
                row.put("studentId", rs.getInt("student_id"));
                row.put("studentName", rs.getString("student_name"));
                row.put("bookId", rs.getInt("book_id"));
                row.put("title", rs.getString("title"));
                row.put("author", rs.getString("author"));
                row.put("borrowDate", rs.getDate("borrow_date").toLocalDate().toString());
                row.put("returnDate", rs.getDate("return_date").toLocalDate().toString());
                row.put("returned", rs.getBoolean("returned"));
                list.add(row);
            }
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // Mark a borrowing as returned
    // -------------------------------------------------------------------------
    public boolean markReturned(int borrowingId) throws SQLException {
        String sql = "UPDATE borrowings SET returned = TRUE WHERE borrowing_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, borrowingId);
            return ps.executeUpdate() > 0;
        }
    }

    // -------------------------------------------------------------------------
    // Map ResultSet row → Borrowing
    // -------------------------------------------------------------------------
    private Borrowing mapRow(ResultSet rs) throws SQLException {
        return new Borrowing(
            rs.getInt("borrowing_id"),
            rs.getInt("student_id"),
            rs.getInt("book_id"),
            rs.getDate("borrow_date").toLocalDate(),
            rs.getDate("return_date").toLocalDate(),
            rs.getBoolean("returned")
        );
    }
}
