---
title: "Features"
sidebar_position: 51
---


Fixture Monkey generates an arbitrary value to avoid edge case that could cause the problem. 
It may be useful in some situations, but it is useless in most situations. 
Especially it is embarrassing for beginners, who expect the readable and valid data.

Fixture Monkey provides a new Plugin `SimpleValueJqwikPlugin` for beginners, who expect the readable and valid value. 
It generates a readable and short String and narrow range of Number and Date value.

It is compatible with other plugins such as `JavaxValidationPlugin`, `JakartaValidationPlugin`.
It applies `XXValidationPlugin` if the property has the validation annotation, applies `SimpleValueJqwikPlugin` without it.

If you use the custom Plugin to constrain the generated value, the latter plugin would work.

For beginners who want to restrict the generated value, looking at the code of `SimpleValueJqwikPlugin` is a good start.

## Default value
### String
The plugin generates a short size of limited String whose length is 0 to 5.

- alphabet
- number
- some special symbols allowed in HTTP query parameter `.`, `-`, `_`, `~`

It can be customised by the options below.
- minStringLength
- maxStringLength
- characterPredicate

### Number
The plugin generates a numeric number and decimal number in the range -10000 to 10000.

It can be customised, whether negative or positive, using the options below.

- minNumberValue
- maxNumberValue

### Date
The plugin generates a Date in the range of last year to next year from today.

It can be customised on a per date basis, using the options below.

- minusDaysFromToday
- plusDaysFromToday

### Container
The term of `Container` refers to the implementations of Collection such as `List`, `Set`, `Iterator` `Iterable`, and `Map`, `Entry`.
The plugin generates a Container whose size is in the range of 0 to 3. 

It can be customised by the options below.
- minContainerSize
- maxContainerSize

## Plugin
```kotlin
val fixtureMonkey = FixtureMonkey.builder()
    .plugin(SimpleValueJqwikPlugin())
    .build()
```

