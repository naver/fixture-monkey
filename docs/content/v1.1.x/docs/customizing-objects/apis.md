---
title: "Fixture Customization APIs"
weight: 41
menu:
docs:
  parent: "customizing-objects"
  identifier: "fixture-customization-apis"
---

Fixture Monkey offers a range of APIs within the ArbitraryBuilder class that enable customization of objects created by it.

## Customizing Fixtures
### set()
The `set()` method is used to assign values to one or more properties referenced by the [expression](../expressions).

Different types, including `Supplier`, [`Arbitrary`](../arbitrary), `ArbitraryBuilder`, `NOT_NULL`, `NULL`, or `Just` can be used as the value.
Additionally, a certain instance of an object can also be used as the value.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

fixtureMonkey.giveMeBuilder(Product.class)
    .set("id", 1000);

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

fixtureMonkey.giveMeBuilder<Product>()
    .setExp(Product::id, 1000)

{{< /tab >}}
{{< /tabpane>}}

##### Just
> Using an instance wrapped by `Just` when using `set()` makes you set the value directly instead of decomposing.
> Normally, when you `set()` a property in `ArbitraryBuilder` it does not use an instance of the given value, it does a deep copy instead.
> So, if you need to set with an instance, you can use `Values.just(instance)`
> This feature can be useful in cases where you need to set a property to a mock instance when using a mocking framework.

> Note that you cannot set a child property after setting with `Just`.
```java
Product product = fixture.giveMeBuilder(Product.class)
    .set("options", Values.just(List.of("red", "medium", "adult"))
    .set("options[0]", "blue")
    .sample();
```
> For example, the product instance created above, will not have the value "blue" for the first element of options. It will remain the list given with `Just`.

### size(), minSize(), maxSize()
The `size()` method lets you specify the size of container properties.
You have the flexibility to either set a precise size or specify a range using the minimum and maximum values.

Alternatively, you can use `minSize()` or `maxSize()` to set only the minimum or maximum container size.
(By default, the size range is from 0 to 3 elements.)

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

fixtureMonkey.giveMeBuilder(Product.class)
    .size("options", 5); // size:5

fixtureMonkey.giveMeBuilder(Product.class)
    .size("options", 3, 5); // minSize:3, maxSize:5

fixtureMonkey.giveMeBuilder(Product.class)
    .minSize("options", 3); // minSize:3

fixtureMonkey.giveMeBuilder(Product.class)
    .maxSize("options", 5); // maxSize:5

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

fixtureMonkey.giveMeBuilder<Product>()
    .sizeExp(Product::options, 5) // size:5

fixtureMonkey.giveMeBuilder<Product>()
    .sizeExp(Product::options, 3, 5) // minSize:3, maxSize:5

fixtureMonkey.giveMeBuilder<Product>()
    .minSizeExp(Product::options, 3) // minSize:3

fixtureMonkey.giveMeBuilder<Product>()
    .maxSizeExp(Product::options, 5) // maxSize:5

{{< /tab >}}
{{< /tabpane>}}

### setNull(), setNotNull()
At times, you might want to ensure that a property is either always set to null or always has a value.
In such situations, you can use `setNull()` or `setNotNull()`.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

fixtureMonkey.giveMeBuilder(Product.class)
    .setNull("id");

fixtureMonkey.giveMeBuilder(Product.class)
    .setNotNull("id");

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

fixtureMonkey.giveMeBuilder<Product>()
    .setNullExp(Product::id)

fixtureMonkey.giveMeBuilder<Product>()
    .setNotNullExp(Product::id)

{{< /tab >}}
{{< /tabpane>}}

### setInner()
With `setInner()` you can apply customizations defined within an `InnerSpec` instance to your builder.
An `InnerSpec` is a type-independent specification for the customizations to be applied.

Instances of `InnerSpec` can be reused to consistently and easily configure nested properties.
This feature is particularly beneficial when customizing map properties.

For additional guidance, refer to [InnerSpec](../innerspec)

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

InnerSpec innerSpec = new InnerSpec()
    .property("merchantInfo", it -> it.entry(1000, "ABC Store"));

fixtureMonkey.giveMeBuilder(Product.class)
    .setInner(innerSpec)

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val innerSpec = InnerSpec()
    .property("merchantInfo") { it.entry(1000, "ABC Store") }

fixtureMonkey.giveMeBuilder(Product.class)
    .setInner(innerSpec)

{{< /tab >}}
{{< /tabpane>}}


### setLazy()
The `setLazy()` function assigns the property a value obtained from the provided Supplier.
The Supplier will run every time the ArbitraryBuilder is sampled.

This can be particularly useful when you need to generate unique sequential IDs or set the most recent value.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

