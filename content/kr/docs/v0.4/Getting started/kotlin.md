---
title: "Kotlin"
weight: 3
---
## Jackson을 사용 가능한 경우
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
	.plugin(KotlinPlugin())
    .plugin(JacksonPlugin(objectMapper))
    .objectIntrospector(JacksonArbitraryIntrospector(objectMapper))
    .build();
```

##### ObjectMapper를 정의하지 않은 경우
```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
    .plugin(KotlinPlugin())
    .plugin(JacksonPlugin())
    .objectIntrospector(JacksonArbitraryIntrospector.INSTANCE)
    .build();
```

## Jackson을 사용하지 못하는 경우
### PrimaryConstructorArbitraryIntrospector

#### 1. 옵션 변경

`LabMonkeyBuilder` 의 옵션 중 `objectIntrospector`를 변경합니다.

```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
    .plugin(KotlinPlugin())
    .objectIntrospector(PrimaryConstructorArbitraryIntrospector.INSTANCE)
    .build();
```

