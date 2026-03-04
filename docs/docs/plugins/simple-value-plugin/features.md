---
title: "Features"
sidebar_position: 51
---


By default, Fixture Monkey generates arbitrary values that cover a wide range of edge cases.
While this is powerful for thorough testing, it often produces values that are hard to read (e.g., very long strings with special characters, extreme numbers).

`SimpleValueJqwikPlugin` generates **readable, short, and realistic** values instead - perfect for beginners or when you need human-readable test data.

## Before and After

| Type | Default (without plugin) | With SimpleValueJqwikPlugin |
|------|--------------------------|----------------------------|
| String | `"嚤ǃ₯⚆..."` (random unicode, variable length) | `"aB3.d"` (short, readable) |
| Integer | `2147483647` or `-1938274` | `-10000` to `10000` |
| LocalDate | `+999999999-12-31` | Within last year to next year |
| List size | `0` to `30` | `0` to `3` |

## Setup

```kotlin
val fixtureMonkey = FixtureMonkey.builder()
    .plugin(SimpleValueJqwikPlugin())
    .build()
```

Compatible with validation plugins - validation annotations take priority:

```kotlin
val fixtureMonkey = FixtureMonkey.builder()
    .plugin(SimpleValueJqwikPlugin())
    .plugin(JakartaValidationPlugin())  // Validation annotations override SimpleValue defaults
    .build()
```

## Default Values

### String
Length 0 to 5, containing:
- Alphabet characters
- Numbers
- HTTP-safe special symbols: `.`, `-`, `_`, `~`

**Customization options**: `minStringLength`, `maxStringLength`, `characterPredicate`

### Number
Range: `-10000` to `10000` (both integer and decimal types)

**Customization options**: `minNumberValue`, `maxNumberValue`

### Date
Range: Last year to next year from today

**Customization options**: `minusDaysFromToday`, `plusDaysFromToday`

### Container
Applies to `List`, `Set`, `Iterator`, `Iterable`, `Map`, `Entry`.

Size range: `0` to `3`

**Customization options**: `minContainerSize`, `maxContainerSize`

## Customization Example

```kotlin
val fixtureMonkey = FixtureMonkey.builder()
    .plugin(
        SimpleValueJqwikPlugin()
            .minStringLength(3)
            .maxStringLength(10)
            .minNumberValue(-100)
            .maxNumberValue(100)
            .minContainerSize(1)
            .maxContainerSize(5)
    )
    .build()
```
