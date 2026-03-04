---
title: "Testing with Interfaces"
sidebar_position: 44
---


## What You'll Learn in This Document
- How to customize properties of interface instances
- How to choose between different interface customization approaches
- How to work with implementation-specific properties

:::tip
For a comprehensive guide on **how Fixture Monkey generates interface types** (InterfacePlugin configuration, generic interfaces, sealed interfaces, advanced resolution), see [Generating Interface Types](../generating-objects/generating-interface).

This document focuses on **customizing** interface instances once they are generated.
:::

## Quick Start: Customizing Interface Properties

When using Fixture Monkey, you can customize interface properties just like regular classes:

```java
// A simple interface with one method
public interface StringSupplier {
	String getValue(); // This method returns a string
}

// Create a Fixture Monkey instance
FixtureMonkey fixture = FixtureMonkey.create();

// Create and customize a StringSupplier
String result = fixture.giveMeBuilder(StringSupplier.class)
	.set("value", "Hello World") // Set the value property
    .sample()                    // Generate an instance
	.getValue();                 // Call the method

// result will be "Hello World"
```

Fixture Monkey automatically creates an anonymous implementation and sets the `value` property. You can set properties that will be returned by the getter methods:

```java
public interface StringProvider {
    String getValue();    // Method to get a string value
    int getNumber();      // Method to get an integer value
}

StringProvider provider = fixture.giveMeBuilder(StringProvider.class)
    .set("value", "Hello World")  // Set what getValue() will return
    .set("number", 42)            // Set what getNumber() will return
    .sample();

String value = provider.getValue();     // Returns "Hello World"
int number = provider.getNumber();      // Returns 42
```

## Customization Approaches

When working with interfaces, there are three approaches to customizing the generated instances:

| Approach | Description | Can Customize Properties? |
|----------|-------------|--------------------------|
| **Anonymous implementation** | Let Fixture Monkey create an anonymous class | Yes, via `set()` |
| **Values.just** | Use an existing implementation instance as-is | No |
| **interfaceImplements** | Register implementations, then customize freely | Yes, including implementation-specific properties |

### Approach 1: Anonymous Implementation (Simplest)

Let Fixture Monkey create an anonymous implementation and customize it with `set()`:

```java
// A product info interface
public interface ProductInfo {
    String getName();
    BigDecimal getPrice();
    String getCategory();
    boolean isAvailable();
    int getStockQuantity();
}

FixtureMonkey fixture = FixtureMonkey.create();

ProductInfo productInfo = fixture.giveMeBuilder(ProductInfo.class)
    .set("name", "Smartphone")
    .set("price", new BigDecimal("999.99"))
    .set("category", "Electronics")
    .set("available", true)
    .set("stockQuantity", 10)
    .sample();
```

This is the simplest approach. Fixture Monkey handles all the implementation details.

### Approach 2: Using Values.just

If you already have an implementation instance, use `Values.just` to use it as-is:

```java
public class OnlineProductInfo implements ProductInfo {
    private String name;
    private BigDecimal price;
    private String category;
    private boolean available;
    private int stockQuantity;

    public OnlineProductInfo(String name, BigDecimal price, String category, boolean available, int stockQuantity) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.available = available;
        this.stockQuantity = stockQuantity;
    }

    // Getters
    @Override public String getName() { return name; }
    @Override public BigDecimal getPrice() { return price; }
    @Override public String getCategory() { return category; }
    @Override public boolean isAvailable() { return available; }
    @Override public int getStockQuantity() { return stockQuantity; }
}

OnlineProductInfo originalProduct = new OnlineProductInfo(
    "Laptop", new BigDecimal("1999.99"), "Electronics", true, 5
);

ProductInfo productInfo = fixture.giveMeBuilder(ProductInfo.class)
    .set("$", Values.just(originalProduct))  // Use existing instance
    .sample();
```

:::danger
You **cannot** further customize properties after using `Values.just`:

```java
// This won't work - set("price", ...) will have no effect
ProductInfo product = fixture.giveMeBuilder(ProductInfo.class)
    .set("$", Values.just(originalProduct))
    .set("price", new BigDecimal("1499.99")) // No effect!
    .sample();
```
:::

### Approach 3: Using interfaceImplements (Most Flexible)

For scenarios where you need to customize implementation-specific properties, register implementations via `InterfacePlugin`:

```java
public class StoreProductInfo implements ProductInfo {
    private String name;
    private BigDecimal price;
    private String category;
    private boolean available;
    private int stockQuantity;
    private String storeLocation; // Implementation-specific property

    // Constructor, getters, setters
    public String getStoreLocation() { return storeLocation; }
    public void setStoreLocation(String storeLocation) { this.storeLocation = storeLocation; }
}

FixtureMonkey fixture = FixtureMonkey.builder()
	.plugin(
		new InterfacePlugin()
			.interfaceImplements(
                ProductInfo.class,
                List.of(OnlineProductInfo.class, StoreProductInfo.class)
			)
	)
	.build();

// Customize both interface and implementation-specific properties
StoreProductInfo product = (StoreProductInfo) fixture.giveMeBuilder(ProductInfo.class)
    .set("$", new StoreProductInfo())              // Specify implementation
    .set("name", "Coffee Maker")                   // Interface property
    .set("price", new BigDecimal("89.99"))         // Interface property
    .set("storeLocation", "America Mall")          // Implementation-specific property
	.sample();
```

For more details on configuring `InterfacePlugin` and `interfaceImplements`, see [Generating Interface Types](../generating-objects/generating-interface).

## Choosing the Right Approach

| Scenario | Recommended Approach |
|----------|---------------------|
| Quick test, don't care about implementation details | Anonymous implementation |
| Already have an instance, no further customization needed | Values.just |
| Need to customize implementation-specific properties | interfaceImplements |
| Need Fixture Monkey to randomly select among implementations | interfaceImplements |
| Reusing the same implementations across multiple tests | interfaceImplements |

## Common Issues and Solutions

### Problem: ClassCastException When Casting

```java
// May fail - default implementation might not be StoreProductInfo
StoreProductInfo product = (StoreProductInfo) fixture.giveMeBuilder(ProductInfo.class)
    .sample();
```

**Solution**: Always specify the implementation when casting:

```java
StoreProductInfo product = (StoreProductInfo) fixture.giveMeBuilder(ProductInfo.class)
    .set("$", new StoreProductInfo())
	.sample();
```

### Problem: Properties Not Being Set

**Solution**: Make sure you're using `interfaceImplements` if you need to customize properties on a specific implementation:

```java
FixtureMonkey fixture = FixtureMonkey.builder()
	.plugin(
		new InterfacePlugin()
			.interfaceImplements(
                ProductInfo.class,
                List.of(OnlineProductInfo.class, StoreProductInfo.class)
			)
	)
	.build();

StoreProductInfo product = (StoreProductInfo) fixture.giveMeBuilder(ProductInfo.class)
    .set("$", new StoreProductInfo())
    .set("name", "Wireless Earbuds")     // This will work
    .set("storeLocation", "City Center") // This will work
	.sample();
```

## Summary

- Customize interface properties with `set()` just like regular classes
- Use **anonymous implementations** for simple, quick tests
- Use **Values.just** when you have an existing instance (but can't customize further)
- Use **interfaceImplements** when you need to work with implementation-specific properties
- For interface generation strategies and advanced configuration, see [Generating Interface Types](../generating-objects/generating-interface)
