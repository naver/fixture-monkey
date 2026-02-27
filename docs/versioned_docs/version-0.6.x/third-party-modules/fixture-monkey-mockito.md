---
title: "fixture-monkey-mockito"
sidebar_position: 64
---

## Features
Generating mock interafce, abstract class by [Mockito](https://site.mockito.org/)

## How-to
### 1. Adding dependency
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-mockito:0.6.12")
```

```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-mockito</artifactId>
  <version>0.6.12</version>
  <scope>test</scope>
</dependency>
```

### 2. Adding option `plugin`
```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new MockitoPlugin())
    .build();
```

## Usage
```java
@Data   // lombok getter, setter
public class Order {
    @NotNull
    private Long id;
    
    private String productName;

    private int quantity;
    
    @NotNull
    private Item item;
}

public interface Item {
    String getName();
}

@Test
void test() {
    // given
    FixtureMonkey sut = FixtureMonkey.builder()
        .plugin(new MockitoPlugin())
        .build();

    // when
    Order actual = sut.giveMeOne(Order.class);

    // then
    then(actual.getItem()).isNotNull();
    
    when(actual.getItem().getName()).thenReturn("ring");
    then(actual.getItem().getName()).isEqualTo("ring");
}
```

