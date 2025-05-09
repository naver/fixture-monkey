---
title: "숙련자를 위한 고급 옵션"
images: []
menu:
docs:
parent: "fixture-monkey-options"
identifier: "advanced-options-for-experts"
weight: 52
---

이 가이드는 경험이 많은 사용자가 복잡한 테스트 시나리오를 해결하기 위해 활용할 수 있는 고급 Fixture Monkey 옵션을 다룹니다.

## 커스텀 타입 등록 및 생성

### 특정 타입을 위한 커스텀 생성기 등록하기

특정 타입을 생성하는 방식을 완전히 제어해야 할 때:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .register(
        CreditCard.class,
        fm -> fm.giveMeBuilder(CreditCard.class)
            .set("number", creditCardGenerator())  // 커스텀 생성기 사용
            .set("expiryDate", () -> LocalDate.now().plusYears(2))
            .set("cvv", Arbitraries.integers().between(100, 999))
    )
    .build();
```

**사용 시기:** 표준 생성 방식이 도메인 특화 객체에 대한 요구 사항을 충족하지 못할 때.

### registerExactType과 registerAssignableType 사용하기

커스텀 생성기가 적용되는 클래스를 정밀하게 제어하기 위해:

```java
// Vehicle 클래스에만 정확히 적용되고 하위 클래스에는 적용되지 않음
fixtureMonkey.registerExactType(
    Vehicle.class,
    fm -> fm.giveMeBuilder(Vehicle.class)
        .set("manufacturer", "Tesla")
);

// Car 클래스와 모든 하위 클래스(SportsCar 등)에 적용됨
fixtureMonkey.registerAssignableType(
    Car.class,
    fm -> fm.giveMeBuilder(Car.class)
        .set("hasFourWheels", true)
);
```

**사용 시기:** 테스트에서 상속 계층 구조를 세밀하게 제어하고 싶을 때.

## 성능 최적화 옵션

### Manipulator Optimizer

대규모 객체 그래프에서는 생성 프로세스를 최적화하여 성능을 크게 향상시킬 수 있습니다:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .manipulatorOptimizer(new DefaultManipulatorOptimizer())
    .generateMaxTries(50)  // 더 나은 성능을 위해 최대 시도 횟수 줄이기
    .build();
```

**사용 시기:** 성능에 민감한 테스트에서 복잡하고 깊게 중첩된 객체 구조를 다룰 때.

### 컨테이너 생성 간소화

효율적인 컨테이너(리스트, 세트, 맵) 처리를 위해:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .defaultArbitraryContainerInfoGenerator(
        new DefaultArbitraryContainerInfoGenerator(1, 5)  // 더 작은 컨테이너
    )
    .build();
```

**사용 시기:** 더 빠른 테스트 실행을 위해 작고 집중적인 컬렉션을 원할 때.

## 고급 커스터마이징

### 커스텀 객체 프로퍼티 생성기 추가하기

커스텀 프로퍼티 생성 전략이 필요한 복잡한 객체:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushObjectPropertyGenerator(
        MatcherOperator.exactTypeMatchOperator(
            MyComplexType.class,
            (property, context) -> {
                // MyComplexType에 대한 프로퍼티 생성을 위한 커스텀 로직
                return customGenerationLogic(property, context);
            }
        )
    )
    .build();
```

**사용 시기:** 특정 객체 프로퍼티가 어떻게 발견되고 생성되는지 완전히 제어해야 할 때.

### 커스텀 프로퍼티 이름 해석

특별한 프로퍼티 명명 규칙을 위해:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushPropertyNameResolver(
        MatcherOperator.exactTypeMatchOperator(
            MyClass.class,
            property -> {
                // MyClass의 프로퍼티 이름을 해석하는 커스텀 로직
                return customNameResolutionLogic(property);
            }
        )
    )
    .build();
```

**사용 시기:** 비관행적인 프로퍼티 명명 체계나 리플렉션 문제가 있을 때.

## 고급 플러그인 커스터마이징

### 기존 플러그인 확장하기

플러그인 동작 방식을 수정해야 할 때:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(
        new JacksonPlugin()
            .registerModule(new JavaTimeModule())
            .objectMapperCustomizer(mapper -> 
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL))
    )
    .build();
```

**사용 시기:** 특정 구성이 필요한 프레임워크와 통합할 때.

### 다중 플러그인과 커스텀 로직 결합하기

여러 프레임워크를 혼합한 복잡한 테스트 환경:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new KotlinPlugin())
    .plugin(new JacksonPlugin())
    .plugin(new JavaxValidationPlugin())
    .plugin(new JqwikPlugin())
    .pushJavaConstraintGeneratorCustomizer(generator -> 
        generator.overrideConstraint(Email.class, property -> 
            Arbitraries.strings().withPattern("[a-z0-9]+@company\\.com")
        )
    )
    .build();
```

**사용 시기:** 여러 프레임워크와 특정 검증 요구사항이 있는 엔터프라이즈 프로젝트에서.

## 표현식 엄격 모드

객체 구성 중 엄격한 검증을 유지하기 위해:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .useExpressionStrictMode()
    .build();

Product product = fixtureMonkey.giveMeBuilder(Product.class)
    .set("price", -10L)  // Product가 양의 가격을 요구하면 거부됨
    .sample();
```

**사용 시기:** 표현식이 도메인 검증 규칙을 엄격하게 준수하기를 원할 때.

## 실제 고급 구성 예시

여러 고급 옵션을 결합한 복잡한 실제 구성:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    // 프레임워크 통합
    .plugin(new JacksonPlugin())
    .plugin(new JavaxValidationPlugin())
    
    // 성능 최적화
    .generateMaxTries(50)
    .manipulatorOptimizer(new DefaultManipulatorOptimizer())
    
    // 도메인별 커스터마이징
    .register(Money.class, fm -> 
        fm.giveMeBuilder(Money.class)
            .set("amount", Arbitraries.longs().between(1, 1_000_000))
            .set("currency", Arbitraries.of("USD", "EUR", "GBP"))
    )
    .register(User.class, fm ->
        fm.giveMeBuilder(User.class)
            .set("email", Arbitraries.emails().endingWith("@company.com"))
            .set("roles", Arbitraries.of(Role.class))
            .set("lastLoginDate", () -> LocalDateTime.now().minusDays(
                ThreadLocalRandom.current().nextLong(0, 30))
            )
    )
    
    // 전역 설정
    .defaultNotNull(true)
    .nullableContainer(false)
    .seed(1234L)  // 재현 가능한 테스트
    
    .build();
```

**사용 시기:** 도메인 제약 조건이 있는 객체 생성에 대한 정밀한 제어가 필요한 엔터프라이즈급 테스트 스위트에서. 
