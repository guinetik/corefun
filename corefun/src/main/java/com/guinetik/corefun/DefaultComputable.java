package com.guinetik.corefun;

/**
 * Default immutable implementation of {@link Computable}.
 * <p>
 * {@code DefaultComputable} provides a simple, thread-safe implementation of the
 * {@link Computable} interface. The wrapped value is stored as a final field,
 * ensuring immutability.
 * </p>
 *
 * <h2>Thread Safety</h2>
 * <p>
 * This class is immutable and therefore thread-safe. The wrapped value itself
 * should also be immutable or thread-safe for the container to be fully thread-safe.
 * </p>
 *
 * <h2>Usage</h2>
 * <p>
 * Typically, you should use {@link Computable#of(Object)} rather than constructing
 * this class directly:
 * </p>
 * <pre>{@code
 * Computable<String> comp = Computable.of("value");  // Preferred
 * Computable<String> comp2 = new DefaultComputable<>("value");  // Also valid
 * }</pre>
 *
 * @param <T> the type of the value
 * @author Guinetik &lt;guinetik@gmail.com&gt;
 * @since 0.1.0
 * @see Computable
 */
public class DefaultComputable<T> implements Computable<T> {
    private final T value;

    /**
     * Creates a new DefaultComputable with the given value.
     *
     * @param value the value to wrap
     */
    public DefaultComputable(T value) {
        this.value = value;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Computable[" + value + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DefaultComputable)) return false;
        DefaultComputable<?> other = (DefaultComputable<?>) obj;
        return value == null ? other.value == null : value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value == null ? 0 : value.hashCode();
    }
}
