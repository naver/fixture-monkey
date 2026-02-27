---
title: "Generating Interface Types"
sidebar_position: 45
---


## Why Generate Interface Types?

When writing tests, you often need to work with interfaces rather than concrete implementations:
- You may be testing code that accepts interfaces as parameters
- Your system under test may return interface types
- You want to test behavior without coupling to specific implementations

Fixture Monkey makes it easy to generate test objects for interfaces - whether they're simple interfaces, generic interfaces, or sealed interfaces.

## Quick Start Example

Here's a simple example to get started with interface generation:

```java
// Define an interface you want to test with
public interface StringSupplier {
	String getValue();
}

// Create a Fixture Monkey instance
FixtureMonkey fixture = FixtureMonkey.create();

// Generate an instance of the interface
StringSupplier supplier = fixture.giveMeOne(StringSupplier.class);

// Use it in your test
String value = supplier.getValue();
assertThat(value).isNotNull(); // Will pass
```

This example generates an anonymous implementation of the `StringSupplier` interface that you can use in your tests. Let's explore more options for interface generation.

## Interface Generation Approaches

Fixture Monkey provides three main approaches for generating interface instances:

| Approach | Description | Best For |
|----------|-------------|----------|
| **Anonymous implementation** | Fixture Monkey creates an anonymous class | Quick tests, simple interfaces |
| **Specific implementation** | You specify which class to use | More control, realistic behavior |
| **Built-in implementations** | Fixture Monkey provides defaults for common interfaces | Standard Java interfaces |

### Examples for Each Approach

```java
// Anonymous implementation
StringSupplier supplier = fixture.giveMeOne(StringSupplier.class);

// Specific implementation
InterfacePlugin plugin = new InterfacePlugin()
    .interfaceImplements(StringSupplier.class, List.of(DefaultStringSupplier.class));

// Built-in implementation
List<String> list = fixture.giveMeOne(new TypeReference<List<String>>() {});
```

## Common Interface Types with Built-in Support

Fixture Monkey provides default implementations for common Java interfaces:

- `List` → `ArrayList`
- `Set` → `HashSet`
- `Map` → `HashMap`
- `Queue` → `LinkedList`
- And more...

You don't need to configure anything special to use these.

## Detailed Examples

### Simple Interface

Let's start with a simple interface example:

```java
// The interface we want to generate
public interface StringSupplier {
	String getValue();
}

// A concrete implementation we might want to use
public class DefaultStringSupplier implements StringSupplier {
	private final String value;

	@ConstructorProperties("value") // It is not needed if you are using Lombok.
	public DefaultStringSupplier(String value) {
		this.value = value;
	}

	@Override
	public String getValue() {
		return "default" + value;
	}
}
```

#### Approach 1: Anonymous Implementation (No Options)

The simplest approach is to let Fixture Monkey generate an anonymous implementation:

```java
@Test
void testWithAnonymousImplementation() {
	// Setup
	FixtureMonkey fixture = FixtureMonkey.create();
	
	// Generate an anonymous implementation
	StringSupplier result = fixture.giveMeOne(StringSupplier.class);
	
	// Test
	assertThat(result.getValue()).isNotNull();
	assertThat(result).isNotInstanceOf(DefaultStringSupplier.class);
}
```

With this approach, Fixture Monkey creates an anonymous object that implements the `StringSupplier` interface. The `getValue()` method returns a randomly generated String.

:::tip[Important]
Fixture Monkey only generates property values for methods that:
- Follow the naming convention of getters (like `getValue()`, `getName()`, etc.)
- Have no parameters

Other methods will always return `null` or default primitive values.
:::

You can customize the generated properties using the same API as for regular classes:

```java
@Test
void testWithCustomizedProperties() {
	// Setup
	FixtureMonkey fixture = FixtureMonkey.create();
	
	// Generate with a specific property value
	StringSupplier result = fixture.giveMeBuilder(StringSupplier.class)
		.set("value", "customValue")
		.sample();
	
	// Test
	assertThat(result.getValue()).isEqualTo("customValue");
}
```

#### Approach 2: Using a Specific Implementation

When you need more realistic behavior, you can tell Fixture Monkey to use your concrete implementation:

