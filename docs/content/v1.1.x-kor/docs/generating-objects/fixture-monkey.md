---
title: "FixtureMonkey"
images: []
menu:
docs:
parent: "generating-objects"
identifier: "fixturemonkey"
weight: 31
---

## FixtureMonkey란?

`FixtureMonkey`는 테스트에 필요한 객체(테스트 픽스처)를 손쉽게 생성해주는 라이브러리의 핵심 클래스입니다. 다양한 타입의 클래스 인스턴스를 유효한 임의의 값으로 자동 생성해주는 일종의 팩토리라고 생각하면 이해하기 쉽습니다. 테스트 데이터를 준비하기 위해 복잡한 설정 코드를 일일이 작성할 필요 없이, 간단하게 테스트 객체를 만들 수 있습니다.

## 사용 방법 - 간단 요약

FixtureMonkey를 사용한 일반적인 과정은 다음과 같습니다:

1. `FixtureMonkey` 인스턴스 만들기
2. 생성 메서드를 사용해 테스트 객체 만들기
3. 필요한 경우 테스트 요구사항에 맞게 객체 속성 조정하기

다음은 FixtureMonkey를 활용한 간단한 테스트 예제입니다:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void testProductDiscount() {
    // 1. FixtureMonkey 인스턴스 생성
    FixtureMonkey fixtureMonkey = FixtureMonkey.create();
    
    // 2. 가격을 100.0으로 설정한 상품 객체 생성
    Product product = fixtureMonkey.giveMeBuilder(Product.class)
        .set("price", 100.0)
        .sample();
    
    // 3. 할인 기능 테스트
    double discountedPrice = productService.applyDiscount(product, 10);
    
    // 4. 예상 결과 확인 (100원의 10% 할인 = 90원)
    assertEquals(90.0, discountedPrice);
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun testProductDiscount() {
    // 1. FixtureMonkey 인스턴스 생성 (코틀린용 플러그인 추가)
    val fixtureMonkey = FixtureMonkey.plugin(KotlinPlugin()).build()
    
    // 2. 가격을 100.0으로 설정한 상품 객체 생성
    val product: Product = fixtureMonkey.giveMeBuilder<Product>()
        .set("price", 100.0)
        .sample()
    
    // 3. 할인 기능 테스트
    val discountedPrice = productService.applyDiscount(product, 10)
    
    // 4. 예상 결과 확인 (100원의 10% 할인 = 90원)
    assertEquals(90.0, discountedPrice)
}
{{< /tab >}}
{{< /tabpane>}}

이제 FixtureMonkey를 테스트에 활용하는 방법을 자세히 알아보겠습니다.

## FixtureMonkey 인스턴스 만들기

테스트 객체를 생성하려면 먼저 `FixtureMonkey` 인스턴스가 필요합니다. 이 인스턴스가 모든 테스트 객체 생성을 담당합니다.

Java에서는 간단히 `create()` 메서드로 생성할 수 있습니다. Kotlin에서는 Kotlin 전용 플러그인을 추가해야 합니다.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

// 기본 설정으로 생성
FixtureMonkey fixtureMonkey = FixtureMonkey.create();

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

// 코틀린 전용 플러그인 추가
val fixtureMonkey = FixtureMonkey
.plugin(KotlinPlugin())
.build()

{{< /tab >}}
{{< /tabpane>}}

테스트 객체 생성 방식을 조정하고 싶다면 빌더 패턴을 사용해 다양한 옵션을 추가할 수 있습니다.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    + 옵션들...
    .build();

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val fixtureMonkey = FixtureMonkey.builder()
    + 옵션들...
    .build()

{{< /tab >}}
{{< /tabpane>}}

사용 가능한 옵션에 대한 자세한 내용은 [Fixture Monkey 옵션 문서](../../fixture-monkey-options/concepts/)를 참고하세요.

## 객체 생성하기

`FixtureMonkey` 클래스는 다양한 방식으로 테스트에 필요한 객체를 생성할 수 있는 메서드들을 제공합니다.

### 상황에 맞는 메서드 선택하기

다음은 상황별 적합한 메서드 선택 가이드입니다:

- `giveMeOne()` - 기본 랜덤 값을 가진 단일 객체가 필요할 때
- `giveMe()` - 기본 랜덤 값을 가진 여러 객체가 필요할 때
- `giveMeBuilder()` - 속성을 세부 조정한 객체가 필요할 때
- `giveMeArbitrary()` - jqwik의 Arbitrary API를 활용한 고급 사용 사례

### giveMeOne()
특정 타입의 객체 하나가 필요할 때는 `giveMeOne()`을 사용합니다. 메서드에 클래스 또는 타입 정보를 전달하면 됩니다.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

// 단일 Product 객체 생성
Product product = fixtureMonkey.giveMeOne(Product.class);

// 문자열 리스트 생성
List<String> strList = fixtureMonkey.giveMeOne(new TypeReference<List<String>>() {});

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

// 코틀린에서는 타입 추론 활용
val product: Product = fixtureMonkey.giveMeOne()

// 문자열 리스트 생성
val strList: List<String> = fixtureMonkey.giveMeOne()

{{< /tab >}}
{{< /tabpane>}}

### giveMe()
동일한 타입의 객체 여러 개가 필요할 때는 `giveMe()`를 사용합니다. 원하는 개수를 지정하여 스트림이나 리스트 형태로 객체들을 생성할 수 있습니다.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

// Product 객체의 스트림 생성
Stream<Product> productStream = fixtureMonkey.giveMe(Product.class);

// 문자열 리스트의 스트림 생성
Stream<List<String>> strListStream = fixtureMonkey.giveMe(new TypeReference<List<String>>() {});

// 3개의 Product 객체로 구성된 리스트 생성
List<Product> productList = fixtureMonkey.giveMe(Product.class, 3);

// 3개의 문자열 리스트로 구성된 리스트 생성
List<List<String>> strListList = fixtureMonkey.giveMe(new TypeReference<List<String>>() {}, 3);

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

// Product 객체의 시퀀스 생성
val productSequence: Sequence<Product> = fixtureMonkey.giveMe()

// 문자열 리스트의 시퀀스 생성
val strListSequence: Sequence<List<String>> = fixtureMonkey.giveMe()

// 3개의 Product 객체로 구성된 리스트 생성
val productList: List<Product> = fixtureMonkey.giveMe(3)

// 3개의 문자열 리스트로 구성된 리스트 생성
val strListList: List<List<String>> = fixtureMonkey.giveMe(3)

{{< /tab >}}
{{< /tabpane>}}

### giveMeBuilder()
객체의 속성을 세부적으로 조정해야 할 때는 `giveMeBuilder()`를 사용합니다. 이 메서드는 해당 타입의 `ArbitraryBuilder`를 반환합니다.
`ArbitraryBuilder`는 Fixture Monkey에서 객체 속성을 조정한 후 [`Arbitrary`](../../customizing-objects/arbitrary/) 객체를 생성하는 데 사용되는 빌더 클래스입니다.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

// Product 타입의 빌더 생성
ArbitraryBuilder<Product> productBuilder = fixtureMonkey.giveMeBuilder(Product.class);

// 문자열 리스트 타입의 빌더 생성
ArbitraryBuilder<List<String>> strListBuilder = fixtureMonkey.giveMeBuilder(new TypeReference<List<String>>() {});

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

// Product 타입의 빌더 생성
val productBuilder: ArbitraryBuilder<Product> = fixtureMonkey.giveMeBuilder()

// 문자열 리스트 타입의 빌더 생성
val strListBuilder: ArbitraryBuilder<List<String>> = fixtureMonkey.giveMeBuilder()

{{< /tab >}}
{{< /tabpane>}}

이미 존재하는 객체를 바탕으로 새로운 객체를 만들 때도 `giveMeBuilder()`를 활용할 수 있습니다.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

// 기존 객체 준비
Product product = new Product(1L, "책", ...);

// 기존 객체를 바탕으로 빌더 생성
ArbitraryBuilder<Product> productBuilder = fixtureMonkey.giveMeBuilder(product);

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

// 기존 객체 준비
val product = Product(1L, "책", ...)

// 기존 객체를 바탕으로 빌더 생성
val productBuilder = fixtureMonkey.giveMeBuilder(product)

{{< /tab >}}
{{< /tabpane>}}

`ArbitraryBuilder`를 통해 객체의 다양한 속성을 조정할 수 있습니다. 속성 조정 방법에 대한 자세한 내용은 [객체 커스터마이징 문서](../../customizing-objects/apis)를 참고하세요.

`ArbitraryBuilder`에서 최종 객체를 얻으려면 `sample()`, `sampleList()`, `sampleStream()` 메서드를 사용합니다.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

ArbitraryBuilder<Product> productBuilder = fixtureMonkey.giveMeBuilder(Product.class);

// 단일 객체 생성
Product product = productBuilder.sample();
// 3개의 객체로 구성된 리스트 생성
List<Product> productList = productBuilder.sampleList(3);
// 객체 스트림 생성
Stream<Product> productStream = productBuilder.sampleStream();

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val productBuilder: ArbitraryBuilder<Product> = fixtureMonkey.giveMeBuilder()

// 단일 객체 생성
val product = productBuilder.sample()
// 3개의 객체로 구성된 리스트 생성
val productList = productBuilder.sampleList(3)
// 객체 스트림 생성
val productStream = productBuilder.sampleStream()

{{< /tab >}}
{{< /tabpane>}}

객체 자체가 아니라 `Arbitrary` 인스턴스가 필요한 경우에는 `build()` 메서드를 호출하면 됩니다.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

ArbitraryBuilder<Product> productBuilder = fixtureMonkey.giveMeBuilder(Product.class);

// Arbitrary 인스턴스 생성
Arbitrary<Product> productArbitrary = productBuilder.build();

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val productBuilder: ArbitraryBuilder<Product> = fixtureMonkey.giveMeBuilder()

// Arbitrary 인스턴스 생성
val productArbitrary = productBuilder.build()

{{< /tab >}}
{{< /tabpane>}}

### giveMeArbitrary()
특정 타입의 `Arbitrary` 객체가 직접 필요한 경우에는 `giveMeArbitrary()` 메서드를 사용합니다.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

// Product 타입의 Arbitrary 객체 생성
Arbitrary<Product> productArbitrary = fixtureMonkey.giveMeArbitrary(Product.class);

// 문자열 리스트 타입의 Arbitrary 객체 생성
Arbitrary<List<String>> strListArbitrary = fixtureMonkey.giveMeArbitrary(new TypeReference<List<String>>() {});

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

// Product 타입의 Arbitrary 객체 생성
val productArbitrary: Arbitrary<Product> = fixtureMonkey.giveMeArbitrary()

// 문자열 리스트 타입의 Arbitrary 객체 생성
val strListArbitrary: Arbitrary<List<String>> = fixtureMonkey.giveMeArbitrary()

{{< /tab >}}
{{< /tabpane>}}

