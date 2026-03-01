---
title: "Advanced Options for Experts"
sidebar_position: 53
---


This guide covers advanced Fixture Monkey options that experienced users can leverage to solve complex testing scenarios.

> **Note for beginners**: If you're new to Fixture Monkey, we recommend starting with the [Essential Options for Beginners](./essential-options-for-beginners) guide first. The options covered here are more advanced and typically needed for complex use cases.
>
> Make sure you understand the core [Concepts](./concepts) before diving into these advanced options.

## Table of Contents

1. [Recommended Usage Sequence](#recommended-usage-sequence)
2. [Performance Optimization Options](#performance-optimization-options)
3. [Custom Type Registration and Generation](#custom-type-registration-and-generation)
4. [Default Arbitrary Generator](#default-arbitrary-generator)
5. [Object Property Generators](#object-property-generators)
6. [Container Handling Options](#container-handling-options)
7. [Validation & Constraints](#validation--constraints)
8. [Property Customization](#property-customization)
9. [Custom Introspection Settings](#custom-introspection-settings)
10. [Expression Strict Mode](#expression-strict-mode)
11. [Real-World Advanced Configuration](#real-world-advanced-configuration)
12. [Common Advanced Testing Scenarios](#common-advanced-testing-scenarios)
13. [What's Next?](#whats-next)

## Recommended Usage Sequence

Follow this sequence to quickly apply the most common advanced options:

1. Performance Optimization Options
2. Custom Type Registration and Generation
3. Default Arbitrary Generator
4. Object Property Generators
5. Container Handling Options
6. Validation & Constraints
7. Property Customization
8. Custom Introspection Settings
9. Expression Strict Mode
10. Real-World Advanced Configuration
11. Common Advanced Testing Scenarios

## Performance Optimization Options

### Manipulator Optimizer

Allows customizing or optimizing the sequence of `ArbitraryManipulator` instances applied during fixture generation. You can implement `ManipulatorOptimizer` to combine, filter, or reorder manipulators, reducing redundant operations and improving performance for complex object graphs:

```java
// Example custom ManipulatorOptimizer implementation
class CustomManipulatorOptimizer implements ManipulatorOptimizer {
    // Implement combine, filter, or reorder manipulators here
}

FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .manipulatorOptimizer(new CustomManipulatorOptimizer())
    .build();
```
**Default behavior:** No manipulator optimizer is applied (uses `NoneManipulatorOptimizer`).

### Adjust Maximum Generation Attempts

- **Default behavior:** 1000 retry attempts.

`generateMaxTries` sets the maximum number of retry attempts when fixture generation fails due to value constraints or expression strict mode. Reducing this can help tests fail fast in worst-case scenarios:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .generateMaxTries(50)
    .build();
```

### Adjust Unique Generation Attempts

- **Default behavior:** 1000 retry attempts.

`generateUniqueMaxTries` sets the maximum number of retry attempts when generating unique elements in collections (e.g., ensuring list or set elements are distinct). Lowering this helps tests fail fast if uniqueness cannot be achieved:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .generateUniqueMaxTries(20)
    .build();
```
**Default behavior:** 1000 retry attempts.

**When to use:** When testing scenarios require unique values in collections and you want generation to fail fast on duplicates.

### Streamlining Container Generation

For efficient container (lists, sets, maps) handling:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushArbitraryContainerInfoGenerator(
        new MatcherOperator<>(
            property -> property.getType().isAssignableFrom(List.class),
            context -> new ArbitraryContainerInfo(2, 10)
        )
    )
    .build();
```
**Default behavior:** Without customization, Fixture Monkey uses a default container size generator that creates containers with sizes from 0 to 3.

**When to use:**
- When dealing with complex, deeply nested object structures in performance-sensitive tests.
- When you need different container size rules for different collection types (e.g., lists vs. sets).
- When you need to control container sizes based on the specific container type or context.

> For basic container configuration, refer to [Container Size Configuration](./essential-options-for-beginners#container-size-configuration) in the Essential Options guide.

## Custom Type Registration and Generation

### Registering Custom Generators for Specific Types

When you need complete control over how certain types are generated:

```java
// Simplified credit card generator example
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .register(CreditCard.class, fm -> fm
        .giveMeBuilder(CreditCard.class)
        .set("number", Arbitraries.strings().numeric().ofLength(16).startsWith("4")) // formatted 16-digit starting with VISA
        .set("expiryDate", () -> LocalDate.now().plusYears(2)) // 2 years from now
        .set("cvv", Arbitraries.integers().between(100, 999)) // 3-digit between 100-999
        )
    .build();

CreditCard card = fixtureMonkey.giveMeOne(CreditCard.class);
```

**When to use each approach:**

1. **Using Arbitraries API with transformations**
   - Best for: Creating complex patterns and formats with built-in randomization
   - Use when: You need to generate values following specific formats or algorithms
   - Examples: Credit card numbers, ISBN codes, formatted identifiers

2. **Lambda expressions** (`() -> LocalDate.now().plusYears(2)`)
   - Best for: Dynamic or time-dependent values evaluated at test runtime  
   - Use when: Values need to be based on current time or change with each execution
   - Examples: Expiration dates, timestamps, incremental IDs

3. **Standard Arbitraries API** (`Arbitraries.integers().between(100, 999)`)
   - Best for: Applying simple constraints to random values
   - Use when: You need values within specific ranges or matching patterns
   - Examples: Age ranges, postal codes, constrained numeric values

**When to use:** When standard generation doesn't meet your requirements for domain-specific objects.

> For basic type registration, see [Type Configuration](./essential-options-for-beginners#type-configuration) in the Essential Options guide.
> For more on the type registration system, see [Type Registration System](./concepts#4-type-registration-system) in the Concepts documentation.

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

Note: When both `registerExactType` and `registerAssignableType` are applied to the same type, the option added last takes precedence.

### Default Arbitrary Generator

**When to use**: When you want to customize how default values are generated across all types, such as ensuring uniqueness or applying custom formatting.

Use these options to customize the base arbitrary generator for all types. Below is an example that ensures generated values are unique by filtering out duplicates:

```java
public static class UniqueArbitraryGenerator implements ArbitraryGenerator {
    private static final Set<Object> UNIQUE = new HashSet<>();
    private final ArbitraryGenerator delegate;

    public UniqueArbitraryGenerator(ArbitraryGenerator delegate) {
        this.delegate = delegate;
    }

    @Override
    public CombinableArbitrary generate(ArbitraryGeneratorContext context) {
        return delegate.generate(context)
            .filter(obj -> {
                if (!UNIQUE.contains(obj)) {
                    UNIQUE.add(obj);
                    return true;
                }
                return false;
            });
    }
}

// Usage
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .defaultArbitraryGenerator(UniqueArbitraryGenerator::new)
    .build();
```

> For a conceptual understanding of generators, see [Generators and Introspectors](./concepts#1-generators-and-introspectors) in the Concepts documentation.

## Property Customization

### Matcher-Based Property Generators

Use `pushPropertyGenerator(MatcherOperator<PropertyGenerator>)` to register a custom `PropertyGenerator` for any matching condition. For example, apply a generator to all instances of a class or package:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushPropertyGenerator(
        MatcherOperator.assignableTypeMatchOperator(
            MyClass.class,
            new CustomPropertyGenerator()
        )
    )
    .build();
```

**When to use:** When you need to apply custom property generation logic across multiple types or conditions.

### Adding Custom Object Property Generators

Use `pushExactTypePropertyGenerator` to customize property generation for specific types:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushExactTypePropertyGenerator(
        PropertyAddress.class, 
        new FieldPropertyGenerator()
    )
    .build();
```

See the table below for available `PropertyGenerator` implementations.

#### Various PropertyGenerator Implementations

<div class="table-responsive">
<table class="table table-striped table-bordered">
  <thead>
    <tr>
      <th>Implementation</th>
      <th>Description</th>
      <th>When to Use</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>FieldPropertyGenerator</code></td>
      <td>Generates properties based on class fields, including inherited and interface fields.</td>
      <td>Use for objects where field access is important. Useful when you need direct access to all fields.</td>
    </tr>
    <tr>
      <td><code>JavaBeansPropertyGenerator</code></td>
      <td>Generates properties based on JavaBeans convention with getter methods.</td>
      <td>Use for POJO classes with getter methods. Suitable for objects with encapsulation where access is only through public APIs.</td>
    </tr>
    <tr>
      <td><code>ConstructorParameterPropertyGenerator</code></td>
      <td>Generates properties based on constructor parameters.</td>
      <td>Use for immutable objects or objects with constructor injection. Particularly useful for objects using annotations like lombok's <code>@AllArgsConstructor</code>.</td>
    </tr>
    <tr>
      <td><code>CompositePropertyGenerator</code></td>
      <td>Combines multiple PropertyGenerators.</td>
      <td>Use for complex object structures where you need to combine different property generation strategies. For example, when you want to handle some properties via fields and others via constructors.</td>
    </tr>
    <tr>
      <td><code>ElementPropertyGenerator</code></td>
      <td>Generates properties for container elements.</td>
      <td>Use when dealing with elements in container types like collections, arrays, or maps.</td>
    </tr>
    <tr>
      <td><code>LazyPropertyGenerator</code></td>
      <td>Delays property generation until needed.</td>
      <td>Use for objects with expensive generation costs or circular references.</td>
    </tr>
  </tbody>
</table>
</div>

When custom property generation is needed, you can choose the most appropriate implementation for your situation or combine multiple implementations. For example, you could generate most properties using `FieldPropertyGenerator` while handling specific fields with custom logic.

**Real-world application scenarios**:
- When your system has objects with special formats or validations like addresses, product codes, or identification numbers
- When you need to completely control how an object is generated and override the default generation logic
- When you want to apply exact business rules or constraints to generate only valid test data

**When to use:** When you need complete control over how properties of a specific object are discovered and generated, especially for complex domain rules or special formats.

### Custom Property Name Resolution

#### Default Property Name Resolver

Use `defaultPropertyNameResolver` to set a global resolver applied to all properties when no specific resolver matches:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .defaultPropertyNameResolver(property -> "'" + property.getName() + "'")
    .build();
```

**When to use:** When you want to customize the default naming convention for all path expressions globally.

#### Specific Type Resolvers

Use the Jackson plugin or `pushExactTypePropertyNameResolver` to handle JSON property names:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new JacksonPlugin())
    .pushExactTypePropertyNameResolver(
        UserProfile.class, new JacksonPropertyNameResolver()
    )
    .build();
```

**When to use:** Classes with `@JsonProperty` or naming conventions that differ from field names.

### Object Property Generators

**When to use**: When you need to customize the discovery and generation of object properties, for example for nested or complex types.

Use these options to configure how object properties are found and generated:

```java
// Default generator for all object properties
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .defaultObjectPropertyGenerator(new CustomObjectPropertyGenerator())
    .build();

// Conditional generator for specific types
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushObjectPropertyGenerator(
        MatcherOperator.assignableTypeMatchOperator(
            MyClass.class,
            new CustomObjectPropertyGenerator()
        )
    )
    .build();
```

**When to use**: When you need to customize the discovery and generation of object properties, for example for nested or complex types.

## Container Handling Options

**When to use**: When you need to customize how Fixture Monkey handles collection types such as lists, sets, and maps, including element generation, introspection, and decomposition.

These options let you control how Fixture Monkey interacts with collection types:

### Container Property Generators

Use these options to control property generation within container elements:

```java
// Custom generator for container elements
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushContainerPropertyGenerator(
        MatcherOperator.assignableTypeMatchOperator(
            List.class,
            new CustomContainerPropertyGenerator()
        )
    )
    .build();
```

### Container Introspection

Use these options to customize how container types are analyzed:

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

### Container Value Decomposition Options

Use these options to configure how elements in containers are processed:

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

> For basic container handling, see [Container Size Configuration](./essential-options-for-beginners#container-size-configuration) in the Essential Options guide.
> For more on how containers are conceptually different, see [Container Types vs. Object Types](./concepts#container-types-vs-object-types) in the Concepts documentation.

## Validation & Constraints

**When to use**: When you need to enforce custom validation logic or apply Java Bean Validation constraints to generated fixtures.

Use these options to enforce custom validation and constraint logic:

```java
// Custom arbitrary validator
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .arbitraryValidator(new CustomArbitraryValidator())
    .build();

// Java constraint handling and valid-only paths
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .javaConstraintGenerator(new CustomJavaConstraintGenerator())
    .pushJavaConstraintGeneratorCustomizer(
        g -> g.overrideConstraint(MyConstraint.class, prop -> Arbitraries.strings().alpha())
    )
    .pushCustomizeValidOnly(
        TreeMatcher.exactPath("root.child.property"), true
    )
    .build();
```

**When to use**: When you need to enforce custom validation logic or apply Java Bean Validation constraints to generated fixtures.

## Custom Introspection Settings
> `pushArbitraryIntrospector`, `pushAssignableTypeArbitraryIntrospector`, `pushExactTypeArbitraryIntrospector`

Use `pushArbitraryIntrospector(MatcherOperator<ArbitraryIntrospector>)` to register a custom introspector based on matching conditions:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushArbitraryIntrospector(
        new MatcherOperator<>(
            new ExactTypeMatcher(MyClass.class), 
            new CustomArbitraryIntrospector()
        )
    )
    .build();
```

For generic classes, you can also include the number of type parameters in the matching condition.
> `SingleGenericTypeMatcher`, `DoubleGenericTypeMatcher`, `TripleGenericTypeMatcher`

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushArbitraryIntrospector(
        new MatcherOperator<>(
            new AssignableTypeMatcher(MyParameterizedClass.class).intersect(new SingleGenericTypeMatcher()),
            new CustomArbitraryIntrospector()
        )
    )
    .build();
```

**When to use:** When you want to control how objects are introspected based on specific conditions.

**Note:** Options registered later are applied first.

For information on implementing custom introspectors, see the [Creating Custom Introspector guide](../generating-objects/custom-introspector).

## Expression Strict Mode

When enabled, path expressions provided to setter APIs (e.g., `.set()`) must match existing object properties; otherwise, an exception is thrown.

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .useExpressionStrictMode()
    .build();

// Throws an exception at sample() if 'nonExistingField' is not a property of Product
Product invalid = fixtureMonkey.giveMeBuilder(Product.class)
    .set("nonExistingField", 123)
    .sample();
```

**When to use:** When you want to strictly enforce property path validity in setter expressions.

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
                .size("departments", 3)
        )
        // Register departments with employees
        .register(Department.class, fm ->
            fm.giveMeBuilder(Department.class)
                .size("employees", 5)
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

After mastering these advanced options, you can consider:

→ [Creating Custom Introspector](../generating-objects/custom-introspector) - Implement your own introspector for special domain requirements

→ [Contributing to Fixture Monkey](https://github.com/naver/fixture-monkey/blob/main/CONTRIBUTING.md) - Join the open source community

