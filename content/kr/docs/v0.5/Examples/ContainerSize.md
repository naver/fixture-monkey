---
title: "컨테이너 사이즈 변경"
weight: 8
---

{{< alert color="secondary" title="Background">}}
Fixture Monkey에서 컨테이너는 Collection을 포함한 자료구조를 의미합니다.
ex. Map, Optional, Set, List...

사용자가 정의한 자료구조도 컨테이너입니다.
{{< /alert >}}

{{< alert color="primary" title="Tip">}}
이 문서에서 설명하는 연산은 `size` 입니다.
{{< /alert >}}


## 0. 클래스

```java
public class Generate {
	List<GenerateElement> values;
}

public class GenerateElement {
	List<String> values;
}
```

## 1. ArbitraryBuilder 정의

```java
ArbitraryBuilder<Generate> generateBuilder = fixtureMonkey.giveMeBuilder(Generate.class);
```

## 2-1. 필드 사이즈 변경

{{< tabpane >}}
{{< tab header="일반 표현식" lang="java" >}}

generateBuilder.size("values", 5);

{{< /tab >}}
{{< tab header="Kotlin Exp" lang="kotlin" >}}

generateBuilder.sizeExp(Generate::values, 5);

{{< /tab >}}
{{< /tabpane >}}


## 2-2. 필드 n번째 요소 사이즈 변경

{{< tabpane >}}
{{< tab header="일반 표현식" lang="java" >}}

generateBuilder.size("values[n]", 5);

{{< /tab >}}
{{< tab header="Kotlin Exp" lang="kotlin" >}}

generateBuilder.sizeExp(Generate::values[n], 5);

{{< /tab >}}
{{< /tabpane >}}

## 2-3. 필드 모든 요소 사이즈 변경

{{< tabpane >}}
{{< tab header="일반 표현식" lang="java" >}}

generateBuilder.size("values[*]", 5);

{{< /tab >}}
{{< tab header="Kotlin Exp" lang="kotlin" >}}

generateBuilder.sizeExp(Generate::values["*"], 5);

{{< /tab >}}
{{< /tabpane >}}
