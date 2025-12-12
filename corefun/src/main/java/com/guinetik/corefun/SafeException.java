package com.guinetik.corefun;

/**
 * A runtime exception wrapper for checked exceptions.
 * <p>
 * {@code SafeException} provides a way to wrap checked exceptions as unchecked
 * exceptions, enabling cleaner functional code without excessive try-catch blocks.
 * It is the standard exception type thrown by the CoreFun library when operations fail.
 * </p>
 *
 * <h2>Design Philosophy</h2>
 * <p>
 * Java's checked exceptions force explicit handling at every call site, which
 * conflicts with functional programming patterns like streams and lambdas.
 * {@code SafeException} bridges this gap by allowing checked exceptions to propagate
 * as unchecked exceptions while preserving the original cause for debugging.
 * </p>
 *
 * <h2>When to Use</h2>
 * <ul>
 *   <li>Wrapping checked exceptions in lambda expressions</li>
 *   <li>Converting library exceptions to application-level exceptions</li>
 *   <li>Providing context messages while preserving the original cause</li>
 * </ul>
 *
 * <h2>Example Usage</h2>
 * <pre class="language-java"><code>
 * // Manual wrapping with context
 * try {
 *     riskyOperation();
 * } catch (IOException e) {
 *     throw new SafeException("Failed to read file: " + path, e);
 * }
 *
 * // Using the static wrap method (preserves RuntimeExceptions)
 * try {
 *     operation();
 * } catch (Exception e) {
 *     throw SafeException.wrap(e);  // Returns original if already RuntimeException
 * }
 *
 * // Adding context to any exception
 * throw SafeException.wrap("Database query failed", e);
 * </code></pre>
 *
 * @author Guinetik &lt;guinetik@gmail.com&gt;
 * @since 0.1.0
 * @see SafeRunnable
 * @see SafeCallable
 * @see SafeExecutor
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
