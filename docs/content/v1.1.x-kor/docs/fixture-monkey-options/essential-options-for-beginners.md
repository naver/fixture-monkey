---
title: "초보자를 위한 필수 옵션"
images: []
menu:
docs:
parent: "fixture-monkey-options"
identifier: "essential-options-for-beginners"
weight: 51
---

Fixture Monkey를 처음 시작할 때, 몇 가지 핵심 옵션만 이해하면 복잡함에 압도되지 않고 필요한 테스트 데이터를 생성할 수 있습니다. 이 가이드는 초보자를 위한 가장 필수적인 옵션에 초점을 맞춥니다.

## 일반 빌더 옵션

이러한 옵션은 `FixtureMonkey` 인스턴스를 생성할 때 설정합니다:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    // 여기에 옵션을 추가합니다
    .build();
```

### 기본 NotNull 설정

기본적으로 Fixture Monkey는 일부 필드에 `null` 값을 생성할 수 있습니다. 모든 필드가 null이 아닌 값을 갖도록 하려면:

```java
.defaultNotNull(true)
```

이 옵션은 테스트에서 `NullPointerException`을 방지하는 데 도움이 되므로 시작할 때 가장 유용한 옵션 중 하나입니다.

### Nullable 컨테이너

컬렉션 타입(`List`, `Set`, `Map` 등)이 null이 될 수 있는지 제어합니다:

```java
.nullableContainer(false)  // 컬렉션은 절대 null이 되지 않습니다
```

### 기본 리스트 크기

생성된 리스트의 기본 크기 범위를 설정합니다:

```java
.defaultListSize(1, 5)  // 리스트는 1~5개의 요소를 갖게 됩니다
```

### 문자열 길이

생성된 문자열의 길이를 제어합니다:

```java
.defaultStringLength(5, 10)  // 문자열은 5~10자 사이가 됩니다
```

## 타입별 구성

특정 타입이 생성되는 방식을 구성할 수 있습니다:

```java
// String 값이 생성되는 방식 구성
.register(String.class, fixtureMonkey -> fixtureMonkey.giveMeBuilder(String.class)
    .set("$", "기본 문자열")  // 기본값 설정
)

// Integer 값이 생성되는 방식 구성
.register(Integer.class, fixtureMonkey -> fixtureMonkey.giveMeBuilder(Integer.class)
    .set("$", it -> it >= 1 && it <= 100)  // 1에서 100 사이의 조건 설정
)

// BigDecimal 값이 생성되는 방식 구성
.register(BigDecimal.class, fixtureMonkey -> fixtureMonkey.giveMeBuilder(BigDecimal.class)
    .set("$", new BigDecimal("10.00"))  // 기본값 설정
)

// List 값이 생성되는 방식 구성
.register(List.class, fixtureMonkey -> fixtureMonkey.giveMeBuilder(List.class)
    .size("$", 1, 5)  // 리스트 크기 1-5로 설정
)
```

## 속성 표현식 옵션

특정 객체를 생성할 때 개별 속성을 커스터마이징할 수 있습니다:

```java
Product product = fixtureMonkey.giveMeBuilder(Product.class)
    .set("name", "인체공학 의자")  // 정확한 값 설정
    .set("price", (BigDecimal p) -> p.compareTo(BigDecimal.TEN) > 0)  // 조건으로 설정
    .set("id", fixtureMonkey.giveMeBuilder(Long.class)
        .set("$", it -> it >= 1000L && it <= 9999L)
        .sample())  // 다른 빌더로 설정
    .set("tags[0]", "가구")  // 배열/리스트 요소 설정
    .sample();
```

## 상황별 옵션 사용법

- **빠른 테스트 데이터가 필요하고 특정 값이 중요하지 않은 경우:**
  - `defaultNotNull(true)`를 사용한 기본 빌더 활용

- **현실적인 값이 필요한 경우(예: 데모용):**
  - 각 타입에 대한 커스텀 생성기 등록
  - 숫자 필드에 최소/최대값 설정
  - 문자열 길이 적절하게 구성

- **경계 조건을 테스트해야 하는 경우:**
  - 중요한 속성에 대해 `.set()`을 사용하여 특정 값이나 제약 조건 설정
  - 복잡한 조건은 술어(predicate) 사용

- **관련 데이터를 생성하려는 경우:**
  - 중첩된 `giveMeBuilder` 호출을 사용하여 관련 객체 생성

## 다음 단계

이제 필수 옵션을 이해했으니 다음을 배울 수 있습니다:

→ [옵션 개념 이해하기](../concepts) - 옵션이 어떻게 작동하는지 더 깊이 이해하기

→ [속성 표현식을 사용하여 값 지정하기](../../generate-objects/property-expression) - 속성을 지정하는 더 고급 방법 배우기

→ [JavaBean 유효성 검사 사용하기](../../plugins/junit-plugin) - 유효성 검사 어노테이션이 데이터 생성을 안내하도록 하기
