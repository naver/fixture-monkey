# Fixture Monkey
![Maven version](https://maven-badges.herokuapp.com/maven-central/com.navercorp.fixturemonkey/fixture-monkey/badge.svg)
[![Build](https://github.com/naver/fixture-monkey/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/naver/fixture-monkey/actions/workflows/build.yml)
[![GitHub license](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/naver/fixture-monkey/blob/main/LICENSE)

<figure align="center">
    <img src= "https://user-images.githubusercontent.com/10272119/227154042-b43ab281-ac73-4648-ba8f-7f2146cde6d5.png" width="100%"/>
    <figcaption>Designed by <a href="https://www.linkedin.com/in/seongin-hong">SeongIn Hong</a> </figcaption>
</figure>


### "Write once, Test anywhere"

Fixture Monkey is designed to easily generate fully-customizable, randomly populated instance. It allows you to focus on the properties of the class that really matter in your test.

It can help you write deterministic tests by generating a random instance of a class with specific property values.
Focus on what you really matters in your test, and let Fixture Monkey handle the rest.

It is a good choice to support both DRY (Don't Repeat Yourself) and DAMP (Descriptive And Meaningful Phrases) principles in your test code.

It is interoperable with almost all test frameworks and libraries, including JUnit, TestNG, Kotest. It also supports Java and Kotlin.
Each primitive type property is generated by [Jqwik](https://github.com/jlink/jqwik) or [kotest-property](https://github.com/kotest/kotest).


## Requirements

* JDK 1.8 or higher
* Jqwik 1.7.3
* Kotlin 1.8 or higher
* kotest-property 5.9.1

## Install

### Gradle

#### Java

```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter:1.1.8")
```

#### Kotlin

```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter-kotlin:1.1.8")
```

### Maven

#### Java

```xml
<dependency>
    <groupId>com.navercorp.fixturemonkey</groupId>
    <artifactId>fixture-monkey-starter</artifactId>
    <version>1.1.8</version>
    <scope>test</scope>
</dependency>
```

#### Kotlin

```xml
<dependency>
    <groupId>com.navercorp.fixturemonkey</groupId>
    <artifactId>fixture-monkey-starter-kotlin</artifactId>
    <version>1.1.8</version>
    <scope>test</scope>
</dependency>
```

## Example
> Add "lombok.anyConstructor.addConstructorProperties=true" in lombok.config

#### Java

```java
@Value
public class Order {
    Long id;

    String orderNo;

    String productName;

    int quantity;

    long price;

    List<String> items;

    Instant orderedAt;
}

@Test
void sampleOrder() {
    // given
    FixtureMonkey sut = FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .build();

    // when
    Order actual = sut.giveMeBuilder(Order.class)
            .set(javaGetter(Order::getOrderNo), "1")
            .set(javaGetter(Order::getProductName), "Line Sally")
            .minSize(javaGetter(Order::getItems), 1)
            .sample();

    // then
    then(actual.getOrderNo()).isEqualTo("1");
    then(actual.getProductName()).isEqualTo("Line Sally");
    then(actual.getItems()).hasSizeGreaterThanOrEqualTo(1);
}
```

#### Kotlin

```kotlin
data class Order (
    val id: Long,

    val orderNo: String,

    val productName: String,

    val quantity: Int,

    val price: Long,

    val items: List<String>,

    val orderedAt: Instant
)

@Test
fun sampleOrder() {
    // given
    val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .build()

    // when
    val actual = sut.giveMeBuilder<Order>()
            .setExp(Order::orderNo, "1")
            .setExp(Order::productName, "Line Sally")
            .minSizeExp(Order::items, 1)
            .sample()

    // then
    then(actual.orderNo).isEqualTo("1")
    then(actual.productName).isEqualTo("Line Sally")
    then(actual.items).hasSizeGreaterThanOrEqualTo(1)
}
```

## Documentation
* [English](https://naver.github.io/fixture-monkey)
* [Korean](https://naver.github.io/fixture-monkey/v1-1-0-kor/)

## [FAQ](https://naver.github.io/fixture-monkey/v1-0-0/docs/cheat-sheet/faq/)

## [Third-party Modules](https://naver.github.io/fixture-monkey/docs/plugins/)

## Plugins
* [FixtureMonkey Helper](https://plugins.jetbrains.com/plugin/19589-fixturemonkey-helper)
  - IntelliJ plugin that makes it easier to use Fixture Monkey string expressions & Kotlin DSL

## Contributors
* 🐒 [ah.jo](https://github.com/seongside)
* 🐒 [mhyeon-lee](https://github.com/mhyeon-lee)
* 🐒 [acktsap](https://github.com/acktsap)
* 🐒 [benelog](https://github.com/benelog)
* 🐒 [jwChung](https://github.com/jwChung)
* 🐒 [SooKim1110](https://github.com/SooKim1110)

Thanks to all [contributors](https://github.com/naver/fixture-monkey/graphs/contributors)

## More about Fixture Monkey
* [Deview 2021](https://tv.naver.com/v/23650158)

## Articles
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

Welcome to write articles about Fixture Monkey!
Please let us know if you'd like to share your post.

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
