---
title: "Creating objects without Lombok"
sidebar_position: 23
---

:::tip
If you're using Lombok in your project, feel free to move on to the next page.
:::

Creating test objects with Fixture Monkey is remarkably simple, even without Lombok. Here's how you can use it:

```java
@Test
void test() {
    // given
    FixtureMonkey fixtureMonkey = FixtureMonkey.create();

    // when
    Product actual = fixtureMonkey.giveMeOne(Product.class);

    // then
    then(actual).isNotNull();
}
```

When you run this code, Fixture Monkey will generate a Product instance with random values.
Below is just an example, and the actual values will be different each time:

```java
Product(
    id=42,
    productName="product-value-1",
    price=1000,
    options=["option1", "option2"],
    createdAt=2024-03-21T10:15:30Z,
    productType=ELECTRONICS,
    merchantInfo={1="merchant1", 2="merchant2"}
)
```

First, create a FixtureMonkey instance that facilitates the creation of test fixtures. You can use `create()` to generate a Fixture Monkey instance with default options.
There are also several custom options available in Fixture Monkey that allow you to generate instances according to your specific requirements.

Fixture Monkey uses `BeanArbitraryIntrospector` as its default method for generating objects.
An `Introspector` defines how Fixture Monkey generates objects.

For example, consider a Product class like this:

```java
public class Product {
    private long id;
    private String productName;
    private long price;
    private List<String> options;
    private Instant createdAt;
    private ProductType productType;
    private Map<Integer, String> merchantInfo;

    public Product() {
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }

    public void setMerchantInfo(Map<Integer, String> merchantInfo) {
        this.merchantInfo = merchantInfo;
    }
}
```

For `BeanArbitraryIntrospector`, the class being generated needs to have a no-args constructor and setters, as shown in the Product class above.
The introspector will create an instance using the no-args constructor and then set random values using the setter methods.
(There are alternative Introspectors available, each with their own requirements. Check out the [`Introspectors` section](../generating-objects/introspector) for more details.)

