---
title: "Introspectors for Kotlin"
images: []
menu:
docs:
parent: "kotlin-plugin"
identifier: "introspectors-for-kotlin"
weight: 62
---

Fixture Monkey provides some additional introspectors that support the generation of Kotlin classes.

## PrimaryConstructorArbitraryIntrospector

The `PrimaryConstructorArbitraryIntrospector` becomes the default introspector when the Kotlin plugin is added.
It creates a Kotlin class with its primary constructor.

**Example Kotlin Class :**
```kotlin
data class Product (
  val id: Long?,

  val productName: String,

  val price: Long,

  val options: List<String>,

  val createdAt: Instant
)
```

**Using PrimaryConstructorArbitraryIntrospector :**
```kotlin
@Test
fun test() {
  val fixtureMonkey = FixtureMonkey.builder()
      .plugin(KotlinPlugin())
      .build()

  val product: Product = fixtureMonkey.giveMeOne()
}
```
