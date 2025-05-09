---
title: "Creating Random Test Data with Conditions"
weight: 43
menu:
docs:
parent: "customizing-objects"
identifier: "arbitrary"
---

## What you will learn in this document
- How to create test data with random but controlled values
- How to set ranges, patterns, and limits for your test data
- When and why to use random values instead of fixed values

## Introduction to Random Test Data
Sometimes in testing, using **fixed values** isn't enough. You might want:
- A range of valid inputs rather than a single value
- Different test data each time the test runs
- Random but realistic data that follows business rules

For example, when testing:
- Age validation: you might want random ages between 18-65
- Username validation: you need random strings following specific patterns
- Payment processing: you need various amounts within certain ranges

## Understanding Arbitrary
In Fixture Monkey, we use `Arbitrary` to create random values that follow rules. Think of an `Arbitrary` as a **value generator with rules**.

> **In simple terms:** An Arbitrary is like a machine that produces random values, but only values that follow your rules.

## Step-by-Step Guide to Random Values

### 1. Basic Usage: Setting a Simple Range

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// Create a member with age between 20 and 30
Member member = fixtureMonkey.giveMeBuilder(Member.class)
    .set("age", Arbitraries.integers().between(20, 30))  // Random age between 20-30
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// Create a member with age between 20 and 30
val member = fixtureMonkey.giveMeBuilder<Member>()
    .setExp(Member::age, Arbitraries.integers().between(20, 30))  // Random age between 20-30
    .sample()
{{< /tab >}}
{{< /tabpane>}}

### 2. Working with Text: String Patterns

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// Create a user with valid username (lowercase letters, 5-10 characters)
User user = fixtureMonkey.giveMeBuilder(User.class)
    .set("username", Arbitraries.strings()
        .withCharRange('a', 'z')  // Only lowercase letters
        .ofMinLength(5)           // At least 5 characters
        .ofMaxLength(10))         // At most 10 characters
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// Create a user with valid username (lowercase letters, 5-10 characters)
val user = fixtureMonkey.giveMeBuilder<User>()
    .setExp(User::username, Arbitraries.strings()
        .withCharRange('a', 'z')  // Only lowercase letters
        .ofMinLength(5)           // At least 5 characters
        .ofMaxLength(10))         // At most 10 characters
    .sample()
{{< /tab >}}
{{< /tabpane>}}

### 3. Selecting from Valid Options

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// Create an order with a valid status
Order order = fixtureMonkey.giveMeBuilder(Order.class)
    .set("status", Arbitraries.of(  // Randomly pick one of these values
        OrderStatus.PENDING,
        OrderStatus.PROCESSING,
        OrderStatus.SHIPPED))
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// Create an order with a valid status
val order = fixtureMonkey.giveMeBuilder<Order>()
    .setExp(Order::status, Arbitraries.of(  // Randomly pick one of these values
        OrderStatus.PENDING,
        OrderStatus.PROCESSING,
        OrderStatus.SHIPPED))
    .sample()
{{< /tab >}}
{{< /tabpane>}}

### 4. Combining Multiple Constraints

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// Create a product with various constraints
Product product = fixtureMonkey.giveMeBuilder(Product.class)
    .set("id", Arbitraries.longs().greaterOrEqual(1000))  // ID at least 1000
    .set("name", Arbitraries.strings().withCharRange('a', 'z').ofMaxLength(10))  // Name max 10 chars
    .set("price", Arbitraries.bigDecimals()
        .between(BigDecimal.valueOf(10.0), BigDecimal.valueOf(1000.0)))  // Price between 10-1000
    .set("category", Arbitraries.of("Electronics", "Clothing", "Books"))  // One of these categories
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// Create a product with various constraints
val product = fixtureMonkey.giveMeBuilder<Product>()
    .setExp(Product::id, Arbitraries.longs().greaterOrEqual(1000))  // ID at least 1000
    .setExp(Product::name, Arbitraries.strings().withCharRange('a', 'z').ofMaxLength(10))  // Name max 10 chars
    .setExp(Product::price, Arbitraries.bigDecimals()
        .between(BigDecimal.valueOf(10.0), BigDecimal.valueOf(1000.0)))  // Price between 10-1000
    .setExp(Product::category, Arbitraries.of("Electronics", "Clothing", "Books"))  // One of these categories
    .sample()
{{< /tab >}}
{{< /tabpane>}}

