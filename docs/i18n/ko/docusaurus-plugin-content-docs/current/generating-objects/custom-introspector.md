---
title: "사용자 정의 인트로스펙터 만들기"
sidebar_position: 36
---


> **참고**: 이 가이드는 Fixture Monkey 기본 개념을 이해하고 있는 고급 사용자를 위한 것입니다. 대부분의 사용자는 내장 인트로스펙터가 일반적인 시나리오를 처리할 수 있기 때문에 사용자 정의 인트로스펙터를 만들 필요가 없습니다. 처음 시작하는 경우 먼저 [인트로스펙터](./introspector) 가이드를 확인하세요.

## 언제 사용자 정의 인트로스펙터가 필요한가요?

다음과 같은 특정 상황에서 사용자 정의 인트로스펙터가 필요할 수 있습니다:

1. 클래스에 내장 인트로스펙터로 처리할 수 없는 고유한 생성 요구사항이 있는 경우
2. 비표준적인 패턴을 따르는 타사 라이브러리와 작업하는 경우
3. 객체에 `instantiate` 메서드로 해결할 수 없는 특별한 초기화 로직이 필요한 경우

이 중 어느 것도 해당하지 않는다면 내장 인트로스펙터로 충분할 것입니다.

## 기본적인 접근 방법

사용자 정의 인트로스펙터를 만드는 두 가지 주요 방법이 있습니다:

### 1. 속성에서 객체 조립하기

이 방법은 속성을 사용하여 수동으로 객체를 구성해야 할 때 유용합니다:

```java
// 특정 클래스 타입을 처리하는 사용자 정의 인트로스펙터 예제
public class CustomArbitraryIntrospector implements ArbitraryIntrospector {
    @Override
    public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
        // 1단계: 이 인트로스펙터가 이 타입을 처리해야 하는지 확인
        Property property = context.getResolvedProperty();
        Class<?> type = Types.getActualType(property.getType());
        if (!MyCustomClass.class.isAssignableFrom(type)) {
            // 대상 타입이 아니면 다른 인트로스펙터가 처리하도록 함
            return ArbitraryIntrospectorResult.NOT_INTROSPECTED;
        }
        
        // 2단계: Fixture Monkey가 이 객체에 대해 생성한 속성 가져오기
        Map<ArbitraryProperty, CombinableArbitrary<?>> arbitrariesByProperty = 
            context.getCombinableArbitrariesByArbitraryProperty();
        
        // 3단계: 이러한 속성을 사용하여 객체 구축
        CombinableArbitrary<MyCustomClass> combinableArbitrary = CombinableArbitrary.objectBuilder()
            .properties(arbitrariesByProperty)
            .build(propertyValues -> {
                // 클래스의 새 인스턴스 생성
                MyCustomClass obj = new MyCustomClass();
                
                // 각 속성 값 설정
                propertyValues.forEach((property, value) -> {
                    String propertyName = property.getName();
                    if ("name".equals(propertyName)) {
                        obj.setName((String) value);
                    } else if ("value".equals(propertyName)) {
                        obj.setValue((Integer) value);
                    }
                });
                
                return obj;
            });
        
        // 4단계: 결과 반환
        return new ArbitraryIntrospectorResult(combinableArbitrary);
    }
}
```

### 1.1 필수 속성 정의하기

때로는 자식 속성이 발견되고 생성되는 방식을 사용자 정의해야 할 수 있습니다:

```java
//특정 속성에 대한 속성 생성을 제어하려면 이 메서드를 오버라이드하세요
@Override
@Nullable
public PropertyGenerator getRequiredPropertyGenerator(Property property) {
    // 이 속성이 특별한 처리가 필요한지 확인
    if ("nestedObject".equals(property.getName())) {
        // 특정 필드만 포함하는 생성기 생성
        return new FieldPropertyGenerator(
            // id와 name 필드만 포함
            field -> "id".equals(field.getName()) || "name".equals(field.getName()),
            // 필터를 통과한 모든 필드 매칭
            field -> true
        );
    }
    
    // 다른 속성의 경우, 기본 생성기 사용
    return null;
}
```

