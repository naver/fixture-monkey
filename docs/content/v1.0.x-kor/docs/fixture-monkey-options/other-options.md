---
title: "기타 옵션"
images: []
menu:
docs:
parent: "fixture-monkey-options"
identifier: "options"
weight: 53
---

이 섹션에서는 `FixtureMonkeyBuilder` 가 제공하는 몇 가지 추가 옵션을 설명합니다.

### plugin

Fixture Monkey는 플러그인을 통한 서드파티 라이브러리 지원 등 몇 가지 추가 기능을 제공합니다.
플러그인 옵션을 사용하여 이 추가 기능을 사용할 수 있습니다.

예시로 아래와 같이 Jackson 플러그인을 추가할 수 있습니다.
이렇게 하면 `JacksonObjectArbitraryIntrospector` 그리고 Jackson 어노테이션 지원과 같은 Jackson의 기능을 사용할 수 있습니다.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new JacksonPlugin())
    .build();

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val fixtureMonkey = FixtureMonkey.builder()
    .plugin(JacksonPlugin())
    .build()

{{< /tab >}}
{{< /tabpane>}}
