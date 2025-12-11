package com.guinetik.corefun;

import java.util.concurrent.Callable;

/**
 * Interface for timing the execution of operations.
 * <p>
 * {@code Timing} provides methods for measuring execution time with pluggable
 * logging. Implement {@link #onTimed(String, long)} to receive timing results.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * public class MyService implements Timing {
 *     @Override
 *     public void onTimed(String description, long milliseconds) {
 *         logger.info("{} took {}ms", description, milliseconds);
 *     }
 *
 *     public User loadUser(long id) {
 *         return timed("Load user " + id, () -> userRepository.findById(id));
 *     }
 * }
 * }</pre>
 */
public interface Timing {

    /**
     * Called after an operation completes with timing information.
     * Implement this to log, record metrics, or otherwise handle timing data.
     *
     * @param description the operation description
     * @param milliseconds the execution time in milliseconds
     */
    void onTimed(String description, long milliseconds);

    /**
     * Executes a callable and times its execution.
     *
     * @param <T> the return type
     * @param description description of the operation (for logging)
     * @param action the operation to execute
     * @return the result of the callable
     * @throws SafeException if the operation throws an exception
     */
    default <T> T timed(String description, Callable<T> action) {
        long startTime = System.nanoTime();
        try {
            return action.call();
        } catch (Exception e) {
            throw SafeException.wrap(description, e);
        } finally {
            long durationMs = (System.nanoTime() - startTime) / 1_000_000;
            onTimed(description, durationMs);
        }
    }

    /**
     * Executes a runnable and times its execution.
     *
     * @param description description of the operation (for logging)
     * @param action the operation to execute
     * @throws SafeException if the operation throws an exception
     */
    default void timedVoid(String description, Runnable action) {
        timed(description, () -> {
            action.run();
            return null;
        });
    }

    /**
     * Executes a SafeRunnable and times its execution.
     *
     * @param description description of the operation (for logging)
     * @param action the operation to execute
     * @throws SafeException if the operation throws an exception
     */
    default void timedSafe(String description, SafeRunnable action) {
        timed(description, () -> {
            action.run();
            return null;
        });
    }

    /**
     * Creates a Timing instance that prints to standard output.
     *
     * @return a Timing that prints timing info
     */
    static Timing println() {
        return (description, ms) -> System.out.println(description + " completed in " + ms + "ms");
    }

    /**
     * Creates a Timing instance that discards timing information.
     *
     * @return a no-op Timing
     */
    static Timing noop() {
        return (description, ms) -> {};
    }
}
