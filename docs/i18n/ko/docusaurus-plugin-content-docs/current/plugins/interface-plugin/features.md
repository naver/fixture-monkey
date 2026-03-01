---
title: "Features"
sidebar_position: 41
---


인터페이스 플러그인은 Fixture Monkey에서 객체 생성 시 인터페이스와 추상 클래스의 구현을 동적으로 처리할 수 있게 해주는 강력한 도구입니다. 테스트 픽스처에서 인터페이스나 추상 클래스의 구체적인 구현을 지정해야 할 때 특히 유용합니다.

## 요약

- 인터페이스의 구체적인 구현 등록
- 추상 클래스의 구체적인 구현 등록
- 익명 임의 인트로스펙터 사용 옵션 (기본값: 활성화)
- CandidateConcretePropertyResolver를 사용한 속성 특성 기반 동적 구현 해결 지원

## 기본 사용법

```java
FixtureMonkey sut = FixtureMonkey.builder()
    .plugin(new InterfacePlugin()
        .interfaceImplements(MyInterface.class, Arrays.asList(MyInterfaceImpl1.class, MyInterfaceImpl2.class))
        .abstractClassExtends(MyAbstractClass.class, Arrays.asList(MyConcreteClass1.class, MyConcreteClass2.class))
    )
    .build();
```

### 익명 임의 인트로스펙터 비활성화

```java
FixtureMonkey sut = FixtureMonkey.builder()
    .plugin(new InterfacePlugin()
        .interfaceImplements(MyInterface.class, Arrays.asList(MyInterfaceImpl.class))
        .useAnonymousArbitraryIntrospector(false)
    )
    .build();
```

## API 참조

### interfaceImplements

주어진 인터페이스에 대한 구현을 등록합니다.

```java
public <T> InterfacePlugin interfaceImplements(
    Class<T> interfaceType,
    List<Class<? extends T>> implementations
)
```

매개변수:
- `interfaceType`: 구현할 인터페이스 클래스
- `implementations`: 인터페이스를 구현하는 클래스 목록

### abstractClassExtends

주어진 추상 클래스에 대한 구현을 등록합니다.

```java
public <T> InterfacePlugin abstractClassExtends(
    Class<T> abstractClassType,
    List<Class<? extends T>> implementations
)
```

매개변수:
- `abstractClassType`: 구현할 추상 클래스 타입
- `implementations`: 추상 클래스를 구현하는 클래스 목록

### useAnonymousArbitraryIntrospector

익명 임의 인트로스펙터 사용 여부를 설정합니다. 기본적으로 이 옵션은 활성화되어 있습니다 (기본값: true).
활성화된 경우, `AnonymousArbitraryIntrospector` 인스턴스를 폴백 인트로스펙터로 사용합니다.

```java
public InterfacePlugin useAnonymousArbitraryIntrospector(boolean useAnonymousArbitraryIntrospector)
```

매개변수:
- `useAnonymousArbitraryIntrospector`: 익명 임의 인트로스펙터 사용 여부 (기본값: true)

예제:
```java
// 기본 동작 (익명 인트로스펙터 활성화)
FixtureMonkey sut = FixtureMonkey.builder()
    .plugin(new InterfacePlugin()
        .interfaceImplements(MyInterface.class, Arrays.asList(MyInterfaceImpl.class))
        .useAnonymousArbitraryIntrospector(true)
    )
    .build();

// 익명 인트로스펙터 비활성화
FixtureMonkey sut2 = FixtureMonkey.builder()
    .plugin(new InterfacePlugin()
        .interfaceImplements(MyInterface.class, Arrays.asList(MyInterfaceImpl.class))
        .useAnonymousArbitraryIntrospector(false)
    )
    .build();
```

## 예제

### 기본 인터페이스 구현

```java
interface Animal {
    String sound();
}

class Dog implements Animal {
    @Override
    public String sound() {
        return "Woof";
    }
}

class Cat implements Animal {
    @Override
    public String sound() {
        return "Meow";
    }
}

FixtureMonkey sut = FixtureMonkey.builder()
    .plugin(new InterfacePlugin()
        .interfaceImplements(Animal.class, Arrays.asList(Dog.class, Cat.class))
    )
    .build();

// Dog 또는 Cat 인스턴스를 반환합니다
Animal animal = sut.giveMeOne(Animal.class);
```

### 추상 클래스 구현

```java
abstract class Vehicle {
    abstract int getWheels();
}

class Car extends Vehicle {
    @Override
    int getWheels() {
        return 4;
    }
}

class Bike extends Vehicle {
    @Override
    int getWheels() {
        return 2;
    }
}

FixtureMonkey sut = FixtureMonkey.builder()
    .plugin(new InterfacePlugin()
        .abstractClassExtends(Vehicle.class, Arrays.asList(Car.class, Bike.class))
    )
    .build();

// Car 또는 Bike 인스턴스를 반환합니다
Vehicle vehicle = sut.giveMeOne(Vehicle.class);
```

