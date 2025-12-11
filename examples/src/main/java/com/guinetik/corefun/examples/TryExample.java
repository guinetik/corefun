package com.guinetik.corefun.examples;

import com.guinetik.corefun.Result;
import com.guinetik.corefun.Try;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

/**
 * Demonstrates usage of Try for converting exceptions to Results.
 *
 * Try is a utility class that bridges exception-throwing code with
 * the functional Result type. It captures exceptions and converts
 * them to Failure results, enabling seamless integration of legacy
 * exception-based APIs with functional error handling.
 */
public class TryExample {

    public static void main(String[] args) {
        System.out.println("=== Try Utility Examples ===\n");

        basicTry();
        tryWithExceptionDetails();
        tryWithCustomErrorMapping();
        tryRunnables();
        lazyEvaluation();
        getOrDefaultPatterns();
    }

    /**
     * Basic Try.of() - converts exceptions to string error messages.
     */
    static void basicTry() {
        System.out.println("--- Basic Try.of() ---");

        // Successful operation
        Result<Integer, String> success = Try.of(() -> Integer.parseInt("42"));
        System.out.println("Parse '42': " + success);

        // Failed operation - exception becomes error message
        Result<Integer, String> failure = Try.of(() -> Integer.parseInt("not-a-number"));
        System.out.println("Parse 'not-a-number': " + failure);

        // Chaining with Result operations
        Result<String, String> doubled = Try.of(() -> Integer.parseInt("21"))
                .map(n -> n * 2)
                .map(n -> "Result: " + n);
        System.out.println("Parsed and doubled: " + doubled);

        System.out.println();
    }

    /**
     * Try.ofException() - preserves the full exception.
     */
    static void tryWithExceptionDetails() {
        System.out.println("--- Try.ofException() ---");

        // When you need the actual exception (for logging, stack traces, etc.)
        Result<Integer, Exception> result = Try.ofException(() -> Integer.parseInt("bad"));

        result.peekFailure(ex -> {
            System.out.println("Exception type: " + ex.getClass().getSimpleName());
            System.out.println("Exception message: " + ex.getMessage());
        });

        // You can map the exception to your own error type
        Result<Integer, String> mapped = result.mapFailure(ex ->
                ex.getClass().getSimpleName() + ": " + ex.getMessage()
        );
        System.out.println("Mapped error: " + mapped);

        System.out.println();
    }

    /**
     * Try.of() with custom error mapping.
     */
    static void tryWithCustomErrorMapping() {
        System.out.println("--- Custom Error Mapping ---");

        // Map exceptions to your own error type
        Result<Path, FileError> fileResult = Try.of(
                () -> Paths.get("/nonexistent/file.txt").toRealPath(),
                ex -> new FileError(ex.getMessage(), "FILE_NOT_FOUND")
        );
        System.out.println("File operation: " + fileResult);

        fileResult.peekFailure(error ->
                System.out.println("  Error code: " + error.code)
        );

        // Useful for creating domain-specific errors
        Result<Integer, ApiError> apiResult = Try.of(
                () -> callExternalApi(),
                ex -> new ApiError(500, "Service unavailable: " + ex.getMessage())
        );
        System.out.println("API call: " + apiResult);

        System.out.println();
    }

    /**
     * Try.run() - for void operations.
     */
    static void tryRunnables() {
        System.out.println("--- Try.run() for Void Operations ---");

        // Successful void operation
        Result<Void, String> success = Try.run(() -> {
            System.out.println("  Executing some work...");
            // operation succeeds
        });
        System.out.println("Run success: " + success);

        // Failed void operation
        Result<Void, String> failure = Try.run(() -> {
            throw new RuntimeException("Simulated failure");
        });
        System.out.println("Run failure: " + failure);

        // Check success/failure
        if (success.isSuccess()) {
            System.out.println("  Operation completed successfully");
        }

        System.out.println();
    }

    /**
     * Try.lazy() - deferred execution.
     */
    static void lazyEvaluation() {
        System.out.println("--- Lazy Evaluation ---");

        // Create a lazy computation (not executed yet)
        Supplier<Result<String, String>> lazyRead = Try.lazy(() -> {
            System.out.println("  [Executing lazy operation now]");
            return readConfigValue("app.name");
        });

        System.out.println("Lazy supplier created (not executed yet)");
        System.out.println("Calling get()...");

        // Now it executes
        Result<String, String> result = lazyRead.get();
        System.out.println("Result: " + result);

        // Useful for conditional execution
        boolean shouldRun = true;
        if (shouldRun) {
            Supplier<Result<Integer, String>> expensive = Try.lazy(() -> {
                Thread.sleep(10); // Simulate expensive operation
                return 42;
            });
            System.out.println("Expensive computation: " + expensive.get());
        }

        System.out.println();
    }

    /**
     * getOrDefault and getOrNull patterns.
     */
    static void getOrDefaultPatterns() {
        System.out.println("--- getOrDefault / getOrNull ---");

        // When you just need a value with a fallback (no Result wrapping)
        int parsed = Try.getOrDefault(() -> Integer.parseInt("bad"), 0);
        System.out.println("getOrDefault with bad input: " + parsed);

        int goodParsed = Try.getOrDefault(() -> Integer.parseInt("42"), 0);
        System.out.println("getOrDefault with good input: " + goodParsed);

        // getOrNull for nullable scenarios
        String nullResult = Try.getOrNull(() -> {
            throw new RuntimeException("Error");
        });
        System.out.println("getOrNull on error: " + nullResult);

        String validResult = Try.getOrNull(() -> "Hello");
        System.out.println("getOrNull on success: " + validResult);

        // Practical example: configuration with fallback
        String dbHost = Try.getOrDefault(
                () -> System.getenv("DB_HOST"),
                "localhost"
        );
        System.out.println("DB_HOST (with fallback): " + (dbHost != null ? dbHost : "localhost"));

        System.out.println();
    }

    // --- Helper methods and classes ---

    static String readConfigValue(String key) throws IOException {
        // Simulated config reading
        if ("app.name".equals(key)) {
            return "MyApp";
        }
        throw new IOException("Config key not found: " + key);
    }

    static Integer callExternalApi() throws Exception {
        throw new Exception("Connection refused");
    }

    static class FileError {
        final String message;
        final String code;

        FileError(String message, String code) {
            this.message = message;
            this.code = code;
        }

        @Override
        public String toString() {
            return "FileError{" + code + ": " + message + "}";
        }
    }

    static class ApiError {
        final int statusCode;
        final String message;

        ApiError(int statusCode, String message) {
            this.statusCode = statusCode;
            this.message = message;
        }

        @Override
        public String toString() {
            return "ApiError{" + statusCode + ": " + message + "}";
        }
    }
}
