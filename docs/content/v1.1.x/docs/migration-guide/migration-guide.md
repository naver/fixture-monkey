---
title: "From 1.0.x"
weight: 11
menu:
docs:
  parent: "migration-guide"
  identifier: "1.0.x"
---

## Create an instance of the Kotlin type
In 1.0.x, when you apply the `KotlinPlugin`, Java and Kotlin types are created by the `PrimaryConstructorArbitraryIntrospector`, which uses the Kotlin primary constructor by default.
Creating a Java type with it causes the exception.

As of 1.1.x, when you apply the `KotlinPlugin`, Java types are created by the `BeanArbitraryIntrospector`, Kotlin types are created by the `PrimaryConstructorArbitraryIntrospector`.

## Different ArbitraryBuilder APIs between Java and Kotlin

In 1.0.x, Java and Kotlin use the same API in ArbitraryBuilder.

As of 1.1.x, Fixture Monkey provides both Java-specific ArbitraryBuilder APIs and Kotlin-specific ArbitraryBuilder APIs. Of course, you can use the
Java-specific APIs when creating a Kotlin type, and vice versa.

### How to use Java ArbitraryBuilder APIs
To use Java-specific APIs, use `FixtureMonkey.giveMeBuilder(Class)` or `FixtureMonkey.giveMeJavaBuilder(Class)`.

### How to use Kotlin ArbitraryBuilder APIs
To use Kotlin-specific APIs, use the extension function `FixtureMonkey.giveMeBuilder<Class>()`.

## Resolves the implementation of the abstract type

In 1.0.x, You must use the `ObjectPropertyGenerator` option in order to resolve the actual type of abstract class or
interface.

As of 1.1.x, all you have to do is use the `CandidateConcretePropertyResolver` option. It is much easier.

Let's take the sealed type as an example.
To create an instance of a sealed class in JDK 17, you have to use the `SealedTypeObjectPropertyGenerator`, which is used by default.
It forces you to know the properties of `ObjectProperty`, most of which are not your concern.

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

As of 1.1.x, you can only focus on the resolved implementation types.

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
