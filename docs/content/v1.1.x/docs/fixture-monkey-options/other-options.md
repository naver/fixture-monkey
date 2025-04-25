---
title: "Other Options"
images: []
menu:
docs:
parent: "fixture-monkey-options"
identifier: "options"
weight: 53
---

This section explains additional options provided by `FixtureMonkeyBuilder` that are not covered in the beginner or expert guides. Options are organized by priority based on how frequently they're needed in testing.

> **Navigating Options Documentation**:
> * If you're new to Fixture Monkey, start with [Essential Options for Beginners](../essential-options-for-beginners)
> * If you're looking for advanced options, check [Advanced Options for Experts](../advanced-options-for-experts)
> * For conceptual understanding, refer to [Option Concepts](../concepts)

## 1. Special Value Handling Options

These options deal with special values that are frequently needed in testing.

### Null Value Handling

Control how and when null values are generated:

```java
// Configure default null injection behavior
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .defaultNullInjectGenerator(
        (context, random) -> random.nextBoolean(0.1) // 10% chance of null
    )
    .build();

// Add type-specific null injection behavior
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushAssignableTypeNullInjectGenerator(
        String.class,
        (context, random) -> random.nextBoolean(0.2) // 20% chance of null for strings
    )
    .build();
```

> **Key Term**: `NullInjectGenerator` determines when a property should be null based on context and randomness.

### Unique Value Generation

Ensure generated values are unique:

```java
// Set maximum tries for generating unique values
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .generateUniqueMaxTries(200)
    .build();
```

## 2. Validation and Constraints Options

Ensure generated objects satisfy certain conditions or constraints.

### Validation

Set up custom validation for generated objects:

```java
// Set a custom validator for generated objects
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .arbitraryValidator(new CustomArbitraryValidator())
    .build();
```

> **Key Term**: `ArbitraryValidator` allows you to verify if generated values meet certain conditions.

### Java Constraint Handling

Handle Java constraints in generated objects:

```java
// Configure a custom Java constraint generator
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .javaConstraintGenerator(new CustomJavaConstraintGenerator())
    .build();

// Customize an existing Java constraint generator
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushJavaConstraintGeneratorCustomizer(generator -> 
        generator.overrideConstraint(MyConstraint.class, 
            prop -> Arbitraries.strings().alpha())
    )
    .build();

// Set valid-only flag for specific property paths
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushCustomizeValidOnly(
        TreeMatcher.exactPath("root.child.property"), 
        true
    )
    .build();
```

> **Key Term**: `JavaConstraintGenerator` handles annotations like `@Min`, `@Max`, `@NotNull` and converts them to appropriate generators.

## 3. Object Analysis and Generation Options

These options control how Fixture Monkey understands object structure and creates instances.

### Introspection Options

Customize how Fixture Monkey analyzes objects:

```java
// Set a custom introspector for a specific class
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushAssignableTypeArbitraryIntrospector(
        MyClass.class, 
        new CustomArbitraryIntrospector()
    )
    .build();

// Set a default object introspector for all objects
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(new CustomObjectIntrospector())
    .build();
```

> **Key Term**: `ArbitraryIntrospector` is responsible for analyzing objects to determine how to generate instances.

### Property Discovery and Access Options

Control how properties are found and accessed in objects:

```java
// Set default property generator
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .defaultPropertyGenerator(new CustomPropertyGenerator())
    .build();

// Push a custom property generator for a specific type
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushAssignableTypePropertyGenerator(
        MyClass.class,
        new CustomPropertyGenerator()
    )
    .build();

// Set default property name resolver
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .defaultPropertyNameResolver(new CustomPropertyNameResolver())
    .build();

// Push property name resolver for a specific type
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushAssignableTypePropertyNameResolver(
        MyClass.class,
        new CustomPropertyNameResolver()
    )
    .build();
```

> **Key Term**: `PropertyGenerator` discovers properties in an object, while `PropertyNameResolver` determines the name of a property for expression-based access.

## 4. Container and Collection Handling Options

Control how container types like collections, maps, and arrays are handled.

### Container Introspection

Customize how container types are analyzed:

```java
// Add custom introspection for container types
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushContainerIntrospector(new CustomContainerIntrospector())
    .build();

// Configure a container type with custom handling
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .addContainerType(
        MyContainer.class,
        new MyContainerPropertyGenerator(),
        new MyContainerArbitraryIntrospector(),
        new MyContainerDecomposedContainerValueFactory()
    )
    .build();
```

> **Key Term**: A `Container` in Fixture Monkey is any object that holds multiple elements (like List, Set, Map, or array).

### Container Value Decomposition Options

Configure how elements in containers are processed:

```java
// Set default container value factory
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .defaultDecomposedContainerValueFactory(
        new CustomDecomposedContainerValueFactory()
    )
    .build();

// Add container value factory for a specific type
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .addDecomposedContainerValueFactory(
        MyCollection.class,
        new CustomDecomposedContainerValueFactory()
    )
    .build();
```

> **Key Term**: `DecomposedContainerValueFactory` helps Fixture Monkey understand how to extract values from containers.

## 5. Extension and Integration Options

These options are used to integrate Fixture Monkey with other libraries or frameworks.

### Plugin Options

Add plugin support for third-party libraries:

```java
// Add Jackson plugin
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new JacksonPlugin())
    .build();

// Combine multiple plugins
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new KotlinPlugin())
    .plugin(new JacksonPlugin())
    .build();
```

> **Key Term**: `Plugin` is an extension point that adds support for third-party libraries or frameworks.

### Group Registration Options

Register generation logic for multiple related types as a group:

```java
// Register a group of arbitraries from a class with factory methods
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .registerGroup(MyArbitraryFactoryGroup.class)
    .build();

// Register group using ArbitraryBuilderGroup interface
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .registerGroup(new MyArbitraryBuilderGroup())
    .build();
```

> **Key Term**: `ArbitraryBuilderGroup` allows registering multiple related types together.

## Code Examples (Java/Kotlin format)

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new JacksonPlugin())
    .build();

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val fixtureMonkey = FixtureMonkey.builder()
    .plugin(JacksonPlugin())
    .build()

{{< /tab >}}
{{< /tabpane>}}

## What's Next?

After exploring these options, you might want to check:

→ [Option Concepts](../concepts) - For a deeper understanding of how Fixture Monkey options work

→ [Advanced Options for Experts](../advanced-options-for-experts) - If you need more control for complex scenarios
