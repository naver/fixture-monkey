---
title: "Path expressions"
sidebar_position: 41
---


## What you will learn in this document
- How to select specific fields or properties of a test object
- How to reference specific parts of an object using string expressions
- How to access properties in various structures like nested objects, arrays, and lists

## Introduction to Path Expressions

When writing tests, you often need to modify specific fields of your test objects. Path expressions in Fixture Monkey are like GPS coordinates that help you precisely locate and modify any part of your test object.

As a beginner, think of path expressions as a way to "navigate" through your object structure to reach exactly the field you want to change.

## Basic Object Structure Example

To understand path expressions, let's use a simple example object:

```java
@Value
public class JavaClass {
    String field;                // A simple string field
    String[] array;              // An array of strings
    List<String> list;           // A list of strings
    Nested object;               // A nested object
    List<Nested> objectList;     // A list of nested objects

    @Value
    public static class Nested {
        String nestedField;      // A field inside the nested object
    }
}
```

## Visual Map of Path Expressions

Think of your object as a tree structure. Each path expression is like directions to a specific location in that tree:

```
JavaClass
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

### 1. Selecting the Root Object

To select the entire object itself, use:
```java
"$"
```

**Example:**
```java
ArbitraryBuilder<JavaClass> builder = fixtureMonkey.giveMeBuilder(JavaClass.class);
// Select and manipulate the entire object
builder.set("$", new JavaClass(...));
```

### 2. Selecting a Direct Field

To select a simple field at the top level:
```java
"field"
```

**Example:**
```java
// Set the "field" property to "Hello World"
builder.set("field", "Hello World");
```

### 3. Selecting a Nested Field

To access a field inside a nested object:
```java
"object.nestedField"
```

**Example:**
```java
// Set the nestedField inside the object to "Nested Value"
builder.set("object.nestedField", "Nested Value");
```

### 4. Working with Collections

#### Selecting a specific item in a list:
```java
"list[0]"  // First item
"list[1]"  // Second item
```

**Example:**
```java
// Set the first item in the list to "First Item"
builder.set("list[0]", "First Item");
```

#### Selecting ALL items in a list (wildcard):
```java
"list[*]"
```

**Example:**
```java
// Set ALL items in the list to "Same Value"
builder.set("list[*]", "Same Value");
```

### 5. Working with Arrays

Very similar to lists:

```java
"array[0]"   // First element
"array[*]"   // All elements
```

**Example:**
```java
// Set all array elements to "Array Item"
builder.set("array[*]", "Array Item");
```

### 6. Complex Nested Paths

You can combine these patterns to go as deep as you need:

```java
"objectList[0].nestedField"  // nestedField of first object in list
"objectList[*].nestedField"  // nestedField of ALL objects in list
```

**Example:**
```java
// Set the nestedField of all objects in objectList to "All Nested"
builder.set("objectList[*].nestedField", "All Nested");
```

## Type-Safe Selection with JavaGetter

If you prefer to avoid string-based expressions, you can use type-safe getters:

### 1. Selecting a Direct Field

```java
javaGetter(JavaClass::getField)
```

**Example:**
```java
builder.set(javaGetter(JavaClass::getField), "Hello World");
```

### 2. Selecting a Nested Field

```java
javaGetter(JavaClass::getObject).into(Nested::getNestedField)
```

**Example:**
```java
builder.set(
    javaGetter(JavaClass::getObject).into(Nested::getNestedField), 
    "Nested Value"
);
```

### 3. Working with Collections

```java
// Select specific element
javaGetter(JavaClass::getList).index(String.class, 0)

// Select all elements
javaGetter(JavaClass::getList).allIndex(String.class)
```

**Example:**
```java
// Set all list elements to "List Item"
builder.set(
    javaGetter(JavaClass::getList).allIndex(String.class), 
    "List Item"
);
```

## Common Beginner Questions

### What happens if I try to access an out-of-bounds index?

If you try to access an element that doesn't exist (e.g., `"list[5]"` when the list only has 3 items), Fixture Monkey will simply ignore that setting. To catch these issues, you can enable [Expression Strict Mode](#expression-strict-mode).

### How do I handle maps?

While you can't directly access map elements with path expressions, you can use [InnerSpec](./innerspec) to customize maps.

### Can I use multiple path expressions at once?

Yes! You can chain multiple `.set()` calls to configure different parts of your object:

```java
ArbitraryBuilder<JavaClass> builder = fixtureMonkey.giveMeBuilder(JavaClass.class)
    .set("field", "Value 1")
    .set("object.nestedField", "Value 2")
    .set("list[*]", "Value 3");
```

## Advanced Options

### Expression Strict Mode

Enable this [option](../fixture-monkey-options/advanced-options-for-experts#expression-strict-mode) to make Fixture Monkey validate all path expressions:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .setExpressionStrictMode(true)
    .build();
```

With strict mode enabled, invalid paths will throw exceptions, helping you catch mistakes early.

### Kotlin Support

If you're using Kotlin, you can use property references for even more elegant expressions:

```kotlin
// Instead of: "user.address.street"
builder.setExp(User::address into Address::street, "Main Street")
```

For more details, see the [Kotlin DSL Exp page](../plugins/kotlin-plugin/kotlin-exp).

## Summary

Path expressions are a powerful feature of Fixture Monkey that let you:
- Navigate to any part of your test object structure
- Set specific values for testing different scenarios
- Modify multiple related fields in one operation
- Keep your test code clean and readable

Start with simple direct field access, then gradually explore collection access and nested properties as you grow comfortable with the syntax.