## Real-world Example: Testing Age Verification

Let's say you're testing a service that only allows adult members (18+) but has senior discounts (65+):

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void adultMembersCanRegister() {
    // Create 50 random adult members for testing
    for (int i = 0; i < 50; i++) {
        Member member = fixtureMonkey.giveMeBuilder(Member.class)
            .set("age", Arbitraries.integers().between(18, 100))  // Adults only
            .sample();
            
        boolean isSenior = member.getAge() >= 65;
        
        // Test registration logic with various ages
        MembershipResponse response = membershipService.register(member);
        
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.hasDiscount()).isEqualTo(isSenior);  // Seniors get discounts
    }
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun adultMembersCanRegister() {
    // Create 50 random adult members for testing
    repeat(50) {
        val member = fixtureMonkey.giveMeBuilder<Member>()
            .setExp(Member::age, Arbitraries.integers().between(18, 100))  // Adults only
            .sample()
            
        val isSenior = member.age >= 65
        
        // Test registration logic with various ages
        val response = membershipService.register(member)
        
        assertThat(response.isSuccess).isTrue()
        assertThat(response.hasDiscount).isEqualTo(isSenior)  // Seniors get discounts
    }
}
{{< /tab >}}
{{< /tabpane>}}

## Common Arbitrary Methods

| Method | Purpose | Example |
|--------|---------|---------|
| `between(min, max)` | Values in range | `Arbitraries.integers().between(1, 100)` |
| `greaterOrEqual(min)` | Values ≥ min | `Arbitraries.longs().greaterOrEqual(1000)` |
| `lessOrEqual(max)` | Values ≤ max | `Arbitraries.doubles().lessOrEqual(99.9)` |
| `ofMaxLength(max)` | Strings with max length | `Arbitraries.strings().ofMaxLength(10)` |
| `withCharRange(from, to)` | Strings with character range | `Arbitraries.strings().withCharRange('a', 'z')` |
| `of(values...)` | Choose from options | `Arbitraries.of("Red", "Green", "Blue")` |

## Frequently Asked Questions

### When should I use Arbitrary instead of fixed values?

Use Arbitrary when:
- You want to test with a variety of inputs rather than a single value
- The exact value doesn't matter, but it needs to follow rules
- You want to discover edge cases automatically
- You need to test many different valid inputs

### Won't random values make my tests inconsistent?

While values are random, they still follow your defined rules. This helps you:
- Find bugs that only appear with certain values
- Ensure your code works with the full range of valid inputs
- Discover unexpected edge cases

If a test fails, you can use Fixture Monkey's `@Seed` annotation to make it reproducible:

```java
import com.navercorp.fixturemonkey.junit.jupiter.annotation.Seed;
import com.navercorp.fixturemonkey.junit.jupiter.extension.FixtureMonkeySeedExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(FixtureMonkeySeedExtension.class)
class MembershipTest {
    @Test
    @Seed(123L)  // Use a specific seed for predictable random values
    void testAdultMembersOnly() {
        Member member = fixtureMonkey.giveMeBuilder(Member.class)
            .set("age", Arbitraries.integers().between(18, 100))
            .sample();
            
        // Your test logic
        assertThat(membershipService.isEligible(member)).isTrue();
    }
}
```

With the `@Seed` annotation, Fixture Monkey will use the specified seed value to generate the same "random" values every time the test runs. This makes tests with random data completely reproducible.

One of the most useful features of `FixtureMonkeySeedExtension` is that it automatically logs the seed value when a test fails:

```
Test Method [MembershipTest#testAdultMembersOnly] failed with seed: 42
```

You can then add this seed value to your `@Seed` annotation to consistently reproduce the exact test scenario that failed.

### How is this different from setPostCondition()?

- `setPostCondition()` generates any value and then checks if it matches a condition
- `Arbitrary` directly generates values that meet the constraints

Use `Arbitrary` when you need more control over the generated values or when `setPostCondition()` is too slow because it has to discard many invalid values.

## Additional Resources

For more details about all available Arbitrary types and methods, see the [Jqwik User Guide](https://jqwik.net/docs/current/user-guide.html#static-arbitraries-methods)
