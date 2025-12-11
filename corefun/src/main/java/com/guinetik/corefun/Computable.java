package com.guinetik.corefun;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A value wrapper that provides functional composition operations.
 * <p>
 * {@code Computable} encapsulates a value and provides methods for transforming,
 * validating, and combining values in a functional style. It promotes immutability
 * and composability, making it useful for data processing pipelines.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * Computable<String> name = Computable.of("hello");
 * Computable<Integer> length = name.map(String::length);
 * System.out.println(length.getValue()); // prints 5
 * }</pre>
 *
 * @param <T> the type of the value being managed
 */
public interface Computable<T> {

    /**
     * Creates a new Computable wrapping the given value.
     *
     * @param value the value to wrap
     * @param <T> the type of the value
     * @return a new Computable containing the value
     */
    static <T> Computable<T> of(T value) {
        return new DefaultComputable<>(value);
    }

    /**
     * Retrieves the value managed by this computable.
     *
     * @return the current value
     */
    T getValue();

    /**
     * Transforms the value using the provided function.
     *
     * <p>Example:</p>
     * <pre>{@code
     * Computable<String> original = Computable.of("hello");
     * Computable<Integer> length = original.map(String::length);
     * }</pre>
     *
     * @param <R> the type of the result
     * @param transformer a function to transform the value
     * @return a new Computable with the transformed value
     */
    default <R> Computable<R> map(Function<? super T, ? extends R> transformer) {
        return new DefaultComputable<>(transformer.apply(getValue()));
    }

    /**
     * Applies a transformation that returns a Computable and flattens the result.
     *
     * <p>Example:</p>
     * <pre>{@code
     * Computable<Integer> age = Computable.of(30);
     * Computable<String> status = age.flatMap(a -> Computable.of(a >= 18 ? "Adult" : "Minor"));
     * }</pre>
     *
     * @param <R> the type of the result
     * @param transformer a function that returns a Computable
     * @return the Computable returned by the transformer
     */
    default <R> Computable<R> flatMap(Function<? super T, Computable<R>> transformer) {
        return transformer.apply(getValue());
    }

    /**
     * Validates the value using the specified predicate.
     *
     * <p>Example:</p>
     * <pre>{@code
     * Computable<Integer> age = Computable.of(25);
     * boolean isAdult = age.isValid(a -> a >= 18);
     * }</pre>
     *
     * @param validator a predicate to test the value
     * @return true if the value passes validation
     */
    default boolean isValid(Predicate<T> validator) {
        return validator.test(getValue());
    }

    /**
     * Filters the value, returning a default if the predicate fails.
     *
     * <p>Example:</p>
     * <pre>{@code
     * Computable<Integer> age = Computable.of(15);
     * Computable<Integer> adultAge = age.filter(a -> a >= 18, () -> 18);
     * }</pre>
     *
     * @param predicate the condition to test
     * @param defaultValue supplier for the default value if test fails
     * @return this Computable if predicate passes, otherwise a new one with default
     */
    default Computable<T> filter(Predicate<T> predicate, Supplier<T> defaultValue) {
        return predicate.test(getValue()) ? this : new DefaultComputable<>(defaultValue.get());
    }

    /**
     * Reduces the value by combining it with an initial value.
     *
     * <p>Example:</p>
     * <pre>{@code
     * Computable<String> name = Computable.of("John");
     * String greeting = name.reduce("Hello, ", (acc, n) -> acc + n);
     * }</pre>
     *
     * @param <R> the type of the result
     * @param initialValue the initial value for reduction
     * @param accumulator the reduction function
     * @return the result of the reduction
     */
    default <R> R reduce(R initialValue, BiFunction<R, T, R> accumulator) {
        return accumulator.apply(initialValue, getValue());
    }

    /**
     * Combines this value with another Computable's value.
     *
     * <p>Example:</p>
     * <pre>{@code
     * Computable<Integer> a = Computable.of(10);
     * Computable<Integer> b = Computable.of(5);
     * Computable<Integer> sum = a.combine(b, Integer::sum);
     * }</pre>
     *
     * @param <U> the type of the other value
     * @param <R> the type of the result
     * @param other another Computable
     * @param combiner function to combine the values
     * @return a new Computable with the combined result
     */
    default <U, R> Computable<R> combine(Computable<U> other, BiFunction<T, U, R> combiner) {
        return new DefaultComputable<>(combiner.apply(this.getValue(), other.getValue()));
    }

    /**
     * Performs an action on the value without altering it.
     *
     * <p>Example:</p>
     * <pre>{@code
     * Computable<String> name = Computable.of("Alice");
     * name.peek(System.out::println).map(String::toUpperCase);
     * }</pre>
     *
     * @param action a consumer to perform on the value
     * @return this Computable for chaining
     */
    default Computable<T> peek(Consumer<T> action) {
        action.accept(getValue());
        return this;
    }

    /**
     * Returns the value if non-null, otherwise returns the default.
     *
     * <p>Example:</p>
     * <pre>{@code
     * Computable<String> nullable = Computable.of(null);
     * String name = nullable.orElse("Unknown");
     * }</pre>
     *
     * @param other the default value
     * @return the value or the default
     */
    default T orElse(T other) {
        T value = getValue();
        return value != null ? value : other;
    }
}
