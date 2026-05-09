package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Book;
import model.Borrowing;
import repository.BookRepository;
import security.AuthFilter;
import security.AuthenticatedUser;
import service.LibraryService;
import util.JsonUtil;
import util.ResponseUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * LibraryController — handles all library-related HTTP endpoints.
 *
 * Routes (registered in Main.java):
 *   GET  /api/books                      → listBooks
 *   POST /api/books/borrow               → borrowBook
 *   POST /api/books/return               → returnBook
 *   GET  /api/books/my-borrowings        → myBorrowings
 *   POST /api/librarian/books            → addBook
 *   POST /api/librarian/books/update     → updateBook
 *   POST /api/librarian/books/delete     → deleteBook
 *   GET  /api/librarian/borrowings       → list all borrowings
 *   POST /api/librarian/borrowings/return → register a return
 */
public class LibraryController implements HttpHandler {

    private final LibraryService  libraryService = new LibraryService();
    private final BookRepository  bookRepo       = new BookRepository();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path   = exchange.getRequestURI().getPath();

        try {
            // ------------------------------------------------------------------
            // GET /api/books  — list all books
            // ------------------------------------------------------------------
            if (method.equals("GET") && path.equals("/api/books")) {
                handleListBooks(exchange);

            // ------------------------------------------------------------------
            // GET /api/books/my-borrowings
            // ------------------------------------------------------------------
            } else if (method.equals("GET") && path.equals("/api/books/my-borrowings")) {
                handleMyBorrowings(exchange);

            // ------------------------------------------------------------------
            // POST /api/books/borrow
            // ------------------------------------------------------------------
            } else if (method.equals("POST") && path.equals("/api/books/borrow")) {
                handleBorrowBook(exchange);

            // ------------------------------------------------------------------
            // POST /api/books/return
            // ------------------------------------------------------------------
            } else if (method.equals("POST") && path.equals("/api/books/return")) {
                handleReturnBook(exchange);

            // ------------------------------------------------------------------
            // POST /api/librarian/books  — add book
            // ------------------------------------------------------------------
            } else if (method.equals("POST") && path.equals("/api/librarian/books")) {
                handleAddBook(exchange);

            // ------------------------------------------------------------------
            // POST /api/librarian/books/update
            // ------------------------------------------------------------------
            } else if (method.equals("POST") && path.equals("/api/librarian/books/update")) {
                handleUpdateBook(exchange);

            // ------------------------------------------------------------------
            // POST /api/librarian/books/delete
            // ------------------------------------------------------------------
            } else if (method.equals("POST") && path.equals("/api/librarian/books/delete")) {
                handleDeleteBook(exchange);

            // ------------------------------------------------------------------
            // GET /api/librarian/borrowings
            // ------------------------------------------------------------------
            } else if (method.equals("GET") && path.equals("/api/librarian/borrowings")) {
                handleAllBorrowings(exchange);

            // ------------------------------------------------------------------
            // POST /api/librarian/borrowings/return
            // ------------------------------------------------------------------
            } else if (method.equals("POST") && path.equals("/api/librarian/borrowings/return")) {
                handleLibrarianReturn(exchange);

            } else {
                ResponseUtil.sendError(exchange, 404, "Endpoint not found.");
            }

        } catch (Exception e) {
            ResponseUtil.sendError(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    // =========================================================================
    // GET /api/books
    // =========================================================================
    private void handleListBooks(HttpExchange exchange) throws IOException {
        try {
            AuthenticatedUser authUser = AuthFilter.getAuthenticatedUser(exchange);
            if (authUser == null) {
                ResponseUtil.sendError(exchange, 401, "Missing authorization token.");
                return;
            }

            String search = getSearchQuery(exchange);
            List<Book> books = libraryService.listBooks(authUser, search);

            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < books.size(); i++) {
                sb.append(bookToJson(books.get(i)));
                if (i < books.size() - 1) sb.append(",");
            }
            sb.append("]");

            ResponseUtil.sendSuccess(exchange, 200, "Books retrieved successfully", sb.toString());

        } catch (Exception e) {
            ResponseUtil.sendError(exchange, 400, e.getMessage());
        }
    }

    // =========================================================================
    // POST /api/books/borrow
    // Body: { "bookId": 1 }
    // =========================================================================
    private void handleBorrowBook(HttpExchange exchange) throws IOException {
        try {
            AuthenticatedUser authUser = AuthFilter.getAuthenticatedUser(exchange);
            if (authUser == null) {
                ResponseUtil.sendError(exchange, 401, "Missing authorization token.");
                return;
            }

            Map<String, Object> body   = JsonUtil.parseBody(exchange);
            int                 bookId = JsonUtil.getInt(body, "bookId");

            Borrowing borrowing = libraryService.borrowBook(authUser, bookId);

            ResponseUtil.sendSuccess(exchange, 201, "Book borrowed successfully",
                borrowingToJson(borrowing));

        } catch (Exception e) {
            ResponseUtil.sendError(exchange, 400, e.getMessage());
        }
    }

    // =========================================================================
    // POST /api/books/return
    // Body: { "borrowingId": 1 }
    // =========================================================================
    private void handleReturnBook(HttpExchange exchange) throws IOException {
        try {
            AuthenticatedUser authUser = AuthFilter.getAuthenticatedUser(exchange);
            if (authUser == null) {
                ResponseUtil.sendError(exchange, 401, "Missing authorization token.");
                return;
            }

            Map<String, Object> body        = JsonUtil.parseBody(exchange);
            int                 borrowingId = JsonUtil.getInt(body, "borrowingId");

            libraryService.returnBook(authUser, borrowingId);

            ResponseUtil.sendSuccess(exchange, 200, "Book returned successfully", "{}");

        } catch (Exception e) {
            ResponseUtil.sendError(exchange, 400, e.getMessage());
        }
    }

    // =========================================================================
    // GET /api/books/my-borrowings
    // =========================================================================
    private void handleMyBorrowings(HttpExchange exchange) throws IOException {
        try {
            AuthenticatedUser authUser = AuthFilter.getAuthenticatedUser(exchange);
            if (authUser == null) {
                ResponseUtil.sendError(exchange, 401, "Missing authorization token.");
                return;
            }

            List<Borrowing> borrowings = libraryService.myBorrowings(authUser);

            // Need book titles — fetch each book for display
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < borrowings.size(); i++) {
                sb.append(myBorrowingToJson(borrowings.get(i)));
                if (i < borrowings.size() - 1) sb.append(",");
            }
            sb.append("]");

            ResponseUtil.sendSuccess(exchange, 200, "Borrowings retrieved successfully", sb.toString());

        } catch (Exception e) {
            ResponseUtil.sendError(exchange, 400, e.getMessage());
        }
    }

