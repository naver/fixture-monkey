---
title: "Creating Custom Introspector"
sidebar_position: 46
---


> **Note**: This guide is for advanced users who already understand Fixture Monkey basics. Most users won't need to create custom introspectors as the built-in ones handle common scenarios. If you're just getting started, check the [Introspector](./introspector) guide first.

## When Would You Need a Custom Introspector?

You might need to create a custom introspector in these specific situations:

1. Your classes have unique creation requirements that built-in introspectors can't handle
2. You're working with third-party libraries that follow unconventional patterns
3. Your objects need special initialization logic that can't be addressed with the `instantiate` method

If none of these apply to you, the built-in introspectors will likely be sufficient.

## Basic Approaches

There are two main ways to create custom introspectors:

### 1. Assembling Objects from Properties

This approach is useful when you need to manually construct objects using their properties:

```java
// Example custom introspector that handles a specific class type
public class CustomArbitraryIntrospector implements ArbitraryIntrospector {
    @Override
    public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
        // Step 1: Check if this introspector should handle this type
        Property property = context.getResolvedProperty();
        Class<?> type = Types.getActualType(property.getType());
        if (!MyCustomClass.class.isAssignableFrom(type)) {
            // If not our target type, let other introspectors handle it
            return ArbitraryIntrospectorResult.NOT_INTROSPECTED;
        }
        
        // Step 2: Get the properties Fixture Monkey generated for this object
        Map<ArbitraryProperty, CombinableArbitrary<?>> arbitrariesByProperty = 
            context.getCombinableArbitrariesByArbitraryProperty();
        
        // Step 3: Build the object using these properties
        CombinableArbitrary<MyCustomClass> combinableArbitrary = CombinableArbitrary.objectBuilder()
            .properties(arbitrariesByProperty)
            .build(propertyValues -> {
                // Create a new instance of our class
                MyCustomClass obj = new MyCustomClass();
                
                // Set each property value
                propertyValues.forEach((property, value) -> {
                    String propertyName = property.getName();
                    if ("name".equals(propertyName)) {
                        obj.setName((String) value);
                    } else if ("value".equals(propertyName)) {
                        obj.setValue((Integer) value);
                    }
                });
                
                return obj;
            });
            
        // Step 4: Return the result
        return new ArbitraryIntrospectorResult(combinableArbitrary);
    }
}
```

### 1.1 Defining Required Properties

Sometimes you need to customize how child properties are discovered and generated:

```java
// Override this method to control property generation for specific properties
@Override
@Nullable
public PropertyGenerator getRequiredPropertyGenerator(Property property) {
    // Check if this property needs special handling
    if ("nestedObject".equals(property.getName())) {
        // Create a generator that only includes specific fields
        return new FieldPropertyGenerator(
            // Only include id and name fields
            field -> "id".equals(field.getName()) || "name".equals(field.getName()),
            // Match all fields that pass the filter
            field -> true
        );
    }
    
    // For other properties, use the default generator
    return null;
}
```

### 2. Returning Fixed Instances

Sometimes you just need to return a constant or specially calculated value:

```java
// Example introspector that returns a fixed value for a specific type
public class ConstantArbitraryIntrospector implements ArbitraryIntrospector {
    private final Object constantValue;
    
    public ConstantArbitraryIntrospector(Object constantValue) {
        this.constantValue = constantValue;
    }
    
    @Override
    public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
        Property property = context.getResolvedProperty();
        Class<?> type = Types.getActualType(property.getType());
        
        // Make sure our constant is the right type
        if (!type.isInstance(constantValue)) {
            return ArbitraryIntrospectorResult.NOT_INTROSPECTED;
        }
        
        // Return our constant value
        return new ArbitraryIntrospectorResult(
            CombinableArbitrary.from(constantValue)
        );
    }
}
```

## Using Your Custom Introspector

After creating your introspector, you can use it in two ways:

### As the Global Introspector

##### Standalone Usage

```java
// Create your custom introspector
ArbitraryIntrospector customIntrospector = new CustomArbitraryIntrospector();

// Use it as the global introspector
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(customIntrospector)
    .build();

// Generate objects
MyCustomClass obj = fixtureMonkey.giveMeOne(MyCustomClass.class);
```

##### Combined with Other Introspectors

Usually, you'll want to combine your custom introspector with the built-in ones:

```java
// Create a Fixture Monkey that tries your introspector first,
// then falls back to the standard ones if yours doesn't apply
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(new FailoverIntrospector(
        Arrays.asList(
            customIntrospector,  // Try your custom one first
            ConstructorPropertiesArbitraryIntrospector.INSTANCE,
            BuilderArbitraryIntrospector.INSTANCE,
            FieldReflectionArbitraryIntrospector.INSTANCE,
            BeanArbitraryIntrospector.INSTANCE
        )
    ))
    .build();
```

