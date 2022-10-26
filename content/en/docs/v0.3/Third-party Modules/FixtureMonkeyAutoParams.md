---
title: "Fixture Monkey AutoParams"
linkTitle: "Fixture Monkey AutoParams"
weight: 3
---

{{< alert color="warning" title="Warning">}}
This module is experimental
{{< /alert >}}
- Extends [AutoParams](https://github.com/JavaUnit/AutoParams) to support parameterized tests.
- `@FixtureMonkeyAutoSource` for AutoParams parameterized tests.

## Installation
### Gradle
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-autoparams:0.3.1")
```

### Maven
```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-autoparams</artifactId>
  <version>0.3.1</version>
  <scope>test</scope>
</dependency>
```

### Try it out!
```java
@Data   // lombok getter, setter
public class Order {
	@NotNull
    private Long id;

    private String productName;

	private int quantity;

	@Nullable
	private String sample;
}

@ParameterizedTest
@FixtureMonkeyAutoSource
void test(Order order, ArbitraryBuilder<Order> orderBuilder) {
    then(order).isNotNull();

    Order actual = orderBuilder
		.generator(JacksonArbitraryGenerator.INSTANCE)
        .set("name", "factory")
        .set("quantity", Arbitraries.integers().between(5, 10))
        .sample();

    // then
    then(actual.getId()).isNotNull();    // @NotNull
    then(actual.getProductName()).isEqualTo("factory");
    then(actual.getQuantity()).isBetween(5, 10);
}
```
