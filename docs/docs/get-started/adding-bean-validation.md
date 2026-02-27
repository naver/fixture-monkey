---
title: "Adding Bean Validation"
sidebar_position: 25
---


Sometimes, you might want to create a valid test object that adheres to the constraints specified by the Bean Validation annotations on your class.
Fixture Monkey makes this easy with support for `jakarta.validation.constraints` and `javax.validation.constraints` packages.

For example, consider a Product class with validation constraints:

```java
@Value
public class Product {
    @Min(1)
    long id;

    @NotBlank
    String productName;

    @Max(100000)
    long price;

    @Size(min = 3)
    List<@NotBlank String> options;

    @Past
    Instant createdAt;
}
```

To generate objects that satisfy these constraints, first add the appropriate dependency:

##### Gradle
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-jakarta-validation:1.1.15")
```

##### Maven
```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-jakarta-validation</artifactId>
  <version>1.1.15</version>
  <scope>test</scope>
</dependency>
```

Then, add the validation plugin to your FixtureMonkey configuration:
```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new JakartaValidationPlugin()) // or new JavaxValidationPlugin()
    .build();
```
Note: If you're using `fixture-monkey-starter`, the validation plugin is already included.

Now you can generate valid objects that satisfy all constraints:

```java
@Test
void test() {
    // given
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
        .plugin(new JakartaValidationPlugin())
        .build();

    // when
    Product actual = fixtureMonkey.giveMeOne(Product.class);

    // then
    then(actual).isNotNull();
    then(actual.getId()).isGreaterThan(0);
    then(actual.getProductName()).isNotBlank();
    then(actual.getPrice()).isLessThanOrEqualTo(100000);
    then(actual.getOptions().size()).isGreaterThanOrEqualTo(3);
    then(actual.getOptions()).allSatisfy(it -> then(it).isNotEmpty());
    then(actual.getCreatedAt()).isNotNull().isLessThanOrEqualTo(Instant.now());
}
```

When you run this code, Fixture Monkey will generate a Product instance that satisfies all validation constraints.
Below is just an example, and the actual values will be different each time:

```java
Product(
    id=42,                   // Satisfies @Min(1)
    productName="product-1", // Satisfies @NotBlank
    price=75000,            // Satisfies @Max(100000)
    options=[               // Satisfies @Size(min = 3)
        "option1",          // Each string satisfies @NotBlank
        "option2",
        "option3"
    ],
    createdAt=2024-03-20T10:15:30Z  // Satisfies @Past
)
```

