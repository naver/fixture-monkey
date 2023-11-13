---
title: "Instantiate Methods"
images: []
menu:
docs:
parent: "generating-objects"
identifier: "instantiate-methods"
weight: 32
---

For each test, you may want to use a different way of creating objects.
For example, even within the same class, one test may require initialization using the constructor, while another test may require initialization using its factory method.

Fixture Monkey allows you to choose the preferred method of creating your object through the `instantiate()` method.
{{< alert icon="💡" text="If the Kotlin Plugin is added, you can use the instantiateBy() method with a custom DSL." />}}

From the `ArbitraryBuilder`, you can determine how the object is created by specifying the preferred instantiation method (constructor or factory method).

This doesn't mean that you have to tell the `ArbitraryBuilder` how to create an object every time you use it.
If you want to set a global option as the default method for creating all objects with a Fixture Monkey instance, refer to the [Introspector](../introspector) page.

The `instantiate()` method is just a convenient way to modify the generation method from the `ArbitraryBuilder`.

## Constuctor
Let's say you have a custom class with a few different constructors that looks like this:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

@Value
public class Product {
    long id;

    String productName;

    long price;

    List<String> options;

    Instant createdAt;

    public Product() {
        this.id = 0;
        this.productName = null;
        this.price = 0;
        this.options = null;
        this.createdAt = null;
    }

    public Product(
        String str,
        long id,
        long price
    ) {
        this.id = id;
        this.productName = str;
        this.price = price;
        this.options = Collections.emptyList();
        this.createdAt = Instant.now();
    }

    public Product(
        long id,
        long price,
        List<String> options
    ) {
        this.id = id;
        this.productName = "defaultProductName";
        this.price = price;
        this.options = options;
        this.createdAt = Instant.now();
    }
}

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

class Product(
    val id: Long,
    val productName: String,
    val price: Long,
    val options: List<String>,
    val createdAt: Instant
) {
    constructor() : this(
        id = 0,
        productName = "",
        price = 0,
        options = emptyList(),
        createdAt = Instant.now()
    )

    constructor(str: String, id: Long, price: Long) : this(
        id = id,
        productName = str,
        price = price,
        options = emptyList(),
        createdAt = Instant.now()
    )

    constructor(id: Long, price: Long, options: List<String>) : this(
        id = id,
        productName = "defaultProductName",
        price = price,
        options = options,
        createdAt = Instant.now()
    )

    companion object {
        fun from(id: Long, price: Long): Product = Product("product", id, price)
    }
}

{{< /tab >}}
{{< /tabpane>}}

Fixture Monkey allows you to select the exact constructor you'd like to use to create the object.

### NoArgsConstructor, DefaultConstructor
The most basic way to use `instantiate` to tell the `ArbitraryBuilder` to create an object with its constructor is as follows:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

@Test
void test() {
    Product product = fixtureMonkey.giveMeBuilder(Product.class)
        .instantiate(constructor())
        .sample();

    then(product.getId()).isEqualTo(0);
}

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

@Test
fun test() {
    val product = fixtureMonkey.giveMeBuilder<Product>()
        .instantiateBy {
            constructor()
        }
        .sample()

    then(product.productName).isEqualTo("")
}

{{< /tab >}}
{{< /tabpane>}}
You can specify to use the constructor method by passing the `constructor()` option.
If there is a NoArgsConstructor, it will be used to create the object. If not, the first written constructor is used.

### Specifying a Constructor
If a class has multiple constructors, you can specify the desired constructor by providing the necessary parameter information.
For instance, consider the Product class with two constructors requiring different parameters.

To use the constructor that creates instances with empty options, specify the paramters as follows:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

@Test
void test() {
    Product product = fixtureMonkey.giveMeBuilder(Product.class)
        .instantiate(
            constructor()
                .parameter(String.class)
                .parameter(long.class)
                .parameter(long.class)
        )
        .sample();

    then(product.getOptions()).isEmpty();
}

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

@RepeatedTest(TEST_COUNT)
fun test() {
    val product = fixtureMonkey.giveMeBuilder<Product>()
        .instantiateBy {
            constructor<Product> {
                parameter<String>()
                parameter<Long>()
                parameter<Long>()
        }
    }
    .sample()

    then(product.options).isEmpty()
}

{{< /tab >}}
{{< /tabpane>}}

