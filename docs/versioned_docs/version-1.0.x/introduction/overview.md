---
title: "Overview"
sidebar_position: 11
---


## Fixture Monkey

Fixture Monkey is a Java & Kotlin library designed to generate controllable arbitrary test objects.

It focuses on simplifying test writing, by facilitating the generation of necessary test fixtures.
Whether you're dealing with basic or complex test fixtures, Fixture Monkey helps you to effortlessly create the test objects you need and easily customize them to match your desired configurations.

Make your JVM tests more concise and safe with Fixture Monkey.

---------

## Why use Fixture Monkey?
### 1. Simplicity
```java
Product actual = fixtureMonkey.giveMeOne(Product.class);
```
Fixture Monkey makes test object generation remarkably easy. With just one line of code, you can effortlessly generate any kind of test object you desire.
It simplifies the given section of the test, enabling you to write tests faster and more easily. You also don't need to change the production code or test environment.

### 2. Reusability
```java
ArbitraryBuilder<Product> actual = fixtureMonkey.giveMeBuilder(Product.class)
    .set("id", 1000L)
    .set("productName", "Book");
```
Fixture Monkey allows you to reuse configurations of instances across multiple tests, saving you time and effort.
Complex specifications only need to be defined once within your builder and can then be reused to obtain instances.

Furthermore, there are additional features that boost reusability. For more details on these features, refer to the sections on 'Registering Default ArbitraryBuilder' and 'InnerSpec'.

### 3. Randomness
```java
ArbitraryBuilder<Product> actual = fixtureMonkey.giveMeBuilder(Product.class);

then(actual.sample()).isNotEqualTo(actual.sample());
```
Fixture Monkey helps tests become more dynamic by generating test objects with random values.
This leads to uncovering edge cases that might remain hidden when using static data.

### 4. Versatility
```java
// inheritance
class Foo {
  String foo;
}

class Bar extends Foo {
    String bar;
}

Foo foo = FixtureMonkey.create().giveMeOne(Foo.class);
Bar bar = FixtureMonkey.create().giveMeOne(Bar.class);

// circular-reference
class Foo {
    String value;

    Foo foo;
}

Foo foo = FixtureMonkey.create().giveMeOne(Foo.class);

// anonymous objects
interface Foo {
    Bar getBar();
}

class Bar {
    String value;
}

Foo foo = FixtureMonkey.create().giveMeOne(Foo.class);
```

Fixture Monkey is capable to create any kind of object you can imagine. It supports generating basic objects such as lists, nested collections, enums and generic types.
It also handles more advanced scenarios, including objects with inheritance relationships, circular-referenced objects, and anonymous objects that implement interfaces.

---------

## Proven Effectiveness
Fixture Monkey was originally developed as an in-house library at [Naver](https://www.navercorp.com/en) and played a crucial role in simplifying test object generation for the Plasma project.
The Plasma project aimed to revolutionize Naver Pay's architecture, which is the most used mobile payment service in South Korea with a daily active user count of 261,400.

The project required thorough testing of complex business requirements, and with Fixture Monkey's assistance, the team efficiently wrote over 10,000 tests, uncovering critical edge cases and ensuring the system's reliability.
Now available as an open-source library, developers worldwide can take advantage of Fixture Monkey to simplify their test codes and build robust applications with confidence.


