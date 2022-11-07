---
title: "객체 필드 변경"
weight: 5
---

{{< alert color="primary" title="Tip">}}
이 문서에서 설명하는 연산은 `set` 입니다.
{{< /alert >}}

## 0. 클래스

```java
public class Generate {
	String value;

	List<String> values;
}
```

## 1. ArbitraryBuilder 정의

```java
ArbitraryBuilder<Generate> generateBuilder = fixtureMonkey.giveMeBuilder(Generate.class);
```

## 2. 값 설정
### 필드 값 설정
{{< tabpane lang = "java" >}}
{{< tab header ="일반 표현식" >}}

generateBuilder.set("value", "test");

{{< /tab >}}
{{< tab header= "Kotlin Exp" >}}

generateBuilder.setExp(Generate::getValue, "test");

{{< /tab >}}
{{< /tabpane>}}


### 필드 n번째 요소 값 설정


{{< tabpane lang = "java" >}}

{{< tab header ="일반 표현식" >}}


generateBuilder.set("values[n]", "test");


{{< /tab>}}

{{< tab header= "Kotlin Exp" >}}


generateBuilder.setExp(Generate::getValues[n], "test");


{{< /tab >}}

{{< /tabpane>}}


### 필드 모든 요소 값 설정


{{< tabpane lang = "java">}}

{{< tab header ="일반 표현식">}}


generateBuilder.set("values[*]", "test");


{{< /tab>}}

{{< tab header="Kotlin Exp" >}}


generateBuilder.setExp(Generate::getValues["*"], "test");


{{< /tab >}}

{{< /tabpane>}}


### 임의의 필드 값 설정


{{< tabpane lang="java" >}}

{{< tab header="일반 표현식">}}


generateBuilder.set("value", Arbitraries.strings());


{{< /tab>}}

{{< tab header="Kotlin Exp" >}}


generateBuilder.setExp(Generate::getValue, Arbitraries.strings());


{{< /tab >}}

{{< /tabpane>}}


### 임의의 필드 n번째 요소 값 설정


{{< tabpane lang="java" >}}

{{<tab header="일반 표현식" >}}


generateBuilder.set("values[n]", Arbitraries.strings());

{{< /tab >}}

{{< tab header="Kotlin Exp" >}}


generateBuilder.setExp(Generate::getValues[n], Arbitraries.strings());


{{< /tab >}}

{{< /tabpane>}}


### 임의의 필드 모든 요소 값 설정


{{< tabpane lang="java" >}}

{{< tab header="일반 표현식" >}}


generateBuilder.set("values[*]", Arbitraries.strings());


{{< /tab >}}

{{< tab header="Kotlin Exp" >}}


generateBuilder.setExp(Generate::getValues["*"], Arbitraries.strings());


{{< /tab >}}

{{< /tabpane>}}
