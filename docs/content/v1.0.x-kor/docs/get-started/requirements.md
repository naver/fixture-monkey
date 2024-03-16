---
title: "요구사항"
images: []
menu:
docs:
parent: "get-started"
identifier: "requirements"
weight: 21
---

{{< alert icon="💡" text="Fixture Monkey 는 테스트 환경용으로 설계되었습니다. 운영 코드에는 포함하지 않는 것을 권장합니다." />}}

## 사용 환경

* JDK 1.8 이상 (또는 Kotlin 1.8 이상)
* JUnit 5 platform
* jqwik 1.7.3

--------

## 종속성

| 종속성                           | 설명                      |
|-------------------------------|-------------------------|
| fixture-monkey                | fixture monkey 코어 라이브러리 |
| fixture-monkey-starter        | fixture monkey 시작 패키지   |
| fixture-monkey-kotlin         | Kotlin 지원               |
| fixture-monkey-starter-kotlin | Kotlin 환경을 위한 시작 패키지    |

**fixture-monkey-starter** 는 Fixture Monkey 를 시작하는 데 도움이 되도록 fixture-monkey-jakarta-validation과 같은 플러그인들이 함께 제공되는 스타터
패키지입니다.

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
    <version>{{<fixture-monkey-version>}}
    </version>
    <scope>test</scope>
</dependency>
```

--------

## 서드파티 라이브러리 지원

| 종속성                               | 설명                    |
|-----------------------------------|-----------------------|
| fixture-monkey-jackson            | Jackson 지원            |
| fixture-monkey-jakarta-validation | Jakarta validation 지원 |
| fixture-monkey-javax-validation   | Javax validation 지원   |
| fixture-monkey-mockito            | Mockito 지원            |
| fixture-monkey-autoparams         | Autoparams 지원         |
