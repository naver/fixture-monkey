---
title: "Overview"
weight: 11
menu:
docs:
  parent: "introduction"
  identifier: "overview"
---

## Fixture Monkey

Fixture Monkey is a Java & Kotlin library designed to generate controllable arbitrary test objects.
Its most distinctive feature is the ability to freely access and configure any nested fields through path-based expressions.

It focuses on simplifying test writing, by facilitating the generation of necessary test fixtures.
Whether you're dealing with basic or complex test fixtures, Fixture Monkey helps you to effortlessly create the test objects you need and easily customize them to match your desired configurations.

Make your JVM tests more concise and safe with Fixture Monkey.

---------

## Quick Start

Add Fixture Monkey to your project:

```gradle
dependencies {
    testImplementation 'com.navercorp.fixturemonkey:fixture-monkey-starter:{{< param "version" >}}'
}
```

Create your first test object:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.create();

// Generate a simple String
String randomString = fixtureMonkey.giveMeOne(String.class);

// Generate a simple Integer
Integer randomNumber = fixtureMonkey.giveMeOne(Integer.class);

// Generate a list of Strings
List<String> randomStrings = fixtureMonkey.giveMe(String.class, 3);
```

## Why use Fixture Monkey?
### 1. One-Line Test Object Generation
```java
// Before: Manual object creation
Product product = new Product();
product.setId(1L);
product.setName("Test Product");
product.setPrice(1000);
product.setCreatedAt(LocalDateTime.now());

// After: With Fixture Monkey
Product product = fixtureMonkey.giveMeOne(Product.class);
```
Stop writing boilerplate code for test object creation. Fixture Monkey generates any test object with a single line of code.
Transform your test preparation from a tedious chore into a simple, elegant solution. No changes to production code or test environment required.

### 2. Intuitive Path-Based Configuration
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

// Set all product names to "Special Product"
ArbitraryBuilder<Order> orderBuilder = fixtureMonkey.giveMeBuilder(Order.class)
    .set("items[*].product.name", "Special Product");

// Set all review ratings to 5 stars
ArbitraryBuilder<Order> orderWithGoodReviews = fixtureMonkey.giveMeBuilder(Order.class)
    .set("items[*].product.reviews[*].rating", 5);

// Set all quantities to 2
ArbitraryBuilder<Order> orderWithFixedQuantity = fixtureMonkey.giveMeBuilder(Order.class)
    .set("items[*].quantity", 2);
```
Bid farewell to endless getter/setter chains. Fixture Monkey's path expressions let you configure any nested field with a single line.
The `[*]` wildcard operator empowers you to manipulate entire collections effortlessly, dramatically reducing boilerplate code and enhancing test maintainability.

### 3. Reusable Test Specifications
```java
// Define a reusable builder
ArbitraryBuilder<Product> productBuilder = fixtureMonkey.giveMeBuilder(Product.class)
    .set("category", "Book")
    .set("price", 1000);

// Reuse in different tests
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
Eliminate test code duplication. Define complex object specifications once and reuse them across your test suite.
ArbitraryBuilder's lazy evaluation ensures objects are only created when needed, optimizing your test performance.

### 4. Universal Object Generation
```java
// inheritance
class Foo {
  String foo;
}

class Bar extends Foo {
    String bar;
}

Foo foo = FixtureMonkey.create().giveMeOne(Foo.class);
Bar bar = FixtureMonkey.create().giveMeOne(Bar.class);

// circular-reference
class Foo {
    String value;
    Foo foo;
}

Foo foo = FixtureMonkey.create().giveMeOne(Foo.class);

// anonymous objects
interface Foo {
    Bar getBar();
}

class Bar {
    String value;
}

Foo foo = FixtureMonkey.create().giveMeOne(Foo.class);
```
From simple POJOs to complex object graphs, Fixture Monkey handles it all. Generate lists, nested collections, enums, generic types, and even objects with inheritance relationships or circular references.
No object structure is too complex for Fixture Monkey.

### 5. Dynamic Test Data
```java
ArbitraryBuilder<Product> actual = fixtureMonkey.giveMeBuilder(Product.class);

then(actual.sample()).isNotEqualTo(actual.sample());
```
Move beyond static test data. Fixture Monkey's random value generation helps you discover edge cases that static data might miss.
Make your tests more robust by testing with varied data in every run.

## Real Test Example
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

--------

## Battle-Tested in Production
Originally developed at [Naver](https://www.navercorp.com/en), Fixture Monkey played a pivotal role in the Plasma project, revolutionizing Naver Pay's architecture.
Supporting over 10,000 tests for South Korea's leading mobile payment service, Fixture Monkey has proven its reliability in handling complex business requirements at scale.
Now available as open-source, bring this battle-tested solution to your projects and write more reliable tests with confidence.
