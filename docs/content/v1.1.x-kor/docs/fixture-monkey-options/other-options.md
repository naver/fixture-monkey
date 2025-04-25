---
title: "기타 옵션"
images: []
menu:
docs:
parent: "fixture-monkey-options"
identifier: "options"
weight: 53
---

이 섹션에서는 초보자 또는 전문가 가이드에서 다루지 않은 `FixtureMonkeyBuilder`에서 제공하는 추가 옵션들을 설명합니다. 테스트에서 필요한 빈도에 따라 옵션들을 우선순위별로 정리했습니다.

> **옵션 문서 탐색 가이드**:
> * Fixture Monkey를 처음 접하신다면, [초보자를 위한 필수 옵션](../essential-options-for-beginners)부터 시작하세요.
> * 고급 옵션을 찾고 계시다면, [전문가를 위한 고급 옵션](../advanced-options-for-experts)을 확인하세요.
> * 개념적 이해를 위해서는 [옵션 개념](../concepts)을 참조하세요.

## 1. 특수 값 처리 옵션

이 옵션들은 테스트에서 자주 필요한 특수 값을 처리합니다.

### Null 값 처리

null 값이 언제 어떻게 생성되는지 제어합니다:

```java
// 기본 null 주입 동작 구성
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .defaultNullInjectGenerator(
        (context, random) -> random.nextBoolean(0.1) // 10% 확률로 null 생성
    )
    .build();

// 타입별 null 주입 동작 추가
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushAssignableTypeNullInjectGenerator(
        String.class,
        (context, random) -> random.nextBoolean(0.2) // 문자열의 경우 20% 확률로 null 생성
    )
    .build();
```

> **핵심 용어**: `NullInjectGenerator`는 컨텍스트와 무작위성에 기반하여 프로퍼티가 null이 되어야 하는지 결정합니다.

### 고유 값 생성

생성된 값이 고유하도록 보장합니다:

```java
// 고유 값 생성 최대 시도 횟수 설정
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .generateUniqueMaxTries(200)
    .build();
```

## 2. 검증 및 제약 조건 옵션

생성된 객체가 특정 조건이나 제약을 만족하도록 합니다.

### 검증

생성된 객체에 대한 맞춤 검증 설정:

```java
// 생성된 객체에 대한 사용자 정의 검증기 설정
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .arbitraryValidator(new CustomArbitraryValidator())
    .build();
```

> **핵심 용어**: `ArbitraryValidator`를 사용하면 생성된 값이 특정 조건을 충족하는지 확인할 수 있습니다.

### Java 제약 조건 처리

생성된 객체의 Java 제약 조건 처리:

```java
// 사용자 정의 Java 제약 조건 생성기 구성
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .javaConstraintGenerator(new CustomJavaConstraintGenerator())
    .build();

// 기존 Java 제약 조건 생성기 사용자 지정
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushJavaConstraintGeneratorCustomizer(generator -> 
        generator.overrideConstraint(MyConstraint.class, 
            prop -> Arbitraries.strings().alpha())
    )
    .build();

// 특정 프로퍼티 경로에 대한 valid-only 플래그 설정
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushCustomizeValidOnly(
        TreeMatcher.exactPath("root.child.property"), 
        true
    )
    .build();
```

> **핵심 용어**: `JavaConstraintGenerator`는 `@Min`, `@Max`, `@NotNull`과 같은 애노테이션을 처리하고 이를 적절한 생성기로 변환합니다.

## 3. 객체 분석 및 생성 옵션

이 옵션들은 Fixture Monkey가 객체 구조를 이해하고 인스턴스를 생성하는 방법을 제어합니다.

### 인트로스펙션 옵션

Fixture Monkey가 객체를 분석하는 방법 사용자 지정:

```java
// 특정 클래스에 대한 사용자 정의 인트로스펙터 설정
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushAssignableTypeArbitraryIntrospector(
        MyClass.class, 
        new CustomArbitraryIntrospector()
    )
    .build();

// 모든 객체에 대한 기본 객체 인트로스펙터 설정
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(new CustomObjectIntrospector())
    .build();
```

> **핵심 용어**: `ArbitraryIntrospector`는 인스턴스 생성 방법을 결정하기 위해 객체를 분석하는 역할을 합니다.

### 프로퍼티 발견 및 접근 옵션

객체에서 프로퍼티를 찾고 접근하는 방법 제어:

