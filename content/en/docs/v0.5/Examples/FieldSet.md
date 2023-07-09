---
title: "Setting field value"
weight: 5
---

{{< alert color="primary" title="Tip">}}
This practice deals with manipulation `set`
{{< /alert >}}

## 0. Class

```java
public class Generate {
	String value;

	List<String> values;
}
```

## 1. Generating ArbitraryBuilder

```java
ArbitraryBuilder<Generate> generateBuilder = fixtureMonkey.giveMeBuilder(Generate.class);
```

## 2. Setting value
### Setting field
{{< tabpane >}}
{{< tab header="general expression" lang="java" >}}

generateBuilder.set("value", "test");

{{< /tab >}}
{{< tab header="Kotlin Exp" lang="kotlin" >}}

generateBuilder.setExp(Generate::value, "test");

{{< /tab >}}
{{< /tabpane >}}


### Setting field n-th element


{{< tabpane >}}

{{< tab header="general expression" lang="java" >}}


generateBuilder.set("values[n]", "test");


{{< /tab>}}

{{< tab header="Kotlin Exp" lang="kotlin" >}}


generateBuilder.setExp(Generate::values[n], "test");


{{< /tab >}}

{{< /tabpane >}}


### Setting field all elements


{{< tabpane >}}

{{< tab header="general expression" lang="java" >}}


generateBuilder.set("values[*]", "test");


{{< /tab>}}

{{< tab header="Kotlin Exp" lang="kotlin" >}}


generateBuilder.setExp(Generate::values["*"], "test");


{{< /tab >}}

{{< /tabpane >}}


### Setting field to arbitrary value


{{< tabpane >}}

{{< tab header="general expression" lang="java" >}}


generateBuilder.set("value", Arbitraries.strings());


{{< /tab>}}

{{< tab header="Kotlin Exp" lang="kotlin" >}}


generateBuilder.setExp(Generate::value, Arbitraries.strings());


{{< /tab >}}

{{< /tabpane >}}


### Setting field n-th element to arbitrary value


{{< tabpane >}}

{{<tab header="general expression" lang="java" >}}


generateBuilder.set("values[n]", Arbitraries.strings());

{{< /tab >}}

{{< tab header="Kotlin Exp" lang="kotlin" >}}


generateBuilder.setExp(Generate::values[n], Arbitraries.strings());


{{< /tab >}}

{{< /tabpane >}}


### Setting field all elements to arbitrary value


{{< tabpane >}}

{{< tab header="general expression" lang="java" >}}


generateBuilder.set("values[*]", Arbitraries.strings());


{{< /tab >}}

{{< tab header="Kotlin Exp" lang="kotlin" >}}


generateBuilder.setExp(Generate::values["*"], Arbitraries.strings());


{{< /tab >}}

{{< /tabpane >}}
