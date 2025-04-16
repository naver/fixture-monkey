---
title: "Customizing objects"
weight: 26
menu:
docs:
parent: "get-started"
identifier: "customizing objects"
---

Fixture Monkey allows you to customize test objects to match your specific test requirements. Let's see how it works with a real-world example.

## Why Customize Test Objects?

Suppose you're testing a discount service that applies a 10% discount only to products priced over 1000. You need to test both scenarios:
- Products that should get a discount (price > 1000)
- Products that shouldn't get a discount (price ≤ 1000)

Without Fixture Monkey, you might write code like this:
```java
// Without Fixture Monkey
Product expensiveProduct = new Product(1, "Expensive Product", 2000, ...);
Product cheapProduct = new Product(2, "Cheap Product", 500, ...);
```

With Fixture Monkey, you can create these test objects more easily and flexibly.

## Step-by-Step Guide

Let's start with a simple Product class:

```java
@Value
public class Product {
    long id;
    String productName;
    long price;
    List<String> options;
    Instant createdAt;
}
```

### Step 1: Create a FixtureMonkey Instance
First, create a FixtureMonkey instance with the appropriate introspector:

{{< tabpane >}}
{{< tab header="Java" >}}
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
    .build();
{{< /tab >}}
{{< tab header="Kotlin" >}}
val fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
    .build()
{{< /tab >}}
{{< /tabpane >}}

### Step 2: Create a Product with Specific Price
Now, let's create a product with a price of 2000 to test the discount scenario:

{{< tabpane >}}
{{< tab header="Java" >}}
@Test
void testDiscountApplied() {
    // given
    Product expensiveProduct = fixtureMonkey.giveMeBuilder(Product.class)
        .set("price", 2000L)    // Set price to 2000
        .sample();

    // when
    double discount = discountService.calculateDiscount(expensiveProduct);

    // then
    then(discount).isEqualTo(200.0);  // 10% of 2000
}
{{< /tab >}}
{{< tab header="Kotlin" >}}
@Test
fun testDiscountApplied() {
    // given
    val expensiveProduct = fixtureMonkey.giveMeBuilder(Product::class.java)
        .set("price", 2000L)    // Set price to 2000
        .sample()

    // when
    val discount = discountService.calculateDiscount(expensiveProduct)

    // then
    then(discount).isEqualTo(200.0)  // 10% of 2000
}
{{< /tab >}}
{{< /tabpane >}}

### Step 3: Create a Product with Customized List
You can also customize collections. For example, to test a product with specific options:

{{< tabpane >}}
{{< tab header="Java" >}}
@Test
void testProductWithOptions() {
    // given
    Product actual = fixtureMonkey.giveMeBuilder(Product.class)
        .size("options", 3)          // Set list size to 3
        .set("options[1]", "red")    // Set second element to "red"
        .sample();

    // then
    then(actual.getOptions()).hasSize(3);
    then(actual.getOptions().get(1)).isEqualTo("red");
}
{{< /tab >}}
{{< tab header="Kotlin" >}}
@Test
fun testProductWithOptions() {
    // given
    val actual = fixtureMonkey.giveMeBuilder(Product::class.java)
        .size("options", 3)          // Set list size to 3
        .set("options[1]", "red")    // Set second element to "red"
        .sample()

    // then
    then(actual.options).hasSize(3)
    then(actual.options[1]).isEqualTo("red")
}
{{< /tab >}}
{{< /tabpane >}}

The generated Product will look like this:
```java
Product(
    id=42,                          // Random value
    productName="product-value-1",  // Random value
    price=2000,                     // Customized value
    options=["option1", "option2"], // Random values
    createdAt=2024-03-21T10:15:30Z // Random value
)
```

## Common Pitfalls and Tips

1. **Field Names**
   - Use exact field names as they appear in your class
   - Wrong: `set("product_name", "test")` (field name mismatch)
   - Right: `set("productName", "test")`
   - Tip: Use IDE's code completion to avoid typos in field names
   - Tip: Use `setExp` or `setExpGetter` for type-safe field access
   - Tip: Install the [Fixture Monkey Helper](https://plugins.jetbrains.com/plugin/19589-fixture-monkey-helper) for enhanced code completion and type safety

{{< tabpane >}}
{{< tab header="Java" >}}
// Type-safe field access
.set(javaGetter(Product::getProductName), "test")
{{< /tab >}}
{{< tab header="Kotlin" >}}
// Type-safe field access
.setExp(Product::productName, "test")
// or
.setExpGetter(Product::productName, { "test" })
{{< /tab >}}
{{< /tabpane >}}

2. **Collection Indexing**
   - Remember that list indices start at 0
   - Wrong: `set("options[3]", "red")` (for a list of size 3)
   - Right: `set("options[2]", "red")`
   - Tip: Use `size()` before setting specific indices to ensure the list is large enough

3. **Type Safety**
   - Make sure to use the correct type for values
   - Wrong: `set("price", "1000")` (String instead of Long)
   - Right: `set("price", 1000L)`
   - Tip: Use IDE's type hints to ensure correct value types

## Comparison: Before and After

Before Fixture Monkey:
```java
// Creating a product with specific options
List<String> options = new ArrayList<>();
options.add("option1");
options.add("red");
options.add("option3");
Product product = new Product(1, "Test Product", 1000, options, Instant.now());
```

After Fixture Monkey:
```java
// Same result with much less code
Product product = fixtureMonkey.giveMeBuilder(Product.class)
    .size("options", 3)
    .set("options[1]", "red")
    .sample();
```

For more examples of how to select properties with expressions and set property values, check out the [customizing section](../../customizing-objects/apis).
