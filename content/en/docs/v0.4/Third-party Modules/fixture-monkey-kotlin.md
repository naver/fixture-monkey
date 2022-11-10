---
title: "fixture-monkey-kotlin"
weight: 3
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
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-kotlin:0.4.3")
```

```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-kotlin</artifactId>
  <version>0.4.3</version>
  <scope>test</scope>
</dependency>
```

### 2. Adding option `plugin`
```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
    .plugin(KotlinPlugin())
    .build();
```
