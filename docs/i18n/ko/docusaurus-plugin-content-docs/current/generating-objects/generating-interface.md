---
title: "인터페이스 타입 생성하기"
sidebar_position: 34
---


## 왜 인터페이스 타입을 생성해야 하나요?

테스트를 작성할 때 구체적인 구현체 대신 인터페이스를 사용해야 하는 경우가 많습니다:
- 인터페이스를 매개변수로 받는 코드를 테스트해야 할 수 있습니다
- 테스트 대상 시스템이 인터페이스 타입을 반환할 수 있습니다
- 특정 구현에 의존하지 않고 동작을 테스트하고 싶을 수 있습니다

Fixture Monkey는 간단한 인터페이스, 제네릭 인터페이스, sealed interface 등 다양한 인터페이스 타입의 테스트 객체를 쉽게 생성할 수 있게 해줍니다.

## 빠른 시작 예제

인터페이스 생성을 시작하기 위한 간단한 예제입니다:

```java
// 테스트하고 싶은 인터페이스 정의
public interface StringSupplier {
    String getValue();
}

// Fixture Monkey 인스턴스 생성
FixtureMonkey fixture = FixtureMonkey.create();

// 인터페이스의 인스턴스 생성
StringSupplier supplier = fixture.giveMeOne(StringSupplier.class);

// 테스트에서 사용
String value = supplier.getValue();
assertThat(value).isNotNull(); // 통과합니다
```

이 예제는 테스트에서 사용할 수 있는 `StringSupplier` 인터페이스의 익명 구현체를 생성합니다. 이제 인터페이스 생성을 위한 더 많은 옵션을 살펴보겠습니다.

## 인터페이스 생성 접근 방식

Fixture Monkey는 인터페이스 인스턴스를 생성하기 위한 세 가지 주요 접근 방식을 제공합니다:

| 접근 방식 | 설명 | 적합한 경우 |
|----------|-------------|----------|
| **익명 구현체** | Fixture Monkey가 익명 클래스를 생성 | 간단한 테스트, 간단한 인터페이스 |
| **특정 구현체 지정** | 어떤 클래스를 사용할지 직접 지정 | 더 많은 제어, 실제 동작이 필요한 경우 |
| **내장 구현체** | Fixture Monkey가 일반적인 인터페이스에 대한 기본 구현체 제공 | 표준 자바 인터페이스 |

### 각 접근 방식 예시

```java
// 익명 구현체
StringSupplier supplier = fixture.giveMeOne(StringSupplier.class);

// 특정 구현체 지정
InterfacePlugin plugin = new InterfacePlugin()
    .interfaceImplements(StringSupplier.class, List.of(DefaultStringSupplier.class));

// 내장 구현체
List<String> list = fixture.giveMeOne(new TypeReference<List<String>>() {});
```

## 내장 지원이 있는 일반적인 인터페이스 타입

Fixture Monkey는 일반적인 자바 인터페이스에 대한 기본 구현체를 제공합니다:

- `List` → `ArrayList`
- `Set` → `HashSet`
- `Map` → `HashMap`
- `Queue` → `LinkedList`
- 그 외 다수...

이러한 인터페이스들은 특별한 설정 없이 사용할 수 있습니다.

## 상세 예제

### 간단한 인터페이스

간단한 인터페이스 예제부터 시작해 보겠습니다:

```java
// 생성하고자 하는 인터페이스
public interface StringSupplier {
    String getValue();
}

// 사용할 수 있는 구체적인 구현체
public class DefaultStringSupplier implements StringSupplier {
    private final String value;

    @ConstructorProperties("value") // Lombok을 사용한다면 필요 없습니다
    public DefaultStringSupplier(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return "default" + value;
    }
}
```

#### 접근법 1: 익명 구현체 (옵션 없음)

가장 간단한 접근법은 Fixture Monkey가 익명 구현체를 생성하도록 하는 것입니다:

