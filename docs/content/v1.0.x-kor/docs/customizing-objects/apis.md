---
title: "커스터마이징 API"
weight: 41
menu:
docs:
  parent: "customizing-objects"
  identifier: "fixture-customization-apis"
---

Fixture Monkey는 ArbitraryBuilder를 통해 생성된 객체를 커스텀할 수 있는 다양한 API를 제공합니다.

## 픽스쳐 커스터마이징하기

### set()

`set()` 메서드는 [표현식](../expressions)에 참조된 하나 이상의 프로퍼티에 값을 설정하는 데 사용됩니다. 

`Supplier`, [`Arbitrary`](../arbitrary), `ArbitraryBuilder`, `NOT_NULL`, `NULL`, 또는 `Just` 를 포함한 다양한 타입을 값으로 설정할 수 있습니다.
또한 객체의 특정 인스턴스를 값으로 사용할 수도 있습니다.

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

> `set()`을 사용할 때 `Just`로 래핑된 객체를 사용하면 인스턴스를 분해하지 않고 값을 직접 설정할 수 있습니다.
> 일반적으로 `ArbitraryBuilder`에서 프로퍼티를 `set()`하면 주어진 인스턴스를 그대로 사용하지 않고 깊은 복사를 수행합니다.
> 따라서 인스턴스로 설정해야 하는 경우 `Values.just(instance)`를 사용해야 합니다.
> 이 기능은 Mocking 프레임워크를 사용할 때 Mock 인스턴스에 프로퍼티를 설정해야 하는 경우 유용합니다.

> `Just` 로 설정한 후에는 하위 속성을 변경할 수 없으니 유의하세요.

```java
Product product = fixture.giveMeBuilder(Product.class)
	  		  .set("options", Values.just(List.of("red", "medium", "adult"))
	  		  .set("options[0]", "blue")
	    		  .sample();
```

> 예를 들어, 위에서 생성된 Product 인스턴스의 options[0] 값은 "blue" 가 아닌 `Just`로 설정된 리스트로 유지됩니다.

### size(), minSize(), maxSize()

`size()` 메서드를 사용하면 컨테이너 프로퍼티의 크기를 지정할 수 있습니다.
정확한 크기를 설정하거나 최소값과 최대값을 사용하여 범위를 지정하는 등 유연하게 사용할 수 있습니다.

혹은 `minSize()` 또는 `maxSize()`를 사용하여 최소 또는 최대 컨테이너 크기만 설정할 수도 있습니다. (디폴트 설정은 0 ~ 3 입니다.)

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

때로는 속성을 항상 null로 설정하거나 항상 값이 존재하도록 보장하고 싶을 수 있습니다.
이러한 상황에서는 `setNull()` 또는 `setNotNull()`을 사용할 수 있습니다.

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

`setInner()`를 사용하면 `InnerSpec` 인스턴스에 정의된 커스텀을 빌더에 적용할 수 있습니다.
`InnerSpec` 은 타입에 독립적으로 사용 가능한 커스텀 명세입니다.

`InnerSpec` 인스턴스를 재사용하여 중첩 프로퍼티를 일관되고 쉽게 구성할 수 있습니다.
특히 Map의 속성을 커스텀할 때 유용합니다.

자세한 내용은 [InnerSpec](../innerspec) 을 참고하세요.

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

The `setLazy()` 함수는 Supplier에서 얻은 값을 프로퍼티에 할당합니다.
이 Supplier은 ArbitraryBuilder가 샘플링(`sample()`)될 때마다 실행됩니다.

이 함수는 고유한 순차 ID를 생성하거나 가장 최근 값으로 설정해야 할 때 특히 유용합니다.


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

`setPostCondition()`은 픽스처가 특정 조건을 준수해야 할 때 사용할 수 있습니다.
이 조건은 predicate를 전달하여 정의할 수 있습니다.


{{< alert icon="🚨" text="까다로운 조건에서 setPostCondition을 사용할 경우 비용이 더 많이 발생할 수 있습니다. 이러한 경우에는 대신 set를 사용하는 것이 좋습니다." />}}


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

`fixed()` 를 사용하면, ArbitraryBuilder가 샘플링될 때마다 동일한 값을 가진 인스턴스를 반환합니다.

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

`set()`, `setLazy()`, 및 `setPostCondition()` 메서드는 추가 매개변수를 통해 커스텀을 적용할 횟수를 제한할 수 있습니다.
표현식이 여러 프로퍼티를 참조하는 경우에 특히 유용합니다.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

fixtureMonkey.giveMeBuilder(Product.class)
  .set("options[*]", "red", 2); // options에 "red"는 2개까지만 설정될 수 있습니다.

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

fixtureMonkey.giveMeBuilder<Product>()
    .set("options[*]", "red", 2) // options에 "red"는 2개까지만 설정될 수 있습니다.

{{< /tab >}}
{{< /tabpane>}}


## 샘플링 결과를 활용해 추가 커스터마이징하기

### thenApply()

`thenApply()` 메서드는 빌더의 샘플링된 결과를 기반으로 필드를 커스텀해야 할 때 편리합니다.
예를 들어, 다음과 같이 `thenApply()`를 사용해 "productName" 필드를 생성된 Product의 "id"와 일치하도록 설정할 수 있습니다.


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

특정 조건에 따라 추가 커스텀을 수행해야 할 수도 있습니다.
이러한 경우 predicate가 충족될 때만 커스텀을 적용하는 `acceptIf()` 메서드를 활용할 수 있습니다.

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

## ArbitraryBuilder 타입 변환하기

### map()

`map()` 함수는 ArbitraryBuilder 의 타입을 다른 타입으로 변환하는 데 사용됩니다.


{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

fixtureMonkey.giveMeBuilder(Product.class)
    .map(Product::getId); // ArbitraryBuilder<Long> 타입으로 변환

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

fixtureMonkey.giveMeBuilder(Product::class.java)
    .map(Product::id) // ArbitraryBuilder<Long> 타입으로 변환

{{< /tab >}}
{{< /tabpane>}}


### zipWith()

`zipWith()` 은 여러 ArbitraryBuilder를 병합하여 다른 타입의 ArbitraryBuilder를 만들 때 유용합니다.
빌더들을 어떻게 결합할 지 명시해야 합니다.

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
