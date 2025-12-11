package com.guinetik.corefun;

import java.util.concurrent.Callable;

/**
 * A functional interface for operations that return a value and may throw checked exceptions.
 * <p>
 * {@code SafeCallable} extends the standard {@link Callable} interface with convenient
 * methods for exception handling and conversion to {@link Result} types. It provides
 * multiple strategies for handling potential failures.
 * </p>
 *
 * <h2>Key Features</h2>
 * <ul>
 *   <li><b>Exception wrapping</b> - Convert checked exceptions to unchecked with {@link #getOrThrow()}</li>
 *   <li><b>Default values</b> - Provide fallbacks with {@link #getOrElse(Object)}</li>
 *   <li><b>Result conversion</b> - Transform to functional Results with {@link #toResult()}</li>
 *   <li><b>Callable compatibility</b> - Use anywhere a standard Callable is expected</li>
 * </ul>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * SafeCallable<String> reader = () -> Files.readString(path);
 *
 * // Execute with automatic exception wrapping
 * String content = reader.getOrThrow();  // throws SafeException on failure
 *
 * // Or use a default value on failure
 * String contentOrDefault = reader.getOrElse("default content");
 *
 * // Convert to Result for functional handling
 * Result<String, Exception> result = reader.toResult();
 * result.fold(
 *     ex -> handleError(ex),
 *     content -> processContent(content)
 * );
 *
 * // Create a callable that always returns a value
 * SafeCallable<Config> defaultConfig = SafeCallable.of(Config.defaults());
 * }</pre>
 *
 * @param <T> the type of the result
 * @author Guinetik &lt;guinetik@gmail.com&gt;
 * @since 0.1.0
 * @see SafeRunnable
 * @see SafeException
 * @see Result
 * @see Try
 */
@FunctionalInterface
public interface SafeCallable<T> extends Callable<T> {

    /**
     * Computes the result, potentially throwing a checked exception.
     *
     * @return the computed result
     * @throws Exception if computation fails
     */
    @Override
    T call() throws Exception;

    /**
     * Executes and returns the result, wrapping any exception in SafeException.
     *
     * @return the computed result
     * @throws SafeException if computation fails
     */
    default T getOrThrow() {
        try {
            return call();
        } catch (Exception e) {
            throw SafeException.wrap(e);
        }
    }

    /**
     * Executes and returns the result, or a default value on failure.
     *
     * @param defaultValue the value to return on failure
     * @return the computed result or default
     */
    default T getOrElse(T defaultValue) {
        try {
            return call();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Converts this callable to a Result, capturing any exception.
     *
     * @return Success with the result, or Failure with the exception
     */
    default Result<T, Exception> toResult() {
        try {
            return Result.success(call());
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    /**
     * Converts this callable to a Result with a string error message.
     *
     * @return Success with the result, or Failure with error message
     */
    default Result<T, String> toResultWithMessage() {
        try {
            return Result.success(call());
        } catch (Exception e) {
            return Result.failure(e.getMessage() != null ? e.getMessage() : e.getClass().getName());
        }
    }

    /**
     * Creates a SafeCallable from a standard Callable.
     *
     * @param <T> the result type
     * @param callable the Callable to wrap
     * @return a SafeCallable that delegates to the Callable
     */
    static <T> SafeCallable<T> from(Callable<T> callable) {
        return callable::call;
    }

    /**
     * Creates a SafeCallable that always returns the given value.
     *
     * @param <T> the result type
     * @param value the value to return
     * @return a SafeCallable that returns the value
     */
    static <T> SafeCallable<T> of(T value) {
        return () -> value;
    }
}
