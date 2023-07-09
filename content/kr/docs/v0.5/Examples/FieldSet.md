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
{{< tabpane >}}
{{< tab header="일반 표현식" lang="java" >}}

generateBuilder.set("value", "test");

{{< /tab >}}
{{< tab header="Kotlin Exp" lang="kotlin" >}}

generateBuilder.setExp(Generatevalue, "test");

{{< /tab >}}
{{< /tabpane >}}


### 필드 n번째 요소 값 설정


{{< tabpane >}}

{{< tab header="일반 표현식" lang="java" >}}


generateBuilder.set("values[n]", "test");


{{< /tab>}}

{{< tab header="Kotlin Exp" lang="kotlin" >}}


generateBuilder.setExp(Generate::values[n], "test");


{{< /tab >}}

{{< /tabpane >}}


### 필드 모든 요소 값 설정


{{< tabpane >}}

{{< tab header="일반 표현식" lang="java" >}}


generateBuilder.set("values[*]", "test");


{{< /tab>}}

{{< tab header="Kotlin Exp" lang="kotlin" >}}


generateBuilder.setExp(Generate::values["*"], "test");


{{< /tab >}}

{{< /tabpane >}}


### 임의의 필드 값 설정


{{< tabpane >}}

{{< tab header="일반 표현식" lang="java" >}}


generateBuilder.set("value", Arbitraries.strings());


{{< /tab>}}

{{< tab header="Kotlin Exp" lang="kotlin" >}}


generateBuilder.setExp(Generatevalue, Arbitraries.strings());


{{< /tab >}}

{{< /tabpane >}}


### 임의의 필드 n번째 요소 값 설정


{{< tabpane >}}

{{<tab header="일반 표현식" lang="java" >}}


generateBuilder.set("values[n]", Arbitraries.strings());

{{< /tab >}}

{{< tab header="Kotlin Exp" lang="kotlin" >}}


generateBuilder.setExp(Generate::values[n], Arbitraries.strings());


{{< /tab >}}

{{< /tabpane >}}


### 임의의 필드 모든 요소 값 설정


{{< tabpane >}}

{{< tab header="일반 표현식" lang="java" >}}


generateBuilder.set("values[*]", Arbitraries.strings());


{{< /tab >}}

{{< tab header="Kotlin Exp" lang="kotlin" >}}


generateBuilder.setExp(Generate::values["*"], Arbitraries.strings());


{{< /tab >}}

{{< /tabpane >}}
