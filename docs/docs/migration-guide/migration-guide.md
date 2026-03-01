---
title: "From 1.0.x"
sidebar_position: 111
---


# Migrating from 1.0.x to 1.1.x

This guide helps you update your code from Fixture Monkey 1.0.x to 1.1.x. We've made several improvements to make the library easier to use while maintaining backward compatibility where possible.

## Major Changes at a Glance

1. Better handling of Kotlin types
2. Separate APIs for Java and Kotlin
3. Simpler way to handle abstract types and interfaces

## Kotlin Type Handling Improvements

### What Changed
- **Before (1.0.x)**: When using `KotlinPlugin`, both Java and Kotlin types were created using Kotlin's primary constructor approach. This caused errors when creating Java types.
- **Now (1.1.x)**: Each language uses its appropriate object creation strategy:
  - Java types → Created using bean properties (getters/setters)
  - Kotlin types → Created using Kotlin primary constructors

### What You Need to Do
No changes needed. Your Java types will now work correctly with the `KotlinPlugin`.

## Java and Kotlin Now Have Separate APIs

### What Changed
- **Before (1.0.x)**: Same ArbitraryBuilder API for both Java and Kotlin
- **Now (1.1.x)**: Specialized APIs for each language to provide a more natural experience

### Java API
Use one of these methods to get a Java-optimized builder:
```java
// Java style API
ArbitraryBuilder<User> userBuilder = fixtureMonkey.giveMeBuilder(User.class);
// or explicitly request Java builder
ArbitraryBuilder<User> userBuilder = fixtureMonkey.giveMeJavaBuilder(User.class);
```

### Kotlin API
Use Kotlin extension functions for a more idiomatic Kotlin experience:
```kotlin
// Kotlin style API with extension function
val userBuilder = fixtureMonkey.giveMeKotlinBuilder<User>()
```

> **Note**: You can still use Java APIs with Kotlin types and vice versa if needed.

## Simpler Way to Handle Abstract Types

### What Changed
- **Before (1.0.x)**: Required complex `ObjectPropertyGenerator` configuration to implement abstract types or interfaces
- **Now (1.1.x)**: Simpler `CandidateConcretePropertyResolver` lets you focus only on which implementations to use

### Example: Handling Sealed Classes

#### Before (1.0.x) - Complex Configuration
You needed to understand many details about `ObjectProperty`:

```java
public final class SealedTypeObjectPropertyGenerator implements ObjectPropertyGenerator {
	@Override
	public ObjectProperty generate(ObjectPropertyGeneratorContext context) {
		Property sealedTypeProperty = context.getProperty();
		double nullInject = context.getNullInjectGenerator().generate(context);
		Class<?> actualType = Types.getActualType(sealedTypeProperty.getType());
		Set<Class<?>> permittedSubclasses = collectPermittedSubclasses(actualType);

		Map<Property, List<Property>> childPropertiesByProperty =
			permittedSubclasses.stream()
				.collect(
					toUnmodifiableMap(
						Function.identity(),
						it -> context.getPropertyGenerator().generateChildProperties(it)
					)
				);

		return new ObjectProperty(
			sealedTypeProperty,
			context.getPropertyNameResolver(),
			nullInject,
			context.getElementIndex(),
			childPropertiesByProperty
		);
	}

	private static Set<Class<?>> collectPermittedSubclasses(Class<?> type) {
		Set<Class<?>> subclasses = new HashSet<>();
		doCollectPermittedSubclasses(type, subclasses);
		return subclasses;
	}

	private static void doCollectPermittedSubclasses(Class<?> type, Set<Class<?>> subclasses) {
		if (type.isSealed()) {
			for (Class<?> subclass : type.getPermittedSubclasses()) {
				doCollectPermittedSubclasses(subclass, subclasses);
			}
		} else {
			subclasses.add(type);
		}
	}
}
```

#### Now (1.1.x) - Simpler Approach
Just focus on which implementation classes to use:

```java
public final class SealedTypeCandidateConcretePropertyResolver implements CandidateConcretePropertyResolver {
	@Override
	public List<Property> resolve(Property property) {
		Class<?> actualType = Types.getActualType(property.getType());
		Set<Class<?>> permittedSubclasses = collectPermittedSubclasses(actualType);

		return permittedSubclasses.stream()
			.map(PropertyUtils::toProperty)
			.toList();
	}

	private static Set<Class<?>> collectPermittedSubclasses(Class<?> type) {
		Set<Class<?>> subclasses = new HashSet<>();
		doCollectPermittedSubclasses(type, subclasses);
		return subclasses;
	}

	private static void doCollectPermittedSubclasses(Class<?> type, Set<Class<?>> subclasses) {
		if (type.isSealed()) {
			for (Class<?> subclass : type.getPermittedSubclasses()) {
				doCollectPermittedSubclasses(subclass, subclasses);
			}
		} else {
			subclasses.add(type);
		}
	}
}
```

## Summary of Benefits

1. **Better Language Support**: Each language (Java/Kotlin) now uses its natural creation approach
2. **More Intuitive APIs**: Language-specific APIs that feel more natural to use
3. **Simpler Complex Type Handling**: Less boilerplate code when working with interfaces, abstract classes, and sealed types

These changes make Fixture Monkey 1.1.x easier to use while maintaining compatibility with most of your existing code.

