package com.guinetik.corefun;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SafeExecutor}.
 */
@DisplayName("SafeExecutor")
public class SafeExecutorTest {

    /** Helper to create a SafeExecutor that captures log messages */
    public SafeExecutor captureExecutor(
        List<String> infos,
        List<String> errors
    ) {
        return () ->
            Loggable.Logger.of(
                infos::add,
                s -> {},
                errors::add,
                (msg, t) ->
                    errors.add(msg + ": " + t.getClass().getSimpleName())
            );
    }

    @Nested
    @DisplayName("safely (Callable)")
    class SafelyCallableTests {

        @Test
        @DisplayName("returns result on success")
        void safely_returnsResult() {
            SafeExecutor executor = SafeExecutor.noop();

            String result = executor.safely("Test", () -> "hello");

            assertEquals("hello", result);
        }

        @Test
        @DisplayName("logs start and complete messages")
        void safely_logsStartAndComplete() {
            List<String> infos = new ArrayList<>();
            SafeExecutor executor = captureExecutor(infos, new ArrayList<>());

            executor.safely("Process data", () -> "done");

            assertEquals(2, infos.size());
            assertTrue(infos.get(0).contains("Executing: Process data"));
            assertTrue(infos.get(1).contains("Completed: Process data"));
            assertTrue(infos.get(1).contains("ms"));
        }

        @Test
        @DisplayName("throws SafeException on failure")
        void safely_throwsSafeException() {
            SafeExecutor executor = SafeExecutor.noop();

            SafeException ex = assertThrows(SafeException.class, () ->
                executor.safely("Fail", () -> {
                    throw new IOException("network error");
                })
            );

            assertEquals("Fail", ex.getMessage());
            assertInstanceOf(IOException.class, ex.getCause());
        }

        @Test
        @DisplayName("logs error on failure")
        void safely_logsError() {
            List<String> errors = new ArrayList<>();
            SafeExecutor executor = captureExecutor(new ArrayList<>(), errors);

            try {
                executor.safely("Crash", () -> {
                    throw new RuntimeException("boom");
                });
            } catch (Exception ignored) {}

            assertEquals(1, errors.size());
            assertTrue(errors.get(0).contains("Failed: Crash"));
        }
    }

    @Nested
    @DisplayName("safelyVoid (Runnable)")
    class SafelyVoidTests {

        @Test
        @DisplayName("executes runnable")
        void safelyVoid_executes() {
            AtomicReference<String> executed = new AtomicReference<>();
            SafeExecutor executor = SafeExecutor.noop();

            executor.safelyVoid("Do work", () -> executed.set("done"));

            assertEquals("done", executed.get());
        }

        @Test
        @DisplayName("logs execution")
        void safelyVoid_logs() {
            List<String> infos = new ArrayList<>();
            SafeExecutor executor = captureExecutor(infos, new ArrayList<>());

            executor.safelyVoid("Cleanup", () -> {});

            assertEquals(2, infos.size());
            assertTrue(infos.get(0).contains("Cleanup"));
        }
    }

    @Nested
    @DisplayName("safelySafe (SafeRunnable)")
    class SafelySafeTests {

        @Test
        @DisplayName("executes safe runnable")
        void safelySafe_executes() {
            AtomicReference<String> executed = new AtomicReference<>();
            SafeExecutor executor = SafeExecutor.noop();

            executor.safelySafe("IO work", () -> executed.set("done"));

            assertEquals("done", executed.get());
        }

        @Test
        @DisplayName("wraps checked exception")
        void safelySafe_wrapsCheckedException() {
            SafeExecutor executor = SafeExecutor.noop();

            SafeException ex = assertThrows(SafeException.class, () ->
                executor.safelySafe("Read file", () -> {
                    throw new IOException("file not found");
                })
            );

            assertInstanceOf(IOException.class, ex.getCause());
        }
    }

    @Nested
    @DisplayName("safelyResult")
    class SafelyResultTests {

        @Test
        @DisplayName("returns success on success")
        void safelyResult_returnsSuccess() {
            SafeExecutor executor = SafeExecutor.noop();

            Result<Integer, String> result = executor.safelyResult(
                "Parse",
                () -> 42
            );

            assertTrue(result.isSuccess());
            assertEquals(42, result.get());
        }

        @Test
        @DisplayName("returns failure with message on exception")
        void safelyResult_returnsFailure() {
            SafeExecutor executor = SafeExecutor.noop();

            Result<Integer, String> result = executor.safelyResult(
                "Parse",
                () -> {
                    throw new NumberFormatException("bad number");
                }
            );

            assertTrue(result.isFailure());
            assertTrue(result.getError().contains("Parse failed"));
            assertTrue(result.getError().contains("bad number"));
        }