AtomicReference<Long> variable = new AtomicReference<>(0L);
ArbitraryBuilder<Long> builder = fixtureMonkey.giveMeBuilder(Long.class)
    .setLazy("$", () -> variable.getAndSet(variable.get() + 1));

Long actual1 = builder.sample(); // actual1 == 0
Long actual2 = builder.sample(); // actual2 == 1

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

var variable = 0L
val builder = fixtureMonkey.giveMeBuilder(Long::class.java)
    .setLazy("$") { variable++ }

val actual1 = builder.sample() // actual1 == 0
val actual2 = builder.sample() // actual2 == 1

{{< /tab >}}
{{< /tabpane>}}


### setPostCondition()

`setPostCondition()` can be used when your fixture needs to adhere to a specific condition.
This condition can be defined by passing a predicate.

{{< alert icon="🚨" text="Using setPostCondition can incur higher costs for narrow conditions. In such cases, it's recommended to use set instead." />}}


{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

fixtureMonkey.giveMeBuilder(Product.class)
    .setPostCondition("id", Long.class, it -> it > 0)

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

fixtureMonkey.giveMeBuilder(Product::class.java)
    .setPostConditionExp(Product::id, Long::class.java) { it: Long -> it > 0 }

{{< /tab >}}
{{< /tabpane>}}

### fixed()
`fixed()` can be used when you want your arbitrary builder to consistently return instances with the same values every time it is sampled.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

fixtureMonkey.giveMeBuilder(Product.class)
    .fixed()

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

fixtureMonkey.giveMeBuilder<Product>()
    .fixed()

{{< /tab >}}
{{< /tabpane>}}

### limit
For the `set()`, `setLazy()`, and `setPostCondition()` methods, you can include an additional parameter that determines the number of times the customization will be applied.
This can be advantageous when the expression refers to multiple properties.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

fixtureMonkey.giveMeBuilder(Product.class)
  .set("options[*]", "red", 2); // up to 2 elements in options will be set to "red"

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

fixtureMonkey.giveMeBuilder<Product>()
    .set("options[*]", "red", 2) // up to 2 elements in options will be set to "red"

{{< /tab >}}
{{< /tabpane>}}


## Expanding Customization using Sampled Results
### thenApply()

The `thenApply()` method becomes handy when you need to customize a field based on the sampled result of the builder.
For instance, let's assume you want the "productName" field to match the generated "id" of the `Product`.
You can use `thenApply()` as follows:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

fixtureMonkey.giveMeBuilder(Product.class)
    .thenApply((it, builder) -> builder.set("productName", it.getId().toString()))

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

fixtureMonkey.giveMeBuilder(Product::class.java)
    .thenApply{it, builder -> builder.setExp(Product::productName, it.id.toString())}

{{< /tab >}}
{{< /tabpane>}}

### acceptIf()

You might also find the need to perform additional customization based on a specific condition.
In such cases, you can utilize the `acceptIf()` method, which applies the customization only when the predicate is satisfied.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

fixtureMonkey.giveMeBuilder(Product.class)
    .acceptIf(
        it -> it.getProductType() == ProductType.CLOTHING,
        builder -> builder.set("price", 1000)
    )

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

fixtureMonkey.giveMeBuilder<Product>()
    .acceptIf(
        { it.productType == ProductType.CLOTHING },
        { builder -> builder.setExp(Product::price, 1000) }
    )

{{< /tab >}}
{{< /tabpane>}}

## Transforming the Type of ArbitraryBuilder
### map()

The `map()` function is used to convert the ArbitraryBuilder type into another type.


{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

fixtureMonkey.giveMeBuilder(Product.class)
    .map(Product::getId); // transforms to ArbitraryBuilder<Long>

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

fixtureMonkey.giveMeBuilder(Product::class.java)
    .map(Product::id) // transforms to ArbitraryBuilder<Long>

{{< /tab >}}
{{< /tabpane>}}


### zipWith()

`zipWith()` becomes useful when you want to merge multiple ArbitraryBuilders to create an ArbitraryBuilder of a different type.
You have to define how you intend to combine the builders.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

ArbitraryBuilder<String> stringBuilder = fixtureMonkey.giveMeBuilder(String.class);

ArbitraryBuilder<String> zipped = fixtureMonkey.giveMeBuilder(Integer.class)
    .zipWith(stringBuilder, (integer, string) -> integer + "" + string);

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val stringBuilder = fixtureMonkey.giveMeBuilder<String>()

val zipped = fixtureMonkey.giveMeBuilder<Int>()
    .zipWith(stringBuilder) { int, string -> int.toString() + "" + string }

{{< /tab >}}
{{< /tabpane>}}

