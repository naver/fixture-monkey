---
title: "Introspector"
images: []
menu:
docs:
parent: "generating-objects"
identifier: "introspector"
weight: 33
---

While you can change the way an object is created in the `ArbitraryBuilder` with [`instantiate`](../instantiate-methods), there may be cases where you want to change the way objects are created globally.
Fixture Monkey lets you choose the way you want to create your object by providing different `Introspectors`.

An `Introspector` defines the default way of how Fixture Monkey creates objects.
Each introspector has some kind of restrictions that the class must have in order for the introspector to generate instances of that class.

You can change the introspector you use by using the `objectIntrospector` option of Fixture Monkey.

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
