package com.guinetik.corefun;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SafeException}.
 */
@DisplayName("SafeException")
public class SafeExceptionTest {

    @Nested
    @DisplayName("constructors")
    class ConstructorTests {

        @Test
        @DisplayName("wraps cause only")
        void constructor_causeOnly() {
            IOException cause = new IOException("disk full");
            SafeException ex = new SafeException(cause);

            assertSame(cause, ex.getCause());
            // RuntimeException(Throwable) sets message to cause.toString()
            assertEquals("java.io.IOException: disk full", ex.getMessage());
        }

        @Test
        @DisplayName("wraps message and cause")
        void constructor_messageAndCause() {
            IOException cause = new IOException("disk full");
            SafeException ex = new SafeException("Failed to write file", cause);

            assertEquals("Failed to write file", ex.getMessage());
            assertSame(cause, ex.getCause());
        }

        @Test
        @DisplayName("wraps message only")
        void constructor_messageOnly() {
            SafeException ex = new SafeException("Something went wrong");

            assertEquals("Something went wrong", ex.getMessage());
            assertNull(ex.getCause());
        }
    }

    @Nested
    @DisplayName("wrap (single arg)")
    class WrapTests {

        @Test
        @DisplayName("wraps checked exception")
        void wrap_wrapsCheckedException() {
            IOException checked = new IOException("file not found");

            RuntimeException result = SafeException.wrap(checked);

            assertInstanceOf(SafeException.class, result);
            assertSame(checked, result.getCause());
        }

        @Test
        @DisplayName("returns RuntimeException as-is")
        void wrap_returnsRuntimeAsIs() {
            IllegalArgumentException runtime = new IllegalArgumentException("bad arg");

            RuntimeException result = SafeException.wrap(runtime);

            assertSame(runtime, result);
        }

        @Test
        @DisplayName("returns SafeException as-is")
        void wrap_returnsSafeExceptionAsIs() {
            SafeException original = new SafeException("error");

            RuntimeException result = SafeException.wrap(original);

            assertSame(original, result);
        }
    }

    @Nested
    @DisplayName("wrap (with message)")
    class WrapWithMessageTests {

        @Test
        @DisplayName("wraps exception with context message")
        void wrapMessage_addsContext() {
            IOException cause = new IOException("connection reset");

            SafeException result = SafeException.wrap("Failed to fetch data", cause);

            assertEquals("Failed to fetch data", result.getMessage());
            assertSame(cause, result.getCause());
        }
    }

    @Nested
    @DisplayName("serialization")
    class SerializationTests {

        @Test
        @DisplayName("has serialVersionUID")
        void hasSerialVersionUID() {
            // SafeException extends RuntimeException which is Serializable
            SafeException ex = new SafeException("test");
            assertNotNull(ex);
        }
    }
}
