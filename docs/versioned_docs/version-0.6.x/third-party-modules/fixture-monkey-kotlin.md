---
title: "fixture-monkey-kotlin"
sidebar_position: 63
---


## Features
- Using objectIntrospector `PrimaryConstructorArbitraryGenerator`
- Extensions, Kotlin DSL Exp [see](https://github.com/naver/fixture-monkey/blob/main/fixture-monkey-kotlin/src/main/kotlin/com/navercorp/fixturemonkey/kotlin/FixtureMonkeyExtensions.kt)

### Exp
Kotlin DSL for generating type-safe expression.

* Manipulation name has a suffix `exp` or `expGetter`
* Using method reference.
* `into`, `intoGetter` is used for referencing a field in the field.
* `[index]`, `["*"]` is used for referencing an element in a container. 

|              | manipulation name | nested field | element        | 
|--------------|-------------------|--------------|----------------|
| Java Class   | expGetter         | intoGetter   | [index], ["*"] |
| Kotlin Class | exp               | into         | [index], ["*"] |

## How-to
### 1. Adding dependency
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-kotlin:0.6.12")
```

```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-kotlin</artifactId>
  <version>0.6.12</version>
  <scope>test</scope>
</dependency>
```

### 2. Adding option `plugin`
```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(KotlinPlugin())
    .build();
```

