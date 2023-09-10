---
title: "Jackson Annotations"
images: []
menu:
docs:
parent: "jackson-plugin"
identifier: "jackson-annotations"
weight: 30
---

With the Jackson plugin, some Jackson annotations are also supported.

### @JsonProperty, @JsonIgnore

We can use the property name specified by @JsonProperty when using the String Expression to customize this property.

The property with @JsonIgnore will have a null value when Fixture Monkey generates the object.

The following example shows how @JsonProperty, @JsonIgnore works with Fixture Monkey.

**Example Java Class :**
```java
@Value // lombok getter, setter
public class Product {
    private long id;

    @JsonProperty("name")
    private String productName;

    private long price;

    @JsonIgnore
    private List<String> options;

    private Instant createdAt;
}
```

```java
@Test
void test() {
// given
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new JacksonPlugin())
    .build();

// when
Product actual = fixtureMonkey.giveMeBuilder(Product.class)
    .set("name", "book")
    .sample();

// then
then(actual.getProductName()).isEqualTo("book"); // @JsonProperty
then(actual.getOptions()).isNull();  // @JsonIgnore
}
```


### @JsonTypeInfo, @JsonSubTypes
Fixture Monkey also supports Jackson's polymorphic type handling annotations `@JsonTypeInfo` and `@JsonSubTypes`.

We can generate an inheritance-implementation relationship object with the help of FixtureMonkey.
