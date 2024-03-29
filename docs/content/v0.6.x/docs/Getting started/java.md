---
title: "Java"
images: []
menu:
docs:
parent: "get-started"
identifier: "java"
weight: 11
---

## Immutable type
### ConstructorPropertiesIntrospector
#### 0. Prerequisites
Should satisfy one of below preconditions
* record type
* any constructors with `@ConstructorProperties`

{{< alert color="primary" title="Tip">}}
Multiple constructors with record would be instantiated by constructor with `@ConstructorProperties`
{{< /alert >}}

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
{{< alert color="primary" title="Tip">}}
This practice should add extra module because it depends on third-party library [Jackson](https://github.com/FasterXML/jackson)
{{< /alert >}}

#### 0. Prerequisites

Type should be serializable/deserializable by Jackson

#### 1. Adding dependency

```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-jackson:{{< param version >}}")
```

```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-jackson</artifactId>
  <version>{{< param version >}}</version>
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

{{< alert color="primary" title="Tip">}}
Multiple constructors with record would be instantiated by constructor with `@ConstructorProperties`
{{< /alert >}}

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
{{< alert color="primary" title="Tip">}}
`BeanArbitraryIntrospector` is default `objectIntrospector`
{{< /alert >}}

#### 0. Prerequisites
1. No args constructor
2. Setter
