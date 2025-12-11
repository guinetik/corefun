package com.guinetik.corefun;

/**
 * A functional interface for operations that may throw checked exceptions.
 * <p>
 * {@code SafeRunnable} allows writing lambda expressions for operations that throw
 * checked exceptions, which can then be converted to standard {@link Runnable}
 * instances with automatic exception wrapping.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * SafeRunnable task = () -> {
 *     Files.delete(path);  // throws IOException
 * };
 *
 * // Convert to standard Runnable for use with executors
 * executor.execute(task.toRunnable());
 * }</pre>
 */
@FunctionalInterface
public interface SafeRunnable {

    /**
     * Executes the operation, potentially throwing a checked exception.
     *
     * @throws Exception if the operation fails
     */
    void run() throws Exception;

    /**
     * Converts this SafeRunnable to a standard Runnable.
     * Any checked exceptions are wrapped in {@link SafeException}.
     *
     * @return a Runnable that wraps checked exceptions
     */
    default Runnable toRunnable() {
        return () -> {
            try {
                run();
            } catch (Exception e) {
                throw SafeException.wrap(e);
            }
        };
    }

    /**
     * Creates a SafeRunnable from a standard Runnable.
     *
     * @param runnable the Runnable to wrap
     * @return a SafeRunnable that delegates to the Runnable
     */
    static SafeRunnable from(Runnable runnable) {
        return runnable::run;
    }

    /**
     * Executes this SafeRunnable, wrapping any exception.
     * Convenience method for immediate execution.
     *
     * @throws SafeException if execution fails
     */
    default void execute() {
        toRunnable().run();
    }
}
