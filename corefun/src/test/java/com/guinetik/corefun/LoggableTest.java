package com.guinetik.corefun;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Loggable} and {@link Loggable.Logger}.
 */
@DisplayName("Loggable")
public class LoggableTest {

    @Nested
    @DisplayName("Logger.of (3 args)")
    class LoggerOfThreeArgsTests {

        @Test
        @DisplayName("delegates info to consumer")
        void of_delegatesInfo() {
            AtomicReference<String> captured = new AtomicReference<>();
            Loggable.Logger logger = Loggable.Logger.of(
                captured::set,
                s -> {},
                s -> {}
            );

            logger.info("hello");

            assertEquals("hello", captured.get());
        }

        @Test
        @DisplayName("delegates warn to consumer")
        void of_delegatesWarn() {
            AtomicReference<String> captured = new AtomicReference<>();
            Loggable.Logger logger = Loggable.Logger.of(
                s -> {},
                captured::set,
                s -> {}
            );

            logger.warn("warning message");

            assertEquals("warning message", captured.get());
        }

        @Test
        @DisplayName("delegates error to consumer")
        void of_delegatesError() {
            AtomicReference<String> captured = new AtomicReference<>();
            Loggable.Logger logger = Loggable.Logger.of(
                s -> {},
                s -> {},
                captured::set
            );

            logger.error("error message");

            assertEquals("error message", captured.get());
        }

        @Test
        @DisplayName("error with throwable appends message")
        void of_errorWithThrowable() {
            AtomicReference<String> captured = new AtomicReference<>();
            Loggable.Logger logger = Loggable.Logger.of(
                s -> {},
                s -> {},
                captured::set
            );

            logger.error("Operation failed", new RuntimeException("cause"));

            assertEquals("Operation failed: cause", captured.get());
        }

        @Test
        @DisplayName("debug is no-op in 3-arg factory")
        void of_debugIsNoOp() {
            Loggable.Logger logger = Loggable.Logger.of(
                s -> fail("info should not be called"),
                s -> fail("warn should not be called"),
                s -> fail("error should not be called")
            );

            // Should not throw or call any consumer
            assertDoesNotThrow(() -> logger.debug("debug message"));
        }
    }

    @Nested
    @DisplayName("Logger.of (4 args)")
    class LoggerOfFourArgsTests {

        @Test
        @DisplayName("uses custom error with throwable handler")
        void of_usesCustomErrorWithThrowable() {
            AtomicReference<String> capturedMsg = new AtomicReference<>();
            AtomicReference<Throwable> capturedThrowable = new AtomicReference<>();

            Loggable.Logger logger = Loggable.Logger.of(
                s -> {},
                s -> {},
                s -> {},
                (msg, t) -> {
                    capturedMsg.set(msg);
                    capturedThrowable.set(t);
                }
            );

            RuntimeException ex = new RuntimeException("boom");
            logger.error("Failed", ex);

            assertEquals("Failed", capturedMsg.get());
            assertSame(ex, capturedThrowable.get());
        }
    }

    @Nested
    @DisplayName("Logger.println")
    class LoggerPrintlnTests {

        @Test
        @DisplayName("creates working logger")
        void println_works() {
            Loggable.Logger logger = Loggable.Logger.println();

            // Should not throw
            assertDoesNotThrow(() -> {
                logger.info("info");
                logger.warn("warn");
                logger.error("error");
                logger.debug("debug");
            });
        }

        @Test
        @DisplayName("error with throwable prints stack trace")
        void println_errorWithThrowable() {
            Loggable.Logger logger = Loggable.Logger.println();

            // Should not throw - exercises error(String, Throwable) path
            assertDoesNotThrow(() ->
                logger.error("Failed operation", new RuntimeException("test error"))
            );
        }
    }

    @Nested
    @DisplayName("Logger.noop")
    class LoggerNoopTests {

        @Test
        @DisplayName("creates silent logger")
        void noop_createsSilent() {
            Loggable.Logger logger = Loggable.Logger.noop();

            // Should not throw
            assertDoesNotThrow(() -> {
                logger.info("info");
                logger.warn("warn");
                logger.error("error");
                logger.error("error", new RuntimeException());
            });
        }
    }

    @Nested
    @DisplayName("Logger.tagged")
    class LoggerTaggedTests {

