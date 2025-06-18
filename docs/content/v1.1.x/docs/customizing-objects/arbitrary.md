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

## Advanced Arbitrary Types (Experimental)

Since version 1.1.12, Fixture Monkey provides specialized arbitrary types for more control over value generation.

### CombinableArbitrary.integers()

The `CombinableArbitrary.integers()` method returns an `IntegerCombinableArbitrary` that provides specialized methods for integer generation:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// Generate integers with various constraints
Member member = fixtureMonkey.giveMeBuilder(Member.class)
    .set("age", CombinableArbitrary.integers()
        .withRange(18, 65)     // Age between 18-65
        .positive())           // Only positive numbers
    .set("score", CombinableArbitrary.integers()
        .even()                // Only even numbers
        .withRange(0, 100))    // Between 0-100
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// Generate integers with various constraints
val member = fixtureMonkey.giveMeBuilder<Member>()
    .setExp(Member::age, CombinableArbitrary.integers()
        .withRange(18, 65)     // Age between 18-65
        .positive())           // Only positive numbers
    .setExp(Member::score, CombinableArbitrary.integers()
        .even()                // Only even numbers
        .withRange(0, 100))    // Between 0-100
    .sample()
{{< /tab >}}
{{< /tabpane>}}

#### IntegerCombinableArbitrary Methods

| Method | Description | Example |
|--------|-------------|---------|
| `withRange(min, max)` | Generate integers between min and max (inclusive) | `integers().withRange(1, 100)` |
| `positive()` | Generate only positive integers (≥ 1) | `integers().positive()` |
| `negative()` | Generate only negative integers (≤ -1) | `integers().negative()` |
| `even()` | Generate only even integers | `integers().even()` |
| `odd()` | Generate only odd integers | `integers().odd()` |

**Important Note:** When multiple constraint methods are chained, the **last method wins**. For example:
```java
// This will generate negative integers, ignoring the positive() call
CombinableArbitrary.integers().positive().negative()

// This will generate integers in range 10-50, ignoring the positive() call
CombinableArbitrary.integers().positive().withRange(10, 50)
```

### CombinableArbitrary.strings()

The `CombinableArbitrary.strings()` method returns a `StringCombinableArbitrary` that provides specialized methods for string generation:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// Generate strings with various character sets and constraints
User user = fixtureMonkey.giveMeBuilder(User.class)
    .set("username", CombinableArbitrary.strings()
        .alphabetic()          // Only alphabetic characters
        .withLength(5, 15))    // Length between 5-15
    .set("password", CombinableArbitrary.strings()
        .ascii()               // ASCII characters
        .withMinLength(8))     // At least 8 characters
    .set("phoneNumber", CombinableArbitrary.strings()
        .numeric()             // Only numeric characters
        .withLength(10, 11))   // 10 or 11 digits
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// Generate strings with various character sets and constraints
val user = fixtureMonkey.giveMeBuilder<User>()
    .setExp(User::username, CombinableArbitrary.strings()
        .alphabetic()          // Only alphabetic characters
        .withLength(5, 15))    // Length between 5-15
    .setExp(User::password, CombinableArbitrary.strings()
        .ascii()               // ASCII characters
        .withMinLength(8))     // At least 8 characters
    .setExp(User::phoneNumber, CombinableArbitrary.strings()
        .numeric()             // Only numeric characters
        .withLength(10, 11))   // 10 or 11 digits
    .sample()
{{< /tab >}}
{{< /tabpane>}}

#### StringCombinableArbitrary Methods

| Method | Description | Example |
|--------|-------------|---------|
| `withLength(min, max)` | Generate strings with length between min and max | `strings().withLength(5, 10)` |
| `withMinLength(min)` | Generate strings with minimum length | `strings().withMinLength(3)` |
| `withMaxLength(max)` | Generate strings with maximum length | `strings().withMaxLength(20)` |
| `alphabetic()` | Generate strings with only alphabetic characters (a-z, A-Z) | `strings().alphabetic()` |
| `ascii()` | Generate strings with only ASCII characters | `strings().ascii()` |
| `numeric()` | Generate strings with only numeric characters (0-9) | `strings().numeric()` |
| `korean()` | Generate strings with only Korean characters (가-힣) | `strings().korean()` |
| `filterCharacter(predicate)` | Filter individual characters in the string | `strings().filterCharacter(c -> c != 'x')` |

