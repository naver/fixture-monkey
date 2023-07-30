---
title: "Creating test objects"
linkTitle: "Java"
weight: 20
menu:
docs:
  parent: "get-started"
  identifier: "creating-test-objects"
---

Consider a scenario where you need a test fixture for a Product class, as shown below:

```java
@Value
public class Product {
    private Long id;

    private String str;

    private String productName;

    private long price;

    private List<String> options;
}
```

With the Fixture Monkey library, generating an instance of Product becomes remarkably simple, requiring just two lines of code.

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

There are several custom options available in Fixture Monkey that allow you to generate instances according to your specific requirements. For more details about Fixture Monkey options, refer to the Fixture Monkey options document.

Next, use the `giveMeOne()` method with the desired test class type to generate an instance of the specified type.

As evident from the then section, an instance of the Product class is created.
