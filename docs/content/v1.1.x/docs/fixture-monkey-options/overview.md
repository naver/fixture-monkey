---
title: "Overview"
images: []
menu:
docs:
parent: "fixture-monkey-options"
identifier: "overview"
weight: 50
---

When you're starting with Fixture Monkey, using options can seem overwhelming. This guide helps you understand how to navigate the options and where to start.

## Options vs ArbitraryBuilder API

There are two main ways to configure test data in Fixture Monkey:

1. **Options**
   - Set during FixtureMonkey instance creation
   - Define global rules that apply to all test data generation
   - Reusable configurations

2. **ArbitraryBuilder API**
   - Set during individual test data creation
   - One-time settings needed for specific test cases
   - More fine-grained control

Example:

```java
// Using options - applies to all Product instances
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .defaultNotNull(true)  // Set all fields to non-null
    .register(Product.class, fm -> fm.giveMeBuilder(Product.class)
        .size("items", 3))  // items always has 3 elements
    .build();

// Using ArbitraryBuilder API - applies only to this test
Product specificProduct = fixtureMonkey.giveMeBuilder(Product.class)
    .set("name", "Test Product")  // Set name just for this test
    .set("price", 1000)          // Set price just for this test
    .sample();
```

## Why Should You Use Options?

There are important reasons to use options:

### 1. Test Data Consistency
- **Problem**: Need to apply the same rules across multiple tests
  ```java
  // Without options - need to repeat settings in every test
  Product product1 = fixtureMonkey.giveMeBuilder(Product.class)
      .set("price", Arbitraries.longs().greaterThan(0))
      .sample();
  
  Product product2 = fixtureMonkey.giveMeBuilder(Product.class)
      .set("price", Arbitraries.longs().greaterThan(0))
      .sample();
  ```
  ```java
  // With options - set once, apply everywhere
  FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
      .register(Product.class, fm -> fm.giveMeBuilder(Product.class)
          .set("price", Arbitraries.longs().greaterThan(0)))
      .build();
  
  Product product1 = fixtureMonkey.giveMeOne(Product.class);  // Automatically positive price
  Product product2 = fixtureMonkey.giveMeOne(Product.class);  // Automatically positive price
  ```

### 2. Test Maintainability
- **Problem**: Need to modify all tests when rules change
  ```java
  // Using options - manage rules in one place
  public class TestConfig {
      public static FixtureMonkey createFixtureMonkey() {
          return FixtureMonkey.builder()
              .defaultNotNull(true)
              .register(Product.class, fm -> fm.giveMeBuilder(Product.class)
                  .set("price", Arbitraries.longs().greaterThan(0))
                  .set("stock", Arbitraries.integers().greaterThan(0)))
              .register(Order.class, fm -> fm.giveMeBuilder(Order.class)
                  // Add orderRules() builder chaining here
              )
              .build();
      }
  }
  ```

### 3. Domain Rule Application
- **Problem**: Need to apply business rules to test data
  ```java
  // Applying domain rules through options
  FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
      .register(Order.class, fm -> fm.giveMeBuilder(Order.class)
          .thenApply((order, b) -> {
              b.set("totalAmount", order.getItems().stream()
                  .mapToInt(Item::getPrice)
                  .sum()
              );
          })
      )
      .build();
  ```

## Most Common Options for Beginners

Here are the essential options you'll likely need first:

### 1. defaultNotNull Option - Preventing null Values

