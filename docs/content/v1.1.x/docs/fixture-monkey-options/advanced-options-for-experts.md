---
title: "Advanced Options for Experts"
images: []
menu:
docs:
parent: "fixture-monkey-options"
identifier: "advanced-options-for-experts"
weight: 51
---

This guide covers advanced Fixture Monkey options that experienced users can leverage to solve complex testing scenarios.

> **Note for beginners**: If you're new to Fixture Monkey, we recommend starting with the [Essential Options for Beginners](../essential-options-for-beginners) guide first. The options covered here are more advanced and typically needed for complex use cases.

## Custom Type Registration and Generation

### Registering Custom Generators for Specific Types

When you need complete control over how certain types are generated:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .register(
        CreditCard.class,
        fm -> fm.giveMeBuilder(CreditCard.class)
            .set("number", creditCardGenerator())  // Use your custom generator
            .set("expiryDate", () -> LocalDate.now().plusYears(2))
            .set("cvv", Arbitraries.integers().between(100, 999))
    )
    .build();
```

**When to use:** When standard generation doesn't meet your requirements for domain-specific objects.

### Using registerExactType vs registerAssignableType

For precise control over which classes are affected by your custom generators:

```java
// Only applies to exactly Vehicle class, not subclasses
fixtureMonkey.registerExactType(
    Vehicle.class,
    fm -> fm.giveMeBuilder(Vehicle.class)
        .set("manufacturer", "Tesla")
);

// Applies to Car class and all its subclasses (SportsCar, etc.)
fixtureMonkey.registerAssignableType(
    Car.class,
    fm -> fm.giveMeBuilder(Car.class)
        .set("hasFourWheels", true)
);
```

**When to use:** When you want fine-grained control over your inheritance hierarchy in tests.

## Performance Optimization Options

### Manipulator Optimizer

For large object graphs, optimizing the generation process can significantly improve performance:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .manipulatorOptimizer(new DefaultManipulatorOptimizer())
    .generateMaxTries(50)  // Reduce max attempts for better performance
    .build();
```

**When to use:** When dealing with complex, deeply nested object structures in performance-sensitive tests.

### Streamlining Container Generation

For efficient container (lists, sets, maps) handling:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .defaultArbitraryContainerInfoGenerator(
        new DefaultArbitraryContainerInfoGenerator(1, 5)  // Smaller containers
    )
    .build();
```

**When to use:** When you want smaller, more focused collections for faster test execution.

## Advanced Customization

### Adding Custom Object Property Generators

For complex objects requiring custom property generation strategies:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushObjectPropertyGenerator(
        MatcherOperator.exactTypeMatchOperator(
            MyComplexType.class,
            (property, context) -> {
                // Custom logic to generate properties for MyComplexType
                return customGenerationLogic(property, context);
            }
        )
    )
    .build();
```

**When to use:** When you need to completely control how certain object properties are discovered and generated.

### Custom Property Name Resolution

For special property naming conventions:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushPropertyNameResolver(
        MatcherOperator.exactTypeMatchOperator(
            MyClass.class,
            property -> {
                // Custom logic to resolve property names in MyClass
                return customNameResolutionLogic(property);
            }
        )
    )
    .build();
```

**When to use:** When working with unconventional property naming schemes or reflection challenges.

## Advanced Plugin Customization

### Extending Existing Plugins

When you need to modify how a plugin works:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(
        new JacksonPlugin()
            .registerModule(new JavaTimeModule())
            .objectMapperCustomizer(mapper -> 
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL))
    )
    .build();
```

**When to use:** When integrating with frameworks that require specific configurations.

### Combining Multiple Plugins with Custom Logic

For complex test environments mixing multiple frameworks:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new KotlinPlugin())
    .plugin(new JacksonPlugin())
    .plugin(new JavaxValidationPlugin())
    .plugin(new JqwikPlugin())
    .pushJavaConstraintGeneratorCustomizer(generator -> 
        generator.overrideConstraint(Email.class, property -> 
            Arbitraries.strings().withPattern("[a-z0-9]+@company\\.com")
        )
    )
    .build();