```java
@Test
void testWithSpecificImplementation() {
	// Setup Fixture Monkey with a specific implementation
	FixtureMonkey fixture = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE) // needed for DefaultStringSupplier's constructor
		.plugin(
			new InterfacePlugin()
				.interfaceImplements(StringSupplier.class, List.of(DefaultStringSupplier.class))
		)
		.build();
	
	// Generate the interface
	StringSupplier result = fixture.giveMeOne(StringSupplier.class);
	
	// Test
	assertThat(result).isInstanceOf(DefaultStringSupplier.class);
	assertThat(result.getValue()).startsWith("default");
}
```

This approach generates a real `DefaultStringSupplier` instance with the behavior defined in your implementation.

### Generic Interface

For generic interfaces, the approach varies depending on whether you specify type parameters:

#### 1. Without Type Parameters

When you create a generic interface without specifying type parameters, Fixture Monkey defaults to using `String` type:

```java
// Generic interface
public interface ObjectValueSupplier<T> {
    T getValue();
}

@Test
void testGenericInterfaceWithoutTypeParameters() {
    FixtureMonkey fixture = FixtureMonkey.create();
    
    // Create without specifying type parameter
    ObjectValueSupplier<?> result = fixture.giveMeOne(ObjectValueSupplier.class);
    
    // String type is used by default
    assertThat(result.getValue()).isInstanceOf(String.class);
}
```

#### 2. With Explicit Type Parameters

You can explicitly specify the type parameter using TypeReference:

```java
@Test
void testGenericInterfaceWithTypeParameters() {
    FixtureMonkey fixture = FixtureMonkey.create();
    
    // Specify Integer as the type parameter
    ObjectValueSupplier<Integer> result = 
        fixture.giveMeOne(new TypeReference<ObjectValueSupplier<Integer>>() {});
    
    // Integer type is used
    assertThat(result.getValue()).isInstanceOf(Integer.class);
}
```

#### 3. Using a Specific Implementation

When using a specific implementation, it follows the type parameters of that implementation:

```java
// Concrete implementation for String
public class StringValueSupplier implements ObjectValueSupplier<String> {
    private final String value;

    @ConstructorProperties("value")
    public StringValueSupplier(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}

@Test
void testGenericInterfaceWithSpecificImplementation() {
    // Setup with specific implementation
    FixtureMonkey fixture = FixtureMonkey.builder()
        .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
        .plugin(
            new InterfacePlugin()
                .interfaceImplements(ObjectValueSupplier.class, List.of(StringValueSupplier.class))
        )
        .build();
    
    // Generate the interface
    ObjectValueSupplier<?> result = fixture.giveMeOne(ObjectValueSupplier.class);
    
    // Test
    assertThat(result).isInstanceOf(StringValueSupplier.class);
    assertThat(result.getValue()).isInstanceOf(String.class);
}
```

:::tip[Note]
When generating a generic interface without type parameters, Fixture Monkey uses `String` as the default type. If you need a different type, use `TypeReference` or specify a concrete implementation.
:::

### Sealed Interface (Java 17+)

Java 17 introduced sealed interfaces, which explicitly define their permitted implementations. Fixture Monkey automatically handles these without additional configuration:

```java
// Sealed interface with permitted implementations
sealed interface SealedStringSupplier {
	String getValue();
}

// Permitted implementation
public static final class SealedDefaultStringSupplier implements SealedStringSupplier {
	private final String value;

	@ConstructorProperties("value")
	public SealedDefaultStringSupplier(String value) {
		this.value = value;
	}

	@Override
	public String getValue() {
		return "sealed" + value;
	}
}

@Test
void testSealedInterface() {
	// Setup
	FixtureMonkey fixture = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.build();
	
	// Generate sealed interface
	SealedStringSupplier result = fixture.giveMeOne(SealedStringSupplier.class);
	
	// Test
	assertThat(result).isInstanceOf(SealedDefaultStringSupplier.class);
	assertThat(result.getValue()).startsWith("sealed");
}
```

## Combining with Other Interfaces

You can also specify which implementation to use for certain interfaces. For example, using `LinkedList` instead of the default `ArrayList` for `List`:

