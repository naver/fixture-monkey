---
title: "객체 생성 방식 변경"
weight: 4
---
## 1. 객체 생성 방식 선택
### BeanArbitraryIntrospector
#### 필요조건
1. 클래스가 파라미터가 없는 빈 생성자를 가지고 있습니다.
2. 클래스에 세터가 존재합니다.

### FieldReflectionArbitraryIntrospector
#### 필요조건
1. 클래스가 파라미터가 없는 빈 생성자를 가지고 있습니다.
2. 클래스에 세터/게터가 존재합니다.

### BuilderArbitraryIntrospector
#### 필요조건
1. 클래스가 빌더를 가지고 있습니다.

### PrimaryConstructorArbitraryIntrospector
#### 필요조건
1. 클래스가 코틀린 클래스 입니다.
2. Primary 생성자가 존재해야 합니다.

### JacksonArbitraryIntrospector
{{< alert color="primary" title="Tip">}}
서드파티 라이브러리 `Jackson`에 의존성이 있어 모듈 추가가 필요합니다.
{{< /alert >}}

#### 필요조건
1. `fixture-monkey-jackson` 의존성을 추가해야 합니다.
2. 객체가 Jackson으로 serialize/deserialize가 가능해야 합니다.

## 2. 옵션 변경
```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
    .objectIntrospector(selectedIntrospector)
    .build();
```
