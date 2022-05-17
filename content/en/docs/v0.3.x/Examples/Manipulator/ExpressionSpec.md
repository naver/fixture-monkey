---
title: "ExpressionSpec"
weight: 3
---

{{< alert color="secondary" title="Note">}}
For detail information check out [here]({{< relref "/docs/v0.3.x/features/manipulator#expression" >}})
{{< /alert >}}


## ExpressionSpec
```java
@Test
void expressionSpec() {
    // given
    FixtureMonkey fixture = FixtureMonkey.create();
    ExpressionSpec initialOrderSpec = new ExpressionSpec()
        .set("price", 10L);

    // when
    Order actual = fixture.giveMeBuilder(Order.class)
        .spec(initialOrderSpec)
        .sample();
    
    then(actual.getPrice()).isEqualTo(10L);
}
```

### Apply Multiple ExpressionSpecs
```java
@Test
void exprssionSpecs() {
    // given
    FixtureMonkey fixture = FixtureMonkey.create();
    ExpressionSpec priceSpec = new ExpressionSpec()
        .set("price", 10L);
    ExpressionSpec orderNoSpec = new ExpressionSpec()
        .set("orderNo", "1");

    // when
    Order actual = fixture.giveMeBuilder(Order.class)
        .spec(priceSpec)
        .spec(orderNoSpec)
        .sample();
    
    then(actual.getPrice()).isEqualTo(10L);
    then(actual.getOrderNo()).isEqualTo("1");
}
```

### Apply Random ExpressionSpec
```java
@Test
void specAny() {
    // given
    FixtureMonkey fixture = FixtureMonkey.create();
    ExpressionSpec initialOrderSpec = new ExpressionSpec()
        .set("price", 10L);
    ExpressionSpec nextOrderSpec = new ExpressionSpec()
        .set("price", 5L);

    // when
    Order actual = fixture.giveMeBuilder(Order.class)
        .specAny(initialOrderSpec, nextOrderSpec)
        .sample();
    
    then(actual.getPrice()).satisfiesAnyOf(
        it -> then(it).isEqualTo(10L),
        it -> then(it).isEqualTo(5L)
    );
}
```



## IterableSpec

### Manipulate Iterable Size
```java
@Test
void size() {
    // given
    FixtureMonkey fixture = FixtureMonkey.create();
    ExpressionSpec spec = new ExpressionSpec()
        .list("items", it -> it.ofSize(2));
    
    // when
    Order actual = fixture.giveMeBuilder(Order.class)
        .spec(spec)
        .sample();
    
    then(actual.getItems()).hasSize(2);
}
```


### Manipulate Specific Index Element
```java
@Test
void setElement() {
    // given
    FixtureMonkey fixture = FixtureMonkey.create();
    ExpressionSpec spec = new ExpressionSpec()
        .list("items", it -> {
            it.ofSize(2);
            it.setElement(0, "set");
        }
    );
    
    // when
    Order actual = fixture.giveMeBuilder(Order.class)
        .spec(spec)
        .sample();
    
    then(actual.getItems().get(0)).isEqualTo("set");
}
```

### Manipulate Specific Index Element Field
```java
@Test
void setElementField() {
    // given
    FixtureMonkey fixture = FixtureMonkey.create();
    ExpressionSpec spec = new ExpressionSpec()
        .list("orders", it -> {
            it.ofSize(1);
            it.setElementField(0, "id", "1");
        }
    );
    
    // when
    StackedOrder actual = fixture.giveMeBuilder(StackedOrder.class)
        .spec(spec)
        .sample();

    then(actual.getOrders()).hasSize(1);
    then(actual.getOrders().get(0).getId()).isEqualTo("1");
}
```

### Manipulate Any Index Element
```java
@Test
void any() {
    // given
    FixtureMonkey fixture = FixtureMonkey.create();
    ExpressionSpec spec = new ExpressionSpec()
        .list("items", it -> {
            it.ofSize(4);
            it.any("set");
        }
    );
    
    // when
    Order actual = fixture.giveMeBuilder(Order.class)
        .spec(spec)
        .sample();
    
    then(actual.getItems()).anyMatch(it -> it.equals("set"));
}
```

### Manipulate All Index Element

```java
@Test
void all() {
    // given
    FixtureMonkey fixture = FixtureMonkey.create();
    ExpressionSpec spec = new ExpressionSpec()
        .list("items", it -> {
            it.all("set");
        }
    );
    
    // when
    Order actual = fixture.giveMeBuilder(Order.class)
        .spec(spec)
        .sample();
    
    then(actual.getItems()).allMatch(it -> it.equals("set"));
}
```
