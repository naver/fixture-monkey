---
title: "InnerSpec"
weight: 44
menu:
docs:
parent: "customizing-objects"
identifier: "innerspec"
---

InnerSpec 은 적용하고자 하는 커스터마이징에 대한 타입에 독립적인 명세입니다.
ArbitraryBuilder 내부의 `setInner()` 메서드를 사용하여 `InnerSpec` 인스턴스 내에 정의된 커스터마이징을 빌더에 적용할 수 있습니다.

`InnerSpec` 에는 커스터마이징 세부사항이 포함되어 있으며, 재사용이 가능합니다.
ArbitraryBuilder 에서 사용하는 `expressions` 과 달리, `InnerSpec` 은 더 중첩된 구조적 접근을 가능하게 합니다.

`InnerSpec` 의 또 다른 장점은 일반적인 표현식과 달리 맵 프로퍼티를 커스터마이징할 수 있다는 점입니다.

{{< alert icon="💡" text="Kotlin EXP 는 InnerSpec 에서 지원하지 않습니다. InnerSpec 은 타입에 독립적으로 설계되었기 때문에, 프로퍼티 이름을 통해 접근해야 합니다." />}}

## ArbitraryBuilder 에 InnerSpec 적용하기

빌더에 미리 정의된 `InnerSpec` 을 적용하려면 다음과 같이 `setInner()` 메서드를 사용하세요.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

InnerSpec innerSpec = new InnerSpec().property("id", 1000);

fixtureMonkey.giveMeBuilder(Product.class)
.setInner(innerSpec);

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val innerSpec = InnerSpec().property("id", 1000)

fixtureMonkey.giveMeBuilder<Product>()
.setInner(innerSpec)

{{< /tab >}}
{{< /tabpane>}}

## 프로퍼티 사용자 정의하기

### property()

ArbitraryBuilder 의 `set()` 메서드와 유사하게, 프로퍼티 이름과 원하는 값을 지정하여 프로퍼티를 커스터마이징할 수 있습니다.

{{< alert icon="🚨" text="원소(`[]`) 또는 중첩 필드(`.`)를 참조하는 Fixture Monkey 표현식은 프로퍼티 이름으로 사용할 수 없습니다. 프로퍼티 이름 자체만 사용할 수 있습니다." />}}

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

InnerSpec innerSpec = new InnerSpec()
.property("id", 1000);

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val innerSpec = InnerSpec()
.property("id", 1000)

{{< /tab >}}
{{< /tabpane>}}

### size(), minSize(), maxSize()

`size()`, `minSize()`, 그리고 `maxSize()` 는 프로퍼티의 크기를 지정하는 데 사용할 수 있습니다.

앞서 언급했듯이, InnerSpec 은 중첩된 방식으로 커스터마이징을 정의합니다.
`property()` 를 사용하여 컨테이너 프로퍼티를 먼저 선택한 다음, 내부에 정의된 `innerSpec` 컨슈머를 사용하여 크기를 설정할 수 있습니다.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

InnerSpec innerSpec = new InnerSpec()
.property("options", options -> options.size(5)); // size:5

InnerSpec innerSpec = new InnerSpec()
.property("options", options -> options.size(3, 5)); // minSize:3, maxSize:5

InnerSpec innerSpec = new InnerSpec()
.property("options", options -> options.minSize(3)); // minSize:3

InnerSpec innerSpec = new InnerSpec()
.property("options", options -> options.maxSize(5)); // maxSize:5

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val innerSpec = InnerSpec()
.property("options") { it.size(5) } // size:5

val innerSpec = InnerSpec()
.property("options") { it.size(3, 5) } // minSize:3, maxSize:5

val innerSpec = InnerSpec()
.property("options") { it.minSize(3) } // minSize:3

val innerSpec = InnerSpec()
.property("options") { it.maxSize(5) } // maxSize:5

{{< /tab >}}
{{< /tabpane>}}

### postCondition()

`postCondition()` 은 프로퍼티가 특정 조건을 만족해야 하는 경우 사용할 수 있습니다.

{{< alert icon="🚨" text="setPostCondition 의 조건을 너무 좁게 설정하면, 생성 비용이 매우 높아질 수 있습니다. 이런 경우 set 을 사용해주세요." />}}

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

InnerSpec innerSpec = new InnerSpec()
.property("id", id -> id.postCondition(Long.class, it -> it > 0));

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val innerSpec = InnerSpec()
.property("id") { it.postCondition(Long::class.java) { it > 0 }}

{{< /tab >}}
{{< /tabpane>}}

### inner()

You can also customize a property using another pre-defined InnerSpec with the help of `inner()`.
또한 `inner()` 를 사용하여 미리 정의된 InnerSpec 을 사용하여 프로퍼티를 커스터마이징할 수 있습니다.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

InnerSpec innerSpec = new InnerSpec()
.property("id", 1000L);

fixtureMonkey.giveMeBuilder(Product.class)
.setInner(
new InnerSpec()
.property("nestedObject", nestedObject -> nestedObject.inner(innerSpec))
);

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val innerSpec = InnerSpec()
.property("id", 1000L)

