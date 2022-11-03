---
title: "ArbitraryBuilder"
weight: 3
---
`ArbitraryBuilder` 는 Fixture Monkey에서 제공하고 있는 [fixture](https://junit.org/junit4/cookbook.html#Fixture) 입니다. Fixture Monkey 인스턴스에서 `giveMeBuilder`를 호출해서 `ArbitraryBuilder`를 생성할 수 있습니다. 예를 들면, `Person` fixture를 아래와 같이 생성할 수 있습니다.
```java
fixtureMonkey.giveMeBuilder(Person.class)
```

이미 정의한 `Person` 인스턴스를 `ArbitraryBuilder`로 변환할 수도 있습니다.

```java
fixtureMonkey.giveMeBuilder(new Person("SALLY"));
```

[Test Data Builder Pattern](http://www.natpryce.com/articles/000714.html) 에 따라 빌더 패턴을 통한 테스트 객체 생성 방법을 제공하고 있습니다. 특정 테스트 케이스에서 fixture의 제어가 필요하다면 테스트 케이스 내에서 `ArbitraryBuilder`에 [연산]({{< relref "/docs/v0.3/features/manipulator" >}})을 호출하면 됩니다. 예를 들면, 다음과 같이 `Person`의 city 필드가 항상 "SEOUL"인 fixture를 정의할 수 있습니다.

```java
fixtureMonkey.giveMeBuilder(Person.class)
    .set("city", "SEOUL")
```
## 특징

* 매번 새로운 객체를 생성하기 때문에 모든 테스트에서 재사용이 가능하다.
* `fixed` 연산을 실행하면 `ArbitraryBuilder`에서 동일한 객체를 반환한다.
* 정의해놓은 인스턴스를 `ArbitraryBuilder`로 전환 가능하다. 전환한 `ArbitraryBuilder`는 연산이 적용 가능하다. 연산을 적용하지 않으면 입력한 객체 인스턴스를 항상 반환한다. [예제]({{< relref "/docs/v0.3/examples/manipulator/complexmanipulator#manipulate-existing-instance" >}})

## ValidOnly
{{< alert color="secondary" title="Note">}}
자세한 정보는 [여기]({{< relref "/docs/v0.3/features/arbitraryvalidator" >}})를 확인해주세요
{{< /alert >}}

* validOnly를 설정하지 않으면 기본 값은 `true` 입니다.
* 만약 `true`로 설정하면 등록한 `ArbitraryValidator`에 유효한 인스턴스만 생성 합니다. 
* 만약 `false`로 설정하면 유효하지 않아도 생성합니다.

## Generator

`ArbitraryBuilder` 에서 [ArbitraryGenerator]({{< relref "/docs/v0.3/features/arbitrarygenerator" >}})를 변경할 수 있습니다.

## Build

`build` 는 [Arbitrary]({{< relref "/docs/v0.3/features/arbitrary" >}})를 반환합니다.

## Sample
`sample` 은 fixture에서 인스턴스를 생성하여 반환합니다.
항상 새로운 값을 반환합니다.

## 더 알아두면 좋은 정보들
* [연산]({{< relref "/docs/v0.3/features/manipulator" >}})
