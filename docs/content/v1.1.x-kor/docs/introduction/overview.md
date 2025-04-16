---
title: "개요"
weight: 11
menu:
docs:
  parent: "introduction"
  identifier: "overview"
---

## Fixture Monkey

Fixture Monkey는 테스트 객체를 쉽게 생성하고 조작할 수 있도록 고안된 Java 및 Kotlin 라이브러리입니다.
객체의 경로를 기반으로 중첩된 모든 필드에 자유롭게 접근하고 설정할 수 있는 것이 가장 큰 특징입니다.

이 라이브러리는 테스트 작성을 간편하게 하기 위해 필요한 테스트 픽스처를 손쉽게 생성하는 데 중점을 두고 있습니다.
기본적이거나 복잡한 테스트 픽스처를 다루고 있더라도, Fixture Monkey는 필요한 테스트 객체를 쉽게 생성하고 원하는 구성에 맞게 손쉽게 수정할 수 있도록 도와줍니다.

Fixture Monkey를 활용하여 JVM 테스트를 간결하고 안전하게 수행하세요.

---------

## 빠른 시작

프로젝트에 Fixture Monkey를 추가하세요:

```gradle
dependencies {
    testImplementation 'com.navercorp.fixturemonkey:fixture-monkey-starter:{{< param "version" >}}'
}
```

첫 번째 테스트 객체를 생성해보세요:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.create();

// 간단한 String 생성
String randomString = fixtureMonkey.giveMeOne(String.class);

// 간단한 Integer 생성
Integer randomNumber = fixtureMonkey.giveMeOne(Integer.class);

// String 리스트 생성
List<String> randomStrings = fixtureMonkey.giveMe(String.class, 3);
```

## Fixture Monkey를 왜 사용해야 하나요?
### 1. 한 줄로 끝나는 테스트 객체 생성
```java
// 이전: 수동으로 객체 생성
Product product = new Product();
product.setId(1L);
product.setName("Test Product");
product.setPrice(1000);
product.setCreatedAt(LocalDateTime.now());

// 이후: Fixture Monkey 사용
Product product = fixtureMonkey.giveMeOne(Product.class);
```
테스트 객체 생성을 위한 보일러플레이트 코드 작성을 그만두세요. Fixture Monkey는 한 줄의 코드로 어떤 테스트 객체든 생성합니다.
지루한 테스트 준비 작업을 간결하고 우아한 솔루션으로 바꿔보세요. 프로덕션 코드나 테스트 환경을 변경할 필요도 없습니다.

### 2. 직관적인 경로 기반 설정
```java
class Order {
    List<OrderItem> items;
    Customer customer;
    Address shippingAddress;
}

class OrderItem {
    Product product;
    int quantity;
}

class Product {
    String name;
    List<Review> reviews;
}

// 모든 상품 이름을 "Special Product"로 설정
ArbitraryBuilder<Order> orderBuilder = fixtureMonkey.giveMeBuilder(Order.class)
    .set("items[*].product.name", "Special Product");

// 모든 리뷰 평점을 5점으로 설정
ArbitraryBuilder<Order> orderWithGoodReviews = fixtureMonkey.giveMeBuilder(Order.class)
    .set("items[*].product.reviews[*].rating", 5);

// 모든 수량을 2로 설정
ArbitraryBuilder<Order> orderWithFixedQuantity = fixtureMonkey.giveMeBuilder(Order.class)
    .set("items[*].quantity", 2);
```
끝없는 getter/setter 체인과 작별하세요. Fixture Monkey의 경로 표현식으로 중첩된 필드를 한 줄로 설정할 수 있습니다.
`[*]` 와일드카드 연산자로 컬렉션 전체를 손쉽게 조작할 수 있어, 보일러플레이트 코드를 크게 줄이고 테스트의 유지보수성을 높입니다.

### 3. 재사용 가능한 테스트 스펙
```java
// 재사용 가능한 빌더 정의
ArbitraryBuilder<Product> productBuilder = fixtureMonkey.giveMeBuilder(Product.class)
    .set("category", "Book")
    .set("price", 1000);

