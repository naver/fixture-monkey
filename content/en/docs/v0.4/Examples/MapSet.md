---
title: "Setting Map"
weight: 6
---

{{< alert color="primary" title="Tip">}}
This practice deals with manipulation `setInner`
{{< /alert >}}

## 0. Class

```java
public class Generate {
    Map<String, String> namesById;
}
```

## 1. Generating ArbitraryBuilder

```java
ArbitraryBuilder<Generate> generateBuilder = fixtureMonkey.giveMeBuilder(Generate.class);
```

## 2. Altering map size
{{< alert color="primary" title="Tip">}}
Setting map without size would result in no change. 

Please check out if using `size` before setting value
{{< /alert >}}

{{< tabpane persistLang=false >}}
{{< tab header="general expression" lang="java">}}

generateBuilder.setInner(
    InnerSpec().property("namesById", m -> m.size(5))
);

{{< /tab >}}
{{< tab header="Kotlin Exp" lang="kotlin">}}

generateBuilder.setInner(
    InnerSpec().property("namesById") { m -> m.size(5) }
)

{{< /tab >}}
{{< /tabpane >}}

## 3. Setting field
### Setting entry
{{< tabpane persistLang=false >}}
{{< tab header="general expression" lang="java">}}

generateBuilder.setInner(
    InnerSpec().property("namesById", m -> m.entry("key", "value"))
);

{{< /tab >}}
{{< tab header="Kotlin Exp" lang="kotlin">}}

generateBuilder.setInner(
    InnerSpec().property("namesById") { m -> m.entry("key", "value") }
)

{{< /tab >}}
{{< /tabpane >}}

### Setting map key
Setting Map key
{{< tabpane persistLang=false >}}
{{< tab header="general expression" lang="java">}}

generateBuilder.setInner(
    InnerSpec().property("namesById", m -> m.key("key"))
);

{{< /tab >}}
{{< tab header="Kotlin Exp" lang="kotlin">}}

generateBuilder.setInner(
    InnerSpec().property("namesById") { m -> m.key("key") }
)

{{< /tab >}}
{{< /tabpane >}}

### Setting map value 
Setting Map value
{{< tabpane persistLang=false >}}
{{< tab header="general expression" lang="java">}}

generateBuilder.setInner(
    InnerSpec().property("namesById", m -> m.value("value"))
);

{{< /tab >}}
{{< tab header="Kotlin Exp" lang="kotlin">}}

generateBuilder.setInner(
    InnerSpec().property("namesById") { m -> m.value("value") }
)

{{< /tab >}}
{{< /tabpane >}}
