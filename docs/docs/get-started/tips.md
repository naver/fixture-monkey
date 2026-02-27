---
title: "Tips for Beginners"
sidebar_position: 27
---


## Essential Tips for Using Fixture Monkey

### 1. Use Type-Safe Methods
- Prefer type-safe methods over string-based ones
- Example:
```java
// Instead of
.set("price", 1000L)

// Use
.set(javaGetter(Product::getPrice), 1000L)
```

### 2. Use Meaningful Test Data
- Use values that make sense in your test context
- Avoid using arbitrary values like "test" or "123"
- Consider business rules and constraints when setting values
- Benefits:
  - Makes tests more readable and self-documenting
  - Helps identify test failures more quickly
  - Makes it easier to understand test scenarios
  - Reduces the need for additional comments
- Example:
```java
// Use meaningful values
Product product = fixtureMonkey.giveMeBuilder(Product.class)
    .set("price", 1000L)    // Use realistic price that matches business rules
    .set("name", "Premium Product")  // Use descriptive name that indicates product type
    .set("category", "ELECTRONICS")  // Use valid category from your domain
    .set("stock", 50)       // Use reasonable stock quantity
    .sample();
```

### 3. Keep Tests Readable
- Add comments to explain why specific values are set
- Example:
```java
Product product = fixtureMonkey.giveMeBuilder(Product.class)
    .set("price", 2000L)    // Price above discount threshold
    .set("category", "PREMIUM")  // Category that gets special treatment
    .sample();
```

### 4. Handle Collections Properly
- Set collection size before accessing specific indices
- Example:
```java
Product product = fixtureMonkey.giveMeBuilder(Product.class)
    .size("options", 3)          // Set size first
    .set("options[1]", "red")    // Then access specific index
    .sample();
```

### 5. Reuse FixtureMonkey Instance
- Create one instance and reuse it across tests
- Example:
```java
public class ProductTest {
    private static final FixtureMonkey FIXTURE_MONKEY = FixtureMonkey.builder()
        .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
        .build();

    @Test
    void test1() {
        Product product = FIXTURE_MONKEY.giveMeBuilder(Product.class).sample();
        // ...
    }

    @Test
    void test2() {
        Product product = FIXTURE_MONKEY.giveMeBuilder(Product.class).sample();
        // ...
    }
}
```

### 6. Reuse ArbitraryBuilder
- Reuse ArbitraryBuilder instances to maintain consistent test data structure
- Share common configurations across multiple tests
- Improve code readability by centralizing test data setup
- Example:
```java
public class ProductTest {
    private static final FixtureMonkey FIXTURE_MONKEY = FixtureMonkey.builder()
        .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
        .build();
    
    // Base configuration for premium products
    private static final ArbitraryBuilder<Product> PREMIUM_PRODUCT_BUILDER = FIXTURE_MONKEY.giveMeBuilder(Product.class)
        .set("category", "PREMIUM")
        .set("price", 1000L);

    @Test
    void testDiscountForPremiumProduct() {
        // Test discount for premium product with price above threshold
        Product product = PREMIUM_PRODUCT_BUILDER
            .set("price", 2000L)  // Price above discount threshold
            .sample();
        // Test discount logic
    }

    @Test
    void testShippingForPremiumProduct() {
        // Test shipping for premium product with minimum order amount
        Product product = PREMIUM_PRODUCT_BUILDER
            .set("price", 5000L)  // Price above free shipping threshold
            .sample();
        // Test shipping logic
    }
}
```

### 7. Start with Simple Objects
- Begin with basic objects before moving to complex ones
- Example:
```java
public class ProductTest {
    private static final FixtureMonkey FIXTURE_MONKEY = FixtureMonkey.builder()
        .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
        .build();

    @Test
    void testBasicProduct() {
        // Start with a simple object
        Product product = FIXTURE_MONKEY.giveMeBuilder(Product.class)
            .set("name", "Test Product")
            .sample();
        // ...
    }
}
```

### 8. Use the IntelliJ Plugin
Install the [Fixture Monkey Helper](https://plugins.jetbrains.com/plugin/19589-fixture-monkey-helper) plugin to enhance your development experience:
- Smart code completion for Fixture Monkey methods
- Type-safe field access suggestions using method references
- Quick navigation to field definitions
- Automatic import suggestions for Fixture Monkey classes
- Real-time validation of field names and types

### 9. Common Use Cases
- Testing validation rules
- Testing business logic with specific conditions
- Creating test data for integration tests
- Generating random but valid test data

### 10. Best Practices
- Keep test data generation close to where it's used
- Use meaningful variable names
- Document complex test scenarios
- Use constants for frequently used values

