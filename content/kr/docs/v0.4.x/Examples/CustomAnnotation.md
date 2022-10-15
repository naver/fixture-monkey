---
title: "자바 객체 커스텀 어노테이션 추가"
weight: 3
---
## 1. JavaArbitraryResolver 인터페이스 구현체 정의

변경하려는 타입 메소드를 override 합니다.

```java
public class CustomJavaArbitraryResolver implements JavaArbitraryResolver{
    @Override
    public Arbitrary<String> strings(StringArbitrary stringArbitrary, ArbitraryGeneratorContext context) {
        ...
	}
}
```

### 참조할만한 구현체 
* JavaxValidationJavaArbitraryResolver

## 2. 옵션 변경
```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
    .plugin(new JavaxValidationPlugin())
    .javaArbitraryResolver(new CustomJavaArbitraryResolver())
    .build();
```
