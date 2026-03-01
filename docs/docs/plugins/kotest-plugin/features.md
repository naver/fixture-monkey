---
title: "Features"
sidebar_position: 91
---


The Kotest plugin provided by Fixture Monkey allows you to enhance your testing experience within the Kotest framework.
- Replaces the default generator for generating random values for primitive types from Jqwik to Kotest's property generator (`Arb`). Use of bean validation annotations also works.
- Support for Kotest's property-based testing functions, including `forAll` and `checkAll`.

:::tip
Adding the Kotest Plugin doesn't mean you have to use Kotest as your testing framework. You can still use Junit.
:::

## Dependencies
##### fixture-monkey-kotlin
#### Gradle
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-kotest:1.1.15")
```

#### Maven
```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-kotest</artifactId>
  <version>1.1.15</version>
  <scope>test</scope>
</dependency>
```

## Plugin
```kotlin
val fixtureMonkey = FixtureMonkey.builder()
    .plugin(KotestPlugin())
    .plugin(KotlinPlugin())
    .build()
```

