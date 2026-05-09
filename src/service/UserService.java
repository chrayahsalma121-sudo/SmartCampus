package service;

import enums.UserRole;
import model.User;
import repository.AdminRepository;
import repository.LibrarianRepository;
import repository.StudentRepository;
import repository.UserRepository;
import security.PasswordUtil;

import java.util.Map;

/**
 * UserService — handles user creation and management.
 */
public class UserService {

    private final UserRepository      userRepo      = new UserRepository();
    private final StudentRepository   studentRepo   = new StudentRepository();
    private final AdminRepository     adminRepo     = new AdminRepository();
    private final LibrarianRepository librarianRepo = new LibrarianRepository();

    public void createUser(Map<String, Object> body) throws Exception {
        String fullName = (String) body.get("fullName");
        String email    = (String) body.get("email");
        String password = (String) body.get("password");
        String roleStr  = (String) body.get("role");

        if (fullName == null || fullName.isBlank()) throw new Exception("Full name is required.");
        if (email == null || email.isBlank()) throw new Exception("Email is required.");
        if (password == null || password.isBlank()) throw new Exception("Password is required.");
        if (roleStr == null || roleStr.isBlank()) throw new Exception("Role is required.");

        UserRole role;
        try {
            role = UserRole.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid role.");
        }

        // Check if user already exists
        User existingUser = userRepo.findByEmail(email.trim().toLowerCase());
        if (existingUser != null) {
            throw new Exception("User with this email already exists.");
        }

        // Validate Student specific fields before creating the base user
        String filiere = null;
        if (role == UserRole.STUDENT) {
            filiere = (String) body.get("filiere");
            if (filiere == null || filiere.isBlank()) {
                throw new Exception("Filiere is required for STUDENT role.");
            }
        }

        // Hash password
        String hashedPassword = PasswordUtil.hash(password);

        // Create base user
        int userId = userRepo.createUser(fullName.trim(), email.trim().toLowerCase(), hashedPassword, role);

        // Create role-specific entity
        switch (role) {
            case STUDENT -> studentRepo.createStudent(userId, filiere.trim());
            case ADMIN -> adminRepo.createAdmin(userId);
            case LIBRARIAN -> librarianRepo.createLibrarian(userId);
        }
    }

    public java.util.List<User> listAllUsers() throws java.sql.SQLException {
        return userRepo.findAll();
    }

    public void deleteUser(int userId) throws Exception {
        User user = userRepo.findById(userId);
        if (user == null) {
            throw new Exception("User not found.");
        }
        userRepo.delete(userId);
    }
}
