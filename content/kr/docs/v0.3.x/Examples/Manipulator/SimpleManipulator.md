---
title: "간단한 연산"
weight: 1
---

## 필드 값을 고정하는 연산
```java
@Test
void set(){
    // given
    FixtureMonkey fixture = FixtureMonkey.create();
    Instant orderedAt = Instant.now().minus(30L, ChronoUnit.DAYS);

    // when
    Order actual = fixture.giveMeBuilder(Order.class)
        .set("orderedAt", orderedAt)
        .sample();

    then(actual.orderedAt).isEqualTo(orderedAt);
}   
```

## 연산 집합인 ExpressionSpec를 사용하여 필드 값들을 고정하는 연산
```java
@Test
void setExpressionSpec(){
	// given
	FixtureMonkey fixture = FixtureMonkey.create();

	// when
	Order actual = fixture.giveMeBuilder(Order.class)
	.set(new ExpressionSpec()
	.set("productName", "BOTTLE")
	.set("price", 5000L)
	)
	.sample();

	then(actual.getProductName()).isEqualTo("BOTTLE");
	then(actual.getPrice()).isEqualTo(5000L);
}   
```

## 객체를 고정하는 연산
```java
@Test
void setRoot(){
    // given
    FixtureMonkey fixture = FixtureMonkey.create();

    // when
    String actual = fixture.giveMeBuilder(String.class)
        .set("setRoot")
        .sample();

    then(actual).isEqualTo("setRoot");
}   
```

```java
@Test
void setRoot(){
    // given
    FixtureMonkey fixture = FixtureMonkey.create();

    // when
    String actual = fixture.giveMeBuilder(String.class)
        .set("$", "setRoot")
        .sample();

    then(actual).isEqualTo("setRoot");
}   
```

## 필드 값을 null로 고정하는 연산
```java
@Test
void setNull() {
    // given
    FixtureMonkey fixture = FixtureMonkey.create();
	
    // when
    Order actual = fixture.giveMeBuilder(Order.class)
        .setNull("sellerEmail")
        .sample();

    then(actual.sellerEmail).isNull();
}
```

## 필드 값을 null이 아니도록 고정하는 연산
```java
@Test
void setNotNull() {
    // given
    FixtureMonkey fixture = FixtureMonkey.create();

    // when
    Order actual = fixture.giveMeBuilder(Order.class)
        .setNotNull("sellerEmail")
        .sample();

    then(actual.sellerEmail).isNotNull();
}
```


## 필드 값을 랜덤하게 설정하는 연산
```java
@Test
void setArbitrary() {
    // given
    FixtureMonkey fixture = FixtureMonkey.create();

    // when
    Order actual = fixture.giveMeBuilder(Order.class)
        .set("id", Arbitraries.integers().between(1, 50))
        .sample();
	
    then(actual.id).isBetween(1, 50);
}
```
**- Set by Value vs. Set by Arbitrary**

Arbitrary로 값을 설정하는 경우에는 하위 클래스의 필드 값을 제어할 수 없습니다. 다음 두 예시를 비교해봅시다:
```java
    // Set by Arbitrary
    Order order = SUT.giveMeBuilder(Order.class)
        .set("product", Arbitraries.just(new Product("Apple")))
        .set("product.name", "Banana")
        .sample();
```

```java
    // Set by Value
    Order order = SUT.giveMeBuilder(Order.class)
        .set("product", new Product("Apple"))
        .set("product.name", "Banana")
        .sample();
```
order의 product.name 값은 Arbitrary로 값을 설정한 위의 예시에서는 "Apple"이고, 직접 값을 설정한 아래 예시에서 "Banana"입니다.

위의 예시에서는 상위 클래스인 product에 설정한 Arbitrary 값이 product를 결정하기 때문에 하위 필드 값을 변경해도 반영되지 않습니다.
반대로, 아래 예시에서는 직접 값을 설정해주고 있어서 product.name이 "Apple"에서 "Banana"로 변경될 수 있습니다.