```java
@Test
void 익명_구현체_테스트() {
    // 설정
    FixtureMonkey fixture = FixtureMonkey.create();
    
    // 익명 구현체 생성
    StringSupplier result = fixture.giveMeOne(StringSupplier.class);
    
    // 테스트
    assertThat(result.getValue()).isNotNull();
    assertThat(result).isNotInstanceOf(DefaultStringSupplier.class);
}
```

이 접근법을 사용하면 Fixture Monkey는 `StringSupplier` 인터페이스를 구현하는 익명 객체를 생성합니다. `getValue()` 메서드는 무작위로 생성된 문자열을 반환합니다.

:::tip[중요]
Fixture Monkey는 다음 조건을 만족하는 메서드에 대해서만 속성 값을 생성합니다:
- getter 네이밍 규칙을 따르는 메서드(`getValue()`, `getName()` 등)
- 매개변수가 없는 메서드

다른 메서드는 항상 `null` 또는 기본 원시값을 반환합니다.
:::

일반 클래스와 마찬가지로 생성된 속성을 커스터마이징할 수 있습니다:

```java
@Test
void 속성이_커스터마이징된_테스트() {
    // 설정
    FixtureMonkey fixture = FixtureMonkey.create();
    
    // 특정 속성 값으로 생성
    StringSupplier result = fixture.giveMeBuilder(StringSupplier.class)
        .set("value", "사용자지정값")
        .sample();
    
    // 테스트
    assertThat(result.getValue()).isEqualTo("사용자지정값");
}
```

#### 접근법 2: 특정 구현체 사용

더 실제적인 동작이 필요할 때는 Fixture Monkey에게 구체적인 구현체를 사용하도록 지시할 수 있습니다:

```java
@Test
void 특정_구현체_테스트() {
    // 특정 구현체를 사용하도록 Fixture Monkey 설정
    FixtureMonkey fixture = FixtureMonkey.builder()
        .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE) // DefaultStringSupplier의 생성자를 위해 필요
        .plugin(
            new InterfacePlugin()
                .interfaceImplements(StringSupplier.class, List.of(DefaultStringSupplier.class))
        )
        .build();
    
    // 인터페이스 생성
    StringSupplier result = fixture.giveMeOne(StringSupplier.class);
    
    // 테스트
    assertThat(result).isInstanceOf(DefaultStringSupplier.class);
    assertThat(result.getValue()).startsWith("default");
}
```

이 접근법은 구현체에 정의된 동작을 가진 실제 `DefaultStringSupplier` 인스턴스를 생성합니다.

### 제네릭 인터페이스

제네릭 인터페이스의 경우 접근 방식은 비슷합니다. 하지만 타입 파라미터의 유무에 따라 Fixture Monkey의 동작이 달라질 수 있습니다:

#### 1. 타입 파라미터 없이 생성하는 경우

타입 파라미터 없이 제네릭 인터페이스를 생성하면, Fixture Monkey는 기본적으로 `String` 타입을 사용합니다:

```java
// 제네릭 인터페이스
public interface ObjectValueSupplier<T> {
    T getValue();
}

@Test
void 타입_파라미터_없는_제네릭_인터페이스_테스트() {
    FixtureMonkey fixture = FixtureMonkey.create();
    
    // 타입 파라미터 없이 생성
    ObjectValueSupplier<?> result = fixture.giveMeOne(ObjectValueSupplier.class);
    
    // 기본적으로 String 타입이 사용됩니다
    assertThat(result.getValue()).isInstanceOf(String.class);
}
```

#### 2. 타입 파라미터를 지정하는 경우

타입 파라미터를 명시적으로 지정하면 해당 타입으로 생성됩니다:

