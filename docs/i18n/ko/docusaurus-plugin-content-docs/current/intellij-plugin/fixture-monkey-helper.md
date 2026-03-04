---
title: "Fixture Monkey Helper"
sidebar_position: 91
---


[Fixture Monkey Helper](https://plugins.jetbrains.com/plugin/19589-fixture-monkey-helper)는 IntelliJ IDE에서 Fixture Monkey를 사용하는 데 도움을 주는 IntelliJ 플러그인입니다.

문자열 표현식과 Kotlin DSL 표현식 사용을 더 쉽게 만들어주는 기능을 제공하며, 비정상적인 코드를 감지하고 수정하는 IntelliJ 인스펙션도 추가합니다.

:::danger
이 플러그인은 현재 Java 소스 코드와 Kotlin 테스트 코드에서만 작동합니다. 확장 계획이 진행 중입니다.
:::

## 설치

1. IntelliJ IDEA를 엽니다
2. **Settings** > **Plugins** > **Marketplace**로 이동합니다
3. **"Fixture Monkey Helper"**를 검색합니다
4. **Install**을 클릭하고 IDE를 재시작합니다

## 기능

### 표현식 지원
- **자동 완성**: `set()`, `size()` 등에서 경로 표현식을 입력할 때 프로퍼티 이름에 대한 스마트 제안
- **표현식 유효성 검사**: 테스트를 실행하기 전에 유효하지 않은 경로 표현식을 하이라이트
- **내비게이션**: 문자열 표현식에서 프로퍼티 이름을 클릭하면 필드 선언으로 이동
- **문자열-Kotlin DSL 변환**: `"address.city"` 같은 문자열 표현식을 Kotlin DSL `Address::city`로 변환

### Kotlin DSL 향상
- **양방향 변환**: Kotlin DSL과 문자열 표현식 간 전환
  - 실시간 양방향 변환 지원 (Beta)
- **코드 접기**: DSL 표현식을 한 줄로 접어서 보기
- **람다 표현식 생성기**: 람다 표현식으로 픽스처 명세 생성
- **람다-DSL 변환**: 람다 표현식을 Fixture Monkey Kotlin DSL로 변환

### 인스펙션
- Fixture Monkey 팩토리 메서드에서 메서드 인수로 전달된 타입 정보를 제네릭 타입 인수로 변경
- 가능한 경우 Fixture Monkey 팩토리 메서드에서 제네릭 타입 인수를 변수 타입으로 변경

### 프로퍼티 개요 도구 창 (Alpha)
- ArbitraryBuilder에 등록된 모든 프로퍼티를 트리 형식으로 한눈에 확인
- **View** > **Tool Windows** > **Fixture Monkey Properties**에서 접근 가능

## 예제: 자동 완성

경로 표현식을 입력하면 플러그인이 사용 가능한 프로퍼티를 제안합니다:

```java
// "p"를 입력하면 자동 완성이 "price", "productName" 등을 제안합니다
fixtureMonkey.giveMeBuilder(Product.class)
    .set("p|", "value")  // <- 커서 위치에서 자동 완성 트리거
```

## 예제: 표현식 유효성 검사

유효하지 않은 표현식은 오류로 하이라이트됩니다:

```java
// "nonExistentField"가 오류로 하이라이트됩니다
fixtureMonkey.giveMeBuilder(Product.class)
    .set("nonExistentField", "value")  // <- 빨간 밑줄
```
