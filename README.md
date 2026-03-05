# Fixture Monkey
![Maven version](https://maven-badges.herokuapp.com/maven-central/com.navercorp.fixturemonkey/fixture-monkey/badge.svg)
[![Build](https://github.com/naver/fixture-monkey/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/naver/fixture-monkey/actions/workflows/build.yml)
[![GitHub license](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/naver/fixture-monkey/blob/main/LICENSE)

<figure align="center">
    <img src= "https://user-images.githubusercontent.com/10272119/227154042-b43ab281-ac73-4648-ba8f-7f2146cde6d5.png" width="100%"/>
    <figcaption>Designed by <a href="https://www.linkedin.com/in/seongin-hong">SeongIn Hong</a> </figcaption>
</figure>

### "Write once, Test anywhere"

Fixture Monkey is a Java & Kotlin library designed to generate controllable arbitrary test objects.
Its most distinctive feature is the ability to freely access and configure any nested fields through path-based expressions.

It focuses on simplifying test writing by facilitating the generation of necessary test fixtures.
Make your JVM tests more concise and safe with Fixture Monkey.

## Table of Contents
- [Quick Start](#quick-start)
- [Why use Fixture Monkey?](#why-use-fixture-monkey)
- [Real Test Example](#real-test-example)
- [Requirements](#requirements)
- [Documentation](#documentation)
- [Additional Resources](#additional-resources)
- [Contributors](#contributors)
- [License](#license)

## Quick Start

Add Fixture Monkey to your project:

### Gradle

```groovy
// Java
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter:1.1.17")

// Kotlin
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter-kotlin:1.1.17")
```

### Maven

```xml
<!-- Java -->
<dependency>
    <groupId>com.navercorp.fixturemonkey</groupId>
    <artifactId>fixture-monkey-starter</artifactId>
    <version>1.1.17</version>
    <scope>test</scope>
</dependency>

<!-- Kotlin -->
<dependency>
    <groupId>com.navercorp.fixturemonkey</groupId>
    <artifactId>fixture-monkey-starter-kotlin</artifactId>
    <version>1.1.17</version>
    <scope>test</scope>
</dependency>
```

Create your first test object:

```java
// Java
FixtureMonkey fixtureMonkey = FixtureMonkey.create();
Product product = fixtureMonkey.giveMeOne(Product.class);

// Kotlin
val fixtureMonkey = FixtureMonkey.create()
val product = fixtureMonkey.giveMeOne<Product>()
```

### Java Example

```java
@Test
void sampleOrder() {
    FixtureMonkey sut = FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .build();

    Order actual = sut.giveMeBuilder(Order.class)
            .set(javaGetter(Order::getOrderNo), "1")
            .set(javaGetter(Order::getProductName), "Line Sally")
            .minSize(javaGetter(Order::getItems), 1)
            .sample();

    then(actual.getOrderNo()).isEqualTo("1");
    then(actual.getProductName()).isEqualTo("Line Sally");
    then(actual.getItems()).hasSizeGreaterThanOrEqualTo(1);
}
```

### Kotlin Example

```kotlin
@Test
fun sampleOrder() {
    val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .build()

    val actual = sut.giveMeBuilder<Order>()
            .setExp(Order::orderNo, "1")
            .setExp(Order::productName, "Line Sally")
            .minSizeExp(Order::items, 1)
            .sample()

    then(actual.orderNo).isEqualTo("1")
    then(actual.productName).isEqualTo("Line Sally")
    then(actual.items).hasSizeGreaterThanOrEqualTo(1)
}
```

> Note: Add "lombok.anyConstructor.addConstructorProperties=true" in lombok.config when using Lombok with Fixture Monkey

## Why use Fixture Monkey?

### 1. One-Line Test Object Generation
```java
// Before: Manual object creation
Product product = new Product();
product.setId(1L);
product.setName("Test Product");
// ... many more setters

// After: With Fixture Monkey
Product product = fixtureMonkey.giveMeOne(Product.class);
```
Stop writing boilerplate code for test object creation. Generate any test object with a single line of code.

### 2. Intuitive Path-Based Configuration
```java
// Set all product names to "Special Product" with a single expression
ArbitraryBuilder<Order> orderBuilder = fixtureMonkey.giveMeBuilder(Order.class)
    .set("items[*].product.name", "Special Product");
```
Bid farewell to endless getter/setter chains. Path expressions let you configure any nested field with a single line.

### 3. Reusable Test Specifications
```java
// Define once, reuse everywhere
ArbitraryBuilder<Product> productBuilder = fixtureMonkey.giveMeBuilder(Product.class)
    .set("category", "Book")
    .set("price", 1000);

// Reuse in different tests
Product product1 = productBuilder.sample();  
Product product2 = productBuilder.size("reviews", 3).sample();
```
Eliminate test code duplication by defining specifications once and reusing them across your test suite.

### 4. Universal Object Generation
```java
// Handles inheritance, circular references, and complex structures
Foo foo = FixtureMonkey.create().giveMeOne(Foo.class);  // even with circular references
Bar bar = FixtureMonkey.create().giveMeOne(Bar.class);  // even with inheritance
```
From simple POJOs to complex object graphs, Fixture Monkey handles all object structures.

### 5. Dynamic Test Data
```java
// Each sample generates unique test data
Product sample1 = fixtureMonkey.giveMeBuilder(Product.class).sample();
Product sample2 = fixtureMonkey.giveMeBuilder(Product.class).sample();
assertThat(sample1).isNotEqualTo(sample2);
```
Move beyond static test data to discover edge cases that static data might miss.

## Real Test Example
```java
@Test
void testOrderProcessing() {
    // Given
    Order order = fixtureMonkey.giveMeBuilder(Order.class)
        .set("items[*].quantity", 2)
        .set("items[*].product.price", 1000)
        .sample();
    
    // When
    OrderResult result = new OrderProcessor().process(order);
    
    // Then
    assertThat(result.getTotalAmount()).isEqualTo(4000); // 2 items * 2 quantity * 1000 price
    assertThat(result.getStatus()).isEqualTo(OrderStatus.COMPLETED);
}
```

## Requirements

* JDK 1.8 or higher
* Jqwik 1.7.3
* Kotlin 1.8 or higher (for Kotlin support)
* kotest-property 5.9.1 (for Kotlin support)

## Battle-Tested in Production
Originally developed at [Naver](https://www.navercorp.com/en), Fixture Monkey has proven its reliability in handling complex business requirements at scale, supporting over 10,000 tests for South Korea's leading mobile payment service.

## Documentation
* [English](https://naver.github.io/fixture-monkey)
* [Korean](https://naver.github.io/fixture-monkey/ko/)

## Additional Resources

### [FAQ](https://naver.github.io/fixture-monkey/docs/cheat-sheet/faq/)

### [Third-party Modules](https://naver.github.io/fixture-monkey/docs/plugins/)

### Tools and Plugins
* [FixtureMonkey Helper](https://plugins.jetbrains.com/plugin/19589-fixturemonkey-helper) - IntelliJ plugin for easier Fixture Monkey usage

### Articles
* [fixure monkey로 예외 발생 테스트](https://yangbongsoo.tistory.com/68?category=982054)
* [테스트 객체를 더쉽게 만들어보자, Fixture-monkey](https://taes-k.github.io/2021/12/12/fixture-monkey/)
* [Junit Test with Fixture Monkey](https://kevin-park.medium.com/junit-test-with-fixture-monkey-ca50f6533385)
* [Fixture monkey](https://leeheefull.tistory.com/m/27)
* [테스트 데이터도구 - Fixture Monkey](https://jiwondev.tistory.com/272)
* [Fixture Monkey란?](https://velog.io/@pang_e/Fixture-Monkey%EB%9E%80)
* [테스트를 작성할 수 밖에 없는 사람들에게](https://brunch.co.kr/@seongside/3)
* [Fixture Monkey 사용해보기](https://codinghejow.tistory.com/419)
* [Simplify Unit Testing with Fixture Monkey](https://medium.com/naver-platform-labs/simplify-unit-testing-with-fixture-monkey-a-concise-pragmatic-and-interoperable-fixture-library-3bf0206258d4)
* [Getting Started Easy Test Fixture Customization with Fixture Monkey](https://medium.com/naver-platform-labs/easy-test-fixture-customization-with-fixture-monkey-4114c6b4b1ef)
* [[Fixture Monkey] 픽스쳐 몽키로 테스트 코드 작성하기 (Java Spring)](https://sunshower99.tistory.com/33)
* [TestFixture를 쉽게 생성해 주는 라이브러리가 있다?](https://oliveyoung.tech/blog/2024-04-01/testcode-use-fixture-monkey/)
* [Fixture Monkey로 테스트 픽스처를 쉽게 생성하고 리팩토링 해보자](https://jxmen.github.io/wiki/project/cs-ai-interviewer/refactoring-with-fixture-monkey/)
* [Fixture Monkey를 적용해보자 w/JPA Test](https://currenjin.github.io/wiki/fixture-monkey-with-jpa/#fixture-monkey%EB%A5%BC-%EC%A0%81%EC%9A%A9%ED%95%B4%EB%B3%B4%EC%9E%90)
* [[Fixture Monkey] 불변 객체를 위한 사용자 정의 인트로스펙터 예제 코드](https://the0.tistory.com/93)

Welcome to write articles about Fixture Monkey!
Please make an issue to let us know if you'd like to share your post.

## Contributors
* 🐒 [ah.jo](https://github.com/seongside)
* 🐒 [mhyeon-lee](https://github.com/mhyeon-lee)
* 🐒 [acktsap](https://github.com/acktsap)
* 🐒 [benelog](https://github.com/benelog)
* 🐒 [jwChung](https://github.com/jwChung)
* 🐒 [SooKim1110](https://github.com/SooKim1110)

Thanks to all [contributors](https://github.com/naver/fixture-monkey/graphs/contributors)

## License

```
Copyright 2021-present NAVER Corp.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
