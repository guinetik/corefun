package com.guinetik.corefun;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SafeRunnable}.
 */
@DisplayName("SafeRunnable")
class SafeRunnableTest {

    @Nested
    @DisplayName("run")
    class RunTests {

        @Test
        @DisplayName("executes successfully")
        void run_executes() throws Exception {
            AtomicBoolean executed = new AtomicBoolean(false);
            SafeRunnable task = () -> executed.set(true);

            task.run();

            assertTrue(executed.get());
        }

        @Test
        @DisplayName("can throw checked exception")
        void run_canThrowChecked() {
            SafeRunnable task = () -> {
                throw new IOException("disk error");
            };

            IOException ex = assertThrows(IOException.class, task::run);
            assertEquals("disk error", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("toRunnable")
    class ToRunnableTests {

        @Test
        @DisplayName("converts to standard Runnable")
        void toRunnable_converts() {
            AtomicBoolean executed = new AtomicBoolean(false);
            SafeRunnable safe = () -> executed.set(true);

            Runnable runnable = safe.toRunnable();
            runnable.run();

            assertTrue(executed.get());
        }

        @Test
        @DisplayName("wraps checked exception in SafeException")
        void toRunnable_wrapsCheckedException() {
            SafeRunnable safe = () -> {
                throw new IOException("file not found");
            };

            Runnable runnable = safe.toRunnable();

            SafeException ex = assertThrows(SafeException.class, runnable::run);
            assertInstanceOf(IOException.class, ex.getCause());
        }

        @Test
        @DisplayName("passes through RuntimeException unwrapped")
        void toRunnable_passesThroughRuntime() {
            IllegalStateException original = new IllegalStateException("bad state");
            SafeRunnable safe = () -> { throw original; };

            Runnable runnable = safe.toRunnable();

            IllegalStateException ex = assertThrows(IllegalStateException.class, runnable::run);
            assertSame(original, ex);
        }
    }

    @Nested
    @DisplayName("from")
    class FromTests {

        @Test
        @DisplayName("wraps standard Runnable")
        void from_wrapsRunnable() throws Exception {
            AtomicBoolean executed = new AtomicBoolean(false);
            Runnable standard = () -> executed.set(true);

            SafeRunnable safe = SafeRunnable.from(standard);
            safe.run();

            assertTrue(executed.get());
        }
    }

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        @Test
        @DisplayName("runs immediately")
        void execute_runsImmediately() {
            AtomicBoolean executed = new AtomicBoolean(false);
            SafeRunnable safe = () -> executed.set(true);

            safe.execute();

            assertTrue(executed.get());
        }

        @Test
        @DisplayName("throws SafeException on failure")
        void execute_throwsSafeException() {
            SafeRunnable safe = () -> {
                throw new IOException("network error");
            };

            SafeException ex = assertThrows(SafeException.class, safe::execute);
            assertInstanceOf(IOException.class, ex.getCause());
        }
    }
}
