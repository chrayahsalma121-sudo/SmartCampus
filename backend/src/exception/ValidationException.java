package exception;

/**
 * ValidationException — thrown when request input fails validation.
 * Example: missing required field, invalid enum value, bad date format.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
