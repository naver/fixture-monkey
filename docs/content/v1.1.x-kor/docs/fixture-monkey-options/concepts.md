---
title: "개념"
images: []
menu:
docs:
parent: "fixture-monkey-options"
identifier: "concepts"
weight: 54
---

Fixture Monkey는 `FixtureMonkeyBuilder`를 통해 설정할 수 있는 다양한 옵션을 제공합니다. 
이 페이지에서는 옵션들이 어떻게 함께 작동하는지 이해하는 데 도움이 되는 핵심 옵션 개념을 설명합니다.

> **옵션 문서 탐색 가이드**:
> * Fixture Monkey를 처음 접하신다면, [초보자를 위한 필수 옵션](../essential-options-for-beginners)부터 시작하세요.
> * 복잡한 테스트 시나리오를 위해서는 [전문가를 위한 고급 옵션](../advanced-options-for-experts)을 참조하세요.
> * 나머지 옵션들의 전체 목록은 [기타 옵션](../other-options)을 확인하세요.

## 옵션 아키텍처의 핵심 컴포넌트

이러한 핵심 컴포넌트를 이해하면 옵션을 더 효과적으로 탐색하는 데 도움이 됩니다:

### 1. 생성기와 인트로스펙터

객체를 생성하기 위해 함께 작동하는 주요 컴포넌트:

**생성기(Generator)**:
- 특정 프로퍼티에 대한 임의의 값을 생성
- 예시: `StringArbitraryGenerator`, `IntegerArbitraryGenerator`

**인트로스펙터(Introspector)**:
- 인스턴스를 구축하는 방법을 결정하기 위해 객체 구조를 분석
- 예시: `ConstructorPropertiesArbitraryIntrospector`, `FieldReflectionArbitraryIntrospector`

초보자라면 생성기를 "무엇"(어떤 값을 생성할지)으로, 인트로스펙터를 "어떻게"(어떻게 객체를 생성할지)로 생각할 수 있습니다.

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

### 3. 옵션 스코핑

옵션은 다양한 스코프에 적용될 수 있습니다:

1. **전역 스코프(Global Scope)**:
   - Fixture Monkey 인스턴스에 의해 생성된 모든 객체에 적용
   - `FixtureMonkeyBuilder`에서 설정

2. **타입 스코프(Type Scope)**:
   - 특정 타입이나 인터페이스에 적용
   - 예시: `.pushAssignableTypePropertyGenerator(String.class, generator)`

3. **표현식 스코프(Expression Scope)**:
   - 표현식으로 식별된 특정 프로퍼티에 적용
   - 예시: `.register(String.class, fixture -> fixture.minSize(5))`

더 구체적인 스코프가 더 일반적인 스코프를 재정의합니다.

### 4. 타입 등록 시스템

Fixture Monkey의 타입 등록 시스템은 특정 타입에 대한 값을 생성하는 방법을 결정합니다:

```java
FixtureMonkey monkey = FixtureMonkey.builder()
    .register(String.class, fixture -> fixture.minSize(5).maxSize(10))
    .register(Integer.class, fixture -> fixture.range(1, 100))
    .build();
```

이는 특정 타입에 대한 커스텀 생성기를 등록하며, 이 타입을 포함하는 모든 객체에서 사용될 수 있습니다.

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

## 옵션 상호작용 방식

Fixture Monkey가 객체를 생성할 때:

1. 먼저 객체에 사용할 인트로스펙터를 결정합니다
2. 인트로스펙터가 객체 구조를 분석합니다
3. 각 프로퍼티에 대해 적절한 생성기를 선택합니다
4. 프로퍼티 생성기는 구성에 따라 값을 생성합니다
5. 인트로스펙터가 최종 객체를 조립합니다

더 구체적인 스코프의 옵션이 더 일반적인 스코프의 옵션보다 우선합니다.

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

여러 옵션이 상호작용하는 다음 예시를 살펴보세요:

```java
FixtureMonkey monkey = FixtureMonkey.builder()
    .plugin(new JavaTimePlugin())                              // 전역 플러그인
    .defaultArbitraryContainerSize(1, 5)                       // 전역 컨테이너 크기
    .nullableContainer(true)                                   // 전역 컨테이너 null 가능성
    .register(String.class, fixture -> fixture.minSize(3))     // 타입별 구성
    .register(
        ExpressionMatcher.exactPath("order.items.price"), 
        fixture -> fixture.greaterThanOrEqualTo(BigDecimal.ZERO)
    )                                                          // 표현식별 구성
    .build();
```

객체를 생성할 때:
1. JavaTimePlugin은 날짜/시간 타입을 구성합니다
2. 컨테이너는 1-5개의 요소를 가지며 null일 수 있습니다
3. 모든 문자열 값은 최소 길이가 3입니다
4. 주문 항목 가격은 항상 음수가 아닙니다

## 결론

이러한 개념을 이해하면 다음과 같은 도움이 됩니다:
- Fixture Monkey를 더 효과적으로 구성
- 생성기가 예상대로 작동하지 않을 때 문제 해결
- 정밀한 제어로 복잡한 테스트 데이터 생성

## 다음 단계

이러한 개념을 이해한 후에는 다음을 살펴보세요:

→ [초보자를 위한 필수 옵션](../essential-options-for-beginners) - 가장 일반적으로 사용되는 옵션부터 시작하기

→ [전문가를 위한 고급 옵션](../advanced-options-for-experts) - 복잡한 테스트 시나리오를 위한 옵션

## Property
문서에서 클래스 객체의 특성을 언급할 때 `field` 대신 일관되게 `property`라는 용어를 사용합니다.
Kotlin의 'property'와 이름이 같지만, Fixture Monkey에서의 개념은 다릅니다.

