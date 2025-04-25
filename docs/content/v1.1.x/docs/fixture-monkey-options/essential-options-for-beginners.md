---
title: "Essential Options for Beginners"
images: []
menu:
docs:
parent: "fixture-monkey-options"
identifier: "essential-options-for-beginners"
weight: 52
---

When you're just starting with Fixture Monkey, understanding a few key options will help you generate test data that meets your needs without overwhelming you with complexity. This guide focuses on the most essential options for beginners.

## Common Builder Options

These options are set when creating your `FixtureMonkey` instance:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    // Options go here
    .build();
```

### Default Not Null

By default, Fixture Monkey might generate `null` values for some fields. If you want to ensure all fields have non-null values:

```java
.defaultNotNull(true)
```

This is one of the most useful options when starting out, as it helps avoid `NullPointerException` in your tests.

### Nullable Container

Controls whether collection types (like `List`, `Set`, `Map`) can be null:

```java
.nullableContainer(false)  // Collections will never be null
```

### Default List Size

Sets the default size range for generated lists:

```java
.defaultListSize(1, 5)  // Lists will have between 1 and 5 elements
```

### String Length

Controls the length of generated strings:

```java
.defaultStringLength(5, 10)  // Strings will be between 5 and 10 characters
```

## Type-Specific Configuration

You can configure how specific types are generated:

```java
// Configure how String values are generated
.register(String.class, fixtureMonkey -> fixtureMonkey.giveMeBuilder(String.class)
    .set("$", "Default string")  // Set default value
)

// Configure how Integer values are generated
.register(Integer.class, fixtureMonkey -> fixtureMonkey.giveMeBuilder(Integer.class)
    .set("$", it -> it >= 1 && it <= 100)  // Set condition for values between 1 and 100
)

// Configure how BigDecimal values are generated
.register(BigDecimal.class, fixtureMonkey -> fixtureMonkey.giveMeBuilder(BigDecimal.class)
    .set("$", new BigDecimal("10.00"))  // Set default value
)

// Configure how List values are generated
.register(List.class, fixtureMonkey -> fixtureMonkey.giveMeBuilder(List.class)
    .size("$", 1, 5)  // Set list size to between 1 and 5
)
```

## Property Expression Options

When generating a specific object, you can customize individual properties:

```java
Product product = fixtureMonkey.giveMeBuilder(Product.class)
    .set("name", "Ergonomic Chair")  // Set exact value
    .set("price", (BigDecimal p) -> p.compareTo(BigDecimal.TEN) > 0)  // Set with predicate
    .set("id", fixtureMonkey.giveMeBuilder(Long.class)
        .set("$", it -> it >= 1000L && it <= 9999L)
        .sample())  // Set with another builder
    .set("tags[0]", "furniture")  // Set array/list element
    .sample();
```

## What Options to Use When

- **You need quick test data and don't care about specific values:**
  - Use the default builder with `defaultNotNull(true)`

- **You need realistic values (e.g., for a demo):**
  - Register custom generators for each type
  - Set minimum/maximum values for numeric fields
  - Configure string length appropriately

- **You need to test boundary conditions:**
  - Set specific values or constraints using `.set()` for properties that matter
  - Use predicates for complex conditions

- **You want to generate related data:**
  - Use nested `giveMeBuilder` calls to create related objects

## Next Steps

Now that you understand the essential options, you may want to learn about:

→ [Understanding Option Concepts](../concepts) - Dive deeper into how options work

→ [Using Property Expression to Target Values](../../generate-objects/property-expression) - Learn more advanced ways to specify properties

→ [Using JavaBean Validation](../../plugins/junit-plugin) - Let your validation annotations guide data generation 
