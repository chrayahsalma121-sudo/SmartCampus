package service;

import enums.UserRole;
import model.User;
import repository.AdminRepository;
import repository.LibrarianRepository;
import repository.StudentRepository;
import repository.UserRepository;
import security.PasswordUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * UserService handles administrator-driven user creation and management.
 */
public class UserService {

    private final UserRepository userRepo = new UserRepository();
    private final StudentRepository studentRepo = new StudentRepository();
    private final AdminRepository adminRepo = new AdminRepository();
    private final LibrarianRepository librarianRepo = new LibrarianRepository();

    public void createUser(Map<String, Object> body) throws Exception {
        String fullName = JsonUtilValue.string(body, "fullName");
        String email = JsonUtilValue.string(body, "email");
        String password = JsonUtilValue.string(body, "password");
        String roleStr = JsonUtilValue.string(body, "role");

        if (fullName == null || fullName.isBlank()) throw new Exception("Full name is required.");
        if (email == null || email.isBlank()) throw new Exception("Email is required.");
        if (password == null || password.isBlank()) throw new Exception("Password is required.");
        if (roleStr == null || roleStr.isBlank()) throw new Exception("Role is required.");

        UserRole role;
        try {
            role = UserRole.valueOf(roleStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid role.");
        }

        String normalizedEmail = email.trim().toLowerCase();
        if (userRepo.findByEmail(normalizedEmail) != null) {
            throw new Exception("User with this email already exists.");
        }

        String filiere = null;
        if (role == UserRole.STUDENT) {
            filiere = JsonUtilValue.string(body, "filiere");
            if (filiere == null || filiere.isBlank()) {
                throw new Exception("Filiere is required for STUDENT role.");
            }
        }

        int userId = userRepo.createUser(
            fullName.trim(),
            normalizedEmail,
            PasswordUtil.hash(password),
            role
        );

        switch (role) {
            case STUDENT -> studentRepo.createStudent(userId, filiere.trim());
            case ADMIN -> adminRepo.createAdmin(userId);
            case LIBRARIAN -> librarianRepo.createLibrarian(userId);
        }
    }

    public List<User> listAllUsers() throws SQLException {
        return userRepo.findAll();
    }

    public void deleteUser(int userId) throws Exception {
        User user = userRepo.findById(userId);
        if (user == null) {
            throw new Exception("User not found.");
        }
        userRepo.delete(userId);
    }

    private static class JsonUtilValue {
        private static String string(Map<String, Object> body, String key) {
            Object value = body.get(key);
            return value == null ? null : value.toString();
        }
    }
}
