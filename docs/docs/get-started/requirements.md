---
title: "Requirements"
sidebar_position: 21
---


:::tip
Fixture Monkey is designed for test environments. It is not recommended for production use.
:::

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
Add `junit-platform-launcher` as a runtime dependency.
The dependency is optional below Gradle 9, but it will be mandatory from Gradle 9 onwards.

[📔 Gradle Offical Documentation](https://docs.gradle.org/current/userguide/upgrading_version_8.html#test_suites)
[⚠️ Issue](https://github.com/gradle/gradle/issues/26114#issuecomment-1729133753)

```groovy
testRuntimeOnly("org.junit.platform:junit-platform-launcher:{version}")
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter:1.1.15")
```

#### Maven
```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-starter</artifactId>
  <version>1.1.15</version>
  <scope>test</scope>
</dependency>
```

--------

## Third party library support
| Dependency | Description |
|--|--|
| fixture-monkey-jackson | Jackson support for serialization and deserialization of objects |
| fixture-monkey-jakarta-validation | Support for Jakarta Bean Validation (JSR 380) annotations |
| fixture-monkey-javax-validation | Support for Javax Bean Validation (JSR 303/349) annotations |
| fixture-monkey-mockito | Support for Mockito mocking framework |
| fixture-monkey-autoparams | Support for AutoParams test data generation |
| fixture-monkey-junit-jupiter | Support for JUnit Jupiter test framework |
| fixture-monkey-kotest | Support for Kotest test framework |

