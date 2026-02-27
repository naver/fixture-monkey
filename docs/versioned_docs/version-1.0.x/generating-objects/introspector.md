---
title: "Introspector"
sidebar_position: 33
---


While you can change the way an object is created in the `ArbitraryBuilder` with [`instantiate`](./instantiate-methods), there may be cases where you want to change the way objects are created globally.
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

:::tip
If there are non-final variables declared, they can be instantiated without the need for getters or setters.
:::

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

If you want to disable the fail log, you should set the constructor argument `enableLoggingFail` to false.

```java
FailoverIntrospector failoverIntrospector = new FailoverIntrospector(introspectors, false);
```

## PriorityConstructorArbitraryIntrospector
Types that Fixture Monkey does not support creating by default can be created using a custom `ArbitraryIntrospector`.
However, creating your own `ArbitraryIntrospector` can be difficult if you are not familiar with Fixture Monkey.
To solve this difficulty, we provide a `PriorityConstructorArbitraryIntrospector` that uses a constructor to create the type.

```java
Timestamp actual = FixtureMonkey.builder()
    .objectIntrospector(PriorityConstructorArbitraryIntrospector.INSTANCE)
    .build()
    .giveMeOne(Timestamp.class);
```

### Differences from `ConstructorPropertiesArbitraryIntrospector`
The `ConstructorPropertiesArbitraryIntrospector` is also an `ArbitraryIntrospector` that uses a constructor to create an object.
The differences from `PriorityConstructorArbitraryIntrospector` are as follows.

|                                             | PriorityConstructorArbitraryIntrospector       | ConstructorPropertiesArbitraryIntrospector          |
|---------------------------------------------|------------------------------------------------|-----------------------------------------------------|
| Need `@ConstructorProperties`               | No                                             | Yes                                                 |
| Can customize the parameters of constructor | Optional (need `withParameterNamesResolver` )  | Yes                                                 |
| Criteria for choosing a constructor         | By the `constructorFilter`,  `sortingCriteria` | The first constructor with `@ConstructorProperties` |

### constructorFilter
The `PriorityConstructorArbitraryIntrospector` uses the `constructorFilter` condition to determine which constructor to use for generation.

The `constructorFilter` can be changed using `withConstructorFilter`.
By default, it is `constructor -> !Modifier.isPrivate(constructor.getModifiers())`.

### sortingCriteria
If there are multiple constructors that satisfy the `constructorFilter` condition, 
an additional `sortingCriteria` condition is used to determine the constructor.  
Use the first constructor when sorted by `Comparator<Constructor<?>>`.

The `sortingCriteria` can be changed using `withSortingCriteria`.
The default setting is the constructor with the least number of constructors. 
`Comparator.comparing(Constructor::getParameterCount)`

### parameterNamesResolver
Fixture Monkey cannot recognise constructor parameter names if any of the following three conditions are not met.
- record type
- Enable JVM option `-parameters`
- Existence of `@ConstructorProperties` in the constructor

If you do not recognise constructor parameter names, you cannot use the `ArbitraryBuilder` API to control constructor parameters.

The `PriorityConstructorArbitraryIntrospector` uses the `parameterNamesResolver` to recognise parameter names.
The `parameterNamesResolver` can be changed using `withParameterNamesResolver`.
The entered parameter names must always be the same as the parameter order.

----------------

Additional introspectors have been introduced inside plugins, such as [`JacksonObjectArbitraryIntrospector`](../plugins/jackson-plugin/jackson-object-arbitrary-introspector) or
[`PrimaryConstructorArbitraryIntrospector`](../plugins/kotlin-plugin/introspectors-for-kotlin)

