---
title: "Features"
images: []
menu:
docs:
parent: "kotlin-plugin"
identifier: "kotlin-plugin-features"
weight: 10
---

To help you take full advantage of the concise, safe, and pragmatic nature of Kotlin, Fixture Monkey provides a Kotlin plugin.
- Using `PrimaryConstructorArbitraryIntrospector` as the default introspector to generate Kotlin classes with its primary constructor.
- Fixture Monkey Extension Functions
- Kotlin DSL Exp

## Dependencies
##### fixture-monkey-kotlin
#### Gradle
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-kotlin:0.6.2")
```

#### Maven
```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-kotlin</artifactId>
  <version>0.6.2</version>
  <scope>test</scope>
</dependency>
```

##### fixture-monkey-starter-kotlin

To help you get started using Fixture Monkey in a Kotlin environment, there is also a starter dependency **fixture-monkey-kotlin-starter** that comes with pre-configured dependencies such as lombok or fixture-monkey-jakarta-validation.

#### Gradle
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter-kotlin:0.6.2")
```

#### Maven
```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-starter-kotlin</artifactId>
  <version>0.6.2</version>
  <scope>test</scope>
</dependency>
```

## Plugin
{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new KotlinPlugin())
    .build();

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val fixtureMonkey = FixtureMonkey.builder()
    .plugin(KotlinPlugin())
    .build()

{{< /tab >}}
{{< /tabpane>}}
