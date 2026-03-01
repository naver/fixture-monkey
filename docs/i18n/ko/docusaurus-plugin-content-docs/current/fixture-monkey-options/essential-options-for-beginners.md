---
title: "초보자를 위한 필수 옵션"
sidebar_position: 51
---


Fixture Monkey를 처음 시작할 때, 몇 가지 핵심 옵션만 이해하면 복잡성에 압도되지 않고 필요한 테스트 데이터를 생성할 수 있습니다. 이 가이드는 초보자를 위한 가장 필수적인 옵션에 초점을 맞춥니다.

> 인트로스펙터, 생성기, 프로퍼티 유형과 같은 핵심 개념에 대한 포괄적인 이해를 위해 [개념](./concepts)을 참조하세요.
> 더 고급 옵션을 사용할 준비가 되었다면 [전문가를 위한 고급 옵션](./advanced-options-for-experts)을 참조하세요.

## 일반 빌더 옵션

이 옵션들은 FixtureMonkey 인스턴스를 생성할 때 설정됩니다:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    // 여기에 옵션 추가
    .build();
```

### 기본 NotNull 설정

기본적으로 Fixture Monkey는 일부 필드에 대해 `null` 값을 생성할 수 있습니다. 모든 필드에 null이 아닌 값을 원한다면:

```java
.defaultNotNull(true)
```

이 옵션은 테스트에서 `NullPointerException`을 방지하는 데 도움이 되므로 시작하기에 가장 유용한 옵션 중 하나입니다.

**기본값**: `false` - 이 설정이 없으면 Fixture Monkey는 `@NotNull` 어노테이션이 표시되지 않은 필드에 대해 null 값을 생성할 수 있습니다.

**사용 시기:** NullPointerException을 방지하기 위해 모든 필드가 null이 아닌 값을 가지도록 할 때 사용합니다.

### Nullable 컨테이너

컬렉션 타입(`List`, `Set`, `Map` 등)이 null이 될 수 있는지 제어합니다:

```java
.nullableContainer(false)
```

**기본값**: `false` - 기본적으로 컬렉션 타입은 null이 될 수 없습니다. 이 값을 `true`로 설정하면 컬렉션 타입이 null이 될 수 있습니다.

**사용 시기:** 전체 컨테이너 타입에 대해 null을 허용하거나 금지해야 할 때 사용합니다.

### Nullable 요소

컬렉션 내부의 요소가 null이 될 수 있는지 제어합니다:

```java
.nullableElement(false)
```

**기본값**: `false` - 기본적으로 컬렉션 내부의 요소는 null이 될 수 없습니다. 이 값을 `true`로 설정하면 컬렉션의 요소가 null이 될 수 있습니다.

**사용 시기:** 컬렉션 내 개별 요소의 null 가능성을 제어하고 싶을 때 사용합니다.

### 컨테이너 크기 구성

컬렉션과 맵의 기본 크기를 제어합니다:

```java
// 모든 컨테이너가 2~5개의 요소를 갖도록 구성
.defaultArbitraryContainerInfoGenerator(
    new DefaultArbitraryContainerInfoGenerator(2, 5)
)
```

**기본값**: 기본적으로 컨테이너는 0에서 3개의 요소를 가집니다.

**사용 시기:** 테스트 전반에 걸쳐 일관된 컬렉션 크기가 필요하거나 생성된 데이터의 양을 제어하고 싶을 때 사용합니다.

> 더 고급 컨테이너 처리 옵션은 개념 문서의 [컨테이너 타입 vs. 객체 타입](./concepts#컨테이너-타입-vs-객체-타입)을 참조하세요.

### 타입 구성

특정 타입이 어떻게 생성되는지 구성할 수 있습니다:

```java
// String 값이 어떻게 생성되는지 구성
.register(String.class, fm -> 
    fm.giveMeBuilder(String.class)
        .set("$", "Default String")
)

// Integer 값이 어떻게 생성되는지 구성
.register(Integer.class, fm -> 
    fm.giveMeBuilder(Integer.class)
        .set("$", Arbitraries.integers().between(1, 100))
)

// BigDecimal 값이 어떻게 생성되는지 구성
.register(BigDecimal.class, fm -> 
    fm.giveMeBuilder(BigDecimal.class)
        .set("$", new BigDecimal("10.00"))
)

