---
title: "fixture-monkey-javax-validation"
sidebar_position: 62
---


## Features
Generating an object validated by [JSR380: Bean Validation 2.0](https://jcp.org/en/jsr/detail?id=380) annotations

## How-to
### 1. Adding dependency
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-javax-validation:0.6.12")
```

```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-javax-validation</artifactId>
  <version>0.6.12</version>
  <scope>test</scope>
</dependency>
```

### 2. Adding option `plugin`
```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new JavaxValidationPlugin())
    .build();
```

