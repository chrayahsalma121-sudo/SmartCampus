package security;

import com.sun.net.httpserver.HttpExchange;
import enums.UserRole;
import repository.AdminRepository;
import repository.LibrarianRepository;
import repository.StudentRepository;

/**
 * AuthFilter — extracts and validates the JWT from an incoming HTTP request,
 * then builds a fully populated AuthenticatedUser object.
 *
 * Usage in every protected controller:
 *   AuthenticatedUser authUser = AuthFilter.getAuthenticatedUser(exchange);
 *   if (authUser == null) { send 401; return; }
 */
public class AuthFilter {

    private static final StudentRepository   studentRepo   = new StudentRepository();
    private static final AdminRepository     adminRepo     = new AdminRepository();
    private static final LibrarianRepository librarianRepo = new LibrarianRepository();

    private AuthFilter() {}

    /**
     * Reads the Authorization header, validates the JWT, and returns
     * a populated AuthenticatedUser — or null if the token is missing/invalid.
     */
    public static AuthenticatedUser getAuthenticatedUser(HttpExchange exchange) {
        try {
            // 1. Read Authorization header
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return null;
            }

            // 2. Strip "Bearer " prefix
            String token = authHeader.substring(7).trim();

            // 3. Validate JWT
            if (!JwtUtil.isTokenValid(token)) {
                return null;
            }

            // 4. Extract userId + role from token
            int      userId   = JwtUtil.extractUserId(token);
            UserRole role     = UserRole.valueOf(JwtUtil.extractRole(token));

            // 5. Look up role-specific ID from DB
            Integer studentId   = null;
            Integer adminId     = null;
            Integer librarianId = null;

            switch (role) {
                case STUDENT   -> studentId   = studentRepo.findStudentIdByUserId(userId);
                case ADMIN     -> adminId     = adminRepo.findAdminIdByUserId(userId);
                case LIBRARIAN -> librarianId = librarianRepo.findLibrarianIdByUserId(userId);
            }

            return new AuthenticatedUser(userId, role, studentId, adminId, librarianId);

        } catch (Exception e) {
            // Any failure (DB error, malformed token) → treat as unauthenticated
            return null;
        }
    }
}
