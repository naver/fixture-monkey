---
title: "자주 묻는 질문"
weight: 101
menu:
docs:
  parent: "cheat-sheet"
  identifier: "faq"
---

### 생성된 문자열의 문자 범위를 제한하려면 어떻게 해야 할까요?
> 관련 질문 - 생성된 인스턴트 값의 범위를 제한할 수 있나요?

일반적으로 픽스처에 제약 조건을 추가하고자 한다면 ArbitraryBuilder와 함께 제공된 Fixture Customization API를 사용할 수 있습니다.
그러나 각 생성된 기본 타입이 특정 제약 조건을 지키길 원하는 경우에는 [`javaTypeArbitaryGenerator`와 `javaTimeTypeArbitraryGenerator`](../../fixture-monkey-options/customization-options/#constraining-java-types)옵션을 사용할 수 있습니다.

`javaTypeArbitaryGenerator` 옵션을 사용하면 문자열 또는 정수와 같은 기본 타입의 초기값을 커스터마이징 할 수 있습니다. 인스턴트와 같은 시간 타입의 경우 `javaTimeTypeArbitaryGenerator` 옵션을 사용할 수 있습니다.

이 옵션은 문자열 생성시 특정 범위의 문자만 사용하려는 경우나 시간 타입을 특정한 범위 내로 생성하려는 경우에 특히 유용합니다.

### 특정 값이 생성되지 않도록 하려면 어떻게 해야 하나요?
[`set()`](../../customizing-objects/apis/#set)을 사용하여 값을 쉽게 지정할 수 있지만, 값을 지정하는 것이 아니라 특정 값이 생성되지 않도록 제외하고 싶은 경우가 있을 수 있습니다.

예를 들어, 클래스에 Enum 타입의 필드가 있고 그 필드가 특정 값을 갖지 않게 하려면, 아래와 같이 [`set()`](.../.../customizing-objects/apis/#set)를 사용할 수 있습니다.

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

혹은 일종의 필터처럼 동작하는 [`setPostCondition()`](../../customizing-objects/apis/#setpostcondition)을 사용할 수 있습니다.

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

`setPostCondition()`을 사용하면 Product 인스턴스가 생성된 후 필터링되므로 제한적인 조건에서 더 높은 비용이 발생할 수 있습니다. 이러한 경우 `set()`을 대신 사용하는 것이 권장됩니다.

### 필드 중 하나가 다른 필드의 값에 의존하고 있습니다. 이러한 픽스처를 커스터마이징 하려면 어떻게 해야 하나요?

`thenApply()` 메서드는 다른 필드에 의존하는 필드를 커스터마이징 할 때 유용합니다. 자세한 내용은 [`thenApply()`](../../customizing-objects/apis/#thenapply)을 확인해주세요.
