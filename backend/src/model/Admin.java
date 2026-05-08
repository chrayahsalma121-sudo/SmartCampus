package model;

import enums.UserRole;

/**
 * Admin — extends User.
 * Maps to the `admins` table joined with `users`.
 */
public class Admin extends User {

    private int adminId;

    public Admin() {}

    public Admin(int userId, String fullName, String email, String password, int adminId) {
        super(userId, fullName, email, password, UserRole.ADMIN);
        this.adminId = adminId;
    }

    public int  getAdminId()              { return adminId; }
    public void setAdminId(int adminId)   { this.adminId = adminId; }
}
