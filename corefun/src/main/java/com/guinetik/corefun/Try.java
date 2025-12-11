package com.guinetik.corefun;

import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utility class for executing operations that may fail and converting to Results.
 * <p>
 * {@code Try} provides static methods for wrapping operations that might throw
 * exceptions and converting them to {@link Result} types for functional error handling.
 * It bridges the gap between exception-based APIs and functional error handling.
 * </p>
 *
 * <h2>Key Features</h2>
 * <ul>
 *   <li><b>Exception to Result conversion</b> - Wrap throwing operations in Results</li>
 *   <li><b>Multiple error representations</b> - String messages, full Exceptions, or custom types</li>
 *   <li><b>Lazy evaluation</b> - Defer execution until result is needed</li>
 *   <li><b>Safe defaults</b> - Execute with fallback values on failure</li>
 * </ul>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * // Execute and get Result with string error
 * Result<String, String> content = Try.of(() -> Files.readString(path));
 *
 * // Chain operations - errors propagate automatically
 * Result<Integer, String> lineCount = Try.of(() -> Files.readString(path))
 *     .map(s -> s.split("\n").length);
 *
 * // Keep full exception for detailed error handling
 * Result<User, Exception> user = Try.ofException(() -> userService.find(id));
 *
 * // Custom error mapping
 * Result<Config, AppError> config = Try.of(
 *     () -> loadConfig(path),
 *     e -> new AppError("CONFIG_LOAD_FAILED", e)
 * );
 *
 * // Safe execution with default
 * int port = Try.getOrDefault(() -> Integer.parseInt(env), 8080);
 * }</pre>
 *
 * @author Guinetik &lt;guinetik@gmail.com&gt;
 * @since 0.1.0
 * @see Result
 * @see SafeCallable
 */
public final class Try {

    private Try() {} // Utility class

    /**
     * Executes an operation and returns a Result with string error messages.
     *
     * @param <T> the result type
     * @param action the operation to execute
     * @return Success with the result, or Failure with error message
     */
    public static <T> Result<T, String> of(Callable<T> action) {
        try {
            return Result.success(action.call());
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
            return Result.failure(message);
        }
    }

    /**
     * Executes an operation and returns a Result preserving the exception.
     *
     * @param <T> the result type
     * @param action the operation to execute
     * @return Success with the result, or Failure with the exception
     */
    public static <T> Result<T, Exception> ofException(Callable<T> action) {
        try {
            return Result.success(action.call());
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    /**
     * Executes an operation and returns a Result with custom error mapping.
     *
     * @param <T> the result type
     * @param <F> the failure type
     * @param action the operation to execute
     * @param errorMapper function to convert exception to failure type
     * @return Success with the result, or Failure with mapped error
     */
    public static <T, F> Result<T, F> of(Callable<T> action, Function<Exception, F> errorMapper) {
        try {
            return Result.success(action.call());
        } catch (Exception e) {
            return Result.failure(errorMapper.apply(e));
        }
    }

    /**
     * Executes a runnable and returns a Result indicating success or failure.
     *
     * @param action the operation to execute
     * @return Success with null, or Failure with error message
     */
    public static Result<Void, String> run(SafeRunnable action) {
        try {
            action.run();
            return Result.success(null);
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
            return Result.failure(message);
        }
    }

    /**
     * Lazily wraps an operation, executing only when the result is accessed.
     *
     * @param <T> the result type
     * @param action the operation to execute lazily
     * @return a Supplier that executes the action when called
     */
    public static <T> Supplier<Result<T, String>> lazy(Callable<T> action) {
        return () -> of(action);
    }

    /**
     * Executes an operation, returning a default value on failure.
     *
     * @param <T> the result type
     * @param action the operation to execute
     * @param defaultValue the default value on failure
     * @return the result or default value
     */
    public static <T> T getOrDefault(Callable<T> action, T defaultValue) {
        try {
            return action.call();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Executes an operation, returning null on failure.
     *
     * @param <T> the result type
     * @param action the operation to execute
     * @return the result or null
     */
    public static <T> T getOrNull(Callable<T> action) {
        return getOrDefault(action, null);
    }
}
