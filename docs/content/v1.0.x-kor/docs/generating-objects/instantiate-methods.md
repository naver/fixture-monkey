---
title: "객체 생성 방법 지정하기"
images: []
menu:
docs:
parent: "generating-objects"
identifier: "instantiate-methods"
weight: 32
---

각 테스트마다 객체 생성을 다르게 하고 싶을 수 있습니다.
예를 들어, 같은 클래스에서도 첫 테스트에서는 생성자로 객체를 생성하고, 다른 테스트에서는 팩터리 메서드로 객체를 생성하고 싶을 수 있습니다

Fixture Monkey는 `instantiate()` 메서드를 제공해 객체 생성 방법을 선택할 수 있게 합니다.
{{< alert icon="💡" text="Kotlin Plugin을 추가한다면 커스텀 DSL에서 instantiateBy() 메서드를 사용할 수 있습니다." />}}

`ArbitraryBuilder`에서 원하는 인스턴스 생성 방법(생성자 또는 팩토리 메서드)으로 객체를 생성할 수 있습니다.

`ArbitraryBuilder`를 사용할 때마다 매번 객체 생성 방법을 지정해야 하는 것은 아닙니다.
전역 옵션으로 FixtureMonkey 인스턴스에서 객체 생성 방식을 지정해주고 싶다면, [Introspector](../introspector) 페이지를 참고해주세요.

`instantiate()` 메서드는 `ArbitraryBuilder`를 사용할 때 객체를 편리하게 생성할 수 있도록 도와주는 메서드일 뿐입니다.

## 생성자
여러 개의 생성자를 가진 커스텀 클래스가 있다고 가정해보겠습니다.

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

Fixture Monkey를 사용하면 여러 생성자 중 원하는 생성자를 선택하여 객체를 만들 수 있습니다.

### 인자가 없는 생성자 또는 기본 생성자
`instantiate` 메서드를 사용하여 생성자에게 객체를 생성할 수 있도록 `ArbitraryBuilder`에 지시하는 기본적인 방법은 다음과 같습니다.

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
생성자 메서드를 사용하도록 지정하려면 `constructor()` 옵션을 전달하면 됩니다.
만약 인자가 없는 생성자가 있다면 해당 생성자를 사용하고, 없다면 첫 번째로 작성된 생성자를 사용합니다.

### 특정 생성자 지정
클래스가 두 개 이상의 생성자를 가진다면 필요한 파라미터 정보를 제공하여 원하는 생성자를 지정할 수 있습니다.
다음 두 개의 생성자를 가진 Product 클래스를 살펴봅시다.

options를 비어있는 리스트로 초기화해주는 생성자를 사용하려면 다음과 같이 매개 변수를 지정합니다.

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

productName을 "defaultProductName"으로 하는 다른 생성자를 사용하려면 다음처럼 매개 변수 정보만 변경하면 됩니다.
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

참고로 private 생성자를 사용하는 것도 가능합니다.

### 매개변수 이름으로 힌트 제공
생성자에 특정 값을 전달하려는 경우 매개변수 이름으로 힌트를 추가할 수 있습니다.

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

이 예제에서는 제품 이름에 대한 매개 변수 이름 힌트를 "str"으로 제공합니다.
이를 통해 `set()` 함수를 사용하여 제품 이름을 원하는 값(이 경우 "book"을 의미합니다.)으로 설정할 수 있습니다.

힌트를 어떤 이름으로든 설정할 수 있지만, 혼동을 피하기 위해 생성자 매개변수에 이름을 사용하는 것이 좋습니다.
또한 매개변수 이름 힌트를 사용하여 이름을 변경한 후에는 더 이상 필드 이름 "productName"을 사용하여 설정할 수 없습니다.

### 기본 인자 설정 (Kotlin)
Kotlin에서는 생성자 매개 변수 옵션에 추가 값을 전달할 수 있는 유연성이 있어 기본 인수를 사용할 경우 사용 여부를 결정할 수 있습니다.

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

### 제네릭 객체
제네릭 객체도 비슷한 방식으로 객체를 생성할 수 있습니다.
샘플 클래스 `GenericObject`를 살펴보겠습니다.

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

제네릭 객체로 작업할 때 생성자를 실제 타입으로 사용하도록 지정할 수 있습니다.

### 중첩된 객체가 포함된 생성자
중첩된 객체가 존재하는 시나리오에서 각 객체의 생성이 생성자를 통해 이루어지도록 하고싶은 경우, 각 타입별로 사용할 생성자를 지정해줄 수 있습니다.

예를 들어 `Product` 클래스를 사용하는 `ProductList` 클래스를 가정해보겠습니다.

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

다음과 같이 생성자와 함께 `ProductList`와 `Product` 모두에 특정 생성자를 사용하도록 지정할 수 있습니다.

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

{{< alert icon="💡" text="객체를 생성하는 메서드 내에서 서로 다른 프로퍼티에 대해 생성자 메서드와 팩토리 메서드 접근 방식을 결합하여 사용할 수도 있습니다. 위의 예제에서 ProductList는 팩토리 메서드를 사용하여 초기화할 수 있고, Product는 생성자를 사용하여 인스턴스화할 수 있습니다." />}}

## 팩토리 메서드
객체를 생성하는 두 번째 방법은 팩토리 메서드를 사용하는 것입니다.

'from'이라는 팩토리 메서드가 포함된 위의 동일한 Product 클래스를 생각해 보겠습니다.

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


### 특정 팩토리 메서드 사용
메서드 이름을 제공하여 사용할 팩토리 메서드를 지정할 수 있습니다.

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

이름이 같은 팩토리 메서드가 여러 개 있는 경우 `constructor()` 메서드에서와 마찬가지로 매개변수 유형 정보로 메서드를 구분할 수 있습니다.

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

### 팩토리 메서드에서 매개변수 이름으로 힌트 제공
매개변수 이름으로 힌트를 제공하는 방법은 `factory()`를 사용할 때도 추가할 수 있으며, `constructor()`에서 매개변수 이름으로 힌트 제공하는 방법과 동일하게 동작합니다.

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

## 필드와 자바 빈 프로퍼티
각 객체를 생성하는 메서드(`constructor()`와 `factory()`)에서 필드 또는 자바 빈 프로퍼티(getter & setter) 기반 중 하나의 방법으로 프로퍼티 생성 여부를 선택할 수 있습니다.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

.instantiate(constructor().field()) // 필드에 기반하여 생성

.instantiate(constructor().javaBeansProperty()) // 자바 빈 프로퍼티에 기반하여 생성

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

필드를 사용하는 경우 필드에 대한 값이 생성됩니다.
자바 빈 프로퍼티를 사용하는 경우 클래스에 게터와 세터가 있으면 충분하며, 임의 값이 생성됩니다.

### 프로퍼티 제외
일부 프로퍼티가 생성되지 않도록 제외하기 위해 `filter()` 메서드를 사용할 수 있습니다.

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

예를 들어 첫 번째 예시와 같이 private 필드가 생성되지 않도록 제외하거나 두 번째 예시와 같이 특정 속성을 이름별로 필터링할 수 있습니다.