// 다양한 테스트에서 재사용
@Test
void testProductCreation() {
    Product product = productBuilder.sample();
    assertThat(product.getCategory()).isEqualTo("Book");
    assertThat(product.getPrice()).isEqualTo(1000);
}

@Test
void testProductWithReviews() {
    Product product = productBuilder
        .size("reviews", 3)
        .sample();
    assertThat(product.getReviews()).hasSize(3);
}

@Test
void testProductWithSpecificReview() {
    Product product = productBuilder
        .set("reviews[0].rating", 5)
        .set("reviews[0].comment", "Excellent!")
        .sample();
    assertThat(product.getReviews().get(0).getRating()).isEqualTo(5);
    assertThat(product.getReviews().get(0).getComment()).isEqualTo("Excellent!");
}
```
테스트 코드 중복을 제거하세요. 복잡한 객체 스펙을 한 번 정의하고 테스트 스위트 전체에서 재사용할 수 있습니다.
ArbitraryBuilder의 지연 평가(lazy evaluation)는 객체가 필요할 때만 생성되도록 보장하여 테스트 성능을 최적화합니다.

### 4. 모든 객체 생성 가능
```java
// 상속
class Foo {
  String foo;
}

class Bar extends Foo {
    String bar;
}

Foo foo = FixtureMonkey.create().giveMeOne(Foo.class);
Bar bar = FixtureMonkey.create().giveMeOne(Bar.class);

// 순환 참조
class Foo {
    String value;
    Foo foo;
}

Foo foo = FixtureMonkey.create().giveMeOne(Foo.class);

// 익명 객체
interface Foo {
    Bar getBar();
}

class Bar {
    String value;
}

Foo foo = FixtureMonkey.create().giveMeOne(Foo.class);
```
단순한 POJO부터 복잡한 객체 그래프까지, Fixture Monkey가 모두 처리합니다. 리스트, 중첩된 컬렉션, 열거형, 제네릭 타입은 물론 상속 관계나 순환 참조를 가진 객체도 생성할 수 있습니다.
Fixture Monkey에게 너무 복잡한 객체 구조란 없습니다.

### 5. 동적인 테스트 데이터
```java
ArbitraryBuilder<Product> actual = fixtureMonkey.giveMeBuilder(Product.class);

then(actual.sample()).isNotEqualTo(actual.sample());
```
정적 테스트 데이터의 한계를 넘어서세요. Fixture Monkey의 랜덤 값 생성으로 정적 데이터에서는 발견하기 어려운 엣지 케이스를 찾을 수 있습니다.
매 실행마다 다양한 데이터로 테스트하여 더 견고한 테스트를 만들어보세요.

## 실제 테스트 예제
```java
@Test
void testOrderProcessing() {
    // Given
    Order order = fixtureMonkey.giveMeBuilder(Order.class)
        .set("items[*].quantity", 2)
        .set("items[*].product.price", 1000)
        .sample();
    
    OrderProcessor processor = new OrderProcessor();
    
    // When
    OrderResult result = processor.process(order);
    
    // Then
    assertThat(result.getTotalAmount()).isEqualTo(4000); // 2 items * 2 quantity * 1000 price
    assertThat(result.getStatus()).isEqualTo(OrderStatus.COMPLETED);
}
```

---------

## 실제 프로덕션에서 검증된 효과
[Naver](https://www.navercorp.com/)에서 개발된 Fixture Monkey는 Plasma 프로젝트에서 핵심적인 역할을 하며 Naver Pay의 아키텍처를 혁신했습니다.
대한민국 최고의 모바일 결제 서비스에서 10,000개가 넘는 테스트를 지원하며, 복잡한 비즈니스 요구사항을 대규모로 처리하는 신뢰성을 입증했습니다.
이제 오픈 소스로 제공되는 이 검증된 솔루션을 당신의 프로젝트에 도입하고, 더 신뢰성 있는 테스트를 작성해보세요.
