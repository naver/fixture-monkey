---
title: "Introspector"
images: []
menu:
docs:
parent: "generating-objects"
identifier: "introspector"
weight: 33
---

[`instantiate`](../instantiate-methods)를 사용하여 `ArbitraryBuilder`에서 객체를 생성하는 방법을 변경할 수 있지만, 전역적으로 객체를 생성하는 방법을 변경하고 싶은 경우가 있을 수 있습니다.
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

`ConstructorPropertiesArbitraryIntrospector`안 경우 클래스 생성자에 `@ConstructorProperties`가 있거나 없으면 클래스가 레코드 타입이어야 합니다.
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

## BuilderArbitraryIntrospector
클래스 빌더를 사용하여 클래스를 생성하려면 `BuilderArbitraryIntrospector`를 사용할 수 있습니다.
이런 경우 클래스에 빌더가 있어야 합니다.

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(BuilderArbitraryIntrospector.INSTANCE)
    .build();
```

## FailoverArbitraryIntrospector
Sometimes your production code may contain several classes with different configurations, making it difficult to generate all objects with a single introspector.
In this case, you can use the `FailoverArbitraryIntrospector`.
This introspector allows you to use multiple introspectors, and it will continue the introspection even if one of the introspectors fails to generate.

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

----------------

Additional introspectors have been introduced inside plugins, such as [`JacksonObjectArbitraryIntrospector`](../../plugins/jackson-plugin/jackson-object-arbitrary-introspector) or
[`PrimaryConstructorArbitraryIntrospector`](../../plugins/kotlin-plugin/introspectors-for-kotlin)
