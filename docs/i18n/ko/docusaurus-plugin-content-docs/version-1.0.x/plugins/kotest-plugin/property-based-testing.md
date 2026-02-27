---
title: "Kotest 프로퍼티 기반 테스트"
sidebar_position: 92
---


Fixture Monkey의 Kotest 플러그인은 [Kotest 프레임워크의 property-based 테스트](https://kotest.io/docs/proptest/property-test-functions.html)의 2가지 주요 기능인 `forAll`과 `checkAll`을 강화할 수 있는 기능을 제공합니다.

이 기능을 활성화하려면 KotestPlugin과 KotlinPlugin을 추가해야 합니다.
```kotlin
val fixtureMonkey: FixtureMonkey = Fixture
    .plugin(KotestPlugin())
    .plugin(KotlinPlugin())
    .build()
```

## ForAll
Kotest는 `(a, ..., n) -> Boolean` 형식의 n-arity 함수를 받아 프로퍼티를 테스트하는 `forAll` 함수를 제공합니다.
모든 입력 값에 대해 함수가 true를 반환하면 테스트가 통과합니다.

이 함수는 타입 매개변수를 받고, 이를 사용하여 Kotest가 적절한 타입의 랜덤 값을 제공하는 생성기를 찾습니다.

```kotlin
class PropertyExample: StringSpec({
   "String size" {
      forAll<String, String> { a, b ->
         (a + b).length == a.length + b.length
      }
   }
})
```

커스텀 생성기가 필요한 경우 Kotest에서는 `Arb` 생성기를 지정할 수 있습니다.
Kotest에서는 제한된 유형의 생성기만 제공되며 커스텀하기 어렵습니다.

Fixture Monkey는 `giveMeArb()` 함수를 사용하여 커스텀 타입에 대한 `Arb`를 생성하는 방법을 제공합니다.
Fixture Monkey의 커스텀 API를 사용하여 생성기를 한층 더 커스텀할 수 있습니다.

다음은 Fixture Monkey를 통해 `forAll`을 사용한 프로퍼티 기반 테스트의 예제 입니다.

```kotlin
class KotestInKotestTest : StringSpec({
    "forAll" {
        forAll(fixtureMonkey.giveMeArb<StringObject> { it.set("value", "test") }) { a ->
            a.value == "test"
        }
    }
}) {
    data class StringObject(val value: String)
}
```

## CheckAll
Fixture Monkey는 Kotest의 `checkAll`과 유사한 확장 함수 `checkAll`을 제공합니다.

### 기본 타입 입력 값
checkAll을 사용하면 아래 예제와 같이 기본 데이터 타입에 대한 테스트를 검증할 수 있습니다.

```kotlin
class Test : StringSpec({
    "checkAll" {
        SUT.checkAll { string: String, int: Int ->
            string shouldNotBeSameInstanceAs int
            string shouldBe string
        }
    }
})
```

### 커스텀 타입 입력 값
Fixture Monkey의 `checkAll` 확장 함수를 이용하면 기본 타입을 생성하는 것 뿐만 아니라 사용자 정의 타입들도 입력 데이터로 사용할 수 있습니다.

```kotlin
class Test : StringSpec({
    "checkAllObject" {
        SUT.checkAll { stringObject: StringObject ->
            stringObject.value shouldNotBe null
        }
    }
}) {
    data class StringObject(val value: String)
}
```

### ArbitraryBuilder 입력 값

또한 `ArbitraryBuilder` 인스턴스를 활용하여 더 세부적인 커스터마이징을 통해 검증을 수행할 수 있습니다.

```kotlin
class Test : StringSpec({
    "checkAllArbitraryBuilder" {
        SUT.checkAll { string: ArbitraryBuilder<List<String>> ->
            string
                .size("$", 3)
                .sample() shouldHaveSize 3
        }
    }
}) {
    data class StringObject(val value: String)
}
```

