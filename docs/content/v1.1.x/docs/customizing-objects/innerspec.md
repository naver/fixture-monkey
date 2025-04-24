---
title: "InnerSpec"
weight: 45
menu:
docs:
parent: "customizing-objects"
identifier: "innerspec"
mermaid: true
---

## What you will learn in this document
- How to customize complex object structures more granularly
- How to effectively handle Map type properties
- How to create reusable customization specifications

## Introduction to InnerSpec

> *In this section, you'll learn the basics of InnerSpec and why it's useful for customizing complex objects.*

If you've learned the basic property modification methods in previous documents, now it's time to learn how to handle **more complex object structures**. 

### What is InnerSpec and why use it?

**InnerSpec** is a powerful tool in Fixture Monkey that helps you customize complex nested objects in a structured way. Think of it as a "specification" of how you want your objects to be customized.

You might want to use InnerSpec when:
- You need to customize deeply nested objects
- You're working with Map type properties (which can't be easily customized with regular expressions)
- You want to create reusable customization patterns across multiple tests
- You need more control over complex object structures

An InnerSpec is a type-independent specification for the customizations you wish to apply.
Using the `setInner()` method within ArbitraryBuilder, you can apply customizations defined within an `InnerSpec` instance into your builder.

### A Simple Example

Let's start with a very simple example to understand how InnerSpec works. Imagine you have a Product class:

```java
public class Product {
    private Long id;
    private String name;
    private BigDecimal price;
    // getters and setters
}
```

Here's how you might customize it using InnerSpec:

```java
// Create a Fixture Monkey instance
FixtureMonkey fixtureMonkey = FixtureMonkey.create();

// Create an InnerSpec to customize product properties
InnerSpec productSpec = new InnerSpec()
    .property("id", 1000L)
    .property("name", "Smartphone")
    .property("price", new BigDecimal("499.99"));

// Apply the InnerSpec to a Product builder
Product product = fixtureMonkey.giveMeBuilder(Product.class)
    .setInner(productSpec)
    .sample();

// Now product has id=1000, name="Smartphone", price=499.99
```

## Visual Representation of InnerSpec

InnerSpec allows you to define customizations in a more structured way. Here's a visual representation:

{{< mermaid >}}
flowchart LR
    subgraph ArbitraryBuilder
    AB[".setInner(innerSpec)"]
    end
    
    subgraph InnerSpec
    IS1[".property('id', 1000)"]
    IS2[".property('options', options -> options.size(3))"]
    IS3[".property('nested', nested -> nested.property('field', 'value'))"]
    end
    
    AB -->|applies| IS1
    AB -->|applies| IS2
    AB -->|applies| IS3
    
    note["InnerSpec can be reused across multiple builders"]
    InnerSpec --- note
{{</ mermaid >}}

### When to use InnerSpec vs Regular Expressions

Let's compare how you would customize a nested structure:

#### Using path expressions:
```java
builder.set("merchantInfo.id", 1001)
       .set("merchantInfo.name", "ABC Store")
       .set("merchantInfo.address.city", "Seoul")
```

#### Using InnerSpec (more structured):
```java
InnerSpec merchantSpec = new InnerSpec()
    .property("id", 1001)
    .property("name", "ABC Store")
    .property("address", address -> address.property("city", "Seoul"));

builder.setInner(
    new InnerSpec().property("merchantInfo", merchantInfo -> merchantInfo.inner(merchantSpec))
);
```

**When to choose InnerSpec:**
- When you need to customize Map properties (not possible with regular expressions)
- When you want to reuse the same customization patterns across multiple tests
- When you have complex nested object structures that are clearer to express as nested InnerSpecs
- When you want more structured and type-independent customizations

**When to choose regular expressions:**
- For simple property access and customization
- For less deeply nested structures
- When you want more concise code for simple customizations

An added advantage of InnerSpec is its ability to customize map properties, unlike normal expressions.

{{< alert icon="💡" text="Kotlin EXP is not supported for InnerSpec, as it is designed to be type-independent. Instead, you need to specify the property by its name." />}}

## Step-by-Step Tutorial: Customizing a Complex Object