fixtureMonkey.giveMeBuilder<Product>()
.setInner(
InnerSpec()
.property("nestedObject") { it.inner(innerSpec) }
)

{{< /tab >}}
{{< /tabpane>}}

## Customizing list properties

### listElement()

목록 내의 개별 요소는 `listElement()`를 사용하여 선택할 수 있습니다.
이는 `expression`을 사용하여 "[n]"으로 요소를 참조하는 것과 동일합니다.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

InnerSpec innerSpec = new InnerSpec()
.property("options", options -> options.listElement(0, "red"));

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val innerSpec = InnerSpec()
.property("options") { it.listElement(0, "red") }

{{< /tab >}}
{{< /tabpane>}}

### allListElement()

만약 목록의 모든 요소를 동시에 설정하려면 `allListElement()`를 사용할 수 있습니다.
이는 `expressions`를 사용하여 "[*]"로 요소를 참조하는 것과 동일합니다.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

InnerSpec innerSpec = new InnerSpec()
.property("options", options -> options.allListElement("red"));

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val innerSpec = InnerSpec()
.property("options") { it.allListElement("red") }

{{< /tab >}}
{{< /tabpane>}}

## Customizing map properties

InnerSpec은 맵 프로퍼티 엔트리를 커스터마이징하기 위해 특별한 메소드를 제공합니다.

{{< alert icon="🚨" text="Similar to lists, setting a map entry without specifying the size first might lead to no change. Prior to setting a value, ensure that the map property has the intended size." />}}

### key(), value(), entry()

`key()`, `value()`, `entry()` 메소드를 사용하여 맵 프로퍼티 엔트리를 커스터마이징할 수 있습니다.
`key()`를 사용하면 맵 엔트리의 키에 지정된 값을 할당하고, 엔트리의 값은 무작위로 설정됩니다.
마찬가지로, `value()`를 사용하면 맵 엔트리의 값에 지정된 값을 할당하고, 키는 무작위로 설정됩니다.
키와 값을 동시에 지정하려면 `entry()`를 사용할 수 있습니다.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

InnerSpec innerSpec = new InnerSpec()
.property("merchantInfo", merchantInfo -> merchantInfo.key(1000));

InnerSpec innerSpec = new InnerSpec()
.property("merchantInfo", merchantInfo -> merchantInfo.value("ABC Store"));

InnerSpec innerSpec = new InnerSpec()
.property("merchantInfo", merchantInfo -> merchantInfo.entry(1000, "ABC Store"));

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val innerSpec = InnerSpec()
.property("merchantInfo") { it.key(1000) }

val innerSpec = InnerSpec()
.property("merchantInfo") { it.value("ABC Store") }

val innerSpec = InnerSpec()
.property("merchantInfo") { it.entry(1000, "ABC Store") }

{{< /tab >}}
{{< /tabpane>}}

### keys(), values(), entries()

맵 내의 여러 개의 엔트리를 설정할 때 `keys()`, `values()`, `entries()`를 사용하여 여러 값을 전달할 수 있습니다.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

InnerSpec innerSpec = new InnerSpec()
.property("merchantInfo", merchantInfo -> merchantInfo.keys(1000, 1001, 1002));

InnerSpec innerSpec = new InnerSpec()
.property("merchantInfo", merchantInfo -> merchantInfo.values("ABC Store", "123 Convenience", "XYZ Mart"));

InnerSpec innerSpec = new InnerSpec()
.property("merchantInfo", merchantInfo -> merchantInfo.entries(1000, "ABC Store", 1001, "123 Convenience", 1002, "XYZ Mart"));

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val innerSpec = InnerSpec()
.property("merchantInfo") { it.keys(1000, 1001, 1002) }

val innerSpec = InnerSpec()
.property("merchantInfo") { it.values("ABC Store", "123 Convenience", "XYZ Mart") }

val innerSpec = InnerSpec()
.property("merchantInfo") { it.entries(1000, "ABC Store", 1001, "123 Convenience", 1002, "XYZ Mart") }

{{< /tab >}}
{{< /tabpane>}}

### allKey(), allValue(), allEntry()

`allListElement()`와 유사하게, `allKey()`, `allValue()`, `allEntry()`를 사용하여 맵 내의 모든 엔트리를 지정된 값으로 설정할 수 있습니다.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

InnerSpec innerSpec = new InnerSpec()
.property("merchantInfo", merchantInfo -> merchantInfo.allKey(1000));

InnerSpec innerSpec = new InnerSpec()
.property("merchantInfo", merchantInfo -> merchantInfo.allValue("ABC Store"));

InnerSpec innerSpec = new InnerSpec()
.property("merchantInfo", merchantInfo -> merchantInfo.allEntry(1000, "ABC Store"));

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val innerSpec = InnerSpec()
.property("merchantInfo") { it.allKey(1000) }

val innerSpec = InnerSpec()
.property("merchantInfo") { it.allValue("ABC Store") }

val innerSpec = InnerSpec()
.property("merchantInfo") { it.allEntry(1000, "ABC Store") }

