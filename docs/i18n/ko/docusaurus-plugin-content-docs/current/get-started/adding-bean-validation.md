---
title: "Bean 유효성 검사 추가하기"
sidebar_position: 25
---


때로는 클래스의 Bean 유효성 검사 어노테이션에 지정된 제약조건을 준수하는 유효한 테스트 객체를 생성하고 싶을 수 있습니다.
Fixture Monkey는 `jakarta.validation.constraints` 및 `javax.validation.constraints` 패키지의 제약 어노테이션을 지원하여 이를 쉽게 만들어줍니다.

예를 들어, 다음과 같이 유효성 검사 제약조건이 있는 Product 클래스를 보겠습니다:

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

이러한 제약조건을 만족하는 객체를 생성하려면, 먼저 적절한 의존성을 추가해야 합니다:

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

그리고 FixtureMonkey 설정에 유효성 검사 플러그인을 추가합니다:
```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new JakartaValidationPlugin()) // 또는 new JavaxValidationPlugin()
    .build();
```
참고: `fixture-monkey-starter`를 사용하는 경우 유효성 검사 플러그인이 이미 포함되어 있습니다.

이제 모든 제약조건을 만족하는 유효한 객체를 생성할 수 있습니다:

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

이 코드를 실행하면, Fixture Monkey는 모든 유효성 검사 제약조건을 만족하는 Product 인스턴스를 생성합니다.
아래는 예시일 뿐이며, 실제로는 매번 다른 임의의 값들이 생성됩니다:

```java
Product(
    id=42,                   // @Min(1) 제약조건 만족
    productName="product-1", // @NotBlank 제약조건 만족
    price=75000,            // @Max(100000) 제약조건 만족
    options=[               // @Size(min = 3) 제약조건 만족
        "option1",          // 각 문자열이 @NotBlank 제약조건 만족
        "option2",
        "option3"
    ],
    createdAt=2024-03-20T10:15:30Z  // @Past 제약조건 만족
)
```


