---
title: "Fixture Monkey Helper"
sidebar_position: 91
---


[Fixture Monkey Helper](https://plugins.jetbrains.com/plugin/19589-fixture-monkey-helper) is an IntelliJ plugin that helps to use Fixture Monkey on the IntelliJ IDE.

It provides some features that make using String Expressions and Kotlin DSL Expressions easier, and also add some IntelliJ inspections to detect and fix abnormal code.

:::danger
This plugin currently operates only within Java source code and Kotlin test code. Plans for expansion are in progress.
:::

### Features

- **Fixture Monkey Expression support**
  - Seamless Conversion: Transform string expressions into the Kotlin DSL provided by Fixture Monkey for ArbitraryBuilder.
  - Expression Validation: Ensure the accuracy of your string expressions before execution.
  - Intuitive Auto-Completion: Speed up your coding with smart suggestions as you type.
  - Easy Navigation: Jump directly to field references within your codebase.

- **FixtureMonkey Kotlin DSL Enhancements**
  - Bidirectional Transformation: Switch between Kotlin DSL and Fixture Monkey string expressions effortlessly.
    - Support for on-the-fly bidirectional Transformation (Beta)
  - Code Folding: Simplify your view by collapsing DSL expressions into a single line.
  - Lambda Expression Generator: Craft fixture specifications with ease using generated lambda expressions.
  - Lambda to DSL Conversion: Convert complex lambda expressions into readable and maintainable Fixture Monkey Kotlin DSL.

- **Inspection**
  - Change type information passed as method arguments in Fixture Monkey factory methods to generic type arguments
  - Change generic type arguments to variable types in Fixture Monkey factory methods when possible

- **Fixture Monkey Property Overview Tool Window (Alpha)**
  - This tool window allows you to view all properties registered with ArbitraryBuilder at a glance, presented in a tree format.

