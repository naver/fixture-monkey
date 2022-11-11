---
title: "Kotlin"
weight: 3
---
## PrimaryConstructorArbitraryIntrospector

### 1. Altering `objectIntrospector` option

```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
    .plugin(KotlinPlugin())
    .build();
```

## JacksonArbitraryIntrospector
{{< alert color="primary" title="Tip">}}
This practice should add extra module because it depends on third-party library [Jackson](https://github.com/FasterXML/jackson)
{{< /alert >}}

### 1. Adding dependency

```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-jackson:0.4.3")
```

```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-jackson</artifactId>
  <version>0.4.3</version>
  <scope>test</scope>
</dependency>
```

### 2. Altering `objectIntrospector` option

#### If you have custom ObjectMapper
```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
	.plugin(KotlinPlugin())
    .plugin(JacksonPlugin(objectMapper))
    .build();
```

#### If you DON'T have a custom ObjectMapper
```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
    .plugin(KotlinPlugin())
    .plugin(JacksonPlugin())
    .build();
```
