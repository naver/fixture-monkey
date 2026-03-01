---
title: "경로 표현식"
sidebar_position: 41
---


## 이 문서에서 배우는 내용
- 테스트 객체의 특정 필드나 속성을 선택하는 방법
- 문자열 표현식을 사용하여 객체의 특정 부분을 참조하는 방법
- 중첩된 객체, 배열, 리스트 등 다양한 구조의 속성에 접근하는 방법

## 경로 표현식 소개

테스트를 작성할 때는 테스트 객체의 특정 필드를 수정해야 하는 경우가 많습니다. Fixture Monkey의 경로 표현식은 GPS 좌표와 같이 테스트 객체의 어떤 부분이든 정확하게 찾아 수정할 수 있게 도와주는 도구입니다.

초보자라면 경로 표현식을 객체 구조를 "탐색"하여 변경하고 싶은 필드에 정확히 도달하는 방법이라고 생각하면 됩니다.

## 기본 객체 구조 예제

경로 표현식을 이해하기 위해 간단한 예제 객체를 사용해보겠습니다:

```java
@Value
public class JavaClass {
    String field;                // 단순 문자열 필드
    String[] array;              // 문자열 배열
    List<String> list;           // 문자열 리스트
    Nested object;               // 중첩된 객체
    List<Nested> objectList;     // 중첩된 객체 리스트

    @Value
    public static class Nested {
        String nestedField;      // 중첩된 객체 내부의 필드
    }
}
```

## 경로 표현식의 시각적 지도

객체를 트리 구조로 생각해보세요. 각 경로 표현식은 그 트리에서 특정 위치로 가는 방향을 안내합니다:

```
JavaClass
│
├── field → "field"               // 직접 필드 접근
│
├── array → "array"               // 전체 배열
│   ├── array[0] → "array[0]"     // 배열의 첫 번째 요소
│   ├── array[1] → "array[1]"     // 배열의 두 번째 요소
│   └── 모든 요소 → "array[*]"     // 배열의 모든 요소 (와일드카드)
│
├── list → "list"                 // 전체 리스트
│   ├── list[0] → "list[0]"       // 리스트의 첫 번째 요소
│   ├── list[1] → "list[1]"       // 리스트의 두 번째 요소
│   └── 모든 요소 → "list[*]"      // 리스트의 모든 요소 (와일드카드)
│
├── object → "object"             // 중첩된 객체
│   └── nestedField → "object.nestedField"  // 중첩된 객체 내부의 필드
│
└── objectList → "objectList"     // 중첩된 객체 리스트
    ├── objectList[0] → "objectList[0]"  // 리스트의 첫 번째 객체
    │   └── nestedField → "objectList[0].nestedField"  // 첫 번째 객체의 필드
    │
    ├── objectList[1] → "objectList[1]"  // 리스트의 두 번째 객체
    │   └── nestedField → "objectList[1].nestedField"  // 두 번째 객체의 필드
    │
    └── 모든 요소 → "objectList[*]"   // 리스트의 모든 객체
        └── nestedField → "objectList[*].nestedField"  // 모든 객체의 필드
```

## 간단한 경로 표현식 가이드

### 1. 루트 객체 선택하기

전체 객체 자체를 선택하려면 다음을 사용합니다:
```java
"$"
```

**예제:**
```java
ArbitraryBuilder<JavaClass> builder = fixtureMonkey.giveMeBuilder(JavaClass.class);
// 전체 객체를 선택하고 조작
builder.set("$", new JavaClass(...));
```

### 2. 직접 필드 선택하기

최상위 레벨의 단순 필드를 선택하려면:
```java
"field"
```

**예제:**
```java
// "field" 속성을 "Hello World"로 설정
builder.set("field", "Hello World");
```

### 3. 중첩된 필드 선택하기

중첩된 객체 내부의 필드에 접근하려면:
```java
"object.nestedField"
```

**예제:**
```java
// object 내부의 nestedField를 "Nested Value"로 설정
builder.set("object.nestedField", "Nested Value");
```

### 4. 컬렉션 다루기

#### 리스트의 특정 항목 선택하기:
```java
"list[0]"  // 첫 번째 항목
"list[1]"  // 두 번째 항목
```

**예제:**
```java
// 리스트의 첫 번째 항목을 "First Item"으로 설정
builder.set("list[0]", "First Item");
```

#### 리스트의 모든 항목 선택하기 (와일드카드):
```java
"list[*]"
```