```java
@Test
void testCustomListImplementation() {
	// Setup
	FixtureMonkey fixture = FixtureMonkey.builder()
		.plugin(
			new InterfacePlugin()
				.interfaceImplements(List.class, List.of(LinkedList.class))
		)
		.build();
	
	// Generate
	List<String> list = fixture.giveMeOne(new TypeReference<List<String>>() {});
	
	// Test
	assertThat(list).isInstanceOf(LinkedList.class);
}
```

## Interface Inheritance

Fixture Monkey can also handle interface inheritance. You can specify implementations at any level of the hierarchy:

```java
interface ObjectValueSupplier {
	Object getValue();
}

interface StringValueSupplier extends ObjectValueSupplier {
	String getValue();
}

@Test
void testInterfaceHierarchy() {
	// Setup
	FixtureMonkey fixture = FixtureMonkey.builder()
		.plugin(
			new InterfacePlugin()
				.interfaceImplements(Collection.class, List.of(List.class))
		)
		.build();
	
	// Generate a Collection, which will use a List implementation
	Collection<String> collection = fixture.giveMeOne(new TypeReference<Collection<String>>() {});
	
	// Test
	assertThat(collection).isInstanceOf(List.class);
}
```

## Advanced Features

For more complex scenarios, Fixture Monkey provides advanced options for interface implementation resolution.

### Dynamic Implementation Resolution

If you have many implementations or need to select implementations based on type conditions:

```java
FixtureMonkey fixture = FixtureMonkey.builder()
	.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
	.plugin(
		new InterfacePlugin()
			.interfaceImplements(
				new AssignableTypeMatcher(ObjectValueSupplier.class),
				property -> {
					Class<?> actualType = Types.getActualType(property.getType());
					if (StringValueSupplier.class.isAssignableFrom(actualType)) {
						return List.of(PropertyUtils.toProperty(DefaultStringValueSupplier.class));
					}

					if (IntegerValueSupplier.class.isAssignableFrom(actualType)) {
						return List.of(PropertyUtils.toProperty(DefaultIntegerValueSupplier.class));
					}
					return List.of();
				}
			)
	)
	.build();
```

:::warning[For Advanced Users]
This section describes advanced features that most beginners won't need initially. Feel free to revisit this when you need more complex interface generation strategies.
:::

### Custom Resolution Implementation

For the most advanced scenarios, you can implement the `CandidateConcretePropertyResolver` interface:

```java
class YourCustomCandidateConcretePropertyResolver implements CandidateConcretePropertyResolver {
	@Override
	public List<Property> resolveCandidateConcreteProperties(Property property) {
		// Your custom logic to resolve implementations
		return List.of(...);
	}
}
```

You can use the built-in `ConcreteTypeCandidateConcretePropertyResolver` to help with type conversion:

```java
FixtureMonkey fixture = FixtureMonkey.builder()
	.plugin(new InterfacePlugin()
		.interfaceImplements(
			new ExactTypeMatcher(Collection.class),
			new ConcreteTypeCandidateConcretePropertyResolver<>(List.of(List.class, Set.class))
		)
	)
	.build();
```

:::tip[Important]
When setting type conditions for option application, be careful with matchers like `AssignableTypeMatcher`. Using it incorrectly can cause infinite recursion if implementations also match the condition.
:::

## Summary

Here's a quick summary of how to generate interface types with Fixture Monkey:

1. **Simple cases**: Just use `fixture.giveMeOne(YourInterface.class)` to get an anonymous implementation
   
2. **Specific implementation**: Use the `InterfacePlugin` with `interfaceImplements`:
   ```java
   new InterfacePlugin().interfaceImplements(YourInterface.class, List.of(YourImplementation.class))
   ```

3. **Built-in implementations**: Common interfaces like `List`, `Set`, etc. are handled automatically

4. **Sealed interfaces**: No special configuration needed - Fixture Monkey uses the permitted implementations

5. **Complex cases**: Use `AssignableTypeMatcher` or implement `CandidateConcretePropertyResolver` for advanced scenarios

Remember that for most testing scenarios, the simpler approaches will be sufficient. The advanced features are there when you need more control over the generated implementations.

