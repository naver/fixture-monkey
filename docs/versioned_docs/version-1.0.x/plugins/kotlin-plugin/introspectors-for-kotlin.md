---
title: "Introspectors for Kotlin"
sidebar_position: 62
---


Fixture Monkey provides some additional introspectors that support the generation of Kotlin classes.

## PrimaryConstructorArbitraryIntrospector

The `PrimaryConstructorArbitraryIntrospector` becomes the default introspector when the Kotlin plugin is added.
It creates a Kotlin class with its primary constructor.

**Example Kotlin Class :**
```kotlin
data class Product (
  val id: Long?,

  val productName: String,

  val price: Long,

  val options: List<String>,

  val createdAt: Instant
)
```

**Using PrimaryConstructorArbitraryIntrospector :**
```kotlin
@Test
fun test() {
  val fixtureMonkey = FixtureMonkey.builder()
      .plugin(KotlinPlugin())
      .build()

  val product: Product = fixtureMonkey.giveMeOne()
}
```

## KotlinAndJavaCompositeArbitraryIntrospector

The `KotlinAndJavaCompositeArbitraryIntrospector` is an introspector designed to assist in the creation of Kotlin classes that reference Java classes.


**Example Kotlin Class :**
```kotlin
class KotlinClassWithJavaClass(val javaObject: JavaObject)
```

**Example Java Class :**
```java
public class JavaObject {
    private String value;
    private Map<String, String> map;

    public JavaObject() {
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }
}
```

**Using PrimaryConstructorArbitraryIntrospector :**
```kotlin
    fun kotlinClassWithJavaClass() {
        // given
        val sut: FixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .objectIntrospector(KotlinAndJavaCompositeArbitraryIntrospector())
            .build()

        // when
        val actual = sut.giveMeOne<KotlinClassWithJavaClass>()

        then(actual).isNotNull
        then(actual.javaObject).isNotNull
    }
```

For Kotlin and Java classes respectively, it uses the PrimaryConstructorArbitraryIntrospector and the BeanArbitraryIntrospector by default.

If changes are desired, these can be injected as arguments.

```kotlin
    // given
    val sut: FixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .objectIntrospector(
            KotlinAndJavaCompositeArbitraryIntrospector(
                kotlinArbitraryIntrospector = PrimaryConstructorArbitraryIntrospector.INSTANCE,
                javaArbitraryIntrospector = ConstructorPropertiesArbitraryIntrospector.INSTANCE
            )
        )
        .build()
```

