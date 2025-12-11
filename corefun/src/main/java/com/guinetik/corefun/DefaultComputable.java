package com.guinetik.corefun;

/**
 * Default immutable implementation of {@link Computable}.
 *
 * @param <T> the type of the value
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
