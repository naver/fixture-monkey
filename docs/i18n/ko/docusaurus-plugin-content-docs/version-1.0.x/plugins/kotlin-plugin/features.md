---
title: "기능"
sidebar_position: 61
---


Kotlin의 특성인 간결성, 안전성, 실용성을 최대한 활용할 수 있도록 Fixture Monkey 는 Kotlin 플러그인을 제공합니다.
- `PrimaryConstructorArbitraryIntrospector` 를 기본 Introspector 로 적용하여 Kotlin 클래스를 주 생성자로 생성
- Fixture Monkey 의 Kotlin 확장 함수 제공
- Kotlin DSL 표현식 및 instantiateBy DSL 제공

## 종속성
##### fixture-monkey-kotlin
#### Gradle
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-kotlin:1.0.20")
```

#### Maven
```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-kotlin</artifactId>
  <version>1.0.20</version>
  <scope>test</scope>
</dependency>
```

##### fixture-monkey-starter-kotlin

Kotlin 환경에서 Fixture Monkey 를 사용하는 것을 돕기 위해 **fixture-monkey-starter** 나 **fixture-monkey-jakarta-validation** 와 같은 사전 구성된 종속성들로 구성되어 있는 스타터 패키지 **fixture-monkey-starter-kotlin** 를 제공합니다.

#### Gradle
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter-kotlin:1.0.20")
```

#### Maven
```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-starter-kotlin</artifactId>
  <version>1.0.20</version>
  <scope>test</scope>
</dependency>
```

## 플러그인
```kotlin
val fixtureMonkey = FixtureMonkey.builder()
    .plugin(KotlinPlugin())
    .build()
```