Let's walk through a complete example to see how InnerSpec can be used to customize a complex object structure.

### Step 1: Define the classes

First, let's define some classes that represent a typical e-commerce domain model:

```java
// A simple Address class
public class Address {
    private String street;
    private String city;
    private String country;
    private String zipCode;
    // getters and setters
}

// A store with location and contact information
public class Store {
    private Long id;
    private String name;
    private Address address;
    private Map<String, String> contactInfo; // e.g., "phone" -> "123-456-7890"
    // getters and setters
}

// A product sold by the store
public class Product {
    private Long id;
    private String name;
    private BigDecimal price;
    private List<String> categories;
    private Store store;
    // getters and setters
}
```

### Step 2: Create an InnerSpec for Address

Let's start by creating an InnerSpec for the Address:

```java
InnerSpec addressSpec = new InnerSpec()
    .property("street", "123 Main St")
    .property("city", "New York")
    .property("country", "USA")
    .property("zipCode", "10001");
```

### Step 3: Create an InnerSpec for Store with the contactInfo Map

Now, let's create an InnerSpec for the Store, incorporating the Address spec and setting up the contactInfo map:

```java
InnerSpec storeSpec = new InnerSpec()
    .property("id", 500L)
    .property("name", "Electronics Store")
    .property("address", address -> address.inner(addressSpec))
    .property("contactInfo", contactInfo -> contactInfo
        .size(2) // Set map size to 2 entries
        .entry("phone", "123-456-7890")
        .entry("email", "contact@electronics.com"));
```

### Step 4: Create an InnerSpec for Product with categories List

Finally, let's create an InnerSpec for the Product, incorporating the Store spec and setting up the categories list:

```java
InnerSpec productSpec = new InnerSpec()
    .property("id", 1000L)
    .property("name", "Ultra HD TV")
    .property("price", new BigDecimal("1299.99"))
    .property("categories", categories -> categories
        .size(3) // Set list size to 3
        .listElement(0, "Electronics")
        .listElement(1, "TVs")
        .listElement(2, "Ultra HD"))
    .property("store", store -> store.inner(storeSpec));
```

### Step 5: Apply the InnerSpec to create a Product

Now, let's use the InnerSpec to create a Product instance:

```java
// Create a Fixture Monkey instance
FixtureMonkey fixtureMonkey = FixtureMonkey.create();

// Apply the InnerSpec to create a Product
Product product = fixtureMonkey.giveMeBuilder(Product.class)
    .setInner(productSpec)
    .sample();

// Now we have a fully customized Product with all its nested objects
```

### Step 6: Verify the result

You can verify that all properties were correctly set:

```java
// Verify Product properties
assertEquals(1000L, product.getId());
assertEquals("Ultra HD TV", product.getName());
assertEquals(new BigDecimal("1299.99"), product.getPrice());

// Verify categories list
List<String> expectedCategories = List.of("Electronics", "TVs", "Ultra HD");
assertEquals(expectedCategories, product.getCategories());

// Verify Store properties
Store store = product.getStore();
assertEquals(500L, store.getId());
assertEquals("Electronics Store", store.getName());

// Verify Address properties
Address address = store.getAddress();
assertEquals("123 Main St", address.getStreet());
assertEquals("New York", address.getCity());
assertEquals("USA", address.getCountry());
assertEquals("10001", address.getZipCode());

// Verify contactInfo map
Map<String, String> contactInfo = store.getContactInfo();
assertEquals(2, contactInfo.size());
assertEquals("123-456-7890", contactInfo.get("phone"));
assertEquals("contact@electronics.com", contactInfo.get("email"));
```

## Applying InnerSpec to the ArbitraryBuilder

To apply your pre-defined InnerSpec to the builder, use the `setInner()` method as shown below:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

InnerSpec innerSpec = new InnerSpec().property("id", 1000);

fixtureMonkey.giveMeBuilder(Product.class)
    .setInner(innerSpec);

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val innerSpec = InnerSpec().property("id", 1000)

fixtureMonkey.giveMeBuilder<Product>()
    .setInner(innerSpec)

{{< /tab >}}
{{< /tabpane>}}

