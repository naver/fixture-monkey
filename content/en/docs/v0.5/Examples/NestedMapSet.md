---
title: "Setting Nested Map"
weight: 7
---

{{< alert color="primary" title="Tip">}}
This practice deals with manipulation `setInner`
{{< /alert >}}

## 0. Class
It works even if key is map type or value is map type.

```java
public class Generate {
	Map<Map<String, String>, String> mapByString;
	Map<String, Map<String, String>> stringByMap;
}
```

### Setting nested map key
{{< tabpane persistLang=false >}}
{{< tab header="general expression" lang="java">}}

generateBuilder.setInner(
    InnerSpec().property("mapByString", m -> m.key(k -> k.entry("key", "value")))
);

{{< /tab >}}
{{< tab header="Kotlin Exp" lang="kotlin">}}

generateBuilder.setInner(
    InnerSpec().property("mapByString") { m -> m.key { k -> k.entry("key", "value") } }
);

{{< /tab >}}
{{< /tabpane >}}

If setting value given key, check out `entry` just as shown below

{{< tabpane persistLang=false >}}
{{< tab header="general expression" lang="java">}}

generateBuilder.setInner(
    InnerSpec().property("mapByString", m -> m.entry(k -> k.entry("innerKey", "innerValue")), "value")
);

{{< /tab >}}
{{< tab header="Kotlin Exp" lang="kotlin">}}

generateBuilder.setInner(
    InnerSpec().property("mapByString") { m -> m.entry({ k -> k.entry("innerKey", "innerValue") }, "value") }
);

{{< /tab >}}
{{< /tabpane >}}

### Setting nested map value
{{< tabpane persistLang=false >}}
{{< tab header="general expression" lang="java">}}

generateBuilder.setInner(
    InnerSpec().property("stringByMap", m -> m.value(v -> v.entry("key", "value")))
);

{{< /tab >}}
{{< tab header="Kotlin Exp" lang="kotlin">}}

generateBuilder.setInner(
    InnerSpec().property("stringByMap") { m -> m.value { v -> v.entry("key", "value") } }
);

{{< /tab >}}
{{< /tabpane >}}

If setting key given value, check out `entry` just as shown below

{{< tabpane persistLang=false >}}
{{< tab header="general expression" lang="java">}}

generateBuilder.setInner(
    InnerSpec().property("stringByMap", m -> m.entry("key", v -> v.entry("innerKey", "innerValue")))
);

{{< /tab >}}
{{< tab header="Kotlin Exp" lang="kotlin">}}

generateBuilder.setInner(
    InnerSpec().property("stringByMap") { m -> m.entry("key") {v -> v.entry("innerKey", "innerValue")} }
);

{{< /tab >}}
{{< /tabpane >}}
