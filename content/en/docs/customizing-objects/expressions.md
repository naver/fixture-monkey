---
title: "Expressions"
weight: 20
menu:
docs:
parent: "customizing-objects"
identifier: "expressions"
---

When using Fixture Monkey, you have the flexibility to select one or more properties of an object using Fixture Monkey expressions.

Let's consider an example object structure:

```java
@Value
public class JavaClass {
    String field;

    List<String> list;

    Nested object;

    List<Nested> objectList;

    @Value
    public static class Nested {
        String nestedField;
    }
}
```


### Selecting Properties Using Expressions

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

##### Combining expressions to select a nested field:
```java
"objectList[0].nestedField"
```

### Selecting Collections
Note that for collections, a property will only be selected if it exists within the collection size.
For instance, if the list has a size of 2 but the expression references `"list[3]"`, which is outside the bounds of the list, it will not be selected.

While Fixture Monkey supports selecting elements from lists and sets, there are currently no dedicated expressions for directly setting the elements of a map.
However, if you need to customize a map, consider using the [InnerSpec](../innerspec.md) method.

### Expression Strict Mode
This option can be turned on to ensure applied expressions strictly match the structure.
If any part of an expression is out of bounds or invalid, Fixture Monkey will raise an exception.
Refer to options.

### Kotlin EXP
By adding the Kotlin plugin, you can select properties using Kotlin's property reference syntax.
In Fixture Monkey, this feature is referred to as `Kotlin EXP` or the `Fixture Monkey Kotlin DSL`.
For further details on its usage, refer to the Kotlin plugin page.