## Customizing properties

### property()

Similar to the `set()` method in ArbitraryBuilder, you can customize a property by specifying its name and providing the desired value.

{{< alert icon="🚨" text="Fixture Monkey expressions such as refering elements (`[]`) or nested fields(`.`) are not allowed as the property name. Only the property name itself is allowed." />}}

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

InnerSpec innerSpec = new InnerSpec()
    .property("id", 1000);

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val innerSpec = InnerSpec()
    .property("id", 1000)

{{< /tab >}}
{{< /tabpane>}}

### size(), minSize(), maxSize()

`size()`, `minSize()`, and `maxSize()` can be used to specify the size of the property.

As previously mentioned, InnerSpec defines customizations in a nested manner.
You can first select the container property using `property()` and then proceed to define an innerSpec consumer to set the size.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

InnerSpec innerSpec = new InnerSpec()
    .property("options", options -> options.size(5)); // size:5

InnerSpec innerSpec = new InnerSpec()
    .property("options", options -> options.size(3, 5)); // minSize:3, maxSize:5

InnerSpec innerSpec = new InnerSpec()
    .property("options", options -> options.minSize(3)); // minSize:3

InnerSpec innerSpec = new InnerSpec()
    .property("options", options -> options.maxSize(5)); // maxSize:5

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val innerSpec = InnerSpec()
    .property("options") { it.size(5) } // size:5

val innerSpec = InnerSpec()
    .property("options") { it.size(3, 5) } // minSize:3, maxSize:5

val innerSpec = InnerSpec()
    .property("options") { it.minSize(3) } // minSize:3

val innerSpec = InnerSpec()
    .property("options") { it.maxSize(5) } // maxSize:5

{{< /tab >}}
{{< /tabpane>}}

### postCondition()

`postCondition()` can be used when you require your property to match a specific condition.

{{< alert icon="🚨" text="Using setPostCondition can incur higher costs for narrow conditions. In such cases, it's recommended to use set instead." />}}

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

InnerSpec innerSpec = new InnerSpec()
    .property("id", id -> id.postCondition(Long.class, it -> it > 0));

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val innerSpec = InnerSpec()
    .property("id") { it.postCondition(Long::class.java) { it > 0 }}

{{< /tab >}}
{{< /tabpane>}}

### inner()

You can also customize a property using another pre-defined InnerSpec with the help of `inner()`.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

InnerSpec innerSpec = new InnerSpec()
    .property("id", 1000L);

fixtureMonkey.giveMeBuilder(Product.class)
    .setInner(
        new InnerSpec()
            .property("nestedObject", nestedObject -> nestedObject.inner(innerSpec))
    );

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val innerSpec = InnerSpec()
    .property("id", 1000L)

fixtureMonkey.giveMeBuilder<Product>()
    .setInner(
        InnerSpec()
            .property("nestedObject") { it.inner(innerSpec) }
    )

{{< /tab >}}
{{< /tabpane>}}

## Customizing list properties

### listElement()

Individual elements within lists can be selected using `listElement()`.
This is equivalent to referencing elements with "[n]" using `expressions`.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

InnerSpec innerSpec = new InnerSpec()
    .property("options", options -> options.listElement(0, "red"));

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val innerSpec = InnerSpec()
    .property("options") { it.listElement(0, "red") }

{{< /tab >}}
{{< /tabpane>}}

### allListElement()

If you wish to set all elements of the list simultaneously, you can use `allListElement()`.
This is equivalent to referencing elements with "[*]" using `expressions`.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

InnerSpec innerSpec = new InnerSpec()
    .property("options", options -> options.allListElement("red"));

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val innerSpec = InnerSpec()
    .property("options") { it.allListElement("red") }

{{< /tab >}}
{{< /tabpane>}}

## Customizing map properties

InnerSpec provides special methods for customizing map property entries.

{{< alert icon="🚨" text="Similar to lists, setting a map entry without specifying the size first might lead to no change. Prior to setting a value, ensure that the map property has the intended size." />}}

### key(), value(), entry()

