package com.guinetik.corefun.examples;

/**
 * Runs all CoreFun examples.
 *
 * Run with: mvn exec:java -pl examples
 * Or: java -cp examples/target/classes:corefun/target/classes com.guinetik.corefun.examples.AllExamples
 */
public class AllExamples {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║              CoreFun - Functional Primitives                 ║");
        System.out.println("║                     Example Gallery                          ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();

        runExample("Result<S, F>", ResultExample::main);
        runExample("Try", TryExample::main);
        runExample("Computable<T>", ComputableExample::main);
        runExample("SafeExecutor", SafeExecutorExample::main);

        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                    All Examples Complete                     ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }

    private static void runExample(String name, java.util.function.Consumer<String[]> example) {
        System.out.println("┌──────────────────────────────────────────────────────────────┐");
        System.out.println("│ Running: " + padRight(name, 51) + "│");
        System.out.println("└──────────────────────────────────────────────────────────────┘");
        System.out.println();

        try {
            example.accept(new String[0]);
        } catch (Exception e) {
            System.err.println("Example failed: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println();
        System.out.println("════════════════════════════════════════════════════════════════");
        System.out.println();
    }

    private static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }
}
