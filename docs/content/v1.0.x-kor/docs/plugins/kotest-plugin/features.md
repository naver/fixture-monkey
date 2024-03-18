---
title: "기능"
images: []
menu:
docs:
parent: "kotest-plugin"
identifier: "kotest-plugin-features"
weight: 91
---

Fixture Monkey에서 제공하는 Kotest 플러그인을 사용하면 더욱 향상된 테스트를 경험할 수 있습니다.
- 기본 타입의 랜덤 값을 생성하는 기본 생성기를 Jqwik에서 Kotest의 프로퍼티 생성기(`Arb`)로 대체합니다. 빈(bean) 검증 어노테이션도 사용할 수 있습니다.
- `forAll`, `checkAll`을 포함한 Kotest의 [property-based 테스트](https://kotest.io/docs/proptest/property-test-functions.html)를 지원합니다.

{{< alert icon="💡" text="Kotest 플러그인 추가 후 반드시 Kotest를 사용해야 하는 것은 아니며, Junit을 사용할 수 있습니다." />}}

## 의존성
##### fixture-monkey-kotlin
#### Gradle
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-kotest:{{< fixture-monkey-version >}}")
```

#### Maven
```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-kotest</artifactId>
  <version>{{< fixture-monkey-version >}}</version>
  <scope>test</scope>
</dependency>
```

## 플러그인
```kotlin
val fixtureMonkey = FixtureMonkey.builder()
    .plugin(KotestPlugin())
    .plugin(KotlinPlugin())
    .build()
```
