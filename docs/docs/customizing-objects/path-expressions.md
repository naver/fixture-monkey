---
title: "Path expressions"
sidebar_position: 41
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';
import CodeSnippet from '@site/src/components/CodeSnippet';
import PathExpressionsTestJava from '@examples-java/customizing/PathExpressionsTest.java';


## What you will learn in this document
- How to select specific fields or properties of a test object
- How to reference specific parts of an object using string expressions
- How to access properties in various structures like nested objects, arrays, and lists

## Introduction to Path Expressions

When writing tests, you often need to modify specific fields of your test objects. Path expressions in Fixture Monkey are like GPS coordinates that help you precisely locate and modify any part of your test object.

As a beginner, think of path expressions as a way to "navigate" through your object structure to reach exactly the field you want to change.

## Basic Object Structure Example

To understand path expressions, let's use a simple example object:


<Tabs groupId="language">
<TabItem value="java" label="Java">

<CodeSnippet src={PathExpressionsTestJava} language="java" showClass />

</TabItem>
<TabItem value="kotlin" label="Kotlin">

```kotlin
data class KotlinClass(
    val field: String,
    val array: Array<String>,
    val list: List<String>,
    val `object`: Nested,
    val objectList: List<Nested>
) {
    data class Nested(
        val nestedField: String
    )
}
```

</TabItem>
</Tabs>


## Visual Map of Path Expressions

Think of your object as a tree structure. Each path expression is like directions to a specific location in that tree:

```
JavaClass / KotlinClass
│
├── field → "field"               // Direct field access
│
├── array → "array"               // The entire array
│   ├── array[0] → "array[0]"     // First element in array
│   ├── array[1] → "array[1]"     // Second element in array
│   └── all elements → "array[*]" // ALL elements in array (wildcard)
│
├── list → "list"                 // The entire list
│   ├── list[0] → "list[0]"       // First element in list
│   ├── list[1] → "list[1]"       // Second element in list
│   └── all elements → "list[*]"  // ALL elements in list (wildcard)
│
├── object → "object"             // The nested object
│   └── nestedField → "object.nestedField"  // Field inside nested object
│
└── objectList → "objectList"     // List of nested objects
    ├── objectList[0] → "objectList[0]"  // First object in the list
    │   └── nestedField → "objectList[0].nestedField"  // Field in first object
    │
    ├── objectList[1] → "objectList[1]"  // Second object in the list
    │   └── nestedField → "objectList[1].nestedField"  // Field in second object
    │
    └── all elements → "objectList[*]"   // ALL objects in the list
        └── nestedField → "objectList[*].nestedField"  // Field in ALL objects
```

## Simple Path Expressions Guide

String path expressions work identically in Java and Kotlin. The examples below show both languages.

### 1. Selecting the Root Object

To select the entire object itself, use `"$"`:


<Tabs groupId="language">
<TabItem value="java" label="Java">

```java
ArbitraryBuilder<JavaClass> builder = fixtureMonkey.giveMeBuilder(JavaClass.class);
builder.set("$", new JavaClass(...));
```

</TabItem>
<TabItem value="kotlin" label="Kotlin">

```kotlin
val builder = fixtureMonkey.giveMeBuilder<KotlinClass>()
builder.set("$", KotlinClass(...))
```

</TabItem>
</Tabs>


### 2. Selecting a Direct Field


<Tabs groupId="language">
<TabItem value="java" label="Java">

```java
builder.set("field", "Hello World");
```

</TabItem>
<TabItem value="kotlin" label="Kotlin">

```kotlin
builder.set("field", "Hello World")
```

</TabItem>
</Tabs>


### 3. Selecting a Nested Field


<Tabs groupId="language">
<TabItem value="java" label="Java">

```java
builder.set("object.nestedField", "Nested Value");
```

</TabItem>
<TabItem value="kotlin" label="Kotlin">

```kotlin
builder.set("object.nestedField", "Nested Value")
```

</TabItem>
</Tabs>


### 4. Working with Collections


<Tabs groupId="language">
<TabItem value="java" label="Java">

```java
// Set the first item in the list
builder.set("list[0]", "First Item");

// Set ALL items in the list (wildcard)
builder.set("list[*]", "Same Value");
```

</TabItem>
<TabItem value="kotlin" label="Kotlin">

