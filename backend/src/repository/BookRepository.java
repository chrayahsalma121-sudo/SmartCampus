package repository;

import database.DatabaseConnection;
import enums.BookStatus;
import model.Book;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * BookRepository — queries the `books` table.
 */
public class BookRepository {

    // -------------------------------------------------------------------------
    // Find all books
    // -------------------------------------------------------------------------
    public List<Book> findAll() throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT book_id, title, author, status FROM books ORDER BY book_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) books.add(mapRow(rs));
        }
        return books;
    }

    // -------------------------------------------------------------------------
    // Find book by id
    // -------------------------------------------------------------------------
    public Book findById(int bookId) throws SQLException {
        String sql = "SELECT book_id, title, author, status FROM books WHERE book_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Insert a new book (librarian — add)
    // -------------------------------------------------------------------------
    public Book save(Book book) throws SQLException {
        String sql = "INSERT INTO books (title, author, status) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getStatus().name());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) book.setBookId(keys.getInt(1));
            }
        }
        return book;
    }

    // -------------------------------------------------------------------------
    // Update book title and author (librarian — update)
    // -------------------------------------------------------------------------
    public boolean update(Book book) throws SQLException {
        String sql = "UPDATE books SET title = ?, author = ? WHERE book_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setInt(3, book.getBookId());
            return ps.executeUpdate() > 0;
        }
    }

    // -------------------------------------------------------------------------
    // Delete a book (librarian — delete)
    // -------------------------------------------------------------------------
    public boolean delete(int bookId) throws SQLException {
        String sql = "DELETE FROM books WHERE book_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookId);
            return ps.executeUpdate() > 0;
        }
    }

    // -------------------------------------------------------------------------
    // Update book status only — used when borrowing / returning
    // -------------------------------------------------------------------------
    public boolean updateStatus(int bookId, BookStatus status) throws SQLException {
        String sql = "UPDATE books SET status = ? WHERE book_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setInt(2, bookId);
            return ps.executeUpdate() > 0;
        }
    }

    // -------------------------------------------------------------------------
    // Active return date for a borrowed book
    // -------------------------------------------------------------------------
    public LocalDate findActiveReturnDate(int bookId) throws SQLException {
        String sql = """
            SELECT return_date
            FROM borrowings
            WHERE book_id = ? AND returned = FALSE
            ORDER BY return_date DESC
            LIMIT 1
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDate("return_date").toLocalDate();
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Map ResultSet row → Book
    // -------------------------------------------------------------------------
    private Book mapRow(ResultSet rs) throws SQLException {
        return new Book(
            rs.getInt("book_id"),
            rs.getString("title"),
            rs.getString("author"),
            BookStatus.valueOf(rs.getString("status"))
        );
    }
}