**예제:**
```java
// 리스트의 모든 항목을 "Same Value"로 설정
builder.set("list[*]", "Same Value");
```

### 5. 배열 다루기

리스트와 매우 유사합니다:

```java
"array[0]"   // 첫 번째 요소
"array[*]"   // 모든 요소
```

**예제:**
```java
// 배열의 모든 요소를 "Array Item"으로 설정
builder.set("array[*]", "Array Item");
```

### 6. 복잡한 중첩 경로

이러한 패턴을 조합하여 필요한 만큼 깊이 들어갈 수 있습니다:

```java
"objectList[0].nestedField"  // 리스트의 첫 번째 객체의 nestedField
"objectList[*].nestedField"  // 리스트의 모든 객체의 nestedField
```

**예제:**
```java
// objectList의 모든 객체의 nestedField를 "All Nested"로 설정
builder.set("objectList[*].nestedField", "All Nested");
```

## JavaGetter를 사용한 타입 안전 선택

문자열 기반 표현식을 사용하지 않으려면 타입 안전 getter를 사용할 수 있습니다:

### 1. 직접 필드 선택하기

```java
javaGetter(JavaClass::getField)
```

**예제:**
```java
builder.set(javaGetter(JavaClass::getField), "Hello World");
```

### 2. 중첩된 필드 선택하기

```java
javaGetter(JavaClass::getObject).into(Nested::getNestedField)
```

**예제:**
```java
builder.set(
    javaGetter(JavaClass::getObject).into(Nested::getNestedField), 
    "Nested Value"
);
```

### 3. 컬렉션 다루기

```java
// 특정 요소 선택
javaGetter(JavaClass::getList).index(String.class, 0)

// 모든 요소 선택
javaGetter(JavaClass::getList).allIndex(String.class)
```

**예제:**
```java
// 리스트의 모든 요소를 "List Item"으로 설정
builder.set(
    javaGetter(JavaClass::getList).allIndex(String.class), 
    "List Item"
);
```

## 초보자를 위한 자주 묻는 질문

### 범위를 벗어난 인덱스에 접근하면 어떻게 되나요?

존재하지 않는 요소에 접근하려고 하면(예: 리스트에 항목이 3개만 있는데 `"list[5]"`를 사용하는 경우), Fixture Monkey는 해당 설정을 무시합니다. 이러한 문제를 잡아내려면 [표현식 엄격 모드](#표현식-엄격-모드)를 활성화할 수 있습니다.

### 맵은 어떻게 처리하나요?

경로 표현식으로 맵 요소에 직접 접근할 수는 없지만, [InnerSpec](./innerspec)을 사용하여 맵을 커스터마이징할 수 있습니다.

### 여러 경로 표현식을 한 번에 사용할 수 있나요?

네! 여러 `.set()` 호출을 체이닝하여 객체의 다른 부분을 구성할 수 있습니다:

```java
ArbitraryBuilder<JavaClass> builder = fixtureMonkey.giveMeBuilder(JavaClass.class)
    .set("field", "Value 1")
    .set("object.nestedField", "Value 2")
    .set("list[*]", "Value 3");
```

## 고급 옵션

### 표현식 엄격 모드

이 [옵션](../fixture-monkey-options/advanced-options-for-experts#표현식-엄격-모드)을 활성화하여 Fixture Monkey가 모든 경로 표현식을 검증하도록 할 수 있습니다:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .setExpressionStrictMode(true)
    .build();
```

엄격 모드가 활성화되면 유효하지 않은 경로는 예외를 발생시켜 초기에 실수를 잡아낼 수 있습니다.

### Kotlin 지원

Kotlin을 사용하는 경우 더 우아한 표현식을 위해 속성 참조를 사용할 수 있습니다:

```kotlin
// 대신: "user.address.street"
builder.set(User::address..Address::street, "Main Street")
```

자세한 내용은 [Kotlin DSL Exp 페이지](../plugins/kotlin-plugin/kotlin-exp)를 참조하세요.

## 요약

경로 표현식은 Fixture Monkey의 강력한 기능으로 다음과 같은 작업을 수행할 수 있습니다:
- 테스트 객체 구조의 어떤 부분으로든 이동
- 다양한 테스트 시나리오를 위해 특정 값 설정
- 한 번의 작업으로 여러 관련 필드 수정
- 테스트 코드를 깔끔하고 읽기 쉽게 유지

직접 필드 접근부터 시작하여 구문에 익숙해지면서 점차 컬렉션 접근과 중첩된 속성으로 확장해 보세요.

