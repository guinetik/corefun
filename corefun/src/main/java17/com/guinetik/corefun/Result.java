package com.guinetik.corefun;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A functional container representing either a success or failure outcome.
 * <p>
 * This is the Java 17+ version using sealed interfaces for exhaustiveness checking
 * in pattern matching.
 * </p>
 *
 * <p>Example with pattern matching (Java 21+):</p>
 * <pre>{@code
 * String message = switch (result) {
 *     case Result.Success<User, String> s -> "Found: " + s.value().getName();
 *     case Result.Failure<User, String> f -> "Error: " + f.error();
 * };
 * }</pre>
 *
 * @param <S> the type of the success value
 * @param <F> the type of the failure value
 */
public sealed interface Result<S, F> permits Result.Success, Result.Failure {

    /**
     * Creates a successful Result containing the given value.
     */
    static <S, F> Result<S, F> success(S value) {
        return new Success<>(value);
    }

    /**
     * Creates a failed Result containing the given error.
     */
    static <S, F> Result<S, F> failure(F error) {
        return new Failure<>(error);
    }

    boolean isSuccess();
    boolean isFailure();
    S get();
    F getError();

    <R> R fold(Function<? super F, ? extends R> onFailure,
               Function<? super S, ? extends R> onSuccess);

    <T> Result<T, F> map(Function<? super S, ? extends T> mapper);
    <T> Result<T, F> flatMap(Function<? super S, Result<T, F>> mapper);
    <E> Result<S, E> mapFailure(Function<? super F, ? extends E> mapper);

    S getOrElse(S defaultValue);
    S getOrElseGet(Supplier<? extends S> supplier);

    Result<S, F> recover(Function<? super F, Result<S, F>> handler);
    Result<S, F> validate(Function<? super S, Boolean> predicate,
                          Function<? super S, ? extends F> failureProducer);

    Result<S, F> peek(Consumer<Object> observer);
    Result<S, F> peekSuccess(Consumer<? super S> action);
    Result<S, F> peekFailure(Consumer<? super F> action);

    /**
     * Converts a list of Results into a Result of a list.
     * Returns the first failure encountered, or a success containing all values.
     */
    static <S, F> Result<List<S>, F> sequence(List<Result<S, F>> results) {
        List<S> values = new ArrayList<>(results.size());
        for (Result<S, F> result : results) {
            if (result.isFailure()) {
                return Result.failure(result.getError());
            }
            values.add(result.get());
        }
        return Result.success(values);
    }

    /**
     * Applies a function to each element and sequences the results.
     */
    static <T, S, F> Result<List<S>, F> traverse(List<T> items, Function<? super T, Result<S, F>> mapper) {
        List<S> values = new ArrayList<>(items.size());
        for (T item : items) {
            Result<S, F> result = mapper.apply(item);
            if (result.isFailure()) {
                return Result.failure(result.getError());
            }
            values.add(result.get());
        }
        return Result.success(values);
    }

    /**
     * Success implementation as a record.
     */
    record Success<S, F>(S value) implements Result<S, F> {
        @Override public boolean isSuccess() { return true; }
        @Override public boolean isFailure() { return false; }
        @Override public S get() { return value; }
        @Override public F getError() {
            throw new IllegalStateException("Cannot get error from a success Result");
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

        @Override public S getOrElse(S defaultValue) { return value; }
        @Override public S getOrElseGet(Supplier<? extends S> supplier) { return value; }

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
    }

    /**
     * Failure implementation as a record.
     */
    record Failure<S, F>(F error) implements Result<S, F> {
        @Override public boolean isSuccess() { return false; }
        @Override public boolean isFailure() { return true; }
        @Override public S get() {
            throw new IllegalStateException("Cannot get value from a failure Result: " + error);
        }
        @Override public F getError() { return error; }

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

        @Override public S getOrElse(S defaultValue) { return defaultValue; }
        @Override public S getOrElseGet(Supplier<? extends S> supplier) { return supplier.get(); }

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
    }
}
