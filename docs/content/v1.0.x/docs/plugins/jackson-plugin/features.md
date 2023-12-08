---
title: "Features"
images: []
menu:
docs:
parent: "jackson-plugin"
identifier: "jackson-plugin-features"
weight: 71
---

Fixture monkey supports [Jackson](https://github.com/FasterXML/jackson) with the Fixture Monkey Jackson plugin.

- Supports the use of `JacksonObjectArbitraryIntrospector` as the default introspector to create objects using the Jackson object mapper.
- Supports Jackson Annotations such as, `@JsonIgnore`, `@JsonProperty`

## Dependencies
#### Gradle
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-jackson:{{< fixture-monkey-version >}}")
```

#### Maven
```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-jackson</artifactId>
  <version>{{< fixture-monkey-version >}}</version>
  <scope>test</scope>
</dependency>
```

## Plugin
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

Pass the object mapper to the JacksonPlugin constructor if you are using Jackson with a custom object mapper.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
ObjectMapper objectMapper = JsonMapper.builder()
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .build()

FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new JacksonPlugin(objectMapper))
    .build();

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
val objectMapper = JsonMapper.builder()
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .build()

val fixtureMonkey = FixtureMonkey.builder()
    .plugin(JacksonPlugin(objectMapper))
    .build()

{{< /tab >}}
{{< /tabpane>}}
