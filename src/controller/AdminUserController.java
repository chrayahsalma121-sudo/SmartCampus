package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import enums.UserRole;
import security.AuthFilter;
import security.AuthenticatedUser;
import service.UserService;
import util.JsonUtil;
import util.ResponseUtil;

import java.io.IOException;
import java.util.Map;

/**
 * AdminUserController — handles user management endpoints for administrators.
 *
 * Routes (registered in Main.java):
 *   POST /api/admin/users — protected, ADMIN only
 */
public class AdminUserController implements HttpHandler {

    private final UserService userService = new UserService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path   = exchange.getRequestURI().getPath();

        try {
            // 1. Authenticate & Authorize
            AuthenticatedUser authUser = AuthFilter.getAuthenticatedUser(exchange);
            if (authUser == null) {
                ResponseUtil.sendError(exchange, 401, "Missing authorization token.");
                return;
            }
            if (authUser.getRole() != UserRole.ADMIN) {
                ResponseUtil.sendError(exchange, 403, "Access denied. Admin role required.");
                return;
            }

            // 2. Route
            if (method.equals("POST") && path.equals("/api/admin/users")) {
                handleCreateUser(exchange);
            } else if (method.equals("GET") && path.equals("/api/admin/users")) {
                handleListUsers(exchange);
            } else if (method.equals("POST") && path.equals("/api/admin/users/delete")) {
                handleDeleteUser(exchange);
            } else {
                ResponseUtil.sendError(exchange, 404, "Endpoint not found.");
            }

        } catch (Exception e) {
            ResponseUtil.sendError(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    // =========================================================================
    // POST /api/admin/users
    // =========================================================================
    private void handleCreateUser(HttpExchange exchange) throws IOException {
        try {
            Map<String, Object> body = JsonUtil.parseBody(exchange);
            userService.createUser(body);
            ResponseUtil.sendSuccess(exchange, 201, "User created successfully", "{}");

        } catch (Exception e) {
            ResponseUtil.sendError(exchange, 400, e.getMessage());
        }
    }

    // =========================================================================
    // GET /api/admin/users
    // =========================================================================
    private void handleListUsers(HttpExchange exchange) throws IOException {
        try {
            java.util.List<model.User> users = userService.listAllUsers();

            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < users.size(); i++) {
                sb.append(userToJson(users.get(i)));
                if (i < users.size() - 1) sb.append(",");
            }
            sb.append("]");

            ResponseUtil.sendSuccess(exchange, 200, "Users retrieved successfully.", sb.toString());
        } catch (Exception e) {
            ResponseUtil.sendError(exchange, 400, e.getMessage());
        }
    }

    // =========================================================================
    // POST /api/admin/users/delete
    // =========================================================================
    private void handleDeleteUser(HttpExchange exchange) throws IOException {
        try {
            Map<String, Object> body = JsonUtil.parseBody(exchange);
            int userId = JsonUtil.getInt(body, "userId");

            userService.deleteUser(userId);
            ResponseUtil.sendSuccess(exchange, 200, "User deleted successfully.", "{}");
        } catch (Exception e) {
            ResponseUtil.sendError(exchange, 400, e.getMessage());
        }
    }

    private String userToJson(model.User u) {
        return String.format(
            "{\"userId\":%d,\"fullName\":\"%s\",\"email\":\"%s\",\"role\":\"%s\"}",
            u.getUserId(), escape(u.getFullName()), escape(u.getEmail()), u.getRole().name()
        );
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}
