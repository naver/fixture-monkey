---
title: "중첩된 맵 변경"
weight: 7
---

{{< alert color="primary" title="Tip">}}
이 문서에서 설명하는 연산은 `setInner` 입니다.
{{< /alert >}}

## 0. 클래스
맵의 키가 맵인 경우나 맵의 값이 맵인 경우에도 값의 설정이 가능합니다.
```java
public class Generate {
	Map<Map<String, String>, String> mapByString;
	Map<String, Map<String, String>> stringByMap;
}
```

### map 타입 key 설정
key의 entry를 설정합니다.
{{< tabpane persistLang=false >}}
{{< tab header="일반 표현식" lang="java">}}

generateBuilder.setInner(
    InnerSpec().property("mapByString", m -> m.key(k -> k.entry("key", "value")))
);

{{< /tab >}}
{{< tab header="Kotlin Exp" lang="kotlin">}}

generateBuilder.setInner(
    InnerSpec().property("mapByString") { m -> m.key { k -> k.entry("key", "value") } }
);

{{< /tab >}}
{{< /tabpane >}}

만약, 해당 key에 대응하는 value값도 함께 설정해주고 싶다면 다음 연산을 사용할 수 있습니다.

{{< tabpane persistLang=false >}}
{{< tab header="일반 표현식" lang="java">}}

generateBuilder.setInner(
    InnerSpec().property("mapByString", m -> m.entry(k -> k.entry("innerKey", "innerValue")), "value")
);

{{< /tab >}}
{{< tab header="Kotlin Exp" lang="kotlin">}}

generateBuilder.setInner(
    InnerSpec().property("mapByString") { m -> m.entry({ k -> k.entry("innerKey", "innerValue") }, "value") }
);

{{< /tab >}}
{{< /tabpane >}}

### map 타입 value 설정
value의 entry를 설정합니다.
{{< tabpane persistLang=false >}}
{{< tab header="일반 표현식" lang="java" >}}

generateBuilder.setInner(
    InnerSpec().property("stringByMap", m -> m.value(v -> v.entry("key", "value")))
);

{{< /tab >}}
{{< tab header="Kotlin Exp" lang="kotlin">}}

generateBuilder.setInner(
    InnerSpec().property("stringByMap") { m -> m.value { v -> v.entry("key", "value") } }
);
{{< /tab >}}
{{< /tabpane >}}

만약, 해당 value에 대응하는 key값도 함께 설정해주고 싶다면 다음 연산을 사용할 수 있습니다.

{{< tabpane persistLang=false >}}
{{< tab header="일반 표현식" lang="java">}}

generateBuilder.setInner(
InnerSpec().property("stringByMap", m -> m.entry("key", v -> v.entry("innerKey", "innerValue")))
);

{{< /tab >}}
{{< tab header="Kotlin Exp" lang="kotlin">}}

generateBuilder.setInner(
InnerSpec().property("stringByMap") { m -> m.entry("key") {v -> v.entry("innerKey", "innerValue")} }
);

{{< /tab >}}
{{< /tabpane >}}
