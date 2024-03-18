---
title: "FixtureMonkey"
images: []
menu:
docs:
parent: "generating-objects"
identifier: "fixturemonkey"
weight: 31
---

테스트 픽스처를 생성하기 위해서는 우선 `FixtureMonkey` 인스턴스를 생성해야 합니다. 해당 인스턴스는 테스트 픽스쳐 생성을 담당합니다.

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

테스트 픽스처를 생성하거나 커스텀하기 위해서는 FixtureMonkey 빌더를 사용하여 옵션을 추가할 수 있습니다.

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

어떤 옵션을 사용할 수 있는지에 대한 정보는 [Fixture Monkey 옵션](../../fixture-monkey-options/options)을 참고해주세요.

## 인스턴스 생성

`FixtureMonkey` 클래스는 테스트에 필요한 객체 생성을 도와주는 API들을 제공합니다.

### giveMeOne()
특정 타입의 인스턴스가 필요하다면 `giveMeOne()`을 사용할 수 있습니다. 인자로 클래스 또는 타입을 전달해주세요.

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
특정한 타입으로 고정된 두 개 이상의 인스턴스가 필요하다면 `giveMe()`를 사용할 수 있습니다. 원하는 크기를 지정하여 인스턴스의 스트림 또는 리스트를 생성할 수 있습니다.

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
인스턴스를 커스텀 할 경우 `giveMeBuilder()`를 사용할 수 있습니다. 이는 주어진 타입의 `ArbitraryBuilder`를 반환합니다.
`ArbitraryBuilder`는 주어진 클래스의 [`Arbitrary`](../arbitrary) 객체를 빌드하는 데 사용되는 Fixture Monkey의 클래스입니다.

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

이미 생성된 인스턴에 추가로 커스텀하는 경우 `giveMeBuilder()`를 사용할 수 있습니다.

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

`ArbitraryBuilder`는 픽스처를 커스텀하는 데 사용될 수 있습니다. 커스터마이징 방법에 대한 자세한 내용은 [객체 커스터마이징 항목](../../customizing-objects/apis)를 참고하십시오.

`ArbitraryBuilder`에서 인스턴스를 얻기 위해 `ArbitraryBuilder`의 `sample()`, `sampleList()`, `sampleStream()` 메서드를 사용할 수 있습니다.

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

인스턴스가 아닌 임의 객체(`Arbitrary`)만 필요한 경우 옵션을 추가하지 않고 `build()` 메서드만 호출하면 됩니다.

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
특정한 유형의 `Arbitrary`를 얻으려면 `giveMeArbitrary()` 메서드를 사용할 수 있습니다.

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

