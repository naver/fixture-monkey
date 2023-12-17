---
title: "Setting object"
weight: 36
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



{{< tabpane lang="java" >}}

{{< tab header="general expression" >}}


generateBuilder.set(generateObject);


{{< /tab>}}

{{< tab header="Kotlin Exp" >}}


generateBuilder.set(generateObject);


{{< /tab >}}

{{< /tabpane>}}


{{< tabpane lang="java" >}}

{{< tab header="general expression" >}}


generateBuilder.set("$", generateObject);


{{< /tab>}}

{{< tab header="Kotlin Exp" >}}


Not supporting yet


{{< /tab >}}

{{< /tabpane>}}
