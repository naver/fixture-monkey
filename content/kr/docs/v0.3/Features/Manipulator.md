---
title: "연산"
linkTitle: "연산"
weight: 4
---

Fixture Monkey는 `ArbitraryBuilder`를 수정할 수 있는 다양한 연산을 제공하여 특정 테스트 케이스에 사용할 수 있는 fixture를 만들 수 있도록 돕습니다.

`ArbitraryBuilder`는 기본적으로 랜덤한 값을 생성합니다. 하지만 특정 테스트 케이스에 사용하기 위해 fixture를 더 세밀하게 조정하고 싶은 경우가 있을 것입니다. 
그럴때 Fixture Monkey의 연산을 이용하면 `ArbitraryBuilder`를 수정할 수 있습니다. 다음 예시를 살펴봅시다:

```java
fixtureMonkey.giveMeBuilder(Person.class)
    .set("city", "Seoul")
```

예시에서는 set이라는 연산을 이용해 `Experssion`과 `Value`를 지정해줍니다. builder가 이제 Person 인스턴스를 만들 때 city 필드는 "Seoul"이라는 값을 가지게됩니다.

Fixture Monkey는 간단하게 특정 필드에 값을 설정하는 연산 (*set*)부터 여러 `ArbitraryBuilder` 를 합치는 연산 (*zip*)까지 다양한 연산을 제공합니다.

더 많은 연산은 [다음]({{< relref "/docs/v0.3.x/examples/manipulator/" >}})을 참고해주세요.

## 구성요소

- 표현식
- 값 혹은 필터
- 제한 횟수
- 분해

## 표현식
표현식으로 연산을 적용할 필드를 나타냅니다. 

- 필드를 가르키는 표현식은 `필드이름`입니다.
- 필드에 존재하는 객체 내부의 필드를 가르키는 표현식은 `외부필드이름.내부필드이름`입니다.
- 객체 내부 모든 필드를 가르키는 표현식은 `*`입니다
- `$`는 생성하는 최상위 객체를 가르킵니다. [JsonPath](https://github.com/json-path/JsonPath) 에서 제공하는 표현식과 동일합니다.
- 리스트 혹은 배열의 특정 요소를 가르키는 표현식은 `[index]`입니다.
- 리스트 혹은 배열의 모든 요소를 가르키는 표현식은 `[*]`입니다.

## 값 혹은 필터
`표현식`에 해당하는 필드는 입력한 `값`이 설정되거나 혹은 `필터`가 적용됩니다.

## 제한 횟수
`표현식`에 해당하는 여러 개의 필드 중 몇 개의 필드에 연산을 적용할 것인지 설정합니다.

## 분해
정의한 인스턴스를 `ArbitraryBuilder`로 변환할 수 있습니다. 불변한 객체라고 해도 연산을 적용할 수 있습니다. 

예를 들어, 다음과 같이 "Value"만을 반환하는 `ArbitraryBuilder`를 만들 수 있습니다.

```
fixtureMonkey.giveMeBuilder("Value")
``` 

분해한 객체의 null 필드에 `setNotNull`을 적용하면 새로 랜덤한 값을 생성합니다.
  
더 많은 예제는 [여기]({{< relref "/docs/v0.3.x/examples/manipulator/complexmanipulator" >}})를 참조해주세요.

## [간단한 연산]({{< relref "/docs/v0.3.x/examples/manipulator/simplemanipulator" >}})
- 연산을 다음과 같은 순서로 적용합니다. `set` → `setPostCondition` → `customize`
- 같은 `표현식`을 가지는 `동일한 연산`은 덮어 씌워집니다. 따라서 가장 마지막에 선언한 `연산`을 적용합니다. 
- 예외적으로 `setPostcondition`, `customize`은 연산을 여러 번 적용할 수 있습니다.

## [복잡한 연산]({{< relref "/docs/v0.3.x/examples/manipulator/complexmanipulator" >}})
- `fixed`는 `ArbitraryBuilder`에서 항상 같은 인스턴스를 반환하게 만듭니다.
