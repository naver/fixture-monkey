---
title: "Altering Java class default value"
sidebar_position: 22
---

## 1. Implementing JavaTypeArbitraryGenerator interface

Override specific type method to redefine default value

```java
public class CustomJavaTypeArbitraryGenerator implements JavaTypeArbitraryGenerator{
    @Override
    public StringArbitrary strings(){
        ...
    }
}
```

## 2. Altering `javaTypeArbitraryGenerator`  option
```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new JavaxValidationPlugin())
    .javaTypeArbitraryGenerator(new CustomJavaTypeArbitraryGenerator())
    .build();
```