{{< /tab >}}
{{< /tabpane>}}

### keyLazy(), valueLazy(), entryLazy()

ArbitraryBuilder의 `setLazy()` 메소드와 유사하게, Supplier를 전달하여 값을 할당할 수 있습니다.
Supplier는 `InnerSpec`이 적용된 ArbitraryBuilder가 샘플링될 때마다 실행됩니다.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

InnerSpec innerSpec = new InnerSpec()
.property("merchantInfo", merchantInfo -> merchantInfo.keyLazy(this::generateMerchantKey));

InnerSpec innerSpec = new InnerSpec()
.property("merchantInfo", merchantInfo -> merchantInfo.valueLazy(this::generateMerchantValue));

InnerSpec innerSpec = new InnerSpec()
.property("merchantInfo", merchantInfo -> merchantInfo.entryLazy(this::generateMerchantKey, this::generateMerchantValue));

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val innerSpec = InnerSpec()
.property("merchantInfo") { it.keyLazy(this::generateMerchantKey) }

val innerSpec = InnerSpec()
.property("merchantInfo") { it.valueLazy(this::generateMerchantValue) }

val innerSpec = InnerSpec()
.property("merchantInfo") { it.entryLazy(this::generateMerchantKey, this::generateMerchantValue) }

{{< /tab >}}
{{< /tabpane>}}

### allKeyLazy(), allValueLazy(), allEntryLazy()

Just as with the `allKey()` method, you can use `allKeyLazy()` to apply `keyLazy()` to every entry within the map.
Both `allValueLazy()` and `allEntryLazy()` function similarly.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

InnerSpec innerSpec = new InnerSpec()
.property("merchantInfo", merchantInfo -> merchantInfo.allKeyLazy(this::generateMerchantKey));

InnerSpec innerSpec = new InnerSpec()
.property("merchantInfo", merchantInfo -> merchantInfo.allValueLazy(this::generateMerchantValue));

InnerSpec innerSpec = new InnerSpec()
.property("merchantInfo", merchantInfo -> merchantInfo.allEntryLazy(this::generateMerchantKey, this::generateMerchantValue));

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val innerSpec = InnerSpec()
.property("merchantInfo") { it.allKeyLazy(this::generateMerchantKey) }

val innerSpec = InnerSpec()
.property("merchantInfo") { it.allValueLazy(this::generateMerchantValue) }

val innerSpec = InnerSpec()
.property("merchantInfo") { it.allEntryLazy(this::generateMerchantKey, this::generateMerchantValue) }

{{< /tab >}}
{{< /tabpane>}}

## Customizing nested Maps

By combining methods within InnerSpec, you can effectively customize maps with map-type keys, map-type values, or both.

Consider the scenario of a nested map structure like the following:

```java
public class Example {
    Map<Map<String, String>, String> mapByString;
    Map<String, Map<String, String>> stringByMap;
}
```

### Setting map-type key
To set a map with a map-type key, you can access the map key using `key()`, and then further customize it.

{{< tabpane persistLang=false >}}
{{< tab header="general expression" lang="java">}}

InnerSpec().property("mapByString", m -> m.key(k -> k.entry("key", "value")));

{{< /tab >}}
{{< tab header="Kotlin Exp" lang="kotlin">}}

InnerSpec().property("mapByString") { m -> m.key { k -> k.entry("key", "value") } }

{{< /tab >}}
{{< /tabpane>}}

If you need to set the entry itself, access the entry with `entry()` and further customize the key using InnerSpec, then set the specific value.

{{< tabpane persistLang=false >}}
{{< tab header="general expression" lang="java">}}

InnerSpec().property("mapByString", m -> m.entry(k -> k.entry("innerKey", "innerValue")), "value")

{{< /tab >}}
{{< tab header="Kotlin Exp" lang="kotlin">}}

InnerSpec().property("mapByString") { m -> m.entry({ k -> k.entry("innerKey", "innerValue") }, "value") }

{{< /tab >}}
{{< /tabpane>}}

### Setting map-type value
For a map with a map-type value, access the map value using `value()`, and then further customize it.

{{< tabpane persistLang=false >}}
{{< tab header="general expression" lang="java">}}

InnerSpec().property("stringByMap", m -> m.value(v -> v.entry("key", "value")))

{{< /tab >}}
{{< tab header="Kotlin Exp" lang="kotlin">}}

InnerSpec().property("stringByMap") { m -> m.value { v -> v.entry("key", "value") } }

{{< /tab >}}
{{< /tabpane>}}

If you need to set the entry itself, access the entry with `entry()` and further customize the value using InnerSpec, then set the specific key.

{{< tabpane persistLang=false >}}
{{< tab header="general expression" lang="java">}}

InnerSpec().property("stringByMap", m -> m.entry("key", v -> v.entry("innerKey", "innerValue")))

{{< /tab >}}
{{< tab header="Kotlin Exp" lang="kotlin">}}

InnerSpec().property("stringByMap") { m -> m.entry("key") {v -> v.entry("innerKey", "innerValue")} }

{{< /tab >}}
{{< /tabpane>}}
