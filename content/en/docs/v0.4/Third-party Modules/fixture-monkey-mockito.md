---
title: "fixture-monkey-mockito"
weight: 4
---
## Features
Generating mock interafce, abstract class by [Mockito](https://site.mockito.org/)

## How-to
### 1. Adding dependency
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-mockito:0.4.14")
```

```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-mockito</artifactId>
  <version>0.4.14</version>
  <scope>test</scope>
</dependency>
```

### 2. Adding option `plguin`
```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
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
    FixtureMonkey sut = LabMonkey.labMonkeyBuilder()
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