If you want to use the other constructor that makes a `Product` with productName as "defaultProductName", you can specify it by just changing the parameter information:
{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

constructor()
    .parameter(long.class)
    .parameter(long.class)
    .parameter(new TypeReference<List<String>>(){})

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

constructor<Product> {
    parameter<Long>()
    parameter<Long>()
    parameter<List<String>>()
}

{{< /tab >}}
{{< /tabpane>}}

Note that using private constructors is also possible.

### Constructor Parameter Hints
In cases where you want to pass a specific value to the constructor, you can add a parameter name hint.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

@Test
void test() {
    Product product = fixtureMonkey.giveMeBuilder(Product.class)
        .instantiate(
            constructor()
                .parameter(String.class, "str")
                .parameter(long.class)
                .parameter(long.class)
        )
        .set("str", "book")
        .sample();

    then(product.getProductName()).isEqualTo("book");
    then(product.getOptions()).isEmpty();
}

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

@Test
fun test() {
    val product = fixtureMonkey.giveMeBuilder<Product>()
        .instantiateBy {
            constructor<Product> {
                parameter<String>("str")
                parameter<Long>()
                parameter<Long>()
            }
        }
        .set("str", "book")
        .sample()

    then(product.productName).isEqualTo("book")
    then(product.options).isEmpty()
}

{{< /tab >}}
{{< /tabpane>}}

In this example, we provide a parameter name hint for the productName to be "str"
This allows us to use the `set()` function to set the productName to the desired value (in this case "book").

Although you can set the hint to any name, we recommend that you use the name in the constructor parameter to avoid confusion.
Also note that once the name has been changed using the parameter name hint, you can no longer set it using the field name "productName".

### Using default arguments (Kotlin)
In Kotlin, you have the flexibility to pass an additional value to a constructor parameter option, allowing you to decide whether to use the default argument if one is available.

```kotlin
@Test
fun test() {
    class Product(val productName: String = "defaultProductName")

    val product = fixtureMonkey.giveMeBuilder<Product>()
        .instantiateBy {
            constructor {
                parameter<String>(useDefaultArgument = true)
            }
        }
        .sample()

    then(product.productName).isEqualTo("defaultProductName")
}
```

### Generic Objects
Generic Objects can also be instantiated in a similar way.
Consider this sample class `GenericObject`:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

@Value
public class GenericObject<T> {
    T value;

    public GenericObject(T value) {
        this.value = value;
    }
}

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

class GenericObject<T>(var value: T)

{{< /tab >}}
{{< /tabpane>}}

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

@Test
void test() {
    ConstructorTestSpecs.GenericObject<String> genericObject = fixtureMonkey.giveMeBuilder(
        new TypeReference<ConstructorTestSpecs.GenericObject<String>>() {
        })
        .instantiate(
            constructor()
                .parameter(String.class)
        )
        .sample();

		then(genericObject).isNotNull();
		then(genericObject.getValue()).isNotNull();
	}

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

@Test
fun test() {
    val genericObject = fixtureMonkey.giveMeBuilder<GenericObject<String>>()
        .instantiateBy {
            constructor() {
                parameter<String>()
            }
        }
        .sample()

    then(genericObject).isNotNull()
    then(genericObject.value).isNotNull()
}

{{< /tab >}}
{{< /tabpane>}}

You can specify to use the constructor with the actual type when working with generic objects.

### Using Constructors with Nested Objects
In scenarios involving nested objects, where you wish to specify the creation of both objects using their constructors, you can designate each type and specify the constructor to be used.

For example, consider the `ProductList` class that uses the `Product` class:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

@Value
public class ProductList {
    String listName;
    List<Product> list;

    public ProductList(List<Product> list) {
        this.listName = "defaultProductListName";
        this.list = list;
    }
}

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

class ProductList(val listName: String, val list: List<Product>) {
    constructor(list: List<Product>) : this("defaultProductListName", list)
}

{{< /tab >}}
{{< /tabpane>}}

You can specify to use a certain constructor for both `ProductList` and `Product` with their constructors like the following:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

@Test
void test() {
    ProductList productList = fixtureMonkey.giveMeBuilder(ProductList.class)
        .instantiate(
            ProductList.class,
            constructor()
                .parameter(new TypeReference<List<Product>>() {}, "list")
        )
        .instantiate(
            Product.class,
            constructor()
                .parameter(long.class)
                .parameter(long.class)
                .parameter(new TypeReference<List<String>>(){})
        )
        .size("list", 1)
        .sample();

    then(productList.getListName()).isEqualTo("defaultProductListName");
    then(productList.getList()).hasSize(1);
    then(productList.getList().get(0).getProductName()).isEqualTo("defaultProductName");
}

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

@Test
fun test() {
    val productList = fixtureMonkey.giveMeBuilder<ProductList>()
        .instantiateBy {
            constructor<ProductList> {
                parameter<List<Product>>("list")
            }
            constructor<Product> {
                parameter<Long>()
                parameter<Long>()
                parameter<List<String>>()
            }
        }
        .size("list", 1)
        .sample()

    then(productList.listName).isEqualTo("defaultProductListName")
    then(productList.list).hasSize(1)
    then(productList.list[0].productName).isEqualTo("defaultProductName")
}

{{< /tab >}}
{{< /tabpane>}}

{{< alert icon="💡" text="It is also possible to combine both constructor and factory method approaches for different properties within the instantiate method. In the above example, the ProductList can be initialized with the factory method, while the Product can be instantiated using the constructor." />}}

## Factory Method
The second way to create an object is by using its factory method.

Consider the same Product class above, which now includes a factory method called `from`.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

@Value
public class Product {
    long id;

    String productName;

    long price;

    List<String> options;

    Instant createdAt;

    public Product(
      String productName,
      long id,
      long price
    ) {
        this.id = id;
        this.productName = productName;
        this.price = price;
        this.options = Collections.emptyList();
        this.createdAt = Instant.now();
    }

    public static Product from(long id, long price) {
        return new Product("product", id, price);
    }
}

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

class Product(
    val id: Long,
    val productName: String,
    val price: Long,
    val options: List<String> = emptyList(),
    val createdAt: Instant = Instant.now()
) {
    companion object {
        fun from(id: Long, price: Long): Product {
            return Product("product", id, price)
        }
    }
}

{{< /tab >}}
{{< /tabpane>}}


### Specifying Factory Method
You can specify the factory method to be used by providing its name.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

@Test
void test() {
    Product product = fixtureMonkey.giveMeBuilder(Product.class)
        .instantiate(
            factoryMethod("from")
        )
        .sample();

    then(product.getProductName()).isEqualTo("product");
}

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

@Test
fun test() {
    val product = fixtureMonkey.giveMeBuilder<Product>()
        .instantiateBy {
            factory<Product>("from")
        }
        .sample()

    then(product.productName).isEqualTo("product")
}

{{< /tab >}}
{{< /tabpane>}}

If there are multiple factory methods with the same name, you can differentiate them by specifying parameter type information, similar to how it's done in the `constructor()` method.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

factoryMethod("from")
    .parameter(String.class)
    .parameter(Long.class)

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

factory<Product>("from") {
    parameter<String>()
    parameter<Long>()
}

{{< /tab >}}
{{< /tabpane>}}

### Factory Method Parameter Hints
Parameter name hints can also be added when using `factory()`, and it works the same as the parameter name hint of `constructor()`.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

@Test
void test() {
    Product product = fixtureMonkey.giveMeBuilder(Product.class)
        .instantiate(
            factoryMethod("from")
                .parameter(long.class, "productId")
                .parameter(long.class)
        )
        .set("productId", 100L)
        .sample();

    then(product.getProductName()).isEqualTo("product");
    then(product.getId()).isEqualTo(100L);
}

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

@Test
fun test() {
    val product = fixtureMonkey.giveMeBuilder<Product>()
        .instantiateBy {
            factory<Product>("from") {
                parameter<String>("productId")
                parameter<Long>()
            }
        }
        .set("productId", 100L)
        .sample()

    then(product.productName).isEqualTo("product")
    then(product.id).isEqualTo(100L)
}

{{< /tab >}}
{{< /tabpane>}}

## Field & JavaBeansProperty
For each instantiation method (constructor() and factory()), you can choose whether to generate properties based on the fields or the JavaBeans Property (getter & setter).

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

.instantiate(constructor().field()) // generate based on fields

.instantiate(constructor().javaBeansProperty()) // generate based on JavaBeans Property

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

.instantiateBy {
    constructor {
        javaField()
    }
}

.instantiateBy {
    constructor {
        javaBeansProperty()
    }
}

{{< /tab >}}
{{< /tabpane>}}

If you use field, the value for the fields will be generated.
If you use javaBeans Property, having a getter and setter in your class is sufficient, and a random value will be generated.

### Excluding properties
To exclude some properties from being generated, you can use the `filter()` method.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

.instantiate(
    constructor()
        .field(it -> it.filter(field -> !Modifier.isPrivate(field.getModifiers())))
)

.instantiate(
    constructor()
        .javaBeansProperty(it -> it.filter(property -> !"string".equals(property.getName())))
)

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

.instantiateBy {
    constructor {
        javaField {
            filter { !Modifier.isPrivate(it.modifiers) }
        }
    }
}

.instantiateBy {
    constructor {
        javaBeansProperty {
            filter { "string" != it.name }
        }
    }
}

{{< /tab >}}
{{< /tabpane>}}

For example, you can exclude private fields from being generated as shown in the first example, or you can filter out certain properties by name, as demonstrated in the second example.
