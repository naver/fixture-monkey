---
title: "ArbitraryBuilder"
weight: 2
---

`ArbitraryBuilder`는 Fixture Monkey에서 제공하는 객체 생성 방법입니다.

## 생성 방법
### 타입
```java
ArbitraryBuilder<Generate> generateBuilder = fixtureMonkey.giveMeBuilder(Generate.class);
```

### 객체
```java
Generate generate = new Generate("test");

ArbitraryBuilder<Generate> generateBuilder = fixtureMonkey.giveMeBuilder(generate);
```

```java
Generate generate = new Generate("test");

ArbitraryBuilder<Generate> generateBuilder = fixtureMonkey.giveMeBuilder(Generate.class)
    .set(generate);
```


## 특징
* 타입에서 생성한 ArbitraryBuilder는 항상 랜덤한 값을 생성합니다.
* `fixed` 연산 혹은 객체에서 생성한 ArbitraryBuilder를 사용하면 고정된 값을 반환합니다.


## 연산
### 값을 변경하는 연산
#### set
`ArbitraryBuilder`에서 생성하는 객체의 필드 값을 변경합니다.

#### setPostCondition
{{< alert color="warning" title="Warning">}}
`setPostCondition`으로 좁은 범위의 조건을 설정하면 조건을 만족할 때까지 객체를 재생성하여 비용이 매우 큽니다.

좁은 범위의 조건은 `set`을 사용하는 것을 추천합니다. 
{{< /alert >}}

`ArbitraryBuilder`에서 생성하는 객체의 필드 값에 조건을 설정합니다.


#### size
`ArbitraryBuilder`에서 생성하는 컨테이너 크기를 변경합니다.

**다른 모든 연산들보다 먼저 실행됩니다.**

#### fixed
`ArbitraryBuilder`에서 반환하는 객체 값을 고정합니다.

fixed를 실행한 `ArbitraryBuilder` 에서는 항상 같은 값을 반환합니다.

### 타입을 변경하는 연산
#### map
```java
public <U> ArbitraryBuilder<U> map(Function<T, U> mapper)
```

`T` 타입에서 `U` 타입의 `ArbitraryBuilder`로 변환합니다.

#### zip
```java
public <U, R> ArbitraryBuilder<R> zipWith(ArbitraryBuilder<U> other, BiFunction<T, U, R> combinator)
```

`T` 타입과 `U` 타입을 묶어 `R` 타입의 `ArbitraryBuilder`로 변환합니다.

### 생성한 객체를 활용하는 연산
#### apply
```java
public ArbitraryBuilder<T> apply(BiConsumer<T, ArbitraryBuilder<T>> biConsumer)
```

생성한 `T` 타입의 객체를 이용한 연산을 `biConsumer`에 정의합니다. 

#### acceptIf
```java
public ArbitraryBuilder<T> acceptIf(Predicate<T> predicate, Consumer<ArbitraryBuilder<T>> consumer)
```

생성한 `T` 타입의 객체가 `predicate` 조건을 만족하면 `consumer`에 정의한 연산을 실행합니다.
