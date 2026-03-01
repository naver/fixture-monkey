---
title: "Java with lombok"
sidebar_position: 12
---


## @Value
### ConstructorPropertiesIntrospector
#### 0. Prerequisites
Should satisfy one of below preconditions
* add `lombok.anyConstructor.addConstructorProperties=true` in `lombok.config`
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

### JacksonArbitraryIntrospector
:::tip[Tip]
This practice should add extra module because it depends on third-party library [Jackson](https://github.com/FasterXML/jackson)
:::

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

## @Builder
### 1. Altering `objectIntrospector` option
```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(BuilderArbitraryIntrospector.INSTANCE)
    .build();
```


## @NoArgsConstructor
### FieldReflectionArbitraryIntrospector
#### 1. Altering `objectIntrospector` option

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
    .build();
```

## @NoArgsConstructor + @Setter
### BeanArbitraryIntrospector
:::tip[Tip]
`BeanArbitraryIntrospector` is default `objectIntrospector`
:::

### 1. Altering `objectIntrospector` option

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
	.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
	.build();
```

