# Fixture Monkey Code Style Guide

This guide outlines the code consistency and style conventions for contributing to the Fixture Monkey project.
Please follow these rules to reduce code review time and maintain project quality.

## 1. General Principles

*   **Language Version**:
    *   Java: JDK 8 or higher compatible
    *   Kotlin: 1.8 or higher
*   **Encoding**: UTF-8
*   **Line Ending**: LF (Line Feed)
*   **Indentation**: Use **Tab** (Size: 4)
    *   Please configure your IDE to match the `.editorconfig` file included in the project root.

## 2. Naming Conventions

*   **Package**: Lowercase, format `com.navercorp.fixturemonkey...`
*   **Class/Interface**: PascalCase (e.g., `MonkeyContext`, `CombinableArbitrary`)
*   **Method/Variable**: camelCase (e.g., `giveMeOne`, `objectBuilder`)
*   **Constant**: UPPER_SNAKE_CASE (e.g., `DEFAULT_MAX_TRIES`)

## 3. Java Coding Rules

### 3.1. Library Dependencies
*   **No Lombok in Production Code**: Do not use Lombok in production code (`src/main/java`). Explicitly write Getters, constructors, etc., or use IDE generation features.
*   **Lombok Allowed in Test Code**: Lombok usage is permitted in test code (`src/test/java`).

### 3.2. API Documentation & Annotations
*   **@API Annotation**: Public APIs must use the `org.apiguardian.api.API` annotation to specify status and introduction version (since).
    ```java
    @API(since = "0.6.0", status = Status.MAINTAINED)
    public interface CombinableArbitrary<T> { ... }
    ```
*   **Javadoc**: Public classes and methods must have Javadoc explaining their role, parameters, and return values.

### 3.3. Immutability
*   Design objects to be Immutable whenever possible.
*   Actively use the `final` keyword for fields.

## 4. Kotlin Coding Rules

*   **Trailing Commas**: It is recommended to use **Trailing Commas** for multi-line parameters or list declarations.
    ```kotlin
    MatchArbitraryIntrospector(
        listOf(
            PrimaryConstructorArbitraryIntrospector.INSTANCE,
            it, // Trailing comma
        )
    )
    ```
*   **Standard Library Usage**: Utilize Kotlin standard library functions (`apply`, `let`, `map`, etc.) appropriately for concise code.

## 5. Test Code Rules

*   **Frameworks**:
    *   **jqwik**: Mainly used for Property-based testing. Use the `@Property` annotation.
    *   **JUnit 5**: Used for general unit testing.
    *   **AssertJ**: Used for writing assertions.
*   **Test Naming**: Write test names that clearly indicate the target and intent.

## 6. License Header

All source files (`*.java`, `*.kt`) must include the Apache License 2.0 header at the very top.

```java
/*
 * Fixture Monkey
 *
 * Copyright (c) 2021-present NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * ...
 */
```

---

## 7. Object Generation Architecture

Understanding the internal object generation flow is crucial for contributing to core logic.

### 7.1. Core Components
*   **FixtureMonkey**: The main entry point. It holds the `MonkeyContext` and creates `ArbitraryBuilder`.
*   **ArbitraryBuilder**: Configures how to generate an object. It accumulates user customizations (manipulators).
*   **ArbitraryResolver**: Resolves the `ArbitraryBuilder` into a `CombinableArbitrary`. It constructs the `ObjectTree`.
*   **ObjectTree**: Represents the structure of the object to be generated. It consists of `ObjectNode`s.
*   **ArbitraryIntrospector**: Determines how to create an instance of a specific type (e.g., Constructor, Bean, Factory method).

### 7.2. Generation Flow
1.  **Initialization**: `FixtureMonkey.create()` initializes options and context.
2.  **Builder Creation**: `fixtureMonkey.giveMeBuilder(Type)` creates a `DefaultArbitraryBuilder` with a `RootProperty`.
3.  **Customization**: Methods like `.set()`, `.size()` add `ArbitraryManipulator`s to the `ArbitraryBuilderContext`.
4.  **Resolution**: Calling `.sample()` triggers `ArbitraryResolver.resolve()`.
    *   It creates an `ObjectTree` representing the object structure.
    *   It applies all registered `ArbitraryManipulator`s to the `ObjectTree`.
    *   It traverses the tree to generate the actual object using `ArbitraryIntrospector`s.
5.  **Generation**: The `ObjectTree` produces the final Java/Kotlin object instance.

### 7.3. Key Concepts
*   **Introspection**: The process of inspecting a class to understand how to instantiate it.
*   **Manipulation**: The process of modifying the generated object or its structure based on user input (path expressions).
*   **Lazy Evaluation**: Objects are not generated until `.sample()` is called, allowing for efficient configuration.