### As a Type-Specific Introspector

```java
// Use customIntrospector only for MyCustomClass
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .pushArbitraryIntrospector(
                new MatcherOperator<>(
                        new ExactTypeMatcher(MyCustomClass.class),
                        customIntrospector
                )
        )
        .build();
```

For more information on various `ArbitraryIntrospector` configuration options, see [Custom Introspection Settings](../fixture-monkey-options/advanced-options-for-experts#custom-introspection-settings).

## Best Practices

When creating custom introspectors:

1. **Always check the type** before processing, returning `NOT_INTROSPECTED` for types your introspector doesn't handle
2. **Handle exceptions gracefully** to prevent test failures
3. **Keep it focused** - each introspector should handle a specific pattern or class type
4. **Consider performance** since introspectors run for every object creation
5. **Test thoroughly** with various edge cases

#### Real-world Example: Class Range

```java
// External library - Class Range<C> using Instant as generic type
public class RangeInstantArbitraryIntrospector implements ArbitraryIntrospector {

    @Override
    public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
        Property property = context.getResolvedProperty();
        Class<?> type = Types.getActualType(property.getType());
        List<AnnotatedType> typeArguments = Types.getGenericsTypes(property.getAnnotatedType());
        Class<?> genericType = typeArguments.isEmpty() ? null : Types.getActualType(typeArguments.getFirst());
        if (!type.equals(Range.class)
                || typeArguments.size() != 1
                || !genericType.equals(Instant.class)) {
            return ArbitraryIntrospectorResult.NOT_INTROSPECTED;
        }

        // ===== Random generation example =====
        int randomInt = (int)(Math.random() * 365) + 1;

        Instant startTime = Instant.now().minus(randomInt, ChronoUnit.DAYS);
        Instant endTime = Instant.now().plus(randomInt, ChronoUnit.DAYS);

        Range<Instant> rangeValue = Range.closed(startTime, endTime);

        return new ArbitraryIntrospectorResult(
                CombinableArbitrary.from(rangeValue)
        );
    }
}
```

#### Real-world Example: Class InetAddress

You can also handle class type matching in `.pushArbitraryIntrospector()` instead of inside the introspector.

```java
// Class java.net.InetAddress
public class InetAddressArbitraryIntrospector implements ArbitraryIntrospector {

    @Override
    public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
        Property property = context.getResolvedProperty();
        Class<?> type = Types.getActualType(property.getType());

        InetAddress inetAddress;
        if (type.equals(Inet4Address.class)){
            inetAddress = generateRandomInet4Address();
        } else {
            inetAddress = generateRandomInet6Address();
        }

        return new ArbitraryIntrospectorResult(
                CombinableArbitrary.from(inetAddress)
        );
    }
    
    private Inet4Address generateRandomInet4Address() {
        // Implement random generation logic
    }

    private Inet6Address generateRandomInet6Address() {
        // Implement random generation logic
    }
}
```

```java
ArbitraryIntrospector inetAddressArbitraryIntrospector = new InetAddressArbitraryIntrospector();

// Use InetAddressArbitraryIntrospector only for InetAddress and its subclasses
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .pushArbitraryIntrospector(
                new MatcherOperator<>(
                        new AssignableTypeMatcher(InetAddress.class),
                        inetAddressArbitraryIntrospector
                )
        )
        // ...
        .build();
```

## Advanced: Property Generators

Fixture Monkey provides several built-in `PropertyGenerator` implementations that can help with custom property discovery:

### FieldPropertyGenerator

Useful for creating properties based on class fields:

```java
// Generate properties based on fields with specific conditions
new FieldPropertyGenerator(
    // Only non-final fields with a specific annotation
    field -> !Modifier.isFinal(field.getModifiers()) && 
             field.isAnnotationPresent(MyRequired.class),
    // Include all fields that pass the filter
    field -> true
)
```

### CompositePropertyGenerator

Combines multiple property generators:

```java
// Use both field and JavaBeans property generation together
new CompositePropertyGenerator(
    Arrays.asList(
        new FieldPropertyGenerator(field -> true, matcher -> true),
        new JavaBeansPropertyGenerator(
            descriptor -> descriptor.getReadMethod() != null, 
            matcher -> true
        )
    )
)
```

### DefaultPropertyGenerator

A pre-configured combination of common generators:

```java
// Uses standard field and JavaBeans property generation
new DefaultPropertyGenerator()
```

## Conclusion

Creating custom introspectors is an advanced topic, but it gives you complete control over object creation in Fixture Monkey. Most users won't need this level of customization, but it's available when you have special requirements that the built-in introspectors can't handle.

If you have any questions about custom introspectors, refer to the source code of the built-in introspectors for examples of different implementation approaches. 

