---
title: "Kotlin"
weight: 3
---
## PrimaryConstructorArbitraryIntrospector

### 1. 옵션 변경

`LabMonkeyBuilder` 의 옵션 중 `objectIntrospector`를 변경합니다.

```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
    .plugin(KotlinPlugin())
    .build();
```

## JacksonArbitraryIntrospector
{{< alert color="primary" title="Tip">}}
서드파티 라이브러리 [Jackson](https://github.com/FasterXML/jackson)에 의존성이 있어 모듈 추가가 필요합니다.
{{< /alert >}}

### 1. 의존성 추가

```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-jackson:0.4.3")
```

```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-jackson</artifactId>
  <version>0.4.3</version>
  <scope>test</scope>
</dependency>
```

### 2. 옵션 변경

`LabMonkeyBuilder` 의 옵션 중 `objectIntrospector`를 변경합니다.

#### ObjectMapper를 정의한 경우
```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
	.plugin(KotlinPlugin())
    .plugin(JacksonPlugin(objectMapper))
    .build();
```

#### ObjectMapper를 정의하지 않은 경우
```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
    .plugin(KotlinPlugin())
    .plugin(JacksonPlugin())
    .build();
```


