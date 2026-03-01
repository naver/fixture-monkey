---
title: "Introspector"
sidebar_position: 33
---


[`instantiate`](./instantiate-methods)를 사용하여 `ArbitraryBuilder`에서 객체를 생성하는 방법을 변경할 수 있지만, 옵션을 통해 전역적으로도 객체 생성 방법을 지정할 수 있습니다.
Fixture Monkey는 다양한 `Introspector`로 객체를 생성하는 방법을 제공합니다.

`Introspector`는 Fixture Monkey가 객체를 생성하는 기본 방법을 정의합니다.
각 introspector는 클래스의 인스턴스를 생성할 수 있는 몇 가지 제약 조건이 있습니다.

사용하려는 introspector를 `FixtureMonkey`의 `objectIntrospector` 옵션을 사용하여 변경할 수 있습니다.

## BeanArbitraryIntrospector
`BeanArbitraryIntrospector`는 Fixture Monkey가 객체 생성에 사용하는 기본 introspector입니다.
리플렉션과 setter 메서드를 사용하여 새 인스턴스를 생성하므로 생성할 클래스에는 인자가 없는 생성자(또는 기본생성자)와 setter가 있어야 합니다.

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
    .build();
```

## ConstructorPropertiesArbitraryIntrospector
주어진 생성자로 객체를 생성하려면 `ConstructorPropertiesArbitraryIntrospector`를 사용해야 합니다.

`ConstructorPropertiesArbitraryIntrospector`인 경우 클래스 생성자에 `@ConstructorProperties`가 있거나 없으면 클래스가 레코드 타입이어야 합니다.
(또는 Lombok을 사용하는 경우 lombok.config 파일에 `lombok.anyConstructor.addConstructorProperties=true`를 추가할 수 있습니다.)

레코드 클래스를 생성할 때 여러 생성자를 가질 경우 `@ConstructorProperties` 주석이 있는 생성자가 우선 선택됩니다.

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
    .build();
```

## FieldReflectionArbitraryIntrospector
`FieldReflectionArbitraryIntrospector`는 리플렉션을 사용하여 새 인스턴스를 생성하고 필드를 설정합니다.
따라서 생성할 클래스는 인자가 없는 생성자(또는 기본 생성자)와 getter 또는 setter 중 하나를 가져야 합니다.

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
    .build();
```

:::tip
만약 final이 아닌 변수가 선언되어 있다면 getter 또는 setter 없이도 사용 가능합니다.
:::

## BuilderArbitraryIntrospector
클래스 빌더를 사용하여 클래스를 생성하려면 `BuilderArbitraryIntrospector`를 사용할 수 있습니다.
이런 경우 클래스에 빌더가 있어야 합니다.

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(BuilderArbitraryIntrospector.INSTANCE)
    .build();
```

## FailoverArbitraryIntrospector
프로덕션 코드에서 다수의 클래스가 있을 때 각 클래스마다 다른 설정을 가진다면 하나의 introspector로 모든 객체를 생성하기 어려울 수 있습니다.
이 경우 `FailoverArbitraryIntrospector`를 사용할 수 있습니다.
`FailoverArbitraryIntrospector`를 사용하면 두 개 이상의 introspector를 사용할 수 있으며, introspector 중 하나가 생성에 실패하더라도 `FailoverArbitraryIntrospector`는 계속 다음 introspector로 객체 생성을 시도합니다.

```java
FixtureMonkey sut = FixtureMonkey.builder()
    .objectIntrospector(new FailoverIntrospector(
        Arrays.asList(
            FieldReflectionArbitraryIntrospector.INSTANCE,
            ConstructorPropertiesArbitraryIntrospector.INSTANCE
        )
    ))
    .build();
```

생성에 실패하면 발생하는 로그를 보고싶지 않다면 생성자 파라미터 `enableLoggingFail`를 false로 설정하면 됩니다.

