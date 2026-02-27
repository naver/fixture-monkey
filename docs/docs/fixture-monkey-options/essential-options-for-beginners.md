---
title: "Essential Options for Beginners"
sidebar_position: 51
---


When first starting with Fixture Monkey, understanding just a few key options can help you generate the test data you need without being overwhelmed by complexity. This guide focuses on the most essential options for beginners.

> For a comprehensive understanding of core concepts like Introspectors, Generators, and Property types, see [Concepts](./concepts).
> When you're ready for more advanced options, refer to [Advanced Options for Experts](./advanced-options-for-experts).

## General Builder Options

These options are set when creating your FixtureMonkey instance:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    // Add options here
    .build();
```

### Default NotNull Setting

By default, Fixture Monkey can generate `null` values for some fields. To have all fields with non-null values:

```java
.defaultNotNull(true)
```

This is one of the most useful options to start with as it helps prevent `NullPointerException` in your tests.

**Default value**: `false` - Without this setting, Fixture Monkey can generate null values for fields not marked with `@NotNull` annotation.

**When to use:** When you want to ensure all fields are non-null to avoid NullPointerExceptions.

### Nullable Containers

Controls whether collection types (`List`, `Set`, `Map`, etc.) can be null:

```java
.nullableContainer(false)
```

**Default value**: `false` - By default, collection types cannot be null. Setting this to `true` allows collection types to be null.

**When to use:** When you need to allow or disallow null for entire container types.

### Nullable Elements

Controls whether elements inside collections can be null:

```java
.nullableElement(false)
```

**Default value**: `false` - By default, elements inside collections cannot be null. Setting this to `true` allows elements in collections to be null.

**When to use:** When you want to control nullability of individual elements within collections.

### Container Size Configuration

Control default size for collections and maps:

```java
// Configure all containers to have between 2 and 5 elements
.defaultArbitraryContainerInfoGenerator(
    new DefaultArbitraryContainerInfoGenerator(2, 5)
)
```

**Default value**: By default, containers have between 0 and 3 elements.

**When to use:** When you need consistent collection sizes across your tests, or when you want to control the volume of generated data.

> For more advanced container handling options, see [Container Types vs. Object Types](./concepts#container-types-vs-object-types) in the Concepts documentation.

### Type Configuration

You can configure how specific types are generated:

```java
// Configure how String values are generated
.register(String.class, fm -> 
    fm.giveMeBuilder(String.class)
        .set("$", "Default String")
)

// Configure how Integer values are generated
.register(Integer.class, fm -> 
    fm.giveMeBuilder(Integer.class)
        .set("$", Arbitraries.integers().between(1, 100))
)

// Configure how BigDecimal values are generated
.register(BigDecimal.class, fm -> 
    fm.giveMeBuilder(BigDecimal.class)
        .set("$", new BigDecimal("10.00"))
)

// Configure how List<String> values are generated
.register(new TypeReference<List<String>>() {}, fm -> 
    fm.giveMeBuilder(new TypeReference<List<String>>() {})
        .size("$", 1, 5)
)
```

**When to use:** When you need to apply custom generation rules for specific types.

> For a deeper understanding of the type registration system, see [Type Registration System](./concepts#4-type-registration-system) in the Concepts documentation.

### JqwikPlugin Options

Fixture Monkey provides JqwikPlugin that integrates with the [Jqwik](https://jqwik.net/) library. This plugin offers several options to control how basic types are generated:

#### Customizing String, Number, and Boolean Generation

You can customize how Strings, Numbers, Booleans and other primitive types are generated:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(
        new JqwikPlugin()
            .javaTypeArbitraryGenerator(new JavaTypeArbitraryGenerator() {
                @Override
                public StringArbitrary strings() {
                    // Generate only 10-character alphabetic strings
                    return Arbitraries.strings().alpha().ofLength(10);
                }
                
                @Override
                public IntegerArbitrary integers() {
                    // Generate only positive integers
                    return Arbitraries.integers().greaterOrEqual(1);
                }
                
                @Override
                public DoubleArbitrary doubles() {
                    // Generate decimals between 0 and 1
                    return Arbitraries.doubles().between(0.0, 1.0);
                }
            })
    )
    .build();
```

**Default behavior**: Without customization, Fixture Monkey generates random values across the entire range of each type.
**When to use**: When you need test data to follow specific patterns or ranges, such as positive numbers only or formatted strings.

#### Customizing Date and Time Generation

Similar to basic types, you can control how date and time values are generated:

