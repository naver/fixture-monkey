---
title: "fixture-monkey-javax-validation"
weight: 2
---

## Features
Generating an object validated by [JSR380: Bean Validation 2.0](https://jcp.org/en/jsr/detail?id=380) annotations

## How-to
### 1. Adding dependency
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-javax-validation:0.4.14")
```

```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-javax-validation</artifactId>
  <version>0.4.14</version>
  <scope>test</scope>
</dependency>
```

### 2. Adding option `plugin`
```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
    .plugin(new JavaxValidationPlugin())
    .build();
```
