package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.AdministrativeRequest;
import security.AuthFilter;
import security.AuthenticatedUser;
import service.AdministrativeRequestService;
import util.JsonUtil;
import util.ResponseUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * AdministrativeRequestController handles all request-related endpoints.
 *
 * Routes (register in Main.java):
 *   POST /api/requests                  → submitRequest
 *   GET  /api/requests/my-requests      → myRequests
 *   GET  /api/admin/requests            → listAllRequests
 *   POST /api/admin/requests/approve    → approveRequest
 *   POST /api/admin/requests/reject     → rejectRequest
 */
public class AdministrativeRequestController implements HttpHandler {

    private final AdministrativeRequestService requestService = new AdministrativeRequestService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path   = exchange.getRequestURI().getPath();

        try {
            // ------------------------------------------------------------------
            // POST /api/requests  — student submits a request
            // ------------------------------------------------------------------
            if (method.equals("POST") && path.equals("/api/requests")) {
                handleSubmitRequest(exchange);

            // ------------------------------------------------------------------
            // GET /api/requests/my-requests  — student views own requests
            // ------------------------------------------------------------------
            } else if (method.equals("GET") && path.equals("/api/requests/my-requests")) {
                handleMyRequests(exchange);

            // ------------------------------------------------------------------
            // GET /api/admin/requests  — admin views all requests
            // ------------------------------------------------------------------
            } else if (method.equals("GET") && path.equals("/api/admin/requests")) {
                handleListAllRequests(exchange);

            // ------------------------------------------------------------------
            // POST /api/admin/requests/approve
            // ------------------------------------------------------------------
            } else if (method.equals("POST") && path.equals("/api/admin/requests/approve")) {
                handleApproveRequest(exchange);

            // ------------------------------------------------------------------
            // POST /api/admin/requests/reject
            // ------------------------------------------------------------------
            } else if (method.equals("POST") && path.equals("/api/admin/requests/reject")) {
                handleRejectRequest(exchange);

            } else {
                ResponseUtil.sendError(exchange, 404, "Endpoint not found.");
            }

        } catch (Exception e) {
            ResponseUtil.sendError(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    // =========================================================================
    // POST /api/requests
    // Body: { "type": "TRANSCRIPT", "description": "Need transcript for visa." }
    // =========================================================================
    private void handleSubmitRequest(HttpExchange exchange) throws IOException {
        try {
            AuthenticatedUser authUser = AuthFilter.getAuthenticatedUser(exchange);
            if (authUser == null) {
                ResponseUtil.sendError(exchange, 401, "Unauthorized. Token missing or invalid.");
                return;
            }

            Map<String, Object> body        = JsonUtil.parseBody(exchange);
            String              type        = JsonUtil.getString(body, "type");
            String              description = JsonUtil.getString(body, "description");

            AdministrativeRequest req = requestService.submitRequest(authUser, type, description);

            ResponseUtil.sendSuccess(exchange, 201, "Request submitted successfully.", requestToJson(req));

        } catch (Exception e) {
            ResponseUtil.sendError(exchange, 400, e.getMessage());
        }
    }

    // =========================================================================
    // GET /api/requests/my-requests
    // =========================================================================
    private void handleMyRequests(HttpExchange exchange) throws IOException {
        try {
            AuthenticatedUser authUser = AuthFilter.getAuthenticatedUser(exchange);
            if (authUser == null) {
                ResponseUtil.sendError(exchange, 401, "Unauthorized. Token missing or invalid.");
                return;
            }

            List<AdministrativeRequest> requests = requestService.myRequests(authUser);

            ResponseUtil.sendSuccess(exchange, 200, "Requests retrieved successfully.", listToJson(requests));

        } catch (Exception e) {
            ResponseUtil.sendError(exchange, 400, e.getMessage());
        }
    }

    // =========================================================================
    // GET /api/admin/requests
    // =========================================================================
    private void handleListAllRequests(HttpExchange exchange) throws IOException {
        try {
            AuthenticatedUser authUser = AuthFilter.getAuthenticatedUser(exchange);
            if (authUser == null) {
                ResponseUtil.sendError(exchange, 401, "Unauthorized. Token missing or invalid.");
                return;
            }

            List<AdministrativeRequest> requests = requestService.listAllRequests(authUser);

            ResponseUtil.sendSuccess(exchange, 200, "All requests retrieved.", listToJson(requests));

        } catch (Exception e) {
            ResponseUtil.sendError(exchange, 400, e.getMessage());
        }
    }

    // =========================================================================
    // POST /api/admin/requests/approve
    // Body: { "requestId": 3 }
    // =========================================================================
    private void handleApproveRequest(HttpExchange exchange) throws IOException {
        try {
            AuthenticatedUser authUser = AuthFilter.getAuthenticatedUser(exchange);
            if (authUser == null) {
                ResponseUtil.sendError(exchange, 401, "Unauthorized. Token missing or invalid.");
                return;
            }

            Map<String, Object> body      = JsonUtil.parseBody(exchange);
            int                 requestId = JsonUtil.getInt(body, "requestId");

            AdministrativeRequest req = requestService.approveRequest(authUser, requestId);

            ResponseUtil.sendSuccess(exchange, 200, "Request approved successfully.", requestToJson(req));

        } catch (Exception e) {
            ResponseUtil.sendError(exchange, 400, e.getMessage());
        }
    }

    // =========================================================================
    // POST /api/admin/requests/reject
    // Body: { "requestId": 3, "refusalReason": "Missing documents." }
    // =========================================================================
    private void handleRejectRequest(HttpExchange exchange) throws IOException {
        try {
            AuthenticatedUser authUser = AuthFilter.getAuthenticatedUser(exchange);
            if (authUser == null) {
                ResponseUtil.sendError(exchange, 401, "Unauthorized. Token missing or invalid.");
                return;
            }

            Map<String, Object> body          = JsonUtil.parseBody(exchange);
            int                 requestId     = JsonUtil.getInt(body, "requestId");
            String              refusalReason = JsonUtil.getString(body, "refusalReason");

            AdministrativeRequest req = requestService.rejectRequest(authUser, requestId, refusalReason);

            ResponseUtil.sendSuccess(exchange, 200, "Request rejected.", requestToJson(req));

        } catch (Exception e) {
            ResponseUtil.sendError(exchange, 400, e.getMessage());
        }
    }

    // =========================================================================
    // JSON helpers
    // =========================================================================
    private String requestToJson(AdministrativeRequest r) {
        String reason = r.getRefusalReason() != null
            ? "\"" + escape(r.getRefusalReason()) + "\""
            : "null";

        return String.format(
            "{\"requestId\":%d,\"studentId\":%d,\"type\":\"%s\"," +
            "\"description\":\"%s\",\"status\":\"%s\"," +
            "\"submissionDate\":\"%s\",\"refusalReason\":%s}",
            r.getRequestId(), r.getStudentId(), r.getType().name(),
            escape(r.getDescription()), r.getStatus().name(),
            r.getSubmissionDate(), reason
        );
    }

    private String listToJson(List<AdministrativeRequest> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(requestToJson(list.get(i)));
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}
