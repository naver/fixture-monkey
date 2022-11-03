---
title: "fixture-monkey-javax-validation"
weight: 2
---

## Features
Generating object validated by javax.validation annotations

## How-to
### 1. Adding dependency
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-javax-validation:0.4.2")
```

```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-javax-validation</artifactId>
  <version>0.4.2</version>
  <scope>test</scope>
</dependency>
```

### 2. Adding option `plugin`
```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
    .plugin(new JavaxValidationPlugin())
    .build();
```
