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
 * and composability, making it useful for data processing pipelines and building
 * domain-specific languages (DSLs).
 * </p>
 *
 * <h2>Key Features</h2>
 * <ul>
 *   <li><b>Immutable transformations</b> - All operations return new instances</li>
 *   <li><b>Fluent API</b> - Chain operations for readable data pipelines</li>
 *   <li><b>Validation</b> - Check values with predicates using {@link #isValid} and {@link #filter}</li>
 *   <li><b>Combination</b> - Combine multiple values with {@link #combine}</li>
 *   <li><b>Side effects</b> - Debug with {@link #peek} without breaking the chain</li>
 * </ul>
 *
 * <h2>Example Usage</h2>
 * <pre class="language-java"><code>
 * // Simple transformation
 * Computable&lt;String&gt; name = Computable.of("hello");
 * Computable&lt;Integer&gt; length = name.map(String::length);
 * System.out.println(length.getValue()); // prints 5
 *
 * // Chained transformations
 * String result = Computable.of(100)
 *     .map(n -&gt; n * 2)
 *     .filter(n -&gt; n &gt; 0, () -&gt; 0)
 *     .map(n -&gt; "Result: " + n)
 *     .getValue();
 *
 * // Combining values
 * Computable&lt;Integer&gt; a = Computable.of(10);
 * Computable&lt;Integer&gt; b = Computable.of(5);
 * int sum = a.combine(b, Integer::sum).getValue(); // 15
 *
 * // Validation
 * boolean valid = Computable.of(email)
 *     .isValid(e -&gt; e.contains("@"));
 * </code></pre>
 *
 * @param <T> the type of the value being managed
 * @author Guinetik &lt;guinetik@gmail.com&gt;
 * @since 0.1.0
 * @see DefaultComputable
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
     * <pre class="language-java"><code>
     * Computable&lt;String&gt; original = Computable.of("hello");
     * Computable&lt;Integer&gt; length = original.map(String::length);
     * </code></pre>
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
     * <pre class="language-java"><code>
     * Computable&lt;Integer&gt; age = Computable.of(30);
     * Computable&lt;String&gt; status = age.flatMap(a -&gt; Computable.of(a &gt;= 18 ? "Adult" : "Minor"));
     * </code></pre>
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
     * <pre class="language-java"><code>
     * Computable&lt;Integer&gt; age = Computable.of(25);
     * boolean isAdult = age.isValid(a -&gt; a &gt;= 18);
     * </code></pre>
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
     * <pre class="language-java"><code>
     * Computable&lt;Integer&gt; age = Computable.of(15);
     * Computable&lt;Integer&gt; adultAge = age.filter(a -&gt; a &gt;= 18, () -&gt; 18);
     * </code></pre>
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
     * <pre class="language-java"><code>
     * Computable&lt;String&gt; name = Computable.of("John");
     * String greeting = name.reduce("Hello, ", (acc, n) -&gt; acc + n);
     * </code></pre>
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
     * <pre class="language-java"><code>
     * Computable&lt;Integer&gt; a = Computable.of(10);
     * Computable&lt;Integer&gt; b = Computable.of(5);
     * Computable&lt;Integer&gt; sum = a.combine(b, Integer::sum);
     * </code></pre>
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
     * <pre class="language-java"><code>
     * Computable&lt;String&gt; name = Computable.of("Alice");
     * name.peek(System.out::println).map(String::toUpperCase);
     * </code></pre>
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
     * <pre class="language-java"><code>
     * Computable&lt;String&gt; nullable = Computable.of(null);
     * String name = nullable.orElse("Unknown");
     * </code></pre>
     *
     * @param other the default value
     * @return the value or the default
     */
    default T orElse(T other) {
        T value = getValue();
        return value != null ? value : other;
    }
}
