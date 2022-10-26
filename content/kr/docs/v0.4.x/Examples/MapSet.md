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

{{< tabpane lang = "java" >}}
{{< tab header ="일반 표현식" >}}

generateBuilder.setInner("namesById", m -> m.size(5));


{{< /tab >}}
{{< tab header= "Exp" >}}

generateBuilder.setInnerExpGetter(Generate::getNamesById) { m -> m.size(5) }

{{< /tab >}}
{{< /tabpane>}}

## 3. 값 설정
### entry 설정
{{< tabpane lang = "java" >}}
{{< tab header ="일반 표현식" >}}

generateBuilder.setInner("namesById", m -> m.entry("key", "value"));

{{< /tab >}}
{{< tab header= "Exp" >}}

generateBuilder.setInnerExpGetter(Generate::getNamesById) { m -> m.entry("key", "value") }

{{< /tab >}}
{{< /tabpane>}}

### key 설정
entry의 key 값만 설정합니다.
{{< tabpane lang = "java" >}}
{{< tab header ="일반 표현식" >}}

generateBuilder.setInner("namesById", m -> m.key("key"));

{{< /tab >}}
{{< tab header= "Exp" >}}

generateBuilder.setInnerExpGetter(Generate::getNamesById) { m -> m.key("key") }

{{< /tab >}}
{{< /tabpane>}}

### value 설정
entry의 value 값만 설정합니다.
{{< tabpane lang = "java" >}}
{{< tab header ="일반 표현식" >}}

generateBuilder.setInner("namesById", m -> m.value("value"));

{{< /tab >}}
{{< tab header= "Exp" >}}

generateBuilder.setInnerExpGetter(Generate::getNamesById) { m -> m.value("value") }

{{< /tab >}}
{{< /tabpane>}}

## +) 중첩된 맵 설정
맵의 키가 맵인 경우나 맵의 값이 맵인 경우에도 값의 설정이 가능합니다.
```java
public class Generate {
	Map<Map<String, String>, String> mapByString;
	Map<String, Map<String, String>> stringByMap;
}
```

### map 타입 key 설정
key의 entry를 설정합니다.
{{< tabpane lang = "java" >}}
{{< tab header ="일반 표현식" >}}

generateBuilder.setInner("mapByString", m -> m.key(k -> k.entry("key", "value")));

{{< /tab >}}
{{< tab header= "Exp" >}}

generateBuilder.setInnerExpGetter(Generate::getMapByString) { m -> m.key { k -> k.entry("key", "value") } }

{{< /tab >}}
{{< /tabpane>}}

만약, 해당 key에 대응하는 value값도 함께 설정해주고 싶다면 다음 연산을 사용할 수 있습니다.

{{< tabpane lang = "java" >}}
{{< tab header ="일반 표현식" >}}

generateBuilder.setInner("mapByString", m -> m.entry(k -> k.entry("innerKey", "innerValue")), "value");

{{< /tab >}}
{{< tab header= "Exp" >}}

generateBuilder.setInnerExpGetter(Generate::getMapByString) { m -> m.entry ({ k -> k.entry("innerKey", "innerValue") }, "value")

{{< /tab >}}
{{< /tabpane>}}

### map 타입 value 설정
value의 entry를 설정합니다.
{{< tabpane lang = "java" >}}
{{< tab header ="일반 표현식" >}}

generateBuilder.setInner("stringByMap", m -> m.value(v -> v.entry("key", "value")));

{{< /tab >}}
{{< tab header= "Exp" >}}

generateBuilder.setInnerExpGetter(Generate::getStringByMap) { m -> m.value { v -> v.entry("key", "value") } }

{{< /tab >}}
{{< /tabpane>}}

만약, 해당 value에 대응하는 key값도 함께 설정해주고 싶다면 다음 연산을 사용할 수 있습니다.

{{< tabpane lang = "java" >}}
{{< tab header ="일반 표현식" >}}

generateBuilder.setInner("stringByMap", m -> m.entry("key", v -> v.entry("innerKey", "innerValue")));

{{< /tab >}}
{{< tab header= "Exp" >}}

generateBuilder.setInnerExpGetter(Generate::getStringByMap) { m -> m.entry ("key") { v -> v.entry("innerKey", "innerValue") } }

{{< /tab >}}
{{< /tabpane>}}