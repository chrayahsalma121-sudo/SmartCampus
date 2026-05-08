package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import security.AuthFilter;
import security.AuthenticatedUser;
import service.AuthService;
import util.JsonUtil;
import util.ResponseUtil;

import java.io.IOException;
import java.util.Map;

/**
 * AuthController — handles authentication endpoints.
 *
 * Routes (registered in Main.java):
 *   POST /api/auth/login  — public, no JWT required
 *   GET  /api/auth/me     — protected, JWT required
 */
public class AuthController implements HttpHandler {

    private final AuthService authService = new AuthService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path   = exchange.getRequestURI().getPath();

        try {
            // ------------------------------------------------------------------
            // POST /api/auth/login
            // ------------------------------------------------------------------
            if (method.equals("POST") && path.equals("/api/auth/login")) {
                handleLogin(exchange);

            // ------------------------------------------------------------------
            // GET /api/auth/me
            // ------------------------------------------------------------------
            } else if (method.equals("GET") && path.equals("/api/auth/me")) {
                handleMe(exchange);

            } else {
                ResponseUtil.sendError(exchange, 404, "Endpoint not found.");
            }

        } catch (Exception e) {
            ResponseUtil.sendError(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    // =========================================================================
    // POST /api/auth/login
    // Body: { "email": "...", "password": "..." }
    // =========================================================================
    private void handleLogin(HttpExchange exchange) throws IOException {
        try {
            Map<String, Object> body     = JsonUtil.parseBody(exchange);
            String              email    = JsonUtil.getString(body, "email");
            String              password = JsonUtil.getString(body, "password");

            Map<String, Object> result = authService.login(email, password);

            // Build data JSON manually to keep it clean
            String dataJson = buildLoginDataJson(result);
            ResponseUtil.sendSuccess(exchange, 200, "Login successful", dataJson);

        } catch (Exception e) {
            ResponseUtil.sendError(exchange, 401, e.getMessage());
        }
    }

    // =========================================================================
    // GET /api/auth/me
    // =========================================================================
    private void handleMe(HttpExchange exchange) throws IOException {
        try {
            AuthenticatedUser authUser = AuthFilter.getAuthenticatedUser(exchange);
            if (authUser == null) {
                ResponseUtil.sendError(exchange, 401, "Missing authorization token.");
                return;
            }

            Map<String, Object> userMap = authService.getCurrentUser(authUser);
            ResponseUtil.sendSuccess(exchange, 200,
                "Current user retrieved successfully", mapToJson(userMap));

        } catch (Exception e) {
            ResponseUtil.sendError(exchange, 400, e.getMessage());
        }
    }

    // =========================================================================
    // JSON builders
    // =========================================================================

    /** Builds { "accessToken": "...", "user": {...} } */
    private String buildLoginDataJson(Map<String, Object> result) {
        String token   = (String) result.get("accessToken");
        @SuppressWarnings("unchecked")
        Map<String, Object> user = (Map<String, Object>) result.get("user");

        return String.format("{\"accessToken\":\"%s\",\"user\":%s}",
            escape(token), mapToJson(user));
    }

    /** Converts a flat Map<String,Object> to a JSON object string. */
    @SuppressWarnings("unchecked")
    private String mapToJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(entry.getKey()).append("\":");
            Object v = entry.getValue();
            if (v == null) {
                sb.append("null");
            } else if (v instanceof Boolean || v instanceof Integer || v instanceof Long) {
                sb.append(v);
            } else if (v instanceof Map) {
                sb.append(mapToJson((Map<String, Object>) v));
            } else {
                sb.append("\"").append(escape(v.toString())).append("\"");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
