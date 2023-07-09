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

## 2. 사이즈 설정
{{< alert color="primary" title="Tip">}}
맵의 사이즈보다 설정하려는 값의 개수가 많은 경우에는 값 설정이 이루어지지 않을 수 있습니다.  
값 설정 전에 맵의 사이즈를 먼저 설정하는 것을 권장합니다.
{{< /alert >}}

{{< tabpane persistLang=false >}}
{{< tab header="일반 표현식" lang="java">}}

generateBuilder.setInner(
    InnerSpec().property("namesById", m -> m.size(5))
);

{{< /tab >}}
{{< tab header="Kotlin Exp" lang="kotlin">}}

generateBuilder.setInner(
    InnerSpec().property("namesById") { m -> m.size(5) }
)

{{< /tab >}}
{{< /tabpane >}}

## 3. 값 설정
### entry 설정
{{< tabpane persistLang=false >}}
{{< tab header="일반 표현식" lang="java">}}

generateBuilder.setInner(
    InnerSpec().property("namesById", m -> m.entry("key", "value"))
);

{{< /tab >}}
{{< tab header="Kotlin Exp" lang="kotlin">}}

generateBuilder.setInner(
    InnerSpec().property("namesById") { m -> m.entry("key", "value") }
)
{{< /tab >}}
{{< /tabpane >}}

### key 설정
entry의 key 값만 설정합니다.
{{< tabpane persistLang=false >}}
{{< tab header="일반 표현식" lang="java">}}

generateBuilder.setInner(
    InnerSpec().property("namesById", m -> m.key("key"))
);

{{< /tab >}}
{{< tab header="Kotlin Exp" lang="kotlin">}}

generateBuilder.setInner(
    InnerSpec().property("namesById") { m -> m.key("key") }
)

{{< /tab >}}
{{< /tabpane >}}

### value 설정
entry의 value 값만 설정합니다.
{{< tabpane persistLang=false >}}
{{< tab header="일반 표현식" lang="java">}}

generateBuilder.setInner(
    InnerSpec().property("namesById", m -> m.value("value"))
);

{{< /tab >}}
{{< tab header="Kotlin Exp" lang="kotlin">}}

generateBuilder.setInner(
    InnerSpec().property("namesById") { m -> m.value("value") }
)

{{< /tab >}}
{{< /tabpane >}}
