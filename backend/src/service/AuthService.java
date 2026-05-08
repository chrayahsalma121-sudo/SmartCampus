package service;

import enums.UserRole;
import model.Admin;
import model.Librarian;
import model.Student;
import model.User;
import repository.AdminRepository;
import repository.LibrarianRepository;
import repository.StudentRepository;
import repository.UserRepository;
import security.AuthenticatedUser;
import security.JwtUtil;
import security.PasswordUtil;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AuthService — handles login and current-user retrieval.
 *
 * POST /api/auth/login  →  login()
 * GET  /api/auth/me     →  getCurrentUser()
 */
public class AuthService {

    private final UserRepository      userRepo      = new UserRepository();
    private final StudentRepository   studentRepo   = new StudentRepository();
    private final AdminRepository     adminRepo     = new AdminRepository();
    private final LibrarianRepository librarianRepo = new LibrarianRepository();

    // =========================================================================
    // LOGIN — returns { accessToken, user }
    // =========================================================================
    public Map<String, Object> login(String email, String password) throws Exception {

        if (email == null || email.isBlank()) {
            throw new Exception("Email is required.");
        }
        if (password == null || password.isBlank()) {
            throw new Exception("Password is required.");
        }

        // 1. Find user by email
        User user = userRepo.findByEmail(email.trim().toLowerCase());
        if (user == null) {
            throw new Exception("Invalid email or password.");
        }

        // 2. Verify password
        if (!PasswordUtil.verify(password, user.getPassword())) {
            throw new Exception("Invalid email or password.");
        }

        // 3. Generate JWT
        String token = JwtUtil.generateToken(user.getUserId(), user.getRole().name());

        // 4. Build role-specific user map
        Map<String, Object> userMap = buildUserMap(user);

        // 5. Compose result
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("accessToken", token);
        result.put("user", userMap);
        return result;
    }

    // =========================================================================
    // CURRENT USER — returns role-specific user object from JWT identity
    // =========================================================================
    public Map<String, Object> getCurrentUser(AuthenticatedUser authUser) throws Exception {

        User user = userRepo.findById(authUser.getUserId());
        if (user == null) {
            throw new Exception("User not found.");
        }

        return buildUserMap(user);
    }

    // =========================================================================
    // Build the user map whose shape depends on role
    // =========================================================================
    private Map<String, Object> buildUserMap(User user) throws SQLException {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("userId",   user.getUserId());
        map.put("fullName", user.getFullName());
        map.put("email",    user.getEmail());
        map.put("role",     user.getRole().name());

        switch (user.getRole()) {
            case STUDENT -> {
                Student s = studentRepo.findByUserId(user.getUserId());
                if (s != null) {
                    map.put("studentId", s.getStudentId());
                    map.put("filiere",   s.getFiliere());
                    map.put("valid",     s.isValid());
                }
            }
            case ADMIN -> {
                Admin a = adminRepo.findByUserId(user.getUserId());
                if (a != null) {
                    map.put("adminId", a.getAdminId());
                }
            }
            case LIBRARIAN -> {
                Librarian l = librarianRepo.findByUserId(user.getUserId());
                if (l != null) {
                    map.put("librarianId", l.getLibrarianId());
                }
            }
        }

        return map;
    }
}