### 2. 고정 인스턴스 반환하기

때로는 상수나 특별히 계산된 값만 반환하면 되는 경우가 있습니다:

```java
// 특정 타입에 대해 고정 값을 반환하는 인트로스펙터 예제
public class ConstantArbitraryIntrospector implements ArbitraryIntrospector {
    private final Object constantValue;
    
    public ConstantArbitraryIntrospector(Object constantValue) {
        this.constantValue = constantValue;
    }
    
    @Override
    public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
        Property property = context.getResolvedProperty();
        Class<?> type = Types.getActualType(property.getType());
        
        // 상수가 올바른 타입인지 확인
        if (!type.isInstance(constantValue)) {
            return ArbitraryIntrospectorResult.NOT_INTROSPECTED;
        }
        
        // 상수 값 반환
        return new ArbitraryIntrospectorResult(
            CombinableArbitrary.from(constantValue)
        );
    }
}
```

## 사용자 정의 인트로스펙터 사용하기

인트로스펙터를 만든 후에는 두 가지 방법으로 사용할 수 있습니다:

### 전역 인트로스펙터로 사용

##### 단독 사용

```java
// 사용자 정의 인트로스펙터 생성
ArbitraryIntrospector customIntrospector = new CustomArbitraryIntrospector();

// 전역 인트로스펙터로 사용
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(customIntrospector)
    .build();

// 객체 생성
MyCustomClass obj = fixtureMonkey.giveMeOne(MyCustomClass.class);
```

##### 다른 인트로스펙터와 함께 사용

일반적으로 사용자 정의 인트로스펙터를 내장 인트로스펙터와 함께 사용하고 싶을 것입니다:

```java
// 사용자 정의 인트로스펙터를 먼저 시도하고,
// 적용되지 않으면 표준 인트로스펙터로 대체하는 Fixture Monkey 생성
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(new FailoverIntrospector(
        Arrays.asList(
            customIntrospector,  // 사용자 정의 인트로스펙터를 먼저 시도
            ConstructorPropertiesArbitraryIntrospector.INSTANCE,
            BuilderArbitraryIntrospector.INSTANCE,
            FieldReflectionArbitraryIntrospector.INSTANCE,
            BeanArbitraryIntrospector.INSTANCE
        )
    ))
    .build();
```

### 특정 타입 전용 인트로스펙터로 사용

```java
// MyCustomClass에 한해 customIntrospector 사용
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .pushArbitraryIntrospector(
                new MatcherOperator<>(
                        new ExactTypeMatcher(MyCustomClass.class),
                        customIntrospector
                )
        )
        .build();
```