The `defaultNotNull` option ensures that properties not explicitly marked as nullable (with annotations like `@Nullable` in Java or `?` in Kotlin) will not be null. This is useful when you want to avoid null-related issues in your tests.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void testDefaultNotNullOption() {
    // Create a FixtureMonkey instance with defaultNotNull option
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .defaultNotNull(true)  // This option ensures properties without @Nullable annotation won't be null
        .build();
    
    // Generate a Product - all properties without annotations will be non-null
    Product product = fixtureMonkey.giveMeOne(Product.class);
    
    assertThat(product.getProductName()).isNotNull();
    assertThat(product.getPrice()).isNotNull();
    assertThat(product.getCategory()).isNotNull();
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun testDefaultNotNullOption() {
    // Create a FixtureMonkey instance with defaultNotNull option
    val fixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .defaultNotNull(true)  // This option ensures properties without ? marker won't be null
        .build()
    
    // Generate a Product - all non-nullable properties will be non-null
    val product = fixtureMonkey.giveMeOne<Product>()
    
    assertThat(product.productName).isNotNull()
    assertThat(product.price).isNotNull()
    assertThat(product.category).isNotNull()
}
{{< /tab >}}
{{< /tabpane>}}

### 2. javaTypeArbitraryGenerator Option - Controlling Basic Type Generation

The `javaTypeArbitraryGenerator` option allows you to customize how basic Java types (String, Integer, etc.) are generated. This option is applied through the JqwikPlugin, which integrates Fixture Monkey with the Jqwik property-based testing library.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void testJavaTypeArbitraryGeneratorOption() {
    // Create a FixtureMonkey with custom string generation
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .plugin(
            new JqwikPlugin()  // JqwikPlugin provides integration with the Jqwik library
                .javaTypeArbitraryGenerator(new JavaTypeArbitraryGenerator() {
                    @Override
                    public StringArbitrary strings() {
                        // Customize to generate strings with only alphabetic characters
                        return Arbitraries.strings().alpha().ofLength(10);
                    }
                })
        )
        .build();
    
    // All generated strings will be 10-character alphabetic strings
    String generatedString = fixtureMonkey.giveMeOne(String.class);
    
    assertThat(generatedString).hasSize(10);
    assertThat(generatedString).matches("[a-zA-Z]+");
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun testJavaTypeArbitraryGeneratorOption() {
    // Create a FixtureMonkey with custom string generation
    val fixtureMonkey = FixtureMonkey.builder()
        .plugin(
            JqwikPlugin()  // JqwikPlugin provides integration with the Jqwik library
                .javaTypeArbitraryGenerator(object : JavaTypeArbitraryGenerator {
                    override fun strings(): StringArbitrary {
                        // Customize to generate strings with only alphabetic characters
                        return Arbitraries.strings().alpha().ofLength(10)
                    }
                })
        )
        .build()
    
    // All generated strings will be 10-character alphabetic strings
    val generatedString = fixtureMonkey.giveMeOne<String>()
    
    assertThat(generatedString).hasSize(10)
    assertThat(generatedString).matches("[a-zA-Z]+")
}
{{< /tab >}}
{{< /tabpane>}}

### 3. register Option - Setting Type-Specific Default Rules

The `register` option allows you to configure default settings for specific types. This is useful when you have consistent requirements for a class across multiple tests.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void testRegisterOption() {
    // Create a FixtureMonkey with defaults for the Product class
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .register(
            Product.class,
            fm -> fm.giveMeBuilder(Product.class)
                .set("price", Arbitraries.longs().greaterThan(0))  // Allow only positive prices
                .set("category", "Electronics")  // Fixed category
        )
        .build();
    
    // All Products will have positive prices and 'Electronics' category
    Product product = fixtureMonkey.giveMeOne(Product.class);
    
    assertThat(product.getPrice()).isPositive();
    assertThat(product.getCategory()).isEqualTo("Electronics");
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun testRegisterOption() {
    // Create a FixtureMonkey with defaults for the Product class
    val fixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .register(Product::class.java) { builder ->
            builder.giveMeBuilder<Product>()
                .set("price", Arbitraries.longs().greaterThan(0))  // Allow only positive prices
                .set("category", "Electronics")  // Fixed category
        }
        .build()
    
    // All Products will have positive prices and 'Electronics' category
    val product = fixtureMonkey.giveMeOne<Product>()
    
    assertThat(product.price).isPositive()
    assertThat(product.category).isEqualTo("Electronics")
}
{{< /tab >}}
{{< /tabpane>}}

### 4. plugin Option - Adding Extended Functionality

The `plugin` option allows you to integrate additional features provided by various plugins that Fixture Monkey offers. This option is essential when working with specific frameworks or libraries.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void testPluginOption() {
    // Create a FixtureMonkey with Jackson plugin for JSON support
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .plugin(new JacksonPlugin())  // Add support for Jackson annotations
        .build();
    
    // Now you can create objects with proper Jackson annotation support
    JsonProduct product = fixtureMonkey.giveMeOne(JsonProduct.class);
    
    // Jackson annotations on JsonProduct will be respected
    // (e.g., @JsonProperty, @JsonIgnore, etc.)
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun testPluginOption() {
    // Create a FixtureMonkey with Jackson plugin for JSON support
    val fixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())  // Support for Kotlin features
        .plugin(JacksonPlugin())  // Add support for Jackson annotations
        .build()
    
    // Now you can create objects with proper Jackson annotation support
    val product = fixtureMonkey.giveMeOne<JsonProduct>()
    
    // Jackson annotations on JsonProduct will be respected
    // (e.g., @JsonProperty, @JsonIgnore, etc.)
}
{{< /tab >}}
{{< /tabpane>}}

### 5. defaultArbitraryContainerInfoGenerator Option - Controlling Container Sizes

The `defaultArbitraryContainerInfoGenerator` option allows you to control the size of generated container types such as lists, sets, and maps. This option is useful when you need containers of specific sizes in your tests.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void testContainerSizeOption() {
    // Create a FixtureMonkey with fixed container sizes
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .defaultArbitraryContainerInfoGenerator(context -> new ArbitraryContainerInfo(3, 3))  // All containers will have exactly 3 elements
        .build();
    
    // Generate a list with exactly 3 elements
    List<String> stringList = fixtureMonkey.giveMeOne(new TypeReference<List<String>>() {});
    
    assertThat(stringList).hasSize(3);
    
    // Generate a map with exactly 3 entries
    Map<Integer, String> map = fixtureMonkey.giveMeOne(new TypeReference<Map<Integer, String>>() {});
    
    assertThat(map).hasSize(3);
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun testContainerSizeOption() {
    // Create a FixtureMonkey with fixed container sizes
    val fixtureMonkey = FixtureMonkey.builder()
        .defaultArbitraryContainerInfoGenerator { context -> ArbitraryContainerInfo(3, 3) }  // All containers will have exactly 3 elements
        .build()
    
    // Generate a list with exactly 3 elements
    val stringList: List<String> = fixtureMonkey.giveMeOne()
    
    assertThat(stringList).hasSize(3)
    
    // Generate a map with exactly 3 entries
    val map: Map<Int, String> = fixtureMonkey.giveMeOne()
    
    assertThat(map).hasSize(3)
}
{{< /tab >}}
{{< /tabpane>}}

## Which Option to Use When?

Here's a simple guide to help you choose which option to use:

| Option | When to Use |
|--------|-------------|
| `defaultNotNull(true)` | When you want to ensure test objects have no null values (except explicitly nullable properties) |
| `javaTypeArbitraryGenerator` | When you need to customize how basic types like strings or numbers are generated |
| `register(Class, function)` | When you need consistent default values or constraints for a specific class |
| `plugin(Plugin)` | When you need additional features like support for frameworks (Jackson, Kotlin, etc.) |
| `defaultArbitraryContainerInfoGenerator` | When you need to control the size of generated containers (lists, sets, maps, etc.) |

## Understanding Option Scope

There are important points to understand when using options:

1. **Instance Scope**
   - Options only apply to the FixtureMonkey instance they're configured on
   - You can create multiple instances with different settings

```java
// Test settings
FixtureMonkey testFixture = FixtureMonkey.builder()
    .defaultNotNull(true)
    .build();

// Development settings
FixtureMonkey devFixture = FixtureMonkey.builder()
    .defaultNotNull(false)
    .build();
```

2. **Option Priority**
   - More specific options take precedence over general ones
   - Later options override earlier ones

```java
FixtureMonkey fixture = FixtureMonkey.builder()
    .defaultNotNull(true)            // All fields non-null
    .register(Product.class, fm -> fm.giveMeBuilder(Product.class)
        .setNull("description"))     // Allow null for description only
    .build();
```

## Next Steps

We recommend following this learning path to make the most of Fixture Monkey:

1. **Start here**: Overview (this page) - Understand what options are and how they're structured
2. **Next step for beginners**: [Essential Options for Beginners](../essential-options-for-beginners) - Learn the most commonly used options for everyday testing needs
3. **Understand the concepts**: [Option Concepts](../concepts) - Gain deeper knowledge of how options work internally and learn key terminology
4. **Advanced features**: [Advanced Options for Experts](../advanced-options-for-experts) - Explore options for complex testing scenarios
