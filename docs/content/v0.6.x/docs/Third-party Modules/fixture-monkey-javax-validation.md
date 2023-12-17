---
title: "fixture-monkey-javax-validation"
weight: 62
---

## Features
Generating an object validated by [JSR380: Bean Validation 2.0](https://jcp.org/en/jsr/detail?id=380) annotations

## How-to
### 1. Adding dependency
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-javax-validation:{{< param version >}}")
```

```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-javax-validation</artifactId>
  <version>{{< param version >}}</version>
  <scope>test</scope>
</dependency>
```

### 2. Adding option `plugin`
```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new JavaxValidationPlugin())
    .build();
```
