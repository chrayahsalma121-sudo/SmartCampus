package model;

import java.time.LocalDate;

/**
 * Borrowing — maps to the `borrowings` table.
 */
public class Borrowing {

    private int       borrowingId;
    private int       studentId;
    private int       bookId;
    private LocalDate borrowDate;
    private LocalDate returnDate;
    private boolean   returned;

    public Borrowing() {}

    public Borrowing(int borrowingId, int studentId, int bookId,
                     LocalDate borrowDate, LocalDate returnDate, boolean returned) {
        this.borrowingId = borrowingId;
        this.studentId   = studentId;
        this.bookId      = bookId;
        this.borrowDate  = borrowDate;
        this.returnDate  = returnDate;
        this.returned    = returned;
    }

    public int       getBorrowingId()                     { return borrowingId; }
    public void      setBorrowingId(int borrowingId)      { this.borrowingId = borrowingId; }

    public int       getStudentId()                       { return studentId; }
    public void      setStudentId(int studentId)          { this.studentId = studentId; }

    public int       getBookId()                          { return bookId; }
    public void      setBookId(int bookId)                { this.bookId = bookId; }

    public LocalDate getBorrowDate()                      { return borrowDate; }
    public void      setBorrowDate(LocalDate borrowDate)  { this.borrowDate = borrowDate; }

    public LocalDate getReturnDate()                      { return returnDate; }
    public void      setReturnDate(LocalDate returnDate)  { this.returnDate = returnDate; }

    public boolean   isReturned()                         { return returned; }
    public void      setReturned(boolean returned)        { this.returned = returned; }
}
