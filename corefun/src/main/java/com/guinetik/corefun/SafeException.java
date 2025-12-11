package com.guinetik.corefun;

/**
 * A runtime exception wrapper for checked exceptions.
 * <p>
 * {@code SafeException} provides a way to wrap checked exceptions as unchecked
 * exceptions, enabling cleaner functional code without excessive try-catch blocks.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * try {
 *     riskyOperation();
 * } catch (IOException e) {
 *     throw new SafeException("Failed to read file", e);
 * }
 * }</pre>
 */
public class SafeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a SafeException wrapping the given cause.
     *
     * @param cause the underlying exception
     */
    public SafeException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a SafeException with a message and cause.
     *
     * @param message descriptive message about the error context
     * @param cause the underlying exception
     */
    public SafeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a SafeException with just a message.
     *
     * @param message descriptive message about the error
     */
    public SafeException(String message) {
        super(message);
    }

    /**
     * Wraps a checked exception as a SafeException.
     * If the exception is already a RuntimeException, it is returned as-is.
     *
     * @param e the exception to wrap
     * @return a RuntimeException (either the original or wrapped)
     */
    public static RuntimeException wrap(Exception e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new SafeException(e);
    }

    /**
     * Wraps a checked exception with a descriptive message.
     *
     * @param message context description
     * @param e the exception to wrap
     * @return a SafeException wrapping the original
     */
    public static SafeException wrap(String message, Exception e) {
        return new SafeException(message, e);
    }
}
