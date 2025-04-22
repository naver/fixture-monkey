---
title: "객체 생성 방법 지정하기"
images: []
menu:
docs:
parent: "generating-objects"
identifier: "instantiate-methods"
weight: 33
---

## 개요: 객체 생성 방법을 직접 지정하는 이유

기본적으로 Fixture Monkey는 인트로스펙터(Introspector)를 통해 객체 생성 방법을 자동으로 결정합니다. 하지만 때로는 다음과 같은 이유로 특정 방법을 직접 지정해야 할 수 있습니다:

- **특정 생성자 사용**: 클래스에 여러 생성자가 있을 때 특정 생성자를 선택하고 싶은 경우
- **팩토리 메서드 활용**: 생성자 대신 팩토리 메서드로 객체를 생성하고 싶은 경우
- **테스트별 다른 초기화**: 같은 클래스지만 테스트마다 다른 방식으로 초기화하고 싶은 경우
- **특수한 초기화 로직**: 인트로스펙터로 자동 처리되지 않는 특별한 초기화가 필요한 경우

이러한 상황에서 `instantiate()` 메서드를 사용하면 객체 생성 방법을 세밀하게 제어할 수 있습니다.

{{< alert icon="💡" text="이 문서에서 설명하는 방법은 테스트별로 객체 생성 방법을 지정하는 것입니다. 모든 테스트에 동일한 방식을 적용하려면 인트로스펙터 페이지를 참고하세요." />}}

## 처음 시작하기: 가장 기본적인 사용법

Fixture Monkey로 객체를 생성할 때 가장 기본적인 방법은 다음과 같습니다:

```java
// 기본 방식 - 인트로스펙터가 자동으로 객체 생성 방법 결정
Product product = fixtureMonkey.giveMeOne(Product.class);
```

하지만 특정 생성자나 팩토리 메서드를 사용하고 싶다면 `instantiate()` 메서드를 사용합니다:

```java
// 특정 생성자 지정
Product product = fixtureMonkey.giveMeBuilder(Product.class)
    .instantiate(constructor())
    .sample();

// 특정 팩토리 메서드 지정
Product product = fixtureMonkey.giveMeBuilder(Product.class)
    .instantiate(factoryMethod("create"))
    .sample();
```

{{< alert icon="⭐" text="초보자 팁: 대부분의 경우 기본 방식(giveMeOne)으로 충분합니다. 특별한 초기화 요구사항이 있을 때만 instantiate() 메서드를 사용하세요." />}}

## 기본 개념

### ArbitraryBuilder란?

`ArbitraryBuilder`는 객체 생성 설정을 구성하기 위한 빌더 클래스입니다. Fixture Monkey에서 `giveMeBuilder()` 메서드를 호출하면 반환됩니다.

```java
// ArbitraryBuilder 얻기
ArbitraryBuilder<Product> builder = fixtureMonkey.giveMeBuilder(Product.class);
```

### instantiate() 메서드란?

`instantiate()` 메서드는 `ArbitraryBuilder`에서 객체를 어떻게 생성할지 지정하는 메서드입니다. 생성자나 팩토리 메서드 중 하나를 선택할 수 있습니다.

📌 **메서드 형식:**

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// Java에서 생성자 지정
.instantiate(constructor())

// Java에서 팩토리 메서드 지정
.instantiate(factoryMethod("methodName"))
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// Kotlin에서 생성자 지정 (Kotlin 플러그인 필요)
.instantiateBy {
    constructor()
}

// Kotlin에서 팩토리 메서드 지정
.instantiateBy {
    factory("methodName")
}
{{< /tab >}}
{{< /tabpane>}}

## 1. 간단한 생성자 사용하기

가장 기본적인 사용법부터 시작해보겠습니다. 간단한 클래스를 예로 들겠습니다:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
public class SimpleProduct {
    private final String name;
    private final int price;
    
    // 생성자
    public SimpleProduct(String name, int price) {
        this.name = name;
        this.price = price;
    }
    
