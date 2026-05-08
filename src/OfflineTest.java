import security.JwtUtil;
import security.PasswordUtil;
import security.AuthenticatedUser;
import enums.UserRole;
import util.JsonUtil;
import util.ResponseUtil;
import util.DateUtil;

import java.util.Map;

/**
 * OfflineTest — validates all non-DB logic without needing MySQL running.
 *
 * Tests:
 *  1. JWT generation, validation, claim extraction
 *  2. PasswordUtil plain-text verify
 *  3. JsonUtil flat JSON parsing + typed getters
 *  4. DateUtil helpers
 *  5. AuthenticatedUser fields
 *  6. Enum values
 */
public class OfflineTest {

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("  SmartCampus — Offline Logic Tests");
        System.out.println("==============================================\n");

        testJwt();
        testPassword();
        testJsonUtil();
        testDateUtil();
        testAuthenticatedUser();
        testEnums();

        System.out.println("\n==============================================");
        System.out.printf("  Results: %d passed, %d failed%n", passed, failed);
        System.out.println("==============================================");

        if (failed > 0) System.exit(1);
    }

    // ── JWT ───────────────────────────────────────────────────────────────────

    static void testJwt() {
        section("JWT");

        String token = JwtUtil.generateToken(42, "STUDENT");
        assertNotNull("generateToken returns non-null", token);
        assertTrue("token has 3 parts", token.split("\\.").length == 3);
        assertTrue("isTokenValid → true for fresh token", JwtUtil.isTokenValid(token));
        assertTrue("extractUserId = 42", JwtUtil.extractUserId(token) == 42);
        assertEqual("extractRole = STUDENT", "STUDENT", JwtUtil.extractRole(token));

        String adminToken = JwtUtil.generateToken(7, "ADMIN");
        assertEqual("extractRole = ADMIN", "ADMIN", JwtUtil.extractRole(adminToken));
        assertTrue("extractUserId = 7", JwtUtil.extractUserId(adminToken) == 7);

        // Tampered token must be invalid
        String tampered = token.substring(0, token.length() - 3) + "XXX";
        assertTrue("tampered token → invalid", !JwtUtil.isTokenValid(tampered));

        // Completely bogus token
        assertTrue("random string → invalid", !JwtUtil.isTokenValid("not.a.token"));
    }

    // ── Password ──────────────────────────────────────────────────────────────

    static void testPassword() {
        section("PasswordUtil");

        assertTrue("verify correct password",   PasswordUtil.verify("1234", "1234"));
        assertTrue("reject wrong password",     !PasswordUtil.verify("wrong", "1234"));
        assertTrue("reject null plain",         !PasswordUtil.verify(null, "1234"));
        assertTrue("reject null stored",        !PasswordUtil.verify("1234", null));
        assertEqual("hash returns plain text",  "abc", PasswordUtil.hash("abc"));
    }

    // ── JsonUtil ──────────────────────────────────────────────────────────────

    static void testJsonUtil() {
        section("JsonUtil");

        // Basic string + int + boolean
        String json = "{\"email\":\"test@x.com\",\"bookId\":5,\"available\":true}";
        Map<String,Object> m = JsonUtil.parseObject(json);

        assertEqual("getString email",       "test@x.com", JsonUtil.getString(m, "email"));
        assertTrue ("getInt bookId = 5",     JsonUtil.getInt(m, "bookId") == 5);
        assertTrue ("getBoolean available",  JsonUtil.getBoolean(m, "available"));

        // Escaped quotes inside string
        String json2 = "{\"title\":\"Clean \\\"Code\\\"\",\"author\":\"Martin\"}";
        Map<String,Object> m2 = JsonUtil.parseObject(json2);
        assertEqual("escaped quote in string", "Clean \"Code\"", JsonUtil.getString(m2, "title"));

        // Missing key defaults
        assertTrue("missing int → 0",    JsonUtil.getInt(m, "missing") == 0);
        assertTrue("missing bool → false", !JsonUtil.getBoolean(m, "missing"));
        assertTrue("missing str → null",  JsonUtil.getString(m, "missing") == null);

        // false boolean
        Map<String,Object> m3 = JsonUtil.parseObject("{\"available\":false}");
        assertTrue("getBoolean false", !JsonUtil.getBoolean(m3, "available"));

        // null value
        Map<String,Object> m4 = JsonUtil.parseObject("{\"reason\":null}");
        assertTrue("null value → null", JsonUtil.getString(m4, "reason") == null);

        // Numeric as double in JSON (e.g. from some frontends)
        Map<String,Object> m5 = JsonUtil.parseObject("{\"roomId\":3,\"capacity\":30}");
        assertTrue("getInt roomId = 3",    JsonUtil.getInt(m5, "roomId") == 3);
        assertTrue("getInt capacity = 30", JsonUtil.getInt(m5, "capacity") == 30);

        // Request body pattern used by reserve room
        String reserveBody = "{\"roomId\":1,\"reservationDate\":\"2026-05-10\",\"startTime\":\"10:00\",\"endTime\":\"12:00\"}";
        Map<String,Object> rm = JsonUtil.parseObject(reserveBody);
        assertTrue("roomId = 1",                    JsonUtil.getInt(rm, "roomId") == 1);
        assertEqual("reservationDate",              "2026-05-10", JsonUtil.getString(rm, "reservationDate"));
        assertEqual("startTime",                    "10:00",      JsonUtil.getString(rm, "startTime"));
        assertEqual("endTime",                      "12:00",      JsonUtil.getString(rm, "endTime"));

        // Login body
        Map<String,Object> login = JsonUtil.parseObject("{\"email\":\"admin@example.com\",\"password\":\"1234\"}");
        assertEqual("login email",    "admin@example.com", JsonUtil.getString(login, "email"));
        assertEqual("login password", "1234",              JsonUtil.getString(login, "password"));

        // Empty body
        Map<String,Object> empty = JsonUtil.parseObject("{}");
        assertTrue("empty object returns empty map", empty.isEmpty());
    }

    // ── DateUtil ──────────────────────────────────────────────────────────────

    static void testDateUtil() {
        section("DateUtil");

        String today = DateUtil.today();
        assertNotNull("today() not null", today);
        assertTrue("today() matches YYYY-MM-DD", today.matches("\\d{4}-\\d{2}-\\d{2}"));

        String future = DateUtil.plusDays("2026-05-08", 14);
        assertEqual("plusDays 14 from 2026-05-08", "2026-05-22", future);

        String same = DateUtil.plusDays("2026-01-01", 0);
        assertEqual("plusDays 0 is same date", "2026-01-01", same);
    }

    // ── AuthenticatedUser ─────────────────────────────────────────────────────

    static void testAuthenticatedUser() {
        section("AuthenticatedUser");

        AuthenticatedUser student = new AuthenticatedUser(1, UserRole.STUDENT, 10, null, null);
        assertTrue("student role",       student.getRole() == UserRole.STUDENT);
        assertTrue("userId = 1",         student.getUserId() == 1);
        assertTrue("studentId = 10",     student.getStudentId() == 10);
        assertTrue("adminId null",       student.getAdminId() == null);
        assertTrue("librarianId null",   student.getLibrarianId() == null);

        AuthenticatedUser admin = new AuthenticatedUser(2, UserRole.ADMIN, null, 5, null);
        assertTrue("admin role",         admin.getRole() == UserRole.ADMIN);
        assertTrue("adminId = 5",        admin.getAdminId() == 5);
        assertTrue("studentId null",     admin.getStudentId() == null);

        AuthenticatedUser lib = new AuthenticatedUser(3, UserRole.LIBRARIAN, null, null, 7);
        assertTrue("librarian role",     lib.getRole() == UserRole.LIBRARIAN);
        assertTrue("librarianId = 7",    lib.getLibrarianId() == 7);
    }

    // ── Enums ─────────────────────────────────────────────────────────────────

    static void testEnums() {
        section("Enums");

        // UserRole
        assertEqual("STUDENT name",   "STUDENT",   UserRole.STUDENT.name());
        assertEqual("ADMIN name",     "ADMIN",      UserRole.ADMIN.name());
        assertEqual("LIBRARIAN name", "LIBRARIAN",  UserRole.LIBRARIAN.name());
        assertTrue("valueOf STUDENT", UserRole.valueOf("STUDENT") == UserRole.STUDENT);

        // BookStatus
        assertEqual("AVAILABLE", "AVAILABLE", enums.BookStatus.AVAILABLE.name());
        assertEqual("BORROWED",  "BORROWED",  enums.BookStatus.BORROWED.name());

        // ReservationStatus
        assertEqual("CONFIRMED",  "CONFIRMED",  enums.ReservationStatus.CONFIRMED.name());
        assertEqual("CANCELLED",  "CANCELLED",  enums.ReservationStatus.CANCELLED.name());

        // RequestStatus
        assertEqual("PENDING",   "PENDING",   enums.RequestStatus.PENDING.name());
        assertEqual("APPROVED",  "APPROVED",  enums.RequestStatus.APPROVED.name());
        assertEqual("REJECTED",  "REJECTED",  enums.RequestStatus.REJECTED.name());

        // RequestType
        assertEqual("SCHOOL_CERTIFICATE",    "SCHOOL_CERTIFICATE",    enums.RequestType.SCHOOL_CERTIFICATE.name());
        assertEqual("ATTENDANCE_CERTIFICATE","ATTENDANCE_CERTIFICATE", enums.RequestType.ATTENDANCE_CERTIFICATE.name());
        assertEqual("TRANSCRIPT",            "TRANSCRIPT",            enums.RequestType.TRANSCRIPT.name());
        assertEqual("OTHER",                 "OTHER",                 enums.RequestType.OTHER.name());
    }

    // ── Assertion helpers ─────────────────────────────────────────────────────

    static void section(String name) {
        System.out.println("\n── " + name + " ─────────────────────────────────────");
    }

    static void assertTrue(String label, boolean condition) {
        if (condition) {
            System.out.println("  ✅ PASS: " + label);
            passed++;
        } else {
            System.out.println("  ❌ FAIL: " + label);
            failed++;
        }
    }

    static void assertEqual(String label, Object expected, Object actual) {
        if (expected == null ? actual == null : expected.equals(actual)) {
            System.out.println("  ✅ PASS: " + label);
            passed++;
        } else {
            System.out.println("  ❌ FAIL: " + label + " → expected [" + expected + "] but got [" + actual + "]");
            failed++;
        }
    }

    static void assertNotNull(String label, Object value) {
        if (value != null) {
            System.out.println("  ✅ PASS: " + label);
            passed++;
        } else {
            System.out.println("  ❌ FAIL: " + label + " → was null");
            failed++;
        }
    }
}
