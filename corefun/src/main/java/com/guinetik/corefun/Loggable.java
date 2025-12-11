package com.guinetik.corefun;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Interface providing logging capabilities to implementing classes.
 * <p>
 * {@code Loggable} is framework-agnostic - implement {@link #logger()} to return
 * a {@link Logger} that delegates to your preferred logging framework (SLF4J, Log4j,
 * java.util.logging, etc.). This allows library code to emit logs without depending
 * on any specific logging implementation.
 * </p>
 *
 * <h2>Design Philosophy</h2>
 * <p>
 * Rather than depending on a logging facade like SLF4J, CoreFun uses a simple
 * functional approach: you provide the logging functions. This means:
 * </p>
 * <ul>
 *   <li>Zero dependencies on logging frameworks</li>
 *   <li>Easy integration with any logging system</li>
 *   <li>Testable - inject mock loggers for testing</li>
 *   <li>Flexible - use lambdas, method references, or custom implementations</li>
 * </ul>
 *
 * <h2>Example with SLF4J</h2>
 * <pre>{@code
 * public class MyService implements Loggable {
 *     private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MyService.class);
 *
 *     @Override
 *     public Logger logger() {
 *         return Logger.of(LOG::info, LOG::warn, LOG::error);
 *     }
 *
 *     public void doWork() {
 *         logger().info("Starting work");
 *         // ...
 *         logger().info("Work completed");
 *     }
 * }
 * }</pre>
 *
 * <h2>Example with java.util.logging</h2>
 * <pre>{@code
 * public class JulService implements Loggable {
 *     private static final java.util.logging.Logger LOG =
 *         java.util.logging.Logger.getLogger(JulService.class.getName());
 *
 *     @Override
 *     public Logger logger() {
 *         return Logger.of(
 *             msg -> LOG.info(msg),
 *             msg -> LOG.warning(msg),
 *             msg -> LOG.severe(msg)
 *         );
 *     }
 * }
 * }</pre>
 *
 * <h2>Example with Simple println</h2>
 * <pre>{@code
 * public class SimpleService implements Loggable {
 *     @Override
 *     public Logger logger() {
 *         return Logger.println();
 *     }
 * }
 * }</pre>
 *
 * <h2>Tagged Logging</h2>
 * <pre>{@code
 * Logger tagged = Logger.tagged("MyComponent", Logger.println());
 * tagged.info("Starting");  // prints: [MyComponent] Starting
 * }</pre>
 *
 * @author Guinetik &lt;guinetik@gmail.com&gt;
 * @since 0.1.0
 * @see SafeExecutor
 */
public interface Loggable {

    /**
     * Returns the logger for this instance.
     * Override to provide a logger backed by your preferred framework.
     *
     * @return a Logger instance
     */
    Logger logger();

    /**
     * A simple, framework-agnostic logger interface.
     * <p>
     * Logger provides a minimal set of logging methods that can be implemented
     * by delegating to any logging framework. Factory methods are provided for
     * common use cases.
     * </p>
     *
     * @author Guinetik &lt;guinetik@gmail.com&gt;
     * @since 0.1.0
     */
    interface Logger {

        /**
         * Logs an informational message.
         *
         * @param message the message to log
         */
        void info(String message);

        /**
         * Logs a warning message.
         *
         * @param message the message to log
         */
        void warn(String message);

        /**
         * Logs an error message.
         *
         * @param message the message to log
         */
        void error(String message);

        /**
         * Logs an error message with an exception.
         *
         * @param message the message to log
         * @param throwable the exception to log
         */
        void error(String message, Throwable throwable);

        /**
         * Logs a debug message.
         *
         * @param message the message to log
         */
        default void debug(String message) {
            // No-op by default, override if needed
        }

        /**
         * Logs a trace message.
         *
         * @param message the message to log
         */
        default void trace(String message) {
            // No-op by default, override if needed
        }

        /**
         * Creates a Logger from individual log functions.
         *
         * @param info function for info messages
         * @param warn function for warn messages
         * @param error function for error messages
         * @return a configured Logger
         */
        static Logger of(Consumer<String> info,
                         Consumer<String> warn,
                         Consumer<String> error) {
            return new Logger() {
                @Override public void info(String message) { info.accept(message); }
                @Override public void warn(String message) { warn.accept(message); }
                @Override public void error(String message) { error.accept(message); }
                @Override public void error(String message, Throwable t) {
                    error.accept(message + ": " + t.getMessage());
                }
            };
        }

        /**
         * Creates a Logger with error-with-throwable support.
         *
         * @param info function for info messages
         * @param warn function for warn messages
         * @param error function for error messages
         * @param errorWithThrowable function for error messages with throwables
         * @return a configured Logger
         */
        static Logger of(Consumer<String> info,
                         Consumer<String> warn,
                         Consumer<String> error,
                         BiConsumer<String, Throwable> errorWithThrowable) {
            return new Logger() {
                @Override public void info(String message) { info.accept(message); }
                @Override public void warn(String message) { warn.accept(message); }
                @Override public void error(String message) { error.accept(message); }
                @Override public void error(String message, Throwable t) {
                    errorWithThrowable.accept(message, t);
                }
            };
        }

        /**
         * Creates a Logger that prints to stdout/stderr.
         *
         * @return a println-based Logger
         */
        static Logger println() {
            return new Logger() {
                @Override public void info(String message) {
                    System.out.println("[INFO] " + message);
                }
                @Override public void warn(String message) {
                    System.out.println("[WARN] " + message);
                }
                @Override public void error(String message) {
                    System.err.println("[ERROR] " + message);
                }
                @Override public void error(String message, Throwable t) {
                    System.err.println("[ERROR] " + message);
                    t.printStackTrace(System.err);
                }
                @Override public void debug(String message) {
                    System.out.println("[DEBUG] " + message);
                }
            };
        }

        /**
         * Creates a no-op Logger that discards all messages.
         *
         * @return a silent Logger
         */
        static Logger noop() {
            return new Logger() {
                @Override public void info(String message) {}
                @Override public void warn(String message) {}
                @Override public void error(String message) {}
                @Override public void error(String message, Throwable t) {}
            };
        }

        /**
         * Creates a Logger that prefixes all messages with a tag.
         *
         * @param tag the prefix tag (typically class name)
         * @param delegate the Logger to delegate to
         * @return a tagged Logger
         */
        static Logger tagged(String tag, Logger delegate) {
            return new Logger() {
                @Override public void info(String message) {
                    delegate.info("[" + tag + "] " + message);
                }
                @Override public void warn(String message) {
                    delegate.warn("[" + tag + "] " + message);
                }
                @Override public void error(String message) {
                    delegate.error("[" + tag + "] " + message);
                }
                @Override public void error(String message, Throwable t) {
                    delegate.error("[" + tag + "] " + message, t);
                }
                @Override public void debug(String message) {
                    delegate.debug("[" + tag + "] " + message);
                }
            };
        }
    }

    /**
     * Creates a Loggable that uses the given Logger.
     *
     * @param logger the Logger to use
     * @return a Loggable instance
     */
    static Loggable of(Logger logger) {
        return () -> logger;
    }

    /**
     * Creates a Loggable that prints to stdout/stderr.
     *
     * @return a println-based Loggable
     */
    static Loggable println() {
        return Logger::println;
    }

    /**
     * Creates a no-op Loggable.
     *
     * @return a silent Loggable
     */
    static Loggable noop() {
        return Logger::noop;
    }
}
