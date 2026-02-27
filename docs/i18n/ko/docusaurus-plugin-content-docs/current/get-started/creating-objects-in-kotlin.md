---
title: "Kotlin 환경에서 테스트 객체 생성하기"
sidebar_position: 24
---


Fixture Monkey를 사용하면 Kotlin 클래스의 테스트 객체를 쉽게 생성할 수 있습니다. 예를 들어, 다음과 같은 Kotlin 데이터 클래스가 있다고 해보겠습니다:

```kotlin
data class Product (
    val id: Long,
    val productName: String,
    val price: Long,
    val options: List<String>,
    val createdAt: Instant,
    val productType: ProductType,
    val merchantInfo: Map<Int, String>
)
```

Fixture Monkey를 사용하면 이 클래스의 테스트 인스턴스를 단 한 줄의 코드로 생성할 수 있습니다:

```kotlin
val product: Product = fixtureMonkey.giveMeOne()
```

생성된 객체는 각 필드 타입에 맞는 적절한 임의의 값을 포함하게 됩니다. 다음은 생성될 수 있는 객체의 예시입니다:

```kotlin
Product(
    id=42,
    productName="product-value-1",
    price=1000,
    options=["option1", "option2"],
    createdAt=2024-03-21T10:15:30Z,
    productType=ELECTRONICS,
    merchantInfo={1="merchant1", 2="merchant2"}
)
```

Kotlin에서 Fixture Monkey를 시작하려면 다음 단계를 따르세요:

1. 프로젝트에 `fixture-monkey-starter-kotlin` 의존성을 추가합니다.

2. Kotlin 플러그인을 사용하여 `FixtureMonkey` 인스턴스를 생성합니다:
```kotlin
@Test
fun test() {
    val fixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .build()
}
```

Kotlin 플러그인은 Fixture Monkey가 Kotlin의 기능들과 함께 작동하도록 해주며, 기본 생성자를 사용하여 객체를 생성합니다.

다음은 전체 테스트 예제입니다:

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

또한 Kotlin의 프로퍼티 참조를 사용하여 생성되는 객체를 원하는 대로 수정할 수 있습니다:

```kotlin
@Test
fun test() {
    // given
    val fixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .build();

    // when
    val actual = fixtureMonkey.giveMeKotlinBuilder<Product>()
        .set(Product::id, 1000L)            // 특정 id 설정
        .size(Product::options, 3)          // options 리스트 크기 설정
        .set(Product::options[1], "red")    // 특정 option 설정
        .sample()

    // then
    then(actual.id).isEqualTo(1000L)
    then(actual.options).hasSize(3)
    then(actual.options[1]).isEqualTo("red")
}
```

Kotlin 전용 기능에 대해 더 자세히 알아보려면 [Kotlin 플러그인](../plugins/kotlin-plugin/features) 문서를 참고하세요.