// List<String> 값이 어떻게 생성되는지 구성
.register(new TypeReference<List<String>>() {}, fm -> 
    fm.giveMeBuilder(new TypeReference<List<String>>() {})
        .size("$", 1, 5)
)
```

**사용 시기:** 특정 타입에 대한 사용자 정의 생성 규칙을 적용해야 할 때 사용합니다.

> 타입 등록 시스템에 대한 더 깊은 이해를 위해 개념 문서의 [타입 등록 시스템](./concepts#4-타입-등록-시스템)을 참조하세요.

### JqwikPlugin 옵션

Fixture Monkey는 [Jqwik](https://jqwik.net/) 라이브러리와 통합되는 JqwikPlugin을 제공합니다. 이 플러그인은 기본 타입이 어떻게 생성되는지 제어하는 여러 옵션을 제공합니다:

#### String, Number 및 Boolean 생성 커스터마이징

String, Number, Boolean 및 기타 기본 타입이 어떻게 생성되는지 커스터마이징할 수 있습니다:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(
        new JqwikPlugin()
            .javaTypeArbitraryGenerator(new JavaTypeArbitraryGenerator() {
                @Override
                public StringArbitrary strings() {
                    // 10자 길이의 알파벳 문자열만 생성
                    return Arbitraries.strings().alpha().ofLength(10);
                }
                
                @Override
                public IntegerArbitrary integers() {
                    // 양의 정수만 생성
                    return Arbitraries.integers().greaterOrEqual(1);
                }
                
                @Override
                public DoubleArbitrary doubles() {
                    // 0과 1 사이의 소수 생성
                    return Arbitraries.doubles().between(0.0, 1.0);
                }
            })
    )
    .build();
```

**기본 동작**: 커스터마이징이 없으면 Fixture Monkey는 각 타입의 전체 범위에 걸쳐 임의의 값을 생성합니다.
**사용 시기**: 양수만 사용하거나 형식이 지정된 문자열과 같이 테스트 데이터가 특정 패턴이나 범위를 따라야 할 때 사용합니다.

#### 날짜 및 시간 생성 커스터마이징

기본 타입과 마찬가지로 날짜 및 시간 값이 어떻게 생성되는지 제어할 수 있습니다:

```java
.plugin(
    new JqwikPlugin()
        .javaTimeTypeArbitraryGenerator(new JavaTimeTypeArbitraryGenerator() {
            @Override
            public Arbitrary<LocalDate> localDates() {
                // 다음 30일 이내의 날짜만 생성
                LocalDate today = LocalDate.now();
                return Arbitraries.dates()
                    .between(today, today.plusDays(30));
            }
            
            @Override
            public Arbitrary<LocalTime> localTimes() {
                // 업무 시간(오전 9시 ~ 오후 5시)만 생성
                return Arbitraries.times()
                    .between(LocalTime.of(9, 0), LocalTime.of(17, 0));
            }
        })
)
```

**기본 동작**: 가능한 전체 범위에 걸쳐 임의의 날짜와 시간.
**사용 시기**: 현실적인 날짜 범위나 특정 시간 패턴이 필요한 테스트에 사용합니다.