    // =========================================================================
    // POST /api/librarian/books
    // Body: { "title": "...", "author": "..." }
    // =========================================================================
    private void handleAddBook(HttpExchange exchange) throws IOException {
        try {
            AuthenticatedUser authUser = AuthFilter.getAuthenticatedUser(exchange);
            if (authUser == null) {
                ResponseUtil.sendError(exchange, 401, "Missing authorization token.");
                return;
            }

            Map<String, Object> body   = JsonUtil.parseBody(exchange);
            String              title  = JsonUtil.getString(body, "title");
            String              author = JsonUtil.getString(body, "author");

            Book book = libraryService.addBook(authUser, title, author);

            ResponseUtil.sendSuccess(exchange, 201, "Book added successfully", bookToJson(book));

        } catch (Exception e) {
            ResponseUtil.sendError(exchange, 400, e.getMessage());
        }
    }

    // =========================================================================
    // POST /api/librarian/books/update
    // Body: { "bookId": 1, "title": "...", "author": "..." }
    // =========================================================================
    private void handleUpdateBook(HttpExchange exchange) throws IOException {
        try {
            AuthenticatedUser authUser = AuthFilter.getAuthenticatedUser(exchange);
            if (authUser == null) {
                ResponseUtil.sendError(exchange, 401, "Missing authorization token.");
                return;
            }

            Map<String, Object> body   = JsonUtil.parseBody(exchange);
            int                 bookId = JsonUtil.getInt(body, "bookId");
            String              title  = JsonUtil.getString(body, "title");
            String              author = JsonUtil.getString(body, "author");

            libraryService.updateBook(authUser, bookId, title, author);

            ResponseUtil.sendSuccess(exchange, 200, "Book updated successfully", "{}");

        } catch (Exception e) {
            ResponseUtil.sendError(exchange, 400, e.getMessage());
        }
    }

    // =========================================================================
    // POST /api/librarian/books/delete
    // Body: { "bookId": 1 }
    // =========================================================================
    private void handleDeleteBook(HttpExchange exchange) throws IOException {
        try {
            AuthenticatedUser authUser = AuthFilter.getAuthenticatedUser(exchange);
            if (authUser == null) {
                ResponseUtil.sendError(exchange, 401, "Missing authorization token.");
                return;
            }

            Map<String, Object> body   = JsonUtil.parseBody(exchange);
            int                 bookId = JsonUtil.getInt(body, "bookId");

            libraryService.deleteBook(authUser, bookId);

            ResponseUtil.sendSuccess(exchange, 200, "Book deleted successfully", "{}");

        } catch (Exception e) {
            ResponseUtil.sendError(exchange, 400, e.getMessage());
        }
    }

