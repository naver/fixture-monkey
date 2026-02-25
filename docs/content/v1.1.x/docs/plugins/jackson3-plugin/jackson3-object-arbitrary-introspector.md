---
title: "Jackson3ObjectArbitraryIntrospector"
images: []
menu:
docs:
parent: "jackson3-plugin"
identifier: "jackson3-object-arbitrary-introspector"
weight: 77
---

## Jackson3ObjectArbitraryIntrospector
The `Jackson3ObjectArbitraryIntrospector` becomes the default introspector when the Jackson3 plugin is added.
It puts the created properties of the given class into a map and deserializes them using Jackson 3's object mapper.

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

**Using Jackson3ObjectArbitraryIntrospector :**
{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

@Test
void test() {
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .plugin(new Jackson3Plugin())
        .build();

    Product product = fixtureMonkey.giveMeOne(Product.class);
}

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter-kotlin:{{< fixture-monkey-version >}}")
testImplementation("tools.jackson.module:jackson-module-kotlin")

@Test
fun test() {
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .plugin(Jackson3Plugin())
        .build();

    val product: Product = fixtureMonkey.giveMeOne()
}

{{< /tab >}}
{{< /tabpane>}}

{{< alert icon="💡" text="To generate Kotlin classes with Jackson3ObjectArbitraryIntrospector, both Kotlin plugin and Jackson3 plugin need to be added. In addition, jackson-module-kotlin for Jackson 3 should be added to the dependency for serialization/deserialization of Kotlin classes." />}}

It has the advantage of being a general purpose introspector because it relies on the widely used Jackson for object creation.
If your production code has both Kotlin and Java classes, it is recommended to use `Jackson3ObjectArbitraryIntrospector`.

However, it does have the disadvantage of potentially not performing as efficiently as other introspectors, as deserialization with Jackson can be more time-consuming.
