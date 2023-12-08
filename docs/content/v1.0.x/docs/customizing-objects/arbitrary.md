---
title: "Customizing random values with Arbitrary"
weight: 43
menu:
docs:
parent: "customizing-objects"
identifier: "arbitrary"
---

Jqwik is a Property-Based Testing library for the JVM environment.
Fixture Monkey relies on Jqwik's [`Arbitrary`](https://jqwik.net/docs/1.2.1/javadoc/net/jqwik/api/Arbitrary.html) to generate random values for primitive types such as characters, strings, and integers.

In Jqwik, `Arbitrary` is the core interface used to represent objects that can be generated and shrunk.
At times, we might desire our fixture properties to have random values while adhering to certain constraints.

In such scenarios, you can achieve this by customizing properties using Fixture Monkey's `set()` method and assigning an `Arbitrary` as the value.
You can generate an `Arbitrary` that matches certain conditions, simply by calling static methods in Jqwik's [Arbitraries class](https://jqwik.net/docs/current/user-guide.html#static-arbitraries-methods).

The following code example demonstrates ways to customize random values using `set()` with `Arbitrary`:
{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

Product actual = fixtureMonkey.giveMeBuilder(Product.class)
    .set("id", Arbitraries.longs().greaterOrEqual(1000))
    .set("productName", Arbitraries.strings().withCharRange('a', 'z').ofMaxLength(10))
    .set("productType", Arbitraries.of(ProductType.CLOTHING, ProductType.ELECTRONICS))
    .sample();

then(actual.getId()).isGreaterThanOrEqualTo(1000);
then(actual.getProductName()).matches("^[a-z]+$");
then(actual.getProductName().length()).isLessThanOrEqualTo(10);
then(actual.getProductType()).matches(it -> it == ProductType.CLOTHING || it == ProductType.ELECTRONICS);

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val actual = fixtureMonkey.giveMeBuilder<Product>()
    .setExp(Product::id, Arbitraries.longs().greaterOrEqual(1000))
    .setExp(Product::productName, Arbitraries.strings().withCharRange('a', 'z').ofMaxLength(10))
    .setExp(Product::productType, Arbitraries.of(ProductType.CLOTHING, ProductType.ELECTRONICS))
    .sample()

then(actual.id).isGreaterThanOrEqualTo(1000)
then(actual.productName).matches("^[a-z]+$")
then(actual.productName.length).isLessThanOrEqualTo(10)
then(actual.productType).matches { it -> it === ProductType.CLOTHING || it === ProductType.ELECTRONICS }

{{< /tab >}}
{{< /tabpane>}}

For further details about Jqwik and `Arbitrary`, check out [Jqwik User Guide](https://jqwik.net/docs/current/user-guide.html)