```java
@Test
void 타입_파라미터_지정_제네릭_인터페이스_테스트() {
    FixtureMonkey fixture = FixtureMonkey.create();
    
    // Integer 타입으로 지정
    ObjectValueSupplier<Integer> result = 
        fixture.giveMeOne(new TypeReference<ObjectValueSupplier<Integer>>() {});
    
    // Integer 타입으로 생성됩니다
    assertThat(result.getValue()).isInstanceOf(Integer.class);
}
```

#### 3. 특정 구현체 사용하기

특정 구현체를 사용하는 경우 해당 구현체의 타입 파라미터를 따릅니다:

```java
// String을 위한 구체적인 구현체
public class StringValueSupplier implements ObjectValueSupplier<String> {
    private final String value;

    @ConstructorProperties("value")
    public StringValueSupplier(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}

@Test
void 특정_구현체_사용_제네릭_인터페이스_테스트() {
    // 특정 구현체로 설정
    FixtureMonkey fixture = FixtureMonkey.builder()
        .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
        .plugin(
            new InterfacePlugin()
                .interfaceImplements(ObjectValueSupplier.class, List.of(StringValueSupplier.class))
        )
        .build();
    
    // 인터페이스 생성
    ObjectValueSupplier<?> result = fixture.giveMeOne(ObjectValueSupplier.class);
    
    // 테스트
    assertThat(result).isInstanceOf(StringValueSupplier.class);
    assertThat(result.getValue()).isInstanceOf(String.class);
}
```

:::tip[알아두세요]
제네릭 인터페이스를 타입 파라미터 없이 생성할 때 기본적으로 `String` 타입이 사용됩니다. 다른 타입을 원한다면 `TypeReference`를 사용하거나 특정 구현체를 지정하세요.
:::

### Sealed Interface (Java 17+)

Java 17은 허용된 구현체를 명시적으로 정의하는 sealed interface를 도입했습니다. Fixture Monkey는 추가 설정 없이도 이를 자동으로 처리합니다:

```java
// 허용된 구현체가 있는 sealed interface
sealed interface SealedStringSupplier {
    String getValue();
}

// 허용된 구현체
public static final class SealedDefaultStringSupplier implements SealedStringSupplier {
    private final String value;

    @ConstructorProperties("value")
    public SealedDefaultStringSupplier(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return "sealed" + value;
    }
}

@Test
void sealed_인터페이스_테스트() {
    // 설정
    FixtureMonkey fixture = FixtureMonkey.builder()
        .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
        .build();
    
    // sealed interface 생성
    SealedStringSupplier result = fixture.giveMeOne(SealedStringSupplier.class);
    
    // 테스트
    assertThat(result).isInstanceOf(SealedDefaultStringSupplier.class);
    assertThat(result.getValue()).startsWith("sealed");
}
```

## 다른 인터페이스와 결합하기

특정 인터페이스에 사용할 구현체를 지정할 수도 있습니다. 예를 들어, `List`의 기본 구현체인 `ArrayList` 대신 `LinkedList`를 사용하려면:

```java
@Test
void 커스텀_리스트_구현체_테스트() {
    // 설정
    FixtureMonkey fixture = FixtureMonkey.builder()
        .plugin(
            new InterfacePlugin()
                .interfaceImplements(List.class, List.of(LinkedList.class))
        )
        .build();
    
    // 생성
    List<String> list = fixture.giveMeOne(new TypeReference<List<String>>() {});
    
    // 테스트
    assertThat(list).isInstanceOf(LinkedList.class);
}
```

## 인터페이스 상속

Fixture Monkey는 인터페이스 상속도 처리할 수 있습니다. 계층 구조의 어느 수준에서든 구현체를 지정할 수 있습니다:

```java
interface ObjectValueSupplier {
    Object getValue();
}

interface StringValueSupplier extends ObjectValueSupplier {
    String getValue();
}

@Test
void 인터페이스_계층_테스트() {
    // 설정
    FixtureMonkey fixture = FixtureMonkey.builder()
        .plugin(
            new InterfacePlugin()
                .interfaceImplements(Collection.class, List.of(List.class))
        )
        .build();
    
    // List 구현체를 사용할 Collection 생성
    Collection<String> collection = fixture.giveMeOne(new TypeReference<Collection<String>>() {});
    
    // 테스트
    assertThat(collection).isInstanceOf(List.class);
}
```

