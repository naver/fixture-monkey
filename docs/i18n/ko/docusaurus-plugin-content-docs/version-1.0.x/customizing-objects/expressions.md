---
title: "표현식"
sidebar_position: 42
---


Fixture Monkey를 사용할 때, Fixture Monkey 표현식을 사용하여 오브젝트의 프로퍼티를 하나 이상 유연하게 선택할 수 있습니다.

아래와 같은 예제 오브젝트가 있다고 가정해보겠습니다.

```java
@Value
public class JavaClass {
    String field;

    String[] array;

    List<String> list;

    Nested object;

    List<Nested> objectList;

    @Value
    public static class Nested {
        String nestedField;
    }
}
```

### String Expressions을 통해 프로퍼티 선택하기

##### root object 선택:
```java
"$"
```

##### 특정 필드 선택:
```java
"field"
```

##### nested 필드 선택:
```java
"object.nestedField"
```

##### 컬렉션의 n번째 요소 선택
```java
"list[n]"
```

##### 컬렉션의 모든 요소 선택
```java
"list[*]"
```

##### 배열의 n번째 요소 선택:
```java
"array[n]"
```

##### 배열의 모든 요소 선택:
```java
"array[*]"
```

##### 표현식을 결합하여 nested 필드 선택:
```java
"objectList[0].nestedField"
```

### JavaGetter Selector를 통해 프로퍼티 선택하기

`javaGetter()` property selector를 사용하여 type-safe하게 프로퍼티를 선택할 수 있습니다.
이 selector는 Java의 getter 메서드 참조를 통해 프로퍼티를 선택하고 표시하도록 설계되었습니다.


##### root object 선택:
- 현재는 지원하지 않습니다.

##### 특정 필드 선택:
```java
javaGetter(JavaClass::getField)
```

##### nested 필드 선택:
```java
javaGetter(JavaClass::getObject).into(Nested::getNestedField)
```

##### 컬렉션의 n번째 요소 선택:
```java
javaGetter(JavaClass::getList).index(String.class, n)
```

##### 컬렉션의 모든 요소 선택:
```java
javaGetter(JavaClass::getList).allIndex(String.class)
```

##### 배열의 n번째 요소 선택:
```java
javaGetter(JavaClass::getArray).index(String.class, n)
```

##### 배열의 모든 요소 선택:
```java
javaGetter(JavaClass::getArray).allIndex(String.class)
```


##### 표현식을 결합하여 nested 필드 선택:
```java
javaGetter(JavaClass::getObjectList)
    .index(Nested.class, 0)
    .into(Nested::getNestedField)
```


### 컬렉션 선택하기 

컬렉션의 경우 프로퍼티는 컬렉션 크기 내에 존재하는 경우에만 선택됩니다. 예를 들어, 리스트의 크기가 2이지만 표현식이 목록의 범위를 벗어난 `"list[3]"`을 참조하는 경우 선택되지 않습니다.

Fixture Monkey 표현식은 리스트와 세트에서 요소를 선택하는 기능을 지원하지만, 맵의 요소를 직접 선택할 수는 없습니다. 대신, 맵을 커스터마이징해야 하는 경우에는 InnerSpec을 사용할 수 있습니다.


### Expression Strict Mode

이 [옵션](../fixture-monkey-options/customization-options/#표현식-엄격-모드-사용하기)을 켜면 적용된 표현식이 구조와 정확하게 일치하는지 확인할 수 있습니다. 표현식의 일부가 범위를 벗어나거나 유효하지 않은 경우 Fixture Monkey는 예외를 발생시킵니다.

### Kotlin EXP

Kotlin 플러그인을 추가하면 Kotlin의 프로퍼티 참조 구문을 사용하여 프로퍼티를 선택할 수 있습니다. Fixture Monkey에서는 이 기능을 `Kotlin EXP` 또는 `Fixture Monkey Kotlin DSL`이라고 합니다. 사용법에 대한 자세한 내용은 [Kotlin DSL Exp](../plugins/kotlin-plugin/kotlin-exp)를 참조하세요.