## 필드 값을 ArbitraryBuilder에서 생성할 객체로 고정하는 연산
```java
@Test
void setBuilder() {
    // given
    FixtureMonkey fixture = FixtureMonkey.create();
    ArbitraryBuilder<String> idBuilder = fixture.giveMeBuilder(String.class);
    
    // when
    Order actual = fixture.giveMeBuilder(Order.class)
        .setBuilder("id", idBuilder)
        .sample();
	
    then(actual.id).isNotNull();
}
```

## 필드 값의 후행조건을 선언하는 연산
```java
@Test
void setPostCondition() {
    // given
    FixtureMonkey fixture = FixtureMonkey.create();

    // when
    Order actual = fixture.giveMeBuilder(Order.class)
        .set("id", Arbitraries.longs().between(-1, 1)) // not affected by postCondition 
	    .set("id", Arbitraries.longs().between(10, 15)) // affected by postCondition
	    .setPostCondition("id", Long.class, it -> 0 <= it && it <= 10)
	    .sample();

    then(actual.id).isEqualTo(10);
}
```
`setPostCondition`는 필드의 후행조건을 선언합니다. 여러 번의 고정 연산을 적용한 경우 마지막으로 고정한 연산에 후행조건을 적용합니다.

## 객체에 후행조건을 적용하는 연산
```java
@Test
void setPostConditionRoot() {
    // given
    FixtureMonkey fixture = FixtureMonkey.create();

    // when
    String actual = fixture.giveMeBuilder(String.class)
	    .setPostCondition(it -> it.length() > 5)
	    .sample();

	then(actual).hasSizeGreaterThan(5);
}
```

```java
@Test
void setPostConditionRoot() {
    // given
    FixtureMonkey fixture = FixtureMonkey.create();

    // when
    String actual = fixture.giveMeBuilder(String.class)
	    .setPostCondition("$", String.class, it -> it.length() > 5)
	    .sample();

	then(actual).hasSizeGreaterThan(5);
}
```

## Customize 연산
```java
@Test
void customize() {
    // given
    FixtureMonkey fixture = FixtureMonkey.create();
    Instant orderedAt = Instant.now().minus(30L, ChronoUnit.DAYS);
	
    // when
    Order actual = fixture.giveMeBuilder(Order.class)
        .customize(Order.class, o -> {
            o.setOrderedAt(orderedAt);
            return o;
        })
        .sample();

    then(actual.orderedAt).isEqualTo(orderedAt);
}
```

## 필드의 컬렉션 크기를 제어하는 연산
```java
@Test
void size() {	
	// given
	FixtureMonkey fixture = FixtureMonkey.create();

	// when
	Order actual = fixture.giveMeBuilder(Order.class)
		.size("items", 5)
		.sample();
	
	then(actual.items).hasSize(5);
}
```

```java
@Test
void size() {	
	// given
	FixtureMonkey fixture = FixtureMonkey.create();

	// when
	Order actual = fixture.giveMeBuilder(Order.class)
		.size("items", 1, 5)
		.sample();
	
	then(actual.items).hasSizeBetween(1, 5);
}
```

## 필드의 컬렉션 최대 크기를 제어하는 연산
```java
@Test
void size() {	
	// given
	FixtureMonkey fixture = FixtureMonkey.create();

	// when
	Order actual = fixture.giveMeBuilder(Order.class)
		.maxSize("items", 5)
		.sample();
	
	then(actual.items).hasSizeLessThanOrEqualTo(5);
}
```

## 필드의 컬렉션 최소 크기를 제어하는 연산
```java
@Test
void size() {	
	// given
	FixtureMonkey fixture = FixtureMonkey.create();

	// when
	Order actual = fixture.giveMeBuilder(Order.class)
		.minSize("items", 1)
		.sample();
	
	then(actual.items).hasSizeGreaterThanOrEqualTo(1);
}
```

