package com.guinetik.corefun.examples;

import com.guinetik.corefun.Result;

/**
 * Demonstrates usage of the Result monad for functional error handling.
 *
 * Result<S, F> represents either a Success containing a value of type S,
 * or a Failure containing an error of type F. This eliminates null-checking
 * and exception handling in favor of explicit, composable error handling.
 */
public class ResultExample {

    public static void main(String[] args) {
        System.out.println("=== Result<S, F> Examples ===\n");

        basicUsage();
        chainingOperations();
        foldPatternMatching();
        validationExample();
        recoveryExample();
    }

    /**
     * Basic creation and inspection of Results.
     */
    static void basicUsage() {
        System.out.println("--- Basic Usage ---");

        // Creating a success
        Result<Integer, String> success = Result.success(42);
        System.out.println("Success: " + success);
        System.out.println("  isSuccess: " + success.isSuccess());
        System.out.println("  get(): " + success.get());

        // Creating a failure
        Result<Integer, String> failure = Result.failure("Something went wrong");
        System.out.println("\nFailure: " + failure);
        System.out.println("  isFailure: " + failure.isFailure());
        System.out.println("  getError(): " + failure.getError());

        // Safe value extraction with defaults
        System.out.println("\nSafe extraction:");
        System.out.println("  success.getOrElse(-1): " + success.getOrElse(-1));
        System.out.println("  failure.getOrElse(-1): " + failure.getOrElse(-1));

        System.out.println();
    }

    /**
     * Chaining operations with map and flatMap.
     */
    static void chainingOperations() {
        System.out.println("--- Chaining Operations ---");

        // Simulating a user lookup
        Result<User, String> userResult = findUser(1);

        // map: transform the success value
        Result<String, String> nameResult = userResult.map(user -> user.name);
        System.out.println("User name: " + nameResult);

        // Chain multiple maps
        Result<String, String> greeting = userResult
                .map(user -> user.name)
                .map(name -> "Hello, " + name + "!");
        System.out.println("Greeting: " + greeting);

        // flatMap: chain operations that return Results
        Result<Account, String> accountResult = userResult
                .flatMap(user -> findAccount(user.accountId));
        System.out.println("Account lookup: " + accountResult);

        // Failure propagates through the chain
        Result<User, String> notFound = findUser(999);
        Result<String, String> failedGreeting = notFound
                .map(user -> user.name)
                .map(name -> "Hello, " + name + "!");
        System.out.println("Failed lookup chain: " + failedGreeting);

        System.out.println();
    }

    /**
     * Using fold for pattern matching.
     */
    static void foldPatternMatching() {
        System.out.println("--- Fold (Pattern Matching) ---");

        Result<User, String> success = findUser(1);
        Result<User, String> failure = findUser(999);

        // fold applies one of two functions based on success/failure
        String successMessage = success.fold(
                error -> "Error: " + error,
                user -> "Found user: " + user.name
        );
        System.out.println("Success fold: " + successMessage);

        String failureMessage = failure.fold(
                error -> "Error: " + error,
                user -> "Found user: " + user.name
        );
        System.out.println("Failure fold: " + failureMessage);

        // Practical example: HTTP response
        int statusCode = success.fold(
                error -> 404,
                user -> 200
        );
        System.out.println("HTTP status: " + statusCode);

        System.out.println();
    }

    /**
     * Validating Results with predicates.
     */
    static void validationExample() {
        System.out.println("--- Validation ---");

        Result<Integer, String> age = Result.success(15);

        // validate: check a condition, fail if not met
        Result<Integer, String> validated = age.validate(
                a -> a >= 18,
                a -> "Age " + a + " is below minimum (18)"
        );
        System.out.println("Age validation (15): " + validated);

        Result<Integer, String> adultAge = Result.success(25);
        Result<Integer, String> validatedAdult = adultAge.validate(
                a -> a >= 18,
                a -> "Age " + a + " is below minimum (18)"
        );
        System.out.println("Age validation (25): " + validatedAdult);

        // Chain validations
        Result<String, String> email = Result.success("user@example.com");
        Result<String, String> validatedEmail = email
                .validate(e -> e.contains("@"), e -> "Missing @ symbol")
                .validate(e -> e.length() > 5, e -> "Email too short");
        System.out.println("Email validation: " + validatedEmail);

        System.out.println();
    }

    /**
     * Recovering from failures.
     */
    static void recoveryExample() {
        System.out.println("--- Recovery ---");

        Result<User, String> failure = findUser(999);
        System.out.println("Original: " + failure);

        // recover: handle failure and potentially return success
        Result<User, String> recovered = failure.recover(error -> {
            System.out.println("  Recovering from: " + error);
            return Result.success(new User(0, "Guest", 0));
        });
        System.out.println("Recovered: " + recovered);

        // Recovery can also fail
        Result<User, String> stillFailed = failure.recover(error ->
                Result.failure("Recovery also failed: " + error)
        );
        System.out.println("Failed recovery: " + stillFailed);

        // Peek for side effects without altering the result
        failure
                .peekSuccess(user -> System.out.println("  Found: " + user.name))
                .peekFailure(error -> System.out.println("  Logging error: " + error));

        System.out.println();
    }

    // --- Domain classes for examples ---

    static class User {
        final int id;
        final String name;
        final int accountId;

        User(int id, String name, int accountId) {
            this.id = id;
            this.name = name;
            this.accountId = accountId;
        }

        @Override
        public String toString() {
            return "User{id=" + id + ", name='" + name + "'}";
        }
    }

    static class Account {
        final int id;
        final double balance;

        Account(int id, double balance) {
            this.id = id;
            this.balance = balance;
        }

        @Override
        public String toString() {
            return "Account{id=" + id + ", balance=" + balance + "}";
        }
    }

    // --- Simulated service methods ---

    static Result<User, String> findUser(int id) {
        if (id == 1) {
            return Result.success(new User(1, "Alice", 100));
        } else if (id == 2) {
            return Result.success(new User(2, "Bob", 200));
        }
        return Result.failure("User not found: " + id);
    }

    static Result<Account, String> findAccount(int accountId) {
        if (accountId == 100) {
            return Result.success(new Account(100, 1500.00));
        }
        return Result.failure("Account not found: " + accountId);
    }
}