```kotlin
// Set the first item in the list
builder.set("list[0]", "First Item")

// Set ALL items in the list (wildcard)
builder.set("list[*]", "Same Value")
```

</TabItem>
</Tabs>


### 5. Working with Arrays

Very similar to lists:


<Tabs groupId="language">
<TabItem value="java" label="Java">

```java
builder.set("array[0]", "First Element");
builder.set("array[*]", "All Elements");
```

</TabItem>
<TabItem value="kotlin" label="Kotlin">

```kotlin
builder.set("array[0]", "First Element")
builder.set("array[*]", "All Elements")
```

</TabItem>
</Tabs>


### 6. Complex Nested Paths

You can combine these patterns to go as deep as you need:


<Tabs groupId="language">
<TabItem value="java" label="Java">

```java
// nestedField of first object in list
builder.set("objectList[0].nestedField", "First Nested");

// nestedField of ALL objects in list
builder.set("objectList[*].nestedField", "All Nested");
```

</TabItem>
<TabItem value="kotlin" label="Kotlin">

```kotlin
// nestedField of first object in list
builder.set("objectList[0].nestedField", "First Nested")

// nestedField of ALL objects in list
builder.set("objectList[*].nestedField", "All Nested")
```

</TabItem>
</Tabs>


## Type-Safe Selection

### Java: JavaGetter

If you prefer to avoid string-based expressions in Java, you can use type-safe getters:

```java
// Direct field
builder.set(javaGetter(JavaClass::getField), "Hello World");

// Nested field
builder.set(
    javaGetter(JavaClass::getObject).into(Nested::getNestedField),
    "Nested Value"
);

// Collection elements
builder.set(javaGetter(JavaClass::getList).index(String.class, 0), "First");
builder.set(javaGetter(JavaClass::getList).allIndex(String.class), "All");
```

### Kotlin: DSL Exp

Kotlin users can use property references for type-safe expressions:

```kotlin
// Direct field
builder.setExp(KotlinClass::field, "Hello World")

// Nested field
builder.setExp(KotlinClass::`object` into KotlinClass.Nested::nestedField, "Nested Value")

// Collection elements
builder.setExp(KotlinClass::objectList["0"] into KotlinClass.Nested::nestedField, "First")
builder.setExp(KotlinClass::objectList["*"] into KotlinClass.Nested::nestedField, "All")
```

For more details on Kotlin DSL Expressions, see the [Kotlin DSL Exp page](../plugins/kotlin-plugin/kotlin-exp).

## Common Beginner Questions

### What happens if I try to access an out-of-bounds index?

If you try to access an element that doesn't exist (e.g., `"list[5]"` when the list only has 3 items), Fixture Monkey will simply ignore that setting. To catch these issues, you can enable [Expression Strict Mode](#expression-strict-mode).

### How do I handle maps?

While you can't directly access map elements with path expressions, you can use [InnerSpec](./innerspec) to customize maps.

### Can I use multiple path expressions at once?

Yes! You can chain multiple `.set()` calls:


<Tabs groupId="language">
<TabItem value="java" label="Java">

```java
ArbitraryBuilder<JavaClass> builder = fixtureMonkey.giveMeBuilder(JavaClass.class)
    .set("field", "Value 1")
    .set("object.nestedField", "Value 2")
    .set("list[*]", "Value 3");
```

</TabItem>
<TabItem value="kotlin" label="Kotlin">

```kotlin
val builder = fixtureMonkey.giveMeBuilder<KotlinClass>()
    .set("field", "Value 1")
    .set("object.nestedField", "Value 2")
    .set("list[*]", "Value 3")
```

</TabItem>
</Tabs>


## Advanced Options

### Expression Strict Mode

Enable this [option](../fixture-monkey-options/advanced-options-for-experts#expression-strict-mode) to make Fixture Monkey validate all path expressions:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .setExpressionStrictMode(true)
    .build();
```

With strict mode enabled, invalid paths will throw exceptions, helping you catch mistakes early.

## Summary

Path expressions are a powerful feature of Fixture Monkey that let you:
- Navigate to any part of your test object structure
- Set specific values for testing different scenarios
- Modify multiple related fields in one operation
- Keep your test code clean and readable

Start with simple direct field access, then gradually explore collection access and nested properties as you grow comfortable with the syntax.
