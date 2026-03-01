---
title: "Creating objects in Kotlin"
sidebar_position: 26
---


Fixture Monkey also supports generating classes written in Kotlin code.

In order to do this, first make sure you added the `fixture-monkey-starter-kotlin` dependency.

Then we can add the Kotlin Plugin, to enable additional features of fixture monkey that support using Kotlin.

```kotlin
@Test
fun test() {
  val fixtureMonkey = FixtureMonkey.builder()
      .plugin(KotlinPlugin())
      .build()
}
```

The Kotlin plugin changes the default `ObjectIntrospector` to `PrimaryConstructorArbitraryIntrospector`,
which generates Kotlin classes with their primary constructor.

Suppose you have a Kotlin class like this:

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

You can generate your Kotlin class just like when you did in Java.

If you are writing your test code in Kotlin, one difference is that you can use `giveMeOne()` without writing the class name,
unlike java when you had to write `Product.class`.

The following code shows how to generate objects in kotlin:
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

The Kotlin plugin also lets you use a new way to reference the property to customize.

```kotlin
@Test
fun test() {
  // given
  val fixtureMonkey = FixtureMonkey.builder()
      .plugin(KotlinPlugin())
      .build();

  // when
  val actual = fixtureMonkey.giveMeBuilder<Product>()
      .setExp(Product::id, 1000L)
      .sizeExp(Product::options, 3)
      .setExp(Product::options[1], "red")
      .sample()

  // then
  then (actual.id).isEqualTo(1000L)
  then (actual.options).hasSize(3)
  then (actual.options[1]).isEqualTo("red")
}
```

Using the `setExp()` method instead of the `set()` method, you can specify property assignments using Kotlin's property reference syntax
The pages under [Kotlin Plugin](../plugins/kotlin-plugin/features) introduces more features that are provided by the Kotlin plugin.

