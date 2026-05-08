package security;

import enums.UserRole;

/**
 * AuthenticatedUser — holds the identity of the currently logged-in user,
 * extracted from a validated JWT token by AuthFilter.
 *
 * Salma's controllers and services depend on this class:
 *   AuthenticatedUser authUser = AuthFilter.getAuthenticatedUser(exchange);
 *   authUser.getRole()
 *   authUser.getStudentId()
 */
public class AuthenticatedUser {

    private int      userId;
    private UserRole role;

    // Role-specific IDs — null when not applicable
    private Integer studentId;
    private Integer adminId;
    private Integer librarianId;

    public AuthenticatedUser() {}

    public AuthenticatedUser(int userId, UserRole role,
                              Integer studentId, Integer adminId, Integer librarianId) {
        this.userId      = userId;
        this.role        = role;
        this.studentId   = studentId;
        this.adminId     = adminId;
        this.librarianId = librarianId;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public int getUserId()           { return userId; }
    public UserRole getRole()        { return role; }
    public Integer getStudentId()    { return studentId; }
    public Integer getAdminId()      { return adminId; }
    public Integer getLibrarianId()  { return librarianId; }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setUserId(int userId)            { this.userId = userId; }
    public void setRole(UserRole role)           { this.role = role; }
    public void setStudentId(Integer studentId)  { this.studentId = studentId; }
    public void setAdminId(Integer adminId)      { this.adminId = adminId; }
    public void setLibrarianId(Integer lid)      { this.librarianId = lid; }
}
