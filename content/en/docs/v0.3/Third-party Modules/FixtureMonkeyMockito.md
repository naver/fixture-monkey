---
title: "Fixture Monkey Mockito"
linkTitle: "Fixture Monkey Mockito"
weight: 4
---
{{< alert color="warning" title="Warning">}}
This module is experimental
{{< /alert >}}
- Supports for generating interfaces and abstract classes as [mockito](https://github.com/mockito/mockito) objects.

## Installation
### Gradle
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-mockito:0.3.1")
```

### Maven
```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-mockito</artifactId>
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
		.defaultInterfaceSupplier(MockitoInterfaceSupplier.INSTANCE)
		.build();

    // when
    Order actual = sut.giveMeOne(Order.class);

    // then
    then(actual.getItem()).isNotNull();
    
    when(actual.getItem().getName()).thenReturn("ring");
    then(actual.getItem().getName()).isEqualTo("ring");
}
```