다양한 `ArbitraryIntrospector` 설정 옵션에 대해서는 [커스텀 인트로스펙션 설정(고급 옵션)](../fixture-monkey-options/advanced-options-for-experts#커스텀-인트로스펙션-설정) 에서 확인하세요.

## 모범 사례

사용자 정의 인트로스펙터를 만들 때:

1. **항상 타입을 확인하세요** - 인트로스펙터가 처리하지 않는 타입에 대해서는 `NOT_INTROSPECTED`를 반환
2. **예외를 우아하게 처리하세요** - 테스트 실패를 방지
3. **집중적으로 유지하세요** - 각 인트로스펙터는 특정 패턴이나 클래스 타입을 처리해야 함
4. **성능을 고려하세요** - 인트로스펙터는 모든 객체 생성에 실행됨
5. **철저하게 테스트하세요** - 다양한 엣지 케이스로 테스트

#### 실제 사례: Range 클래스

```java
// 외부 라이브러리 - Instant를 제네릭 타입으로 사용하는 Range<C> 클래스
public class RangeInstantArbitraryIntrospector implements ArbitraryIntrospector {

    @Override
    public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
        Property property = context.getResolvedProperty();
        Class<?> type = Types.getActualType(property.getType());
        List<AnnotatedType> typeArguments = Types.getGenericsTypes(property.getAnnotatedType());
        Class<?> genericType = typeArguments.isEmpty() ? null : Types.getActualType(typeArguments.getFirst());
        
        if (!type.equals(Range.class)
                || typeArguments.size() != 1
                || !genericType.equals(Instant.class)) {
            return ArbitraryIntrospectorResult.NOT_INTROSPECTED;
        }
        
        // ===== 난수 생성 예시 =====
        int randomInt = (int)(Math.random() * 365) + 1;

        Instant startTime = Instant.now().minus(randomInt, ChronoUnit.DAYS);
        Instant endTime = Instant.now().plus(randomInt, ChronoUnit.DAYS);

        Range<Instant> rangeValue = Range.closed(startTime, endTime);
        
        return new ArbitraryIntrospectorResult(
                CombinableArbitrary.from(rangeValue)
        );
    }
}
```

#### 실제 사례: InetAddress 클래스

클래스 타입 매칭을 인트로스펙터 내부가 아닌 `.pushArbitraryIntrospector()` 에서 처리할 수도 있습니다.

```java
// java.net.InetAddress 클래스
public class InetAddressArbitraryIntrospector implements ArbitraryIntrospector {

    @Override
    public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
        Property property = context.getResolvedProperty();
        Class<?> type = Types.getActualType(property.getType());

        InetAddress inetAddress;
        if (type.equals(Inet4Address.class)){
            inetAddress = generateRandomInet4Address();
        } else {
            inetAddress = generateRandomInet6Address();
        }

        return new ArbitraryIntrospectorResult(
                CombinableArbitrary.from(inetAddress)
        );
    }

    private Inet4Address generateRandomInet4Address() {
        // 랜덤 생성 로직 구현
    }

    private Inet6Address generateRandomInet6Address() {
        // 랜덤 생성 로직 구현
    }
}
```

```java
ArbitraryIntrospector inetAddressArbitraryIntrospector = new InetAddressArbitraryIntrospector();

// InetAddress 및 그 하위 클래스에 한해 InetAddressArbitraryIntrospector 사용
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .pushArbitraryIntrospector(
                new MatcherOperator<>(
                        new AssignableTypeMatcher(InetAddress.class),
                        inetAddressArbitraryIntrospector
                )
        )
        // ...
        .build();
```

## 고급: 속성 생성기

Fixture Monkey는 사용자 정의 속성 발견에 도움이 될 수 있는 여러 내장 `PropertyGenerator` 구현체를 제공합니다:

### FieldPropertyGenerator

클래스 필드를 기반으로 속성을 생성할 때 유용합니다:

```java
// 특정 조건의 필드를 기반으로 속성 생성
new FieldPropertyGenerator(
    // final이 아니고 특정 어노테이션이 있는 필드만
    field -> !Modifier.isFinal(field.getModifiers()) && 
             field.isAnnotationPresent(MyRequired.class),
    // 필터를 통과한 모든 필드 포함
    field -> true
)
```

### CompositePropertyGenerator

여러 속성 생성기를 결합합니다:

```java
// 필드와 JavaBeans 속성 생성을 함께 사용
new CompositePropertyGenerator(
    Arrays.asList(
        new FieldPropertyGenerator(field -> true, matcher -> true),
        new JavaBeansPropertyGenerator(
            descriptor -> descriptor.getReadMethod() != null, 
            matcher -> true
        )
    )
)
```

### DefaultPropertyGenerator

일반적인 생성기의 미리 구성된 조합:

```java
// 표준 필드 및 JavaBeans 속성 생성 사용
new DefaultPropertyGenerator()
```

## 결론

사용자 정의 인트로스펙터 생성은 고급 주제이지만 Fixture Monkey에서 객체 생성을 완전히 제어할 수 있게 해줍니다. 대부분의 사용자는 이 수준의 사용자 정의가 필요하지 않지만, 내장 인트로스펙터로 해결할 수 없는 특별한 요구 사항이 있을 때 사용할 수 있습니다.

사용자 정의 인트로스펙터에 대해 질문이 있다면 다양한 구현 접근 방식의 예제를 위해 내장 인트로스펙터의 소스 코드를 참조하세요.

