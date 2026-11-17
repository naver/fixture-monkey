---
title: "기능"
sidebar_position: 111
---


MockK 플러그인은 Fixture Monkey가 생성하는 객체 안에 [MockK](https://mockk.io/) mock을 생성합니다.
`fixture-monkey-mockito`의 Kotlin 버전입니다.
- Fixture Monkey가 생성할 수 없는 인터페이스와 추상 클래스를 MockK mock으로 생성합니다.
- 지정한 타입은 final Kotlin 클래스를 포함해 항상 mock으로 생성할 수 있습니다.
- `every { ... }`로 작성한 stubbing을 해당 타입으로 생성되는 모든 mock에 적용할 수 있습니다.

## 의존성
#### Gradle
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-mockk:{{fixtureMonkeyVersion}}")
```

#### Maven
```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-mockk</artifactId>
  <version>{{fixtureMonkeyVersion}}</version>
  <scope>test</scope>
</dependency>
```

## 플러그인 설정
```kotlin
val fixtureMonkey = FixtureMonkey.builder()
    .plugin(KotlinPlugin())
    .plugin(MockkPlugin())
    .build()
```

## 인터페이스와 추상 클래스 mock 생성
프로퍼티 타입이 인터페이스나 추상 클래스이고 사용할 구현체가 없으면 플러그인이 MockK mock을 생성합니다.
샘플마다 새로운 mock이 생성되므로 한 테스트의 stubbing이 다른 테스트에 영향을 주지 않습니다.

```kotlin
interface PaymentGateway {
    fun charge(amount: Long): String
}

data class Order(val id: String, val gateway: PaymentGateway)

val order = fixtureMonkey.giveMeOne<Order>()

every { order.gateway.charge(any()) } returns "approved"
order.gateway.charge(1_000L) // "approved"
verify { order.gateway.charge(1_000L) }
```

`InterfacePlugin`에 등록한 구현체와 `KotlinPlugin`이 찾은 sealed 하위 클래스가 먼저 사용됩니다.
플러그인은 그 외에 생성에 실패하는 타입만 처리합니다.

## 특정 타입을 항상 mock으로 생성
`mock<T>()`를 사용하면 구체 클래스도 mock으로 생성합니다. final Kotlin 클래스도 지원하므로
테스트에서 실제로 호출되면 안 되는 클라이언트에 유용합니다.

```kotlin
class PricingClient(private val baseUrl: String) {
    fun price(sku: String): Long = TODO("calls a remote service")
}

val fixtureMonkey = FixtureMonkey.builder()
    .plugin(KotlinPlugin())
    .plugin(MockkPlugin().mock<PricingClient>())
    .build()
```

## 생성되는 모든 mock에 stubbing 적용
`mock<T>`에 블록을 전달하면 `T`의 mock이 생성될 때마다 실행되므로 응답을 한 번만 선언하면 됩니다.

```kotlin
val fixtureMonkey = FixtureMonkey.builder()
    .plugin(KotlinPlugin())
    .plugin(
        MockkPlugin()
            .mock<PaymentGateway> {
                every { charge(any()) } answers { "approved-${firstArg<Long>()}" }
            }
            .mock<PricingClient> {
                every { price(any()) } returns 1_000L
            }
    )
    .build()

val orders = fixtureMonkey.giveMe<Order>(3) // 모든 gateway가 "approved-<amount>"를 반환
```

reified가 아닌 `mock(PricingClient::class) { ... }`도 사용할 수 있습니다.

`set`으로 지정한 값은 항상 생성된 mock보다 우선합니다.

## relaxed mock과 strict mock
생성되는 mock은 기본적으로 relaxed이므로 stubbing하지 않은 호출은 예외 대신 기본값(`0`, `""`, `false` 등)을 반환합니다.
`fixture-monkey-mockito`의 mock과 같은 동작입니다.

예상하지 못한 호출에서 바로 실패하게 하려면 relaxed를 끄면 됩니다. `relaxUnitFun(true)`를 사용하면 `Unit`을 반환하는 함수는 relaxed로 유지됩니다.

```kotlin
MockkPlugin()
    .relaxed(false)
    .relaxUnitFun(true)
```
