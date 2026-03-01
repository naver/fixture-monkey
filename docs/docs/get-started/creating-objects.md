---
title: "Creating objects"
sidebar_position: 22
---


> Fixture Monkey works in both Java and Kotlin.
> We have a separate 'Getting Started' page for each environment you can use: [Java](./creating-objects), [Java without Lombok](./creating-objects-without-lombok), and [Kotlin](./creating-objects-in-kotlin).
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

## Understanding the Setup

First, create a FixtureMonkey instance that facilitates the creation of test fixtures.
The key part is choosing the right **Introspector** — it defines _how_ Fixture Monkey creates objects.

### Choosing an Introspector

The introspector you need depends on how your class is structured:

| Your class has... | Use this introspector |
|---|---|
| Lombok `@Value` / `@AllArgsConstructor` | `ConstructorPropertiesArbitraryIntrospector` |
| No-args constructor + setters | `BeanArbitraryIntrospector` (default) |
| Jackson annotations | `JacksonObjectArbitraryIntrospector` (via [Jackson Plugin](../plugins/jackson-plugin/features)) |
| Kotlin data class | `PrimaryConstructorArbitraryIntrospector` (via [Kotlin Plugin](../plugins/kotlin-plugin/features)) |

For more details, see the [Introspectors](../generating-objects/introspector) section.

### Example Class

For this example, we use a Lombok `@Value` class:

:::tip
Add `lombok.anyConstructor.addConstructorProperties=true` to your `lombok.config` file.
This is required for `ConstructorPropertiesArbitraryIntrospector` to work with Lombok.
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

(If you're working without Lombok, see [creating objects without Lombok](./creating-objects-without-lombok))

## Next Steps

Now that you can create objects, learn how to [customize them](./customizing-objects) to match your test scenarios.