        @Test
        @DisplayName("prefixes messages with tag")
        void tagged_prefixesMessages() {
            List<String> messages = new ArrayList<>();
            Loggable.Logger delegate = Loggable.Logger.of(
                messages::add,
                messages::add,
                messages::add
            );

            Loggable.Logger tagged = Loggable.Logger.tagged("MyService", delegate);

            tagged.info("starting");
            tagged.warn("slow response");
            tagged.error("failed");

            assertEquals("[MyService] starting", messages.get(0));
            assertEquals("[MyService] slow response", messages.get(1));
            assertEquals("[MyService] failed", messages.get(2));
        }

        @Test
        @DisplayName("prefixes error with throwable")
        void tagged_prefixesErrorWithThrowable() {
            AtomicReference<String> capturedMsg = new AtomicReference<>();
            AtomicReference<Throwable> capturedThrowable = new AtomicReference<>();

            Loggable.Logger delegate = Loggable.Logger.of(
                s -> {},
                s -> {},
                s -> {},
                (msg, t) -> {
                    capturedMsg.set(msg);
                    capturedThrowable.set(t);
                }
            );

            Loggable.Logger tagged = Loggable.Logger.tagged("MyService", delegate);
            RuntimeException ex = new RuntimeException("boom");
            tagged.error("operation failed", ex);

            assertEquals("[MyService] operation failed", capturedMsg.get());
            assertSame(ex, capturedThrowable.get());
        }

        @Test
        @DisplayName("prefixes debug messages")
        void tagged_prefixesDebug() {
            List<String> debugMessages = new ArrayList<>();

            // Create a logger that captures debug
            Loggable.Logger delegate = new Loggable.Logger() {
                @Override public void info(String message) {}
                @Override public void warn(String message) {}
                @Override public void error(String message) {}
                @Override public void error(String message, Throwable t) {}
                @Override public void debug(String message) {
                    debugMessages.add(message);
                }
            };

            Loggable.Logger tagged = Loggable.Logger.tagged("Debug", delegate);
            tagged.debug("test message");

            assertEquals(1, debugMessages.size());
            assertEquals("[Debug] test message", debugMessages.get(0));
        }
    }

    @Nested
    @DisplayName("Logger default methods")
    class LoggerDefaultMethodsTests {

        @Test
        @DisplayName("debug is no-op by default")
        void debug_isNoOp() {
            // Create a logger with no debug override
            Loggable.Logger logger = Loggable.Logger.of(
                s -> {},
                s -> {},
                s -> {}
            );

            // Should not throw
            assertDoesNotThrow(() -> logger.debug("message"));
        }

        @Test
        @DisplayName("trace is no-op by default")
        void trace_isNoOp() {
            Loggable.Logger logger = Loggable.Logger.of(
                s -> {},
                s -> {},
                s -> {}
            );

            assertDoesNotThrow(() -> logger.trace("message"));
        }
    }

    @Nested
    @DisplayName("Loggable factory methods")
    class LoggableFactoryTests {

        @Test
        @DisplayName("of creates loggable from logger")
        void of_createsFromLogger() {
            AtomicReference<String> captured = new AtomicReference<>();
            Loggable.Logger logger = Loggable.Logger.of(
                captured::set, s -> {}, s -> {}
            );

            Loggable loggable = Loggable.of(logger);
            loggable.logger().info("test");

            assertEquals("test", captured.get());
        }

        @Test
        @DisplayName("println creates println loggable")
        void println_works() {
            Loggable loggable = Loggable.println();

            assertDoesNotThrow(() -> loggable.logger().info("test"));
        }

        @Test
        @DisplayName("noop creates silent loggable")
        void noop_works() {
            Loggable loggable = Loggable.noop();

            assertDoesNotThrow(() -> loggable.logger().info("test"));
        }
    }

    @Nested
    @DisplayName("implementing Loggable")
    class ImplementingLoggableTests {

        @Test
        @DisplayName("class can implement Loggable")
        void classImplementsLoggable() {
            class MyService implements Loggable {
                private final List<String> messages = new ArrayList<>();

                @Override
                public Logger logger() {
                    return Logger.of(
                        messages::add,
                        messages::add,
                        messages::add
                    );
                }

                void doWork() {
                    logger().info("Starting work");
                    logger().info("Work completed");
                }
            }

            MyService service = new MyService();
            service.doWork();

            assertEquals(2, service.messages.size());
            assertEquals("Starting work", service.messages.get(0));
            assertEquals("Work completed", service.messages.get(1));
        }
    }
}
