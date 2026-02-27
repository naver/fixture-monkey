---
title: "Bean Validation"
sidebar_position: 82
---

## Generating valid data
Using the Jakarta Validation plugin, we can generate valid data based on Jakarta Bean validation annotations on properties.

For example, there can be a `Product` class annotated as follows:

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

An instance of the Product class that is compliant with the annotations can be created in the following manner:

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

## Supported Annotations

Every annotation from the `jakarta.validation.constraints` package is supported.
Different types support different annotation constraints.

### Numeric Type
Supported Types: `BigDecimal`, `BigInteger`, `byte`, `double`, `float`, `int`, `long`, `short`

- @Digits (fraction is currently not supported)
- @Max
- @Min
- @Negative
- @NegativeOrZero
- @DecimalMax
- @DecimalMin
- @Positive
- @PositiveOrZero

### Boolean Type
- @AssertFalse
- @AssertTrue

### String Type
- @Null
- @NotNull
- @NotBlank
- @NotEmpty
- @Size
- @Digits
- @Pattern
- @Email

### Time Type
Supported Types: `Calendar`, `Date`, `Instant`, `LocalDate`, `LocalDateTime`, `LocalTime`, `ZonedDateTime`, `Year`, `YearMonth`, `MonthDay`, `OffsetDateTime`, `OffsetTime`
- @Past
- @PastOrPresent
- @Future
- @FutureOrPresent

### Container Type
- @Size
- @NotEmpty