## 고급 기능

더 복잡한 시나리오를 위해 Fixture Monkey는 인터페이스 구현 해결을 위한 고급 옵션을 제공합니다.

### 동적 구현체 해결

많은 구현체가 있거나 타입 조건에 따라 구현체를 선택해야 하는 경우:

```java
FixtureMonkey fixture = FixtureMonkey.builder()
    .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
    .plugin(
        new InterfacePlugin()
            .interfaceImplements(
                new AssignableTypeMatcher(ObjectValueSupplier.class),
                property -> {
                    Class<?> actualType = Types.getActualType(property.getType());
                    if (StringValueSupplier.class.isAssignableFrom(actualType)) {
                        return List.of(PropertyUtils.toProperty(DefaultStringValueSupplier.class));
                    }

                    if (IntegerValueSupplier.class.isAssignableFrom(actualType)) {
                        return List.of(PropertyUtils.toProperty(DefaultIntegerValueSupplier.class));
                    }
                    return List.of();
                }
            )
    )
    .build();
```

:::warning[고급 사용자를 위한 내용]
이 섹션은 대부분의 초보자가 처음에는 필요로 하지 않을 고급 기능을 설명합니다. 더 복잡한 인터페이스 생성 전략이 필요할 때 다시 참조하시기 바랍니다.
:::

### 사용자 정의 해결 구현

가장 고급 시나리오의 경우 `CandidateConcretePropertyResolver` 인터페이스를 구현할 수 있습니다:

```java
class YourCustomCandidateConcretePropertyResolver implements CandidateConcretePropertyResolver {
    @Override
    public List<Property> resolveCandidateConcreteProperties(Property property) {
        // 구현체를 해결하기 위한 사용자 정의 로직
        return List.of(...);
    }
}
```

타입 변환에 도움이 되는 내장 `ConcreteTypeCandidateConcretePropertyResolver`를 사용할 수 있습니다:

```java
FixtureMonkey fixture = FixtureMonkey.builder()
    .plugin(new InterfacePlugin()
        .interfaceImplements(
            new ExactTypeMatcher(Collection.class),
            new ConcreteTypeCandidateConcretePropertyResolver<>(List.of(List.class, Set.class))
        )
    )
    .build();
```

:::tip[중요]
옵션 적용을 위한 타입 조건을 설정할 때 `AssignableTypeMatcher`와 같은 매처를 주의해서 사용하세요. 구현체도 조건을 만족하면 무한 재귀가 발생할 수 있습니다.
:::

## 요약

Fixture Monkey로 인터페이스 타입을 생성하는 방법을 요약하면 다음과 같습니다:

1. **간단한 경우**: 익명 구현체를 얻기 위해 `fixture.giveMeOne(YourInterface.class)`를 사용
   
2. **특정 구현체**: `InterfacePlugin`과 `interfaceImplements`를 사용:
   ```java
   new InterfacePlugin().interfaceImplements(YourInterface.class, List.of(YourImplementation.class))
   ```

3. **내장 구현체**: `List`, `Set` 등과 같은 일반적인 인터페이스는 자동으로 처리됨

4. **Sealed interface**: 특별한 설정이 필요 없음 - Fixture Monkey가 허용된 구현체를 사용

5. **복잡한 경우**: 고급 시나리오를 위해 `AssignableTypeMatcher`를 사용하거나 `CandidateConcretePropertyResolver` 구현

대부분의 테스트 시나리오에서는 간단한 접근 방식으로 충분하다는 점을 기억하세요. 고급 기능은 생성된 구현체에 대한 더 많은 제어가 필요할 때 사용할 수 있습니다.

