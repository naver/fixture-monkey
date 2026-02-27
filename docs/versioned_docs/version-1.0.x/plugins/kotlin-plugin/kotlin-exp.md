---
title: "Kotlin DSL Exp"
sidebar_position: 63
---


Fixture Monkey utilizes Kotlin's DSL feature to ensure type-safety with expressions.
Let's explore how we can employ Kotlin Exp instead of the standard [Java String Expression](../../customizing-objects/expressions).

### Referencing a property

Suppose we have an object structure similar to the one described earlier, written in both Java and Kotlin:

```java
@Value
public class JavaClass {
    String field;

    List<String> list;

    Nested nestedObject;

    List<Nested> nestedObjectList;

    @Value
    public static class Nested {
        String nestedField;
    }
}
```

```kotlin
data class KotlinClass(
  val field: String,

  val list: List<String>,

  val nestedObject: Nested,

  val nestedObjectList: List<Nested>
) {
  data class Nested(
    val nestedField: String
  )
}
```

To use Kotlin Exp to reference a property, you need to use the `Exp` or `ExpGetter` suffix to the normal [Fixture Customization APIs](../../customizing-objects/apis).

Let's look at the example of customizing properties with Kotlin Exp using `setExp()` and `setExpGetter()`.

```kotlin
@Test
fun test() {
    // given
    val fixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .plugin(JacksonPlugin())
        .build()

    // when
    val javaClass = fixtureMonkey.giveMeBuilder<JavaClass>()
        .setExpGetter(JavaClass::getField, "field")
        .sample()

    val kotlinClass = fixtureMonkey.giveMeBuilder<KotlinClass>()
        .setExp(KotlinClass::field, "field")
        .sample()

    // then
    then(javaClass.field).isEqualTo("field")
    then(kotlinClass.field).isEqualTo("field")
}
```

In the code above, we can see that we are using Kotlin's method reference to select a property.

`setExp()` takes an argument of type `KProperty`, while `setExpGetter()` takes an argument of type `KFunction`.

If the class is defined in Java, the expression (e.g. JavaClass::getField) is of type `KFunction` because it is a reference to a Java getter.
Therefore you can only use the `setExpGetter()` method.

If it is a Kotlin class, the expression (e.g. KotlinClass::field) is `KProperty`, so you should use `setExp()`.

### Referencing a nested property

To access a nested field, the infix functions `into` and `intoGetter` are used.
`into` takes a parameter of type `KProperty`, while `intoGetter` takes a parameter of type `KFunction`.

```kotlin
@Test
fun test() {
    // given
    val fixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .plugin(JacksonPlugin())
        .build()

    // when
    val javaClass = fixtureMonkey.giveMeBuilder<JavaClass>()
        .setExp(JavaClass::getNestedObject intoGetter JavaClass.Nested::getNestedField, "nestedField")
        .sample()

    val kotlinClass = fixtureMonkey.giveMeBuilder<KotlinClass>()
        .setExp(KotlinClass::nestedObject into KotlinClass.Nested::nestedField, "nestedField")
        .sample()

    then(javaClass.nestedObject.nestedField).isEqualTo("nestedField")
    then(kotlinClass.nestedObject.nestedField).isEqualTo("nestedField")
}
```

An expression that contains an into or intoGetter operator becomes an `ExpressionGenerator` type in fixture monkey.
Both `setExp()` and `setExpGetter()`) are defined to take ExpressionGenerator types as arguments, so you can use both.

------------

### Selecting Properties Using Kotlin DSL Expressions

##### Selecting the root object:
- Currently Not Supported

##### Selecting a specific field:
```kotlin
JavaClass::getField // java class

KotlinClass::field // kotlin class
```

##### Selecting a nested field:
```kotlin
JavaClass::getNestedObject intoGetter JavaClass.Nested::getNestedField // java class

KotlinClass::nestedObject into KotlinClass.Nested::nestedField // kotlin class
```

##### Selecting the n-th element of a collection:
```kotlin
JavaClass::getNestedObjectList["0"] // java class

KotlinClass::nestedObjectList["0"] // kotlin class
```

##### Selecting all elements of a collection:
```kotlin
JavaClass::getNestedObjectList["*"] // java class

KotlinClass::nestedObjectList["*"] // kotlin class
```

##### Combining expressions to select a nested field:
```java
JavaClass::getNestedObject intoGetter JavaClass.Nested::getNestedField // java class

KotlinClass::nestedObject into KotlinClass.Nested::nestedField // kotlin class
```


