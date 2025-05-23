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
  - [customizeProperty() - Fine-tune property generation behavior](#customizeproperty)
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
| customizeProperty() | Fine-tune property generation behavior | Filter values, transform data, generate unique values |

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

### customizeProperty()
`customizeProperty()` is used when you want to fine-tune how Fixture Monkey generates values for specific properties.
This is an advanced feature that gives you more control than `set()` by allowing transformations and filtering.

#### When do you need customizeProperty()?

You'll find `customizeProperty()` useful when:
- **You want to transform generated values**: "Make all names start with 'Mr.'"
- **You need conditional filtering**: "Only positive numbers"
- **You want unique values in collections**: "No duplicate items in a list"

{{< alert icon="⚠️" text="customizeProperty requires TypedPropertySelector. This is more complex than basic APIs, so make sure you're comfortable with set(), size(), and other basic APIs first." />}}

#### Simple Property Customization

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// You need to import the property selector
import static com.navercorp.fixturemonkey.api.experimental.JavaGetterMethodPropertySelector.javaGetter;

// Transform a property value
String expected = "transformed";
String actual = fixtureMonkey.giveMeBuilder(Member.class)
    .customizeProperty(javaGetter(Member::getName), arb -> arb.map(name -> expected))
    .sample()
    .getName();

// Filter values to meet conditions  
Member adult = fixtureMonkey.giveMeBuilder(Member.class)
    .customizeProperty(javaGetter(Member::getAge), arb -> arb.filter(age -> age >= 18))
    .sample();

// Combine filtering and transformation
Member vipMember = fixtureMonkey.giveMeBuilder(Member.class)
    .customizeProperty(javaGetter(Member::getEmail), arb -> 
        arb.filter(email -> email.contains("@"))
           .map(email -> "vip-" + email))
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// Kotlin can use property references directly
class StringObject(val string: String)

val expected = "test"
val actual = fixtureMonkey.giveMeKotlinBuilder<StringObject>()
    .customizeProperty(StringObject::string) {
        it.map { _ -> expected }
    }
    .sample()
    .string

// Filter values to meet conditions
class Member(val name: String, val age: Int)

val adult = fixtureMonkey.giveMeKotlinBuilder<Member>()
    .customizeProperty(Member::age) { arb -> 
        arb.filter { age -> age >= 18 }
    }
    .sample()

// Combine filtering and transformation  
val vipMember = fixtureMonkey.giveMeKotlinBuilder<Member>()
    .customizeProperty(Member::name) { arb ->
        arb.filter { name -> name.isNotBlank() }
           .map { name -> "VIP-$name" }
    }
    .sample()
{{< /tab >}}
{{< /tabpane>}}

#### Working with Nested Properties

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// Customize nested object properties
String nestedValue = fixtureMonkey.giveMeBuilder(Order.class)
    .customizeProperty(
        javaGetter(Order::getCustomer).into(Customer::getName),
        arb -> arb.map(name -> "Mr. " + name)
    )
    .sample()
    .getCustomer()
    .getName();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// Customize nested object properties
class Customer(val name: String)
class Order(val customer: Customer)

val nestedValue = fixtureMonkey.giveMeKotlinBuilder<Order>()
    .customizeProperty(Order::customer into Customer::name) {
        it.map { name -> "Mr. $name" }
    }
    .sample()
    .customer
    .name
{{< /tab >}}
{{< /tabpane>}}

#### Working with Collections

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// Customize individual elements in a collection
String firstItem = fixtureMonkey.giveMeBuilder(Cart.class)
    .size("items", 3)
    .customizeProperty(
        javaGetter(Cart::getItems).index(String.class, 0),
        arb -> arb.map(item -> "ITEM-" + item)
    )
    .sample()
    .getItems()
    .get(0);

// Make a list unique (requires experimental API)
import static com.navercorp.fixturemonkey.api.experimental.TypedExpressionGenerator.typedRoot;

List<Integer> uniqueList = fixtureMonkey.giveMeExperimentalBuilder(new TypeReference<List<Integer>>() {})
    .<List<Integer>>customizeProperty(typedRoot(), CombinableArbitrary::unique)
    .size("$", 10)
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// Customize individual elements in a collection  
class Cart(val items: List<String>)

val firstItem = fixtureMonkey.giveMeKotlinBuilder<Cart>()
    .size(Cart::items, 3)
    .customizeProperty(Cart::items[0]) {
        it.map { item -> "ITEM-$item" }
    }
    .sample()
    .items[0]

// Make a list unique (requires experimental API)
import com.navercorp.fixturemonkey.api.experimental.TypedExpressionGenerator.typedRoot

val uniqueList = fixtureMonkey.giveMeExperimentalBuilder<List<Int>>()
    .customizeProperty(typedRoot<List<Int>>()) { 
        it.unique() 
    }
    .size(List<Int>::root, 10)
    .sample()
{{< /tab >}}
{{< /tabpane>}}

#### Real-World Testing Scenarios

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// Testing user registration with business rules
Member validUser = fixtureMonkey.giveMeBuilder(Member.class)
    .customizeProperty(javaGetter(Member::getEmail), arb ->
        arb.filter(email -> email.contains("@") && email.contains("."))
           .map(email -> email.toLowerCase()))
    .customizeProperty(javaGetter(Member::getAge), arb ->
        arb.filter(age -> age >= 18 && age <= 120))
    .sample();

// Testing orders with minimum amounts
Order validOrder = fixtureMonkey.giveMeBuilder(Order.class)
    .customizeProperty(javaGetter(Order::getTotalAmount), arb ->
        arb.filter(amount -> amount.compareTo(BigDecimal.valueOf(10)) >= 0))
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// Testing user registration with business rules
class User(val email: String, val age: Int, val name: String)

val validUser = fixtureMonkey.giveMeKotlinBuilder<User>()
    .customizeProperty(User::email) { arb ->
        arb.filter { email -> email.contains("@") && email.contains(".") }
           .map { email -> email.lowercase() }
    }
    .customizeProperty(User::age) { arb ->
        arb.filter { age -> age in 18..120 }
    }
    .sample()

// Testing orders with minimum amounts  
class Order(val totalAmount: BigDecimal)

val validOrder = fixtureMonkey.giveMeKotlinBuilder<Order>()
    .customizeProperty(Order::totalAmount) { arb ->
        arb.filter { amount -> amount >= BigDecimal.valueOf(10) }
    }
    .sample()
{{< /tab >}}
{{< /tabpane>}}

#### Important Things to Remember

1. **Learn basic APIs first**: Make sure you understand `set()`, `size()`, `setNull()` before using `customizeProperty()`

2. **Import required classes**:
   ```java
   // For Java
   import static com.navercorp.fixturemonkey.api.experimental.JavaGetterMethodPropertySelector.javaGetter;
   
   // For experimental features
   import static com.navercorp.fixturemonkey.api.experimental.TypedExpressionGenerator.typedRoot;
   ```

3. **Order matters**: `set()` will override `customizeProperty()`
   ```java
   // This won't work as expected
   .customizeProperty(javaGetter(Member::getName), arb -> arb.map(name -> "Mr. " + name))
   .set("name", "John")  // This overwrites the customization above
   ```

4. **Keep filters reasonable**: Too strict filters can cause generation to fail
   ```java
   // Too strict - might fail
   .customizeProperty(javaGetter(Member::getAge), arb -> arb.filter(age -> age == 25))
   
   // Better - more flexible range
   .customizeProperty(javaGetter(Member::getAge), arb -> arb.filter(age -> age >= 20 && age <= 30))
   ```

5. **Use for complex transformations**: If you just need a specific value, use `set()` instead

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

### Q: What's the difference between set() and customizeProperty()?

- `set()` directly assigns a specific value to a property
- `customizeProperty()` modifies how the property value is generated, allowing for filtering, transformation, and conditional logic

Use `set()` when you know exactly what value you want. Use `customizeProperty()` when you need to apply transformations or filters to generated values:

```java
// Direct assignment with set()
Member member = fixtureMonkey.giveMeBuilder(Member.class)
    .set("email", "john@test.com")
    .sample();

// Transformation with customizeProperty()  
Member memberWithCustomEmail = fixtureMonkey.giveMeBuilder(Member.class)
    .customizeProperty("email", arb -> arb.map(email -> "vip-" + email))
    .sample();
```

### Q: Can I use customizeProperty() with collections?

Yes! You can customize individual elements or the entire collection:

```java
// Customize all elements in a list
List<String> customizedList = fixtureMonkey.giveMeBuilder(new TypeReference<List<String>>() {})
    .customizeProperty("$[*]", arb -> arb.map(str -> "PREFIX-" + str))
    .sample();

// Make the list unique
List<Integer> uniqueList = fixtureMonkey.giveMeBuilder(new TypeReference<List<Integer>>() {})
    .<List<Integer>>customizeProperty(typedRoot(), CombinableArbitrary::unique)
    .size("$", 10)
    .sample();
```

