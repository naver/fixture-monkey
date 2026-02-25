---
title: "Jackson3 Annotations"
images: []
menu:
  docs:
    parent: "jackson3-plugin"
    identifier: "jackson3-annotations"
    weight: 78
---

With the Jackson3 plugin, some Jackson annotations are also supported.

### @JsonProperty, @JsonIgnore

We can use the property name specified by @JsonProperty when using the String Expression to customize this property.

The property with @JsonIgnore will have a null value when Fixture Monkey generates the object.

The following example shows how @JsonProperty, @JsonIgnore works with Fixture Monkey.

**Example Java Class :**
```java
@Value // lombok getter, setter
public class Product {
    long id;

    @JsonProperty("name")
    String productName;

    long price;

    @JsonIgnore
    List<String> options;

    Instant createdAt;
}
```

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

@Test
void test() {
    // given
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .plugin(new Jackson3Plugin())
        .build();

    // when
    Product actual = fixtureMonkey.giveMeBuilder(Product.class)
        .set("name", "book")
        .sample();

    // then
    then(actual.getProductName()).isEqualTo("book"); // @JsonProperty
    then(actual.getOptions()).isNull(); // @JsonIgnore
}

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

@Test
fun test() {
    // given
    val fixtureMonkey = FixtureMonkey.builder()
        .plugin(Jackson3Plugin())
        .build()

    // when
    val actual = fixtureMonkey.giveMeBuilder<Product>()
        .set("name", "book")
        .sample()

    // then
    then(actual.productName).isEqualTo("book") // @JsonProperty
    then(actual.options).isNull() // @JsonIgnore
}

{{< /tab >}}
{{< /tabpane>}}


### @JsonTypeInfo, @JsonSubTypes
Fixture Monkey also supports Jackson's polymorphic type handling annotations `@JsonTypeInfo` and `@JsonSubTypes`.

We can generate an inheritance-implementation relationship object with the help of FixtureMonkey.
