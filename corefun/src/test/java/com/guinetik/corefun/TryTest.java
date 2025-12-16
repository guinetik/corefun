package com.guinetik.corefun;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Try}.
 */
@DisplayName("Try")
public class TryTest {

    @Nested
    @DisplayName("of (with string error)")
    class OfStringTests {

        @Test
        @DisplayName("returns success for successful operation")
        void of_returnsSuccess() {
            Result<Integer, String> result = Try.of(() -> 42);

            assertTrue(result.isSuccess());
            assertEquals(42, result.get());
        }

        @Test
        @DisplayName("returns failure with message for exception")
        void of_returnsFailureWithMessage() {
            Result<Integer, String> result = Try.of(() -> {
                throw new IllegalArgumentException("bad input");
            });

            assertTrue(result.isFailure());
            assertEquals("bad input", result.getError());
        }

        @Test
        @DisplayName("returns exception class name when message is null")
        void of_returnsClassNameForNullMessage() {
            Result<Integer, String> result = Try.of(() -> {
                throw new NullPointerException();
            });

            assertTrue(result.isFailure());
            assertEquals("java.lang.NullPointerException", result.getError());
        }

        @Test
        @DisplayName("handles checked exceptions")
        void of_handlesCheckedExceptions() {
            Result<String, String> result = Try.of(() -> {
                throw new IOException("file not found");
            });

            assertTrue(result.isFailure());
            assertEquals("file not found", result.getError());
        }
    }

    @Nested
    @DisplayName("ofException")
    class OfExceptionTests {

        @Test
        @DisplayName("returns success preserving value")
        void ofException_returnsSuccess() {
            Result<String, Exception> result = Try.ofException(() -> "hello");

            assertTrue(result.isSuccess());
            assertEquals("hello", result.get());
        }

        @Test
        @DisplayName("returns failure preserving exception")
        void ofException_preservesException() {
            IOException original = new IOException("disk full");
            Result<String, Exception> result = Try.ofException(() -> {
                throw original;
            });

            assertTrue(result.isFailure());
            assertSame(original, result.getError());
        }
    }

    @Nested
    @DisplayName("of (with custom error mapper)")
    class OfCustomMapperTests {

        @Test
        @DisplayName("returns success unchanged")
        void ofMapper_returnsSuccess() {
            Result<Integer, Integer> result = Try.of(
                () -> 100,
                e -> -1
            );

            assertTrue(result.isSuccess());
            assertEquals(100, result.get());
        }

        @Test
        @DisplayName("applies mapper to exception")
        void ofMapper_appliesMapper() {
            Result<Integer, Integer> result = Try.of(
                () -> { throw new RuntimeException("error"); },
                e -> e.getMessage().length()
            );

            assertTrue(result.isFailure());
            assertEquals(5, result.getError()); // "error".length()
        }
    }

    @Nested
    @DisplayName("run")
    class RunTests {

        @Test
        @DisplayName("returns success for successful runnable")
        void run_returnsSuccess() {
            Result<Void, String> result = Try.run(() -> {
                // do nothing
            });

            assertTrue(result.isSuccess());
            assertNull(result.get());
        }

        @Test
        @DisplayName("returns failure for throwing runnable")
        void run_returnsFailure() {
            Result<Void, String> result = Try.run(() -> {
                throw new IllegalStateException("bad state");
            });

            assertTrue(result.isFailure());
            assertEquals("bad state", result.getError());
        }
    }

    @Nested
    @DisplayName("lazy")
    class LazyTests {

        @Test
        @DisplayName("defers execution until get is called")
        void lazy_defersExecution() {
            int[] counter = {0};
            Supplier<Result<Integer, String>> lazy = Try.lazy(() -> {
                counter[0]++;
                return counter[0];
            });

            assertEquals(0, counter[0]); // Not executed yet
            Result<Integer, String> result = lazy.get();
            assertEquals(1, counter[0]); // Now executed
            assertEquals(1, result.get());
        }

        @Test
        @DisplayName("executes each time get is called")
        void lazy_executesEachTime() {
            int[] counter = {0};
            Supplier<Result<Integer, String>> lazy = Try.lazy(() -> ++counter[0]);

            lazy.get();
            lazy.get();
            lazy.get();
            assertEquals(3, counter[0]);
        }
    }

    @Nested
    @DisplayName("getOrDefault")
    class GetOrDefaultTests {

        @Test
        @DisplayName("returns value on success")
        void getOrDefault_returnsValue() {
            String result = Try.getOrDefault(() -> "hello", "default");

            assertEquals("hello", result);
        }

        @Test
        @DisplayName("returns default on exception")
        void getOrDefault_returnsDefault() {
            String result = Try.getOrDefault(() -> {
                throw new RuntimeException();
            }, "default");

            assertEquals("default", result);
        }
    }

    @Nested
    @DisplayName("getOrNull")
    class GetOrNullTests {

        @Test
        @DisplayName("returns value on success")
        void getOrNull_returnsValue() {
            String result = Try.getOrNull(() -> "value");

            assertEquals("value", result);
        }

        @Test
        @DisplayName("returns null on exception")
        void getOrNull_returnsNull() {
            String result = Try.getOrNull(() -> {
                throw new RuntimeException();
            });

            assertNull(result);
        }
    }
}
