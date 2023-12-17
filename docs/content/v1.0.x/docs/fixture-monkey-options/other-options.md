---
title: "Other Options"
images: []
menu:
docs:
parent: "fixture-monkey-options"
identifier: "options"
weight: 53
---

This section explains some additional options that the `FixtureMonkeyBuilder` provide.

### plugin

Fixture Monkey offers several additional features, including support for third-party libraries through plugins.
You can use the plugin option to use these additional features.

For example, you can add the Jackson plugin as shown below.
This will allow you to use Jackson features such as `JacksonObjectArbitraryIntrospector` and Jackson annotation support.

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
