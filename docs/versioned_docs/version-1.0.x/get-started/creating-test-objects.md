---
title: "Creating test objects"
sidebar_position: 22
---


> Fixture Monkey works in both Java and Kotlin.
> We have a separate 'Getting Started' page for each environment you can use: [Java](./creating-test-objects), [Java without Lombok](./creating-test-objects-without-lombok), and [Kotlin](./creating-objects-in-kotlin).
>
> This page explains the Java environment. Please refer to the appropriate page for your environment.

Consider a scenario where you need a test fixture for a Product class, as shown below:

:::tip
lombok.anyConstructor.addConstructorProperties=true should be added in lombok.config
:::

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
(Note that the Lombok annotation `@Value` is used to make Immutable classes. If you're working in an environment without Lombok, go to [creating test objects without lombok](./creating-test-objects-without-lombok))

With the Fixture Monkey library, generating an instance of Product becomes remarkably simple, requiring just few lines of code.

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

First, create a FixtureMonkey instance that facilitates the creation of test fixtures.
There are several custom options available in Fixture Monkey that allow you to generate instances according to your specific requirements.

Here we are configuring the `objectIntrospector` to use `ConstructorPropertiesArbitraryIntrospector`, which means that the object will be constructed using the constructor annotated with @ConstructorProperties.
An `Introspector` defines how Fixture Monkey generates objects.

For `ConstructorPropertiesArbitraryIntrospector`, the generated class should have a constructor with @ConstructorProperties or you can add `lombok.anyConstructor.addConstructorProperties=true` in the lombok.config file.
(There are alternative Introspectors available, each with their own requirements. Check out the [`Introspectors` section](../generating-objects/introspector) for more details.)

Next, use the `giveMeOne()` method with the desired test class type to generate an instance of the specified type.

As evident from the then section, an instance of the Product class is created.

