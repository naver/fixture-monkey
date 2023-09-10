---
title: "Fixture Monkey Helper"
images: []
menu:
docs:
parent: "intellij-plugin"
identifier: "fixture-monkey-helper"
weight: 10
---

[Fixture Monkey Helper](https://plugins.jetbrains.com/plugin/19589-fixture-monkey-helper) is an IntelliJ plugin that helps to use Fixture Monkey on the IntelliJ IDE.

It provides some features that make using String Expressions and Kotlin DSL Expressions easier, and also add some IntelliJ inspections to detect and fix abnormal code.

### Features

- **Fixture Monkey String Expression support**
  - Conversion of string expressions to Fixture Monkey Kotlin DSL
  - Expression validation
  - Auto completion
  - Go to target field (reference)

- **FixtureMonkey Kotlin DSL support**
  - Convert Kotlin DSL to Fixture Monkey string expression
  - Folding to Fixture Monkey string expression
  - Generate Kotlin Lambda expression that helps define Fixture specification easily
  - Convert Lambda to Fixture Monkey Kotlin DSL

- **Inspection**
  - Change type information passed as method arguments in Fixture Monkey factory methods to generic type arguments
  - Change generic type arguments to variable types in Fixture Monkey factory methods when possible
