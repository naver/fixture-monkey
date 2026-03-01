---
title: "Java"
sidebar_position: 11
---


## Immutable type
### ConstructorPropertiesIntrospector
#### 0. Prerequisites
Should satisfy one of below preconditions
* record type
* any constructors with `@ConstructorProperties`

:::tip[Tip]
Multiple constructors with record would be instantiated by constructor with `@ConstructorProperties`
:::

#### 1. Altering `objectIntrospector` option

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
    .build();
```

### FactoryMethodArbitraryIntrospector
#### 0. Prerequisites
* static factory method

#### 1. Altering `objectIntrospector` option

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(FactoryMethodArbitraryIntrospector.INSTANCE)
    .build();
```

### JacksonArbitraryIntrospector
:::tip[Tip]
This practice should add extra module because it depends on third-party library [Jackson](https://github.com/FasterXML/jackson)
:::

#### 0. Prerequisites

Type should be serializable/deserializable by Jackson

#### 1. Adding dependency

```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-jackson:0.6.12")
```

```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-jackson</artifactId>
  <version>0.6.12</version>
  <scope>test</scope>
</dependency>
```

#### 2. Altering `objectIntrospector` option

##### If you have custom ObjectMapper
```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new JacksonPlugin(objectMapper))
    .build();
```

##### If you DON'T have a custom ObjectMapper
```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new JacksonPlugin())
    .build();
```

## Mutable type
### ConstructorPropertiesIntrospector
#### 0. Prerequisites
Should satisfy one of below preconditions
* record type
* any constructors with `@ConstructorProperties`

:::tip[Tip]
Multiple constructors with record would be instantiated by constructor with `@ConstructorProperties`
:::

#### 1. Altering `objectIntrospector` option

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
    .build();
```

### FieldReflectionArbitraryIntrospector
#### 0. Prerequisites
1. No args constructor
2. Getter / Setter

#### 1. Altering `objectIntrospector` option

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
    .build();
```

### BeanArbitraryIntrospector
:::tip[Tip]
`BeanArbitraryIntrospector` is default `objectIntrospector`
:::

#### 0. Prerequisites
1. No args constructor
2. Setter

