---
title: "Requirements"
images: []
menu:
docs:
  parent: "get-started"
  identifier: "requirements"
weight: 21
---

{{< alert icon="💡" text="Fixture Monkey is designed for test environments. It is not recommended for production use." />}}

## Prerequisites
* JDK 1.8 or higher (Or Kotlin 1.8 or higher)
* JUnit 5 platform
* jqwik 1.7.3

--------

## Dependencies
| Dependency | Description |
|--|--|
| fixture-monkey | Core library |
| fixture-monkey-starter | Starter dependency for fixture monkey |
| fixture-monkey-kotlin | Kotlin support |
| fixture-monkey-starter-kotlin | Starter dependency for fixture monkey kotlin |

**fixture-monkey-starter** is a starter dependency that comes with pre-configured dependencies such as fixture-monkey-jakarta-validation to help you get started using Fixture Monkey.

For Kotlin environments, you can use **fixture-monkey-starter-kotlin**

#### Gradle
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter:{{< fixture-monkey-version >}}")
```

#### Maven
```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-starter</artifactId>
  <version>{{< fixture-monkey-version >}}</version>
  <scope>test</scope>
</dependency>
```

--------

## Third party library support
| Dependency | Description |
|--|--|
| fixture-monkey-jackson | Jackson support |
| fixture-monkey-jakarta-validation | Jakarta validation support |
| fixture-monkey-javax-validation | Javax validation support |
| fixture-monkey-mockito | Mockito support |
| fixture-monkey-autoparams | Autoparams support |
