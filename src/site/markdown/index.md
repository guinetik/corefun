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

## Quick Start

```java
// Functional error handling with Result
Result<User, String> result = findUser(id);

String message = result.fold(
    error -> "Error: " + error,
    user -> "Found: " + user.getName()
);

// Chain operations safely
Result<String, String> greeting = findUser(id)
    .map(User::getName)
    .map(name -> "Hello, " + name);
```

## Core Components

| Component | Description |
|-----------|-------------|
| [Result&lt;S,F&gt;](apidocs/com/guinetik/corefun/Result.html) | Either-style monad for success/failure handling |
| [Try](apidocs/com/guinetik/corefun/Try.html) | Convert exceptions to Results |
| [Computable&lt;T&gt;](apidocs/com/guinetik/corefun/Computable.html) | Functional value wrapper with composition |
| [SafeRunnable](apidocs/com/guinetik/corefun/SafeRunnable.html) | Runnable that throws checked exceptions |
| [SafeCallable&lt;T&gt;](apidocs/com/guinetik/corefun/SafeCallable.html) | Callable with convenience methods |
| [SafeExecutor](apidocs/com/guinetik/corefun/SafeExecutor.html) | Safe execution with timing and logging |
| [Loggable](apidocs/com/guinetik/corefun/Loggable.html) | Framework-agnostic logging interface |
| [Timing](apidocs/com/guinetik/corefun/Timing.html) | Operation timing with pluggable reporting |

## Java 17+ Features

On Java 17+, `Result` is a sealed interface with record implementations, enabling exhaustive pattern matching:

```java
// Java 21+ pattern matching
String message = switch (result) {
    case Result.Success<User, String>(var user) -> "Found: " + user.getName();
    case Result.Failure<User, String>(var error) -> "Error: " + error;
};
```

## License

Apache License 2.0
