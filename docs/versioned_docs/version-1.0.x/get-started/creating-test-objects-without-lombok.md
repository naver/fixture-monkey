---
title: "Creating test objects without Lombok"
sidebar_position: 23
---

:::tip
If you're using Lombok in your project, feel free to move on to the next page.
:::

Consider a scenario where you need a test fixture for a Product class, as shown below:

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

With the Fixture Monkey library, generating an instance of Product becomes remarkably simple, requiring just few lines of code.

```
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

First, create a FixtureMonkey instance that facilitates the creation of test fixtures. You can use `create()` to generate a Fixture Monkey instance with default options.
There are also several custom options available in Fixture Monkey that allow you to generate instances according to your specific requirements.

Fixture Monkey uses `BeanArbitraryIntrospector` as its default method for generating objects.
An `Introspector` defines how Fixture Monkey generates objects.

For `BeanArbitraryIntrospector`, the class being generated needs to have a no-args constructor and setters.
(There are alternative Introspectors available, each with their own requirements. Check out the [`Introspectors` section](../generating-objects/introspector) for more details.)

Next, use the `giveMeOne()` method with the desired test class type to generate an instance of the specified type.

As evident from the then section, an instance of the Product class is created.

