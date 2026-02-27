---
title: "Altering the way of instantiating"
sidebar_position: 24
---


## 1. Altering instantiating
### BeanArbitraryIntrospector
#### Preconditions
1. No args constructor
2. Setter

### FieldReflectionArbitraryIntrospector
#### Preconditions
1. No args constructor

### BuilderArbitraryIntrospector
#### Preconditions
1. Builder

### ConstructorPropertiesIntrospector
#### Preconditions
Should satisfy one of below preconditions
* record type
* lombok `@Value`, enabled `lombok.anyConstructor.addConstructorProperties=true` option
* any constructors with `@ConstructorProperties`

:::tip[Tip]
Multiple constructors with record would be instantiated by constructor with `@ConstructorProperties`
:::

### FactoryMethodArbitraryIntrospector
#### Preconditions
* static factory method

### PrimaryConstructorArbitraryIntrospector
#### Preconditions
1. Kotlin class
2. Primary constructor.

### JacksonArbitraryIntrospector
:::tip[Tip]
This practice should add extra module because it depends on third-party library `Jackson`
:::

#### Preconditions
1. Adding `fixture-monkey-jackson` dependency
2. serializable / deserializable by Jackson

## 2. Altering `objectIntrospector` option
```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(selectedIntrospector)
    .build();
```

