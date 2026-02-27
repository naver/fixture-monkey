---
title: "Kotlin 지원 Introspector"
sidebar_position: 62
---


Fixture Monkey 는 Kotlin 클래스를 생성하기 위한 추가적인 introspector 들을 제공합니다.

## PrimaryConstructorArbitraryIntrospector

`PrimaryConstructorArbitraryIntrospector` 는 코틀린 플러그인이 추가되면 자동으로 기본 introspector 로 설정됩니다.
이 introspector 는 주 생성자를 기반으로 Kotlin 클래스를 생성합니다. 

`PrimaryConstructorArbitraryIntrospector`를 사용하면 `코틀린 생성자의 파라미터` 정보만 생성합니다. `ArbitraryBuilder` API를 사용하면 `코틀린 생성자의 파라미터`만 변경할 수 있습니다.

`pushArbitraryIntrospector` 옵션을 사용해서 `PrimaryConstructorArbitraryIntrospector`를 사용하지 않게 되면 `코틀린 생성자의 파라미터`는 물론 `필드`와 `게터` 정보를 같이 생성합니다.
따라서 부모 클래스의 `필드`, `게터` 정보도 모두 가지고 있습니다. 이때는 `ArbitraryBuilder` API를 사용하면 가지고 있는 정보를 모두 변경 가능합니다.

예를 들어, `KotlinPlugin`을 적용한 후에 `JacksonPlugin`을 적용하면 Jackson으로 코틀린 객체를 생성할 수 있습니다. (순서 의존성이 있습니다.) 
Jackson으로 생성하는 경우에는 부모의 필드도 변경이 가능합니다.

 

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

