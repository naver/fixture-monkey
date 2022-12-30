---
title: "JSR380: Bean Validation 2.0 어노테이션 적용"
weight: 1
---
{{< alert color="primary" title="Tip">}}
서드파티 라이브러리 `javax.validation`에 의존성이 있어 모듈 추가가 필요합니다.
{{< /alert >}}

[JSR380: Bean Validation 2.0](https://jcp.org/en/jsr/detail?id=380) 어노테이션을 만족하는 객체를 생성합니다.

## 1. 의존성 추가
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-javax-validation:{{< param version >}}")
```

```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-javax-validation</artifactId>
  <version>{{< param version >}}</version>
  <scope>test</scope>
</dependency>
```

## 2. 플러그인 추가
`LabMonkeyBuilder` 의 옵션 `plugin`을 추가합니다.

```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
    .plugin(new JavaxValidationPlugin())
    .build();
```
