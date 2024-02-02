---
title: "JacksonObjectArbitraryIntrospector"
images: []
menu:
docs:
parent: "jackson-plugin"
identifier: "jackson-object-arbitrary-introspector"
weight: 72
---

## JacksonObjectArbitraryIntrospector
The `JacksonObjectArbitraryIntrospector` becomes the default introspector when the Jackson plugin is added.
It puts the created properties of the given class into a map and deserializes them using Jackson's object mapper.

**Example Java Class :**
```java
@Value
public class Product {
    long id;

    String productName;

    long price;

    List<String> options;

    Instant createdAt;
}
```

**Using JacksonObjectArbitraryIntrospector :**
{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

@Test
void test() {
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .plugin(new JacksonPlugin())
        .build();

    Product product = fixtureMonkey.giveMeOne(Product.class);
}

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter-kotlin:{{< fixture-monkey-version >}}")
testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin")

@Test
fun test() {
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .plugin(JacksonPlugin())
        .build();

    val product: Product = fixtureMonkey.giveMeOne()
}

{{< /tab >}}
{{< /tabpane>}}

{{< alert icon="💡" text="To generate Kotlin classes with JacksonObjectArbitraryIntrospector, both Kotlin plugin and Jackson plugin need to be added. In addition, fasterxml jackson-module-kotlin should be added to the dependency for serialization/deserialization of Kotlin classes." />}}

It has the advantage of being a general purpose introspector because it relies on the widely used Jackson for object creation.
If your production code has both Kotlin and Java classes, it is recommended to use `JacksonObjectArbitraryIntrospector`.

However, it does have the disadvantage of potentially not performing as efficiently as other introspectors, as deserialization with Jackson can be more time-consuming.


