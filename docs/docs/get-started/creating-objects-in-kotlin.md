---
title: "Creating objects in Kotlin"
sidebar_position: 24
---

import CodeSnippet from '@site/src/components/CodeSnippet';
import CreatingObjectsKotlinTest from '@examples-kotlin/getstarted/CreatingObjectsKotlinTest.kt';

Fixture Monkey helps you create test objects for your Kotlin classes easily. For example, suppose you have a Kotlin data class:

<CodeSnippet src={CreatingObjectsKotlinTest} language="kotlin" showClass />

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

<CodeSnippet src={CreatingObjectsKotlinTest} language="kotlin" method="giveMeOneUsage" />

The Kotlin plugin enables Fixture Monkey to work with Kotlin's features, using the primary constructor to create objects.

Here's a complete test example:

<CodeSnippet src={CreatingObjectsKotlinTest} language="kotlin" method="test" />

You can also customize the generated objects using Kotlin's property references:

<CodeSnippet src={CreatingObjectsKotlinTest} language="kotlin" method="customizeTest" />

For more Kotlin-specific features, check out the [Kotlin Plugin](../plugins/kotlin-plugin/features) documentation.

