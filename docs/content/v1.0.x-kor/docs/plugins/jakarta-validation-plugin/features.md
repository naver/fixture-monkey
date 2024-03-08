---
title: "기능"
images: []
menu:
docs:
parent: "jakarta-validation-plugin"
identifier: "jakarta-validation-plugin-features"
weight: 81
---

Fixture Monkey는 Fixture Monkey Jakarta Validation 플러그인을 사용하여 Jakarta Bean Validation 3.0 어노테이션들을 기반으로 유효한 데이터 생성을 지원합니다.

{{< alert icon="💡" text="Fixture Monkey Javax Validation 플러그인은 Javax Bean Validation도 지원합니다." />}}

### Dependencies
#### Gradle
```
testImplementation("com.navercorp.fixturemonkey:jakarta-validation:1.0.0")
```
#### Maven
```
<dependency>
<groupId>com.navercorp.fixturemonkey</groupId>
<artifactId>fixture-monkey-jakarta-validation</artifactId>
<version>1.0.0</version>
<scope>test</scope>
</dependency>
```
Jakarta Validation API와 Hibernate Validator는 이미 의존성의 일부로 제공됩니다.

### Plugin
- Java
```Java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new JakartaValidationPlugin())
    .build();
```

- Kotlin
```Kotlin
val fixtureMonkey = FixtureMonkey.builder()
    .plugin(JakartaValidationPlugin())
    .build()
```
