---
title: "fixture-monkey-jakarta-validation"
weight: 8
---

## 기능
[Jakarta Bean Validation 3.0](https://jakarta.ee/specifications/bean-validation/3.0/jakarta-bean-validation-spec-3.0.html) annotation을 활용해 객체의 값을 제어할 수 있도록 플러그인을 지원합니다.

## 설정
### 1. 의존성 추가
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-jakarta-validation:0.4.14")
```

```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-jakarta-validation</artifactId>
  <version>0.4.14</version>
  <scope>test</scope>
</dependency>
```

### 2. 옵션 변경
```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
    .plugin(new JakartaValidationPlugin())
    .build();
```
