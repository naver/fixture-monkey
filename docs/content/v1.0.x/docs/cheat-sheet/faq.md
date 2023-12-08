---
title: "FAQ"
weight: 101
menu:
docs:
  parent: "cheat-sheet"
  identifier: "faq"
---

### How can I limit the range of characters for my generated Strings?
> Related - How can I constrain the range of my generated Instant values?

Normally, if you want to add constraints to your fixture, you can do so using the Fixture Customization APIs provided with the ArbitraryBuilder.
However, in cases where you want each generated primitive type to adhere to specific constraints, you can use the [`javaTypeArbitaryGenerator`
and `javaTimeTypeArbitraryGenerator`](../../fixture-monkey-options/customization-options/#constraining-java-types) options.

The `javaTypeArbitaryGenerator` option allows you to customize the default values for primitive types such as Strings or Integers. For time types, such as Instant, you can use the `javaTimeTypeArbitraryGenerator` option.

This option is particularly useful if you want the generated strings to fall within a certain range of characters, or if you want your time types to be generated at predefined intervals.

### How can I exclude certain values from being generated?
You can easily specify a value using [`set()`](../../customizing-objects/apis/#set), but there may be cases where you want to EXCLUDE certain values.

For instance, you might have an Enum-typed field in your class, and you don't want it to have certain values.
In these situations, you can use [`set()`](../../customizing-objects/apis/#set) as shown below.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

Product product = sut.giveMeBuilder(Product.class)
    .set("productType", ArbitraryUtils.toCombinableArbitrary(Arbitraries.of(ProductType)).filter(it -> it != CLOTHING && it != ELECTRONICS)))
    .sample();

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val product = sut.giveMeBuilder<Product>()
    .setExpGetter(Product::getProductType, ArbitraryUtils.toCombinableArbitrary(Arbitraries.of(ProductType::class.java)).filter { it != ProductType.CLOTHING && it != ProductType.ELECTRONICS })
    .sample()

{{< /tab >}}
{{< /tabpane>}}

Or you can use [`setPostCondition()`](../../customizing-objects/apis/#setpostcondition) which works like a filter.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

Product product = sut.giveMeBuilder(Product.class)
    .setPostCondition("productType", ProductType.class, it -> it != CLOTHING || it != ELECTRONICS)
    .sample();

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val product = sut.giveMeBuilder<Product>()
    .setPostConditionExpGetter(Product::getProductType, ProductType::class.java) { it != ProductType.CLOTHING || it != ProductType.ELECTRONICS }
    .sample()

{{< /tab >}}
{{< /tabpane>}}

Please note that using `setPostCondition()` can incur higher costs for narrow conditions because it filters after the Product instance has been created.
In such cases, it's recommended to use `set()` instead.

### One of my fields depends on the value of another field. How can I customize my fixture?

The `thenApply()` method comes in handy when you need to customize a field that relies on another field.
For more information, check the [`thenApply()` section](../../customizing-objects/apis/#thenapply)
