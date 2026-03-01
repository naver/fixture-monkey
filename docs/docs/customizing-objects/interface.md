---
title: "Testing with Interfaces"
sidebar_position: 44
---


## What You'll Learn in This Document
- How to generate test data for interfaces
- How to specify which implementation to use
- How to customize attributes of interface implementations
- How to choose between different interface testing approaches

## The Importance of Interface Testing

> *In this section, you'll learn why interfaces are crucial in software development and why testing them is valuable.*

Interfaces play a vital role in modern software development for several important reasons:

### Why Interfaces Matter in Real Applications

1. **Loose Coupling**: Interfaces allow components to establish contracts without needing to know each other's internal details. This reduces dependencies between parts of your application.

2. **Dependency Injection**: Interfaces make dependency injection easier, allowing implementations to be substituted with alternatives or mocks during testing.

3. **Flexibility and Extensibility**: New implementations can be added without changing the code that uses the interface. This follows the Open/Closed Principle (open for extension, closed for modification).

4. **Better Testability**: Interfaces make it possible to replace actual implementations with test doubles, making unit tests much more manageable.

### Real-World Example: ProductInfo Interface

Let's look at how interfaces are used in real applications through an example:

```java
// A product info interface with getters
public interface ProductInfo {
    String getName();         // Get the product name
    BigDecimal getPrice();    // Get the product price
    String getCategory();     // Get the product category
    boolean isAvailable();    // Check if product is in stock
    int getStockQuantity();   // Get the quantity in stock
}

// A service that uses the interface as method parameters
public class ProductService {
    // Check if a specific quantity can be purchased
    public boolean canPurchase(ProductInfo productInfo, int quantity) {
        return productInfo.isAvailable() && 
               productInfo.getStockQuantity() >= quantity;
    }
    
    // Calculate total price for a quantity
    public BigDecimal calculateTotal(ProductInfo productInfo, int quantity) {
        return productInfo.getPrice().multiply(new BigDecimal(quantity));
    }
    
    // Generate a product summary
    public String getProductSummary(ProductInfo productInfo) {
        return String.format("%s (%s) - $%s, Stock: %d", 
            productInfo.getName(), 
            productInfo.getCategory(), 
            productInfo.getPrice(), 
            productInfo.getStockQuantity());
    }
}
```

In this example, `ProductService` works with any implementation of `ProductInfo` - it doesn't need to know where the product data comes from.

### Why ProductInfo Should Be an Interface

In real applications, product information could come from multiple sources:

1. Online store products (OnlineProductInfo)
2. Physical store products (StoreProductInfo) 
3. Inventory management system products (InventoryProductInfo)
4. Promotional products (PromotionProductInfo)

Each source provides the same basic information (name, price, category) but might have additional information or behave differently. For example:
- Online products need shipping date information
- Store products need store location information
- Inventory products need warehouse location information

The ProductService needs to work with the basic product information regardless of where it comes from. Using an interface allows:

1. The service to be more flexible by not depending on specific implementations
2. New types of product information (e.g., international products) to be added without changing the service code
3. Easier testing since we can test without actual product databases

### Challenges When Testing Interfaces

Despite the benefits, interfaces present unique testing challenges:

1. Testing requires concrete implementations, but creating these can be time-consuming
2. Each implementation may need different setups and configurations
3. Test data must match the expected behavior of the interface contract
4. Implementation-specific properties may need customization

### How Fixture Monkey Helps

Fixture Monkey addresses these challenges by:

1. Automatically generating implementations for testing
2. Providing flexible ways to customize interface behavior
3. Supporting multiple approaches for different testing scenarios
4. Reducing boilerplate code needed for test implementations

## Basic Interface Testing Concepts

> *This section introduces the basic concepts of interface testing with Fixture Monkey, starting with simple examples.*

### Customizing Interface Properties

Let's start with a simple example. When using Fixture Monkey, you can customize interface properties just like regular classes:

