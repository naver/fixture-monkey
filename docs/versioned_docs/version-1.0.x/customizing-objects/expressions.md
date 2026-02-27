---
title: "Expressions"
sidebar_position: 42
---


When using Fixture Monkey, you have the flexibility to select one or more properties of an object using Fixture Monkey expressions.

Let's consider an example object structure:

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


### Selecting Properties Using String Expressions

##### Selecting the root object:
```java
"$"
```

##### Selecting a specific field:
```java
"field"
```

##### Selecting a nested field:
```java
"object.nestedField"
```

##### Selecting the n-th element of a collection:
```java
"list[n]"
```

##### Selecting all elements of a collection:
```java
"list[*]"
```

##### Selecting the n-th element of an array:
```java
"array[n]"
```

##### Selecting all elements of an array:
```java
"array[*]"
```

##### Combining expressions to select a nested field:
```java
"objectList[0].nestedField"
```

### Selecting Properties Using JavaGetter Selector

There is a type-safe way to select properties using a `javaGetter()` property selector.
This selector is designed to choose and represent a property through a getter method reference in Java.

##### Selecting the root object:
- Currently Not Supported

##### Selecting a specific field:
```java
javaGetter(JavaClass::getField)
```

##### Selecting a nested field:
```java
javaGetter(JavaClass::getObject).into(Nested::getNestedField)
```

##### Selecting the n-th element of a collection:
```java
javaGetter(JavaClass::getList).index(String.class, n)
```

##### Selecting all elements of a collection:
```java
javaGetter(JavaClass::getList).allIndex(String.class)
```

##### Selecting the n-th element of an array:
```java
javaGetter(JavaClass::getArray).index(String.class, n)
```

##### Selecting all elements of an array:
```java
javaGetter(JavaClass::getArray).allIndex(String.class)
```


##### Combining expressions to select a nested field:
```java
javaGetter(JavaClass::getObjectList)
    .index(Nested.class, 0)
    .into(Nested::getNestedField)
```


### Selecting Collections
Note that for collections, a property will only be selected if it exists within the collection size.
For instance, if the list has a size of 2 but the expression references `"list[3]"`, which is outside the bounds of the list, it will not be selected.

While Fixture Monkey supports selecting elements from lists and sets, there are currently no dedicated expressions for directly setting the elements of a map.
However, if you need to customize a map, consider using the [InnerSpec](./innerspec) method.

### Expression Strict Mode
This [option](../fixture-monkey-options/customization-options/#use-expression-strict-mode) can be turned on to ensure applied expressions strictly match the structure.
If any part of an expression is out of bounds or invalid, Fixture Monkey will raise an exception.

### Kotlin EXP
By adding the Kotlin plugin, you can select properties using Kotlin's property reference syntax.
In Fixture Monkey, this feature is referred to as `Kotlin EXP` or the `Fixture Monkey Kotlin DSL`.
For further details on its usage, refer to the [Kotlin DSL Exp page](../plugins/kotlin-plugin/kotlin-exp).

