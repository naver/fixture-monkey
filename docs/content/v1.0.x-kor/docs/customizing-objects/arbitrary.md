---
title: "Arbitrary를 활용한 커스터마이징"
weight: 43
menu:
docs:
parent: "customizing-objects"
identifier: "arbitrary"
---

`Jqwik`은 JVM 환경에서 사용할 수 있는 프로퍼티 기반 테스트 라이브러리입니다.
Fixture Monkey는 문자, 문자열, 정수 등의 기본 타입에 대한 랜덤 값을 생성하기 위해 Jqwik의 [`Arbitrary`](https://jqwik.net/docs/1.2.1/javadoc/net/jqwik/api/Arbitrary.html)를 사용합니다.

Jqwik에서 `Arbitrary`는 생성(Generating) 및 축소(Shrinking)할 수 있는 객체를 나타내는 핵심 인터페이스입니다.
때때로 픽스처 프로퍼티가 특정 제약 조건을 준수하면서 랜덤 값을 가지도록 원할 수 있습니다.

이러한 경우에는 Fixture Monkey의 `set()` 메서드로 프로퍼티의 값을 `Arbitrary`로 할당하여 랜덤 값을 가지도록 할 수 있습니다.
Jqwik의 [Arbitraries 클래스](https://jqwik.net/docs/current/user-guide.html#static-arbitraries-methods)의 정적 메서드를 호출하여 특정 조건을 충족하는 `Arbitrary`를 생성할 수 있습니다.

다음 코드 예제는 `Arbitrary`를 사용하여 `set()`을 통해 랜덤 값을 커스터마이징하는 방법을 보여줍니다:
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

[Jqwik 사용자 가이드](https://jqwik.net/docs/current/user-guide.html)에서 `Arbitrary`에 대한 자세한 내용을 확인하세요.
