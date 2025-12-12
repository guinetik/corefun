package com.guinetik.corefun;

/**
 * A functional interface for operations that may throw checked exceptions.
 * <p>
 * {@code SafeRunnable} allows writing lambda expressions for operations that throw
 * checked exceptions, which can then be converted to standard {@link Runnable}
 * instances with automatic exception wrapping. This eliminates the boilerplate
 * of try-catch blocks in lambda expressions.
 * </p>
 *
 * <h2>Problem Solved</h2>
 * <p>
 * Java's {@link Runnable} interface declares {@code void run()} without any
 * checked exceptions. This makes it impossible to use lambdas that throw checked
 * exceptions directly with executors or other APIs expecting Runnable.
 * </p>
 *
 * <h2>Example Usage</h2>
 * <pre class="language-java"><code>
 * // Define an operation that throws checked exceptions
 * SafeRunnable task = () -&gt; {
 *     Files.delete(path);  // throws IOException
 * };
 *
 * // Convert to standard Runnable for use with executors
 * executor.execute(task.toRunnable());
 *
 * // Or execute immediately with automatic wrapping
 * task.execute();  // throws SafeException on failure
 *
 * // Create from existing Runnable
 * SafeRunnable fromRunnable = SafeRunnable.from(() -&gt; doSomething());
 * </code></pre>
 *
 * @author Guinetik &lt;guinetik@gmail.com&gt;
 * @since 0.1.0
 * @see SafeCallable
 * @see SafeException
 * @see SafeExecutor#safelySafe(String, SafeRunnable)
 */
@FunctionalInterface
public interface SafeRunnable {

    /**
     * Executes the operation, potentially throwing a checked exception.
     *
     * @throws Exception if the operation fails
     */
    void run() throws Exception;

    /**
     * Converts this SafeRunnable to a standard Runnable.
     * Any checked exceptions are wrapped in {@link SafeException}.
     *
     * @return a Runnable that wraps checked exceptions
     */
    default Runnable toRunnable() {
        return () -> {
            try {
                run();
            } catch (Exception e) {
                throw SafeException.wrap(e);
            }
        };
    }

    /**
     * Creates a SafeRunnable from a standard Runnable.
     *
     * @param runnable the Runnable to wrap
     * @return a SafeRunnable that delegates to the Runnable
     */
    static SafeRunnable from(Runnable runnable) {
        return runnable::run;
    }

    /**
     * Executes this SafeRunnable, wrapping any exception.
     * Convenience method for immediate execution.
     *
     * @throws SafeException if execution fails
     */
    default void execute() {
        toRunnable().run();
    }
}
