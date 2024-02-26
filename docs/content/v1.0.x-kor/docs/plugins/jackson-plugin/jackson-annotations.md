---
title: "Jackson 애노테이션"
images: []
menu:
docs:
parent: "jackson-plugin"
identifier: "jackson-annotations"
weight: 73
---

Jackson 플러그인을 사용하면 일부 Jackson 애노테이션도 지원됩니다.

### @JsonProperty, @JsonIgnore

String 표현식을 사용하여 속성을 사용자 정의할 때는 @JsonProperty에서 지정한 속성 이름을 활용할 수 있습니다.

Fixture Monkey는 객체를 생성할 때 @JsonIgnore가 지정된 속성을 null 값으로 처리합니다.

다음 예제는 @JsonProperty와 @JsonIgnore가 Fixture Monkey와 함께 작동하는 방법을 보여줍니다.

**예제 자바 클래스 :**
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
    .plugin(new JacksonPlugin())
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
        .plugin(JacksonPlugin())
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
Fixture Monkey는 @JsonTypeInfo 및 @JsonSubTypes와 같은 Jackson의 다형성 타입을 처리할 수 있는 애노테이션도 지원합니다.

FixtureMonkey를 활용하여 상속-구현 관계의 객체를 생성할 수 있습니다.
