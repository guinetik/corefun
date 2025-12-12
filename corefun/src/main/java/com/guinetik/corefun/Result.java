package com.guinetik.corefun;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A functional container representing either a success or failure outcome.
 * <p>
 * {@code Result} is an Either-style monad that encapsulates either a successful value
 * or an error, providing a functional approach to error handling without exceptions.
 * This eliminates the need for null checks and exception handling in business logic,
 * promoting cleaner, more composable code.
 * </p>
 *
 * <h2>Key Features</h2>
 * <ul>
 *   <li><b>Type-safe error handling</b> - Errors are part of the type signature</li>
 *   <li><b>Functional composition</b> - Chain operations with {@link #map} and {@link #flatMap}</li>
 *   <li><b>Pattern matching</b> - Use {@link #fold} to handle both cases uniformly</li>
 *   <li><b>Validation support</b> - Combine with {@link #validate} for input validation</li>
 *   <li><b>Recovery</b> - Handle failures gracefully with {@link #recover}</li>
 * </ul>
 *
 * <h2>Example Usage</h2>
 * <pre class="language-java"><code>
 * Result&lt;User, String&gt; result = findUser(id);
 *
 * // Pattern matching with fold
 * String message = result.fold(
 *     error -&gt; "Error: " + error,
 *     user -&gt; "Found: " + user.getName()
 * );
 *
 * // Chaining operations
 * Result&lt;String, String&gt; greeting = result
 *     .map(user -&gt; user.getName())
 *     .map(name -&gt; "Hello, " + name);
 *
 * // Validation
 * Result&lt;Integer, String&gt; validAge = Result.success(age)
 *     .validate(a -&gt; a &gt;= 0 &amp;&amp; a &lt;= 150, a -&gt; "Invalid age: " + a);
 *
 * // Recovery from errors
 * Result&lt;User, String&gt; userOrGuest = findUser(id)
 *     .recover(error -&gt; Result.success(guestUser));
 * </code></pre>
 *
 * @param <S> the type of the success value
 * @param <F> the type of the failure value
 * @author Guinetik &lt;guinetik@gmail.com&gt;
 * @since 0.1.0
 * @see Try
 * @see SafeCallable#toResult()
 */
public abstract class Result<S, F> {

    // Private constructor to prevent external subclassing
    Result() {}

    /**
     * Creates a successful Result containing the given value.
     *
     * @param value the success value
     * @param <S> success type
     * @param <F> failure type
     * @return a successful Result
     */
    public static <S, F> Result<S, F> success(S value) {
        return new Success<>(value);
    }

    /**
     * Creates a failed Result containing the given error.
     *
     * @param error the failure value
     * @param <S> success type
     * @param <F> failure type
     * @return a failed Result
     */
    public static <S, F> Result<S, F> failure(F error) {
        return new Failure<>(error);
    }

    /**
     * Returns true if this Result represents a success.
     *
     * @return true if success, false if failure
     */
    public abstract boolean isSuccess();

    /**
     * Returns true if this Result represents a failure.
     *
     * @return true if failure, false if success
     */
    public abstract boolean isFailure();

    /**
     * Gets the success value. Throws if this is a failure.
     *
     * @return the success value
     * @throws IllegalStateException if this is a failure
     */
    public abstract S getSuccess();

    /**
     * Gets the failure value. Throws if this is a success.
     *
     * @return the failure value
     * @throws IllegalStateException if this is a success
     */
    public abstract F getFailure();

    /**
     * Applies one of two functions depending on the Result state.
     * This is the primary way to extract values from a Result.
     *
     * <p>Example:</p>
     * <pre class="language-java"><code>
     * String message = result.fold(
     *     error -&gt; "Failed: " + error,
     *     value -&gt; "Success: " + value
     * );
     * </code></pre>
     *
     * @param <R> the return type
     * @param onFailure function to apply if failure
     * @param onSuccess function to apply if success
     * @return the result of applying the appropriate function
     */
    public abstract <R> R fold(Function<? super F, ? extends R> onFailure,
                                Function<? super S, ? extends R> onSuccess);

    /**
     * Transforms the success value using the provided function.
     * If this is a failure, returns a new failure with the same error.
     *
     * @param <T> the new success type
     * @param mapper function to transform the success value
     * @return a new Result with the transformed value
     */
    public abstract <T> Result<T, F> map(Function<? super S, ? extends T> mapper);

    /**
     * Transforms the success value with a function that returns a Result.
     * Useful for chaining operations that might fail.
     *
     * <p>Example:</p>
     * <pre class="language-java"><code>
     * Result&lt;User, String&gt; user = findUser(id);
     * Result&lt;Account, String&gt; account = user.flatMap(u -&gt; findAccount(u.getAccountId()));
     * </code></pre>
     *
     * @param <T> the new success type
     * @param mapper function that returns a Result
     * @return the Result from the mapper, or a failure
     */
    public abstract <T> Result<T, F> flatMap(Function<? super S, Result<T, F>> mapper);

    /**
     * Transforms the failure value using the provided function.
     *
     * @param <E> the new failure type
     * @param mapper function to transform the failure value
     * @return a new Result with the transformed failure
     */
    public abstract <E> Result<S, E> mapFailure(Function<? super F, ? extends E> mapper);

    /**
     * Returns the success value or a default if this is a failure.
     *
     * @param defaultValue the default to return on failure
     * @return success value or default
     */
    public abstract S getOrElse(S defaultValue);

    /**
     * Returns the success value or computes a default if this is a failure.
     *
     * @param supplier supplies the default value
     * @return success value or computed default
     */
    public abstract S getOrElseGet(Supplier<? extends S> supplier);

    /**
     * Recovers from a failure by applying a function that returns a new Result.
     *
     * <p>Example:</p>
     * <pre class="language-java"><code>
     * Result&lt;User, String&gt; user = findUser(id)
     *     .recover(error -&gt; findGuestUser());
     * </code></pre>
     *
     * @param handler function to handle the failure
     * @return this if success, or the result of the handler
     */
    public abstract Result<S, F> recover(Function<? super F, Result<S, F>> handler);

    /**
     * Validates the success value against a predicate.
     * If validation fails, produces a failure using the provided function.
     *
     * @param predicate condition to test
     * @param failureProducer produces failure value if predicate fails
     * @return this if valid, or a failure
     */
    public abstract Result<S, F> validate(Function<? super S, Boolean> predicate,
                                           Function<? super S, ? extends F> failureProducer);

    /**
     * Performs an action on the value (success or failure) without altering the Result.
     *
     * @param observer consumer that receives the value
     * @return this Result unchanged
     */
    public abstract Result<S, F> peek(Consumer<Object> observer);

    /**
     * Performs an action on the success value if present.
     *
     * @param action consumer for the success value
     * @return this Result unchanged
     */
    public abstract Result<S, F> peekSuccess(Consumer<? super S> action);

    /**
     * Performs an action on the failure value if present.
     *
     * @param action consumer for the failure value
     * @return this Result unchanged
     */
    public abstract Result<S, F> peekFailure(Consumer<? super F> action);

    // --- Success implementation ---

    private static final class Success<S, F> extends Result<S, F> {
        private final S value;

        Success(S value) {
            this.value = value;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public boolean isFailure() {
            return false;
        }

        @Override
        public S getSuccess() {
            return value;
        }

        @Override
        public F getFailure() {
            throw new IllegalStateException("Cannot get failure from a success Result");
        }

        @Override
        public <R> R fold(Function<? super F, ? extends R> onFailure,
                          Function<? super S, ? extends R> onSuccess) {
            return onSuccess.apply(value);
        }

        @Override
        public <T> Result<T, F> map(Function<? super S, ? extends T> mapper) {
            return Result.success(mapper.apply(value));
        }

        @Override
        public <T> Result<T, F> flatMap(Function<? super S, Result<T, F>> mapper) {
            return mapper.apply(value);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <E> Result<S, E> mapFailure(Function<? super F, ? extends E> mapper) {
            return (Result<S, E>) this;
        }

        @Override
        public S getOrElse(S defaultValue) {
            return value;
        }

        @Override
        public S getOrElseGet(Supplier<? extends S> supplier) {
            return value;
        }

        @Override
        public Result<S, F> recover(Function<? super F, Result<S, F>> handler) {
            return this;
        }

        @Override
        public Result<S, F> validate(Function<? super S, Boolean> predicate,
                                     Function<? super S, ? extends F> failureProducer) {
            if (!predicate.apply(value)) {
                return Result.failure(failureProducer.apply(value));
            }
            return this;
        }

        @Override
        public Result<S, F> peek(Consumer<Object> observer) {
            observer.accept(value);
            return this;
        }

        @Override
        public Result<S, F> peekSuccess(Consumer<? super S> action) {
            action.accept(value);
            return this;
        }

        @Override
        public Result<S, F> peekFailure(Consumer<? super F> action) {
            return this;
        }

        @Override
        public String toString() {
            return "Success[" + value + "]";
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Success)) return false;
            Success<?, ?> other = (Success<?, ?>) obj;
            return value == null ? other.value == null : value.equals(other.value);
        }

        @Override
        public int hashCode() {
            return value == null ? 0 : value.hashCode();
        }
    }

    // --- Failure implementation ---

    private static final class Failure<S, F> extends Result<S, F> {
        private final F error;

        Failure(F error) {
            this.error = error;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public boolean isFailure() {
            return true;
        }

        @Override
        public S getSuccess() {
            throw new IllegalStateException("Cannot get success from a failure Result: " + error);
        }

        @Override
        public F getFailure() {
            return error;
        }

        @Override
        public <R> R fold(Function<? super F, ? extends R> onFailure,
                          Function<? super S, ? extends R> onSuccess) {
            return onFailure.apply(error);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Result<T, F> map(Function<? super S, ? extends T> mapper) {
            return (Result<T, F>) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Result<T, F> flatMap(Function<? super S, Result<T, F>> mapper) {
            return (Result<T, F>) this;
        }

        @Override
        public <E> Result<S, E> mapFailure(Function<? super F, ? extends E> mapper) {
            return Result.failure(mapper.apply(error));
        }

        @Override
        public S getOrElse(S defaultValue) {
            return defaultValue;
        }

        @Override
        public S getOrElseGet(Supplier<? extends S> supplier) {
            return supplier.get();
        }

        @Override
        public Result<S, F> recover(Function<? super F, Result<S, F>> handler) {
            return handler.apply(error);
        }

        @Override
        public Result<S, F> validate(Function<? super S, Boolean> predicate,
                                     Function<? super S, ? extends F> failureProducer) {
            return this;
        }

        @Override
        public Result<S, F> peek(Consumer<Object> observer) {
            observer.accept(error);
            return this;
        }

        @Override
        public Result<S, F> peekSuccess(Consumer<? super S> action) {
            return this;
        }

        @Override
        public Result<S, F> peekFailure(Consumer<? super F> action) {
            action.accept(error);
            return this;
        }

        @Override
        public String toString() {
            return "Failure[" + error + "]";
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Failure)) return false;
            Failure<?, ?> other = (Failure<?, ?>) obj;
            return error == null ? other.error == null : error.equals(other.error);
        }

        @Override
        public int hashCode() {
            return error == null ? 0 : error.hashCode();
        }
    }
}
