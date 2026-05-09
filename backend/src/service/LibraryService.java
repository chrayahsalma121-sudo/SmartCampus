package service;

import enums.BookStatus;
import enums.UserRole;
import model.Book;
import model.Borrowing;
import repository.BookRepository;
import repository.BorrowingRepository;
import repository.StudentRepository;
import security.AuthenticatedUser;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * LibraryService — business logic for all library operations.
 *
 * Endpoints:
 *   GET  /api/books                    → listBooks()
 *   POST /api/books/borrow             → borrowBook()
 *   POST /api/books/return             → returnBook()
 *   GET  /api/books/my-borrowings      → myBorrowings()
 *   POST /api/librarian/books          → addBook()
 *   POST /api/librarian/books/update   → updateBook()
 *   POST /api/librarian/books/delete   → deleteBook()
 */
public class LibraryService {

    private static final int MAX_BORROWINGS = 3;
    private static final int LOAN_DAYS      = 14;

    private final BookRepository     bookRepo     = new BookRepository();
    private final BorrowingRepository borrowingRepo = new BorrowingRepository();
    private final StudentRepository  studentRepo  = new StudentRepository();

    // =========================================================================
    // LIST BOOKS — GET /api/books
    // Allowed: STUDENT, LIBRARIAN, ADMIN
    // =========================================================================
    public List<Book> listBooks(AuthenticatedUser authUser) throws Exception {
        return listBooks(authUser, null);
    }

    public List<Book> listBooks(AuthenticatedUser authUser, String search) throws Exception {
        // All authenticated roles may list books
        if (authUser == null) {
            throw new Exception("Authentication required.");
        }
        return bookRepo.findAll(search);
    }

    // =========================================================================
    // BORROW BOOK — POST /api/books/borrow
    // Body: { bookId }
    // =========================================================================
    public Borrowing borrowBook(AuthenticatedUser authUser, int bookId) throws Exception {

        // --- Role check ---
        if (authUser.getRole() != UserRole.STUDENT) {
            throw new Exception("Only students can borrow books.");
        }

        int studentId = authUser.getStudentId();

        // --- Student validity ---
        if (!studentRepo.isStudentValid(studentId)) {
            throw new Exception("Student is not valid.");
        }

        // --- Book exists ---
        Book book = bookRepo.findById(bookId);
        if (book == null) {
            throw new Exception("Book not found.");
        }

        // --- Book available ---
        if (book.getStatus() != BookStatus.AVAILABLE) {
            throw new Exception("Book is not available.");
        }

        // --- Borrow limit ---
        int active = borrowingRepo.countActiveBorrowings(studentId);
        if (active >= MAX_BORROWINGS) {
            throw new Exception("Student already has 3 borrowed books.");
        }

        // --- Create borrowing ---
        LocalDate today      = LocalDate.now();
        LocalDate returnDate = today.plusDays(LOAN_DAYS);

        Borrowing borrowing = new Borrowing();
        borrowing.setStudentId(studentId);
        borrowing.setBookId(bookId);
        borrowing.setBorrowDate(today);
        borrowing.setReturnDate(returnDate);
        borrowing.setReturned(false);

        // --- Mark book as BORROWED ---
        bookRepo.updateStatus(bookId, BookStatus.BORROWED);

        return borrowingRepo.save(borrowing);
    }

    // =========================================================================
    // RETURN BOOK — POST /api/books/return
    // Body: { borrowingId }
    // =========================================================================
    public void returnBook(AuthenticatedUser authUser, int borrowingId) throws Exception {

        // --- Role check ---
        if (authUser.getRole() != UserRole.STUDENT) {
            throw new Exception("Only students can return books.");
        }

        // --- Borrowing exists ---
        Borrowing borrowing = borrowingRepo.findById(borrowingId);
        if (borrowing == null) {
            throw new Exception("Borrowing not found.");
        }

        // --- Ownership check ---
        if (borrowing.getStudentId() != authUser.getStudentId()) {
            throw new Exception("You can only return your own borrowed books.");
        }

        // --- Not already returned ---
        if (borrowing.isReturned()) {
            throw new Exception("This book has already been returned.");
        }

        // --- Mark returned ---
        borrowingRepo.markReturned(borrowingId);
        bookRepo.updateStatus(borrowing.getBookId(), BookStatus.AVAILABLE);
    }