```

**When to use:** In enterprise projects with multiple frameworks and specific validation requirements.

## Expression Strict Mode

For maintaining strict validation during object construction:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .useExpressionStrictMode()
    .build();

Product product = fixtureMonkey.giveMeBuilder(Product.class)
    .set("price", -10L)  // Will be rejected if Product requires positive prices
    .sample();
```

**When to use:** When you want expressions to strictly adhere to domain validation rules.

## Real-World Advanced Configuration

Complex real-world configuration combining multiple advanced options:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    // Framework integrations
    .plugin(new JacksonPlugin())
    .plugin(new JavaxValidationPlugin())
    
    // Performance optimizations
    .generateMaxTries(50)
    .manipulatorOptimizer(new DefaultManipulatorOptimizer())
    
    // Domain-specific customizations
    .register(Money.class, fm -> 
        fm.giveMeBuilder(Money.class)
            .set("amount", Arbitraries.longs().between(1, 1_000_000))
            .set("currency", Arbitraries.of("USD", "EUR", "GBP"))
    )
    .register(User.class, fm ->
        fm.giveMeBuilder(User.class)
            .set("email", Arbitraries.emails().endingWith("@company.com"))
            .set("roles", Arbitraries.of(Role.class))
            .set("lastLoginDate", () -> LocalDateTime.now().minusDays(
                ThreadLocalRandom.current().nextLong(0, 30))
            )
    )
    
    // Global settings
    .defaultNotNull(true)
    .nullableContainer(false)
    .seed(1234L)  // Reproducible tests
    
    .build();
```

**When to use:** For enterprise-grade test suites requiring precise control over object generation with domain constraints.

## Common Advanced Testing Scenarios

### Complex Domain Models with Relationship Constraints

```java
@Test
void testComplexDomainRelationships() {
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .defaultNotNull(true)
        // Register company with specific departments
        .register(Company.class, fm -> 
            fm.giveMeBuilder(Company.class)
                .set("departments", Arbitraries.of(Department.class).list().ofSize(3))
        )
        // Register departments with employees
        .register(Department.class, fm ->
            fm.giveMeBuilder(Department.class)
                .set("employees", Arbitraries.of(Employee.class).list().ofSize(5))
        )
        .build();
    
    // Generate a complex company structure
    Company company = fixtureMonkey.giveMeOne(Company.class);
    
    // Test complex operations
    ReorganizationResult result = reorganizationService.optimizeStructure(company);
    assertThat(result.getEfficiencyGain()).isGreaterThan(0.15); // 15% improvement
}
```

### Performance Testing with Large Data Sets

```java
@Test
void testLargeDataSetPerformance() {
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .manipulatorOptimizer(new DefaultManipulatorOptimizer())
        .defaultArbitraryContainerInfoGenerator(
            new DefaultArbitraryContainerInfoGenerator(500, 500) // Large containers
        )
        .build();
    
    // Generate large dataset
    List<Transaction> transactions = fixtureMonkey.giveMe(
        new TypeReference<List<Transaction>>() {}, 1).get(0);
    
    // Measure performance
    long startTime = System.currentTimeMillis();
    ProcessingResult result = batchProcessor.process(transactions);
    long endTime = System.currentTimeMillis();
    
    assertThat(endTime - startTime).isLessThan(2000); // Process in under 2 seconds
}
```

## What's Next?

If you're working with these advanced options, you might also want to check:

→ [Other Options by Priority](../other-options) - Explore additional options that might complement your advanced setup

→ [Option Concepts](../concepts) - Get a deeper technical understanding of how Fixture Monkey options work under the hood

If you need to customize Fixture Monkey beyond what's covered in these documents, consider extending the library directly by implementing your own plugins or generators. 
