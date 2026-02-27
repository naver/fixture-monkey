---
title: "기능"
sidebar_position: 51
---


픽스쳐 몽키는 엣지 케이스를 포함한 임의의 값을 생성합니다. 완전히 임의의 값을 생성하기 때문에 읽을 수 없는 데이터나 인코딩/디코딩 문제로 테스트를 깨트리는 데이터를 생성하기도 합니다.
이러한 특성은 발견하기 어려운 테스트 케이스를 검증하는데 도움을 주지만, 정밀한 제어가 요구됩니다. 특히 픽스처 몽키에 익숙하지 않은 사용자들은 이러한 특성이 테스트 코드 관리에 어려움을 느낄 수 있습니다.   

이런 사용자들을 위해 픽스쳐 몽키는 `SimpleValueJqwikPlugin` 이라는 플러그인을 새로 만들었습니다. 이 플러그인은 읽을 수 있고 극단적이지 않은 값을 생성해주는 플러그인입니다. 
플러그인이 변경하는 타입은 **문자열**, **숫자**, **날짜**, **컨테이너**입니다. 

이 플러그인은 다른 플러그인, 특히 `JavaxValidationPlugin`, `JakartaValidationPlugin` 같은 플러그인과도 같이 사용할 수 있습니다.
JSR-380 어노테이션이 있는 프로퍼티는 `XXValidationPlugin`을 적용하고 어노테이션이 없는 프로퍼티는 `SimpleValueJqwikPlugin`을 적용합니다.

만약 값을 제한하는 플러그인을 만들어서 사용하신다면 가장 마지막에 등록한 플러그인이 우선순위를 가집니다. 

값을 제한하는 플러그인을 만들고 싶은데 방법을 모르신다면 `SimpleValueJqwikPlugin`의 코드를 보시는 걸 추천합니다.

## 기본 값
### String
이 플러그인은 길이가 0부터 5까지인 문자열을 생성합니다. 생성하는 문자열은 아래와 같은 분류로 정리할 수 있습니다. 

- 알파벳
- 숫자
- HTTP 쿼리 파라미터로 사용 가능한 특수문자들 `.`, `-`, `_`, `~`

다음 옵션을 사용해서 변경할 수 있습니다.
- minStringLength
- maxStringLength
- characterPredicate

### Number
이 플러그인은 -10000부터 10000의 범위를 가지는 **정수**와 **실수**를 생성합니다.

다음 옵션을 사용해서 변경할 수 있습니다.
- minNumberValue
- maxNumberValue

### Date
이 플러그인은 작년부터 내년까지 범위를 가지는 **날짜**를 생성합니다.

다음 옵션을 사용해서 변경할 수 있습니다. 일 단위로 변경 가능합니다.
- minusDaysFromToday
- plusDaysFromToday

### Container
`Container`라는 용어는 Collection 인터페이스의 구현체 `List`, `Set`, `Iterator`, `Iterable`와 `Map`, `Entry` 등을 의미합니다.

이 플러그인은 0개부터 3개의 요소를 가지는 컨테이너를 생성합니다. 

다음 옵션을 사용해서 변경할 수 있습니다. 
- minContainerSize
- maxContainerSize

## Plugin
```kotlin
val fixtureMonkey = FixtureMonkey.builder()
    .plugin(SimpleValueJqwikPlugin())
    .build()
```

