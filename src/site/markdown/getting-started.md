# Getting Started

This guide will help you get started with CoreFun in your Java project.

## Requirements

- Java 8 or higher
- Maven or Gradle build tool

## Installation

### Maven

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.guinetik</groupId>
    <artifactId>corefun</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.guinetik:corefun:0.1.0'
```

## Basic Usage

### Result - Functional Error Handling

`Result<S, F>` represents either a success or failure outcome:

```java
import com.guinetik.corefun.Result;

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

### Try - Exception to Result Conversion

```java
import com.guinetik.corefun.Try;

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

### Computable - Value Wrapper

```java
import com.guinetik.corefun.Computable;

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

### SafeExecutor - Logging and Timing

```java
import com.guinetik.corefun.SafeExecutor;
import com.guinetik.corefun.Loggable;

public class DataProcessor implements SafeExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(DataProcessor.class);

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

On Java 17+, CoreFun provides sealed types via Multi-Release JAR:

```java
// Java 21+ pattern matching
String message = switch (result) {
    case Result.Success<User, String>(var user) -> "Found: " + user.getName();
    case Result.Failure<User, String>(var error) -> "Error: " + error;
};
```

## Next Steps

- Browse the [API Documentation](apidocs/index.html)
- Check out the [examples module](https://github.com/guinetik/corefun/tree/masters/examples) on GitHub
