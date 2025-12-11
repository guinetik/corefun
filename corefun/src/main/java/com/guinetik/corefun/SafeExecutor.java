package com.guinetik.corefun;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Provides safe execution of operations with timing, logging, and error handling.
 * <p>
 * {@code SafeExecutor} extends {@link Loggable} and combines timing, logging, and
 * exception handling into a single interface for safely executing operations.
 * By default, it logs operation start, completion (with timing), and errors.
 * </p>
 *
 * <p>Example usage with SLF4J:</p>
 * <pre>{@code
 * public class DataProcessor implements SafeExecutor {
 *     private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DataProcessor.class);
 *
 *     @Override
 *     public Loggable.Logger logger() {
 *         return Loggable.Logger.of(LOG::info, LOG::warn, LOG::error);
 *     }
 *
 *     public Data process() {
 *         return safely("Process data", () -> {
 *             // processing logic - automatically logged
 *             return processedData;
 *         });
 *     }
 * }
 * }</pre>
 *
 * <p>Example with simple println:</p>
 * <pre>{@code
 * public class SimpleProcessor implements SafeExecutor {
 *     @Override
 *     public Loggable.Logger logger() {
 *         return Loggable.Logger.println();
 *     }
 *
 *     public void doWork() {
 *         safely("Do work", () -> performWork());
 *         // Logs: [INFO] Executing: Do work
 *         // Logs: [INFO] Completed: Do work in 123ms
 *     }
 * }
 * }</pre>
 */
public interface SafeExecutor extends Loggable {

    /**
     * Called when an operation starts. Default implementation logs via {@link #logger()}.
     *
     * @param description the operation description
     */
    default void onStart(String description) {
        logger().info("Executing: " + description);
    }

    /**
     * Called when an operation completes successfully. Default implementation logs via {@link #logger()}.
     *
     * @param description the operation description
     * @param milliseconds the execution time
     */
    default void onComplete(String description, long milliseconds) {
        logger().info("Completed: " + description + " in " + milliseconds + "ms");
    }

    /**
     * Called when an operation fails. Default implementation logs via {@link #logger()}.
     *
     * @param description the operation description
     * @param e the exception that occurred
     */
    default void onError(String description, Exception e) {
        logger().error("Failed: " + description, e);
    }

    /**
     * Executes a callable safely with timing and error handling.
     *
     * @param <T> the return type
     * @param description description of the operation
     * @param action the operation to execute
     * @return the result of the operation
     * @throws SafeException if the operation fails
     */
    default <T> T safely(String description, Callable<T> action) {
        onStart(description);
        long startTime = System.nanoTime();
        try {
            T result = action.call();
            long durationMs = (System.nanoTime() - startTime) / 1_000_000;
            onComplete(description, durationMs);
            return result;
        } catch (Exception e) {
            onError(description, e);
            throw SafeException.wrap(description, e);
        }
    }

    /**
     * Executes a runnable safely with timing and error handling.
     *
     * @param description description of the operation
     * @param action the operation to execute
     * @throws SafeException if the operation fails
     */
    default void safelyVoid(String description, Runnable action) {
        safely(description, () -> {
            action.run();
            return null;
        });
    }

    /**
     * Executes a SafeRunnable safely with timing and error handling.
     *
     * @param description description of the operation
     * @param action the operation to execute
     * @throws SafeException if the operation fails
     */
    default void safelySafe(String description, SafeRunnable action) {
        safely(description, () -> {
            action.run();
            return null;
        });
    }

    /**
     * Executes an operation and returns a Result instead of throwing.
     *
     * @param <T> the return type
     * @param description description of the operation
     * @param action the operation to execute
     * @return Success with result, or Failure with error message
     */
    default <T> Result<T, String> safelyResult(String description, Callable<T> action) {
        onStart(description);
        long startTime = System.nanoTime();
        try {
            T result = action.call();
            long durationMs = (System.nanoTime() - startTime) / 1_000_000;
            onComplete(description, durationMs);
            return Result.success(result);
        } catch (Exception e) {
            onError(description, e);
            String message = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
            return Result.failure(description + " failed: " + message);
        }
    }

    /**
     * Creates a SafeExecutor with the given logger.
     *
     * @param logger the logger to use
     * @return a SafeExecutor that uses the provided logger
     */
    static SafeExecutor withLogger(Loggable.Logger logger) {
        return () -> logger;
    }

    /**
     * Creates a SafeExecutor that prints to standard output.
     *
     * @return a SafeExecutor that prints execution info
     */
    static SafeExecutor println() {
        return withLogger(Loggable.Logger.println());
    }

    /**
     * Creates a no-op SafeExecutor that doesn't log.
     *
     * @return a silent SafeExecutor
     */
    static SafeExecutor noop() {
        return withLogger(Loggable.Logger.noop());
    }
}
