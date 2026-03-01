---
title: "Adding Java class custom annotations"
sidebar_position: 23
---

## 1. Implementing JavaArbitraryResolver interface

Override specific type method to define how annotation works 

```java
public class CustomJavaArbitraryResolver implements JavaArbitraryResolver{
    @Override
    public Arbitrary<String> strings(StringArbitrary stringArbitrary, ArbitraryGeneratorContext context) {
        ...
	}
}
```

### Concrete Class
* JavaxValidationJavaArbitraryResolver

## 2. Altering `javaArbitraryResolver` option
```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new JavaxValidationPlugin())
    .javaArbitraryResolver(new CustomJavaArbitraryResolver())
    .build();
```