```java
// A simple interface with one method
public interface StringSupplier {
	String getValue(); // This method returns a string
}

// Create a Fixture Monkey instance with InterfacePlugin
// NOTE: InterfacePlugin is required for all interface operations
FixtureMonkey fixture = FixtureMonkey.builder()
    .plugin(new InterfacePlugin())
    .build();

// Create and customize a StringSupplier
String result = fixture.giveMeBuilder(StringSupplier.class)
	.set("value", "Hello World") // Set the value property
    .sample()                    // Generate an instance
	.getValue();                 // Call the method

// result will be "Hello World"
```

In this example, Fixture Monkey automatically creates an implementation of the `StringSupplier` interface and sets the `value` property.

### Setting Properties on Interface Implementations

When working with interfaces in Fixture Monkey, you can set properties that will be returned by the implemented methods:

```java
// An example interface with multiple methods
public interface StringProvider {
    String getValue();    // Method to get a string value
    int getNumber();      // Method to get an integer value
}

// Create a Fixture Monkey instance with InterfacePlugin
FixtureMonkey fixture = FixtureMonkey.builder()
    .plugin(new InterfacePlugin())
    .build();

// Create an interface implementation with specified property values
StringProvider provider = fixture.giveMeBuilder(StringProvider.class)
    .set("value", "Hello World")  // Set what getValue() will return
    .set("number", 42)            // Set what getNumber() will return
    .sample();

// Use the implementation
String value = provider.getValue();     // Returns "Hello World"
int number = provider.getNumber();      // Returns 42
```

## Interface Implementation Approaches

> *This section will teach you three different approaches to creating and working with interface implementations, from simplest to most advanced.*

When working with interfaces that have multiple implementations, there are three main approaches, starting with the simplest:

### Approach 1: Using Anonymous Implementations (Simplest)

For simple testing scenarios, Fixture Monkey can automatically create anonymous implementations of your interfaces. This approach is useful when:
- You need quick implementations for testing without creating actual classes
- You want to focus on test values rather than implementation details
- You need different values for each test

Here's how to use automatic anonymous implementations with Fixture Monkey:

```java
// A product info interface
public interface ProductInfo {
    String getName();             // Method to get the product name
    BigDecimal getPrice();        // Method to get the product price
    String getCategory();         // Method to get the product category
    boolean isAvailable();        // Method to check if product is in stock
    int getStockQuantity();       // Method to get the quantity in stock
}

// Create a Fixture Monkey instance with InterfacePlugin
// NOTE: InterfacePlugin is always required when working with interfaces
FixtureMonkey fixture = FixtureMonkey.builder()
    .plugin(new InterfacePlugin())
    .build();

// Let Fixture Monkey create an anonymous implementation
ProductInfo productInfo = fixture.giveMeBuilder(ProductInfo.class)
    .set("name", "Smartphone")                 // Set product name
    .set("price", new BigDecimal("999.99"))    // Set product price
    .set("category", "Electronics")            // Set product category
    .set("available", true)                    // Set as available in stock
    .set("stockQuantity", 10)                  // Set stock quantity to 10
    .sample();

// Use in tests
ProductService service = new ProductService();
boolean canPurchase = service.canPurchase(productInfo, 5); // Returns true
String summary = service.getProductSummary(productInfo); 
// Returns "Smartphone (Electronics) - $999.99, Stock: 10"
```

The main advantage of this approach is that Fixture Monkey handles all the implementation details. You just define what values the interface methods should return, and Fixture Monkey internally creates an appropriate anonymous implementation.

### Approach 2: Using Values.just

If you already have an implementation instance you want to use, you can simply use `Values.just`. This is useful when you have a specific implementation ready to use.

