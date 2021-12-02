---
title: "ArbitraryBuilder"
weight: 3
---
`ArbitraryBuilder` is a [fixture](https://junit.org/junit4/cookbook.html#Fixture) in Fixture Monkey. You could make `ArbitraryBuilder` by calling `giveMeBuilder`. For example, You can make a class `Person` fixture as below.
```java
fixtureMonkey.giveMeBuilder(Person.class)
```

Or You could make `ArbitraryBuilder` from existing an instance of `Person` class as below.

```java
fixtureMonkey.giveMeBuilder(new Person("SALLY"));
```

It offers a generic implementation of [Test Data Builder Pattern](http://www.natpryce.com/articles/000714.html). If you want a fixture for certain test case, you could manipulate it by [manipulators]({{< relref "/docs/v0.3.x/features/manipulator" >}}). For example, you could get a fixture returns `Person` instance which city is "SEOUL".

```java
fixtureMonkey.giveMeBuilder(Person.class)
    .set("city", "SEOUL")
```
## Feature

* Reusable in all tests since sampling always returns a different instance if `ComplexManipulator` is not applied.
* Could make existing instance into `ArbitraryBuilder`, but it always returns same instance. Check
  out [Example]({{< relref "/docs/v0.3.x/examples/manipulator/complexmanipulator#manipulate-existing-instance" >}})

## ValidOnly
{{< alert color="secondary" title="Note">}}
If you want more information, check out [here]({{< relref "/docs/v0.3.x/features/arbitraryvalidator" >}})
{{< /alert >}}

* If `true`, generates valid instance validated by registered `ArbitraryValidator`. 
  Default is `true`
* If `false`, generates not valid instance.

## Generator

Change [ArbitraryGenerator]({{< relref "/docs/v0.3.x/features/arbitrarygenerator" >}}) to generate given `ArbitraryBuilder`

## Build

`build` would return [Arbitrary]({{< relref "/docs/v0.3.x/features/arbitrary" >}})

## Sample

`sample` would return instance in fixture

* Always return different instance

## Where should I go next?
* [Manipulator]({{< relref "/docs/v0.3.x/features/manipulator" >}})
