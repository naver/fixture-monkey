---
title: "Kotlin DSL 표현식"
sidebar_position: 63
---


Fixture Monkey 는 코틀린의 DSL 기능을 이용해 표현식과 함께 타입 안정성을 보장합니다.
표준 [자바 문자열 표현식](../../customizing-objects/path-expressions) 대신 코틀린 표현식을 이용하는 방법을 알아보겠습니다.

### 프로퍼티 참조

앞서 설명한 것과 비슷한 객체 구조를 자바와 코틀린으로 모두 작성했다고 가정해보겠습니다.

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

코틀린 표현식을 사용해 프로퍼티를 참조하기 위해서는 일반 [Fixture Customization APIs](../../customizing-objects/apis) 에 `Exp` 나 `ExpGetter` 접미사를 사용합니다.

`setExp()` 와 `setExpGetter()` 를 사용해 코틀린 표현식으로 프로퍼티를 커스텀하는 예를 살펴보겠습니다.

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
위의 예제 코드에서 프로퍼티를 선택하기 위해 코틀린의 메서드 참조를 사용하고 있습니다.

`setExp()` 는 `KProperty` 타입을 인수로 받으며 `setExpGetter()` 는 `KFunction` 타입을 인수로 받습니다.

객체가 자바로 정의되어 있는 경우 JavaClass::getField 과 같은 표현식은 Java 의 getter 함수를 참조해야 하므로 `KFunction` 타입이 됩니다.
따라서 이 경우, `setExpGetter()` 메서드만 사용할 수 있습니다.

코틀린 객체의 경우 KotlinClass::field 와 같은 표현식은 `KProperty` 타입이므로 `setExp()` 메서드를 사용하여야 합니다.

### 중첩된 프로퍼티를 참조

중첩된 프로퍼티에 접근하기 위해 infix 함수인 `into` 와 `intoGetter` 를 사용할 수 있습니다.
`into` 함수는 `KProperty` 타입의 파라미터를 사용하며 `intoGetter` 함수는 `KFunction` 타입의 파라미터를 사용합니다.

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

Fixture Monkey 에서 into 나 intoGetter 연산자를 포함한 표현식은 `ExpressionGenerator` 타입으로 변경됩니다.
`setExp()` 혹은 `setExpGetter()` 함수는 모두 ExpressionGenerator 타입을 인수로 받도록 정의되어 있으므로 둘 다 사용할 수 있습니다.

------------

### 코틀린 DSL 표현식을 이용해 프로퍼티를 선택하기

##### 루트 객체를 선택:
- 현재는 지원되지 않습니다.

##### 특정 필드를 선택:
```kotlin
JavaClass::getField // java class

KotlinClass::field // kotlin class
```

##### 중첩 구조의 필드를 선택:
```kotlin
JavaClass::getNestedObject intoGetter JavaClass.Nested::getNestedField // java class

KotlinClass::nestedObject into KotlinClass.Nested::nestedField // kotlin class
```

##### 컬렉션의 n 번째 요소를 선택:
```kotlin
JavaClass::getNestedObjectList["0"] // java class

KotlinClass::nestedObjectList["0"] // kotlin class
```

##### 컬렉션의 전체 요소를 선택:
```kotlin
JavaClass::getNestedObjectList["*"] // java class

KotlinClass::nestedObjectList["*"] // kotlin class
```

##### 중첩 구조의 필드를 선택하기 위한 표현식 조합:
```java
JavaClass::getNestedObject intoGetter JavaClass.Nested::getNestedField // java class

KotlinClass::nestedObject into KotlinClass.Nested::nestedField // kotlin class
```

