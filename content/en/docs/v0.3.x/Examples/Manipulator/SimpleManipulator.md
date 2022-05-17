---
title: "Simple Manipulator"
weight: 1
---

## Set
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

    then(actual.getOrderedAt()).isEqualTo(orderedAt);
}   
```

## Set ExpressionSpec
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

## SetRoot
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

## SetNull
```java
@Test
void setNull() {
    // given
    FixtureMonkey fixture = FixtureMonkey.create();
    
    // when
    Order actual = fixture.giveMeBuilder(Order.class)
        .setNull("sellerEmail")
        .sample();

    then(actual.getSellerEmail()).isNull();
}
```

## SetNotNull
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


## SetArbitrary
```java
@Test
void setArbitrary() {
    // given
    FixtureMonkey fixture = FixtureMonkey.create();

    // when
    Order actual = fixture.giveMeBuilder(Order.class)
        .set("id", Arbitraries.longs().between(1, 50))
        .sample();
    
    then(actual.getId()).isBetween(1L, 50L);
}
```
**- Set by Value vs. Set by Arbitrary**

When a field value is set using an Arbitrary, you cannot control the field value of its subclass. Let's compare the following examples:
```java
    // Set by Arbitrary
    Order order = fixture.giveMeBuilder(Order.class)
        .set("product", Arbitraries.just(new Product("Apple")))
        .set("product.name", "Banana")
        .sample();

    then(order.getProduct().getName()).isEqualTo("Apple");
```

```java
    // Set by Value
    Order order = fixture.giveMeBuilder(Order.class)
        .set("product", new Product("Apple"))
        .set("product.name", "Banana")
        .sample();

    then(order.getProduct().getName()).isEqualTo("Banana");
```
In the above, product.name of order is "Apple", while in the example below it is "Banana".

This difference is because the field 'product' is set using an Arbitrary in the first case and by value in the second case.
When using an Arbitrary, the value of "product" gets determined by the Arbitrary. So, even though you modify the field of its subclass, it will be ignored.

## SetBuilder
```java
@Test
void setBuilder() {
    // given
    FixtureMonkey fixture = FixtureMonkey.create();
    ArbitraryBuilder<String> orderNoBuilder = fixture.giveMeBuilder(String.class);
    
    // when
    Order actual = fixture.giveMeBuilder(Order.class)
        .setBuilder("orderNo", orderNoBuilder)
        .sample();
    
    then(actual.getOrderNo()).isNotNull();
}
```

## SetPostCondition
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

    then(actual.getId()).isEqualTo(10);
}
```
`setPostCondition` set post-condition to field, so it applies to last set value

## SetPostConditionRoot
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

## Customize
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

    then(actual.getOrderedAt()).isEqualTo(orderedAt);
}
```

## Size
```java
@Test
void size() {	
    // given
    FixtureMonkey fixture = FixtureMonkey.create();

    // when
    Order actual = fixture.giveMeBuilder(Order.class)
        .size("items", 5)
        .sample();
    
    then(actual.getItems()).hasSize(5);
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
    
    then(actual.getItems()).hasSizeBetween(1, 5);
}
```

## MaxSize
```java
@Test
void size() {	
    // given
    FixtureMonkey fixture = FixtureMonkey.create();

    // when
    Order actual = fixture.giveMeBuilder(Order.class)
        .maxSize("items", 5)
        .sample();
    
    then(actual.getItems()).hasSizeLessThanOrEqualTo(5);
}
```

## MinSize
```java
@Test
void size() {	
    // given
    FixtureMonkey fixture = FixtureMonkey.create();

    // when
    Order actual = fixture.giveMeBuilder(Order.class)
        .minSize("items", 1)
        .sample();
    
    then(actual.getItems()).hasSizeGreaterThanOrEqualTo(1);
}
```