    // =========================================================================
    // MY BORROWINGS — GET /api/books/my-borrowings
    // =========================================================================
    public List<Borrowing> myBorrowings(AuthenticatedUser authUser) throws Exception {

        if (authUser.getRole() != UserRole.STUDENT) {
            throw new Exception("Only students can view their borrowings.");
        }

        return borrowingRepo.findAllByStudentId(authUser.getStudentId());
    }

    // =========================================================================
    // LIBRARIAN: ALL BORROWINGS — GET /api/librarian/borrowings
    // =========================================================================
    public List<Map<String, Object>> allBorrowings(AuthenticatedUser authUser) throws Exception {

        if (authUser.getRole() != UserRole.LIBRARIAN) {
            throw new Exception("Only librarians can view all borrowings.");
        }

        return borrowingRepo.findAllDetailed();
    }

    // =========================================================================
    // LIBRARIAN: RETURN BORROWING — POST /api/librarian/borrowings/return
    // Body: { borrowingId }
    // =========================================================================
    public void returnBorrowingAsLibrarian(AuthenticatedUser authUser, int borrowingId) throws Exception {

        if (authUser.getRole() != UserRole.LIBRARIAN) {
            throw new Exception("Only librarians can register returns.");
        }

        Borrowing borrowing = borrowingRepo.findById(borrowingId);
        if (borrowing == null) {
            throw new Exception("Borrowing not found.");
        }

        if (borrowing.isReturned()) {
            throw new Exception("This book has already been returned.");
        }

        borrowingRepo.markReturned(borrowingId);
        bookRepo.updateStatus(borrowing.getBookId(), BookStatus.AVAILABLE);
    }

    // =========================================================================
    // ADD BOOK — POST /api/librarian/books
    // Body: { title, author }
    // =========================================================================
    public Book addBook(AuthenticatedUser authUser, String title, String author) throws Exception {

        if (authUser.getRole() != UserRole.LIBRARIAN) {
            throw new Exception("Only librarians can add books.");
        }

        if (title == null || title.isBlank()) {
            throw new Exception("Book title is required.");
        }
        if (author == null || author.isBlank()) {
            throw new Exception("Book author is required.");
        }

        Book book = new Book();
        book.setTitle(title.trim());
        book.setAuthor(author.trim());
        book.setStatus(BookStatus.AVAILABLE);

        return bookRepo.save(book);
    }

    // =========================================================================
    // UPDATE BOOK — POST /api/librarian/books/update
    // Body: { bookId, title, author }
    // =========================================================================
    public void updateBook(AuthenticatedUser authUser,
                           int bookId, String title, String author) throws Exception {

        if (authUser.getRole() != UserRole.LIBRARIAN) {
            throw new Exception("Only librarians can update books.");
        }

        Book book = bookRepo.findById(bookId);
        if (book == null) {
            throw new Exception("Book not found.");
        }

        if (title  != null && !title.isBlank())  book.setTitle(title.trim());
        if (author != null && !author.isBlank()) book.setAuthor(author.trim());

        bookRepo.update(book);
    }

    // =========================================================================
    // DELETE BOOK — POST /api/librarian/books/delete
    // Body: { bookId }
    // =========================================================================
    public void deleteBook(AuthenticatedUser authUser, int bookId) throws Exception {

        if (authUser.getRole() != UserRole.LIBRARIAN) {
            throw new Exception("Only librarians can delete books.");
        }

        Book book = bookRepo.findById(bookId);
        if (book == null) {
            throw new Exception("Book not found.");
        }

        bookRepo.delete(bookId);
    }
}