```java
FailoverIntrospector failoverIntrospector = new FailoverIntrospector(introspectors, false);
```

## PriorityConstructorArbitraryIntrospector
픽스쳐 몽키에서 기본으로 생성을 지원하지 않는 타입은 사용자 정의 `ArbitraryIntrospector`를 사용하면 생성할 수 있습니다. 
하지만 픽스쳐 몽키에 익숙하지 않다면 `ArbitraryIntrospector`를 만들기는 어렵습니다. 
이런 어려움을 해결해주기 위해 생성자를 사용해서 타입을 생성하는 `PriorityConstructorArbitraryIntrospector`를 제공합니다.

```java
Timestamp actual = FixtureMonkey.builder()
    .objectIntrospector(PriorityConstructorArbitraryIntrospector.INSTANCE)
    .build()
    .giveMeOne(Timestamp.class);
```

### `ConstructorPropertiesArbitraryIntrospector` 와의 차이점
`ConstructorPropertiesArbitraryIntrospector`도 생성자를 사용해서 객체를 생성하는 `ArbitraryIntrospector` 입니다.
`PriorityConstructorArbitraryIntrospector`와의 차이점은 다음과 같습니다.

|                                | PriorityConstructorArbitraryIntrospector             | ConstructorPropertiesArbitraryIntrospector |
|--------------------------------|------------------------------------------------------|--------------------------------------------|
| `@ConstructorProperties` 필요 여부 | 필요없음                                                 | 필요함                                        |
| 생성자의 파라미터를 제어할 수 있는지           | 조건부 (`withParameterNamesResolver`를 설정한 경우)           | 가능함                                        |
| 생성에 사용할 생성자를 결정하는 방법           | `constructorFilter`와  `sortingCriteria` 조건을 사용해서 결정함 | `@ConstructorProperties`가 있는 첫 번째 생성자      |

### constructorFilter
`PriorityConstructorArbitraryIntrospector`는 생성에 사용할 생성자를 결정할 때 `constructorFilter` 조건을 사용합니다.

`constructorFilter`는 `withConstructorFilter`를 사용해서 변경할 수 있습니다.
기본 조건은 `constructor -> !Modifier.isPrivate(constructor.getModifiers())`입니다.

### sortingCriteria
`constructorFilter` 조건을 만족하는 생성자가 여러 개 일경우 추가적으로 `sortingCriteria` 조건을 사용해서 생성자를 결정합니다.  
`Comparator<Constructor<?>>`로 정렬했을 때 첫 번째 생성자를 사용합니다.

`sortingCriteria`는 `withSortingCriteria`를 사용해서 변경할 수 있습니다.
기본 설정은 생성자 수가 가장 적은 생성자입니다. `Comparator.comparing(Constructor::getParameterCount)

### parameterNamesResolver
다음 세 가지 조건 중 하나도 만족하지 않으면 픽스쳐 몽키에서 생성자 파라미터 이름을 인식할 수 없습니다.
- record 타입
- JVM 옵션 `-parameters` 활성화
- 생성자에 `@ConstructorProperties` 존재 

생성자 파라미터 이름을 인식하지 못하면 `ArbitraryBuilder` API를 사용해 생성자 파라미터를 제어할 수 없습니다.

`PriorityConstructorArbitraryIntrospector`에서 `parameterNamesResolver`를 사용해 파라미터 이름을 인식합니다.
`parameterNamesResolver`는 `withParameterNamesResolver`를 사용해서 변경할 수 있습니다.
입력한 파라미터 이름은 항상 파라미터 순서와 동일해야 합니다.

----------------

플러그인 별로 관련된 introspector도 존재합니다. 예를들어 [`JacksonObjectArbitraryIntrospector`](../plugins/jackson-plugin/jackson-object-arbitrary-introspector)와 [`PrimaryConstructorArbitraryIntrospector`](../plugins/kotlin-plugin/introspectors-for-kotlin)가 존재합니다.

