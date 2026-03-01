---
title: "Introspector"
sidebar_position: 41
---


## What is an Introspector?

An `Introspector` in Fixture Monkey is simply a tool that determines how test objects are created. Think of it as a "factory" that figures out the best way to create objects for your tests.

For example, it decides:
- Whether to use a constructor or a builder to create objects
- How to set values for fields
- How to handle different types of classes in your codebase

## Quick Start: Recommended Setup for Most Projects

If you're new to Fixture Monkey and want to get started quickly, here's the setup that works for most projects:

```java
// Recommended setup that handles most class types
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(new FailoverIntrospector(
        Arrays.asList(
            ConstructorPropertiesArbitraryIntrospector.INSTANCE,
            BuilderArbitraryIntrospector.INSTANCE,
            FieldReflectionArbitraryIntrospector.INSTANCE,
            BeanArbitraryIntrospector.INSTANCE
        ),
        false // Disable logging for cleaner test output
    ))
    .build();

// Use it in your tests
@Test
void testExample() {
    // Generate a test object
    MyClass myObject = fixtureMonkey.giveMeOne(MyClass.class);
    
    // Use the generated object in your test
    assertThat(myObject).isNotNull();
    // more assertions...
}
```

This setup combines multiple strategies to handle different class types, so it works well for most real-world projects without additional configuration.

## Simplest Approach (If You Just Want Basic Setup)

If you prefer the simplest possible setup, you can use the default configuration:

```java
// Simplest approach with default settings
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .build();

// Generate a test object
MyClass myObject = fixtureMonkey.giveMeOne(MyClass.class);
```

However, this basic approach only works well with simple JavaBean classes that have a no-arguments constructor and setter methods.

## Choosing the Right Introspector for Your Classes

Different class types require different approaches to object creation. Here's a simple guide to help you choose:

| Class Type | Recommended Introspector | Example |
|------------|--------------------------|---------|
| **Classes with setters (JavaBeans)** | `BeanArbitraryIntrospector` | Classes with getters/setters |
| **Immutable classes with constructors** | `ConstructorPropertiesArbitraryIntrospector` | Records, classes with annotated constructors |
| **Classes with mixed field access** | `FieldReflectionArbitraryIntrospector` | Classes with public fields, no-args constructor |
| **Classes using builder pattern** | `BuilderArbitraryIntrospector` | Classes with `.builder()` method |
| **Mixed codebase with different patterns** | `FailoverArbitraryIntrospector` | Projects with various class types |

## Examples for Common Class Types

### Example 1: Standard JavaBean Class (with getters/setters)

```java
// Class definition
public class Customer {
    private String name;
    private int age;
    
    // No-args constructor
    public Customer() {}
    
    // Setters
    public void setName(String name) { this.name = name; }
    public void setAge(int age) { this.age = age; }
    
    // Getters
    public String getName() { return name; }
    public int getAge() { return age; }
}

// Test code
@Test
void testCustomer() {
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .objectIntrospector(BeanArbitraryIntrospector.INSTANCE) // Default, so optional
        .build();
    
    Customer customer = fixtureMonkey.giveMeOne(Customer.class);
    
    assertThat(customer.getName()).isNotNull();
    assertThat(customer.getAge()).isGreaterThanOrEqualTo(0);
}
```

### Example 2: Immutable Class with Constructor

```java
// Class definition (with @ConstructorProperties)
public class Product {
    private final String name;
    private final double price;
    
    @ConstructorProperties({"name", "price"})
    public Product(String name, double price) {
        this.name = name;
        this.price = price;
    }
    
    public String getName() { return name; }
    public double getPrice() { return price; }
}

// Test code
@Test
void testProduct() {
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
        .build();
    
    Product product = fixtureMonkey.giveMeOne(Product.class);
    
    assertThat(product.getName()).isNotNull();
    assertThat(product.getPrice()).isGreaterThanOrEqualTo(0.0);
}

// Works with Java records too
public record OrderItem(String productId, int quantity, double price) {}
```

### Example 3: Class with Builder Pattern

```java
// Class definition with builder
public class User {
    private final String username;
    private final String email;
    
    private User(Builder builder) {
        this.username = builder.username;
        this.email = builder.email;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String username;
        private String email;
        
        public Builder username(String username) {
            this.username = username;
            return this;
        }
        
        public Builder email(String email) {
            this.email = email;
            return this;
        }
        
        public User build() {
            return new User(this);
        }
    }
    
    public String getUsername() { return username; }
    public String getEmail() { return email; }
}

// Test code
@Test
void testUser() {
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .objectIntrospector(BuilderArbitraryIntrospector.INSTANCE)
        .build();
    
    User user = fixtureMonkey.giveMeOne(User.class);
    
    assertThat(user.getUsername()).isNotNull();
    assertThat(user.getEmail()).isNotNull();
}
```

## Why Introspectors Matter

Different projects use different patterns for object creation:

- Some use simple classes with getters/setters
- Others use immutable objects with constructors
- Some follow the builder pattern
- Frameworks like Lombok generate code in specific ways

By choosing the right introspector, you can make Fixture Monkey work with your existing code without modifications, saving you time and effort.

