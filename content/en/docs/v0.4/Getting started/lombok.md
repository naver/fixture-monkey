---
title: "Java with lombok"
weight: 1
---

## @Value
### ConstructorPropertiesIntrospector
#### 0. Prerequisites
Should satisfy one of below preconditions
* add `lombok.anyConstructor.addConstructorProperties=true` in `lombok.config`
* any constructors with `@ConstructorProperties`

{{< alert color="primary" title="Tip">}}
Multiple constructors with record would be instantiated by constructor with `@ConstructorProperties`
{{< /alert >}}

#### 1. Altering `objectIntrospector` option

```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
    .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
    .build();
```

### JacksonArbitraryIntrospector
{{< alert color="primary" title="Tip">}}
This practice should add extra module because it depends on third-party library `Jackson`
{{< /alert >}}

#### 1. Adding dependency
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-jackson:0.4.2")
```

```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-jackson</artifactId>
  <version>0.4.2</version>
  <scope>test</scope>
</dependency>
```

#### 2. Altering `objectIntrospector` option

##### If you have custom ObjectMapper
```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
    .plugin(new JacksonPlugin(objectMapper))
    .objectIntrospector(new JacksonArbitraryIntrospector(objectMapper))
    .build();
```

##### If you DON'T have a custom ObjectMapper
```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
	.plugin(new JacksonPlugin())
	.objectIntrospector(JacksonArbitraryIntrospector.INSTANCE)
	.build();
```

## @Builder
### 1. Altering `objectIntrospector` option
```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
    .objectIntrospector(BuilderArbitraryIntrospector.INSTANCE)
    .build();
```


## @NoArgsConstructor + @Getter
### FieldReflectionArbitraryIntrospector
#### 1. Altering `objectIntrospector` option

```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
    .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
    .build();
```

### BeanArbitraryIntrospector
{{< alert color="primary" title="Tip">}}
`BeanArbitraryIntrospector` is default `objectIntrospector`
{{< /alert >}}

## @NoArgsConstructor + @Setter
### 1. Altering `objectIntrospector` option

```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
	.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
	.build();
```
