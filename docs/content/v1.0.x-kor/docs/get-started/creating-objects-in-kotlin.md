---
title: "Kotlin 환경에서 테스트 객체 생성하기"
weight: 26
menu:
docs:
parent: "get-started"
identifier: "creating-objects-in-kotlin"
---

Fixture Monkey 는 Kotlin 으로 작성된 클래스를 생성하는 것도 지원합니다.

먼저 `fixture-monkey-starter-kotlin` 의존성을 추가했는지 확인하세요.

그런 다음 Kotlin 플러그인을 추가하여, Fixture Monkey 의 Kotlin 지원 기능을 활성화 할 수 있습니다.

```kotlin
@Test
fun test() {
  val fixtureMonkey = FixtureMonkey.builder()
      .plugin(KotlinPlugin())
      .build()
}
```

Kotlin 플러그인은 기본 `ObjectIntrospector` 를 `PrimaryConstructorArbitraryIntrospector` 로 변경합니다.
이는 Kotlin 클래스의 기본 생성자를 사용하여 객체를 생성합니다.

다음과 같은 Kotlin 클래스가 있다고 가정해보겠습니다.

```kotlin
data class Product (
  val id: Long,

  val productName: String,

  val price: Long,

  val options: List<String>,

  val createdAt: Instant,

  val productType: ProductType,

  val merchantInfo: Map<Integer, String>
)
```

Java 에서 했던 것처럼 Kotlin 클래스를 생성할 수 있습니다.

만약 Kotlin 으로 테스트 코드를 작성하고 있다면, 클래스 이름을 작성하지 않고 `giveMeOne()` 을 사용할 수 있습니다.
Java 에서는 `Product.class` 와 같이 클래스 이름을 작성해야 했지만요.

다음 코드는 Kotlin 환경에서 객체를 생성하는 방법을 보여줍니다.

```kotlin
@Test
fun test() {
    // given
    val fixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .build()

    // when
    val actual: Product = fixtureMonkey.giveMeOne()

    // then
    then(actual).isNotNull
}
```

또한 Kotlin 플러그인은 프로퍼티를 참조하여 객체를 사용자 지정할 수 있는 새로운 방법을 제공합니다.

```kotlin
@Test
fun test() {
  // given
  val fixtureMonkey = FixtureMonkey.builder()
      .plugin(KotlinPlugin())
      .build();

  // when
  val actual = fixtureMonkey.giveMeBuilder<Product>()
      .setExp(Product::id, 1000L)
      .sizeExp(Product::options, 3)
      .setExp(Product::options[1], "red")
      .sample()

  // then
  then (actual.id).isEqualTo(1000L)
  then (actual.options).hasSize(3)
  then (actual.options[1]).isEqualTo("red")
}
```

`set()` 메서드 대신 `setExp()` 메서드를 사용하면 Kotlin 의 프로퍼티 참조 구문을 사용하여 프로퍼티 할당을 지정할 수 있습니다.
[Kotlin Plugin](../../plugins/kotlin-plugin/features) 페이지에서 Kotlin 플러그인이 제공하는 기능에 대해 자세히 알아볼 수 있습니다.
