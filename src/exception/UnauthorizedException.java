package exception;

/**
 * UnauthorizedException — thrown when a user is not authenticated
 * or does not have the required role for an action.
 * Example: missing/invalid token, wrong role accessing a protected endpoint.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