```java
.plugin(
    new JqwikPlugin()
        .javaTimeTypeArbitraryGenerator(new JavaTimeTypeArbitraryGenerator() {
            @Override
            public Arbitrary<LocalDate> localDates() {
                // Generate only dates within the next 30 days
                LocalDate today = LocalDate.now();
                return Arbitraries.dates()
                    .between(today, today.plusDays(30));
            }
            
            @Override
            public Arbitrary<LocalTime> localTimes() {
                // Generate only business hours (9 AM to 5 PM)
                return Arbitraries.times()
                    .between(LocalTime.of(9, 0), LocalTime.of(17, 0));
            }
        })
)
```

**Default behavior**: Random dates and times across their entire possible ranges.
**When to use**: When you need realistic date ranges or specific time patterns for your tests.

> For more advanced customization of types and generators, see [Advanced Options for Experts](./advanced-options-for-experts#custom-type-registration-and-generation).

### Custom Null Probability

Control how often null values are generated:

```java
// Configure default null injection behavior (10% chance of null)
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .defaultNullInjectGenerator(context -> 0.1)
    .build();

// Configure type-specific null injection behavior (20% chance of null for Strings)
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushAssignableTypeNullInjectGenerator(
        String.class,
        context -> 0.2
    )
    .build();
```

**Default value**: By default, null generation probability is determined by the property's annotations, typically 0 for fields with `@NotNull` and a non-zero value otherwise.

**When to use**: When you need to control how frequently null values appear in your test fixtures, either globally or for specific types.

### Excluding Types from Generation

Control which types or packages should be excluded from test data generation:

```java
// Exclude a specific class
.addExceptGenerateClass(MyInternalClass.class)

// Exclude multiple classes
.addExceptGenerateClasses(ClassA.class, ClassB.class)

// Exclude an entire package
.addExceptGeneratePackage("com.mycompany.internal")

// Exclude based on a custom matcher
.pushExceptGenerateType(property -> property.getName().equals("sensitiveField"))
```

**Default value**: None - By default, Fixture Monkey attempts to generate all properties.

**When to use:** When you want to exclude internal implementation details, sensitive fields, or complex dependencies from test data generation.

### Registering Groups of Types

Use `registerGroup` to register multiple related types at once via a group class or implementation:

```java
// Using a factory group class
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .registerGroup(MyArbitraryFactoryGroup.class)
    .build();

// Using an implementation of ArbitraryBuilderGroup
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .registerGroup(new MyArbitraryBuilderGroup())
    .build();
```

**When to use:** When you have several related types with similar generation logic and want to register them together.

### Custom Object Introspection

Controls how Fixture Monkey analyzes and understands your objects to generate test data:

```java
// Set a custom introspector for all types
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(new CustomObjectIntrospector())
    .build();

// Example: Using a built-in introspector for all types
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
    .build();
```

**Default value**: The default introspector depends on the plugins you've added. Without plugins, it uses field-based introspection.

**When to use:** When you need to change how Fixture Monkey discovers fields and properties in your classes. For example, if you want to use field-based introspection instead of getter/setter-based introspection, or if you need a custom strategy for handling specific object types.

> For a deeper understanding of introspectors and generators, see [Generators and Introspectors](./concepts#1-generators-and-introspectors) in the Concepts documentation.

### Debugging and Reproducibility Options

These options help with debugging and ensuring test reproducibility:

```java
// Enable detailed logging for generation failures
.enableLoggingFail(true)

// Set a fixed seed for deterministic test data generation
.seed(1234L)
```

**Default values**: 
- `enableLoggingFail`: `false` - detailed error logs are not shown by default
- `seed`: Current system time - making each run use different random values by default

**When to use:**
- Enable logging when you need to debug why fixture generation is failing
- Set a fixed seed when you need reproducible test data across test runs

## Using Options in Different Scenarios

- **When you need quick test data and specific values don't matter:**
  - Use the basic builder with `defaultNotNull(true)`

- **When you need realistic values (e.g., for demos):**
  - Register custom generators for each type
  - Set min/max values for numeric fields

- **When you need to test boundary conditions:**
  - Use `.set()` for important properties with specific values or constraints
  - Use predicates for complex conditions

- **When you need to generate related data:**
  - Use nested `giveMeBuilder` calls to create related objects

## Next Steps

Now that you understand the essential options, you can learn about:

→ [Option Concepts](./concepts) - Gain deeper knowledge of how options work internally

→ [JavaBean Validation](../plugins/jakarta-validation-plugin/features) - Use validation annotations to guide data generation

