---
title: "fixture-monkey-jakarta-validation"
weight: 8
---

## Features
Generating an object validated by [JSR380: Bean Validation 2.0](https://jcp.org/en/jsr/detail?id=380) annotations

## How-to
### 1. Adding dependency
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-jakarta-validation:{{< param version >}}")
```

```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-jakarta-validation</artifactId>
  <version>{{< param version >}}</version>
  <scope>test</scope>
</dependency>
```

### 2. Adding option `plugin`
```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
    .plugin(new JakartaValidationPlugin())
    .build();
```
