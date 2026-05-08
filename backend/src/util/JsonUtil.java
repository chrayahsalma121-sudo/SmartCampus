package util;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * JsonUtil — minimal hand-written JSON parser.
 *
 * Supports flat JSON objects only (no nested objects in request bodies),
 * which is all the MVP endpoints need.
 *
 * Example input:  {"bookId":1,"title":"Clean Code","available":true}
 */
public class JsonUtil {

    private JsonUtil() {}

    // -------------------------------------------------------------------------
    // Read the raw request body and parse it into a Map<String, Object>
    // Values are stored as String (text) or Number (parsed to Double/Integer).
    // -------------------------------------------------------------------------
    public static Map<String, Object> parseBody(HttpExchange exchange) throws IOException {
        InputStream is  = exchange.getRequestBody();
        byte[]      buf = is.readAllBytes();
        String      raw = new String(buf, StandardCharsets.UTF_8).trim();
        return parseObject(raw);
    }

    // -------------------------------------------------------------------------
    // Parse a JSON object string into a flat map.
    // -------------------------------------------------------------------------
    public static Map<String, Object> parseObject(String json) {
        Map<String, Object> map = new HashMap<>();
        if (json == null) return map;

        // Strip outer braces
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}"))   json = json.substring(0, json.length() - 1);
        json = json.trim();

        if (json.isEmpty()) return map;

        // Split by commas that are NOT inside a string value.
        // Simple approach: iterate char by char.
        boolean  inString = false;
        int      depth    = 0;
        int      start    = 0;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                inString = !inString;
            } else if (!inString && (c == '{' || c == '[')) {
                depth++;
            } else if (!inString && (c == '}' || c == ']')) {
                depth--;
            } else if (!inString && depth == 0 && c == ',') {
                parsePair(json.substring(start, i).trim(), map);
                start = i + 1;
            }
        }
        // Last pair
        parsePair(json.substring(start).trim(), map);

        return map;
    }

    // -------------------------------------------------------------------------
    // Parse a single "key": value pair and put it into the map.
    // -------------------------------------------------------------------------
    private static void parsePair(String pair, Map<String, Object> map) {
        if (pair == null || pair.isEmpty()) return;

        // Find the colon separator
        int colonIdx = pair.indexOf(':');
        if (colonIdx < 0) return;

        String rawKey = pair.substring(0, colonIdx).trim();
        String rawVal = pair.substring(colonIdx + 1).trim();

        // Remove surrounding quotes from key
        String key = unquote(rawKey);

        // Determine value type
        Object value;
        if (rawVal.startsWith("\"")) {
            // String value
            value = unquote(rawVal);
        } else if (rawVal.equalsIgnoreCase("true")) {
            value = Boolean.TRUE;
        } else if (rawVal.equalsIgnoreCase("false")) {
            value = Boolean.FALSE;
        } else if (rawVal.equalsIgnoreCase("null")) {
            value = null;
        } else {
            // Number — try int first, then double
            try {
                value = Integer.parseInt(rawVal);
            } catch (NumberFormatException e) {
                try {
                    value = Double.parseDouble(rawVal);
                } catch (NumberFormatException e2) {
                    value = rawVal; // fallback: keep as string
                }
            }
        }

        map.put(key, value);
    }

    // -------------------------------------------------------------------------
    // Remove surrounding double-quotes and unescape \" sequences.
    // -------------------------------------------------------------------------
    private static String unquote(String s) {
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1);
        }
        return s.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    // =========================================================================
    // Typed getters — used by controllers
    // =========================================================================

    /** Get a String value. Returns null if missing. */
    public static String getString(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v == null ? null : v.toString();
    }

    /** Get an int value. Returns 0 if missing or not parseable. */
    public static int getInt(Map<String, Object> body, String key) {
        Object v = body.get(key);
        if (v == null) return 0;
        if (v instanceof Integer) return (Integer) v;
        if (v instanceof Double)  return ((Double) v).intValue();
        try { return Integer.parseInt(v.toString()); }
        catch (NumberFormatException e) { return 0; }
    }

    /** Get a boolean value. Returns false if missing. */
    public static boolean getBoolean(Map<String, Object> body, String key) {
        Object v = body.get(key);
        if (v == null) return false;
        if (v instanceof Boolean) return (Boolean) v;
        return Boolean.parseBoolean(v.toString());
    }
}
