---
title: "From 1.1.x"
sidebar_position: 111
---

# Migrating from 1.1.x to 1.2.x

In 1.2.0 the legacy object tree engine (the default since 0.4.0) was **removed**, and the adapter engine introduced as experimental in 1.1.17 became the **only and default** object generation engine — wired automatically by `FixtureMonkeyBuilder`.

## Do I need to change anything?

**For most users, no.** If you use `FixtureMonkey.builder()`, `giveMe*`, and `.set()` / `.size()` / `.thenApply()`, your code keeps working unchanged:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder().build(); // adapter engine, automatically
```

You need to make changes only if you:
- registered `JavaNodeTreeAdapterPlugin` / `KotlinNodeTreeAdapterPlugin` explicitly,
- configured a tracer, or
- implemented a custom `Property`, `PropertyGenerator`, `ArbitraryIntrospector`, or `object-farm-api` type.

## Remove the adapter plugin

### What Changed
- **Before (1.1.x)**: the node tree adapter was an opt-in plugin.
- **Now (1.2.x)**: it is the default engine; the plugin classes were removed.

### What You Need to Do
Delete the plugin registration — nothing replaces it.

```java
// Before (1.1.x)
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new JavaNodeTreeAdapterPlugin())
    .build();

// Now (1.2.x)
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .build();
```

For Kotlin, drop `KotlinNodeTreeAdapterPlugin()` (keep `KotlinPlugin()`).

## Tracing moved to the builder

### What Changed
- **Before (1.1.x)**: `AdapterTracer` was configured on the plugin.
- **Now (1.2.x)**: use `AssemblyTracer` on the builder via `FixtureMonkeyBuilder#tracer(...)`.

### What You Need to Do
```java
// Before (1.1.x)
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new JavaNodeTreeAdapterPlugin()
        .tracer(AdapterTracer.console()))
    .build();

// Now (1.2.x)
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .tracer(AssemblyTracer.console())
    .build();
```

`FixtureMonkeyOptionsBuilder#adapterTracer(...)` / `#nodeTreeAdapter(...)` were removed. See the [Tracing guide](../debugging/tracing) for the full tracer reference.

## Seed behavior

### What Changed
- `@Seed` now takes precedence over `FixtureMonkeyBuilder#seed(long)`. While a `@Seed` (or `FixtureMonkeySeedExtension`) is active, the builder's own seed is ignored.
- The seed is scoped to the test and released afterwards — even if the test or another extension throws — so it no longer leaks into later tests.
- Container sizes are now reproducible across reruns with the same seed.

### What You Need to Do
Nothing to change, but note that because iteration order and seed handling changed, the random stream for a fixed seed may differ from 1.1.x. Tests that pin values from a specific seed may need to be re-baselined. See [Reproducible generation](../debugging/reproducible-generation).

## Custom extension points

If you implement `Property` (or a custom `PropertyGenerator` / `ArbitraryIntrospector`), the type-system surface changed. Use this mapping:

| Before (1.1.x) | Now (1.2.x) |
|----------------|-------------|
| `Property#getType()` | `Property#getJvmType()` |
| `Property#getAnnotatedType()` | `Types.toAnnotatedType(property.getJvmType())` |
| `Property#getValue(Object)` | removed — value extraction is no longer part of `Property` |
| `ArbitraryGeneratorContext#getResolvedType()` (`Type`) | `getResolvedJvmType()` (now returns `Class<?>`, deprecated) |
| `ArbitraryGeneratorContext#getResolvedAnnotatedType()` | `getResolvedJvmType()` (deprecated) |
| `PropertyUtils#toProperty(AnnotatedType)` | `PropertyUtils#toProperty(JvmType)` |
| `ContainerPropertyGenerator.DEFAULT_ELEMENT_RAW_TYPE` | `DEFAULT_ELEMENT_JVM_TYPE` |
| `JavaType(...)` (object-farm-api) | `ReflectiveJvmType(...)` |
| `JvmType#getAnnotatedType()` | `getRawType()` + `getAnnotations()` |

Built-in `Property` implementations (`FieldProperty`, `ConstructorProperty`, `RootProperty`, `MapEntryElementProperty`, …) also have new constructor signatures accordingly.

To install a custom engine for a JVM language, use the new `JvmTypeSystemPlugin` SPI:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new MyJvmTypeSystemPlugin()) // configures a custom AssemblyPlanner
    .build();
```

## Summary

- Most users: no changes required.
- Remove `JavaNodeTreeAdapterPlugin` / `KotlinNodeTreeAdapterPlugin`; the engine is now the default.
- Move tracing to `FixtureMonkeyBuilder#tracer(AssemblyTracer...)`.
- Custom `Property` / plugin authors: migrate to `JvmType` and the new `JvmTypeSystemPlugin` SPI.
