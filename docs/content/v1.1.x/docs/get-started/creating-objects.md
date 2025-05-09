---
title: "Creating objects"
weight: 22
menu:
docs:
  parent: "get-started"
  identifier: "creating-objects"
---

> Fixture Monkey works in both Java and Kotlin.
> We have a separate 'Getting Started' page for each environment you can use: [Java](../creating-test-objects), [Java without Lombok](../creating-test-objects-without-lombok), and [Kotlin](../creating-objects-in-kotlin).
>
> This page explains the Java environment. Please refer to the appropriate page for your environment.

Creating test objects with Fixture Monkey is remarkably simple. Here's how you can use it:

```java
@Test
void test() {
    // given
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
        .build();

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

First, create a FixtureMonkey instance that facilitates the creation of test fixtures.
There are several custom options available in Fixture Monkey that allow you to generate instances according to your specific requirements.

Here we are configuring the `objectIntrospector` to use `ConstructorPropertiesArbitraryIntrospector`, which means that the object will be constructed using the constructor annotated with @ConstructorProperties.
An `Introspector` defines how Fixture Monkey generates objects.

For example, consider a Product class like this:

{{< alert icon="💡" text="lombok.anyConstructor.addConstructorProperties=true should be added in lombok.config" />}}

```java
@Value
public class Product {
    long id;
    String productName;
    long price;
    List<String> options;
    Instant createdAt;
    ProductType productType;
    Map<Integer, String> merchantInfo;
}
```

(Note that the Lombok annotation `@Value` is used to make Immutable classes. If you're working in an environment without Lombok, go to [creating test objects without lombok](../creating-test-objects-without-lombok))

For `ConstructorPropertiesArbitraryIntrospector`, the generated class should have a constructor with @ConstructorProperties or you can add `lombok.anyConstructor.addConstructorProperties=true` in the lombok.config file.
(There are alternative Introspectors available, each with their own requirements. Check out the [`Introspectors` section](../../generating-objects/introspector) for more details.)

As shown in the then section above, an instance of the Product class is created.
