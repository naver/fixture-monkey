---
title: "Instantiate Methods"
images: []
menu:
docs:
parent: "generating-objects"
identifier: "instantiate-methods"
weight: 33
---

## Overview: Why Specify Object Creation Methods

By default, Fixture Monkey automatically determines how to create objects through Introspectors. However, sometimes you may need to specify a particular creation method for reasons such as:

- **Specific constructor usage**: When a class has multiple constructors and you want to choose a specific one
- **Factory method utilization**: When you want to create objects using factory methods instead of constructors
- **Different initialization per test**: When you need different initialization methods for the same class in different tests
- **Special initialization logic**: When you need special initialization that can't be handled automatically by introspectors

In these situations, the `instantiate()` method allows you to precisely control how objects are created.

{{< alert icon="💡" text="This document explains how to specify object creation methods on a per-test basis. If you want to apply the same method for all tests, refer to the Introspector page." />}}

## Getting Started: Basic Usage

The most basic way to create objects with Fixture Monkey is:

```java
// Basic approach - let the introspector automatically determine how to create objects
Product product = fixtureMonkey.giveMeOne(Product.class);
```

However, if you want to use a specific constructor or factory method, you can use the `instantiate()` method:

```java
// Specify a constructor
Product product = fixtureMonkey.giveMeBuilder(Product.class)
    .instantiate(constructor())
    .sample();

// Specify a factory method
Product product = fixtureMonkey.giveMeBuilder(Product.class)
    .instantiate(factoryMethod("create"))
    .sample();
```

{{< alert icon="⭐" text="Beginner tip: In most cases, the basic approach (giveMeOne) is sufficient. Only use the instantiate() method when you have special initialization requirements." />}}

## Basic Concepts

### What is ArbitraryBuilder?

`ArbitraryBuilder` is a builder class for configuring object creation settings. It's returned when you call the `giveMeBuilder()` method in Fixture Monkey.

```java
// Get an ArbitraryBuilder
ArbitraryBuilder<Product> builder = fixtureMonkey.giveMeBuilder(Product.class);
```

### What is the instantiate() method?

The `instantiate()` method allows you to specify how the `ArbitraryBuilder` should create objects. You can choose between constructors and factory methods:

📌 **Method format:**

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// Specifying a constructor in Java
.instantiate(constructor())

// Specifying a factory method in Java
.instantiate(factoryMethod("methodName"))
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// Specifying a constructor in Kotlin (Kotlin Plugin required)
.instantiateBy {
    constructor()
}

// Specifying a factory method in Kotlin
.instantiateBy {
    factory("methodName")
}
{{< /tab >}}
{{< /tabpane>}}

## 1. Using Simple Constructors

Let's start with the most basic usage. Here's a simple class example:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
public class SimpleProduct {
    private final String name;
    private final int price;
    
    // Constructor
    public SimpleProduct(String name, int price) {
        this.name = name;
        this.price = price;
    }
    
