---
title: "Bean 유효성 검사 추가하기"
sidebar_position: 24
---


때로는 클래스의 Bean 유효성 검사 어노테이션에 지정된 제약조건을 준수하는 유효한 테스트 객체를 생성하고 싶을 수 있습니다.

Fixture Monkey는 `jakarta.validation.constraints` 및 `javax.validation.constraints` 패키지의 제약 어노테이션을 지원합니다.

이 기능을 사용하려면 다음과 같이 프로젝트에 `fixture-monkey-jakarta-validation` 종속성을 추가해야 합니다.
<br/>

javax.validation.constraints를 사용하는 경우 `fixture-monkey-javax-validation`을 추가해야 합니다.

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
Bean 유효성 검사 어노테이션을 기반으로 객체를 생성하려면 아래 그림과 같이 FixtureMonkey에 `JakartaValidationPlugin` (`javax.validation.constraints`를 사용하는 경우 `JavaxValidationPlugin`) 옵션을 추가해야 합니다.
<br />

`fixture-monkey-starter` 종속성을 추가했다면 이미 포함되어 있을 것입니다.

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new JakartaValidationPlugin()) // or new JavaxValidationPlugin()
    .build();
```

이전 `Product`클래스에 몇 가지 유효성 검사 어노테이션을 추가했다고 가정해보겠습니다.

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

앞서 생성한 FixtureMonkey 인스턴스로 이제 유효한 객체를 생성할 수 있습니다.

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

검증문을 통해 `FixtureMonkey`로 생성된 객체가 모든 유효성 검사 어노테이션 요구 사항을 충족한다는 것을 알 수 있습니다.


