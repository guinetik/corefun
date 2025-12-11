package com.guinetik.corefun;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Timing}.
 */
@DisplayName("Timing")
class TimingTest {

    @Nested
    @DisplayName("timed (Callable)")
    class TimedCallableTests {

        @Test
        @DisplayName("returns result from callable")
        void timed_returnsResult() {
            Timing timing = (desc, ms) -> {};

            String result = timing.timed("test", () -> "hello");

            assertEquals("hello", result);
        }

        @Test
        @DisplayName("calls onTimed with description and duration")
        void timed_callsOnTimed() {
            AtomicReference<String> capturedDesc = new AtomicReference<>();
            AtomicLong capturedMs = new AtomicLong(-1);

            Timing timing = (desc, ms) -> {
                capturedDesc.set(desc);
                capturedMs.set(ms);
            };

            timing.timed("Load data", () -> {
                Thread.sleep(10);
                return "data";
            });

            assertEquals("Load data", capturedDesc.get());
            assertTrue(capturedMs.get() >= 0, "Duration should be non-negative");
        }

        @Test
        @DisplayName("wraps exception with description")
        void timed_wrapsException() {
            Timing timing = (desc, ms) -> {};

            SafeException ex = assertThrows(SafeException.class, () ->
                timing.timed("Fetch user", () -> {
                    throw new IOException("network error");
                })
            );

            assertEquals("Fetch user", ex.getMessage());
            assertInstanceOf(IOException.class, ex.getCause());
        }

        @Test
        @DisplayName("calls onTimed even on exception")
        void timed_callsOnTimedOnException() {
            AtomicReference<String> capturedDesc = new AtomicReference<>();
            Timing timing = (desc, ms) -> capturedDesc.set(desc);

            try {
                timing.timed("Failing op", () -> {
                    throw new RuntimeException("fail");
                });
            } catch (Exception ignored) {}

            assertEquals("Failing op", capturedDesc.get());
        }
    }

    @Nested
    @DisplayName("timedVoid (Runnable)")
    class TimedVoidTests {

        @Test
        @DisplayName("executes runnable")
        void timedVoid_executes() {
            AtomicReference<String> executed = new AtomicReference<>();
            Timing timing = (desc, ms) -> {};

            timing.timedVoid("Do work", () -> executed.set("done"));

            assertEquals("done", executed.get());
        }

        @Test
        @DisplayName("reports timing")
        void timedVoid_reportsTiming() {
            AtomicReference<String> capturedDesc = new AtomicReference<>();
            Timing timing = (desc, ms) -> capturedDesc.set(desc);

            timing.timedVoid("Process", () -> {});

            assertEquals("Process", capturedDesc.get());
        }
    }

    @Nested
    @DisplayName("timedSafe (SafeRunnable)")
    class TimedSafeTests {

        @Test
        @DisplayName("executes safe runnable with checked exception")
        void timedSafe_executesWithCheckedException() {
            AtomicReference<String> executed = new AtomicReference<>();
            Timing timing = (desc, ms) -> {};

            timing.timedSafe("IO operation", () -> {
                // This can throw checked exception
                executed.set("done");
            });

            assertEquals("done", executed.get());
        }

        @Test
        @DisplayName("wraps checked exception")
        void timedSafe_wrapsCheckedException() {
            Timing timing = (desc, ms) -> {};

            SafeException ex = assertThrows(SafeException.class, () ->
                timing.timedSafe("Read file", () -> {
                    throw new IOException("file not found");
                })
            );

            assertInstanceOf(IOException.class, ex.getCause());
        }
    }

    @Nested
    @DisplayName("factory methods")
    class FactoryTests {

        @Test
        @DisplayName("println creates working timing")
        void println_works() {
            Timing timing = Timing.println();

            // Should not throw
            String result = timing.timed("Test", () -> "value");
            assertEquals("value", result);
        }

        @Test
        @DisplayName("noop creates silent timing")
        void noop_works() {
            Timing timing = Timing.noop();

            // Should not throw and returns result
            String result = timing.timed("Test", () -> "value");
            assertEquals("value", result);
        }
    }
}
