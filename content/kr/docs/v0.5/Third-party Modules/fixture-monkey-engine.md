---
title: "fixture-monkey-engine"
weight: 7
---

## 기능
JUnit Jupiter 테스트에서 사용할 수 있는 `FixtureMonkeySessionExtension` 을 제공합니다.
- `@ExtendWith(FixtureMonkeySessionExtension.class)`를 테스트 클래스에 선언해서 사용할 수 있습니다.
- `FixtureMonkeySessionExtension`은 테스트 시작과 끝의 라이프 사이클에 JqwikSession 라이프 사이클을 적용합니다. 
- Jqwik에서 사용한 내부 static cache 를 비워주기 때문에 Fixture Monkey 로 객체를 생성하는 테스트가 많을 경우, 성능상 이득을 볼 수 있습니다.

## 설정
### 1. 의존성 추가
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-engine:{{< param version >}}")
```

```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-engine</artifactId>
  <version>{{< param version >}}</version>
  <scope>test</scope>
</dependency>
```