> 타입과 생성기의 더 고급 커스터마이징은 [전문가를 위한 고급 옵션](./advanced-options-for-experts#커스텀-타입-등록-및-생성)을 참조하세요.

### 사용자 정의 Null 확률

null 값이 얼마나 자주 생성되는지 제어합니다:

```java
// 기본 null 삽입 동작 구성(10% 확률로 null)
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .defaultNullInjectGenerator(context -> 0.1)
    .build();

// 타입별 null 삽입 동작 구성(String에 대해 20% 확률로 null)
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushAssignableTypeNullInjectGenerator(
        String.class,
        context -> 0.2
    )
    .build();
```

**기본값**: 기본적으로 null 생성 확률은 프로퍼티의 어노테이션에 의해 결정되며, 일반적으로 `@NotNull`이 있는 필드는 0, 그렇지 않은 경우 0이 아닌 값입니다.

**사용 시기**: 전역적으로 또는 특정 타입에 대해 테스트 픽스처에서 null 값이 나타나는 빈도를 제어해야 할 때 사용합니다.

### 생성에서 타입 제외

어떤 타입이나 패키지가 테스트 데이터 생성에서 제외되어야 하는지 제어합니다:

```java
// 특정 클래스 제외
.addExceptGenerateClass(MyInternalClass.class)

// 여러 클래스 제외
.addExceptGenerateClasses(ClassA.class, ClassB.class)

// 전체 패키지 제외
.addExceptGeneratePackage("com.mycompany.internal")

// 사용자 정의 매처 기반 제외
.pushExceptGenerateType(property -> property.getName().equals("sensitiveField"))
```

**기본값**: 없음 - 기본적으로 Fixture Monkey는 모든 프로퍼티를 생성하려고 시도합니다.

**사용 시기:** 내부 구현 세부 사항, 민감한 필드 또는 복잡한 종속성을 테스트 데이터 생성에서 제외하고 싶을 때 사용합니다.

### 관련 타입 그룹 등록

`registerGroup`을 사용하여 그룹 클래스나 구현을 통해 관련된 여러 타입을 한 번에 등록합니다:

```java
// 팩토리 그룹 클래스 사용
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .registerGroup(MyArbitraryFactoryGroup.class)
    .build();

// ArbitraryBuilderGroup 구현 사용
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .registerGroup(new MyArbitraryBuilderGroup())
    .build();
```

**사용 시기:** 비슷한 생성 로직을 가진 여러 관련 타입이 있고 이를 함께 등록하고 싶을 때 사용합니다.

### 사용자 정의 객체 인트로스펙션

Fixture Monkey가 테스트 데이터를 생성하기 위해 객체를 분석하고 이해하는 방법을 제어합니다:

```java
// 모든 타입에 대한 사용자 정의 인트로스펙터 설정
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(new CustomObjectIntrospector())
    .build();

// 예시: 모든 타입에 대해 내장 인트로스펙터 사용
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
    .build();
```

**기본값**: 기본 인트로스펙터는 추가한 플러그인에 따라 다릅니다. 플러그인이 없으면 필드 기반 인트로스펙션을 사용합니다.

**사용 시기:** Fixture Monkey가 클래스의 필드와 프로퍼티를 발견하는 방법을 변경해야 할 때 사용합니다. 예를 들어, getter/setter 기반 인트로스펙션 대신 필드 기반 인트로스펙션을 사용하고 싶거나, 특정 객체 타입을 처리하기 위한 사용자 정의 전략이 필요할 때 사용합니다.

> 인트로스펙터와 생성기에 대한 더 깊은 이해를 위해 개념 문서의 [생성기와 인트로스펙터](./concepts#1-생성기와-인트로스펙터)를 참조하세요.

### 디버깅 및 재현성 옵션

이러한 옵션은 디버깅과 테스트 재현성 보장에 도움이 됩니다:

```java
// 생성 실패에 대한 상세 로깅 활성화
.enableLoggingFail(true)

// 결정론적 테스트 데이터 생성을 위한 고정 시드 설정
.seed(1234L)
```

**기본값**: 
- `enableLoggingFail`: `false` - 기본적으로 상세 오류 로그는 표시되지 않습니다
- `seed`: 현재 시스템 시간 - 실행마다 다른 임의 값을 사용하게 합니다

**사용 시기:**
- 픽스처 생성이 실패하는 이유를 디버깅해야 할 때 로깅을 활성화합니다
- 테스트 실행 간에 재현 가능한 테스트 데이터가 필요할 때 고정 시드를 설정합니다

## 다양한 시나리오에서 옵션 사용하기

- **특정 값이 중요하지 않고 빠른 테스트 데이터가 필요할 때:**
  - `defaultNotNull(true)`로 기본 빌더 사용

- **시연용으로 현실적인 값이 필요할 때:**
  - 각 타입에 대한 사용자 정의 생성기 등록
  - 숫자 필드에 대한 최소/최대 값 설정

- **경계 조건을 테스트해야 할 때:**
  - 특정 값이나 제약 조건이 있는 중요한 속성에 `.set()` 사용
  - 복잡한 조건에 대해 predicate 사용

- **관련 데이터를 생성해야 할 때:**
  - 관련 객체를 생성하기 위해 중첩된 `giveMeBuilder` 호출 사용

## 다음 단계

이제 필수 옵션을 이해했으니 다음을 배울 수 있습니다:

→ [옵션 개념](./concepts) - 옵션이 내부적으로 어떻게 작동하는지 더 깊이 이해하기

→ [JavaBean 유효성 검사](../plugins/jakarta-validation-plugin/features) - 데이터 생성을 안내하기 위한 유효성 검사 어노테이션 사용하기

