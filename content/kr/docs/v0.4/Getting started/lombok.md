---
title: "Lombok을 사용하는 Java"
weight: 1
---

## @Value
### ConstructorPropertiesIntrospector
#### 0. 필요조건
둘 중 하나의 조건만 만족하면 됩니다.
* `lombok.config`에 `lombok.anyConstructor.addConstructorProperties=true` 옵션을 추가합니다.
* 생성자에 `@ConstructorProperties` 가 있습니다.

#### 1. 옵션 변경
`LabMonkeyBuilder`의 옵션 중 `objectIntrospector`를 변경합니다.

```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
    .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
    .build();
```

### JacksonArbitraryIntrospector
{{< alert color="primary" title="Tip">}}
서드파티 라이브러리 `Jackson`에 의존성이 있어 모듈 추가가 필요합니다.
{{< /alert >}}

#### 1. 의존성 추가
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

#### 2. 옵션 변경
`LabMonkeyBuilder` 의 옵션 중 `objectIntrospector`를 변경합니다.

##### ObjectMapper를 정의한 경우
```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
    .plugin(new JacksonPlugin(objectMapper))
    .objectIntrospector(new JacksonArbitraryIntrospector(objectMapper))
    .build();
```

##### ObjectMapper를 정의하지 않은 경우
```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
    .plugin(new JacksonPlugin())
    .objectIntrospector(JacksonArbitraryIntrospector.INSTANCE)
    .build();
```

## @Builder
### 1. 옵션 변경
`LabMonkeyBuilder` 의 옵션 중 `objectIntrospector`를 변경합니다.
```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
    .objectIntrospector(BuilderArbitraryIntrospector.INSTANCE)
    .build();
```


## @NoArgsConstructor + @Getter
### FieldReflectionArbitraryIntrospector
#### 1. 옵션 변경
`LabMonkeyBuilder` 의 옵션 중 `objectIntrospector`를 변경합니다.

```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
    .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
    .build();
```

### BeanArbitraryIntrospector
{{< alert color="primary" title="Tip">}}
기본으로 설정된 `objectIntrospector` 입니다.
{{< /alert >}}

## @NoArgsConstructor + @Setter
### 1. 옵션 변경
`LabMonkeyBuilder` 의 옵션 중 `objectIntrospector`를 변경합니다.

```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
	.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
	.build();
```
