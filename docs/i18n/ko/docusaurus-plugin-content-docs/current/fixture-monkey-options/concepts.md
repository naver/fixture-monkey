---
title: "개념"
sidebar_position: 52
---


이 문서는 Fixture Monkey의 옵션 시스템의 핵심 개념과 용어를 설명합니다. 이러한 개념을 이해하면 Fixture Monkey를 더 효과적으로 탐색하고 사용할 수 있습니다.

> 이러한 개념의 실제 구현에 대해서는 다음을 참조하세요:
> * [초보자를 위한 필수 옵션](./essential-options-for-beginners) - Fixture Monkey를 처음 사용한다면 여기서 시작하세요
> * [전문가를 위한 고급 옵션](./advanced-options-for-experts) - 더 복잡한 옵션 탐색하기

## 알아야 할 핵심 용어

상세한 개념을 살펴보기 전에, Fixture Monkey 전반에 걸쳐 사용되는 기본 용어를 이해해 봅시다:

| 용어 | 설명 |
|------|-------------|
| **인트로스펙션(Introspection)** | Fixture Monkey가 객체의 구조와 속성을 분석하는 방법 |
| **Arbitrary** | 특정 타입에 대해 임의의 값을 생성하는 생성기 |
| **컨테이너(Container)** | 컬렉션, 맵, 배열 등 여러 값을 담을 수 있는 구조 |
| **프로퍼티(Property)** | 객체의 값을 가질 수 있는 특성으로, 필드, getter/setter 메서드 또는 코틀린 프로퍼티로 구현될 수 있습니다. 프로퍼티는 타입, 어노테이션, 이름 등의 정보를 포함합니다. |
| **제약 조건(Constraint)** | 생성될 수 있는 값의 범위를 제한하는 규칙 (예: 최소/최대 값) |

Fixture Monkey는 `FixtureMonkeyBuilder`를 통해 구성할 수 있는 다양한 옵션을 제공합니다. 
이 페이지에서는 여러 옵션이 어떻게 함께 작동하는지 이해하는 데 도움이 되는 핵심 옵션 개념을 설명합니다.

## 옵션 아키텍처의 핵심 컴포넌트

이러한 핵심 컴포넌트를 이해하면 옵션을 더 효과적으로 탐색하는 데 도움이 됩니다:

### 1. 생성기와 인트로스펙터

객체를 생성하기 위해 함께 작동하는 주요 컴포넌트:

**생성기(Generator)**:
- 특정 특성을 가진 값을 생성하는 더 넓은 범위의 역할을 담당
- 값 생성의 "무엇"(고유한 값, 패턴, 범위 등)에 초점을 맞춤
- 예시: `UniqueArbitraryGenerator`
- **쉽게 말하자면**: 생성기는 테스트 데이터가 어떤 모양을 가질지 결정하는 쿠키 커터와 같습니다

**인트로스펙터(Introspector)**:
- 객체 생성의 구체적인 방법을 지정
- 객체 인스턴스화의 "어떻게"(생성자, 팩토리 등을 통해)에 초점을 맞춤
- 예시: `ConstructorArbitraryIntrospector`, `FieldReflectionArbitraryIntrospector`
- **쉽게 말하자면**: 인트로스펙터는 사용 가능한 재료를 사용하여 실제로 객체를 만드는 방법을 알아내는 제빵사와 같습니다

초보자라면 생성기는 "어떤 종류의 데이터"를 생성할지 정의하고, 인트로스펙터는 "실제로 객체를 어떻게 구성"할지 결정한다고 생각할 수 있습니다.

**실용적인 예**: 유효한 신용카드 번호로 결제 시스템을 테스트해야 할 때, 생성기는 올바른 형식의 번호를 생성하는 데 도움을 주고, 인트로스펙터는 생성자, 빌더 패턴 또는 팩토리 메서드를 통해 이를 생성할지 결정합니다.

