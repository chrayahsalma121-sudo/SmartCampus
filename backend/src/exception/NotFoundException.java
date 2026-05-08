package exception;

/**
 * NotFoundException — thrown when a requested resource does not exist.
 * Example: book not found, reservation not found, user not found.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
