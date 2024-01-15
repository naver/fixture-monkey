---
title: "Requirements"
images: [ ]
menu:
docs:
  parent: "get-started"
  identifier: "requirements"
weight: 21
---

{{< alert icon="💡" text="Fixture Monkey 는 테스트 환경용으로 설계되었습니다. 운영 코드에는 포함되지 않도록 하는게 좋습니다." />}}

## 전제 조건

* JDK 1.8 이상 (또는 Kotlin 1.8 이상)
* JUnit 5 platform
* jqwik 1.7.3

--------

## 종속성

| Dependency                    | Description                                  |
|-------------------------------|----------------------------------------------|
| fixture-monkey                | Core library                                 |
| fixture-monkey-starter        | Starter dependency for fixture monkey        |
| fixture-monkey-kotlin         | Kotlin support                               |
| fixture-monkey-starter-kotlin | Starter dependency for fixture monkey kotlin |

**fixture-monkey-starter** 는 Fixture Monkey 를 시작하는 데 도움이 되도록 fixture-monkey-jakarta-validation과 같은 플러그인들과 함께 제공되는 스타터 패키지입니다.

Kotlin 환경에서는 **fixture-monkey-starter-kotlin** 을 대신 사용할 수 있습니다.

#### Gradle

```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter:{{< fixture-monkey-version >}}")
```

#### Maven

```xml

<dependency>
    <groupId>com.navercorp.fixturemonkey</groupId>
    <artifactId>fixture-monkey-starter</artifactId>
    <version>{{< fixture-monkey-version>}}
    </version>
    <scope>test</scope>
</dependency>
```

--------

## 서드파티 라이브러리 지원

| Dependency                        | Description                |
|-----------------------------------|----------------------------|
| fixture-monkey-jackson            | Jackson support            |
| fixture-monkey-jakarta-validation | Jakarta validation support |
| fixture-monkey-javax-validation   | Javax validation support   |
| fixture-monkey-mockito            | Mockito support            |
| fixture-monkey-autoparams         | Autoparams support         |
