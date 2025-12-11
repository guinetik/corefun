package com.guinetik.corefun;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SafeCallable}.
 */
@DisplayName("SafeCallable")
class SafeCallableTest {

    @Nested
    @DisplayName("call")
    class CallTests {

        @Test
        @DisplayName("returns value on success")
        void call_returnsValue() throws Exception {
            SafeCallable<String> callable = () -> "hello";

            assertEquals("hello", callable.call());
        }

        @Test
        @DisplayName("can throw checked exception")
        void call_canThrowChecked() {
            SafeCallable<String> callable = () -> {
                throw new IOException("read error");
            };

            assertThrows(IOException.class, callable::call);
        }
    }

    @Nested
    @DisplayName("getOrThrow")
    class GetOrThrowTests {

        @Test
        @DisplayName("returns value on success")
        void getOrThrow_returnsValue() {
            SafeCallable<Integer> callable = () -> 42;

            assertEquals(42, callable.getOrThrow());
        }

        @Test
        @DisplayName("wraps checked exception")
        void getOrThrow_wrapsCheckedException() {
            SafeCallable<String> callable = () -> {
                throw new IOException("disk full");
            };

            SafeException ex = assertThrows(SafeException.class, callable::getOrThrow);
            assertInstanceOf(IOException.class, ex.getCause());
        }

        @Test
        @DisplayName("passes through runtime exception")
        void getOrThrow_passesThroughRuntime() {
            IllegalArgumentException original = new IllegalArgumentException("bad");
            SafeCallable<String> callable = () -> { throw original; };

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, callable::getOrThrow);
            assertSame(original, ex);
        }
    }

    @Nested
    @DisplayName("getOrElse")
    class GetOrElseTests {

        @Test
        @DisplayName("returns value on success")
        void getOrElse_returnsValue() {
            SafeCallable<String> callable = () -> "result";

            assertEquals("result", callable.getOrElse("default"));
        }

        @Test
        @DisplayName("returns default on failure")
        void getOrElse_returnsDefault() {
            SafeCallable<String> callable = () -> {
                throw new RuntimeException();
            };

            assertEquals("default", callable.getOrElse("default"));
        }
    }

    @Nested
    @DisplayName("toResult")
    class ToResultTests {

        @Test
        @DisplayName("returns success with value")
        void toResult_returnsSuccess() {
            SafeCallable<String> callable = () -> "hello";

            Result<String, Exception> result = callable.toResult();

            assertTrue(result.isSuccess());
            assertEquals("hello", result.getSuccess());
        }

        @Test
        @DisplayName("returns failure with exception")
        void toResult_returnsFailure() {
            IOException cause = new IOException("network error");
            SafeCallable<String> callable = () -> { throw cause; };

            Result<String, Exception> result = callable.toResult();

            assertTrue(result.isFailure());
            assertSame(cause, result.getFailure());
        }
    }

    @Nested
    @DisplayName("toResultWithMessage")
    class ToResultWithMessageTests {

        @Test
        @DisplayName("returns success with value")
        void toResultWithMessage_returnsSuccess() {
            SafeCallable<Integer> callable = () -> 100;

            Result<Integer, String> result = callable.toResultWithMessage();

            assertTrue(result.isSuccess());
            assertEquals(100, result.getSuccess());
        }

        @Test
        @DisplayName("returns failure with message")
        void toResultWithMessage_returnsMessage() {
            SafeCallable<String> callable = () -> {
                throw new IllegalArgumentException("invalid input");
            };

            Result<String, String> result = callable.toResultWithMessage();

            assertTrue(result.isFailure());
            assertEquals("invalid input", result.getFailure());
        }

        @Test
        @DisplayName("uses class name when message is null")
        void toResultWithMessage_usesClassName() {
            SafeCallable<String> callable = () -> {
                throw new NullPointerException();
            };

            Result<String, String> result = callable.toResultWithMessage();

            assertTrue(result.isFailure());
            assertEquals("java.lang.NullPointerException", result.getFailure());
        }
    }

    @Nested
    @DisplayName("from")
    class FromTests {

        @Test
        @DisplayName("wraps standard Callable")
        void from_wrapsCallable() throws Exception {
            Callable<String> standard = () -> "wrapped";

            SafeCallable<String> safe = SafeCallable.from(standard);

            assertEquals("wrapped", safe.call());
        }
    }

    @Nested
    @DisplayName("of")
    class OfTests {

        @Test
        @DisplayName("creates callable returning value")
        void of_returnsValue() throws Exception {
            SafeCallable<String> callable = SafeCallable.of("constant");

            assertEquals("constant", callable.call());
            assertEquals("constant", callable.call()); // Repeatable
        }
    }
}
