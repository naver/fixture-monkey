---
title: "Features"
sidebar_position: 111
---


The MockK plugin generates [MockK](https://mockk.io/) mocks inside the objects Fixture Monkey creates.
It is the Kotlin counterpart of `fixture-monkey-mockito`.
- Interfaces and abstract classes that Fixture Monkey cannot instantiate become MockK mocks
- Selected types can always be generated as mocks, including final Kotlin classes
- Stubbing written with `every { ... }` can be applied to every generated mock of a type

## Dependencies
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

## Plugin Setup
```kotlin
val fixtureMonkey = FixtureMonkey.builder()
    .plugin(KotlinPlugin())
    .plugin(MockkPlugin())
    .build()
```

## Mocking interfaces and abstract classes
When a property's type is an interface or an abstract class and Fixture Monkey has no implementation to use,
the plugin generates a MockK mock for it. Every sample gets a new mock, so stubbing in one test does not leak into another.

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

Implementations registered with `InterfacePlugin` and sealed subclasses resolved by `KotlinPlugin` are still used first.
The plugin only takes over types that would otherwise fail to generate.

## Always mocking a type
Use `mock<T>()` to generate a type as a mock even if it is a concrete class. This works for final Kotlin classes too,
which is useful for clients that should never run for real in a test.

```kotlin
class PricingClient(private val baseUrl: String) {
    fun price(sku: String): Long = TODO("calls a remote service")
}

val fixtureMonkey = FixtureMonkey.builder()
    .plugin(KotlinPlugin())
    .plugin(MockkPlugin().mock<PricingClient>())
    .build()
```

## Stubbing every generated mock
`mock<T>` takes an optional block that runs on each new mock of `T`, so you can declare answers once for all fixtures.

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

val orders = fixtureMonkey.giveMe<Order>(3) // every gateway answers "approved-<amount>"
```

A non-reified variant, `mock(PricingClient::class) { ... }`, is also available.

A value set with `set` always wins over a generated mock.

## Relaxed and strict mocks
Generated mocks are relaxed by default, so unstubbed calls return default values (such as `0`, `""` and `false`)
instead of throwing. This matches how mocks behave in `fixture-monkey-mockito`.

To fail fast on unexpected calls, turn relaxing off. `relaxUnitFun(true)` keeps functions returning `Unit` relaxed.

```kotlin
MockkPlugin()
    .relaxed(false)
    .relaxUnitFun(true)
```
