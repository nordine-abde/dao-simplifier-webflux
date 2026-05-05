package anordine.dao.simplifier.webflux.cursor;

/**
 * Raised when an opaque cursor string cannot be decoded as the expected cursor
 * value type.
 */
public class CursorDecodingException extends IllegalArgumentException {

    public CursorDecodingException(String message) {
        super(message);
    }

    public CursorDecodingException(String message, Throwable cause) {
        super(message, cause);
    }
}
