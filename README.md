# CoreFun

Lightweight functional primitives for Java 8+. Zero dependencies.

## Why CoreFun?

- **Zero dependencies** - No transitive dependency hell
- **Java 8+ compatible** - Works on legacy enterprise systems
- **Modern Java enhanced** - Sealed types on Java 17+ via Multi-Release JAR
- **Tiny footprint** - Just the essentials, nothing more
- **Enterprise-focused** - Built for real-world use cases

## Installation

```xml
<dependency>
    <groupId>com.guinetik</groupId>
    <artifactId>corefun</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Core Components

### Result<S, F>

An Either-style monad for functional error handling without exceptions.

```java
// Create results
Result<User, String> success = Result.success(user);
Result<User, String> failure = Result.failure("User not found");

// Pattern matching with fold
String message = result.fold(
    error -> "Error: " + error,
    user -> "Found: " + user.getName()
);

// Chaining operations
Result<String, String> greeting = findUser(id)
    .map(User::getName)
    .map(name -> "Hello, " + name);

// Validation
Result<Integer, String> validated = Result.success(age)
    .validate(a -> a >= 18, a -> "Must be 18 or older");

// Recovery
Result<User, String> user = findUser(id)
    .recover(error -> Result.success(guestUser));
```

### Try

Utility for converting exceptions to Results.

```java
// Execute and get Result
Result<String, String> content = Try.of(() -> Files.readString(path));

// With custom error mapping
Result<Config, AppError> config = Try.of(
    () -> loadConfig(),
    e -> new AppError("Config load failed", e)
);

// Get with default
String content = Try.getOrDefault(() -> Files.readString(path), "");
```

### Computable<T>

A functional value wrapper with composition operations.

```java
Computable<String> name = Computable.of("hello");
Computable<Integer> length = name.map(String::length);
Computable<String> upper = name.map(String::toUpperCase);

// Combine values
Computable<Integer> a = Computable.of(10);
Computable<Integer> b = Computable.of(5);
Computable<Integer> sum = a.combine(b, Integer::sum);

// Validation
boolean valid = Computable.of(25).isValid(age -> age >= 18);
```

### SafeRunnable & SafeCallable

Functional interfaces for operations that throw checked exceptions.

```java
// SafeRunnable - like Runnable but throws
SafeRunnable task = () -> Files.delete(path);
executor.execute(task.toRunnable());  // Exceptions wrapped automatically

// SafeCallable - like Callable with convenience methods
SafeCallable<String> reader = () -> Files.readString(path);
String content = reader.getOrElse("default");
Result<String, Exception> result = reader.toResult();
```

### Timing

Interface for timing operations with pluggable logging.

```java
public class MyService implements Timing {
    @Override
    public void onTimed(String description, long milliseconds) {
        logger.info("{} took {}ms", description, milliseconds);
    }

    public Data process() {
        return timed("Process data", () -> doProcessing());
    }
}

// Or use built-in implementations
Timing.println().timed("Operation", () -> doSomething());
```

### Loggable

Framework-agnostic logging interface. Plug in any logging library.

```java
// With SLF4J
public class MyService implements Loggable {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MyService.class);

    @Override
    public Logger logger() {
        return Logger.of(LOG::info, LOG::warn, LOG::error);
    }

    public void doWork() {
        logger().info("Starting work");
        // ...
    }
}

// Simple println logger
Loggable simple = Loggable.println();
simple.logger().info("Hello");  // prints: [INFO] Hello

// Tagged logger
Logger tagged = Logger.tagged("MyClass", Logger.println());
tagged.info("Message");  // prints: [INFO] [MyClass] Message
```

### SafeExecutor

Combined timing, logging, and error handling. Extends `Loggable` for automatic logging.

```java
public class DataProcessor implements SafeExecutor {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DataProcessor.class);

    @Override
    public Loggable.Logger logger() {
        return Loggable.Logger.of(LOG::info, LOG::warn, LOG::error);
    }

    public Result<Data, String> process() {
        // Automatically logs: "Executing: Process data"
        // On success: "Completed: Process data in 123ms"
        // On error: "Failed: Process data" with stack trace
        return safelyResult("Process data", () -> doProcessing());
    }
}

// Quick usage with println
SafeExecutor.println().safely("Quick task", () -> doSomething());
```

## Java 17+ Features

On Java 17+, `Result` is a sealed interface with record implementations, enabling:

- Exhaustive pattern matching (compiler checks all cases)
- More concise syntax with records
- Better performance with value semantics

```java
// Java 21+ pattern matching
String message = switch (result) {
    case Result.Success<User, String>(var user) -> "Found: " + user.getName();
    case Result.Failure<User, String>(var error) -> "Error: " + error;
};
```

## License

Apache License 2.0