## Frequently Asked Questions (FAQ)

### Q: I'm not sure which introspector to use. What should I do?
**A**: Start with the recommended setup (using `FailoverIntrospector` with multiple introspectors). It works for most projects and automatically tries different strategies.

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(new FailoverIntrospector(
        Arrays.asList(
            ConstructorPropertiesArbitraryIntrospector.INSTANCE,
            BuilderArbitraryIntrospector.INSTANCE,
            FieldReflectionArbitraryIntrospector.INSTANCE,
            BeanArbitraryIntrospector.INSTANCE
        ),
        false // Disable logging for cleaner test output
    ))
    .build();
```

### Q: My objects aren't being generated. What should I check?
**A**: Ensure your class has one of the following:
- A no-args constructor with setters (for `BeanArbitraryIntrospector`)
- A constructor with `@ConstructorProperties` (for `ConstructorPropertiesArbitraryIntrospector`)
- A builder method (for `BuilderArbitraryIntrospector`)

### Q: I'm using Lombok and my objects aren't generating properly. What should I do?
**A**: Add `lombok.anyConstructor.addConstructorProperties=true` to your lombok.config file and use `ConstructorPropertiesArbitraryIntrospector`.

### Q: What if I need custom creation logic for a specific class?
**A**: For specific cases, you can use the `instantiate` method to specify how an instance should be created:

```java
MySpecialClass object = fixtureMonkey.giveMeBuilder(MySpecialClass.class)
    .instantiate(() -> new MySpecialClass(specialParam1, specialParam2))
    .sample();
```

For more advanced custom logic, see the [Custom Introspector](./custom-introspector) guide, but most users won't need this.

## Available Introspectors (More Details)

### BeanArbitraryIntrospector (Default)
Best for: Standard JavaBean classes with setters

Requirements:
- Class must have a no-args constructor
- Class must have setter methods for properties

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(BeanArbitraryIntrospector.INSTANCE) // This is the default
    .build();
```

### ConstructorPropertiesArbitraryIntrospector
Best for: Immutable objects with constructors

Requirements:
- Class must have a constructor with `@ConstructorProperties` or be a record type
- For Lombok, add `lombok.anyConstructor.addConstructorProperties=true` to lombok.config

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
    .build();
```

### FieldReflectionArbitraryIntrospector
Best for: Classes with field access

Requirements:
- Class must have a no-args constructor
- Fields can be accessed via reflection

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
    .build();
```

### BuilderArbitraryIntrospector
Best for: Classes using the builder pattern

Requirements:
- Class must have a builder with set methods and a build method

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(BuilderArbitraryIntrospector.INSTANCE)
    .build();
```

### FailoverArbitraryIntrospector (Recommended for Mixed Codebases)
Best for: Projects with a mix of class types

Benefits:
- Tries multiple introspectors in sequence
- Works with various class patterns
- Most versatile option

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(new FailoverIntrospector(
        Arrays.asList(
            ConstructorPropertiesArbitraryIntrospector.INSTANCE,
            BuilderArbitraryIntrospector.INSTANCE,
            FieldReflectionArbitraryIntrospector.INSTANCE,
            BeanArbitraryIntrospector.INSTANCE
        ),
        false // Disable logging for cleaner test output
    ))
    .build();
```

If you want to disable the fail log, set the constructor argument `enableLoggingFail` to false as shown above.

:::warning
Performance note: `FailoverArbitraryIntrospector` may increase generation costs as it attempts to create objects using each registered introspector in sequence. When performance is a concern, use a specific introspector if you know your class patterns.
:::

### PriorityConstructorArbitraryIntrospector
Best for: Special cases where other introspectors don't work

Benefits:
- Uses available constructors even without `@ConstructorProperties`
- Helpful for library classes you can't modify

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(PriorityConstructorArbitraryIntrospector.INSTANCE)
    .build();
```

## Additional Introspectors from Plugins

Plugins provide additional introspectors for specific needs:
- [`JacksonObjectArbitraryIntrospector`](../plugins/jackson-plugin/jackson-object-arbitrary-introspector) for Jackson JSON objects
- [`PrimaryConstructorArbitraryIntrospector`](../plugins/kotlin-plugin/introspectors-for-kotlin) for Kotlin classes

## How Introspectors Work (Technical Details)

```mermaid
graph TD
    A[Object Creation Request] --> B{Introspector Selection}
    B -- BeanArbitraryIntrospector --> C[Use Default Constructor + Setters]
    B -- ConstructorProperties --> D[Use Annotated Constructor]
    B -- FieldReflection --> E[Set Fields with Reflection]
    B -- Builder --> F[Use Builder Pattern]
    B -- Failover --> G[Try Multiple Introspectors Sequentially]
    C --> H[Object Instance]
    D --> H
    E --> H
    F --> H
    G --> H
```

## Need More Advanced Customization?

If you have special requirements for object creation that aren't covered by the built-in introspectors, you might need to create a custom introspector.

This is an advanced topic and most users won't need it. If you're interested, see the [Custom Introspector](./custom-introspector) guide.

