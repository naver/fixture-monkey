---
title: "Fixture Monkey Helper"
sidebar_position: 91
---


[Fixture Monkey Helper](https://plugins.jetbrains.com/plugin/19589-fixture-monkey-helper) is an IntelliJ plugin that helps to use Fixture Monkey on the IntelliJ IDE.

It provides some features that make using String Expressions and Kotlin DSL Expressions easier, and also add some IntelliJ inspections to detect and fix abnormal code.

:::danger
This plugin currently operates only within Java source code and Kotlin test code. Plans for expansion are in progress.
:::

## Installation

1. Open IntelliJ IDEA
2. Go to **Settings** > **Plugins** > **Marketplace**
3. Search for **"Fixture Monkey Helper"**
4. Click **Install** and restart the IDE

## Features

### Expression Support
- **Auto-Completion**: Smart suggestions for property names as you type path expressions in `set()`, `size()`, etc.
- **Expression Validation**: Highlights invalid path expressions before running tests
- **Navigation**: Click on a property name in a string expression to jump to its field declaration
- **String-to-Kotlin DSL Conversion**: Convert string expressions like `"address.city"` to Kotlin DSL `Address::city`

### Kotlin DSL Enhancements
- **Bidirectional Transformation**: Switch between Kotlin DSL and string expressions
  - Support for on-the-fly bidirectional transformation (Beta)
- **Code Folding**: Collapse DSL expressions into a single readable line
- **Lambda Expression Generator**: Generate fixture specifications with lambda expressions
- **Lambda to DSL Conversion**: Convert lambda expressions into Fixture Monkey Kotlin DSL

### Inspections
- Change type information passed as method arguments in Fixture Monkey factory methods to generic type arguments
- Change generic type arguments to variable types in Fixture Monkey factory methods when possible

### Property Overview Tool Window (Alpha)
- View all properties registered with ArbitraryBuilder at a glance in a tree format
- Accessible via **View** > **Tool Windows** > **Fixture Monkey Properties**

## Example: Auto-Completion

When you type a path expression, the plugin suggests available properties:

```java
// Type "p" and auto-completion will suggest "price", "productName", etc.
fixtureMonkey.giveMeBuilder(Product.class)
    .set("p|", "value")  // <- cursor here triggers auto-completion
```

## Example: Expression Validation

Invalid expressions are highlighted as errors:

```java
// "nonExistentField" will be highlighted as an error
fixtureMonkey.giveMeBuilder(Product.class)
    .set("nonExistentField", "value")  // <- red underline
```
