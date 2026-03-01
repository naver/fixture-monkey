---
title: "Creating objects in Kotlin"
sidebar_position: 24
---


Fixture Monkey helps you create test objects for your Kotlin classes easily. For example, suppose you have a Kotlin data class:

```kotlin
data class Product (
    val id: Long,
    val productName: String,
    val price: Long,
    val options: List<String>,
    val createdAt: Instant,
    val productType: ProductType,
    val merchantInfo: Map<Int, String>
)
```

With Fixture Monkey, you can create test instances of this class with just one line of code:

```kotlin
val product: Product = fixtureMonkey.giveMeOne()
```

The generated object will contain random values that make sense for each field type. Here's an example of what you might get:

```kotlin
Product(
    id=42,
    productName="product-value-1",
    price=1000,
    options=["option1", "option2"],
    createdAt=2024-03-21T10:15:30Z,
    productType=ELECTRONICS,
    merchantInfo={1="merchant1", 2="merchant2"}
)
```

To start using Fixture Monkey with Kotlin, follow these steps:

1. Add the `fixture-monkey-starter-kotlin` dependency to your project.

2. Create a `FixtureMonkey` instance with the Kotlin plugin:
```kotlin
@Test
fun test() {
    val fixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .build()
}
```

The Kotlin plugin enables Fixture Monkey to work with Kotlin's features, using the primary constructor to create objects.

Here's a complete test example:

```kotlin
@Test
fun test() {
    // given
    val fixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .build()

    // when
    val actual: Product = fixtureMonkey.giveMeOne()

    // then
    then(actual).isNotNull
}
```

You can also customize the generated objects using Kotlin's property references:

```kotlin
@Test
fun test() {
    // given
    val fixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .build()

    // when
    val actual = fixtureMonkey.giveMeKotlinBuilder<Product>()
        .setExp(Product::id, 1000L)            // Set specific id
        .sizeExp(Product::options, 3)          // Set options list size
        .set("options[1]", "red")              // Set specific option
        .sample()

    // then
    then(actual.id).isEqualTo(1000L)
    then(actual.options).hasSize(3)
    then(actual.options[1]).isEqualTo("red")
}
```

For more Kotlin-specific features, check out the [Kotlin Plugin](../plugins/kotlin-plugin/features) documentation.

