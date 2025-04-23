---
title: "Customization APIs"
weight: 42
menu:
docs:
  parent: "customizing-objects"
  identifier: "customization-apis"
---

## What you will learn in this document
- How to easily create test data
- How to generate objects with desired values
- How to apply these customizations in real testing scenarios

## Before you start
This document introduces various ways to create test data easily.
Here are some common scenarios where you can use Fixture Monkey APIs:

- When you need member data of a specific age range for registration tests
- When you need a shopping cart with multiple products for order tests
- When you need orders above a certain amount for payment tests

### Useful Terms to Know
- **Sampling**: The process of actually creating test data. Each time you call the `sample()` method, new test data is generated.
- **Builder**: A tool that helps you create objects step by step. In Fixture Monkey, you create a builder using `giveMeBuilder()`.
- **Path Expression**: A way to specify which property of an object to modify. For example, "age" refers to the age property, "items[0]" refers to the first item in a list, and "address.city" refers to the city property within an address object.

## Table of Contents
- [API Summary](#api-summary)
- [Using Basic APIs](#using-basic-apis)
  - [set() - Setting Specific Values](#set)
  - [size() - Controlling List Sizes](#size-minsize-maxsize)
  - [setNull() - Handling Null Values](#setnull-setnotnull)
- [Learning Intermediate APIs](#learning-intermediate-apis)
  - [setInner() - Creating Reusable Settings](#setinner)
  - [setLazy() - Generating Dynamic Values](#setlazy)
  - [setPostCondition() - Creating Values with Conditions](#setpostcondition)
  - [fixed() - Generating Same Values](#fixed)
  - [limit - Setting Values Partially](#limit)
- [Using Advanced APIs](#using-advanced-apis)
  - [thenApply() - Setting Related Values](#thenapply)
- [Frequently Asked Questions (FAQ)](#frequently-asked-questions-faq)

## API Summary

### Basic APIs (Essential APIs for Beginners)
| API | Description | Example Scenario |
|-----|-------------|-----------------|
| set() | Set specific values directly | Set member's age to 20 |
| size() | Set collection sizes | Add 3 items to cart |
| setNull() | Set null values | Set email to null for withdrawn members |

### Intermediate APIs (Use after getting familiar with basics)
| API | Description | Example Scenario |
|-----|-------------|-----------------|
| setInner() | Create reusable settings | Use same member info across multiple tests |
| setLazy() | Generate dynamic values | Create sequential order numbers |
| setPostCondition() | Create values meeting conditions | Test adults-only service |
| fixed() | Generate same values consistently | Use same test data across tests |
| limit | Set values for some elements only | Apply discount to some cart items |

### Advanced APIs (For complex test scenarios)
| API | Description | Example Scenario |
|-----|-------------|-----------------|
| thenApply() | Set related values | Set order total as sum of item prices |

## Using Basic APIs

### set()
The `set()` method is used to set specific values for object properties.
This is the most basic and commonly used API.

#### Basic Usage

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// Creating member data
Member member = fixtureMonkey.giveMeBuilder(Member.class)
    .set("name", "John Doe")        // Set name
    .set("age", 25)                 // Set age
    .set("email", "john@test.com")  // Set email
    .sample();

// Creating order data
Order order = fixtureMonkey.giveMeBuilder(Order.class)
    .set("orderId", "ORDER-001")           // Set order ID
    .set("totalAmount", BigDecimal.valueOf(15000)) // Set order amount
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// Creating member data
val member = fixtureMonkey.giveMeBuilder<Member>()
    .setExp(Member::name, "John Doe")        // Set name
    .setExp(Member::age, 25)                 // Set age
    .setExp(Member::email, "john@test.com")  // Set email
    .sample()

// Creating order data
val order = fixtureMonkey.giveMeBuilder<Order>()
    .setExp(Order::orderId, "ORDER-001")           // Set order ID
    .setExp(Order::totalAmount, BigDecimal.valueOf(15000)) // Set order amount
    .sample()
{{< /tab >}}
{{< /tabpane>}}

### size(), minSize(), maxSize()
The `size()` method is used to specify the size of collections like lists or arrays.
You can set exact sizes or specify minimum/maximum sizes.

#### Basic Usage

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// Adding 3 items to cart
Cart cart = fixtureMonkey.giveMeBuilder(Cart.class)
    .size("items", 3)  // 3 items in cart
    .sample();

// Creating a product with 2-4 reviews
Product product = fixtureMonkey.giveMeBuilder(Product.class)
    .size("reviews", 2, 4)  // Min 2, max 4 reviews
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// Adding 3 items to cart
val cart = fixtureMonkey.giveMeBuilder<Cart>()
    .sizeExp(Cart::items, 3)  // 3 items in cart
    .sample()

// Creating a product with 2-4 reviews
val product = fixtureMonkey.giveMeBuilder<Product>()
    .sizeExp(Product::reviews, 2, 4)  // Min 2, max 4 reviews
    .sample()
{{< /tab >}}
{{< /tabpane>}}

### setNull(), setNotNull()
`setNull()` and `setNotNull()` are used to make properties null or ensure they have values.

#### Basic Usage

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// Creating withdrawn member data (email is null)
Member withdrawnMember = fixtureMonkey.giveMeBuilder(Member.class)
    .set("name", "John Doe")
    .setNull("email")      // Set email to null
    .sample();

// Creating order with required fields
Order validOrder = fixtureMonkey.giveMeBuilder(Order.class)
    .setNotNull("orderId")     // Order ID must exist
    .setNotNull("orderDate")   // Order date must exist
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// Creating withdrawn member data (email is null)
val withdrawnMember = fixtureMonkey.giveMeBuilder<Member>()
    .setExp(Member::name, "John Doe")
    .setNullExp(Member::email)      // Set email to null
    .sample()

// Creating order with required fields
val validOrder = fixtureMonkey.giveMeBuilder<Order>()
    .setNotNullExp(Order::orderId)     // Order ID must exist
    .setNotNullExp(Order::orderDate)   // Order date must exist
    .sample()
{{< /tab >}}
{{< /tabpane>}}

## Learning Intermediate APIs

### setInner()
`setInner()` is used to create reusable settings for multiple tests.
It's useful when you need the same member or order information across different tests.

#### Basic Usage

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// Setting up VIP member info
InnerSpec vipMemberSpec = new InnerSpec()
    .property("grade", "VIP")
    .property("point", 10000)
    .property("joinDate", LocalDate.now().minusYears(1));

// Reusing for VIP member creation
Member vipMember = fixtureMonkey.giveMeBuilder(Member.class)
    .setInner(vipMemberSpec)
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// Setting up VIP member info
val vipMemberSpec = InnerSpec()
    .property("grade", "VIP")
    .property("point", 10000)
    .property("joinDate", LocalDate.now().minusYears(1))

// Reusing for VIP member creation
val vipMember = fixtureMonkey.giveMeBuilder<Member>()
    .setInner(vipMemberSpec)
    .sample()
{{< /tab >}}
{{< /tabpane>}}

### setLazy()
`setLazy()` is used to generate different or sequential values each time.
It's useful for creating sequential order numbers or using current timestamps.

#### Basic Usage

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// Generating sequential order numbers
AtomicInteger orderCounter = new AtomicInteger(1);
Order order = fixtureMonkey.giveMeBuilder(Order.class)
    .setLazy("orderId", () -> "ORDER-" + orderCounter.getAndIncrement())
    .sample();  // ORDER-1

Order nextOrder = fixtureMonkey.giveMeBuilder(Order.class)
    .setLazy("orderId", () -> "ORDER-" + orderCounter.getAndIncrement())
    .sample();  // ORDER-2
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// Generating sequential order numbers
var orderCounter = AtomicInteger(1)
val order = fixtureMonkey.giveMeBuilder<Order>()
    .setLazy("orderId") { "ORDER-${orderCounter.getAndIncrement()}" }
    .sample()  // ORDER-1

val nextOrder = fixtureMonkey.giveMeBuilder<Order>()
    .setLazy("orderId") { "ORDER-${orderCounter.getAndIncrement()}" }
    .sample()  // ORDER-2
{{< /tab >}}
{{< /tabpane>}}

### setPostCondition()
`setPostCondition()` is used to generate values that meet specific conditions.
It's useful when testing services with specific requirements, like adults-only services.

{{< alert icon="🚨" text="If conditions are too strict, finding values might take longer. Use set() when possible." />}}

#### Basic Usage

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// Creating adult members only
Member adultMember = fixtureMonkey.giveMeBuilder(Member.class)
    .setPostCondition("age", Integer.class, age -> age >= 19)
    .sample();

// Creating large orders only
Order largeOrder = fixtureMonkey.giveMeBuilder(Order.class)
    .setPostCondition("totalAmount", BigDecimal.class, 
        amount -> amount.compareTo(BigDecimal.valueOf(100000)) >= 0)
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// Creating adult members only
val adultMember = fixtureMonkey.giveMeBuilder<Member>()
    .setPostConditionExp(Member::age, Int::class.java) { it >= 19 }
    .sample()

// Creating large orders only
val largeOrder = fixtureMonkey.giveMeBuilder<Order>()
    .setPostConditionExp(Order::totalAmount, BigDecimal::class.java) { 
        it >= BigDecimal.valueOf(100000) 
    }
    .sample()
{{< /tab >}}
{{< /tabpane>}}

### fixed()
`fixed()` is used when you need the same test data every time you run your tests.

#### Basic Usage

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// Testing with same member info
Member member = fixtureMonkey.giveMeBuilder(Member.class)
    .set("name", "John Doe")
    .set("age", 30)
    .fixed()  // Generate same data every time
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// Testing with same member info
val member = fixtureMonkey.giveMeBuilder<Member>()
    .setExp(Member::name, "John Doe")
    .setExp(Member::age, 30)
    .fixed()  // Generate same data every time
    .sample()
{{< /tab >}}
{{< /tabpane>}}

### limit
`limit` is used when you want to set specific values for only some elements in a collection.

#### Basic Usage

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// Applying discount to some cart items only
Cart cart = fixtureMonkey.giveMeBuilder(Cart.class)
    .size("items", 5)                    // 5 items
    .set("items[*].onSale", true, 2)    // Only 2 items on sale
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// Applying discount to some cart items only
val cart = fixtureMonkey.giveMeBuilder<Cart>()
    .sizeExp(Cart::items, 5)                    // 5 items
    .set("items[*].onSale", true, 2)    // Only 2 items on sale
    .sample()
{{< /tab >}}
{{< /tabpane>}}

## Using Advanced APIs

### thenApply()
`thenApply()` is used when you need to set values based on other values in the object.
For example, setting an order's total amount based on its item prices.

#### Basic Usage

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// Setting order total as sum of item prices
Order order = fixtureMonkey.giveMeBuilder(Order.class)
    .size("items", 3)  // 3 items
    .thenApply((tempOrder, orderBuilder) -> {
        // Calculate total
        BigDecimal total = tempOrder.getItems().stream()
            .map(item -> item.getPrice())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        // Set calculated total
        orderBuilder.set("totalAmount", total);
    })
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// Setting order total as sum of item prices
val order = fixtureMonkey.giveMeBuilder<Order>()
    .sizeExp(Order::items, 3)  // 3 items
    .thenApply { tempOrder, orderBuilder ->
        // Calculate total
        val total = tempOrder.items
            .map { it.price }
            .fold(BigDecimal.ZERO, BigDecimal::add)
        // Set calculated total
        orderBuilder.set("totalAmount", total)
    }
    .sample()
{{< /tab >}}
{{< /tabpane>}}

## Frequently Asked Questions (FAQ)

### Q: Which APIs should I learn first?

We recommend learning in this order:
1. `set()` - This is the most basic and commonly used API.
2. `size()` - You'll need this when working with lists and arrays.
3. `setNull()`, `setNotNull()` - Use these when handling null values.

After you're comfortable with these, you can gradually learn other APIs.

### Q: How can I use the same test data across tests?

You can use `fixed()`. For example:

```java
// Using same member info across tests
ArbitraryBuilder<Member> memberBuilder = fixtureMonkey.giveMeBuilder(Member.class)
    .set("name", "John Doe")
    .set("age", 30)
    .fixed();  // Generate same data every time

Member member1 = memberBuilder.sample(); // Always same data
Member member2 = memberBuilder.sample(); // Same as member1
```

### Q: How can I prevent incorrect values from being generated?

You can use `setPostCondition()` to specify value ranges or conditions:

```java
// Age must be between 1-100
Member member = fixtureMonkey.giveMeBuilder(Member.class)
    .setPostCondition("age", Integer.class, age -> age >= 1 && age <= 100)
    .sample();
```