```java
// An online store implementation
public class OnlineProductInfo implements ProductInfo {
    private String name;            // Product name
    private BigDecimal price;       // Product price
    private String category;        // Product category 
    private boolean available;      // Product availability
    private int stockQuantity;      // Stock quantity
    
    // Constructor
    public OnlineProductInfo(String name, BigDecimal price, String category, boolean available, int stockQuantity) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.available = available;
        this.stockQuantity = stockQuantity;
    }
    
    // Getters
    @Override
    public String getName() { return name; }
    
    @Override
    public BigDecimal getPrice() { return price; }
    
    @Override
    public String getCategory() { return category; }
    
    @Override
    public boolean isAvailable() { return available; }
    
    @Override
    public int getStockQuantity() { return stockQuantity; }
}

// Create a Fixture Monkey instance with InterfacePlugin
FixtureMonkey fixture = FixtureMonkey.builder()
    .plugin(new InterfacePlugin())
    .build();

// Create a product instance
OnlineProductInfo originalProduct = new OnlineProductInfo(
    "Laptop", 
    new BigDecimal("1999.99"), 
    "Electronics", 
    true, 
    5
);

// Use Values.just to use this instance in tests
ProductInfo productInfo = fixture.giveMeBuilder(ProductInfo.class)
    .set("$", Values.just(originalProduct))  // Use existing instance
    .sample();

// Use in tests
ProductService service = new ProductService();
BigDecimal total = service.calculateTotal(productInfo, 2); // Returns 3999.98
```

The main benefit of this approach is simplicity - no additional configuration is needed. However, there's also a limitation - you **cannot** further customize the implementation properties:

```java
// This won't work - you can't modify properties in the Values.just approach
OnlineProductInfo product = (OnlineProductInfo)fixture.giveMeBuilder(ProductInfo.class)
    .set("$", Values.just(originalProduct))
    .set("price", new BigDecimal("1499.99")) // This will have no effect
    .sample();
```

### Approach 3: Using the interfaceImplements Option (Most Flexible)

For more complex scenarios where you need property customization, you can use the `interfaceImplements` option. This approach tells Fixture Monkey about all possible implementations of your interface so it can select and customize the right one.

#### Step 1: Configure Fixture Monkey with Implementations

```java
// A product info interface
public interface ProductInfo {
    String getName();
    BigDecimal getPrice();
    String getCategory();
    boolean isAvailable();
    int getStockQuantity();
}

// An online store implementation
public class OnlineProductInfo implements ProductInfo {
    private String name;
    private BigDecimal price;
    private String category;
    private boolean available;
    private int stockQuantity;
    
    // Constructor, getters, setters
}

// A physical store implementation with additional properties
public class StoreProductInfo implements ProductInfo {
    private String name;
    private BigDecimal price;
    private String category;
    private boolean available;
    private int stockQuantity;
    private String storeLocation; // Additional property
    
    // Constructor, getters, setters
    
    public String getStoreLocation() {
        return storeLocation;
    }
    
    public void setStoreLocation(String storeLocation) {
        this.storeLocation = storeLocation;
    }
}

// Configure Fixture Monkey with implementations
FixtureMonkey fixture = FixtureMonkey.builder()
	.plugin(
		new InterfacePlugin()
			.interfaceImplements(
                ProductInfo.class,                                  // The interface
                List.of(OnlineProductInfo.class, StoreProductInfo.class)  // The implementations
			)
	)
	.build();
```

#### Step 2: Create Specific Implementations

```java
// Create a StoreProductInfo instance
StoreProductInfo storeProduct = (StoreProductInfo)fixture.giveMeBuilder(ProductInfo.class)
    .set("$", new StoreProductInfo())  // Specify which implementation to use
	.sample();

// Now you can access implementation-specific properties
storeProduct.setStoreLocation("Downtown"); // Set implementation-specific property
String location = storeProduct.getStoreLocation(); // "Downtown"
```

#### Step 3: Customize Implementation Properties

With the interfaceImplements option, you can also modify the properties of the implementation:

```java
// Create and customize a StoreProductInfo
StoreProductInfo product = (StoreProductInfo)fixture.giveMeBuilder(ProductInfo.class)
    .set("$", new StoreProductInfo())              // Use StoreProductInfo implementation
    .set("name", "Coffee Maker")                   // Set product name
    .set("price", new BigDecimal("89.99"))         // Set product price
    .set("category", "Kitchen Appliances")         // Set product category
    .set("available", true)                        // Set as available
    .set("stockQuantity", 15)                      // Set stock quantity
    .set("storeLocation", "America Mall")          // Set implementation-specific property
	.sample();

// Use in tests
ProductService service = new ProductService();
String summary = service.getProductSummary(product);
// Returns "Coffee Maker (Kitchen Appliances) - $89.99, Stock: 15"

// Implementation-specific property is set
assertEquals("America Mall", product.getStoreLocation());
```