    // Getter methods
    public String getName() { return name; }
    public int getPrice() { return price; }
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
class SimpleProduct(
    val name: String,
    val price: Int
)
{{< /tab >}}
{{< /tabpane>}}

Using the constructor to create an object:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void usingSimpleConstructor() {
    SimpleProduct product = fixtureMonkey.giveMeBuilder(SimpleProduct.class)
        .instantiate(constructor())
        .sample();
    
    // Verify the created object
    assertThat(product).isNotNull();
    assertThat(product.getName()).isNotNull();
    assertThat(product.getPrice()).isNotNegative();
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun usingSimpleConstructor() {
    val product = fixtureMonkey.giveMeBuilder<SimpleProduct>()
        .instantiateBy {
            constructor()
        }
        .sample()
    
    // Verify the created object
    assertThat(product).isNotNull()
    assertThat(product.name).isNotNull()
    assertThat(product.price).isNotNegative()
}
{{< /tab >}}
{{< /tabpane>}}

In this example, `constructor()` specifies that the constructor of SimpleProduct should be used. Fixture Monkey automatically generates appropriate values and passes them to the constructor.

## 2. Choosing Between Multiple Constructors

Now let's look at a class with multiple constructors:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
public class Product {
    private final long id;
    private final String name;
    private final long price;
    private final List<String> options;
    
    // Default constructor (all fields with default values)
    public Product() {
        this.id = 0;
        this.name = "defaultProduct";
        this.price = 0;
        this.options = null;
    }
    
    // Simple product constructor without options
    public Product(String name, long price) {
        this.id = new Random().nextLong();
        this.name = name;
        this.price = price;
        this.options = Collections.emptyList();
    }
    
    // Product constructor with options
    public Product(String name, long price, List<String> options) {
        this.id = new Random().nextLong();
        this.name = name;
        this.price = price;
        this.options = options;
    }
    
    // Getter methods
    public long getId() { return id; }
    public String getName() { return name; }
    public long getPrice() { return price; }
    public List<String> getOptions() { return options; }
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
class Product {
    val id: Long
    val name: String
    val price: Long
    val options: List<String>
    
    // Default constructor (all fields with default values)
    constructor() {
        this.id = 0
        this.name = "defaultProduct"
        this.price = 0
        this.options = emptyList()
    }
    
    // Simple product constructor without options
    constructor(name: String, price: Long) {
        this.id = Random().nextLong()
        this.name = name
        this.price = price
        this.options = emptyList()
    }
    
    // Product constructor with options
    constructor(name: String, price: Long, options: List<String>) {
        this.id = Random().nextLong()
        this.name = name
        this.price = price
        this.options = options
    }
}
{{< /tab >}}
{{< /tabpane>}}

### 2.1 Using the Default Constructor

To use the default constructor:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void usingDefaultConstructor() {
    Product product = fixtureMonkey.giveMeBuilder(Product.class)
        .instantiate(constructor())  // No parameters means default constructor
        .sample();
    
    assertThat(product.getId()).isEqualTo(0);
    assertThat(product.getName()).isEqualTo("defaultProduct");
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun usingDefaultConstructor() {
    val product = fixtureMonkey.giveMeBuilder<Product>()
        .instantiateBy {
            constructor()  // No parameters means default constructor
        }
        .sample()
    
    assertThat(product.id).isEqualTo(0)
    assertThat(product.name).isEqualTo("defaultProduct")
}
{{< /tab >}}
{{< /tabpane>}}

When you specify `constructor()` without parameters, Fixture Monkey uses the no-args constructor (default constructor).

### 2.2 Selecting a Specific Constructor

When a class has multiple constructors, you can specify parameter types to select the desired constructor:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void selectingConstructorWithoutOptions() {
    Product product = fixtureMonkey.giveMeBuilder(Product.class)
        .instantiate(
            constructor()
                .parameter(String.class)  // First parameter type
                .parameter(long.class)    // Second parameter type
        )
        .sample();
    
    assertThat(product.getOptions()).isEmpty();
}

@Test
void selectingConstructorWithOptions() {
    Product product = fixtureMonkey.giveMeBuilder(Product.class)
        .instantiate(
            constructor()
                .parameter(String.class)
                .parameter(long.class)
                .parameter(new TypeReference<List<String>>(){})  // Generic type
        )
        .sample();
    
    assertThat(product.getOptions()).isNotNull();
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun selectingConstructorWithoutOptions() {
    val product = fixtureMonkey.giveMeBuilder<Product>()
        .instantiateBy {
            constructor<Product> {
                parameter<String>()  // First parameter type
                parameter<Long>()    // Second parameter type
            }
        }
        .sample()
    
    assertThat(product.options).isEmpty()
}

@Test
fun selectingConstructorWithOptions() {
    val product = fixtureMonkey.giveMeBuilder<Product>()
        .instantiateBy {
            constructor<Product> {
                parameter<String>()
                parameter<Long>()
                parameter<List<String>>()  // Generic type
            }
        }
        .sample()
    
    assertThat(product.options).isNotNull()
}
{{< /tab >}}
{{< /tabpane>}}

> **Term Explanation**: The `parameter()` method specifies the parameter types to select the desired constructor.

### 2.3 Specifying Constructor Parameter Values

To provide specific values for constructor parameters, you can use parameter name hints:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void specifyingParameterValues() {
    Product product = fixtureMonkey.giveMeBuilder(Product.class)
        .instantiate(
            constructor()
                .parameter(String.class, "productName")  // Parameter name hint
                .parameter(long.class)
        )
        .set("productName", "specialProduct")  // Set value using the hint name
        .sample();
    
    assertThat(product.getName()).isEqualTo("specialProduct");
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun specifyingParameterValues() {
    val product = fixtureMonkey.giveMeBuilder<Product>()
        .instantiateBy {
            constructor<Product> {
                parameter<String>("productName")  // Parameter name hint
                parameter<Long>()
            }
        }
        .set("productName", "specialProduct")  // Set value using the hint name
        .sample()
    
    assertThat(product.name).isEqualTo("specialProduct")
}
{{< /tab >}}
{{< /tabpane>}}

> **Term Explanation**: A parameter name hint assigns an alias to a constructor parameter, allowing you to set values for it later using this name.

## 3. Using Factory Methods

Besides constructors, you can create objects using factory methods. Let's look at a class with factory methods:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
public class Product {
    // Fields and constructors defined earlier...
    
    // Factory method
    public static Product create(String name, long price) {
        return new Product(name, price);
    }
    
    // Recommended product factory method
    public static Product createRecommended(long price) {
        return new Product("recommendedProduct", price);
    }
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
class Product {
    // Fields and constructors defined earlier...
    
    companion object {
        // Factory method
        fun create(name: String, price: Long): Product {
            return Product(name, price)
        }
        
        // Recommended product factory method
        fun createRecommended(price: Long): Product {
            return Product("recommendedProduct", price)
        }
    }
}
{{< /tab >}}
{{< /tabpane>}}

### 3.1 Basic Factory Method Usage

To create an object using a factory method:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void usingFactoryMethod() {
    Product product = fixtureMonkey.giveMeBuilder(Product.class)
        .instantiate(
            factoryMethod("create")  // Specify factory method name
        )
        .sample();
    
    assertThat(product).isNotNull();
    assertThat(product.getOptions()).isEmpty();
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun usingFactoryMethod() {
    val product = fixtureMonkey.giveMeBuilder<Product>()
        .instantiateBy {
            factory<Product>("create")  // Specify factory method name
        }
        .sample()
    
    assertThat(product).isNotNull()
    assertThat(product.options).isEmpty()
}
{{< /tab >}}
{{< /tabpane>}}

> **Term Explanation**: A factory method is a static method responsible for object creation, used instead of directly calling constructors.

### 3.2 Selecting a Specific Factory Method

When there are multiple factory methods, you can specify parameter types to select the desired method:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void selectingSpecificFactoryMethod() {
    Product product = fixtureMonkey.giveMeBuilder(Product.class)
        .instantiate(
            factoryMethod("createRecommended")
                .parameter(long.class)  // Parameter type
        )
        .sample();
    
    assertThat(product.getName()).isEqualTo("recommendedProduct");
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun selectingSpecificFactoryMethod() {
    val product = fixtureMonkey.giveMeBuilder<Product>()
        .instantiateBy {
            factory<Product>("createRecommended") {
                parameter<Long>()  // Parameter type
            }
        }
        .sample()
    
    assertThat(product.name).isEqualTo("recommendedProduct")
}
{{< /tab >}}
{{< /tabpane>}}

### 3.3 Specifying Factory Method Parameter Values

To provide specific values for factory method parameters:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void specifyingFactoryMethodParameterValues() {
    Product product = fixtureMonkey.giveMeBuilder(Product.class)
        .instantiate(
            factoryMethod("create")
                .parameter(String.class, "productName")  // Parameter name hint
                .parameter(long.class, "productPrice")
        )
        .set("productName", "customProduct")
        .set("productPrice", 9900L)
        .sample();
    
    assertThat(product.getName()).isEqualTo("customProduct");
    assertThat(product.getPrice()).isEqualTo(9900L);
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun specifyingFactoryMethodParameterValues() {
    val product = fixtureMonkey.giveMeBuilder<Product>()
        .instantiateBy {
            factory<Product>("create") {
                parameter<String>("productName")  // Parameter name hint
                parameter<Long>("productPrice")
            }
        }
        .set("productName", "customProduct")
        .set("productPrice", 9900L)
        .sample()
    
    assertThat(product.name).isEqualTo("customProduct")
    assertThat(product.price).isEqualTo(9900L)
}
{{< /tab >}}
{{< /tabpane>}}

## 4. Advanced Features and Important Notes

Here are some advanced features and important notes to be aware of when creating objects.

### 4.1 Choosing Between Field and JavaBeansProperty

You can control how property values are set during object creation. There are two main approaches:

{{< alert icon="📘" text="These options determine how properties not initialized by the constructor or factory method will be set after the object is created." />}}

1. **field()**: Generate properties based on class fields
   - Pros: Direct field access, works without setters
   - Cons: Bypasses encapsulation, ignores validation logic

2. **javaBeansProperty()**: Generate properties based on getter/setter methods
   - Pros: Respects encapsulation, uses validation logic in setters
   - Cons: Requires setter methods to set properties

📋 **Quick selection guide**:
- If setter methods have validation logic and you want to test it: **javaBeansProperty()**
- If there are no setter methods or you want to bypass validation: **field()**

#### 4.1.1 Field-Based Property Generation

To generate properties based on fields:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void fieldBasedPropertyGeneration() {
    Product product = fixtureMonkey.giveMeBuilder(Product.class)
        .instantiate(
            constructor().field()  // Field-based property generation
        )
        .sample();
    
    assertThat(product).isNotNull();
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun fieldBasedPropertyGeneration() {
    val product = fixtureMonkey.giveMeBuilder<Product>()
        .instantiateBy {
            constructor {
                javaField()  // Field-based property generation
            }
        }
        .sample()
    
    assertThat(product).isNotNull()
}
{{< /tab >}}
{{< /tabpane>}}

> **Term Explanation**: Fields are variables defined in a class that store the object's state. Field-based property generation uses these fields directly to set values.

#### 4.1.2 JavaBeansProperty-Based Property Generation

To generate properties based on JavaBeansProperty:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void javaBeanPropertyBasedGeneration() {
    Product product = fixtureMonkey.giveMeBuilder(Product.class)
        .instantiate(
            constructor().javaBeansProperty()  // JavaBeansProperty-based generation
        )
        .sample();
    
    assertThat(product).isNotNull();
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun javaBeanPropertyBasedGeneration() {
    val product = fixtureMonkey.giveMeBuilder<Product>()
        .instantiateBy {
            constructor {
                javaBeansProperty()  // JavaBeansProperty-based generation
            }
        }
        .sample()
    
    assertThat(product).isNotNull()
}
{{< /tab >}}
{{< /tabpane>}}

> **Term Explanation**: JavaBeansProperty refers to properties represented by getter/setter method pairs. For example, the getName()/setName() method pair represents the 'name' property.

### 4.2 Property Setting After Constructor Invocation

{{< alert icon="⚠️" text="This section explains important cautions when Fixture Monkey sets property values after object creation." />}}

When you specify a constructor using the `instantiate()` method, Fixture Monkey sets random values for properties not handled by the constructor after object creation. This feature is useful when you want to generate test data for fields that are not initialized in the constructor.

#### How it works at a glance:

1. Specify constructor: `instantiate(constructor()...)`
2. Create object using constructor
3. Set random values for properties not initialized by constructor
4. Return complete object

Let's see an example:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
public class PartiallyInitializedObject {
    private final String name;      // Initialized in constructor
    private int count;              // Not initialized in constructor
    private List<String> items;     // Not initialized in constructor
    
    public PartiallyInitializedObject(String name) {
        this.name = name;
    }
    
    // Getter/Setter
    public String getName() { return name; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
    public List<String> getItems() { return items; }
    public void setItems(List<String> items) { this.items = items; }
}

@Test
void propertySettingAfterConstructor() {
    PartiallyInitializedObject obj = fixtureMonkey.giveMeBuilder(PartiallyInitializedObject.class)
        .instantiate(constructor().parameter(String.class))
        .sample();
    
    assertThat(obj.getName()).isNotNull();       // Initialized in constructor
    assertThat(obj.getCount()).isNotZero();      // Initialized after constructor
    assertThat(obj.getItems()).isNotNull();      // Initialized after constructor
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
class PartiallyInitializedObject(
    val name: String               // Initialized in constructor
) {
    var count: Int = 0             // Not initialized in constructor
    var items: List<String>? = null // Not initialized in constructor
}

@Test
fun propertySettingAfterConstructor() {
    val obj = fixtureMonkey.giveMeBuilder<PartiallyInitializedObject>()
        .instantiateBy {
            constructor<PartiallyInitializedObject> {
                parameter<String>()
            }
        }
        .sample()
    
    assertThat(obj.name).isNotNull()       // Initialized in constructor
    assertThat(obj.count).isNotZero()      // Initialized after constructor
    assertThat(obj.items).isNotNull()      // Initialized after constructor
}
{{< /tab >}}
{{< /tabpane>}}

#### 4.2.1 Caution

There's one important caution when using this feature:

{{< alert icon="⚠️" text="Fixture Monkey may also **modify property values that were already set in the constructor**. This could lead to unexpected test results." />}}

**Problem scenario:**
1. Set `name = "specificName"` in constructor
2. Fixture Monkey automatically assigns random value to `name` after object creation
3. `name` changes from "specificName" to some other value

**Solution:**
You can solve this problem by explicitly setting important values:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void preservingConstructorSetValues() {
    String specificName = "specificName";
    
    PartiallyInitializedObject obj = fixtureMonkey.giveMeBuilder(PartiallyInitializedObject.class)
        .instantiate(
            constructor()
                .parameter(String.class, "name")
        )
        .set("name", specificName)  // Explicitly set constructor parameter value
        .sample();
    
    assertThat(obj.getName()).isEqualTo(specificName);  // Explicitly set value is preserved
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun preservingConstructorSetValues() {
    val specificName = "specificName"
    
    val obj = fixtureMonkey.giveMeBuilder<PartiallyInitializedObject>()
        .instantiateBy {
            constructor<PartiallyInitializedObject> {
                parameter<String>("name")
            }
        }
        .set("name", specificName)  // Explicitly set constructor parameter value
        .sample()
    
    assertThat(obj.name).isEqualTo(specificName)  // Explicitly set value is preserved
}
{{< /tab >}}
{{< /tabpane>}}

{{< alert icon="💡" text="Beginner tip: Always explicitly set important values using the `.set()` method to ensure predictable test results!" />}}

## Frequently Asked Questions (FAQ)

### Q: What's the difference between instantiate and introspectors?

**A**: Introspectors are global settings applied to all object creation, while instantiate is a local setting applied only to specific tests or objects.

**Simply put:**
- **Introspector**: "Create all objects this way for all tests"
- **instantiate**: "Create objects this way only for this specific test"

In most cases, introspectors are sufficient, but use instantiate when you need special creation logic.

### Q: How do I decide which constructor to choose among multiple options?

**A**: Choose the constructor that best fits your test purpose. Generally:

- **Simple tests**: Use constructors with fewer arguments
- **Testing specific fields**: Choose constructors that initialize the fields you're focusing on
- **Testing validation logic**: Use constructors with validation or special initialization logic

### Q: What are the benefits of parameter name hints?

**A**: Parameter name hints allow you to:
- Assign meaningful names to constructor or factory method parameters
- Easily set specific parameter values using the set() method
- Improve code readability

```java
// Without parameter name hints
.instantiate(constructor().parameter(String.class))
.set("__ANONYMOUS_0", "value")  // Hard to understand which parameter this is

// With parameter name hints
.instantiate(constructor().parameter(String.class, "name"))
.set("name", "John")  // Clearly indicates which parameter
```

### Q: Which should I use: field() or javaBeansProperty()?

**A**:
- **field()**: Suitable for classes where fields need to be accessed directly or setter methods aren't available
- **javaBeansProperty()**: Suitable for classes where setter methods include validation or special processing
- If unsure, use the default (don't specify). Fixture Monkey will choose an appropriate method.

### Q: How do I specify generic type parameters?

**A**: Generic types are specified using TypeReference:

```java
// Java
.parameter(new TypeReference<List<String>>(){})
```

```kotlin
// Kotlin
parameter<List<String>>()
```

### Q: How do I prevent values set in the constructor from being changed?

**A**: Use the `.set()` method to explicitly set important values:

```java
fixtureMonkey.giveMeBuilder(MyClass.class)
    .instantiate(constructor().parameter(String.class, "name"))
    .set("name", "importantValue")  // This value won't be changed
    .sample();
```

## Summary

{{< alert icon="📌" text="Key Points Summary" />}}

- The **instantiate() method** provides fine-grained control over object creation methods
- You can choose between **constructors** and **factory methods** as the two main object creation approaches
- **Parameter name hints** allow you to set specific values for constructor or factory method parameters
- **field()** and **javaBeansProperty()** control how property values are generated
- **In most cases, introspector settings are sufficient**, and instantiate is only needed for special cases

By properly utilizing these features, you can create even complex objects accurately for your testing purposes.

### Next Steps

To learn more about test data generation:
- [Introspector](../introspector): How to set object creation methods globally
- [Fixture Monkey](../fixture-monkey): Basic usage of Fixture Monkey
- [Generating Complex Types](../generating-complex-types): How to generate complex object structures
