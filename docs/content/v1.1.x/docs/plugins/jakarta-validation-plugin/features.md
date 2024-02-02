---
title: "Features"
images: []
menu:
docs:
parent: "jakarta-validation-plugin"
identifier: "jakarta-validation-plugin-features"
weight: 81
---

Fixture monkey supports generating valid data based on [Jakarta Bean Validation 3.0 annotations](https://beanvalidation.org/) with the Fixture Monkey Jakarta Validation Plugin.

{{< alert icon="💡" text="Javax Bean Validation is also supported with the Fixture Monkey Javax Validation Plugin" />}}

## Dependencies
#### Gradle
```groovy
testImplementation("com.navercorp.fixturemonkey:jakarta-validation:{{< fixture-monkey-version >}}")
```

#### Maven
```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-jakarta-validation</artifactId>
  <version>{{< fixture-monkey-version >}}</version>
  <scope>test</scope>
</dependency>
```

The jakarta validation API and the Hibernate validator are already provided as part of the dependency.

## Plugin
{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new JakartaValidationPlugin())
    .build();

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val fixtureMonkey = FixtureMonkey.builder()
    .plugin(JakartaValidationPlugin())
    .build()

{{< /tab >}}
{{< /tabpane>}}
