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

### BuilderArbitraryIntrospector
#### 필요조건
1. 클래스가 빌더를 가지고 있습니다.

### ConstructorPropertiesIntrospector
#### 필요조건
타입이 셋 중 하나의 조건만 만족하면 됩니다.
* record 타입입니다.
* lombok `@Value`을 사용하고 `lombok.anyConstructor.addConstructorProperties=true` 옵션을 추가합니다.
* 생성자에 `@ConstructorProperties` 가 있습니다

{{< alert color="primary" title="Tip">}}
record에 생성자가 여러 개 일경우 `@ConstructorProperties`가 있는 생성자가 우선순위를 가집니다.
{{< /alert >}}

### FactoryMethodArbitraryIntrospector
#### 필요조건
* static한 팩토리 메서드가 있어야 합니다.

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
