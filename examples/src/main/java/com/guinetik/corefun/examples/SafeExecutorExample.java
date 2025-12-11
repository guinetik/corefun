package com.guinetik.corefun.examples;

import com.guinetik.corefun.Loggable;
import com.guinetik.corefun.Result;
import com.guinetik.corefun.SafeExecutor;

/**
 * Demonstrates usage of SafeExecutor for timed, logged operations.
 *
 * SafeExecutor combines timing, logging, and exception handling into
 * a unified interface. It integrates with Loggable for framework-agnostic
 * logging, making it easy to plug in SLF4J, Log4j, or any other logger.
 */
public class SafeExecutorExample {

    public static void main(String[] args) {
        System.out.println("=== SafeExecutor Examples ===\n");

        basicUsage();
        implementingSafeExecutor();
        safelyResultPattern();
        customLogging();
    }

    /**
     * Basic usage with factory methods.
     */
    static void basicUsage() {
        System.out.println("--- Basic Usage (println logger) ---");

        // Create a SafeExecutor with println logging
        SafeExecutor executor = SafeExecutor.println();

        // Execute with timing and logging
        String result = executor.safely("Compute greeting", () -> {
            Thread.sleep(50); // Simulate work
            return "Hello, World!";
        });
        System.out.println("Result: " + result);

        // Void operations (using safelySafe for operations that throw)
        System.out.println("\nVoid operation:");
        executor.safelySafe("Perform cleanup", () -> {
            Thread.sleep(30);
            System.out.println("  [Cleanup work happening...]");
        });

        // Silent executor (no logging)
        System.out.println("\nSilent executor:");
        SafeExecutor silent = SafeExecutor.noop();
        String silentResult = silent.safely("No logs", () -> "Done quietly");
        System.out.println("Silent result: " + silentResult);

        System.out.println();
    }

    /**
     * Implementing SafeExecutor in your own class.
     */
    static void implementingSafeExecutor() {
        System.out.println("--- Implementing SafeExecutor ---");

        // Your service class can implement SafeExecutor
        DataProcessor processor = new DataProcessor();
        processor.processData("some-data-id");

        System.out.println();
    }

    /**
     * Using safelyResult for non-throwing operations.
     */
    static void safelyResultPattern() {
        System.out.println("--- safelyResult (No Exceptions) ---");

        SafeExecutor executor = SafeExecutor.println();

        // Returns Result instead of throwing
        Result<Integer, String> success = executor.safelyResult(
                "Parse valid number",
                () -> Integer.parseInt("42")
        );
        System.out.println("Parse result: " + success);

        Result<Integer, String> failure = executor.safelyResult(
                "Parse invalid number",
                () -> Integer.parseInt("not-a-number")
        );
        System.out.println("Parse failure: " + failure);

        // Chain with Result operations
        String message = success
                .map(n -> n * 2)
                .fold(
                        error -> "Error: " + error,
                        value -> "Doubled value: " + value
                );
        System.out.println(message);

        System.out.println();
    }

    /**
     * Custom logging integration examples.
     */
    static void customLogging() {
        System.out.println("--- Custom Logging ---");

        // Create a logger with custom formatting
        Loggable.Logger customLogger = new Loggable.Logger() {
            @Override
            public void info(String message) {
                System.out.println("📘 " + message);
            }

            @Override
            public void warn(String message) {
                System.out.println("⚠️  " + message);
            }

            @Override
            public void error(String message) {
                System.err.println("❌ " + message);
            }

            @Override
            public void error(String message, Throwable t) {
                System.err.println("❌ " + message + " - " + t.getMessage());
            }
        };

        SafeExecutor customExecutor = SafeExecutor.withLogger(customLogger);
        customExecutor.safely("Custom logged operation", () -> {
            Thread.sleep(25);
            return "done";
        });

        // Tagged logger (adds prefix)
        System.out.println("\nTagged logger:");
        Loggable.Logger tagged = Loggable.Logger.tagged("DataService",
                Loggable.Logger.println());
        SafeExecutor taggedExecutor = SafeExecutor.withLogger(tagged);
        taggedExecutor.safely("Tagged operation", () -> "result");

        // Example: SLF4J integration (pseudo-code)
        System.out.println("\n// SLF4J integration example:");
        System.out.println("// private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MyClass.class);");
        System.out.println("// Loggable.Logger logger = Loggable.Logger.of(LOG::info, LOG::warn, LOG::error);");

        System.out.println();
    }

    /**
     * Example service class implementing SafeExecutor.
     */
    static class DataProcessor implements SafeExecutor {
        // In a real app, this would be your SLF4J/Log4j logger
        private final Loggable.Logger log = Loggable.Logger.tagged("DataProcessor",
                Loggable.Logger.println());

        @Override
        public Loggable.Logger logger() {
            return log;
        }

        public void processData(String dataId) {
            // The safely() method is inherited from SafeExecutor
            Result<String, String> result = safelyResult(
                    "Process data " + dataId,
                    () -> {
                        // Simulate processing
                        Thread.sleep(75);
                        return "Processed: " + dataId;
                    }
            );

            result
                    .peekSuccess(data -> logger().info("Success: " + data))
                    .peekFailure(error -> logger().error("Failed: " + error));
        }
    }
}