You can customize map property entries using `key()`, `value()`, and `entry()` methods.
Using `key()` assigns a specified value to the key of a map entry, while the entry's value remains randomized.
Similarly, `value()` assigns a specified value to the map entry's value, while the key becomes randomized.
If you want to specify both the key and value at once, you can use `entry()`.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

InnerSpec innerSpec = new InnerSpec()
    .property("merchantInfo", merchantInfo -> merchantInfo.key(1000));

InnerSpec innerSpec = new InnerSpec()
    .property("merchantInfo", merchantInfo -> merchantInfo.value("ABC Store"));

InnerSpec innerSpec = new InnerSpec()
    .property("merchantInfo", merchantInfo -> merchantInfo.entry(1000, "ABC Store"));

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val innerSpec = InnerSpec()
    .property("merchantInfo") { it.key(1000) }

val innerSpec = InnerSpec()
    .property("merchantInfo") { it.value("ABC Store") }

val innerSpec = InnerSpec()
    .property("merchantInfo") { it.entry(1000, "ABC Store") }

{{< /tab >}}
{{< /tabpane>}}

### keys(), values(), entries()

When setting multiple entries within a map, you can use `keys()`, `values()`, and `entries()` to pass multiple values.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

InnerSpec innerSpec = new InnerSpec()
    .property("merchantInfo", merchantInfo -> merchantInfo.keys(1000, 1001, 1002));

InnerSpec innerSpec = new InnerSpec()
    .property("merchantInfo", merchantInfo -> merchantInfo.values("ABC Store", "123 Convenience", "XYZ Mart"));

InnerSpec innerSpec = new InnerSpec()
    .property("merchantInfo", merchantInfo -> merchantInfo.entries(1000, "ABC Store", 1001, "123 Convenience", 1002, "XYZ Mart"));

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val innerSpec = InnerSpec()
    .property("merchantInfo") { it.keys(1000, 1001, 1002) }

val innerSpec = InnerSpec()
    .property("merchantInfo") { it.values("ABC Store", "123 Convenience", "XYZ Mart") }

val innerSpec = InnerSpec()
    .property("merchantInfo") { it.entries(1000, "ABC Store", 1001, "123 Convenience", 1002, "XYZ Mart") }

{{< /tab >}}
{{< /tabpane>}}

### allKey(), allValue(), allEntry()

Similar to `allListElement()`, it is possible to set every entry within the map to the specified value with `allKey()`, `allValue()`, and `allEntry()`.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

InnerSpec innerSpec = new InnerSpec()
    .property("merchantInfo", merchantInfo -> merchantInfo.allKey(1000));

InnerSpec innerSpec = new InnerSpec()
    .property("merchantInfo", merchantInfo -> merchantInfo.allValue("ABC Store"));

InnerSpec innerSpec = new InnerSpec()
    .property("merchantInfo", merchantInfo -> merchantInfo.allEntry(1000, "ABC Store"));

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val innerSpec = InnerSpec()
    .property("merchantInfo") { it.allKey(1000) }

val innerSpec = InnerSpec()
    .property("merchantInfo") { it.allValue("ABC Store") }

val innerSpec = InnerSpec()
    .property("merchantInfo") { it.allEntry(1000, "ABC Store") }

{{< /tab >}}
{{< /tabpane>}}

### keyLazy(), valueLazy(), entryLazy()

Similar to the `setLazy()` method in ArbitraryBuilder, you can pass a Supplier to assign the value.
The Supplier will run every time the ArbitraryBuilder with the `InnerSpec` applied is sampled.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

InnerSpec innerSpec = new InnerSpec()
    .property("merchantInfo", merchantInfo -> merchantInfo.keyLazy(this::generateMerchantKey));

InnerSpec innerSpec = new InnerSpec()
    .property("merchantInfo", merchantInfo -> merchantInfo.valueLazy(this::generateMerchantValue));

InnerSpec innerSpec = new InnerSpec()
    .property("merchantInfo", merchantInfo -> merchantInfo.entryLazy(this::generateMerchantKey, this::generateMerchantValue));

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val innerSpec = InnerSpec()
    .property("merchantInfo") { it.keyLazy(this::generateMerchantKey) }

