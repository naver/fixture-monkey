---
title: "Adding Bean Validation"
sidebar_position: 24
---


Sometimes, you might want to create a valid test object that adheres to the constraints specified by the Bean Validation annotations on your class.

Fixture Monkey supports constraint annotations from the `jakarta.validation.constraints` and `javax.validation.constraints` packages.

To enable this feature, you need to add the `fixture-monkey-jakarta-validation` dependency (or `fixture-monkey-javax-validation` if you are using javax.validation.constraints) to your project as follows:

##### Gradle
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-jakarta-validation:1.0.20")
```

##### Maven
```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-jakarta-validation</artifactId>
  <version>1.0.20</version>
  <scope>test</scope>
</dependency>
```

Fixture Monkey provides additional features as plugins.

To generate objects based on Bean Validation annotations, you need to add the `JakartaValidationPlugin` (or `JavaxValidationPlugin` if you are using `javax.validation.constraints`) option to FixtureMonkey as shown below.
If you've added the fixture-monkey-starter dependency, it's already in place.

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new JakartaValidationPlugin()) // or new JavaxValidationPlugin()
    .build();
```

Let's assume that we have added several validation annotations to the previous `Product` class.

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

With the FixtureMonkey instance we created earlier, we can now generate valid objects:

```
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

From the assertions, it's clear that the object created with FixtureMonkey meets all the validation annotation requirements.

