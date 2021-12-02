---
title: "Fixture Monkey Jackson"
linkTitle: "Fixture Monkey Jackson"
weight: 1
---

- Instantiating by [Jackson](https://github.com/FasterXML/jackson) Map Deserializer
- Does not generate `@JsonIgnore` field
- `@JsonProperty` would change field name, when apply manipulator `Expression` is same as `@JsonProperty` value
    - Example [here]({{< relref "/docs/v0.3.x/examples/arbitrarygenerator#set-field-in-jacksonarbitrarygenerator-with-jsonproperty" >}})
- Could inject `ObjectMapper` in `JacksonArbitraryGenerator`
- Could not generate `interface`, you should set default implementation by [InterfaceSupplier]({{< relref "/docs/v0.3.x/features/interfacesupplier" >}})

## Installation
### Gradle
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-jackson:0.3.1")
```

### Maven
```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-jackson</artifactId>
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

	@JsonProperty("name")
    private String productName;

	private int quantity;
	
	@JsonIgnore
	private String sample;
}

@Test
void test() {
	// given
    FixtureMonkey sut = FixtureMonkey.create();

    // when
    Order actual = sut.giveMeBuilder(Order.class)
		.generator(JacksonArbitraryGenerator.INSTANCE)
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