val innerSpec = InnerSpec()
    .property("merchantInfo") { it.valueLazy(this::generateMerchantValue) }

val innerSpec = InnerSpec()
    .property("merchantInfo") { it.entryLazy(this::generateMerchantKey, this::generateMerchantValue) }

{{< /tab >}}
{{< /tabpane>}}

### allKeyLazy(), allValueLazy(), allEntryLazy()

Just as with the `allKey()` method, you can use `allKeyLazy()` to apply `keyLazy()` to every entry within the map.
Both `allValueLazy()` and `allEntryLazy()` function similarly.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

InnerSpec innerSpec = new InnerSpec()
    .property("merchantInfo", merchantInfo -> merchantInfo.allKeyLazy(this::generateMerchantKey));

InnerSpec innerSpec = new InnerSpec()
    .property("merchantInfo", merchantInfo -> merchantInfo.allValueLazy(this::generateMerchantValue));

InnerSpec innerSpec = new InnerSpec()
    .property("merchantInfo", merchantInfo -> merchantInfo.allEntryLazy(this::generateMerchantKey, this::generateMerchantValue));

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val innerSpec = InnerSpec()
    .property("merchantInfo") { it.allKeyLazy(this::generateMerchantKey) }

val innerSpec = InnerSpec()
    .property("merchantInfo") { it.allValueLazy(this::generateMerchantValue) }

val innerSpec = InnerSpec()
    .property("merchantInfo") { it.allEntryLazy(this::generateMerchantKey, this::generateMerchantValue) }

{{< /tab >}}
{{< /tabpane>}}

## Customizing nested Maps

By combining methods within InnerSpec, you can effectively customize maps with map-type keys, map-type values, or both.

Consider the scenario of a nested map structure like the following:

```java
public class Example {
    Map<Map<String, String>, String> mapByString;
    Map<String, Map<String, String>> stringByMap;
}
```

### Setting map-type key
To set a map with a map-type key, you can access the map key using `key()`, and then further customize it.

{{< tabpane persistLang=false >}}
{{< tab header="general expression" lang="java">}}

InnerSpec().property("mapByString", m -> m.key(k -> k.entry("key", "value")));

{{< /tab >}}
{{< tab header="Kotlin Exp" lang="kotlin">}}

InnerSpec().property("mapByString") { m -> m.key { k -> k.entry("key", "value") } }

{{< /tab >}}
{{< /tabpane>}}

If you need to set the entry itself, access the entry with `entry()` and further customize the key using InnerSpec, then set the specific value.

{{< tabpane persistLang=false >}}
{{< tab header="general expression" lang="java">}}

InnerSpec().property("mapByString", m -> m.entry(k -> k.entry("innerKey", "innerValue")), "value")

{{< /tab >}}
{{< tab header="Kotlin Exp" lang="kotlin">}}

InnerSpec().property("mapByString") { m -> m.entry({ k -> k.entry("innerKey", "innerValue") }, "value") }

{{< /tab >}}
{{< /tabpane>}}

### Setting map-type value
For a map with a map-type value, access the map value using `value()`, and then further customize it.

{{< tabpane persistLang=false >}}
{{< tab header="general expression" lang="java">}}

InnerSpec().property("stringByMap", m -> m.value(v -> v.entry("key", "value")))

{{< /tab >}}
{{< tab header="Kotlin Exp" lang="kotlin">}}

InnerSpec().property("stringByMap") { m -> m.value { v -> v.entry("key", "value") } }

{{< /tab >}}
{{< /tabpane>}}

If you need to set the entry itself, access the entry with `entry()` and further customize the value using InnerSpec, then set the specific key.

{{< tabpane persistLang=false >}}
{{< tab header="general expression" lang="java">}}

InnerSpec().property("stringByMap", m -> m.entry("key", v -> v.entry("innerKey", "innerValue")))

{{< /tab >}}
{{< tab header="Kotlin Exp" lang="kotlin">}}

InnerSpec().property("stringByMap") { m -> m.entry("key") {v -> v.entry("innerKey", "innerValue")} }

{{< /tab >}}
{{< /tabpane>}}

## Real-World Use Case: Testing an E-commerce System

