---
title: "Features"
sidebar_position: 91
---


The Kotest plugin provided by Fixture Monkey allows you to enhance your testing experience within the Kotest framework.
- Replaces the default generator for generating random values for primitive types from Jqwik to Kotest's property generator (`Arb`). Use of bean validation annotations also works.
- Support for Kotest's property-based testing functions, including `forAll` and `checkAll`.

:::tip
Adding the Kotest Plugin doesn't mean you have to use Kotest as your testing framework. You can still use JUnit.
:::

## Dependencies

#### Gradle
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-kotest:1.1.18")
```

#### Maven
```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-kotest</artifactId>
  <version>1.1.18</version>
  <scope>test</scope>
</dependency>
```

## Setup

```kotlin
val fixtureMonkey = FixtureMonkey.builder()
    .plugin(KotestPlugin())
    .plugin(KotlinPlugin())
    .build()
```

## Usage

### Basic Object Generation

```kotlin
data class Product(
    val id: Long,
    val name: String,
    val price: Int
)

val product: Product = fixtureMonkey.giveMeOne()
```

### Using with Kotest Property Testing

The Kotest plugin integrates with Kotest's property-based testing functions:

```kotlin
class ProductTest : StringSpec({
    val fixtureMonkey = FixtureMonkey.builder()
        .plugin(KotestPlugin())
        .plugin(KotlinPlugin())
        .build()

    "product name should not be blank after trim" {
        checkAll(fixtureMonkey.giveMeArb<Product>()) { product ->
            // property-based test with Fixture Monkey generated data
            product.id shouldNotBe null
        }
    }
})
```

### Using with Bean Validation

Bean validation annotations work with the Kotest plugin:

```kotlin
data class Order(
    @field:Min(1)
    val quantity: Int,

    @field:NotBlank
    val customerName: String,

    @field:Positive
    val price: Long
)

val fixtureMonkey = FixtureMonkey.builder()
    .plugin(KotestPlugin())
    .plugin(KotlinPlugin())
    .plugin(JakartaValidationPlugin())
    .build()

val order: Order = fixtureMonkey.giveMeOne()
// order.quantity >= 1, order.customerName is not blank, order.price > 0
```

## When to Use KotestPlugin vs JqwikPlugin

| Feature | KotestPlugin | JqwikPlugin (default) |
|---------|-------------|----------------------|
| Random value generator | Kotest `Arb` | Jqwik |
| Property testing integration | `checkAll`, `forAll` | Jqwik `@Property` |
| Recommended for | Kotest users | JUnit users |
| Bean validation support | Yes | Yes |
