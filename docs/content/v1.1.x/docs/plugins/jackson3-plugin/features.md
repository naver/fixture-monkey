---
title: "Features"
images: []
menu:
  docs:
    parent: "jackson3-plugin"
    identifier: "jackson3-plugin-features"
    weight: 76
---

Fixture monkey supports [Jackson 3](https://github.com/FasterXML/jackson) with the Fixture Monkey Jackson3 plugin.

Jackson 3 requires JDK 17 or higher. If you are using JDK 8, please use the [Jackson Plugin]({{< relref "../jackson-plugin" >}}) instead.

- Supports the use of `Jackson3ObjectArbitraryIntrospector` as the default introspector to create objects using the Jackson 3 object mapper.
- Supports Jackson Annotations such as, `@JsonIgnore`, `@JsonProperty`

## Dependencies
#### Gradle
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-jackson3:{{< fixture-monkey-version >}}")
```

#### Maven
```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-jackson3</artifactId>
  <version>{{< fixture-monkey-version >}}</version>
  <scope>test</scope>
</dependency>
```

## Plugin
{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new Jackson3Plugin())
    .build();

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val fixtureMonkey = FixtureMonkey.builder()
    .plugin(Jackson3Plugin())
    .build()

{{< /tab >}}
{{< /tabpane>}}

Pass the object mapper to the Jackson3Plugin constructor if you are using Jackson 3 with a custom object mapper.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
ObjectMapper objectMapper = JsonMapper.builder()
    .findAndAddModules()
    .build();

FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new Jackson3Plugin(objectMapper))
    .build();

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
val objectMapper = JsonMapper.builder()
    .findAndAddModules()
    .build()

val fixtureMonkey = FixtureMonkey.builder()
    .plugin(Jackson3Plugin(objectMapper))
    .build()

{{< /tab >}}
{{< /tabpane>}}
