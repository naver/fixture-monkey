---
title: "Features"
sidebar_position: 61
---


To help you take full advantage of the concise, safe, and pragmatic nature of Kotlin, Fixture Monkey provides a Kotlin plugin.
- Using `PrimaryConstructorArbitraryIntrospector` as the default introspector to generate Kotlin classes with its primary constructor.
- Fixture Monkey Extension Functions
- Kotlin DSL Exp, Kotlin instantiateBy DSL

## Dependencies
##### fixture-monkey-kotlin
#### Gradle
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-kotlin:1.0.20")
```

#### Maven
```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-kotlin</artifactId>
  <version>1.0.20</version>
  <scope>test</scope>
</dependency>
```

##### fixture-monkey-starter-kotlin

To help you get started using Fixture Monkey in a Kotlin environment, there is also a starter dependency **fixture-monkey-starter-kotlin** that comes with pre-configured dependencies such as **fixture-monkey-starter** or **fixture-monkey-jakarta-validation**.

#### Gradle
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter-kotlin:1.0.20")
```

#### Maven
```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-starter-kotlin</artifactId>
  <version>1.0.20</version>
  <scope>test</scope>
</dependency>
```

## Plugin
```kotlin
val fixtureMonkey = FixtureMonkey.builder()
    .plugin(KotlinPlugin())
    .build()
```

