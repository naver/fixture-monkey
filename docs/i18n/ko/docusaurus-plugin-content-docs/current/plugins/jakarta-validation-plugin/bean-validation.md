---
title: "Bean 유효성 검사"
sidebar_position: 82
---

### 유효한 데이터 생성하기

Jakarta Validation 플러그인을 사용하여 프로퍼티에 있는 Jakarta Bean Validation 어노테이션들을 기반으로 유효한 데이터를 생성할 수 있습니다.

예를 들어, 다음과 같이 어노테이션들이 지정된 Product 클래스가 있을 수 있습니다:
```
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
위 어노테이션들을 준수하는 Product 클래스의 인스턴스는 다음과 같은 방식으로 생성될 수 있습니다:
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
### 지원하는 어노테이션들
```jakarta.validation.constraints package``` 의 모든 어노테이션들을 지원합니다. 
각 타입별로 알맞은 어노테이션 제약들을 지원합니다.

#### Numeric 타입
지원되는 타입들: `BigDecimal`, `BigInteger`, `byte`, `double`, `float`, `int`, `long`, `short`

- @Digits (소수 자리수는 현재 지원되지 않음)
- @Max
- @Min
- @Negative
- @NegativeOrZero
- @DecimalMax
- @DecimalMin
- @Positive
- @PositiveOrZero

#### Boolean 타입
- @AssertFalse
- @AssertTrue

#### String 타입
- @Null
- @NotNull
- @NotBlank
- @NotEmpty
- @Size
- @Digits
- @Pattern
- @Email

#### Time 타입
Supported Types: `Calendar`, `Date`, `Instant`, `LocalDate`, `LocalDateTime`, `LocalTime`, `ZonedDateTime`, `Year`, `YearMonth`, `MonthDay`, `OffsetDateTime`, `OffsetTime`

- @Past
- @PastOrPresent
- @Future
- @FutureOrPresent

#### Container Type
- @Size
- @NotEmpty

