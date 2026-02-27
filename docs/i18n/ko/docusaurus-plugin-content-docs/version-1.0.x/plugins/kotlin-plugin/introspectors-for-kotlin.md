---
title: "Kotlin 지원 Introspector"
sidebar_position: 62
---


Fixture Monkey 는 Kotlin 클래스를 생성하기 위한 추가적인 introspector 들을 제공합니다.

## PrimaryConstructorArbitraryIntrospector

`PrimaryConstructorArbitraryIntrospector` 는 코틀린 플러그인이 추가되면 자동으로 기본 introspector 로 설정됩니다.
이 introspector 는 주 생성자를 기반으로 Kotlin 클래스를 생성합니다.

**예제 Kotlin 클래스:**
```kotlin
data class Product (
  val id: Long?,

  val productName: String,

  val price: Long,

  val options: List<String>,

  val createdAt: Instant
)
```

**PrimaryConstructorArbitraryIntrospector 를 이용해 픽스쳐 생성:**
```kotlin
@Test
fun test() {
  val fixtureMonkey = FixtureMonkey.builder()
      .plugin(KotlinPlugin())
      .build()

  val product: Product = fixtureMonkey.giveMeOne()
}
```

