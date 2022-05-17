---
title: "복잡한 연산"
weight: 2
---

## 이미 선언한 객체 제어
```java
@Test
void manipulateExistingInstance() {
    // given    
    FixtureMonkey fixture = FixtureMonkey.create();
    Order givenOrder = fixture.giveMeBuilder(Order.class)
        .sample();
    
    
    // when
    Order actual = fixture.giveMeBuilder(givenOrder)
        .set("quantity", 1)
        .sample();
    
    then(actual.getQuantity()).isEqualTo(1);
}
```

## Map 연산
```java
@Test
void map() {
    // given
    FixtureMonkey fixture = FixtureMonkey.create();

    // when
    Order actual = fixture.giveMeBuilder(RegisterOrder.class)
        .map(it -> {
            Order tempOrder = fixture.giveMeOne(Order.class);
            tempOrder.setOrderNo(it.getOrderNo());
            return tempOrder;
        })
        .sample();
}
```

## Zip 연산
```java
@Test
void zip(){
    // given
    FixtureMonkey fixture = FixtureMonkey.create();
    ArbitraryBuilder<String> stringArbitraryBuilder = this.sut.giveMeBuilder(String.class);
    ArbitraryBuilder<Integer> integerArbitraryBuilder = this.sut.giveMeBuilder(Integer.class);
    
    // when
    ArbitraryBuilder<String> actual = ArbitraryBuilders.zip(
        stringArbitraryBuilder,
        integerArbitraryBuilder,
        (string, integer) -> integer + ". " + string
    );
    
    then(actual.sample()).contains(".");
}
```

## 생성할 객체가 특정 조건을 만족할경우 실행할 연산
```java
@Test
void acceptIf() {
    // given
    FixtureMonkey fixture = FixtureMonkey.create();

    // when
    Order actual = fixture.giveMeBuilder(Order.class)
        .set("productName", "Apple")
        .acceptIf(it -> it.getProductName().equals("Apple"),
            builder -> builder
                .set("productId", 100L)
        )
        .sample();

    then(actual.getProductId()).isEqualTo(100L);

```

## 객체의 모든 필드가 항상 같은 값을 반환하도록 고정하는 연산
```java
@Test
void fixed() {	
    // given
    FixtureMonkey fixture = FixtureMonkey.create();
    ArbitraryBuilder<Order> orderBuilder = fixture.giveMeBuilder(Order.class)
        .fixed();
    
    // when
    Order actual1 = orderBuilder.sample(); 
    Order actual2 = orderBuilder.sample(); 
    
    then(actual1).isEqualTo(actual2);
}
```
