---
title: "Java"
weight: 2
---

## 불변 객체
### JacksonArbitraryIntrospector
{{< alert color="primary" title="Tip">}}
서드파티 라이브러리 `Jackson`에 의존성이 있어 모듈 추가가 필요합니다.
{{< /alert >}}

#### 0. 필요조건

객체가 Jackson으로 serialize/deserialize가 가능해야 합니다.

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

## 가변 객체
### FieldReflectionArbitraryIntrospector
#### 0. 필요조건
빈 생성자와 Getter/Setter 중 하나가 있어야 합니다.

#### 1. 옵션 변경
`LabMonkeyBuilder`의 옵션 중 `objectIntrospector`를 변경합니다.

```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
    .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
    .build();
```

### BeanArbitraryIntrospector
{{< alert color="primary" title="Tip">}}
기본으로 설정된 `objectIntrospector` 입니다.
{{< /alert >}}

#### 0. 필요조건
빈 생성자와 Setter가 있어야 합니다.
