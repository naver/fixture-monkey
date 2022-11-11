---
title: "fixture-monkey-javax-validation"
weight: 2
---

## 기능
[JSR380: Bean Validation 2.0](https://jcp.org/en/jsr/detail?id=380) annotation을 활용해 객체의 값을 제어할 수 있도록 플러그인을 지원합니다.

## 설정
### 1. 의존성 추가
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-javax-validation:0.4.3")
```

```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-javax-validation</artifactId>
  <version>0.4.3</version>
  <scope>test</scope>
</dependency>
```

### 2. 옵션 변경
```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
    .plugin(new JavaxValidationPlugin())
    .build();
```
