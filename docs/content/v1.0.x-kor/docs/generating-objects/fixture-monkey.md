---
title: "FixtureMonkey"
images: []
menu:
docs:
parent: "generating-objects"
identifier: "fixturemonkey"
weight: 31
---

테스트 픽스처를 생성하기 위해서는 우선 `FixtureMonkey` 인스턴스를 생성해야 합니다. 해당 인스턴스는 테스트 픽스처 생성에 도움을 줍니다.

`FixtureMonkey` 인스턴스를 생성하기 위해서는 `create()` 메서드를 사용하면 됩니다. Kotlin 환경에서는 Kotlin 플러그인을 추가해야 합니다.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

FixtureMonkey fixtureMonkey = FixtureMonkey.create();

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val fixtureMonkey = FixtureMonkey
.plugin(KotlinPlugin())
.build()

{{< /tab >}}
{{< /tabpane>}}

테스트 픽스터를 생성하거나 커스텀하기 위해서는 FixtureMonkey 빌더를 사용하여 옵션을 추가할 수 있습니다.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
+ options...
.build();

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val fixtureMonkey = FixtureMonkey.builder()
+ options...
.build()

{{< /tab >}}
{{< /tabpane>}}

어떤 옵션을 사용할 수 있는지에 대한 정보는 [Fixture Monkey Options section](../../fixture-monkey-options/options)을 참고하십시오.

## Generating instances

The `FixtureMonkey` class provides several methods to help create test objects of the required type.


### giveMeOne()
If you need an instance of a certain type, you can use `giveMeOne()`. Pass either a class or a type reference.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

Product product = fixtureMonkey.giveMeOne(Product.class);

List<String> strList = fixtureMonkey.giveMeOne(new TypeReference<List<String>>() {});

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val product: Product = fixtureMonkey.giveMeOne()

val strList: List<String> = fixtureMonkey.giveMeOne()

{{< /tab >}}
{{< /tabpane>}}


### giveMe()
If you need multiple instances of a certain type, you can use the `giveMe()` method.
You can choose to generate either a stream of instances or a list by specifying the desired size.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

Stream<Product> productStream = fixtureMonkey.giveMe(Product.class);

Stream<List<String>> strListStream = fixtureMonkey.giveMe(new TypeReference<List<String>>() {});

List<Product> productList = fixtureMonkey.giveMe(Product.class, 3);

List<List<String>> strListList = fixtureMonkey.giveMe(new TypeReference<List<String>>() {}, 3);

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val productSequence: Sequence<Product> = fixtureMonkey.giveMe()

val strListSequence: Sequence<List<String>> = fixtureMonkey.giveMe()

val productList: List<Product> = fixtureMonkey.giveMe(3)

val strListList: List<List<String>> = fixtureMonkey.giveMe(3)

{{< /tab >}}
{{< /tabpane>}}

### giveMeBuilder()
If you need to further customize the instance to be created, you can use `giveMeBuilder()`. This will return an `ArbitraryBuilder` of the given type.
An `ArbitraryBuilder` is a class in Fixture Monkey that acts as a builder for an [`Arbitrary`](../arbitrary) object of the given class.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

ArbitraryBuilder<Product> productBuilder = fixtureMonkey.giveMeBuilder(Product.class);

ArbitraryBuilder<List<String>> strListBuilder = fixtureMonkey.giveMeBuilder(new TypeReference<List<String>>() {});

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val productBuilder: ArbitraryBuilder<Product> = fixtureMonkey.giveMeBuilder()

val strListBuilder: ArbitraryBuilder<List<String>> = fixtureMonkey.giveMeBuilder()

{{< /tab >}}
{{< /tabpane>}}

For cases where you already have a generated instance and want to customize it further, you can also use `giveMeBuilder()`.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

Product product = new Product(1L, "Book", ...);

ArbitraryBuilder<Product> productBuilder = fixtureMonkey.giveMeBuilder(product);

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val product = Product(1L, "Book", ...)

val productBuilder = fixtureMonkey.giveMeBuilder(product)

{{< /tab >}}
{{< /tabpane>}}

The generated `ArbitraryBuilder` can be used for further customization of your fixture. For more information on customization options, see the [section on customization objects](../../customizing-objects/apis).

To obtain an instance from the `ArbitraryBuilder`, you can use the `sample()`, `sampleList()`, `sampleStream()` methods of the `ArbitraryBuilder`.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

ArbitraryBuilder<Product> productBuilder = fixtureMonkey.giveMeBuilder(Product.class);

Product product = productBuilder.sample();
List<Product> productList = productBuilder.sampleList(3);
Stream<Product> productStream = productBuilder.sampleStream();

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val productBuilder: ArbitraryBuilder<Product> = fixtureMonkey.giveMeBuilder()

val product = productBuilder.sample()
val productList = productBuilder.sampleList(3)
val productStream = productBuilder.sampleStream()

{{< /tab >}}
{{< /tabpane>}}

In cases where you need an `Arbitrary` itself rather than an instance, you can simply call the `build()` method.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

ArbitraryBuilder<Product> productBuilder = fixtureMonkey.giveMeBuilder(Product.class);

Arbitrary<Product> productArbitrary = productBuilder.build();

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val productBuilder: ArbitraryBuilder<Product> = fixtureMonkey.giveMeBuilder()

val productArbitrary = productBuilder.build()

{{< /tab >}}
{{< /tabpane>}}

### giveMeArbitrary()
To get an `Arbitrary` of the specified type, you can use the `giveMeArbitrary()` method.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

Arbitrary<Product> productArbitrary = fixtureMonkey.giveMeArbitrary(Product.class);

Arbitrary<List<String>> strListArbitrary = fixtureMonkey.giveMeArbitrary(new TypeReference<List<String>>() {});

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val productArbitrary: Arbitrary<Product> = fixtureMonkey.giveMeArbitrary()

val strListArbitrary: Arbitrary<List<String>> = fixtureMonkey.giveMeArbitrary()

{{< /tab >}}
{{< /tabpane>}}

