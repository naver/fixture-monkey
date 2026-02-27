---
title: "복잡한 객체 생성하기"
sidebar_position: 32
---


## 테스트에서 복잡한 타입이 중요한 이유

실제 테스트를 작성할 때 다음과 같은 복잡한 객체로 작업해야 하는 경우가 많습니다:
- 여러 타입 파라미터를 가진 제네릭 타입
- 트리나 그래프 같은 자기 참조 구조
- 복잡한 인터페이스 계층 구조
- sealed 클래스나 추상 클래스

테스트를 위해 이러한 타입의 인스턴스를 수동으로 생성하는 것은 매우 지루하고 오류가 발생하기 쉽습니다. 이런 경우 Fixture Monkey가 빛을 발합니다 - 최소한의 코드로 가장 복잡한 타입의 유효한 인스턴스까지 자동으로 생성할 수 있습니다.

## Fixture Monkey가 복잡한 타입을 처리하는 방법

Fixture Monkey는 런타임에 클래스와 인터페이스의 구조를 분석하여 관계와 제약 조건을 이해합니다. 그런 다음 중첩되고 재귀적인 구조에서도 필요한 모든 필드가 채워진 유효한 인스턴스를 생성합니다.

인터페이스의 경우, Fixture Monkey는 특별한 처리를 합니다. 하나의 인터페이스에 여러 구현체가 있을 때는 사용 가능한 구현체 중 하나를 무작위로 선택하여 생성합니다. 이는 다양한 구현체가 있는 인터페이스를 테스트할 때 매우 유용합니다. 물론 특정 구현체를 명시적으로 지정할 수도 있습니다. 이러한 동작은 `InterfacePlugin`을 통해 세부적으로 제어할 수 있습니다.

```java
// UserService 인터페이스에 대해 여러 구현체 지정 예시
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(
        new InterfacePlugin()
            .interfaceImplements(UserService.class, 
                List.of(BasicUserService.class, PremiumUserService.class))
    )
    .build();

// 지정된 구현체 중 하나가 무작위로 선택됨
UserService userService = fixtureMonkey.giveMeOne(UserService.class);
```

복잡한 타입의 예시와 Fixture Monkey로 생성하는 방법을 살펴보겠습니다.

## Java

### Generic Objects

타입 파라미터가 있는 제네릭 타입은 테스트에서 올바르게 인스턴스화하기 어려울 수 있습니다:

```java
@Value
public static class GenericObject<T> {
   T foo;
}

@Value
public static class GenericArrayObject<T> {
   GenericObject<T>[] foo;
}

@Value
public static class TwoGenericObject<T, U> {
   T foo;
   U bar;
}

@Value
public static class ThreeGenericObject<T, U, V> {
   T foo;
   U bar;
   V baz;
}
```

Fixture Monkey로 이러한 제네릭 타입의 인스턴스를 생성하는 방법:

```java
// String을 타입 파라미터로 갖는 단순 제네릭
GenericObject<String> stringGeneric = fixtureMonkey.giveMeOne(
    new TypeReference<GenericObject<String>>() {}
);

// 배열을 포함한 제네릭
GenericArrayObject<Integer> arrayGeneric = fixtureMonkey.giveMeOne(
    new TypeReference<GenericArrayObject<Integer>>() {}
);

// 여러 타입 파라미터
TwoGenericObject<String, Integer> twoParamGeneric = fixtureMonkey.giveMeOne(
    new TypeReference<TwoGenericObject<String, Integer>>() {}
);
```

### Generic Interfaces
```java
public interface GenericInterface<T> {
}

@Value
public static class GenericInterfaceImpl<T> implements GenericInterface<T> {
   T foo;
}

public interface TwoGenericInterface<T, U> {
}

@Value
public static class TwoGenericImpl<T, U> implements TwoGenericInterface<T, U> {
   T foo;

   U bar;
}
```

인터페이스 구현체 생성:

```java
// GenericInterface<String> 구현체 생성
GenericInterface<String> genericInterface = fixtureMonkey.giveMeOne(
    new TypeReference<GenericInterface<String>>() {}
);
```