> 생성기에 대한 구현 세부 사항은 다음을 참조하세요:
> * [타입 구성](./essential-options-for-beginners#타입-구성) (필수 옵션)
> * [커스텀 타입 등록 및 생성](./advanced-options-for-experts#커스텀-타입-등록-및-생성) (고급 옵션)

### 2. 빌더 패턴과 옵션 체이닝

Fixture Monkey는 설정을 위해 빌더 패턴을 사용합니다. 옵션 적용 순서를 이해하는 것이 중요합니다:

```java
FixtureMonkey monkey = FixtureMonkey.builder()
    .plugin(new KotlinPlugin())           // 첫 번째로 적용
    .nullableContainer(true)              // 두 번째로 적용
    .defaultArbitraryContainerSize(3, 5)  // 세 번째로 적용
    .build();
```

옵션은 정의된 순서대로 적용됩니다. 동일한 설정을 대상으로 하는 경우 나중에 정의된 옵션이 이전 옵션을 재정의할 수 있습니다.

**실용적인 예**: 애플리케이션이 다양한 제품 목록을 처리하는 방식을 테스트해야 하는 경우, 기본 컨테이너 크기를 설정하고 특정 테스트 케이스에서는 경계 조건을 테스트하기 위해 이를 재정의할 수 있습니다.

### 3. 옵션 스코핑

옵션은 다양한 스코프나 수준에 적용될 수 있습니다:

1. **전역 스코프(Global Scope)**:
   - Fixture Monkey 인스턴스에 의해 생성된 모든 객체에 적용
   - `FixtureMonkeyBuilder`에서 설정
   - **사용 사례 예**: 테스트 전체에서 null 값을 방지하기 위해 `defaultNotNull(true)` 설정

2. **타입 스코프(Type Scope)**:
   - 특정 타입이나 인터페이스에 적용
   - 예시: `.pushAssignableTypePropertyGenerator(String.class, generator)`
   - **사용 사례 예**: 모든 String 값이 이메일 주소와 같은 특정 패턴을 따르도록 구성

3. **경로 표현식 스코프(Path Expression Scope)**:
   - 경로 표현식으로 식별된 특정 프로퍼티에 적용
   - 예시: `.register(String.class, fixtureMonkey -> fixtureMonkey.giveMeBuilder(String.class).set("$", Arbitraries.strings().ofMinLength(3)))`
   - **사용 사례 예**: "price"와 같은 특정 속성이 항상 양수가 되도록 설정

더 구체적인 스코프가 더 일반적인 스코프를 재정의합니다 - 웹 개발에 익숙하다면 CSS 명시성이 작동하는 방식과 유사합니다.

> 옵션 스코핑의 실제 사용에 대해서는 다음을 참조하세요:
> * [일반 빌더 옵션](./essential-options-for-beginners#일반-빌더-옵션) (필수 옵션)
> * [프로퍼티 커스터마이징](./advanced-options-for-experts#프로퍼티-커스터마이징) (고급 옵션)

### 4. 타입 등록 시스템

Fixture Monkey의 타입 등록 시스템은 특정 타입에 대한 값을 생성하는 방법을 결정합니다:

```java
FixtureMonkey monkey = FixtureMonkey.builder()
    .register(String.class, fixtureMonkey -> fixtureMonkey.giveMeBuilder(String.class)
        .set("$", Arbitraries.strings().ofMinLength(5).ofMaxLength(10)))
    .register(Integer.class, fixtureMonkey -> fixtureMonkey.giveMeBuilder(Integer.class)
        .set("$", Arbitraries.integers().between(1, 100)))
    .build();
```

이는 특정 타입에 대한 커스텀 생성기를 등록하며, 이 타입을 포함하는 모든 객체에서 사용될 수 있습니다.

**실용적인 예**: 애플리케이션에서 모든 사용자 ID가 1에서 100 사이여야 하는 경우, 이 규칙을 한 번 등록하면 모든 테스트 객체가 이를 따르게 됩니다.

> 타입 등록 구현에 대한 자세한 내용은 다음을 참조하세요:
> * [타입 구성](./essential-options-for-beginners#타입-구성) (필수 옵션)
> * [커스텀 타입 등록 및 생성](./advanced-options-for-experts#커스텀-타입-등록-및-생성) (고급 옵션)

### 5. 플러그인 시스템

플러그인은 일반적인 사용 사례에 대한 사전 구성된 설정을 제공합니다:

```java
FixtureMonkey monkey = FixtureMonkey.builder()
    .plugin(new KotlinPlugin())
    .plugin(new JacksonPlugin())
    .build();
```

플러그인은 다음을 수행할 수 있습니다:
- 타입별 생성기 및 인트로스펙터 등록
- 기본 동작 구성
- 서드파티 라이브러리 지원 추가

**쉽게 말하자면**: 플러그인은 일반적인 시나리오에 대한 사전 구성된 설정이 포함된 요리책과 같습니다.

**실용적인 예**: Kotlin 프로젝트를 사용하는 경우, KotlinPlugin은 Kotlin 클래스와 잘 작동하도록 모든 것을 자동으로 구성하여 많은 수동 구성을 작성할 필요가 없습니다.

> 플러그인 사용 예제는 다음을 참조하세요:
> * [JqwikPlugin 옵션](./essential-options-for-beginners#jqwikplugin-옵션) (필수 옵션)

## 프로퍼티와 컨테이너

Fixture Monkey는 객체 작업 시 단순히 `필드` 대신 더 넓은 개념인 `프로퍼티`를 사용합니다. 이러한 유연성을 통해 Fixture Monkey는 다양한 프로그래밍 패러다임과 프레임워크와 함께 작동할 수 있습니다.

### Fixture Monkey의 프로퍼티

Fixture Monkey에서 `프로퍼티`는 다음을 포함합니다:
- 해당 `Type`(String, Integer, 커스텀 클래스 등)
- 적용된 모든 `Annotation`
- 그것의 `name`

Fixture Monkey는 두 가지 주요 프로퍼티 유형을 구분합니다:

#### 1. ObjectProperty
다음을 포함하는 일반 객체 프로퍼티에 대한 정보를 나타냅니다:
- 프로퍼티 자체
- 프로퍼티 이름이 어떻게 해석되는지
- 컬렉션의 일부인 경우 해당 인덱스

**코드 사용 예시**:
```java
// ObjectProperty 실제 사용 - 특정 프로퍼티에 값 설정
Product product = fixtureMonkey.giveMeBuilder(Product.class)
    .set("name", "스마트폰")      // ObjectProperty: name
    .set("price", BigDecimal.valueOf(599.99))  // ObjectProperty: price
    .sample();
```

#### 2. ContainerProperty
다음에 대한 정보를 포함하는 컬렉션을 나타냅니다:
- 요소 프로퍼티 목록
- 컨테이너 크기에 대한 정보

**코드 사용 예시**:
```java
// ContainerProperty 실제 사용 - 컬렉션 작업
List<Product> products = fixtureMonkey.giveMeBuilder(new TypeReference<List<Product>>() {})
    .size(3)  // 컨테이너 크기 설정
    .set("$[0].name", "첫 번째 상품")  // 첫 번째 요소의 프로퍼티 접근
    .sample();
```

### 컨테이너 타입 vs. 객체 타입

Fixture Monkey는 컨테이너 타입(`List`, `Set`, `Map` 등)을 일반 객체 타입과 다르게 처리합니다. 이 구분은 컬렉션이 포함된 테스트 시나리오에서 중요합니다.

**테스트에서 왜 중요한가**: 이 구분을 이해하면 컬렉션 크기, 요소 프로퍼티 및 고유성 제약 조건을 올바르게 구성하는 데 도움이 됩니다.

**실제 예시**:
```java
// 여러 상품이 있는 쇼핑 카트 테스트
ShoppingCart cart = fixtureMonkey.giveMeBuilder(ShoppingCart.class)
    .size("items", 3)  // ContainerProperty: 카트에 3개 상품
    .set("items[0].productName", "특별 상품")  // 첫 번째 상품에 특정 이름 지정
    .set("customer.address.country", "대한민국")  // 중첩 경로가 있는 ObjectProperty
    .sample();
```

> 프로퍼티 처리에 대한 더 자세한 내용은 다음을 참조하세요:
> * [커스텀 객체 인트로스펙션](./essential-options-for-beginners#사용자-정의-객체-인트로스펙션) (필수 옵션)
> * [객체 프로퍼티 생성기](./advanced-options-for-experts#커스텀-객체-프로퍼티-생성기-추가하기) (고급 옵션)
> * [컨테이너 처리 옵션](./advanced-options-for-experts#컨테이너-생성-최적화) (고급 옵션)

## 옵션 상호작용 방식

Fixture Monkey가 객체를 생성할 때:

1. 먼저 객체에 사용할 인트로스펙터를 결정합니다
2. 인트로스펙터가 객체 구조를 분석합니다
3. 각 프로퍼티에 대해 적절한 생성기를 선택합니다
4. 프로퍼티 생성기는 구성에 따라 값을 생성합니다
5. 인트로스펙터가 최종 객체를 조립합니다

더 구체적인 스코프의 옵션이 더 일반적인 스코프의 옵션보다 우선합니다.

**실용적인 예**: 모든 숫자가 양수여야 한다는 전역 규칙을 설정했지만 특정 "할인" 필드는 음수여야 한다고 지정한 경우, 할인 필드에 대한 특정 규칙이 전역 규칙을 재정의합니다.

## 시각적 설명

다음은 이러한 컴포넌트가 상호작용하는 방식을 간략하게 보여줍니다:

```
┌─────────────────────────────────────────┐
│           FixtureMonkeyBuilder          │
└───────────────┬─────────────────────────┘
                │
                ▼
┌───────────────────────────────────┐    ┌───────────────────┐
│          옵션 해결                 │◄───┤     플러그인      │
└───────────────┬───────────────────┘    └───────────────────┘
                │
                ▼
┌───────────────────────────────────┐
│       타입/프로퍼티 선택           │
└───────────────┬───────────────────┘
                │
                ▼
┌──────────────┐      ┌──────────────┐
│ 인트로스펙터  │◄─────┤   생성기     │
└──────────┬───┘      └──────────────┘
           │
           ▼
┌───────────────────────────────────┐
│           생성된 객체              │
└───────────────────────────────────┘
```

## 예시: 옵션 상호작용

여러 옵션이 상호작용하는 더 구체적인 예를 살펴보겠습니다:

```java
FixtureMonkey monkey = FixtureMonkey.builder()
    .plugin(new JavaTimePlugin())                              // 전역 플러그인
    .defaultArbitraryContainerSize(1, 5)                       // 전역 컨테이너 크기
    .nullableContainer(true)                                   // 전역 컨테이너 null 가능성
    .register(String.class, fixtureMonkey -> fixtureMonkey.giveMeBuilder(String.class)
        .set("$", Arbitraries.strings().ofMinLength(3)))      // 타입별 구성
    .register(
        new MatcherOperator<>(
            property -> property.getName().equals("price"),    // "price"라는 이름의 모든 속성과 일치
            fixtureMonkey -> fixtureMonkey.giveMeBuilder(BigDecimal.class)
                .set("$", Arbitraries.bigDecimals().greaterOrEqual(BigDecimal.ZERO))
        )
    )                                                          // 간단한 속성 이름 매처
    .build();
```

객체를 생성할 때:
1. JavaTimePlugin은 날짜/시간 타입을 구성합니다
2. 컨테이너는 1-5개의 요소를 가지며 null일 수 있습니다
3. 모든 문자열 값은 최소 길이가 3입니다
4. "price"라는 이름의 모든 속성은 항상 음수가 아닙니다

**실제 시나리오**: 이 구성은 다음과 같은 이커머스 애플리케이션 테스트에 유용할 것입니다:
- 다양한 제품 수(1-5개 항목)로 테스트해야 함
- 제품 설명은 최소 3자 이상이어야 함
- 가격은 항상 0 이상이어야 함

> 복잡한 구성의 예시는 [실제 고급 구성](./advanced-options-for-experts#실제-고급-구성-예시) (고급 옵션)을 참조하세요.

## 결론

이러한 개념을 이해하면 다음과 같은 도움이 됩니다:
- 특정 테스트 요구사항에 맞게 Fixture Monkey를 더 효과적으로 구성
- 생성기가 예상대로 작동하지 않을 때 문제 해결
- 정밀한 제어로 복잡한 테스트 데이터 생성

프로퍼티, 컨테이너 및 옵션 시스템을 효과적으로 활용하면 프로덕션 요구사항에 더 가깝게 일치하는 더 현실적이고 대상이 명확한 테스트 데이터를 생성할 수 있습니다.

## 다음 단계

이러한 개념을 이해한 후에는 다음을 할 수 있습니다:

→ [초보자를 위한 필수 옵션](./essential-options-for-beginners) - 이러한 개념의 실제 구현 방법 학습하기

→ [전문가를 위한 고급 옵션](./advanced-options-for-experts) - 복잡한 테스트 시나리오를 위한 고급 옵션 탐색하기

→ [커스텀 인트로스펙터 만들기](../generating-objects/custom-introspector) - 특별한 도메인 요구사항을 위한 자체 인트로스펙터 구현하기

