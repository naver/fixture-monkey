---
title: "맵 변경"
weight: 6
---

{{< alert color="primary" title="Tip">}}
이 문서에서 설명하는 연산은 `setInner` 입니다.
{{< /alert >}}

## 0. 클래스

```java
public class Generate {
	Map<String, String> namesById;
}
```

## 1. ArbitraryBuilder 정의

```java
ArbitraryBuilder<Generate> generateBuilder = fixtureMonkey.giveMeBuilder(Generate.class);
```

