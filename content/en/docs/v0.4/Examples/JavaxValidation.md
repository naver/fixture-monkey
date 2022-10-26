---
title: "Apply Javax.validation annotations"
weight: 1
---
{{< alert color="primary" title="Tip">}}
This practice should add extra module because it depends on third-party library `Javax.validation`
{{< /alert >}}

## 1. Adding dependency
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

## 2. Adding `plugin` option

```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
    .plugin(new JavaxValidationPlugin())
    .build();
```
