---
title: "fixture-monkey-jackson"
weight: 61
---

## Features
Supporting instantiating by [Jackson](https://github.com/FasterXML/jackson) ObjectMapper
- supporting custom ObjectMapper
- supporting `@JsonIgnore`, `@JsonProperty`


## How-to
### 1. Adding dependency
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-jackson:{{< param version >}}")
```

```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-jackson</artifactId>
  <version>{{< param version >}}</version>
  <scope>test</scope>
</dependency>
```

### 2. Adding option `plugin`
```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new JacksonPlugin(objectMapper))
    .build();
```

## Usage
```java
@Data   // lombok getter, setter
public class Order {
    @NotNull
    private Long id;

    @JsonProperty("name")
    private String productName;

    private int quantity;
    
    @JsonIgnore
    private String sample;
}

@Test
void test() {
    // given
    FixtureMonkey sut = FixtureMonkey.builder()
        .plugin(new JacksonPlugin(objectMapper))
        .build();

    // when
    Order actual = sut.giveMeBuilder(Order.class)
        .set("name", "factory")
        .set("quantity", Arbitraries.integers().between(5, 10))
        .sample();

    // then
    then(actual.getId()).isNotNull();    // @NotNull
    then(actual.getProductName()).isEqualTo("factory");
    then(actual.getQuantity()).isBetween(5, 10);
    then(actual.getSample()).isNull();  // @JsonIgnore
}
```
