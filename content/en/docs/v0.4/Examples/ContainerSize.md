---
title: "Altering Container Size"
weight: 8
---

{{< alert color="secondary" title="Background">}}
In Fixture Monkey Container means data structures including Collection.
ex. Map, Optional, Set, List...

user-defined data structures are also considered as a Container
{{< /alert >}}

{{< alert color="primary" title="Tip">}}
This practice deals with manipulation `size`.
{{< /alert >}}


## 0. Class

```java
public class Generate {
	List<GenerateElement> values;
}

public class GenerateElement {
	List<String> values;
}
```

## 1. Generating ArbitraryBuilder

```java
ArbitraryBuilder<Generate> generateBuilder = fixtureMonkey.giveMeBuilder(Generate.class);
```

## 2-1. Alter field size

{{< tabpane >}}
{{< tab header="general expression" lang="java" >}}

generateBuilder.size("values", 5);

{{< /tab >}}
{{< tab header="Kotlin Exp" lang="kotlin" >}}

generateBuilder.sizeExp(Generate::values, 5);

{{< /tab >}}
{{< /tabpane >}}


## 2-2. Alter field n-th element size

{{< tabpane >}}
{{< tab header="general expression" lang="java" >}}

generateBuilder.size("values[n]", 5);

{{< /tab >}}
{{< tab header="Kotlin Exp" lang="kotlin" >}}

generateBuilder.sizeExp(Generate::values[n], 5);

{{< /tab >}}
{{< /tabpane >}}

## 2-3. Alter field all elements size

{{< tabpane >}}
{{< tab header="general expression" lang="java" >}}

generateBuilder.size("values[*]", 5);

{{< /tab >}}
{{< tab header="Kotlin Exp" lang="kotlin" >}}

generateBuilder.sizeExp(Generate::values["*"], 5);

{{< /tab >}}
{{< /tabpane >}}
