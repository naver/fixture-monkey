---
title: "객체 값 설정"
weight: 6
---

{{< alert color="primary" title="Tip">}}
이 문서에서 설명하는 연산은 `set` 입니다.
{{< /alert >}}

## 0. 클래스

```java
public class Generate {
	String value;
}
```

## 1. ArbitraryBuilder 정의

```java
ArbitraryBuilder<Generate> generateBuilder = fixtureMonkey.giveMeBuilder(Generate.class);
```

### 2. 객체 값 설정

```java
Generate generateObject = new Generate("test");
```



{{< tabpane >}}

{{< tab header="일반 표현식" lang="java" >}}


generateBuilder.set(generateObject);


{{< /tab>}}

{{< tab header="Kotlin Exp" lang="kotlin" >}}


generateBuilder.set(generateObject);


{{< /tab >}}

{{< /tabpane >}}


{{< tabpane >}}

{{< tab header="일반 표현식" lang="java" >}}


generateBuilder.set("$", generateObject);


{{< /tab>}}

{{< tab header="Kotlin Exp" lang="kotlin" >}}


미지원


{{< /tab >}}

{{< /tabpane >}}
