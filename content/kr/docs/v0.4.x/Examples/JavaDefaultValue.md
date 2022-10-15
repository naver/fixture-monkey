---
title: "자바 객체 기본값 변경"
weight: 2
---
## 1. JavaTypeArbitraryGenerator 인터페이스 구현체 정의

변경하려는 타입 메소드를 override 합니다.

```java
public class CustomJavaTypeArbitraryGenerator implements JavaTypeArbitraryGenerator{
    @Override
    public StringArbitrary strings(){
        ...
    }
}
```

## 2. 옵션 변경
```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
    .plugin(new JavaxValidationPlugin())
    .javaTypeArbitraryGenerator(new CustomJavaTypeArbitraryGenerator())
    .build();
```