예를 들어, 같은 인터페이스를 구현하는 여러 클래스가 있는 경우:

```java
public interface PaymentProcessor {
    void processPayment(double amount);
}

public class CreditCardProcessor implements PaymentProcessor {
    @Override
    public void processPayment(double amount) {
        // 신용카드 결제 처리 로직
    }
}

public class BankTransferProcessor implements PaymentProcessor {
    @Override
    public void processPayment(double amount) {
        // 계좌이체 결제 처리 로직
    }
}

// 구현체 중 하나가 무작위로 선택됨
PaymentProcessor processor = fixtureMonkey.giveMeOne(PaymentProcessor.class);
```

### SelfReference

자기 참조 타입은 수동으로 생성하기 특히 어렵지만 Fixture Monkey를 사용하면 쉽습니다:

```java
@Value
public class SelfReference {
   String foo;
   SelfReference bar;
}

@Value
public class SelfReferenceList {
   String foo;
   List<SelfReferenceList> bar;
}
```

깊이 제어와 함께 자기 참조 객체 생성:

```java
// 기본 생성 (무한 재귀를 피하기 위한 제한된 중첩 깊이)
SelfReference selfRef = fixtureMonkey.giveMeOne(SelfReference.class);

// 재귀 깊이를 제어하기 위한 사용자 정의 구성
FixtureMonkey customFixture = FixtureMonkey.builder()
    .defaultArbitraryContainerInfo(new ContainerInfo(2, 2)) // 리스트 크기 제어
    .build();
    
SelfReferenceList refList = customFixture.giveMeOne(SelfReferenceList.class);
```

### Interface
```java
public interface Interface {
   String foo();

   Integer bar();
}

public interface InheritedInterface extends Interface {
   String foo();
}

public interface InheritedInterfaceWithSameNameMethod extends Interface {
   String foo();
}

public interface ContainerInterface {
   List<String> baz();

   Map<String, Integer> qux();
}

public interface InheritedTwoInterface extends Interface, ContainerInterface {
}
```

## Kotlin
### Generic Objects
```kotlin
class Generic<T>(val foo: T)

class GenericImpl(val foo: Generic<String>)
```

Kotlin 제네릭 객체 생성:

```kotlin
// Generic<Int> 생성
val genericInt: Generic<Int> = fixtureMonkey.giveMeOne()

// 중첩된 Generic<String>이 있는 GenericImpl 생성
val genericImpl: GenericImpl = fixtureMonkey.giveMeOne()
```

### SelfReference
```kotlin
class SelfReference(val foo: String, val bar: SelfReference?)
```

### Sealed class, Value class
```kotlin
sealed class SealedClass

object ObjectSealedClass : SealedClass()

class SealedClassImpl(val foo: String) : SealedClass()

@JvmInline
value class ValueClass(val foo: String)
```

Kotlin에서 sealed 클래스 및 value 클래스 생성:

```kotlin
// Fixture Monkey는 sealed 클래스의 구체적인 구현체를 선택합니다
val sealedClass: SealedClass = fixtureMonkey.giveMeOne()

// value 클래스 생성
val valueClass: ValueClass = fixtureMonkey.giveMeOne()
```

Kotlin의 sealed 클래스도 인터페이스와 유사하게 처리됩니다. Fixture Monkey는 sealed 클래스의 모든 하위 클래스 중 하나를 무작위로 선택하여 생성합니다.

## 복잡한 타입 작업을 위한 팁

1. 제네릭 타입에는 타입 정보를 보존하기 위해 `TypeReference`를 사용하세요
2. 복잡한 인터페이스의 경우 `InterfacePlugin`을 사용하여 구현 클래스를 구성해야 할 수 있습니다
3. 인터페이스나 추상 클래스에 대해 특정 구현체만 사용하고 싶다면 `InterfacePlugin.interfaceImplements()`를 활용하세요
4. 매우 복잡한 구조의 경우 단계별로 분해하여 구축하는 것을 고려하세요

