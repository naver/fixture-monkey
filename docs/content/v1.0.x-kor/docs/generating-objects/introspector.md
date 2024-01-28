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
Fixture Monkey는 다양한 `Introspector`를 제공한 객체 생성 방법을 제공합니다.

`Introspector`는 Fixture Monkey가 객체를 생성하는 기본 방법을 정의합니다.
각 introspector는 클래스의 인스턴스를 생성할 수 있는 몇 가지 제약 조건이 있습니다.

사용하려는 introspector를 `FixtureMonkey`의 `objectIntrospector` 옵션을 사용하여 변경할 수 있습니다.

## BeanArbitraryIntrospector
The `BeanArbitraryIntrospector` is the default introspector that fixture monkey uses for object creation.
It creates new instances using reflection and the setter method, so the class it creates must have a no-args constructor and setters.

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
    .build();
```

## ConstructorPropertiesArbitraryIntrospector
To generate an object with its given constructor, you can use `ConstructorPropertiesArbitraryIntrospector`.

For `ConstructorPropertiesArbitraryIntrospector`, the generated class should have a constructor with `@ConstructorProperties` or the class should be a record type.
(Or, if you are using Lombok, you can add `lombok.anyConstructor.addConstructorProperties=true` to the lombok.config file.)

When you create a record class and have multiple constructors, the constructor with the `@ConstructorProperties` annotation has priority.

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
    .build();
```

## FieldReflectionArbitraryIntrospector
`FieldReflectionArbitraryIntrospector` creates new instances with reflection and also sets the fields with reflection.
So the class to be generated must have a no-args constructor and one of the getters or setters.

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
    .build();
```

## BuilderArbitraryIntrospector
To generate a class using the class's builder, you can use `BuilderArbitraryIntrospector`.
It requires that the class has a builder.

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
