---
title: "숙련자를 위한 고급 옵션"
sidebar_position: 53
---


이 가이드는 경험이 많은 사용자가 복잡한 테스트 시나리오를 해결하기 위해 활용할 수 있는 고급 Fixture Monkey 옵션을 다룹니다.

> **초보자를 위한 참고사항**: Fixture Monkey를 처음 사용하신다면, 먼저 [초보자를 위한 필수 옵션](./essential-options-for-beginners) 가이드로 시작하는 것을 권장합니다. 여기서 다루는 옵션들은 더 고급이며 일반적으로 복잡한 사용 사례에 필요합니다.
>
> 이러한 고급 옵션을 자세히 살펴보기 전에 핵심 [개념](./concepts)을 이해하고 있는지 확인하세요.

## 목차

1. [추천 사용 순서](#추천-사용-순서)
2. [성능 최적화 옵션](#성능-최적화-옵션)
3. [커스텀 Arbitrary 생성 로직](#커스텀-arbitrary-생성-로직)
4. [커스텀 타입 등록 및 생성](#커스텀-타입-등록-및-생성)
5. [프로퍼티 커스터마이징](#프로퍼티-커스터마이징)
6. [커스텀 인트로스펙션 설정](#커스텀-인트로스펙션-설정)
7. [표현식 엄격 모드](#표현식-엄격-모드)
8. [실제 고급 구성 예시](#실제-고급-구성-예시)
9. [일반적인 고급 테스트 시나리오](#일반적인-고급-테스트-시나리오)
10. [다음은 무엇인가요?](#다음은-무엇인가요)

## 추천 사용 순서

아래 순서로 진행하여 가장 많이 사용하는 고급 옵션을 빠르게 적용하세요:

1. 성능 최적화 옵션
2. 커스텀 Arbitrary 생성 로직 
3. 커스텀 타입 등록 및 생성 
4. 프로퍼티 커스터마이징
5. 커스텀 인트로스펙션 설정
6. 표현식 엄격 모드 
7. 실제 고급 구성 예시 
8. 일반적인 고급 테스트 시나리오

## 성능 최적화 옵션

### Manipulator Optimizer

FixtureMonkey 생성 중 적용되는 `ArbitraryManipulator` 시퀀스를 커스터마이징하거나 최적화할 수 있습니다. `ManipulatorOptimizer` 구현체를 통해 불필요한 조작을 결합, 필터링 또는 재정렬하여 복잡한 객체 그래프의 처리 효율을 높일 수 있습니다:

```java
// 사용자 정의 ManipulatorOptimizer 구현 예시
class CustomManipulatorOptimizer implements ManipulatorOptimizer {
    // manipulator 결합, 필터링, 재정렬 로직을 구현하세요
}

FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .manipulatorOptimizer(new CustomManipulatorOptimizer())
    .build();
```
**기본 동작:** 최적화기가 적용되지 않으며(`NoneManipulatorOptimizer`)를 사용합니다.
**사용 시기:** 복잡한 객체 그래프에서 불필요한 `ArbitraryManipulator` 실행을 최적화하고 싶을 때.

### 최대 시도 횟수 조정

기본값: `1000`회 재시도.

`generateMaxTries`는 값 제약 조건 또는 표현식 엄격 모드로 인해 픽스처 생성이 실패했을 때 재시도하는 최대 횟수를 설정합니다. 이 값을 줄이면 최악의 경우 테스트가 빠르게 실패하도록 할 수 있습니다:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .generateMaxTries(50)
    .build();
```
**사용 시기:** 픽스처 생성 실패 시 재시도 횟수를 제어하여 테스트 실패를 빠르게 확인하고 싶을 때.

### 고유 생성 시도 횟수 조정

기본값: `1000`회 재시도.

`generateUniqueMaxTries`는 컬렉션 내 고유한 요소(리스트나 세트 등)를 생성할 때, 중복 충돌로 인한 재시도 최대 횟수를 설정합니다. 이 값을 줄이면 고유 생성 실패 시 빠르게 예외를 발생시킬 수 있습니다:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .generateUniqueMaxTries(20)
    .build();
```
**사용 시기:** 컬렉션 요소의 고유성을 보장하고 싶을 때, 또는 중복 발생 시 빠르게 실패하도록 설정하고 싶을 때.

**사용 시기:** 컬렉션 요소가 중복 없이 고유해야 하는 시나리오에서, 중복 발생 시 빠르게 실패하도록 설정할 때.

### 컨테이너 생성 최적화

컨테이너(리스트, 세트, 맵) 처리를 효율적으로 하기 위한 옵션:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushArbitraryContainerInfoGenerator(
        new MatcherOperator<>(
            property -> property.getType().isAssignableFrom(List.class),
            context -> new ArbitraryContainerInfo(2, 10)
        )
    )
    .build();
```

**기본 동작:** 커스터마이징 없이도 Fixture Monkey는 기본 컨테이너 크기 생성기를 사용하여 0에서 3 사이의 크기를 가진 컨테이너를 생성합니다.

**사용 시기:**
- 성능에 민감한 테스트에서 복잡하고 깊게 중첩된 객체 구조를 다룰 때
- 다른 컬렉션 타입(예: 리스트와 세트)에 대해 서로 다른 컨테이너 크기 규칙이 필요할 때
- 특정 컨테이너 타입이나 컨텍스트에 따라 컨테이너 크기를 제어해야 할 때

## 커스텀 Arbitrary 생성 로직

### 기본 null 주입 생성기

null 주입 동작을 제어합니다.

```java
// 기본 null 주입 동작 구성
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .defaultNullInjectGenerator((context, random) -> random.nextBoolean(0.1)) // 10% 확률로 null 생성
    .build();
```
**사용 시기:** 모든 생성된 값에 대한 전역 null 주입 확률을 제어하고 싶을 때.

### 타입별 null 주입 생성기

타입별 null 주입 동작을 제어합니다.

```java
// 타입별 null 주입 동작 추가
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushAssignableTypeNullInjectGenerator(
        String.class,
        (context, random) -> random.nextBoolean(0.2) // 문자열의 경우 20% 확률로 null 생성
    )
    .build();
```
**사용 시기:** 특정 타입의 null 주입 확률을 다르게 설정하고 싶을 때.

- **사용 시기:** 생성된 테스트 데이터에서 null 값 비율을 전역 또는 타입별로 조정해야 할 때.

## 커스텀 타입 등록 및 생성

### 특정 타입에 대한 커스텀 생성기 등록하기

특정 타입의 생성 방식을 완전히 제어해야 할 때 사용합니다:

```java
// 간략화된 신용카드 생성기 예시
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .register(CreditCard.class, fm -> fm
        .giveMeBuilder(CreditCard.class)
        .set("number", Arbitraries.strings().numeric().ofLength(16).startsWith("4")) // VISA 형식의 16자리
        .set("expiryDate", () -> LocalDate.now().plusYears(2)) // 2년 후 날짜
        .set("cvv", Arbitraries.integers().between(100, 999)) // 100-999 사이 3자리
        )
    .build();

CreditCard card = fixtureMonkey.giveMeOne(CreditCard.class);
```

**각 접근 방식을 사용하는 경우:**

1. **변환이 있는 Arbitraries API**
   - 최적 사용: 내장된 랜덤화로 복잡한 패턴과 형식 생성
   - 사용 시점: 특정 형식이나 알고리즘을 따르는 값을 생성해야 할 때
   - 예: 신용카드 번호, ISBN 코드, 형식화된 식별자

2. **람다 표현식** (`() -> LocalDate.now().plusYears(2)`)
   - 최적 사용: 테스트 실행 시점에 평가되는 동적 또는 시간 의존적 값  
   - 사용 시점: 현재 시간을 기준으로 하거나 실행마다 변경되어야 하는 값
   - 예: 만료일, 타임스탬프, 증분 ID

3. **표준 Arbitraries API** (`Arbitraries.integers().between(100, 999)`)
   - 최적 사용: 무작위 값에 간단한 제약 조건 적용
   - 사용 시점: 특정 범위 내 값이나 패턴에 맞는 값이 필요한 경우
   - 예: 나이 범위, 우편번호, 제한된 숫자 값

**사용 시점:** 표준 생성 방식이 도메인 특화 객체에 대한 요구사항을 충족하지 못할 때.

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

참고: 동일한 타입에 `registerExactType`과 `registerAssignableType`을 모두 적용한 경우, 마지막에 추가된 설정이 우선 적용됩니다.

## 프로퍼티 커스터마이징

### Matcher 기반 프로퍼티 생성기 추가하기

`pushPropertyGenerator(MatcherOperator<PropertyGenerator>)`를 사용하여 다양한 조건에 맞는 `PropertyGenerator`를 등록할 수 있습니다. 예를 들어 특정 클래스나 패키지에 대해 커스텀 생성기를 한 번에 적용할 수 있습니다:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushPropertyGenerator(
        MatcherOperator.assignableTypeMatchOperator(
            MyClass.class,
            new CustomPropertyGenerator()
        )
    )
    .build();
```

**사용 시기:** 여러 타입이나 조건에 걸쳐 공통된 프로퍼티 생성 로직을 적용해야 할 때.

### 커스텀 객체 프로퍼티 생성기 추가하기

`pushExactTypePropertyGenerator`를 사용하여 특정 타입의 프로퍼티 생성 로직을 커스터마이징할 수 있습니다:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushExactTypePropertyGenerator(
        PropertyAddress.class, 
        new FieldPropertyGenerator()
    )
    .build();
```

#### 다양한 PropertyGenerator 구현체

위 예제에서는 `FieldPropertyGenerator`를 사용하여 필드를 기반으로 프로퍼티를 생성했지만, 상황에 따라 다양한 `PropertyGenerator` 구현체를 사용할 수 있습니다. 아래는 Fixture Monkey가 제공하는 주요 구현체입니다:

<div class="table-responsive">
<table class="table table-striped table-bordered">
  <thead>
    <tr>
      <th>구현체</th>
      <th>설명</th>
      <th>사용 시점</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>FieldPropertyGenerator</code></td>
      <td>클래스의 필드를 기반으로 프로퍼티를 생성합니다. 상속된 필드와 인터페이스 필드도 포함합니다.</td>
      <td>필드 접근 방식이 중요한 객체에 사용합니다. 모든 필드에 직접 접근하고 싶을 때 유용합니다.</td>
    </tr>
    <tr>
      <td><code>JavaBeansPropertyGenerator</code></td>
      <td>JavaBeans 규약에 따라 getter 메서드가 있는 프로퍼티를 생성합니다.</td>
      <td>getter 메서드가 있는 POJO 클래스에서 사용합니다. 캡슐화가 중요하고 public API를 통해서만 접근 가능한 객체에 적합합니다.</td>
    </tr>
    <tr>
      <td><code>ConstructorParameterPropertyGenerator</code></td>
      <td>생성자 파라미터를 기반으로 프로퍼티를 생성합니다.</td>
      <td>불변 객체나 생성자 주입 방식을 사용하는 객체에 적합합니다. 특히 lombok의 <code>@AllArgsConstructor</code>와 같은 어노테이션을 사용하는 객체에 유용합니다.</td>
    </tr>
    <tr>
      <td><code>CompositePropertyGenerator</code></td>
      <td>여러 PropertyGenerator를 결합하여 사용합니다.</td>
      <td>복잡한 객체 구조에서 여러 프로퍼티 생성 전략을 결합해야 할 때 사용합니다. 예를 들어 일부는 필드로, 일부는 생성자로 처리하고 싶을 때 유용합니다.</td>
    </tr>
    <tr>
      <td><code>ElementPropertyGenerator</code></td>
      <td>컨테이너 요소에 대한 프로퍼티를 생성합니다.</td>
      <td>컬렉션, 배열, 맵과 같은 컨테이너 타입의 요소를 다룰 때 사용합니다.</td>
    </tr>
    <tr>
      <td><code>LazyPropertyGenerator</code></td>
      <td>프로퍼티 생성을 지연시키고 필요할 때만 실행합니다.</td>
      <td>생성 비용이 높거나 순환 참조가 있는 객체 구조에서 사용합니다.</td>
    </tr>
  </tbody>
</table>
</div>

커스텀 프로퍼티 생성이 필요할 때는 상황에 가장 적합한 구현체를 선택하거나, 여러 구현체를 조합하여 사용할 수 있습니다. 예를 들어 대부분의 프로퍼티는 `FieldPropertyGenerator`로 생성하되, 특정 필드만 커스텀 로직으로 처리하는 방식을 구현할 수 있습니다.

**실제 적용 시나리오**: 
- 여러분의 시스템에 주소, 상품 코드, 신원 확인 번호와 같이 특별한 형식이나 유효성 검사가 필요한 객체가 있을 때
- 객체의 생성 방식을 완전히 제어하고 기본 생성 로직을 재정의해야 할 때
- 정확한 비즈니스 규칙이나 제약 조건을 적용하여 유효한 테스트 데이터만 생성하고 싶을 때

**사용 시기:** 특정 객체의 프로퍼티가 발견되고 생성되는 방식을 완전히 제어해야 할 때, 특히 복잡한 도메인 규칙이나 특별한 형식이 필요한 경우에 유용합니다.

### 커스텀 프로퍼티 이름 해석

#### 기본 프로퍼티 이름 해석기

`defaultPropertyNameResolver`를 사용하여 특정 해석기가 매칭되지 않을 때 모든 프로퍼티에 적용되는 전역 해석기를 설정할 수 있습니다:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .defaultPropertyNameResolver(property -> "'" + property.getName() + "'")
    .build();
```

**사용 시기:** 모든 경로 표현식에 대해 기본 네이밍 규칙을 전역적으로 커스터마이징할 때.

#### 특정 타입 해석기

Jackson 플러그인 또는 `pushExactTypePropertyNameResolver`를 사용하여 JSON 프로퍼티 이름을 처리할 수 있습니다:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new JacksonPlugin())
    .pushExactTypePropertyNameResolver(
        UserProfile.class, new JacksonPropertyNameResolver()
    )
    .build();
```

**사용 시기:** `@JsonProperty`로 정의된 이름 또는 JSON 필드가 코드 프로퍼티와 다른 경우.

## 커스텀 인트로스펙션 설정
> `pushArbitraryIntrospector`, `pushAssignableTypeArbitraryIntrospector`, `pushExactTypeArbitraryIntrospector`

`pushArbitraryIntrospector(MatcherOperator<ArbitraryIntrospector>)`를 사용하여 매칭 조건에 따른 커스텀 인트로스펙터를 등록할 수 있습니다:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushArbitraryIntrospector(
        new MatcherOperator<>(
            new ExactTypeMatcher(MyClass.class), 
            new CustomArbitraryIntrospector()
        )
    )
    .build();
```

제네릭 클래스의 경우, 타입 파라미터 개수도 매칭 조건에 포함할 수 있습니다.
> `SingleGenericTypeMatcher`, `DoubleGenericTypeMatcher`, `TripleGenericTypeMatcher`

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushArbitraryIntrospector(
        new MatcherOperator<>(
            new AssignableTypeMatcher(MyParameterizedClass.class).intersect(new SingleGenericTypeMatcher()),
            new CustomArbitraryIntrospector()
        )
    )
    .build();
```

**사용 시기:** 객체 인트로스펙션 방식을 조건별로 제어하고 싶을 때.

**유의:** 나중에 등록된 옵션이 먼저 적용됩니다.

커스텀 인트로스펙터 작성 방법은 [사용자 정의 인트로스펙터 만들기 가이드](../generating-objects/custom-introspector)를 참고하세요.

## 표현식 엄격 모드

`set()` 등의 경로 표현(path expression)이 실제 필드나 프로퍼티에 대응하지 않으면 예외를 발생시킵니다.

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .useExpressionStrictMode()
    .build();

// 'nonExistingField'가 Product 클래스에 없는 필드이면 sample() 단계에서 예외 발생
Product invalid = fixtureMonkey.giveMeBuilder(Product.class)
    .set("nonExistingField", 123)
    .sample();
```

**사용 시기:** `set()` 같은 표현식 기반 API의 경로 유효성 검사를 엄격히 적용하고 싶을 때.

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

## 일반적인 고급 테스트 시나리오

### 관계 제약 조건이 있는 복잡한 도메인 모델

```java
@Test
void testComplexDomainRelationships() {
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .defaultNotNull(true)
        // 특정 부서가 있는 회사 등록
        .register(Company.class, fm -> 
            fm.giveMeBuilder(Company.class)
                .size("departments", 3)
        )
        // 직원이 있는 부서 등록
        .register(Department.class, fm ->
            fm.giveMeBuilder(Department.class)
                .size("employees", 5)
        )
        .build();
    
    // 복잡한 회사 구조 생성
    Company company = fixtureMonkey.giveMeOne(Company.class);
    
    // 복잡한 작업 테스트
    ReorganizationResult result = reorganizationService.optimizeStructure(company);
    assertThat(result.getEfficiencyGain()).isGreaterThan(0.15); // 15% 향상
}
```

### 대규모 데이터 세트를 이용한 성능 테스트

```java
@Test
void testLargeDataSetPerformance() {
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .manipulatorOptimizer(new DefaultManipulatorOptimizer())
        .defaultArbitraryContainerInfoGenerator(
            new DefaultArbitraryContainerInfoGenerator(500, 500) // 큰 컨테이너
        )
        .build();
    
    // 대규모 데이터 세트 생성
    List<Transaction> transactions = fixtureMonkey.giveMe(
        new TypeReference<List<Transaction>>() {}, 1).get(0);
    
    // 성능 측정
    long startTime = System.currentTimeMillis();
    ProcessingResult result = batchProcessor.process(transactions);
    long endTime = System.currentTimeMillis();
    
    assertThat(endTime - startTime).isLessThan(2000); // 2초 내에 처리
}
```

## 다음은 무엇인가요?

이러한 고급 옵션을 마스터한 후에는 다음을 고려해 볼 수 있습니다:

→ [커스텀 인트로스펙터 만들기](../generating-objects/custom-introspector) - 특별한 도메인 요구사항을 위한 자체 인트로스펙터 구현하기

→ [Fixture Monkey에 기여하기](https://github.com/naver/fixture-monkey/blob/main/CONTRIBUTING.md) - 오픈 소스 커뮤니티에 참여하기

