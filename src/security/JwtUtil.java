package security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * JwtUtil — hand-rolled JWT implementation using HMAC-SHA256.
 *
 * Format: Base64Url(header).Base64Url(payload).Base64Url(signature)
 *
 * The jjwt.jar in /lib is empty (placeholder), so we implement JWT
 * manually using Java's built-in javax.crypto.Mac (HMAC-SHA256).
 *
 * Token payload stores:
 *   userId  (int)
 *   role    (String — STUDENT | ADMIN | LIBRARIAN)
 *   exp     (long — Unix timestamp ms, 24 h from issuance)
 */
public class JwtUtil {

    private static final String SECRET     = "smartcampus_secret_key_2026_marwa";
    private static final long   EXPIRATION = 86_400_000L; // 24 hours in ms

    private static final String HEADER_JSON =
        "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

    private JwtUtil() {}

    // =========================================================================
    // Generate a JWT token for a given userId and role
    // =========================================================================
    public static String generateToken(int userId, String role) {
        long issuedAt = System.currentTimeMillis();
        long exp      = issuedAt + EXPIRATION;

        String payloadJson = String.format(
            "{\"userId\":%d,\"role\":\"%s\",\"exp\":%d}",
            userId, role, exp
        );

        String headerEncoded  = base64UrlEncode(HEADER_JSON.getBytes(StandardCharsets.UTF_8));
        String payloadEncoded = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signingInput   = headerEncoded + "." + payloadEncoded;

        String signature = sign(signingInput);

        return signingInput + "." + signature;
    }

    // =========================================================================
    // Validate a token — returns true if signature is valid AND not expired
    // =========================================================================
    public static boolean isTokenValid(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return false;

            // Verify signature
            String signingInput     = parts[0] + "." + parts[1];
            String expectedSig      = sign(signingInput);
            if (!expectedSig.equals(parts[2])) return false;

            // Verify expiry
            String payloadJson = new String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8);
            long   exp         = extractLong(payloadJson, "exp");
            return System.currentTimeMillis() < exp;

        } catch (Exception e) {
            return false;
        }
    }

    // =========================================================================
    // Extract userId from token payload (no signature re-check — call after isValid)
    // =========================================================================
    public static int extractUserId(String token) {
        String payload = decodePayload(token);
        return (int) extractLong(payload, "userId");
    }

    // =========================================================================
    // Extract role from token payload
    // =========================================================================
    public static String extractRole(String token) {
        String payload = decodePayload(token);
        return extractString(payload, "role");
    }

    // =========================================================================
    // Internal helpers
    // =========================================================================

    private static String decodePayload(String token) {
        String[] parts = token.split("\\.");
        return new String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8);
    }

    private static String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"
            ));
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return base64UrlEncode(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("JWT signing failed", e);
        }
    }

    private static String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private static byte[] base64UrlDecode(String data) {
        return Base64.getUrlDecoder().decode(data);
    }

    /** Extract a long value from a flat JSON string like {"key":12345,...} */
    private static long extractLong(String json, String key) {
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx < 0) throw new RuntimeException("Key not found: " + key);
        int start = idx + search.length();
        int end   = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
            end++;
        }
        return Long.parseLong(json.substring(start, end));
    }

    /** Extract a String value from a flat JSON string like {"key":"value",...} */
    private static String extractString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int idx = json.indexOf(search);
        if (idx < 0) throw new RuntimeException("Key not found: " + key);
        int start = idx + search.length();
        int end   = json.indexOf("\"", start);
        return json.substring(start, end);
    }
}
