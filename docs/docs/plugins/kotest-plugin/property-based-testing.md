---
title: "Kotest Property Based Testing"
sidebar_position: 92
---

Fixture Monkey's Kotest plugin introduces two primary features that enhance [property-based testing within the Kotest framework](https://kotest.io/docs/proptest/property-test-functions.html): `forAll` and `checkAll`.

The KotestPlugin and KotlinPlugin should be added to enable this feature.
```kotlin
val fixtureMonkey: FixtureMonkey = FixtureMonkey.builder()
    .plugin(KotestPlugin())
    .plugin(KotlinPlugin())
    .build()
```

## ForAll
Kotest provides a `forAll` function that accepts an n-arity function `(a, ..., n) -> Boolean` to test a property.
The test passes if, for all input values, the function returns true.

This function accepts type parameters for the argument types, which Kotest uses to locate a generator that provides random values of a suitable type.

```kotlin
class PropertyExample: StringSpec({
   "String size" {
      forAll<String, String> { a, b ->
         (a + b).length == a.length + b.length
      }
   }
})
```

For cases when a custom generator is needed, it's possible to specify generators (called `Arb` in Kotest).
However, only generators of limited types are provided with kotest, and it is hard to customize.

Fixture Monkey offers a way to generate `Arb` for custom types using the `giveMeArb()` function.
You can further customize the generator using Fixture Monkey's customization APIs.

Here's an example of performing property-based testing with `forAll` using Fixture Monkey:

```kotlin
class KotestInKotestTest : StringSpec({
    "forAll" {
        forAll(fixtureMonkey.giveMeArb<StringObject> { it.set("value", "test") }) { a ->
            a.value == "test"
        }
    }
}) {
    data class StringObject(val value: String)
}
```

## CheckAll
Fixture Monkey also provides the extension function `checkAll` similar to Kotest's `checkAll`.

### Primitive Type Input
With checkAll, you can test assertions against primitive data types, as shown in the example below:
```kotlin
class Test : StringSpec({
    "checkAll" {
        SUT.checkAll { string: String, int: Int ->
            string shouldNotBeSameInstanceAs int
            string shouldBe string
        }
    }
})
```

### Custom Type Input
Fixture Monkey's `checkAll` extension function goes beyond primitive types.
You can also use custom types as input data, generated with Fixture Monkey.

```kotlin
class Test : StringSpec({
    "checkAllObject" {
        SUT.checkAll { stringObject: StringObject ->
            stringObject.value shouldNotBe null
        }
    }
}) {
    data class StringObject(val value: String)
}
```

### ArbitraryBuilder Input
Additionally, you can work with ArbitraryBuilder instances and further customize them to execute assertions.

```kotlin
class Test : StringSpec({
    "checkAllArbitraryBuilder" {
        SUT.checkAll { string: ArbitraryBuilder<List<String>> ->
            string
                .size("$", 3)
                .sample() shouldHaveSize 3
        }
    }
}) {
    data class StringObject(val value: String)
}
```

