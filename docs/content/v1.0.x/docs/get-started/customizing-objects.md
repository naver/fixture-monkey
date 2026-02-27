---
title: "Customizing objects"
weight: 25
menu:
docs:
parent: "get-started"
identifier: "customizing-objects"
---

Suppose you need to customize your test fixture for a specific unit test.
In that case, you can use Fixture Monkey to generate a builder and further customize it.

```java
@Value
public class Product {
    long id;

    String productName;

    long price;

    List<String> options;

    Instant createdAt;
}
```

For example, for a certain test you might need a Product instance with an id of 1,000.

In order to do this, you can get a type builder from fixture monkey with the `giveMeBuilder` method.
The Builder allows chaining additional method calls to customize your fixture.
In this case you can use the `set()` function to set the id to 1,000.
From the Builder use `sample()` to get an instance from the builder.

```java
@Test
void test() {
    // given
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
        .build();
    long id = 1000;

    // when
    Product actual = fixtureMonkey.giveMeBuilder(Product.class)
        .set("id", id)
        .sample();

    // then
    then(actual.getId()).isEqualTo(1000);
}
```

In the example above, you can see that the field `id` is set to a value you desire.

You can also use Fixture Monkey to work with fields that are collections.
For instance, you might want the list "options" to have a specific size, and you might want a certain element of it to have a certain value.

```java
@Test
void test() {
    // given
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
        .build();

    // when
    Product actual = fixtureMonkey.giveMeBuilder(Product.class)
        .size("options", 3)
        .set("options[1]", "red")
        .sample();

    // then
    then(actual.getOptions()).hasSize(3);
    then(actual.getOptions().get(1)).isEqualTo("red");
}
```

You can specify the size of a certain collection (list, set, map) and set an element to a specific value using the set() function,
and then get your instance by calling the sample() method.

For more examples of how to select properties with expressions and set property values, you can go to the [customizing section](../../customizing-objects/apis).