**Important Notes:** 
1. **Character set methods conflict with each other.** When multiple character set methods are chained, the **last method wins**:
   ```java
   // This will generate Korean characters only, ignoring alphabetic()
   CombinableArbitrary.strings().alphabetic().korean()
   ```

2. **Character set methods ignore other configuration methods.** When a character set method is called, it creates a new instance that ignores previous configurations:
   ```java
   // The withLength(5, 10) is ignored when alphabetic() is called
   CombinableArbitrary.strings().withLength(5, 10).alphabetic()
   ```

### Advanced Filtering

Both `IntegerCombinableArbitrary` and `StringCombinableArbitrary` support advanced filtering:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// Filter integers with custom conditions
Integer score = CombinableArbitrary.integers()
    .withRange(0, 100)
    .filter(n -> n % 5 == 0)  // Only multiples of 5
    .combined();

// Filter strings with custom character conditions
String code = CombinableArbitrary.strings()
    .withLength(6, 8)
    .filterCharacter(c -> Character.isUpperCase(c) || Character.isDigit(c))  // Only uppercase letters and digits
    .combined();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// Filter integers with custom conditions
val score = CombinableArbitrary.integers()
    .withRange(0, 100)
    .filter { it % 5 == 0 }  // Only multiples of 5
    .combined()

// Filter strings with custom character conditions
val code = CombinableArbitrary.strings()
    .withLength(6, 8)
    .filterCharacter { it.isUpperCase() || it.isDigit() }  // Only uppercase letters and digits
    .combined()
{{< /tab >}}
{{< /tabpane>}}

### Real-world Example: User Registration Validation

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void validateUserRegistrationWithVariousInputs() {
    for (int i = 0; i < 100; i++) {
        User user = fixtureMonkey.giveMeBuilder(User.class)
            .set("username", CombinableArbitrary.strings()
                .alphabetic()
                .withLength(3, 20))           // Valid username: 3-20 alphabetic chars
            .set("email", CombinableArbitrary.strings()
                .ascii()
                .withLength(5, 50)
                .filter(s -> s.contains("@"))) // Simple email validation
            .set("age", CombinableArbitrary.integers()
                .withRange(13, 120))          // Valid age range
            .set("score", CombinableArbitrary.integers()
                .withRange(0, 100)
                .filter(n -> n % 10 == 0))    // Score in multiples of 10
            .sample();
            
        // Test with various valid inputs
        ValidationResult result = userService.validateRegistration(user);
        assertThat(result.isValid()).isTrue();
    }
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun validateUserRegistrationWithVariousInputs() {
    repeat(100) {
        val user = fixtureMonkey.giveMeBuilder<User>()
            .setExp(User::username, CombinableArbitrary.strings()
                .alphabetic()
                .withLength(3, 20))           // Valid username: 3-20 alphabetic chars
            .setExp(User::email, CombinableArbitrary.strings()
                .ascii()
                .withLength(5, 50)
                .filter { it.contains("@") }) // Simple email validation
            .setExp(User::age, CombinableArbitrary.integers()
                .withRange(13, 120))          // Valid age range
            .setExp(User::score, CombinableArbitrary.integers()
                .withRange(0, 100)
                .filter { it % 10 == 0 })     // Score in multiples of 10
            .sample()
            
        // Test with various valid inputs
        val result = userService.validateRegistration(user)
        assertThat(result.isValid).isTrue()
    }
}
{{< /tab >}}
{{< /tabpane>}}

## Additional Resources

For more details about all available Arbitrary types and methods, see the [Jqwik User Guide](https://jqwik.net/docs/current/user-guide.html#static-arbitraries-methods)
