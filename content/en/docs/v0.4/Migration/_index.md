---
title: "Migrating from 0.3"
weight: 4
---
{{< alert color="primary" title="Abstract">}}
Migration guide for Fixture Monkey 0.3 to 0.4
{{< /alert >}}

## Options

### addAnnotatedArbitraryGenerator
```java
public FixtureOptionsBuilder addAnnotatedArbitraryGenerator(Class<?> clazz, AnnotatedArbitraryGenerator<?> generator) 
```

```java
public interface AnnotatedArbitraryGenerator<T> {
	Arbitrary<T> generate(AnnotationSource annotationSource);
}
```

Adding or altering new AnnotatedType used `addAnnotatedArbitraryGenerator` in 0.3

It is much complicated because there are two concerns, generating object, applying annotations.  

#### 0.4
There are two interfaces for generating object, applying annotations in 0.4

Option `JavaTypeArbitraryGenerator` supports generating default Java type.

Override methods for defining your custom default value.

```java
public LabMonkeyBuilder javaTypeArbitraryGenerator(
    JavaTypeArbitraryGenerator javaTypeArbitraryGenerator
)
```

```java
public interface JavaTypeArbitraryGenerator {
	default StringArbitrary strings() {
		return Arbitraries.strings();
	}
    ...
}
```

Option `JavaArbitraryResolver` supports applying annotations for default Java type. 

Override methods for defining how applying your custom annotations.

```java
public LabMonkeyBuilder javaArbitraryResolver(JavaArbitraryResolver javaArbitraryResolver)
```

```java
public interface JavaArbitraryResolver {
	default Arbitrary<String> strings(StringArbitrary stringArbitrary, ArbitraryGeneratorContext context) {
		return stringArbitrary;
	}
    ...
}
```


Option `JavaTimeTypeArbitraryGenerator` supports generating default Java time/date type.

Override methods for defining your custom default value.

```java
public LabMonkeyBuilder javaTimeTypeArbitraryGenerator(
		JavaTimeTypeArbitraryGenerator javaTimeTypeArbitraryGenerator
	)
```

```java
public interface JavaTimeTypeArbitraryGenerator {
	default CalendarArbitrary calendars() {
		Instant now = Instant.now();
		Calendar min = Calendar.getInstance();
		min.setTimeInMillis(now.minus(365, ChronoUnit.DAYS).toEpochMilli());
		Calendar max = Calendar.getInstance();
		max.setTimeInMillis(now.plus(365, ChronoUnit.DAYS).toEpochMilli());
		return Dates.datesAsCalendar()
			.between(min, max);
	}
    ...
}
```

Option `pushAssignableTypeArbitraryIntrospector` supports generating all other types.

```java
public LabMonkeyBuilder pushAssignableTypeArbitraryIntrospector(
		Class<?> type,
		ArbitraryIntrospector arbitraryIntrospector
) 
```

### defaultGenerator
```java
public FixtureMonkeyBuilder defaultGenerator(ArbitraryGenerator defaultCombiner)
```

Option `defaultGenerator` supports altering the way of instantiating in 0.3.

#### 0.4
For same usage, using interface `ArbitraryIntrospector` instead of `ArbitraryGenerator` in 0.4.
Option `objectIntrospector` supports same feature.

Option `objectIntrospector` decides the way of instantiating.

ex. `ConstructorPropertiesArbitraryIntrospector`, `BeanArbitraryIntrospector` 

```java
public LabMonkeyBuilder objectIntrospector(ArbitraryIntrospector objectIntrospector)
```

### putGenerator
Option `putGenerator` supports generating types without applying annotations in 0.3.

```java
public FixtureMonkeyBuilder putGenerator(Class<?> type, ArbitraryGenerator generator)
```

#### 0.4
Option `pushAssignableTypeArbitraryIntrospector` is same option in 0.4. 

```java
public LabMonkeyBuilder pushAssignableTypeArbitraryIntrospector(
	Class<?> type,
	ArbitraryIntrospector arbitraryIntrospector
)
```

### addInterfaceSupplier
```java
public <T> FixtureMonkeyBuilder addInterfaceSupplier(Class<T> clazz, InterfaceSupplier<T> interfaceSupplier)
```

Option `addIntrefaceSupplier` supports generating interface.

#### 0.4
Option `pushAssignableTypeArbitraryIntrospector` is same option in 0.4.

```java
public LabMonkeyBuilder pushAssignableTypeArbitraryIntrospector(
	Class<?> type,
	ArbitraryIntrospector arbitraryIntrospector
)
```

### null Option
#### nullableArbitraryEvaluator
```java
public FixtureOptionsBuilder nullableArbitraryEvaluator(NullableArbitraryEvaluator nullableArbitraryEvaluator)
```

```java
public interface NullableArbitraryEvaluator {
	default boolean isNullable(Field field) {
		return true;
	}
}
```

Option `nullableArbitraryEvaluator` is for some languages like Kotlin have some properties for determining nullable field.

```kotlin
class KotlinNullableArbitraryEvaluator : NullableArbitraryEvaluator {
    override fun isNullable(field: Field): Boolean {
        return field.kotlinProperty?.returnType?.isMarkedNullable ?: true
    }
}
```

#### nullInject
```java
public FixtureOptionsBuilder nullInject(double nullInject)
```

Setting nullInject probabilities. 

#### nullableContainer
```java
public FixtureOptionsBuilder nullableContainer(boolean nullableContainer)
```

Determines if container type would be nullable or not.

#### defaultNotNull
```java
public FixtureMonkeyBuilder defaultNotNull(boolean defaultNotNull)
```

Not nullInject even if marking as `@Nullable`.
Only set as null when using `setNull`.

#### 0.4
Interface `NullInjectGenerator` is used for all features about null in 0.4.
Option `defaultNullInjectGenerator` supports altering default `NullInjectGenerator`.

You could set all of 0.3 options mentioned above when instantiating DefaultNullInjectGenerator

```java
public GenerateOptionsBuilder defaultNullInjectGenerator(NullInjectGenerator defaultNullInjectGenerator)
```

```java
public final class DefaultNullInjectGenerator implements NullInjectGenerator {
	private final double defaultNullInject;
	private final boolean nullableContainer;
	private final boolean defaultNotNull;

	private final boolean nullableElement;
	private final Set<String> nullableAnnotationTypes;
	private final Set<String> notNullAnnotationTypes;
}
```


### Overview
#### Type generation
##### 0.3
* Three type options `AnnotatedType`, `Type`, `Interface`
* Instantiating and applying use same interface

##### 0.4
* Two type options `Java type`, `other types`
* Instantiating and applying use different interfaces

#### Null options
##### 0.3
* `nullableArbitraryEvaluator`, `nullInject`, `nullableContainer`, `defaultNotNull`

##### 0.4
* `defaultNullInjectGenerator`

## Plugin
```java
public LabMonkeyBuilder plugin(Plugin plugin) {
		generateOptionsBuilder.plugin(plugin);
		return this;
}
```

```java
public interface Plugin {
	void accept(GenerateOptionsBuilder optionsBuilder);
}
```

Interface `Plugin` is added for better use of third-party library.

Adding third-party dependency and adding option `plugin`.

`Plugin` overwrites same option, ordering is important.