        @Test
        @DisplayName("uses class name when message is null")
        void safelyResult_usesClassName() {
            SafeExecutor executor = SafeExecutor.noop();

            Result<Integer, String> result = executor.safelyResult(
                "Parse",
                () -> {
                    throw new NullPointerException();
                }
            );

            assertTrue(result.isFailure());
            assertTrue(result.getError().contains("NullPointerException"));
        }

        @Test
        @DisplayName("logs start and complete on success")
        void safelyResult_logsOnSuccess() {
            List<String> infos = new ArrayList<>();
            SafeExecutor executor = captureExecutor(infos, new ArrayList<>());

            executor.safelyResult("Compute", () -> 1);

            assertEquals(2, infos.size());
        }

        @Test
        @DisplayName("logs error on failure")
        void safelyResult_logsOnFailure() {
            List<String> errors = new ArrayList<>();
            SafeExecutor executor = captureExecutor(new ArrayList<>(), errors);

            executor.safelyResult("Compute", () -> {
                throw new RuntimeException("fail");
            });

            assertEquals(1, errors.size());
        }
    }

    @Nested
    @DisplayName("factory methods")
    class FactoryTests {

        @Test
        @DisplayName("withLogger creates executor with custom logger")
        void withLogger_works() {
            List<String> infos = new ArrayList<>();
            Loggable.Logger logger = Loggable.Logger.of(
                infos::add,
                s -> {},
                s -> {}
            );

            SafeExecutor executor = SafeExecutor.withLogger(logger);
            executor.safely("Test", () -> "result");

            assertFalse(infos.isEmpty());
        }

        @Test
        @DisplayName("println creates working executor")
        void println_works() {
            SafeExecutor executor = SafeExecutor.println();

            String result = executor.safely("Test", () -> "hello");

            assertEquals("hello", result);
        }

        @Test
        @DisplayName("noop creates silent executor")
        void noop_works() {
            SafeExecutor executor = SafeExecutor.noop();

            String result = executor.safely("Test", () -> "hello");

            assertEquals("hello", result);
        }
    }

    @Nested
    @DisplayName("callback hooks")
    class CallbackHooksTests {

        @Test
        @DisplayName("onStart can be overridden")
        void onStart_canBeOverridden() {
            AtomicReference<String> startDesc = new AtomicReference<>();

            SafeExecutor executor = new SafeExecutor() {
                @Override
                public Loggable.Logger logger() {
                    return Loggable.Logger.noop();
                }

                @Override
                public void onStart(String description) {
                    startDesc.set("CUSTOM: " + description);
                }
            };

            executor.safely("My op", () -> "done");

            assertEquals("CUSTOM: My op", startDesc.get());
        }

        @Test
        @DisplayName("onComplete can be overridden")
        void onComplete_canBeOverridden() {
            AtomicReference<Long> capturedMs = new AtomicReference<>();

            SafeExecutor executor = new SafeExecutor() {
                @Override
                public Loggable.Logger logger() {
                    return Loggable.Logger.noop();
                }

                @Override
                public void onComplete(String description, long milliseconds) {
                    capturedMs.set(milliseconds);
                }
            };

            executor.safely("Slow op", () -> {
                Thread.sleep(10);
                return "done";
            });

            assertNotNull(capturedMs.get());
            assertTrue(capturedMs.get() >= 0);
        }

        @Test
        @DisplayName("onError can be overridden")
        void onError_canBeOverridden() {
            AtomicReference<Exception> capturedEx = new AtomicReference<>();

            SafeExecutor executor = new SafeExecutor() {
                @Override
                public Loggable.Logger logger() {
                    return Loggable.Logger.noop();
                }

                @Override
                public void onError(String description, Exception e) {
                    capturedEx.set(e);
                }
            };

            try {
                executor.safely("Failing", () -> {
                    throw new IllegalStateException("bad state");
                });
            } catch (Exception ignored) {}

            assertNotNull(capturedEx.get());
            assertInstanceOf(IllegalStateException.class, capturedEx.get());
        }
    }

    @Nested
    @DisplayName("implementing SafeExecutor")
    class ImplementingSafeExecutorTests {

        @Test
        @DisplayName("service class can implement SafeExecutor")
        void serviceImplementsSafeExecutor() {
            class DataService implements SafeExecutor {

                private final List<String> logs = new ArrayList<>();

                @Override
                public Loggable.Logger logger() {
                    return Loggable.Logger.of(logs::add, logs::add, logs::add);
                }

                String fetchData() {
                    return safely("Fetch data", () -> "data");
                }
            }

            DataService service = new DataService();
            String result = service.fetchData();

            assertEquals("data", result);
            assertEquals(2, service.logs.size());
        }
    }
}
