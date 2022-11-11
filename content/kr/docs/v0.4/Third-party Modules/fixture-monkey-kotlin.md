---
title: "fixture-monkey-kotlin"
weight: 3
---

## 기능
다양한 코틀린 관련 기능들을 활용할 수 있도록 플러그인을 지원합니다.
- `PrimaryConstructorArbitraryGenerator` 를 객체 기본 생성 방식으로 사용합니다.
- 코틀린 DSL을 활용한 표현식 작성과 확장 함수들을 제공합니다. [see](https://github.com/naver/fixture-monkey/blob/main/fixture-monkey-kotlin/src/main/kotlin/com/navercorp/fixturemonkey/kotlin/FixtureMonkeyExtensions.kt)

### Exp
type-safe한 표현식을 만들어주는 코틀린 DSL입니다.

* Exp를 사용하는 연산은 연산 이름 뒤에 `exp`, `expGetter`가 붙습니다.
* 참조하려고 하는 필드를 메소드 레퍼런스로 나타냅니다.
* 필드가 객체이고 객체 내부 필드를 다시 참조하고 싶을 경우 `into`, `intoGetter`를 사용합니다.
* 컨테이너 내부 요소를 참조할 때는 메소드 레퍼런스에 `[인덱스]`, `["*"]`를 추가합니다.

|              | 연산 이름     | 객체 내부 필드 참조 | 내부 요소 참조     | 
|--------------|-----------|-------------|--------------|
| Java Class   | expGetter | intoGetter  | [인덱스], ["*"] |
| Kotlin Class | exp       | into        | [인덱스], ["*"] |




## 설정
### 1. 의존성 추가
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-kotlin:0.4.3")
```

```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-kotlin</artifactId>
  <version>0.4.3</version>
  <scope>test</scope>
</dependency>
```

### 2. 옵션 변경
```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
    .plugin(KotlinPlugin())
    .build();
```
