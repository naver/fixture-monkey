---
title: "fixture-monkey-kotlin"
weight: 3
---

### 기능
다양한 코틀린 관련 기능들을 활용할 수 있도록 플러그인을 지원합니다.
- `PrimaryConstructorArbitraryGenerator` 를 객체 기본 생성 방식으로 사용합니다.
- 코틀린 DSL을 활용한 표현식 작성과 확장 함수들을 제공합니다. [see](https://github.com/naver/fixture-monkey/blob/main/fixture-monkey-kotlin/src/main/kotlin/com/navercorp/fixturemonkey/kotlin/FixtureMonkeyExtensions.kt)


### 설정
#### 1. 의존성 추가
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-kotlin:0.4.2")
```

```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-kotlin</artifactId>
  <version>0.4.2</version>
  <scope>test</scope>
</dependency>
```

#### 2. 옵션 변경
```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
	.plugin(KotlinPlugin())
    .build();
```