    // =========================================================================
    // GET /api/librarian/borrowings
    // =========================================================================
    private void handleAllBorrowings(HttpExchange exchange) throws IOException {
        try {
            AuthenticatedUser authUser = AuthFilter.getAuthenticatedUser(exchange);
            if (authUser == null) {
                ResponseUtil.sendError(exchange, 401, "Missing authorization token.");
                return;
            }

            List<Map<String, Object>> borrowings = libraryService.allBorrowings(authUser);

            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < borrowings.size(); i++) {
                sb.append(detailedBorrowingToJson(borrowings.get(i)));
                if (i < borrowings.size() - 1) sb.append(",");
            }
            sb.append("]");

            ResponseUtil.sendSuccess(exchange, 200, "All borrowings retrieved successfully", sb.toString());

        } catch (Exception e) {
            ResponseUtil.sendError(exchange, 400, e.getMessage());
        }
    }

    // =========================================================================
    // POST /api/librarian/borrowings/return
    // Body: { "borrowingId": 1 }
    // =========================================================================
    private void handleLibrarianReturn(HttpExchange exchange) throws IOException {
        try {
            AuthenticatedUser authUser = AuthFilter.getAuthenticatedUser(exchange);
            if (authUser == null) {
                ResponseUtil.sendError(exchange, 401, "Missing authorization token.");
                return;
            }

            Map<String, Object> body        = JsonUtil.parseBody(exchange);
            int                 borrowingId = JsonUtil.getInt(body, "borrowingId");

            libraryService.returnBorrowingAsLibrarian(authUser, borrowingId);

            ResponseUtil.sendSuccess(exchange, 200, "Book returned successfully", "{}");

        } catch (Exception e) {
            ResponseUtil.sendError(exchange, 400, e.getMessage());
        }
    }

    // =========================================================================
    // JSON helpers
    // =========================================================================

    private String bookToJson(Book b) {
        String returnDate = "null";
        try {
            java.time.LocalDate date = bookRepo.findActiveReturnDate(b.getBookId());
            if (date != null) returnDate = "\"" + date + "\"";
        } catch (Exception ignored) {}

        return String.format(
            "{\"bookId\":%d,\"title\":\"%s\",\"author\":\"%s\",\"status\":\"%s\",\"returnDate\":%s}",
            b.getBookId(), escape(b.getTitle()), escape(b.getAuthor()), b.getStatus().name(), returnDate
        );
    }

    /** Full borrow response (after borrowing) */
    private String borrowingToJson(Borrowing b) {
        return String.format(
            "{\"borrowingId\":%d,\"studentId\":%d,\"bookId\":%d," +
            "\"borrowDate\":\"%s\",\"returnDate\":\"%s\"}",
            b.getBorrowingId(), b.getStudentId(), b.getBookId(),
            b.getBorrowDate(), b.getReturnDate()
        );
    }

    /** My-borrowings list entry — includes book title fetched from DB */
    private String myBorrowingToJson(Borrowing b) {
        String title = "";
        String author = "";
        try {
            Book book = bookRepo.findById(b.getBookId());
            if (book != null) {
                title = escape(book.getTitle());
                author = escape(book.getAuthor());
            }
        } catch (Exception ignored) {}

        return String.format(
            "{\"borrowingId\":%d,\"bookId\":%d,\"title\":\"%s\",\"author\":\"%s\"," +
            "\"borrowDate\":\"%s\",\"returnDate\":\"%s\",\"returned\":%b}",
            b.getBorrowingId(), b.getBookId(), title, author,
            b.getBorrowDate(), b.getReturnDate(), b.isReturned()
        );
    }

    private String detailedBorrowingToJson(Map<String, Object> b) {
        return String.format(
            "{\"borrowingId\":%d,\"studentId\":%d,\"studentName\":\"%s\"," +
            "\"bookId\":%d,\"title\":\"%s\",\"author\":\"%s\"," +
            "\"borrowDate\":\"%s\",\"returnDate\":\"%s\",\"returned\":%s}",
            (Integer) b.get("borrowingId"),
            (Integer) b.get("studentId"),
            escape((String) b.get("studentName")),
            (Integer) b.get("bookId"),
            escape((String) b.get("title")),
            escape((String) b.get("author")),
            b.get("borrowDate"),
            b.get("returnDate"),
            b.get("returned")
        );
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String getSearchQuery(HttpExchange exchange) {
        String query = exchange.getRequestURI().getQuery();
        if (query == null || query.isBlank()) return null;

        for (String part : query.split("&")) {
            int idx = part.indexOf('=');
            if (idx <= 0) continue;
            String key = java.net.URLDecoder.decode(part.substring(0, idx), java.nio.charset.StandardCharsets.UTF_8);
            if ("search".equals(key)) {
                return java.net.URLDecoder.decode(part.substring(idx + 1), java.nio.charset.StandardCharsets.UTF_8);
            }
        }

        return null;
    }
}
