---
title: "Features"
sidebar_position: 61
---

import CodeSnippet from '@site/src/components/CodeSnippet';
import KotlinPluginFeaturesTest from '@examples-kotlin/plugins/KotlinPluginFeaturesTest.kt';

The Kotlin plugin helps you take full advantage of Kotlin's concise, safe, and pragmatic nature when generating test fixtures.

### What the Kotlin Plugin provides

- **`PrimaryConstructorArbitraryIntrospector`** as the default introspector — generates Kotlin classes using their primary constructor
- **Extension functions** — `giveMeOne<T>()`, `giveMeKotlinBuilder<T>()` for idiomatic Kotlin usage
- **Kotlin DSL Exp** — type-safe property references (`KotlinClass::field`) instead of string-based paths
- **Kotlin `instantiateBy` DSL** — choose which constructor or factory method to use

## Dependencies

:::tip Recommended
Use **fixture-monkey-starter-kotlin** for new projects. It includes `fixture-monkey-starter` and `fixture-monkey-jakarta-validation` out of the box.
:::

#### Gradle
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter-kotlin:{{fixtureMonkeyVersion}}")
```

#### Maven
```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-starter-kotlin</artifactId>
  <version>{{fixtureMonkeyVersion}}</version>
  <scope>test</scope>
</dependency>
```

If you only need the core Kotlin support without the starter bundle:

```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-kotlin:{{fixtureMonkeyVersion}}")
```

## Plugin Setup

```kotlin
val fixtureMonkey = FixtureMonkey.builder()
    .plugin(KotlinPlugin())
    .build()
```

## Quick Example

<CodeSnippet src={KotlinPluginFeaturesTest} language="kotlin" method="quickExample" />

With customization using type-safe property references:

<CodeSnippet src={KotlinPluginFeaturesTest} language="kotlin" method="customizeWithTypeSafeReferences" />

## Extension Functions

The Kotlin plugin provides extension functions that leverage Kotlin's reified type parameters, so you don't need to pass `Class<T>` explicitly:

| Extension Function | Description |
|---|---|
| `giveMeOne<T>()` | Generate a single random instance |
| `giveMeBuilder<T>()` | Get an `ArbitraryBuilder<T>` (string-based API) |
| `giveMeKotlinBuilder<T>()` | Get a Kotlin-aware builder with `setExp`, `sizeExp` etc. |
| `giveMe<T>(count)` | Generate a stream of random instances |

```kotlin
// No need for Product::class.java
val product: Product = fixtureMonkey.giveMeOne()
val products: List<Product> = fixtureMonkey.giveMe<Product>(5).toList()
val builder = fixtureMonkey.giveMeKotlinBuilder<Product>()
```

## Type-Safe Property References (Kotlin DSL Exp)

Instead of string-based path expressions like `set("name", value)`, use Kotlin property references for compile-time safety:

```kotlin
// String-based (works but no compile-time check)
builder.set("name", "Fixture Monkey")

// Type-safe (compile error if property doesn't exist)
builder.setExp(Product::name, "Fixture Monkey")
```

For nested properties, use the `into` infix function:

<CodeSnippet src={KotlinPluginFeaturesTest} language="kotlin" method="nestedPropertyReference" />

For Java classes (getter-based), use `setExpGetter` and `intoGetter`:

```kotlin
builder.setExpGetter(JavaProduct::getName, "Fixture Monkey")
builder.setExpGetter(
    JavaOrder::getProduct intoGetter JavaProduct::getName,
    "Fixture Monkey"
)
```

See [Kotlin DSL Exp](./kotlin-exp) for the full reference.

## Introspectors for Kotlin

The Kotlin plugin sets `PrimaryConstructorArbitraryIntrospector` as the default, which creates objects using Kotlin's primary constructor.

For projects that mix Kotlin and Java classes, use `KotlinAndJavaCompositeArbitraryIntrospector`:

```kotlin
val fixtureMonkey = FixtureMonkey.builder()
    .plugin(KotlinPlugin())
    .objectIntrospector(KotlinAndJavaCompositeArbitraryIntrospector())
    .build()
```

See [Introspectors for Kotlin](./introspectors-for-kotlin) for details.
