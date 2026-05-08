package security;

/**
 * PasswordUtil — plain-text password handling for MVP.
 *
 * No hashing is applied per the project decision.
 * Passwords are stored and compared as-is.
 */
public class PasswordUtil {

    private PasswordUtil() {}

    /**
     * "Hash" a password — returns the plain text as-is for MVP.
     * Called when creating users (seed data).
     */
    public static String hash(String plainPassword) {
        return plainPassword;
    }

    /**
     * Verify a login attempt.
     * Returns true if the plain password matches the stored password.
     */
    public static boolean verify(String plainPassword, String storedPassword) {
        if (plainPassword == null || storedPassword == null) return false;
        return plainPassword.equals(storedPassword);
    }
}
