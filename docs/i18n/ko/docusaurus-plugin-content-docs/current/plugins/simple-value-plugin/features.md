---
title: "기능"
sidebar_position: 51
---


기본적으로 Fixture Monkey는 다양한 엣지 케이스를 커버하는 임의의 값을 생성합니다.
이는 철저한 테스트에는 강력하지만, 읽기 어려운 값을 생성하는 경우가 많습니다(예: 특수 문자가 포함된 매우 긴 문자열, 극단적인 숫자).

`SimpleValueJqwikPlugin`은 **읽기 쉽고, 짧고, 현실적인** 값을 생성합니다 - 초보자나 사람이 읽을 수 있는 테스트 데이터가 필요할 때 적합합니다.

## Before and After

| 타입 | 기본값 (플러그인 없이) | SimpleValueJqwikPlugin 사용 시 |
|------|----------------------|-------------------------------|
| String | `"嚤ǃ₯⚆..."` (랜덤 유니코드, 가변 길이) | `"aB3.d"` (짧고, 읽기 쉬움) |
| Integer | `2147483647` 또는 `-1938274` | `-10000` ~ `10000` |
| LocalDate | `+999999999-12-31` | 작년부터 내년까지 |
| List 크기 | `0` ~ `30` | `0` ~ `3` |

## 설정

```kotlin
val fixtureMonkey = FixtureMonkey.builder()
    .plugin(SimpleValueJqwikPlugin())
    .build()
```

검증 플러그인과 호환됩니다 - 검증 어노테이션이 우선합니다:

```kotlin
val fixtureMonkey = FixtureMonkey.builder()
    .plugin(SimpleValueJqwikPlugin())
    .plugin(JakartaValidationPlugin())  // 검증 어노테이션이 SimpleValue 기본값을 오버라이드
    .build()
```

## 기본값

### String
길이 0~5, 포함 문자:
- 알파벳 문자
- 숫자
- HTTP 안전 특수 기호: `.`, `-`, `_`, `~`

**커스터마이징 옵션**: `minStringLength`, `maxStringLength`, `characterPredicate`

### Number
범위: `-10000` ~ `10000` (정수 및 소수 타입 모두)

**커스터마이징 옵션**: `minNumberValue`, `maxNumberValue`

### Date
범위: 오늘 기준 작년부터 내년까지

**커스터마이징 옵션**: `minusDaysFromToday`, `plusDaysFromToday`

### Container
`List`, `Set`, `Iterator`, `Iterable`, `Map`, `Entry`에 적용됩니다.

크기 범위: `0` ~ `3`

**커스터마이징 옵션**: `minContainerSize`, `maxContainerSize`

## 커스터마이징 예제

```kotlin
val fixtureMonkey = FixtureMonkey.builder()
    .plugin(
        SimpleValueJqwikPlugin()
            .minStringLength(3)
            .maxStringLength(10)
            .minNumberValue(-100)
            .maxNumberValue(100)
            .minContainerSize(1)
            .maxContainerSize(5)
    )
    .build()
```
