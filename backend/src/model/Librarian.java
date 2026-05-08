package model;

import enums.UserRole;

/**
 * Librarian — extends User.
 * Maps to the `librarians` table joined with `users`.
 */
public class Librarian extends User {

    private int librarianId;

    public Librarian() {}

    public Librarian(int userId, String fullName, String email, String password, int librarianId) {
        super(userId, fullName, email, password, UserRole.LIBRARIAN);
        this.librarianId = librarianId;
    }

    public int  getLibrarianId()                  { return librarianId; }
    public void setLibrarianId(int librarianId)   { this.librarianId = librarianId; }
}
