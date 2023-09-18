---
title: "InnerSpec"
weight: 44
menu:
docs:
parent: "customizing-objects"
identifier: "innerspec"
---

An InnerSpec is a type-independent specification for the customizations you wish to apply.
Using the `setInner()` method within ArbitraryBuilder, you can apply customizations defined within an `InnerSpec` instance into your builder.

`InnerSpec` holds customization details and can be reused on ArbitraryBuilders.
Unlike using `expressions` used in ArbitraryBuilder, `InnerSpec` enables a more nested and structured approach.

An added advantage of InnerSpec is its ability to customize map properties, unlike normal expressions.

{{< alert icon="💡" text="Kotlin EXP is not supported for InnerSpec, as it is designed to be type-independent. Instead, you need to specify the property by its name." />}}

## Applying InnerSpec to the ArbitraryBuilder

To apply your pre-defined InnerSpec to the builder, use the `setInner()` method as shown below:

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

## Customizing properties

### property()

Similar to the `set()` method in ArbitraryBuilder, you can customize a property by specifying its name and providing the desired value.

{{< alert icon="🚨" text="Fixture Monkey expressions such as refering elements (`[]`) or nested fields(`.`) are not allowed as the property name. Only the property name itself is allowed." />}}

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

`size()`, `minSize()`, and `maxSize()` can be used to specify the size of the property.

As previously mentioned, InnerSpec defines customizations in a nested manner.
You can first select the container property using `property()` and then proceed to define an innerSpec consumer to set the size.

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

`postCondition()` can be used when you require your property to match a specific condition.

{{< alert icon="🚨" text="Using setPostCondition can incur higher costs for narrow conditions. In such cases, it's recommended to use set instead." />}}

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

Individual elements within lists can be selected using `listElement()`.
This is equivalent to referencing elements with "[n]" using `expressions`.

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

If you wish to set all elements of the list simultaneously, you can use `allListElement()`.
This is equivalent to referencing elements with "[*]" using `expressions`.

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

InnerSpec provides special methods for customizing map property entries.

{{< alert icon="🚨" text="Similar to lists, setting a map entry without specifying the size first might lead to no change. Prior to setting a value, ensure that the map property has the intended size." />}}

### key(), value(), entry()

You can customize map property entries using `key()`, `value()`, and `entry()` methods.
Using `key()` assigns a specified value to the key of a map entry, while the entry's value remains randomized.
Similarly, `value()` assigns a specified value to the map entry's value, while the key becomes randomized.
If you want to specify both the key and value at once, you can use `entry()`.

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

When setting multiple entries within a map, you can use `keys()`, `values()`, and `entries()` to pass multiple values.

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

Similar to `allListElement()`, it is possible to set every entry within the map to the specified value with `allKey()`, `allValue()`, and `allEntry()`.

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

Similar to the `setLazy()` method in ArbitraryBuilder, you can pass a Supplier to assign the value.
The Supplier will run every time the ArbitraryBuilder with the `InnerSpec` applied is sampled.

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