Fixture Monkey의 초기 구조는 주로 필드를 기반으로 했으며, 이는 메서드 및 기타 메커니즘을 통한 구성 및 제어에 제한을 두었습니다.
예를 들어, 필드에만 의존할 때는 setter 메서드의 애노테이션에 접근할 수 없습니다.
이러한 제한을 해결하기 위해 `Property` 인터페이스가 도입되었으며, 이는 필드를 넘어 지원을 확장합니다.

Fixture Monkey에서 `property`는 클래스 내의 기본 구성 요소로 기능하며 `Field`, `Method` 또는 Kotlin `Property`를 나타낼 수 있습니다.
이것은 해당 `Type`, 그에 대한 `Annotation`, 그리고 `name`에 관한 정보를 포함합니다.

또한, Fixture Monkey에서 `Objects`와 `Containers` 모두의 특성도 `property` 개념을 통해 표현됩니다.

### ObjectProperty
`ObjectProperty`는 변경 불가능한 객체 정보를 나타내는 프로퍼티입니다. 여기에는 다음이 포함됩니다:

- **property**: 객체 자체의 프로퍼티.
- **propertyNameResolver**: 프로퍼티 이름이 어떻게 해석되는지 결정합니다.
- **elementIndex**: 객체가 Container의 요소인 경우, 인덱스를 나타냅니다.

```java
public final class ObjectProperty {
    private final Property property;

    private final PropertyNameResolver propertyNameResolver;
	
    @Nullable
    private final Integer elementIndex;
}
```

### ContainerProperty
컨테이너 타입의 프로퍼티는 변경 불가능한 컨테이너 정보를 설명하는 `ContainerProperty`로 표현됩니다. 여기에는 다음이 포함됩니다:

- **elementProperties**: 요소 프로퍼티 목록.
- **containerInfo**: 컨테이너의 크기를 결정하는 `ArbitraryContainerInfo`.

```java
public final class ContainerProperty {
    private final List<Property> elementProperties;

    private final ArbitraryContainerInfo containerInfo;
}
```

## Options
Fixture Monkey에서 여러 옵션들은 공통적인 특성을 공유합니다.
예를 들어, `ObjectPropertyGenerator`를 수정하는 것과 관련된 옵션들을 살펴보겠습니다.

> `defaultObjectPropertyGenerator`, `pushObjectPropertyGenerator`, `pushAssignableTypeObjectPropertyGenerator`, `pushExactTypeObjectPropertyGenerator`

default 접두사가 있는 옵션은 Fixture Monkey가 생성하는 모든 프로퍼티에 기본값으로 적용됩니다.
이러한 기본값은 모든 프로퍼티 타입에 균일하게 영향을 미치는 기본 동작을 설정합니다.

그러나 특정 타입에 특정 옵션을 적용해야 하는 경우, push로 시작하는 옵션을 사용할 수 있습니다.
이러한 push 옵션에는 세 가지 변형이 있습니다.

- `push~`: 매개변수로 MatcherOperator를 받습니다.
- `pushAssignableType~`: 이 옵션은 주어진 타입(옵션과 연관된)이 할당 가능한 모든 프로퍼티 타입에 지정된 설정을 적용합니다. 이는 옵션이 정확한 주어진 타입뿐만 아니라 프로퍼티 타입에 할당될 수 있는 모든 타입(상위 클래스나 상위 인터페이스 포함)에도 적용됨을 의미합니다.
- `pushExactType~`: 이 옵션은 정확히 동일한 타입의 프로퍼티로 설정을 제한합니다. 하위 타입이나 상위 타입 관계를 가진 프로퍼티에는 영향을 미치지 않습니다.

`push` 변형을 사용하여 설정된 옵션이 `default` 옵션보다 우선한다는 점이 중요합니다. 즉, 특정 타입에 대해 `push` 옵션이 정의되면, 해당 타입에 대한 모든 해당 `default` 옵션을 재정의합니다.

```java
// 우선순위 예제:
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .defaultNullInjectGenerator(context -> 0.0)  // 1. 기본적으로 null이 아님
    .pushExactTypeNullInjectGenerator(String.class, context -> 1.0)  // 2. 문자열은 null이 됨
    .build();

Product product = fixtureMonkey.giveMeBuilder(Product.class)
    .set("name", "Override")  // 3. 이 특정 값이 null 설정을 재정의함
    .sample();

// 결과: String null 설정에도 불구하고 product.name은 "Override"가 됨
```

---

이러한 개념을 이해하면 Fixture Monkey의 옵션을 더 효과적으로 사용하여 정확히 필요한 테스트 데이터를 만들 수 있습니다. 다음 섹션에서는 테스트 객체를 커스터마이징하는 데 사용할 수 있는 특정 옵션들을 살펴보겠습니다.

## Container Types vs. Object Types

Fixture Monkey는 컨테이너 타입(`List`, `Set`, `Map` 등)을 일반 객체 타입과 다르게 처리합니다.

**테스트에서 왜 중요한가:** 이 차이를 이해하면 컬렉션이나 맵이 포함된 테스트를 올바르게 설정하는 데 도움이 됩니다.

예를 들어, 여러 상품이 담긴 장바구니를 테스트해야 한다면:

```java
// 상품 목록 생성
List<Product> productList = fixtureMonkey.giveMeBuilder(new TypeReference<List<Product>>() {})
    .size(3)  // 3개의 상품을 원한다고 지정
    .set("[0].name", "첫 번째 상품")  // 첫 번째 요소의 속성 설정
    .sample();
```
