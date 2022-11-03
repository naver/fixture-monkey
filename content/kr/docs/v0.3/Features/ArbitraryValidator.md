---
title: "ArbitraryValidator"
linkTitle: "ArbitraryValidator"
weight: 8
---
- Fixture Monkey가 생성한 객체는 `ArbitraryValidator`가 유효성 검사를 진행합니다.
- 유효성 검사를 통과하지 못한 객체는 생성하지 않습니다.
- `10000` 번을 시도했음에도 유효성 검사를 통과하지 못하면 `TooManyFilterMissesException`가 발생합니다. ([MaxTriesLoop](https://github.com/jlink/jqwik/blob/master/engine/src/main/java/net/jqwik/engine/properties/MaxTriesLoop.java))

{{< alert color="secondary" title="Note">}}
JSR 380 Validator 구현체를 의존성에 추가하면 `ArbitraryValidator`에서 자동으로 등록합니다.

예를 들면 다음과 같은 의존성을 추가하면 Hibernate Validator가 유효성 검사를 진행합니다.
`testImplementation("org.hibernate.validator:hibernate-validator:6.1.7.Final")`
{{< /alert >}}

## DefaultArbitraryValidator
- `javax.validation.Validator`가 객체의 유효성 검사를 진행합니다.
- `javax.validation.constraints`를 준수해야 합니다.

## CompositeFixtureValidator (default)
- `ArbitraryValidator`는 `DefaultArbitraryValidator`와 유저가 정의한 `ArbitraryValidator`를 조합해서 만들어집니다.
