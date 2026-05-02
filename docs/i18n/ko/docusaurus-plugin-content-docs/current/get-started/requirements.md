---
title: "요구사항"
sidebar_position: 21
---


:::tip
Fixture Monkey 는 테스트 환경용으로 설계되었습니다. 운영 코드에는 포함하지 않는 것을 권장합니다.
:::

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

`junit-platform-launcher` 런타임 의존성을 추가하셔야 합니다.
Gradle 9 이전은 런타임 의존성이 없어도 동작하나 Gradle 9부터 필수가 될 예정입니다.


[📔 Gradle 공식문서](https://docs.gradle.org/current/userguide/upgrading_version_8.html#test_suites)
[⚠️ 이슈](https://github.com/gradle/gradle/issues/26114#issuecomment-1729133753)

```groovy
testRuntimeOnly("org.junit.platform:junit-platform-launcher:{version}")
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter:{{fixtureMonkeyVersion}}")
```

#### Maven

```xml

<dependency>
    <groupId>com.navercorp.fixturemonkey</groupId>
    <artifactId>fixture-monkey-starter</artifactId>
    <version>{{fixtureMonkeyVersion}}
    </version>
    <scope>test</scope>
</dependency>
```

--------

## 서드파티 라이브러리 지원

| 종속성                               | 설명                                                                 |
|-----------------------------------|--------------------------------------------------------------------|
| fixture-monkey-jackson            | 객체의 직렬화와 역직렬화를 위한 Jackson 지원                              |
| fixture-monkey-jakarta-validation | Jakarta Bean Validation (JSR 380) 어노테이션 지원                      |
| fixture-monkey-javax-validation   | Javax Bean Validation (JSR 303/349) 어노테이션 지원                    |
| fixture-monkey-mockito            | Mockito 모킹 프레임워크 지원                                           |
| fixture-monkey-autoparams         | AutoParams 테스트 데이터 생성 지원                                      |
| fixture-monkey-junit-jupiter      | JUnit Jupiter 테스트 프레임워크 지원                                    |
| fixture-monkey-kotest             | Kotest 테스트 프레임워크 지원                                           |

