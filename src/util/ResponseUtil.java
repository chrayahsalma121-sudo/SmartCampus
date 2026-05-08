package util;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * ResponseUtil — sends standardised JSON responses.
 *
 * Success shape:
 *   { "success": true,  "message": "...", "data": <dataJson> }
 *
 * Error shape:
 *   { "success": false, "message": "..." }
 */
public class ResponseUtil {

    private ResponseUtil() {}

    // -------------------------------------------------------------------------
    // Success response — data is a raw JSON string (object or array)
    // -------------------------------------------------------------------------
    public static void sendSuccess(HttpExchange exchange,
                                   int statusCode,
                                   String message,
                                   String dataJson) throws IOException {

        String body = String.format(
            "{\"success\":true,\"message\":\"%s\",\"data\":%s}",
            escapeJson(message),
            dataJson == null ? "null" : dataJson
        );
        send(exchange, statusCode, body);
    }

    // -------------------------------------------------------------------------
    // Error response — no data field
    // -------------------------------------------------------------------------
    public static void sendError(HttpExchange exchange,
                                  int statusCode,
                                  String message) throws IOException {

        String body = String.format(
            "{\"success\":false,\"message\":\"%s\"}",
            escapeJson(message)
        );
        send(exchange, statusCode, body);
    }

    // -------------------------------------------------------------------------
    // Internal send
    // -------------------------------------------------------------------------
    private static void send(HttpExchange exchange,
                              int statusCode,
                              String body) throws IOException {

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // -------------------------------------------------------------------------
    // Escape characters that would break JSON string values
    // -------------------------------------------------------------------------
    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
