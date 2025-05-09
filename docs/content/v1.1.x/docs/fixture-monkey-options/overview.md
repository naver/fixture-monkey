---
title: "Overview"
images: []
menu:
docs:
parent: "fixture-monkey-options"
identifier: "overview"
weight: 50
---

When you're starting with Fixture Monkey, using options can seem overwhelming. This guide helps you understand how to navigate the options and where to start.

## Options vs ArbitraryBuilder API

There are two main ways to configure test data in Fixture Monkey:

1. **Options**
   - Set during FixtureMonkey instance creation
   - Define global rules that apply to all test data generation
   - Reusable configurations

2. **ArbitraryBuilder API**
   - Set during individual test data creation
   - One-time settings needed for specific test cases
   - More fine-grained control

Here's an example:

```java
// Using options - applies to all Product instances
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .defaultNotNull(true)  // Set all fields to non-null
    .register(Product.class, builder -> builder
        .size("items", 3))  // items always has 3 elements
    .build();

// Using ArbitraryBuilder API - applies only to this test
Product specificProduct = fixtureMonkey.giveMeBuilder(Product.class)
    .set("name", "Test Product")  // Set name just for this test
    .set("price", 1000)          // Set price just for this test
    .sample();
```

## Why Should You Use Options?

There are important reasons to use options:

### 1. Test Data Consistency
- **Problem**: Need to apply the same rules across multiple tests
  ```java
  // Without options - need to repeat settings in every test
  Product product1 = fixtureMonkey.giveMeBuilder(Product.class)
      .set("price", Arbitraries.longs().greaterThan(0))
      .sample();
  
  Product product2 = fixtureMonkey.giveMeBuilder(Product.class)
      .set("price", Arbitraries.longs().greaterThan(0))
      .sample();
  ```
  ```java
  // With options - set once, apply everywhere
  FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
      .register(Product.class, builder -> builder
          .set("price", Arbitraries.longs().greaterThan(0)))
      .build();
  
  Product product1 = fixtureMonkey.giveMeOne(Product.class);  // Automatically positive price
  Product product2 = fixtureMonkey.giveMeOne(Product.class);  // Automatically positive price
  ```

### 2. Domain Rule Application
- **Problem**: Need to apply business rules to test data
  ```java
  // Applying domain rules through options
  FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
      .register(Order.class, builder -> builder
          .set("totalAmount", (order) -> 
              order.getItems().stream()
                  .mapToInt(Item::getPrice)
                  .sum()))  // totalAmount is always sum of item prices
      .build();
  ```

### 3. Test Maintainability
- **Problem**: Need to modify all tests when rules change
  ```java
  // Using options - manage rules in one place
  public class TestConfig {
      public static FixtureMonkey createFixtureMonkey() {
          return FixtureMonkey.builder()
              .defaultNotNull(true)
              .register(Product.class, productRules())
              .register(Order.class, orderRules())
              .build();
      }
  
      private static Consumer<ArbitraryBuilder<?>> productRules() {
          return builder -> builder
              .set("price", Arbitraries.longs().greaterThan(0))
              .set("stock", Arbitraries.integers().greaterThan(0));
      }
  }
  ```

## Understanding Option Scope

There are important points to understand when using options:

1. **Instance Scope**
   - Options only apply to the FixtureMonkey instance they're configured on
   - You can create multiple instances with different settings

```java
// Test settings
FixtureMonkey testFixture = FixtureMonkey.builder()
    .defaultNotNull(true)
    .build();

// Development settings
FixtureMonkey devFixture = FixtureMonkey.builder()
    .defaultNotNull(false)
    .build();
```

2. **Option Priority**
   - More specific options take precedence over general ones
   - Later options override earlier ones

```java
FixtureMonkey fixture = FixtureMonkey.builder()
    .defaultNotNull(true)            // All fields non-null
    .register(Product.class, builder -> builder
        .setNull("description"))     // Allow null for description only
    .build();
```
