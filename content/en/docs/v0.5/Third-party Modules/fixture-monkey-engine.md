---
title: "fixture-monkey-engine"
weight: 7
---

## Features
`FixtureMonkeySessionExtension` for JUnit Jupiter
- Adding JqwikSession life cycle  
- Clear Jqwik inner static cache, better performance

## How-to
### 1. Adding dependency
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-engine:{{< param version >}}")
```

```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-engine</artifactId>
  <version>{{< param version >}}</version>
  <scope>test</scope>
</dependency>
```
