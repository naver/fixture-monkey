---
title: "기능"
images: []
menu:
docs:
parent: "jackson-plugin"
identifier: "jackson-plugin-features"
weight: 71
---

Fixture monkey는 Fixture Monkey Jackson 플러그인을 사용하여 [Jackson](https://github.com/FasterXML/jackson)를 지원합니다.

- `JacksonObjectArbitraryIntrospector`를 기본 introspector로 사용하여 Jackson 객체 매퍼를 통해 객체를 생성하는 기능을 지원합니다.
- `@JsonIgnore`, `@JsonProperty`와 같은 Jackson 애노테이션을 지원합니다.

## 의존성
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

## 플러그인
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

JacksonPlugin을 사용할 때 사용자 정의 객체 매퍼와 함께 Jackson을 사용하려면 JacksonPlugin 생성자에 objectMapper를 사용해야 합니다.

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
