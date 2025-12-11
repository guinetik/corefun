package com.guinetik.corefun.examples;

import com.guinetik.corefun.Computable;

/**
 * Demonstrates usage of Computable for functional value transformations.
 *
 * Computable is a lightweight wrapper that provides functional composition
 * operations on values. Think of it as a simple container that enables
 * fluent transformations, validations, and combinations of data.
 */
public class ComputableExample {

    public static void main(String[] args) {
        System.out.println("=== Computable<T> Examples ===\n");

        basicTransformations();
        validationAndFiltering();
        combiningValues();
        dataProcessingPipeline();
    }

    /**
     * Basic map and flatMap operations.
     */
    static void basicTransformations() {
        System.out.println("--- Basic Transformations ---");

        // Create a Computable from a value
        Computable<String> name = Computable.of("hello");
        System.out.println("Original: " + name.getValue());

        // map: transform the value
        Computable<Integer> length = name.map(String::length);
        System.out.println("Length: " + length.getValue());

        // Chain multiple transformations
        Computable<String> result = Computable.of("  hello world  ")
                .map(String::trim)
                .map(String::toUpperCase)
                .map(s -> s.replace(" ", "_"));
        System.out.println("Transformed: " + result.getValue());

        // flatMap: for computations that return Computable
        Computable<String> greeting = Computable.of("World")
                .flatMap(n -> Computable.of("Hello, " + n + "!"));
        System.out.println("Greeting: " + greeting.getValue());

        System.out.println();
    }

    /**
     * Validation and filtering.
     */
    static void validationAndFiltering() {
        System.out.println("--- Validation and Filtering ---");

        Computable<Integer> age = Computable.of(25);

        // isValid: check a predicate
        boolean isAdult = age.isValid(a -> a >= 18);
        System.out.println("Age " + age.getValue() + " is adult: " + isAdult);

        boolean isTeenager = Computable.of(15).isValid(a -> a >= 13 && a < 20);
        System.out.println("Age 15 is teenager: " + isTeenager);

        // filter: replace with default if predicate fails
        Computable<Integer> minAge = Computable.of(15)
                .filter(a -> a >= 18, () -> 18);
        System.out.println("Minimum age (from 15): " + minAge.getValue());

        Computable<Integer> keepAge = Computable.of(25)
                .filter(a -> a >= 18, () -> 18);
        System.out.println("Minimum age (from 25): " + keepAge.getValue());

        // Practical: ensure non-null with default
        Computable<String> nullableValue = Computable.of((String) null);
        String safeValue = nullableValue.orElse("default");
        System.out.println("Nullable with orElse: " + safeValue);

        System.out.println();
    }

    /**
     * Combining multiple Computables.
     */
    static void combiningValues() {
        System.out.println("--- Combining Values ---");

        // combine: merge two Computables with a function
        Computable<Integer> a = Computable.of(10);
        Computable<Integer> b = Computable.of(5);

        Computable<Integer> sum = a.combine(b, Integer::sum);
        System.out.println("10 + 5 = " + sum.getValue());

        Computable<Integer> product = a.combine(b, (x, y) -> x * y);
        System.out.println("10 * 5 = " + product.getValue());

        // Combining different types
        Computable<String> firstName = Computable.of("John");
        Computable<String> lastName = Computable.of("Doe");
        Computable<String> fullName = firstName.combine(lastName, (f, l) -> f + " " + l);
        System.out.println("Full name: " + fullName.getValue());

        // reduce: fold with an initial value
        Computable<String> word = Computable.of("world");
        String reduced = word.reduce("Hello, ", (acc, w) -> acc + w + "!");
        System.out.println("Reduced: " + reduced);

        System.out.println();
    }

    /**
     * A practical data processing pipeline.
     */
    static void dataProcessingPipeline() {
        System.out.println("--- Data Processing Pipeline ---");

        // Simulate processing user input
        String rawInput = "   john.doe@example.com   ";

        Computable<String> processedEmail = Computable.of(rawInput)
                .peek(v -> System.out.println("Raw input: '" + v + "'"))
                .map(String::trim)
                .peek(v -> System.out.println("After trim: '" + v + "'"))
                .map(String::toLowerCase)
                .peek(v -> System.out.println("After lowercase: '" + v + "'"))
                .filter(e -> e.contains("@"), () -> "invalid@email.com")
                .peek(v -> System.out.println("After validation: '" + v + "'"));

        System.out.println("Final result: " + processedEmail.getValue());

        // Processing a user record
        System.out.println("\nProcessing user record:");
        User rawUser = new User("  Bob  ", 25, "  admin  ");

        Computable<User> cleanUser = Computable.of(rawUser)
                .map(u -> new User(u.name.trim(), u.age, u.role.trim().toLowerCase()));

        System.out.println("Cleaned user: " + cleanUser.getValue());

        // Validation pipeline
        boolean isValidAdmin = cleanUser
                .isValid(u -> u.name.length() > 0)
                && cleanUser.isValid(u -> u.age >= 18)
                && cleanUser.isValid(u -> "admin".equals(u.role));

        System.out.println("Is valid admin: " + isValidAdmin);

        System.out.println();
    }

    // Helper class
    static class User {
        final String name;
        final int age;
        final String role;

        User(String name, int age, String role) {
            this.name = name;
            this.age = age;
            this.role = role;
        }

        @Override
        public String toString() {
            return "User{name='" + name + "', age=" + age + ", role='" + role + "'}";
        }
    }
}
