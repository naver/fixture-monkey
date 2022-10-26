---
title: "Fixture Monkey Kotlin"
linkTitle: "Fixture Monkey Kotlin"
weight: 2
---

- Supports Kotlin.
- `KFixtureMonkey.create()` / `KFixtureMonkeyBuilder().build()` for kotlin module.
- `PrimaryConstructorArbitraryGenerator` for kotlin primary constructor. (default generator)
- Extension methods for kotlin. [see](https://github.com/naver/fixture-monkey/blob/main/fixture-monkey-kotlin/src/main/kotlin/com/navercorp/fixturemonkey/kotlin/FixtureMonkeyExtensions.kt)

## Installation
### Gradle
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-kotlin:0.3.1")
```

### Maven
```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-kotlin</artifactId>
  <version>0.3.1</version>
  <scope>test</scope>
</dependency>
```

### Try it out!
```kotlin
data class Order(
	val id: Long,
	val productName: String,
	val quantity: Int,
	var sample: String?
)

@Test
fun test() {
	// given
    val sut = KFixtureMonkey.create()

    // when
    val actual = sut.giveMeOne<Order>()
    
    // then
    // ...
}
```