## Choosing the Right Approach

> *This guide will help you decide which approach to use based on your testing requirements.*

Here's a simple guide to help you choose the right approach:

### When to Use Anonymous Implementations:
- When you need quick, one-off implementations for testing
- When you don't want to create whole classes just for testing
- When you need custom behavior specific to a single test
- When you're just getting started with interface testing

### When to Use Values.just:
- When you need a quick solution without additional configuration
- When you'll only use the implementation once or twice
- When you already have an instance of the implementation
- When you don't need to modify properties after creation

### When to Use interfaceImplements:
- When you need to customize implementation properties
- When you'll use the same set of implementations across multiple tests
- When you want Fixture Monkey to randomly select among implementations
- When building more complex testing scenarios

## Real-World Example: Testing Product Service

> *This section provides a complete example of testing a service that uses interfaces.*

Here's how to use interfaces in a real testing scenario:

```java
@Test
void testProductService() {
	// Configure Fixture Monkey
	FixtureMonkey fixture = FixtureMonkey.builder()
		.plugin(
			new InterfacePlugin()
				.interfaceImplements(
                    ProductInfo.class,
                    List.of(OnlineProductInfo.class, StoreProductInfo.class)
				)
		)
		.build();
	
    // Create a product with specific properties
    ProductInfo product = fixture.giveMeBuilder(ProductInfo.class)
        .set("name", "Bluetooth Speaker")         // Set product name
        .set("price", new BigDecimal("79.99"))    // Set product price
        .set("category", "Audio")                 // Set product category
        .set("available", true)                   // Set as available
        .set("stockQuantity", 8)                  // Set stock quantity
		.sample();
	
    // Test the product service
    ProductService service = new ProductService();
    
    // Test various methods
    boolean canPurchase = service.canPurchase(product, 3);
    BigDecimal total = service.calculateTotal(product, 3);
    String summary = service.getProductSummary(product);
    
    // Verify results
    assertTrue(canPurchase);
    assertEquals(new BigDecimal("239.97"), total);
    assertEquals("Bluetooth Speaker (Audio) - $79.99, Stock: 8", summary);
}
```

## Common Issues and Solutions

> *This section addresses frequently encountered problems when testing interfaces.*

### Problem: ClassCastException When Accessing Implementation Properties

```java
// ClassCastException will occur if the wrong type is used
StoreProductInfo product = (StoreProductInfo)fixture.giveMeBuilder(ProductInfo.class)
    .sample(); // Default implementation might not be StoreProductInfo
```

**Solution**: Always specify the implementation when casting is needed:

```java
// Safe approach - explicitly specify implementation type
StoreProductInfo product = (StoreProductInfo)fixture.giveMeBuilder(ProductInfo.class)
    .set("$", new StoreProductInfo())  // Explicitly set implementation type
	.sample();
```

### Problem: Properties Not Being Set

**Solution**: Make sure you're using the interfaceImplements option if you need to customize properties:

```java
// First configure with interfaceImplements
FixtureMonkey fixture = FixtureMonkey.builder()
	.plugin(
		new InterfacePlugin()
			.interfaceImplements(
                ProductInfo.class,
                List.of(OnlineProductInfo.class, StoreProductInfo.class)
			)
	)
	.build();

// Then you can customize properties
StoreProductInfo product = (StoreProductInfo)fixture.giveMeBuilder(ProductInfo.class)
    .set("$", new StoreProductInfo())
    .set("name", "Wireless Earbuds") // This will work
    .set("storeLocation", "City Center") // This will work
	.sample();
```

## Summary

- Interface testing is common in real applications that use dependency injection and interfaces
- Start with simple anonymous implementations for quick tests
- Use Values.just when you have a specific implementation ready to use
- Use interfaceImplements when you need to customize implementation properties
- Always specify the implementation when you need to access implementation-specific properties


