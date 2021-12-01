---
title: "Getting Started"
linkTitle: "Getting Started"
weight: 2
---

## Prerequisites
* JDK 8+
* JUnit 5 platform
* jqwik 1.3.9

## Installation
### Gradle 
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter:0.3.1")
```


### Maven
```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-starter</artifactId>
  <version>0.3.1</version>
  <scope>test</scope>
</dependency>
```

## Try it out!

{{< alert color="primary" title="Tip">}}
Getter and Setter is not mandatory.
You could generate immutable instances by Fixture Monkey with [BuilderArbitraryGenerator]({{< relref "/docs/v0.3.x/features/arbitrarygenerator#builderarbitrarygenerator" >}}) or [JacksonArbitraryGenerator]({{< relref "/docs/v0.3.x/features/arbitrarygenerator#jacksonarbitrarygenerator" >}}).

You would not need to change anything for using Fixture Monkey.
Choose the way of instantiating.
Check out details [here]({{< relref "/docs/v0.3.x/features/arbitrarygenerator" >}}).
{{< /alert >}}
```java
@Data   // lombok getter, setter
public class Order {
    @NotNull
    private Long id;

    @NotBlank
    private String orderNo;

    @Size(min = 2, max = 10)
    private String productName;

    @Min(1)
    @Max(100)
    private int quantity;

    @Min(0)
    private long price;

    @Size(max = 3)
    private List<@NotBlank @Size(max = 10) String> items = new ArrayList<>();

    @PastOrPresent
    private Instant orderedAt;
}

@Test
void test() {
    // given
    FixtureMonkey sut = FixtureMonkey.create();

    // when
    Order actual = sut.giveMeOne(Order.class);

    // then
    then(actual.getId()).isNotNull(); // @NotNull
	then(actual.getOrderNo()).isNotBlank(); // @NotBlank
	then(actual.getProductName().length()).isBetween(2, 10); // @Size(min = 2, max = 10)
	then(actual.getQuantity()).isBetween(1, 100); // @Min(1) @Max(100)
	then(actual.getPrice()).isGreaterThanOrEqualTo(0); // @Min(0)
	then(actual.getItems()).hasSizeLessThan(3); // @Size(max = 3)
	then(actual.getItems()).allMatch(it -> it.length() <= 10); // @NotBlank @Size(max = 10)
    then(actual.getOrderedAt()).isBeforeOrEqualTo(Instant.now()); // @PastOrPresent
}
```

## Where should I go next?
* [Examples]({{< relref "/docs/v0.3.x/examples" >}})
* [DefaultSupportedTypes]({{< relref "/docs/v0.3.x/features/defaultsupportedtypes" >}})
* [ArbitraryBuilder]({{< relref "/docs/v0.3.x/features/arbitrarybuilder" >}})
* [Manipulator]({{< relref "/docs/v0.3.x/features/manipulator" >}})
* [FAQ]({{< relref "/docs/v0.3.x/faq" >}})
