---
title: "기능"
sidebar_position: 91
---


Fixture Monkey에서 제공하는 Kotest 플러그인을 사용하면 더욱 향상된 테스트를 경험할 수 있습니다.
- 기본 타입의 랜덤 값을 생성하는 기본 생성기를 Jqwik에서 Kotest의 프로퍼티 생성기(`Arb`)로 대체합니다. 빈(bean) 검증 어노테이션도 사용할 수 있습니다.
- `forAll`, `checkAll`을 포함한 Kotest의 [property-based 테스트](https://kotest.io/docs/proptest/property-test-functions.html)를 지원합니다.

:::tip
Kotest 플러그인 추가 후 반드시 Kotest를 사용해야 하는 것은 아니며, JUnit을 사용할 수 있습니다.
:::

## 의존성
##### fixture-monkey-kotest
#### Gradle
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-kotest:1.1.16")
```

#### Maven
```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-kotest</artifactId>
  <version>1.1.16</version>
  <scope>test</scope>
</dependency>
```

## 플러그인
```kotlin
val fixtureMonkey = FixtureMonkey.builder()
    .plugin(KotestPlugin())
    .plugin(KotlinPlugin())
    .build()
```

## 사용 예제

### 기본 객체 생성

```kotlin
data class Product(val name: String, val price: Int)

val product: Product = fixtureMonkey.giveMeOne()
```

### Kotest property-based 테스트

`giveMeArb()`를 사용하면 Kotest의 `Arb` 생성기와 통합하여 property-based 테스트를 작성할 수 있습니다:

```kotlin
class ProductTest : StringSpec({
    val fixtureMonkey = FixtureMonkey.builder()
        .plugin(KotestPlugin())
        .plugin(KotlinPlugin())
        .build()

    "price should be positive" {
        checkAll(fixtureMonkey.giveMeArb<Product>()) { product ->
            product.price shouldBeGreaterThan 0
        }
    }
})
```

### Bean 검증 어노테이션 지원

```kotlin
data class User(
    @field:NotBlank val name: String,
    @field:Min(0) val age: Int
)

val fixtureMonkey = FixtureMonkey.builder()
    .plugin(KotestPlugin())
    .plugin(KotlinPlugin())
    .plugin(JakartaValidationPlugin())
    .build()

val user: User = fixtureMonkey.giveMeOne()
// name은 빈 문자열이 아니고, age는 0 이상
```

## KotestPlugin vs JqwikPlugin

| 기능 | KotestPlugin | JqwikPlugin |
|------|-------------|-------------|
| 기본 타입 생성 엔진 | Kotest `Arb` | Jqwik `Arbitrary` |
| property-based 테스트 | `checkAll`, `forAll` | Jqwik `@Property` |
| Kotlin 친화적 | 네이티브 Kotlin | Java 기반 |
| 검증 어노테이션 | 지원 (별도 플러그인) | 지원 (별도 플러그인) |
