package exception;

/**
 * ConflictException — thrown when an action conflicts with existing data.
 * Example: room already reserved for this time slot, book already borrowed.
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
