package model;

import enums.BookStatus;

/**
 * Book — maps to the `books` table.
 */
public class Book {

    private int        bookId;
    private String     title;
    private String     author;
    private BookStatus status;

    public Book() {}

    public Book(int bookId, String title, String author, BookStatus status) {
        this.bookId = bookId;
        this.title  = title;
        this.author = author;
        this.status = status;
    }

    public int        getBookId()               { return bookId; }
    public void       setBookId(int bookId)     { this.bookId = bookId; }

    public String     getTitle()                { return title; }
    public void       setTitle(String title)    { this.title = title; }

    public String     getAuthor()               { return author; }
    public void       setAuthor(String author)  { this.author = author; }

    public BookStatus getStatus()               { return status; }
    public void       setStatus(BookStatus s)   { this.status = s; }
}