    // Getter 메서드
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

이 클래스의 생성자를 사용하여 객체를 생성하는 방법:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void 단순_생성자_사용() {
    SimpleProduct product = fixtureMonkey.giveMeBuilder(SimpleProduct.class)
        .instantiate(constructor())
        .sample();
    
    // 생성된 객체 확인
    assertThat(product).isNotNull();
    assertThat(product.getName()).isNotNull();
    assertThat(product.getPrice()).isNotNegative();
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun 단순_생성자_사용() {
    val product = fixtureMonkey.giveMeBuilder<SimpleProduct>()
        .instantiateBy {
            constructor()
        }
        .sample()
    
    // 생성된 객체 확인
    assertThat(product).isNotNull()
    assertThat(product.name).isNotNull()
    assertThat(product.price).isNotNegative()
}
{{< /tab >}}
{{< /tabpane>}}

이 예제에서 `constructor()`는 SimpleProduct의 생성자를 사용하도록 지정합니다. Fixture Monkey는 자동으로 적절한 값을 생성하여 생성자에 전달합니다.

## 2. 여러 생성자 중 선택하기

이제 여러 생성자를 가진 클래스를 살펴보겠습니다:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
public class Product {
    private final long id;
    private final String name;
    private final long price;
    private final List<String> options;
    
    // 기본 생성자 (모든 필드 기본값)
    public Product() {
        this.id = 0;
        this.name = "기본상품";
        this.price = 0;
        this.options = null;
    }
    
    // 옵션이 없는 간단한 상품 생성자
    public Product(String name, long price) {
        this.id = new Random().nextLong();
        this.name = name;
        this.price = price;
        this.options = Collections.emptyList();
    }
    
    // 옵션이 있는 상품 생성자
    public Product(String name, long price, List<String> options) {
        this.id = new Random().nextLong();
        this.name = name;
        this.price = price;
        this.options = options;
    }
    
    // Getter 메서드
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
    
    // 기본 생성자 (모든 필드 기본값)
    constructor() {
        this.id = 0
        this.name = "기본상품"
        this.price = 0
        this.options = emptyList()
    }
    
    // 옵션이 없는 간단한 상품 생성자
    constructor(name: String, price: Long) {
        this.id = Random().nextLong()
        this.name = name
        this.price = price
        this.options = emptyList()
    }
    
    // 옵션이 있는 상품 생성자
    constructor(name: String, price: Long, options: List<String>) {
        this.id = Random().nextLong()
        this.name = name
        this.price = price
        this.options = options
    }
}
{{< /tab >}}
{{< /tabpane>}}

### 2.1 기본 생성자 사용

기본 생성자를 사용하려면:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void 기본_생성자_사용() {
    Product product = fixtureMonkey.giveMeBuilder(Product.class)
        .instantiate(constructor())  // 파라미터 없으면 기본 생성자 선택
        .sample();
    
    assertThat(product.getId()).isEqualTo(0);
    assertThat(product.getName()).isEqualTo("기본상품");
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun 기본_생성자_사용() {
    val product = fixtureMonkey.giveMeBuilder<Product>()
        .instantiateBy {
            constructor()  // 파라미터 없으면 기본 생성자 선택
        }
        .sample()
    
    assertThat(product.id).isEqualTo(0)
    assertThat(product.name).isEqualTo("기본상품")
}
{{< /tab >}}
{{< /tabpane>}}

`constructor()`에 파라미터를 지정하지 않으면, Fixture Monkey는 기본 생성자(인자가 없는 생성자)를 사용합니다.

### 2.2 특정 생성자 선택하기

클래스에 여러 생성자가 있을 때 파라미터 타입을 지정하여 원하는 생성자를 선택할 수 있습니다:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void 옵션없는_생성자_선택() {
    Product product = fixtureMonkey.giveMeBuilder(Product.class)
        .instantiate(
            constructor()
                .parameter(String.class)  // 첫 번째 파라미터 타입
                .parameter(long.class)    // 두 번째 파라미터 타입
        )
        .sample();
    
    assertThat(product.getOptions()).isEmpty();
}

@Test
void 옵션있는_생성자_선택() {
    Product product = fixtureMonkey.giveMeBuilder(Product.class)
        .instantiate(
            constructor()
                .parameter(String.class)
                .parameter(long.class)
                .parameter(new TypeReference<List<String>>(){})  // 제네릭 타입 지정
        )
        .sample();
    
    assertThat(product.getOptions()).isNotNull();
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun 옵션없는_생성자_선택() {
    val product = fixtureMonkey.giveMeBuilder<Product>()
        .instantiateBy {
            constructor<Product> {
                parameter<String>()  // 첫 번째 파라미터 타입
                parameter<Long>()    // 두 번째 파라미터 타입
            }
        }
        .sample()
    
    assertThat(product.options).isEmpty()
}

@Test
fun 옵션있는_생성자_선택() {
    val product = fixtureMonkey.giveMeBuilder<Product>()
        .instantiateBy {
            constructor<Product> {
                parameter<String>()
                parameter<Long>()
                parameter<List<String>>()  // 제네릭 타입 지정
            }
        }
        .sample()
    
    assertThat(product.options).isNotNull()
}
{{< /tab >}}
{{< /tabpane>}}

> **용어 설명**: `parameter()` 메서드는 생성자 파라미터의 타입을 지정하여 원하는 생성자를 선택하는 역할을 합니다.

### 2.3 생성자 파라미터 값 지정하기

파라미터 값을 직접 지정하려면 파라미터 이름 힌트(parameter name hint)를 사용할 수 있습니다:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void 파라미터_값_지정() {
    Product product = fixtureMonkey.giveMeBuilder(Product.class)
        .instantiate(
            constructor()
                .parameter(String.class, "productName")  // 파라미터 이름 힌트 지정
                .parameter(long.class)
        )
        .set("productName", "특별상품")  // 힌트로 지정한 이름으로 값 설정
        .sample();
    
    assertThat(product.getName()).isEqualTo("특별상품");
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun 파라미터_값_지정() {
    val product = fixtureMonkey.giveMeBuilder<Product>()
        .instantiateBy {
            constructor<Product> {
                parameter<String>("productName")  // 파라미터 이름 힌트 지정
                parameter<Long>()
            }
        }
        .set("productName", "특별상품")  // 힌트로 지정한 이름으로 값 설정
        .sample()
    
    assertThat(product.name).isEqualTo("특별상품")
}
{{< /tab >}}
{{< /tabpane>}}

> **용어 설명**: 파라미터 이름 힌트(parameter name hint)는 생성자 파라미터에 별칭을 부여하여 나중에 이 이름으로 값을 설정할 수 있게 해주는 기능입니다.

## 3. 팩토리 메서드 사용하기

생성자 외에도 팩토리 메서드를 사용하여 객체를 생성할 수 있습니다. 팩토리 메서드가 있는 클래스를 살펴보겠습니다:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
public class Product {
    // 앞에서 정의한 필드와 생성자들...
    
    // 팩토리 메서드
    public static Product create(String name, long price) {
        return new Product(name, price);
    }
    
    // 추천 상품 생성 팩토리 메서드
    public static Product createRecommended(long price) {
        return new Product("추천상품", price);
    }
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
class Product {
    // 앞에서 정의한 필드와 생성자들...
    
    companion object {
        // 팩토리 메서드
        fun create(name: String, price: Long): Product {
            return Product(name, price)
        }
        
        // 추천 상품 생성 팩토리 메서드
        fun createRecommended(price: Long): Product {
            return Product("추천상품", price)
        }
    }
}
{{< /tab >}}
{{< /tabpane>}}

### 3.1 기본 팩토리 메서드 사용

팩토리 메서드를 사용하여 객체를 생성하려면:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void 팩토리_메서드_사용() {
    Product product = fixtureMonkey.giveMeBuilder(Product.class)
        .instantiate(
            factoryMethod("create")  // 팩토리 메서드 이름 지정
        )
        .sample();
    
    assertThat(product).isNotNull();
    assertThat(product.getOptions()).isEmpty();
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun 팩토리_메서드_사용() {
    val product = fixtureMonkey.giveMeBuilder<Product>()
        .instantiateBy {
            factory<Product>("create")  // 팩토리 메서드 이름 지정
        }
        .sample()
    
    assertThat(product).isNotNull()
    assertThat(product.options).isEmpty()
}
{{< /tab >}}
{{< /tabpane>}}

> **용어 설명**: 팩토리 메서드(Factory Method)는 객체 생성을 담당하는 정적 메서드로, 생성자를 직접 호출하는 대신 이 메서드를 통해 객체를 생성합니다.

### 3.2 특정 팩토리 메서드 선택하기

여러 팩토리 메서드가 있을 때 파라미터 타입을 지정하여 원하는 메서드를 선택할 수 있습니다:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void 특정_팩토리_메서드_선택() {
    Product product = fixtureMonkey.giveMeBuilder(Product.class)
        .instantiate(
            factoryMethod("createRecommended")
                .parameter(long.class)  // 파라미터 타입 지정
        )
        .sample();
    
    assertThat(product.getName()).isEqualTo("추천상품");
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun 특정_팩토리_메서드_선택() {
    val product = fixtureMonkey.giveMeBuilder<Product>()
        .instantiateBy {
            factory<Product>("createRecommended") {
                parameter<Long>()  // 파라미터 타입 지정
            }
        }
        .sample()
    
    assertThat(product.name).isEqualTo("추천상품")
}
{{< /tab >}}
{{< /tabpane>}}

### 3.3 팩토리 메서드 파라미터 값 지정하기

팩토리 메서드의 파라미터 값을 직접 지정하려면:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void 팩토리_메서드_파라미터_값_지정() {
    Product product = fixtureMonkey.giveMeBuilder(Product.class)
        .instantiate(
            factoryMethod("create")
                .parameter(String.class, "productName")  // 파라미터 이름 힌트
                .parameter(long.class, "productPrice")
        )
        .set("productName", "커스텀상품")
        .set("productPrice", 9900L)
        .sample();
    
    assertThat(product.getName()).isEqualTo("커스텀상품");
    assertThat(product.getPrice()).isEqualTo(9900L);
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun 팩토리_메서드_파라미터_값_지정() {
    val product = fixtureMonkey.giveMeBuilder<Product>()
        .instantiateBy {
            factory<Product>("create") {
                parameter<String>("productName")  // 파라미터 이름 힌트
                parameter<Long>("productPrice")
            }
        }
        .set("productName", "커스텀상품")
        .set("productPrice", 9900L)
        .sample()
    
    assertThat(product.name).isEqualTo("커스텀상품")
    assertThat(product.price).isEqualTo(9900L)
}
{{< /tab >}}
{{< /tabpane>}}

## 4. 고급 기능과 주의사항

객체 생성 과정에서 알아두면 좋은 고급 기능과 주의사항을 살펴보겠습니다.

### 4.1 필드 vs JavaBeansProperty 선택하기

객체 생성 시 속성 값을 어떻게 설정할지 제어할 수 있습니다. 두 가지 주요 방법이 있습니다:

{{< alert icon="📘" text="생성자나 팩토리 메서드로 객체를 생성한 후, 생성자에서 초기화되지 않은 속성들을 어떻게 설정할지 결정하는 옵션입니다." />}}

1. **field()**: 클래스의 필드를 기반으로 속성 생성
   - 장점: 직접 필드에 접근하므로 setter가 없어도 됨
   - 단점: 캡슐화 우회, 유효성 검사 로직 무시

2. **javaBeansProperty()**: getter/setter 메서드를 기반으로 속성 생성
   - 장점: 캡슐화 유지, setter의 유효성 검사 로직 활용
   - 단점: setter가 없으면 속성 설정 불가능

📋 **간단한 선택 가이드**:
- setter 메서드에 유효성 검사 로직이 있고 이를 테스트하고 싶다면: **javaBeansProperty()**
- setter 메서드가 없거나 유효성 검사를 우회하고 싶다면: **field()**

#### 4.1.1 필드 기반 속성 생성

필드 기반으로 속성을 생성하려면:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void 필드_기반_속성_생성() {
    Product product = fixtureMonkey.giveMeBuilder(Product.class)
        .instantiate(
            constructor().field()  // 필드 기반 속성 생성
        )
        .sample();
    
    assertThat(product).isNotNull();
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun 필드_기반_속성_생성() {
    val product = fixtureMonkey.giveMeBuilder<Product>()
        .instantiateBy {
            constructor {
                javaField()  // 필드 기반 속성 생성
            }
        }
        .sample()
    
    assertThat(product).isNotNull()
}
{{< /tab >}}
{{< /tabpane>}}

> **용어 설명**: 필드(Field)는 클래스 내에 정의된 변수로, 객체의 상태를 저장합니다. 필드 기반 속성 생성은 이러한 필드를 직접 사용하여 값을 설정합니다.

#### 4.1.2 JavaBeansProperty 기반 속성 생성

JavaBeansProperty 기반으로 속성을 생성하려면:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void JavaBeansProperty_기반_속성_생성() {
    Product product = fixtureMonkey.giveMeBuilder(Product.class)
        .instantiate(
            constructor().javaBeansProperty()  // JavaBeansProperty 기반 속성 생성
        )
        .sample();
    
    assertThat(product).isNotNull();
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun JavaBeansProperty_기반_속성_생성() {
    val product = fixtureMonkey.giveMeBuilder<Product>()
        .instantiateBy {
            constructor {
                javaBeansProperty()  // JavaBeansProperty 기반 속성 생성
            }
        }
        .sample()
    
    assertThat(product).isNotNull()
}
{{< /tab >}}
{{< /tabpane>}}

> **용어 설명**: JavaBeansProperty는 getter/setter 메서드 쌍으로 표현되는 속성을 말합니다. 예를 들어 getName()/setName() 메서드 쌍은 'name' 속성을 나타냅니다.

### 4.2 생성자 이후 속성 설정 주의사항

{{< alert icon="⚠️" text="이 섹션은 Fixture Monkey가 객체 생성 후 속성을 설정할 때의 주의점을 설명합니다." />}}

`instantiate()` 메서드로 생성자를 지정했을 때, Fixture Monkey는 생성자를 통해 객체를 생성한 후에도 생성자에서 다루지 않은 속성들에 대해 임의의 값을 설정합니다. 이 기능은 생성자에서 초기화되지 않은 필드에 대해서도 테스트 데이터를 생성하고 싶을 때 유용합니다.

#### 작동 방식 한눈에 보기:

1. 생성자 지정: `instantiate(constructor()...)`
2. 생성자로 객체 생성
3. 생성자에서 초기화되지 않은 속성들에 임의 값 설정
4. 전체 객체 반환

예제 코드로 살펴보겠습니다:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
public class PartiallyInitializedObject {
    private final String name;      // 생성자에서 초기화
    private int count;              // 생성자에서 초기화되지 않음
    private List<String> items;     // 생성자에서 초기화되지 않음
    
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
void 생성자후_속성_설정() {
    PartiallyInitializedObject obj = fixtureMonkey.giveMeBuilder(PartiallyInitializedObject.class)
        .instantiate(constructor().parameter(String.class))
        .sample();
    
    assertThat(obj.getName()).isNotNull();       // 생성자에서 초기화됨
    assertThat(obj.getCount()).isNotZero();      // 생성자 이후 초기화됨
    assertThat(obj.getItems()).isNotNull();      // 생성자 이후 초기화됨
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
class PartiallyInitializedObject(
    val name: String               // 생성자에서 초기화
) {
    var count: Int = 0             // 생성자에서 초기화되지 않음 
    var items: List<String>? = null // 생성자에서 초기화되지 않음
}

@Test
fun 생성자후_속성_설정() {
    val obj = fixtureMonkey.giveMeBuilder<PartiallyInitializedObject>()
        .instantiateBy {
            constructor<PartiallyInitializedObject> {
                parameter<String>()
            }
        }
        .sample()
    
    assertThat(obj.name).isNotNull()       // 생성자에서 초기화됨
    assertThat(obj.count).isNotZero()      // 생성자 이후 초기화됨
    assertThat(obj.items).isNotNull()      // 생성자 이후 초기화됨
}
{{< /tab >}}
{{< /tabpane>}}

#### 4.2.1 주의사항

이 기능을 사용할 때 한 가지 중요한 주의사항이 있습니다:

{{< alert icon="⚠️" text="Fixture Monkey는 객체 생성 후에 **생성자에서 이미 설정된 속성값도 변경할 수 있습니다**. 이로 인해 의도하지 않은 테스트 결과가 발생할 수 있습니다." />}}

**문제 상황:**
1. 생성자에서 `name = "특정이름"` 으로 설정
2. Fixture Monkey가 객체 생성 후 자동으로 `name`에 임의 값 할당
3. `name`이 원래 지정한 "특정이름"이 아닌 다른 값으로 변경됨

**해결 방법:**
중요한 값을 명시적으로 설정하여 이 문제를 해결할 수 있습니다:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
void 생성자에서_설정된_값_보존하기() {
    String specificName = "특정이름";
    
    PartiallyInitializedObject obj = fixtureMonkey.giveMeBuilder(PartiallyInitializedObject.class)
        .instantiate(
            constructor()
                .parameter(String.class, "name")
        )
        .set("name", specificName)  // 생성자 파라미터 값을 명시적으로 설정
        .sample();
    
    assertThat(obj.getName()).isEqualTo(specificName);  // 명시적으로 설정한 값이 보존됨
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
fun 생성자에서_설정된_값_보존하기() {
    val specificName = "특정이름"
    
    val obj = fixtureMonkey.giveMeBuilder<PartiallyInitializedObject>()
        .instantiateBy {
            constructor<PartiallyInitializedObject> {
                parameter<String>("name")
            }
        }
        .set("name", specificName)  // 생성자 파라미터 값을 명시적으로 설정
        .sample()
    
    assertThat(obj.name).isEqualTo(specificName)  // 명시적으로 설정한 값이 보존됨
}
{{< /tab >}}
{{< /tabpane>}}

{{< alert icon="💡" text="초보자 팁: 중요한 값은 항상 `.set()` 메서드로 명시적으로 설정하여 예측 가능한 테스트 결과를 얻으세요!" />}}

## 자주 묻는 질문 (FAQ)

### Q: instantiate와 인트로스펙터의 차이점은 무엇인가요?

**A**: 인트로스펙터는 모든 객체 생성에 적용되는 글로벌 설정인 반면, instantiate는 특정 테스트나 객체에만 적용하는 지역 설정입니다. 

**간단히 말하면:**
- **인트로스펙터**: "모든 테스트에서 이 방식으로 객체를 만들어줘"
- **instantiate**: "이 특정 테스트에서만 이 방식으로 객체를 만들어줘"

대부분의 경우 인트로스펙터만으로 충분하지만, 특별한 생성 로직이 필요할 때 instantiate를 사용하세요.

### Q: 여러 생성자 중 어떤 것을 선택해야 할지 어떻게 결정하나요?

**A**: 테스트 목적에 가장 적합한 생성자를 선택하세요. 일반적으로:

- **간단한 테스트**: 인자가 적은 생성자 선택
- **특정 필드 테스트**: 해당 필드를 초기화하는 생성자 선택
- **유효성 검사 테스트**: 유효성 검사 로직이 있는 생성자 선택

### Q: 파라미터 이름 힌트의 이점은 무엇인가요?

**A**: 파라미터 이름 힌트를 사용하면:
- 생성자나 팩토리 메서드 파라미터에 의미 있는 이름을 부여할 수 있습니다.
- set() 메서드로 특정 파라미터 값을 쉽게 설정할 수 있습니다.
- 코드 가독성이 향상됩니다.

### Q: 필드와 JavaBeansProperty 중 어떤 것을 사용해야 하나요?

**A**:
- 필드(field())는 클래스에 setter 메서드가 없거나 직접 필드에 접근하고 싶을 때 사용합니다.
- JavaBeansProperty(javaBeansProperty())는 setter 메서드를 통해 유효성 검사나 특별한 처리가 필요할 때 사용합니다.
- 확실하지 않다면 기본값(명시하지 않음)을 사용하세요. Fixture Monkey가 적절한 방법을 선택합니다.

### Q: 제네릭 타입의 파라미터를 어떻게 지정하나요?

**A**: 제네릭 타입은 TypeReference를 사용하여 지정합니다:

```java
// Java
.parameter(new TypeReference<List<String>>(){})
```

```kotlin
// Kotlin
parameter<List<String>>()
```

### Q: 생성자에서 설정한 값이 변경되는 문제를 어떻게 방지하나요?

**A**: `.set()` 메서드를 사용하여 중요한 값을 명시적으로 설정하세요:

```java
fixtureMonkey.giveMeBuilder(MyClass.class)
    .instantiate(constructor().parameter(String.class, "name"))
    .set("name", "중요한값")  // 이 값은 변경되지 않음
    .sample();
```

## 요약

{{< alert icon="📌" text="핵심 포인트 요약" />}}

- **instantiate() 메서드**는 객체 생성 방법을 세밀하게 제어하는 기능을 제공합니다.
- **생성자**와 **팩토리 메서드** 두 가지 주요 객체 생성 방법 중 선택할 수 있습니다.
- **파라미터 이름 힌트**를 사용하면 생성자나 팩토리 메서드의 특정 파라미터 값을 설정할 수 있습니다.
- **field()** 와 **javaBeansProperty()** 로 속성 생성 방식을 제어할 수 있습니다.
- **대부분의 경우 인트로스펙터 설정으로 충분하며**, instantiate는 특수한 경우에만 필요합니다.

이 기능들을 적절히 활용하면 복잡한 객체도 테스트 목적에 맞게 정확하게 생성할 수 있습니다.

### 다음 단계

테스트 데이터 생성에 관한 더 자세한 내용을 알아보려면:
- [인트로스펙터](../introspector): 객체 생성 방법을 전역적으로  설정하는 방법
- [객체 생성하기](../fixture-monkey): Fixture Monkey의 기본 사용법
- [복잡한 타입 생성하기](../generating-complex-types): 복잡한 객체 구조 생성 방법
