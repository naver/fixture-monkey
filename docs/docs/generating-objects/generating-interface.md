---
title: "Generating Interface Types"
sidebar_position: 45
---

import CodeSnippet from '@site/src/components/CodeSnippet';
import GeneratingInterfaceTestJava from '@examples-java/generating/GeneratingInterfaceTest.java';

## Why Generate Interface Types?

When writing tests, you often need to work with interfaces rather than concrete implementations:
- You may be testing code that accepts interfaces as parameters
- Your system under test may return interface types
- You want to test behavior without coupling to specific implementations

Fixture Monkey makes it easy to generate test objects for interfaces - whether they're simple interfaces, generic interfaces, or sealed interfaces.

## Quick Start Example

Here's a simple example to get started with interface generation:

:::note
Anonymous interface generation requires the `InterfacePlugin` with `useAnonymousArbitraryIntrospector(true)` enabled.
:::

<CodeSnippet src={GeneratingInterfaceTestJava} language="java" method="quickStart" />

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

<CodeSnippet src={GeneratingInterfaceTestJava} language="java" showClass />

#### Approach 1: Anonymous Implementation (No Options)

The simplest approach is to let Fixture Monkey generate an anonymous implementation:

<CodeSnippet src={GeneratingInterfaceTestJava} language="java" method="testWithAnonymousImplementation" />

With this approach, Fixture Monkey creates an anonymous object that implements the `StringSupplier` interface. The `getValue()` method returns a randomly generated String.

:::tip[Important]
Fixture Monkey only generates property values for methods that:
- Follow the naming convention of getters (like `getValue()`, `getName()`, etc.)
- Have no parameters

Other methods will always return `null` or default primitive values.
:::

You can customize the generated properties using the same API as for regular classes:

<CodeSnippet src={GeneratingInterfaceTestJava} language="java" method="testWithCustomizedProperties" />

#### Approach 2: Using a Specific Implementation

When you need more realistic behavior, you can tell Fixture Monkey to use your concrete implementation:

<CodeSnippet src={GeneratingInterfaceTestJava} language="java" method="testWithSpecificImplementation" />

This approach generates a real `DefaultStringSupplier` instance with the behavior defined in your implementation.

### Generic Interface

For generic interfaces, the approach varies depending on whether you specify type parameters:

#### 1. Without Type Parameters

When you create a generic interface without specifying type parameters, Fixture Monkey defaults to using `String` type:

<CodeSnippet src={GeneratingInterfaceTestJava} language="java" method="testGenericInterfaceWithoutTypeParameters" />

#### 2. With Explicit Type Parameters

You can specify the type parameter using `TypeReference` along with a registered implementation:

<CodeSnippet src={GeneratingInterfaceTestJava} language="java" method="testGenericInterfaceWithTypeParameters" />

#### 3. Using a Specific Implementation

When using a specific implementation, it follows the type parameters of that implementation:

<CodeSnippet src={GeneratingInterfaceTestJava} language="java" method="testGenericInterfaceWithSpecificImplementation" />

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

You can also specify which implementation to use for certain interfaces. For example, adding `LinkedList` as an implementation for `List`:

<CodeSnippet src={GeneratingInterfaceTestJava} language="java" method="testCustomListImplementation" />

:::note
When you add a custom implementation using `interfaceImplements`, the default built-in implementation (e.g., `ArrayList` for `List`) may also remain as a candidate. Fixture Monkey can randomly select from all available candidates, including both the default and custom implementations.
:::

## Interface Inheritance

Fixture Monkey can also handle interface inheritance. You can specify implementations at any level of the hierarchy:

<CodeSnippet src={GeneratingInterfaceTestJava} language="java" method="testInterfaceHierarchy" />

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

