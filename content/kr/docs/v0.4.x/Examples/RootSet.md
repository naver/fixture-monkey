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

## 연산
### 1. ArbitraryBuilder 정의

```java
ArbitraryBuilder<Generate> generateBuilder = fixtureMonkey.giveMeBuilder(Generate.class);
```

### 2. 객체 값 설정

```java

Generate generateObject = new Generate("test"); 

```



{{< tabpane lang="java" >}}

{{< tab header="일반 표현식" >}}


generateBuilder.set(generateObject);


{{< /tab>}}

{{< tab header="Exp" >}}


generateBuilder.set(generateObject);


{{< /tab >}}

{{< /tabpane>}}


{{< tabpane lang="java" >}}

{{< tab header="일반 표현식" >}}


generateBuilder.set("$", generateObject);


{{< /tab>}}

{{< tab header="Exp" >}}


미지원


{{< /tab >}}

{{< /tabpane>}}
