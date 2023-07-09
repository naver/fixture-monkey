---
title: "Setting object"
weight: 6
---

{{< alert color="primary" title="Tip">}}
This practice deals with manipulation `set`
{{< /alert >}}

## 0. Class

```java
public class Generate {
	String value;
}
```

## 1. Generating ArbitraryBuilder

```java
ArbitraryBuilder<Generate> generateBuilder = fixtureMonkey.giveMeBuilder(Generate.class);
```

## 2. Setting object

```java
Generate generateObject = new Generate("test");
```



{{< tabpane >}}

{{< tab header="general expression" lang="java" >}}


generateBuilder.set(generateObject);


{{< /tab>}}

{{< tab header="Kotlin Exp" lang="kotlin" >}}


generateBuilder.set(generateObject);


{{< /tab >}}

{{< /tabpane >}}


{{< tabpane >}}

{{< tab header="general expression" lang="java" >}}


generateBuilder.set("$", generateObject);


{{< /tab>}}

{{< tab header="Kotlin Exp" lang="kotlin" >}}


Not supporting yet


{{< /tab >}}

{{< /tabpane >}}
