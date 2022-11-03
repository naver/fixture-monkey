---
title: "ExpressionSpec"
weight: 3
---

{{< alert color="secondary" title="Note">}}
자세한 내용은 [여기]({{< relref "/docs/v0.3/features/manipulator#expression" >}})를 참조해주세요.
{{< /alert >}}


## ExpressionSpec을 적용하는 예시
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

### 여러 개의 ExpressionSpec을 적용하는 예시
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

### 여러 개의 ExpressionSpec 중 임의의 하나를 적용하는 예시
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

### 내부 컬렉션의 크기를 제어하는 연산
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


### 특정 인덱스 요소를 제어하는 연산
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

### 특정 인덱스 요소의 필드를 제어하는 연산
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

### 임의의 요소를 제어하는 연산
```java
@Test
void any() {
    // given
    ExpressionSpec spec = new ExpressionSpec()
        .list("items", it -> {
            it.ofSize(4);
            it.any("set");
        }
    );
    
    // when
    Order actual = this.fixture.giveMeBuilder(Order.class)
        .spec(spec)
        .sample();
    
    then(actual.getItems()).anyMatch(it -> it.equals("set"));
}
```

### 모든 요소를 제어하는 연산

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