Let's look at a practical example where InnerSpec shines - testing a method in an e-commerce system that calculates discounts based on a complex object structure.

### The Domain Model

```java
// Order with customer, items, and payment information
public class Order {
    private Long id;
    private Customer customer;
    private List<OrderItem> items;
    private Map<String, PaymentInfo> paymentOptions;
    private String selectedPaymentMethod;
    // getters and setters
}

public class Customer {
    private Long id;
    private String name;
    private CustomerType type; // REGULAR, PREMIUM, VIP
    private LocalDate memberSince;
    // getters and setters
}

public class OrderItem {
    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal pricePerUnit;
    // getters and setters
}

public class PaymentInfo {
    private PaymentType type;
    private BigDecimal processingFeePercent;
    private boolean supportsInstallments;
    // getters and setters
}

public enum CustomerType { REGULAR, PREMIUM, VIP }
public enum PaymentType { CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, DIGITAL_WALLET }
```

### The Service to Test

```java
public class DiscountService {
    /**
     * Calculates the discount percentage based on order details
     * - VIP customers get at least 10% discount
     * - Premium customers get 5% discount
     * - Orders with more than 5 items get additional 3% discount
     * - Orders over $500 get additional 5% discount
     * - Payment method can add 1-2% discount depending on type
     */
    public BigDecimal calculateDiscountPercentage(Order order) {
        // Implementation details...
    }
}
```

### Creating a Test with InnerSpec

```java
@Test
public void testVipCustomerWithLargeOrderGetsMaxDiscount() {
    // Create a Fixture Monkey instance
    FixtureMonkey fixtureMonkey = FixtureMonkey.create();
    
    // Create Customer InnerSpec
    InnerSpec customerSpec = new InnerSpec()
        .property("id", 500L)
        .property("name", "John Doe")
        .property("type", CustomerType.VIP)
        .property("memberSince", LocalDate.of(2020, 1, 1));
    
    // Create OrderItems InnerSpec (for multiple items)
    InnerSpec orderItemsSpec = new InnerSpec()
        .property("items", items -> items
            .size(6) // 6 items for additional discount
            .allListElement(item -> item
                .property("pricePerUnit", new BigDecimal("100.00"))
                .property("quantity", 1)
            )
        );
    
    // Create PaymentInfo InnerSpec
    InnerSpec paymentInfoSpec = new InnerSpec()
        .property("paymentOptions", options -> options
            .size(2)
            .entry("creditCard", creditCard -> creditCard
                .property("type", PaymentType.CREDIT_CARD)
                .property("processingFeePercent", new BigDecimal("2.5"))
                .property("supportsInstallments", true)
            )
            .entry("digitalWallet", digitalWallet -> digitalWallet
                .property("type", PaymentType.DIGITAL_WALLET)
                .property("processingFeePercent", new BigDecimal("1.0"))
                .property("supportsInstallments", false)
            )
        );
    
    // Combine all specs into the Order spec
    InnerSpec orderSpec = new InnerSpec()
        .property("id", 1000L)
        .property("customer", customer -> customer.inner(customerSpec))
        .inner(orderItemsSpec) // Merge the items spec
        .inner(paymentInfoSpec) // Merge the payment info spec
        .property("selectedPaymentMethod", "digitalWallet"); // Choose digital wallet for max discount
    
    // Create the Order using the combined spec
    Order order = fixtureMonkey.giveMeBuilder(Order.class)
        .setInner(orderSpec)
        .sample();
    
    // Test the discount service
    DiscountService discountService = new DiscountService();
    BigDecimal discount = discountService.calculateDiscountPercentage(order);
    
    // VIP (10%) + Items>5 (3%) + Order>$500 (5%) + Digital Wallet (2%) = 20%
    assertEquals(new BigDecimal("20.00"), discount);
}
```

This real-world example demonstrates how InnerSpec makes it easy to create complex test scenarios with deeply nested objects, lists, and maps - all in a reusable, structured manner.

## Useful Patterns and Techniques

### Two Ways to Handle Nested Objects

There are two valid approaches when working with nested objects in InnerSpec:

