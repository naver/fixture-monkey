---
title: "Complex Manipulator"
weight: 2
---

## Manipulate Existing Instance
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

## Map
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

## Zip
```java
@Test
void zip(){
    // given
    FixtureMonkey fixture = FixtureMonkey.create();
    ArbitraryBuilder<String> stringArbitraryBuilder = fixture.giveMeBuilder(String.class);
    ArbitraryBuilder<Integer> integerArbitraryBuilder = fixture.giveMeBuilder(Integer.class);
    
    // when
    ArbitraryBuilder<String> actual = ArbitraryBuilders.zip(
        stringArbitraryBuilder,
        integerArbitraryBuilder,
        (string, integer) -> integer + ". " + string
    );
    
    then(actual.sample()).contains(".");
}
```

## AcceptIf
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
}

```

## Fixed
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