### 익명 객체 생성

`useAnonymousArbitraryIntrospector`가 활성화되어 있을 때, 등록된 구현이 없는 인터페이스에 대해 익명 구현을 생성할 수 있습니다. 이 플러그인은 JDK Dynamic Proxy를 사용하여 이러한 구현을 생성하며, 생성된 값을 커스터마이징할 수 있습니다. 다음은 그 예제입니다:

```java
interface UserService {
    String getUserName();
    int getUserAge();
    List<String> getUserRoles();
    
    // Default 메서드 - JDK 버전에 따라 동작이 다릅니다
    default String getFullInfo() {
        return getUserName() + " (" + getUserAge() + ")";
    }
}

FixtureMonkey sut = FixtureMonkey.builder()
    .plugin(new InterfacePlugin()
        .useAnonymousArbitraryIntrospector(true)
    )
    .build();

// JDK Dynamic Proxy를 사용하여 랜덤한 값을 가진 익명 구현을 생성합니다
UserService anonymousUserService = sut.giveMeOne(UserService.class);

// 생성된 값을 커스터마이즈합니다
UserService customAnonymousUserService = sut.giveMeBuilder(UserService.class)
    .set("userName", "John Doe")
    .set("userAge", 30)
    .set("userRoles", Arrays.asList("ADMIN", "USER"))
    .sample();
```

생성된 익명 구현은 다음과 같은 특징을 가집니다:
- JDK Dynamic Proxy를 사용하여 생성됩니다
- 기본적으로 모든 인터페이스 메서드에 대해 랜덤한 값을 반환합니다
- `giveMeBuilder`를 사용하여 커스터마이즈할 수 있습니다
- 여러 메서드 호출에서 일관된 값을 유지합니다
- 모든 기본 타입, 객체, 컬렉션을 지원합니다

참고: Default 메서드의 동작은 JDK 버전에 따라 다릅니다:
- JDK 17에서는 default 메서드가 원래 구현대로 동작합니다
- JDK 17 이전 버전에서는 default 메서드가 프록시되어 임의의 값을 반환하는 메서드로 생성됩니다
  - 이는 default 메서드의 원래 구현이 무시되고, 다른 메서드들과 마찬가지로 랜덤한 값을 반환한다는 의미입니다
  - 예를 들어, `getFullInfo()` 메서드는 원래 구현 대신 랜덤한 문자열을 반환하게 됩니다

## 고급 사용법

### 커스텀 매처 사용

```java
FixtureMonkey sut = FixtureMonkey.builder()
    .plugin(new InterfacePlugin()
        .interfaceImplements(
            new ExactTypeMatcher(MyInterface.class),
            Arrays.asList(MyInterfaceImpl1.class, MyInterfaceImpl2.class)
        )
    )
    .build();
```

### CandidateConcretePropertyResolver 사용

`CandidateConcretePropertyResolver`는 인터페이스나 추상 클래스의 구체적인 구현을 동적으로 결정할 수 있는 유연한 방법을 제공합니다. 다음과 같은 요소들을 기반으로 런타임에 결정을 내릴 수 있습니다:

- 프로퍼티 이름
- 프로퍼티 타입
- 프로퍼티 어노테이션
- 프로퍼티 메타데이터
- 기타 프로퍼티 특성

리졸버의 `resolve` 메서드는 구체적인 구현이 필요한 각 프로퍼티에 대해 호출되며, 이를 통해 다음과 같은 작업이 가능합니다:
1. 특정 케이스에 대해 단일 구현 반환
2. 랜덤 선택을 위한 여러 구현 반환
3. 프로퍼티 특성에 따라 다른 구현 반환
4. 적절한 구현을 결정하기 위한 복잡한 비즈니스 로직 적용

다음은 그 예제입니다:

```java
interface Animal {
    String sound();
    String getName();
}

class Dog implements Animal {
    @Override
    public String sound() {
        return "Woof";
    }

    @Override
    public String getName() {
        return "Dog";
    }
}

class Cat implements Animal {
    @Override
    public String sound() {
        return "Meow";
    }

    @Override
    public String getName() {
        return "Cat";
    }
}

// 프로퍼티 이름이 "animal"일 때만 구현을 반환하는 커스텀 리졸버
class AnimalResolver implements CandidateConcretePropertyResolver {
    @Override
    public List<Class<?>> resolve(Property property) {
        if ("animal".equals(property.getName())) {
            return Arrays.asList(Dog.class, Cat.class);
        }
        return Collections.emptyList();
    }
}

FixtureMonkey sut = FixtureMonkey.builder()
    .plugin(new InterfacePlugin()
        .interfaceImplements(
            new ExactTypeMatcher(Animal.class),
            new AnimalResolver()
        )
    )
    .build();

// 프로퍼티 이름이 "animal"일 때 Dog 또는 Cat 중 랜덤하게 선택
Animal animal = sut.giveMeOne(Animal.class);
``` 