```java
// 기본 프로퍼티 생성기 설정
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .defaultPropertyGenerator(new CustomPropertyGenerator())
    .build();

// 특정 타입에 대한 사용자 정의 프로퍼티 생성기 추가
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushAssignableTypePropertyGenerator(
        MyClass.class,
        new CustomPropertyGenerator()
    )
    .build();

// 기본 프로퍼티 이름 리졸버 설정
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .defaultPropertyNameResolver(new CustomPropertyNameResolver())
    .build();

// 특정 타입에 대한 프로퍼티 이름 리졸버 추가
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushAssignableTypePropertyNameResolver(
        MyClass.class,
        new CustomPropertyNameResolver()
    )
    .build();
```

> **핵심 용어**: `PropertyGenerator`는 객체의 프로퍼티를 발견하는 역할을 하며, `PropertyNameResolver`는 표현식 기반 접근을 위한 프로퍼티 이름을 결정합니다.

## 4. 컨테이너 및 컬렉션 처리 옵션

컬렉션, 맵, 배열과 같은 컨테이너 타입의 처리 방법을 제어합니다.

### 컨테이너 인트로스펙션

컨테이너 타입의 분석 방법 사용자 지정:

```java
// 컨테이너 타입에 대한 사용자 정의 인트로스펙션 추가
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushContainerIntrospector(new CustomContainerIntrospector())
    .build();

// 사용자 정의 처리로 컨테이너 타입 구성
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .addContainerType(
        MyContainer.class,
        new MyContainerPropertyGenerator(),
        new MyContainerArbitraryIntrospector(),
        new MyContainerDecomposedContainerValueFactory()
    )
    .build();
```

> **핵심 용어**: Fixture Monkey에서 `컨테이너`는 여러 요소를 보유하는 모든 객체(List, Set, Map 또는 배열 등)입니다.

### 컨테이너 값 분해 옵션

컨테이너의 요소가 처리되는 방식 구성:

```java
// 기본 컨테이너 값 팩토리 설정
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .defaultDecomposedContainerValueFactory(
        new CustomDecomposedContainerValueFactory()
    )
    .build();

// 특정 타입에 대한 컨테이너 값 팩토리 추가
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .addDecomposedContainerValueFactory(
        MyCollection.class,
        new CustomDecomposedContainerValueFactory()
    )
    .build();
```

> **핵심 용어**: `DecomposedContainerValueFactory`는 Fixture Monkey가 컨테이너에서 값을 추출하는 방법을 이해하는 데 도움을 줍니다.

## 5. 확장 및 통합 옵션

이 옵션들은 Fixture Monkey를 다른 라이브러리나 프레임워크와 통합하는 데 사용됩니다.

### 플러그인 옵션

서드파티 라이브러리에 대한 플러그인 지원 추가:

```java
// Jackson 플러그인 추가
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new JacksonPlugin())
    .build();

// 여러 플러그인 결합
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new KotlinPlugin())
    .plugin(new JacksonPlugin())
    .build();
```

> **핵심 용어**: `플러그인`은 서드파티 라이브러리나 프레임워크에 대한 지원을 추가하는 확장 지점입니다.

### 그룹 등록 옵션

관련 타입들의 생성 로직을 그룹으로 등록:

```java
// 팩토리 메서드가 있는 클래스에서 임의의 그룹 등록
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .registerGroup(MyArbitraryFactoryGroup.class)
    .build();

// ArbitraryBuilderGroup 인터페이스를 사용하여 그룹 등록
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .registerGroup(new MyArbitraryBuilderGroup())
    .build();
```

> **핵심 용어**: `ArbitraryBuilderGroup`을 사용하면 관련 타입들을 함께 등록할 수 있습니다.

## 코드 예제 (Java/Kotlin 형식)

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new JacksonPlugin())
    .build();

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val fixtureMonkey = FixtureMonkey.builder()
    .plugin(JacksonPlugin())
    .build()

{{< /tab >}}
{{< /tabpane>}}

## 다음 단계

이러한 옵션들을 살펴본 후에는 다음을 확인해 보세요:

→ [옵션 개념](../concepts) - Fixture Monkey 옵션이 어떻게 작동하는지 더 깊이 이해하기 위해

→ [전문가를 위한 고급 옵션](../advanced-options-for-experts) - 복잡한 시나리오에 더 많은 제어가 필요한 경우