**Approach 1: Directly Passing an InnerSpec Object**

You can directly pass a created InnerSpec object to the property() method:

```java
// Approach 1: Directly passing an InnerSpec object
InnerSpec addressSpec = new InnerSpec()
    .property("street", "123 Main St")
    .property("zipCode", "12345");

// Directly pass the InnerSpec object as value to property() method
InnerSpec personSpec = new InnerSpec()
    .property("name", "John Doe")
    .property("address", addressSpec);  // Directly passing the InnerSpec object
```

This approach is concise and intuitive, making it suitable for simple nested structures.

**Approach 2: Using the inner() Method**

Alternatively, you can apply a nested InnerSpec using a lambda and the inner() method:

```java
// Approach 2: Using the inner() method
InnerSpec addressSpec = new InnerSpec()
    .property("street", "123 Main St")
    .property("zipCode", "12345");

// Use lambda and inner() method to define nested structure
InnerSpec personSpec = new InnerSpec()
    .property("name", "John Doe")
    .property("address", address -> address
        .inner(addressSpec)
        // Advantage of this approach: you can apply additional customization here
        .property("additionalField", "extra information")
    );
```

Both approaches work, but approach 2 is generally preferred because it offers more flexibility when dealing with complex nested objects, allowing for additional customization as shown in the example above.

**Tip for beginners:** Start with approach 1 for simplicity, and move to approach 2 when you need more complex nested structures or additional property settings.

## Common Mistakes and Solutions

Here are some common issues beginners face when using InnerSpec and how to solve them:

### 1. Not Setting Collection Size First

**Problem:** If you try to add elements to a list or map without setting its size first, your changes may not be applied.

```java
// Incorrect approach:
InnerSpec innerSpec = new InnerSpec()
    .property("options", options -> options.listElement(0, "red"));
// Result: options list might be empty or not the expected size

// For maps:
InnerSpec innerSpec = new InnerSpec()
    .property("merchantInfo", merchantInfo -> merchantInfo.entry(1000, "ABC Store"));
// Result: entry might not be added to the merchantInfo map
```

**Solution:** Always set the collection size before setting elements:

```java
// Correct approach:
InnerSpec innerSpec = new InnerSpec()
    .property("options", options -> options
        .size(1)  // Set size first - important!
        .listElement(0, "red")
    );

// For maps:
InnerSpec innerSpec = new InnerSpec()
    .property("merchantInfo", merchantInfo -> merchantInfo
        .size(1)  // Set size first - important!
        .entry(1000, "ABC Store")
    );
```

### 2. Map Key/Value Type Mismatch

**Problem:** You'll get errors if the types of keys or values you're trying to set don't match the actual map types.

```java
// When the map type is Map<Long, String>:
InnerSpec innerSpec = new InnerSpec()
    .property("merchantInfo", merchantInfo -> merchantInfo
        .size(1)
        .entry("key is a string", "ABC Store")  // Error: Key should be Long but using String
    );
```

**Solution:** Check and match the map's key and value types correctly:

```java
// Correct approach:
InnerSpec innerSpec = new InnerSpec()
    .property("merchantInfo", merchantInfo -> merchantInfo
        .size(1)
        .entry(1000L, "ABC Store")  // Using Long type for key
    );
```

### 3. Confusing Lambda Syntax in Kotlin

**Problem:** The nested lambda expression syntax in Kotlin can be confusing.

**Solution:** In Kotlin, use curly braces to define lambdas and indent code blocks for clarity:

```kotlin
// Clear Kotlin syntax:
val innerSpec = InnerSpec()
    .property("options") { it  // Lambda starts with curly brace
        .size(3)
        .listElement(0, "red")
        .listElement(1, "green")
        .listElement(2, "blue")
    }  // Lambda ends
```

When using nested lambdas, make them more readable with proper indentation and comments:

```kotlin
val spec = InnerSpec()
    .property("person") { person ->  // Outer lambda
        person.property("address") { address ->  // Nested lambda
            address
                .property("city", "New York")
                .property("zipCode", "10001")
        }
    }
```

Avoiding these common mistakes will help you customize complex objects more easily with InnerSpec.
