---
title: "Features"
images: []
menu:
docs:
parent: "kotest-plugin"
identifier: "kotest-plugin-features"
weight: 91
---

The Kotest plugin provided by Fixture Monkey allows you to enhance your testing experience within the Kotest framework.
- Replaces the default generator for generating random values for primitive types from Jqwik to Kotest's property generator (`Arb`). Use of bean validation annotations also works.
- Support for Kotest's property-based testing functions, including `forAll` and `checkAll`.

{{< alert icon="💡" text="Adding the Kotest Plugin doesn't mean you have to use Kotest as your testing framework. You can still use Junit." />}}

## Dependencies
##### fixture-monkey-kotlin
#### Gradle
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-kotest:{{< fixture-monkey-version >}}")
```

#### Maven
```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-kotest</artifactId>
  <version>{{< fixture-monkey-version >}}</version>
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
