---
title: "Apply JSR380: Bean Validation 2.0 annotations"
weight: 1
---
{{< alert color="primary" title="Tip">}}
This practice should add extra module because it depends on third-party library `javax.validation`
{{< /alert >}}

Creating an instance validated by [JSR380: Bean Validation 2.0](https://jcp.org/en/jsr/detail?id=380) annotations

## 1. Adding dependency
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

## 2. Adding `plugin` option

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new JavaxValidationPlugin())
    .build();
```
