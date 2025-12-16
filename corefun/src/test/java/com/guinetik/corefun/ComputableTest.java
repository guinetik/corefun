package com.guinetik.corefun;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Computable} and {@link DefaultComputable}.
 */
@DisplayName("Computable")
public class ComputableTest {

    @Nested
    @DisplayName("of (factory)")
    class OfTests {

        @Test
        @DisplayName("creates computable with value")
        void of_createsWithValue() {
            Computable<String> c = Computable.of("hello");

            assertEquals("hello", c.getValue());
        }

        @Test
        @DisplayName("handles null value")
        void of_handlesNull() {
            Computable<String> c = Computable.of(null);

            assertNull(c.getValue());
        }
    }

    @Nested
    @DisplayName("map")
    class MapTests {

        @Test
        @DisplayName("transforms value")
        void map_transformsValue() {
            Computable<Integer> result = Computable.of("hello").map(String::length);

            assertEquals(5, result.getValue());
        }

        @Test
        @DisplayName("chains multiple maps")
        void map_chains() {
            Computable<String> result = Computable.of(10)
                .map(n -> n * 2)
                .map(n -> n + 5)
                .map(n -> "Result: " + n);

            assertEquals("Result: 25", result.getValue());
        }
    }

    @Nested
    @DisplayName("flatMap")
    class FlatMapTests {

        @Test
        @DisplayName("flattens nested Computable")
        void flatMap_flattens() {
            Computable<String> result = Computable.of(25)
                .flatMap(age -> Computable.of(age >= 18 ? "Adult" : "Minor"));

            assertEquals("Adult", result.getValue());
        }

        @Test
        @DisplayName("chains flatMap operations")
        void flatMap_chains() {
            Computable<Integer> result = Computable.of(5)
                .flatMap(n -> Computable.of(n * 2))
                .flatMap(n -> Computable.of(n + 3));

            assertEquals(13, result.getValue());
        }
    }

    @Nested
    @DisplayName("isValid")
    class IsValidTests {

        @Test
        @DisplayName("returns true when predicate passes")
        void isValid_returnsTrue() {
            boolean valid = Computable.of(25).isValid(age -> age >= 18);

            assertTrue(valid);
        }

        @Test
        @DisplayName("returns false when predicate fails")
        void isValid_returnsFalse() {
            boolean valid = Computable.of(15).isValid(age -> age >= 18);

            assertFalse(valid);
        }
    }

    @Nested
    @DisplayName("filter")
    class FilterTests {

        @Test
        @DisplayName("keeps value when predicate passes")
        void filter_keepsValue() {
            Computable<Integer> result = Computable.of(25)
                .filter(age -> age >= 18, () -> 0);

            assertEquals(25, result.getValue());
        }

        @Test
        @DisplayName("uses default when predicate fails")
        void filter_usesDefault() {
            Computable<Integer> result = Computable.of(15)
                .filter(age -> age >= 18, () -> 18);

            assertEquals(18, result.getValue());
        }
    }

    @Nested
    @DisplayName("reduce")
    class ReduceTests {

        @Test
        @DisplayName("combines value with initial")
        void reduce_combines() {
            String result = Computable.of("World")
                .reduce("Hello, ", (acc, val) -> acc + val);

            assertEquals("Hello, World", result);
        }

        @Test
        @DisplayName("works with different types")
        void reduce_differentTypes() {
            int result = Computable.of("hello")
                .reduce(0, (acc, val) -> acc + val.length());

            assertEquals(5, result);
        }
    }

    @Nested
    @DisplayName("combine")
    class CombineTests {

        @Test
        @DisplayName("combines two computables")
        void combine_twoValues() {
            Computable<Integer> a = Computable.of(10);
            Computable<Integer> b = Computable.of(5);

            Computable<Integer> sum = a.combine(b, Integer::sum);

            assertEquals(15, sum.getValue());
        }

        @Test
        @DisplayName("combines different types")
        void combine_differentTypes() {
            Computable<String> name = Computable.of("Alice");
            Computable<Integer> age = Computable.of(30);

            Computable<String> result = name.combine(age,
                (n, a) -> n + " is " + a + " years old");

            assertEquals("Alice is 30 years old", result.getValue());
        }
    }

    @Nested
    @DisplayName("peek")
    class PeekTests {

        @Test
        @DisplayName("executes side effect without modifying value")
        void peek_executesWithoutModifying() {
            AtomicReference<String> captured = new AtomicReference<>();

            Computable<String> result = Computable.of("hello")
                .peek(captured::set)
                .map(String::toUpperCase);

            assertEquals("hello", captured.get());
            assertEquals("HELLO", result.getValue());
        }

        @Test
        @DisplayName("returns same computable for chaining")
        void peek_returnsThis() {
            Computable<Integer> original = Computable.of(42);
            Computable<Integer> peeked = original.peek(n -> {});

            assertSame(original, peeked);
        }
    }

    @Nested
    @DisplayName("orElse")
    class OrElseTests {

        @Test
        @DisplayName("returns value when non-null")
        void orElse_returnsValue() {
            String result = Computable.of("hello").orElse("default");

            assertEquals("hello", result);
        }

        @Test
        @DisplayName("returns default when null")
        void orElse_returnsDefault() {
            String result = Computable.<String>of(null).orElse("default");

            assertEquals("default", result);
        }
    }

    @Nested
    @DisplayName("equality")
    class EqualityTests {

        @Test
        @DisplayName("equal values are equal")
        void equals_sameValue() {
            Computable<String> a = Computable.of("test");
            Computable<String> b = Computable.of("test");

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("different values are not equal")
        void equals_differentValue() {
            Computable<String> a = Computable.of("one");
            Computable<String> b = Computable.of("two");

            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("null values are equal")
        void equals_nullValues() {
            Computable<String> a = Computable.of(null);
            Computable<String> b = Computable.of(null);

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("toString shows wrapper")
        void toString_showsWrapper() {
            Computable<String> c = Computable.of("hello");

            assertEquals("Computable[hello]", c.toString());
        }

        @Test
        @DisplayName("equals itself")
        void equals_itself() {
            Computable<String> a = Computable.of("test");

            assertEquals(a, a);
        }

        @Test
        @DisplayName("not equal to non-Computable")
        void equals_notEqualToNonComputable() {
            Computable<String> a = Computable.of("test");

            assertNotEquals(a, "test");
            assertNotEquals(a, null);
        }

        @Test
        @DisplayName("null value not equal to non-null value")
        void equals_nullNotEqualToValue() {
            Computable<String> nullComp = Computable.of(null);
            Computable<String> valueComp = Computable.of("test");

            assertNotEquals(nullComp, valueComp);
            assertNotEquals(valueComp, nullComp);
        }
    }
}
