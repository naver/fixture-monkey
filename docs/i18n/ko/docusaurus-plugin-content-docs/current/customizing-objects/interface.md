---
title: "인터페이스 테스트하기"
sidebar_position: 44
---


## 이 문서에서 배울 내용
- 인터페이스에 대한 테스트 데이터를 생성하는 방법
- 어떤 구현체를 사용할지 지정하는 방법
- 인터페이스 구현체의 속성을 커스터마이징하는 방법
- 다양한 인터페이스 테스팅 접근법 중 선택하는 방법

## 인터페이스 테스팅의 중요성

> *이 섹션에서는 소프트웨어 개발에서 인터페이스가 왜 중요한지, 그리고 왜 인터페이스 테스팅이 가치있는지 배웁니다.*

인터페이스는 현대 소프트웨어 개발에서 다음과 같은 여러 중요한 이유로 필수적인 역할을 합니다:

### 실제 애플리케이션에서 인터페이스가 중요한 이유

1. **느슨한 결합(Loose Coupling)**: 인터페이스는 컴포넌트들이 서로의 내부 세부사항을 알 필요 없이 계약을 맺을 수 있게 합니다. 이는 애플리케이션의 각 부분 간의 의존성을 줄입니다.

2. **의존성 주입(Dependency Injection)**: 인터페이스는 의존성 주입을 쉽게 만들어, 구현체를 테스트 시 대안이나 목(mock)으로 대체할 수 있게 합니다.

3. **유연성과 확장성(Flexibility and Extensibility)**: 인터페이스를 사용하는 코드를 변경하지 않고도 새로운 구현체를 추가할 수 있습니다. 이는 개방-폐쇄 원칙(확장에는 열려있고, 수정에는 닫혀있음)을 따릅니다.

4. **향상된 테스트 용이성(Better Testability)**: 인터페이스를 사용하면 실제 구현체를 테스트 대역(test doubles)으로 대체할 수 있어, 단위 테스트가 훨씬 관리하기 쉬워집니다.

### 실제 예제: ProductInfo 인터페이스

실제 애플리케이션에서 인터페이스가 어떻게 사용되는지 예시를 통해 살펴보겠습니다:

```java
// 게터가 있는 상품 정보 인터페이스
public interface ProductInfo {
    String getName();         // 상품 이름 가져오기
    BigDecimal getPrice();    // 상품 가격 가져오기
    String getCategory();     // 상품 카테고리 가져오기
    boolean isAvailable();    // 상품 재고 여부 확인
    int getStockQuantity();   // 재고 수량 가져오기
}

// 인터페이스를 메서드 매개변수로 사용하는 서비스
public class ProductService {
    // 특정 수량을 구매할 수 있는지 확인
    public boolean canPurchase(ProductInfo productInfo, int quantity) {
        return productInfo.isAvailable() && 
               productInfo.getStockQuantity() >= quantity;
    }
    
    // 수량에 대한 총 가격 계산
    public BigDecimal calculateTotal(ProductInfo productInfo, int quantity) {
        return productInfo.getPrice().multiply(new BigDecimal(quantity));
    }
    
    // 상품 요약 정보 생성
    public String getProductSummary(ProductInfo productInfo) {
        return String.format("%s (%s) - $%s, 재고: %d", 
            productInfo.getName(), 
            productInfo.getCategory(), 
            productInfo.getPrice(), 
            productInfo.getStockQuantity());
    }
}
```

이 예제에서 `ProductService`는 `ProductInfo`의 어떤 구현체라도 함께 작동할 수 있습니다 - 상품 데이터가 어디서 오는지 알 필요가 없습니다.

### ProductInfo가 인터페이스여야 하는 이유

실제 애플리케이션에서 상품 정보는 다양한 출처에서 올 수 있습니다:

1. 온라인 스토어 상품 (OnlineProductInfo)
2. 오프라인 매장 상품 (StoreProductInfo) 
3. 재고 관리 시스템 상품 (InventoryProductInfo)
4. 프로모션 상품 (PromotionProductInfo)

각 출처는 동일한 기본 정보(이름, 가격, 카테고리)를 제공하지만, 추가 정보를 가지거나 동작이 다를 수 있습니다. 예를 들면:
- 온라인 상품은 배송 날짜 정보가 필요합니다
- 매장 상품은 매장 위치 정보가 필요합니다
- 재고 상품은 창고 위치 정보가 필요합니다

ProductService는 상품 정보가 어디서 오든 기본적인 상품 정보만 알면 작동해야 합니다. 인터페이스를 사용하면 다음과 같은 이점이 있습니다:

1. 특정 구현체에 의존하지 않아 서비스를 더 유연하게 만듭니다
2. 서비스 코드를 변경하지 않고도 새로운 유형의 상품 정보(예: 해외 상품)를 추가할 수 있습니다
3. 실제 상품 데이터베이스 없이도 테스트할 수 있어 테스트가 더 쉬워집니다

### 인터페이스 테스팅 시 도전 과제

이점에도 불구하고, 인터페이스는 고유한 테스팅 도전 과제를 제시합니다:

1. 테스트하려면 구체적인 구현체가 필요하지만, 이를 만드는 것은 시간이 많이 걸릴 수 있습니다
2. 각 구현체는 서로 다른 설정과 구성이 필요할 수 있습니다
3. 테스트 데이터는 인터페이스 계약의 예상 동작과 일치해야 합니다
4. 구현체별 속성은 커스터마이징이 필요할 수 있습니다

### Fixture Monkey가 어떻게 도움이 되는가

Fixture Monkey는 다음과 같은 방식으로 이러한 과제를 해결합니다:

1. 테스트를 위한 구현체를 자동으로 생성합니다
2. 인터페이스 동작을 커스터마이징하는 유연한 방법을 제공합니다
3. 다양한 테스트 시나리오에 맞는 여러 접근법을 지원합니다
4. 테스트 구현에 필요한 상용구 코드를 줄입니다

## 기본 인터페이스 테스팅 개념

> *이 섹션에서는 간단한 예제부터 시작하여 Fixture Monkey와 함께하는 인터페이스 테스팅의 기본 개념을 소개합니다.*

### 인터페이스 속성 커스터마이징하기

간단한 예제로 시작해보겠습니다. Fixture Monkey를 사용할 때, 일반 클래스와 마찬가지로 인터페이스 속성을 커스터마이징할 수 있습니다:

```java
// 하나의 메서드가 있는 간단한 인터페이스
public interface StringSupplier {
    String getValue(); // 이 메서드는 문자열을 반환합니다
}

// InterfacePlugin을 추가한 Fixture Monkey 인스턴스 생성
// 참고: 모든 인터페이스 작업에는 InterfacePlugin이 필요합니다
FixtureMonkey fixture = FixtureMonkey.builder()
    .plugin(new InterfacePlugin())
    .build();

// StringSupplier 생성 및 커스터마이징
String result = fixture.giveMeBuilder(StringSupplier.class)
    .set("value", "Hello World") // value 속성 설정
    .sample()                    // 인스턴스 생성
    .getValue();                 // 메서드 호출

// result는 "Hello World"가 됩니다
```

이 예제에서 Fixture Monkey는 자동으로 `StringSupplier` 인터페이스의 구현체를 생성하고 `value` 속성을 설정합니다.

### 인터페이스 구현체의 속성 설정하기

Fixture Monkey에서 인터페이스로 작업할 때, 구현된 메서드가 반환할 속성을 설정할 수 있습니다:

```java
// 여러 메서드가 있는 예제 인터페이스
public interface StringProvider {
    String getValue();    // 문자열 값을 가져오는 메서드
    int getNumber();      // 정수 값을 가져오는 메서드
}

// InterfacePlugin을 추가한 Fixture Monkey 인스턴스 생성
FixtureMonkey fixture = FixtureMonkey.builder()
    .plugin(new InterfacePlugin())
    .build();

// 지정된 속성 값으로 인터페이스 구현체 생성
StringProvider provider = fixture.giveMeBuilder(StringProvider.class)
    .set("value", "Hello World")  // getValue()가 반환할 값 설정
    .set("number", 42)            // getNumber()가 반환할 값 설정
    .sample();

// 구현체 사용
String value = provider.getValue();     // "Hello World" 반환
int number = provider.getNumber();      // 42 반환
```

## 인터페이스 구현 접근법

> *이 섹션에서는 가장 간단한 것부터 가장 고급 방법까지, 인터페이스 구현체를 생성하고 작업하는 세 가지 접근법을 배웁니다.*

여러 구현체를 가진 인터페이스로 작업할 때, 가장 간단한 것부터 시작하여, 세 가지 주요 접근법이 있습니다:

### 접근법 1: 익명 구현체 사용하기 (가장 간단한 방법)

간단한 테스트 시나리오의 경우, Fixture Monkey는 인터페이스의 익명 구현체를 자동으로 생성할 수 있습니다. 이 접근법은 다음과 같은 경우에 유용합니다:
- 실제 클래스를 만들지 않고도 테스트를 위한 빠른 구현체가 필요할 때
- 구현 세부사항보다는 테스트 값에 집중하고 싶을 때
- 각 테스트마다 다른 값이 필요할 때

다음은 Fixture Monkey로 자동 익명 구현체를 사용하는 방법입니다:

```java
// 상품 정보 인터페이스
public interface ProductInfo {
    String getName();             // 상품 이름을 가져오는 메서드
    BigDecimal getPrice();        // 상품 가격을 가져오는 메서드
    String getCategory();         // 상품 카테고리를 가져오는 메서드
    boolean isAvailable();        // 상품 재고 여부를 확인하는 메서드
    int getStockQuantity();       // 재고 수량을 가져오는 메서드
}

// InterfacePlugin을 추가한 Fixture Monkey 인스턴스 생성
// 참고: 인터페이스로 작업할 때는 항상 InterfacePlugin이 필요합니다
FixtureMonkey fixture = FixtureMonkey.builder()
    .plugin(new InterfacePlugin())
    .build();

// Fixture Monkey가 익명 구현체를 생성하도록 함
ProductInfo productInfo = fixture.giveMeBuilder(ProductInfo.class)
    .set("name", "Smartphone")                 // 상품 이름 설정
    .set("price", new BigDecimal("999.99"))    // 상품 가격 설정
    .set("category", "Electronics")            // 상품 카테고리 설정
    .set("available", true)                    // 재고 있음으로 설정
    .set("stockQuantity", 10)                  // 재고 수량을 10으로 설정
    .sample();

// 테스트에서 사용
ProductService service = new ProductService();
boolean canPurchase = service.canPurchase(productInfo, 5); // true 반환
String summary = service.getProductSummary(productInfo); 
// "Smartphone (Electronics) - $999.99, 재고: 10" 반환
```

이 접근법의 주요 장점은 Fixture Monkey가 모든 구현 세부사항을 처리한다는 것입니다. 여러분은 인터페이스 메서드가 반환해야 할 값만 정의하면, Fixture Monkey가 내부적으로 적절한 익명 구현체를 생성합니다.

### 접근법 2: Values.just 사용하기

이미 사용하고자 하는 구현체 인스턴스가 있다면, 간단히 `Values.just`를 사용할 수 있습니다. 이는 바로 사용할 수 있는 특정 구현체가 있을 때 유용합니다.

```java
// 온라인 스토어 구현체
public class OnlineProductInfo implements ProductInfo {
    private String name;            // 상품 이름
    private BigDecimal price;       // 상품 가격
    private String category;        // 상품 카테고리 
    private boolean available;      // 상품 가용성
    private int stockQuantity;      // 재고 수량
    
    // 생성자
    public OnlineProductInfo(String name, BigDecimal price, String category, boolean available, int stockQuantity) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.available = available;
        this.stockQuantity = stockQuantity;
    }
    
    // 게터
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

// InterfacePlugin을 추가한 Fixture Monkey 인스턴스 생성
FixtureMonkey fixture = FixtureMonkey.builder()
    .plugin(new InterfacePlugin())
    .build();

// 상품 인스턴스 생성
OnlineProductInfo originalProduct = new OnlineProductInfo(
    "Laptop", 
    new BigDecimal("1999.99"), 
    "Electronics", 
    true, 
    5
);

// Values.just를 사용하여 테스트에서 이 인스턴스 사용
ProductInfo productInfo = fixture.giveMeBuilder(ProductInfo.class)
    .set("$", Values.just(originalProduct))  // 기존 인스턴스 사용
    .sample();

// 테스트에서 사용
ProductService service = new ProductService();
BigDecimal total = service.calculateTotal(productInfo, 2); // 3999.98 반환
```

이 접근법의 주요 이점은 단순함입니다 - 추가 구성이 필요하지 않습니다. 그러나 제한점도 있습니다 - 구현체 속성을 **추가로** 커스터마이징할 수 **없습니다**:

```java
// 이것은 작동하지 않습니다 - Values.just 접근법에서는 속성을 수정할 수 없습니다
OnlineProductInfo product = (OnlineProductInfo)fixture.giveMeBuilder(ProductInfo.class)
    .set("$", Values.just(originalProduct))
    .set("price", new BigDecimal("1499.99")) // 이것은 효과가 없을 것입니다
    .sample();
```

### 접근법 3: interfaceImplements 옵션 사용하기 (가장 유연한 방법)

속성 커스터마이징이 필요한 더 복잡한 시나리오의 경우, `interfaceImplements` 옵션을 사용할 수 있습니다. 이 접근법은 Fixture Monkey에게 인터페이스의 모든 가능한 구현체를 알려주어, 올바른 구현체를 선택하고 커스터마이징할 수 있게 합니다.

#### 1단계: 구현체로 Fixture Monkey 구성하기

```java
// 상품 정보 인터페이스
public interface ProductInfo {
    String getName();
    BigDecimal getPrice();
    String getCategory();
    boolean isAvailable();
    int getStockQuantity();
}

// 온라인 스토어 구현체
public class OnlineProductInfo implements ProductInfo {
    private String name;
    private BigDecimal price;
    private String category;
    private boolean available;
    private int stockQuantity;
    
    // 생성자, 게터, 세터
}

// 추가 속성이 있는 물리적 스토어 구현체
public class StoreProductInfo implements ProductInfo {
    private String name;
    private BigDecimal price;
    private String category;
    private boolean available;
    private int stockQuantity;
    private String storeLocation; // 추가 속성
    
    // 생성자, 게터, 세터
    
    public String getStoreLocation() {
        return storeLocation;
    }
    
    public void setStoreLocation(String storeLocation) {
        this.storeLocation = storeLocation;
    }
}

// 구현체로 Fixture Monkey 구성
FixtureMonkey fixture = FixtureMonkey.builder()
    .plugin(
        new InterfacePlugin()
            .interfaceImplements(
                ProductInfo.class,                                  // 인터페이스
                List.of(OnlineProductInfo.class, StoreProductInfo.class)  // 구현체들
            )
    )
    .build();
```

#### 2단계: 특정 구현체 생성하기

```java
// StoreProductInfo 인스턴스 생성
StoreProductInfo storeProduct = (StoreProductInfo)fixture.giveMeBuilder(ProductInfo.class)
    .set("$", new StoreProductInfo())  // 어떤 구현체를 사용할지 지정
    .sample();

// 이제 구현체별 속성에 접근할 수 있습니다
storeProduct.setStoreLocation("Downtown"); // 구현체별 속성 설정
String location = storeProduct.getStoreLocation(); // "Downtown"
```

#### 3단계: 구현체 속성 커스터마이징하기

interfaceImplements 옵션을 사용하면 구현체의 속성도 수정할 수 있습니다:

```java
// StoreProductInfo 생성 및 커스터마이징
StoreProductInfo product = (StoreProductInfo)fixture.giveMeBuilder(ProductInfo.class)
    .set("$", new StoreProductInfo())              // StoreProductInfo 구현체 사용
    .set("name", "Coffee Maker")                   // 상품 이름 설정
    .set("price", new BigDecimal("89.99"))         // 상품 가격 설정
    .set("category", "Kitchen Appliances")         // 상품 카테고리 설정
    .set("available", true)                        // 재고 있음으로 설정
    .set("stockQuantity", 15)                      // 재고 수량 설정
    .set("storeLocation", "America Mall")          // 구현체별 속성 설정
    .sample();

// 테스트에서 사용
ProductService service = new ProductService();
String summary = service.getProductSummary(product);
// "Coffee Maker (Kitchen Appliances) - $89.99, 재고: 15" 반환

// 구현체별 속성이 설정됨
assertEquals("America Mall", product.getStoreLocation());
```

## 올바른 접근법 선택하기

> *이 가이드는 테스트 요구사항에 따라 어떤 접근법을 선택할지 결정하는 데 도움이 됩니다.*

올바른 접근법을 선택하는 데 도움이 되는 간단한 가이드는 다음과 같습니다:

### 익명 구현체를 사용해야 할 때:
- 테스트를 위한 빠르고 일회성 구현체가 필요할 때
- 테스트만을 위해 전체 클래스를 만들고 싶지 않을 때
- 단일 테스트에 특화된 맞춤 동작이 필요할 때
- 인터페이스 테스팅을 막 시작했을 때

### Values.just를 사용해야 할 때:
- 추가 구성 없이 빠른 솔루션이 필요할 때
- 구현체를 한두 번만 사용할 때
- 이미 구현체의 인스턴스가 있을 때
- 생성 후 속성을 수정할 필요가 없을 때

### interfaceImplements를 사용해야 할 때:
- 구현체 속성을 커스터마이징해야 할 때
- 여러 테스트에 걸쳐 동일한 구현체 세트를 사용할 때
- Fixture Monkey가 구현체들 중에서 무작위로 선택하기를 원할 때
- 더 복잡한 테스트 시나리오를 구축할 때

## 실제 예제: 상품 서비스 테스팅

> *이 섹션은 인터페이스를 사용하는 서비스 테스트의 완전한 예제를 제공합니다.*

실제 테스트 시나리오에서 인터페이스를 사용하는 방법은 다음과 같습니다:

```java
@Test
void testProductService() {
    // Fixture Monkey 구성
    FixtureMonkey fixture = FixtureMonkey.builder()
        .plugin(
            new InterfacePlugin()
                .interfaceImplements(
                    ProductInfo.class,
                    List.of(OnlineProductInfo.class, StoreProductInfo.class)
                )
        )
        .build();
    
    // 특정 속성을 가진 상품 생성
    ProductInfo product = fixture.giveMeBuilder(ProductInfo.class)
        .set("name", "Bluetooth Speaker")         // 상품 이름 설정
        .set("price", new BigDecimal("79.99"))    // 상품 가격 설정
        .set("category", "Audio")                 // 상품 카테고리 설정
        .set("available", true)                   // 재고 있음으로 설정
        .set("stockQuantity", 8)                  // 재고 수량 설정
        .sample();
    
    // 상품 서비스 테스트
    ProductService service = new ProductService();
    
    // 다양한 메서드 테스트
    boolean canPurchase = service.canPurchase(product, 3);
    BigDecimal total = service.calculateTotal(product, 3);
    String summary = service.getProductSummary(product);
    
    // 결과 검증
    assertTrue(canPurchase);
    assertEquals(new BigDecimal("239.97"), total);
    assertEquals("Bluetooth Speaker (Audio) - $79.99, 재고: 8", summary);
}
```

## 일반적인 문제와 해결책

> *이 섹션은 인터페이스 테스팅 시 자주 발생하는 문제를 다룹니다.*

### 문제: 구현체 속성 접근 시 ClassCastException 발생

```java
// 잘못된 타입이 사용되면 ClassCastException이 발생합니다
StoreProductInfo product = (StoreProductInfo)fixture.giveMeBuilder(ProductInfo.class)
    .sample(); // 기본 구현체가 StoreProductInfo가 아닐 수 있습니다
```

**해결책**: 캐스팅이 필요할 때는 항상 구현체를 지정하세요:

```java
// 안전한 접근법 - 명시적으로 구현체 타입 지정
StoreProductInfo product = (StoreProductInfo)fixture.giveMeBuilder(ProductInfo.class)
    .set("$", new StoreProductInfo())  // 명시적으로 구현체 타입 설정
    .sample();
```

### 문제: 속성이 설정되지 않음

**해결책**: 속성을 커스터마이징해야 한다면 interfaceImplements 옵션을 사용하고 있는지 확인하세요:

```java
// 먼저 interfaceImplements로 구성
FixtureMonkey fixture = FixtureMonkey.builder()
    .plugin(
        new InterfacePlugin()
            .interfaceImplements(
                ProductInfo.class,
                List.of(OnlineProductInfo.class, StoreProductInfo.class)
            )
    )
    .build();

// 그런 다음 속성을 커스터마이징할 수 있습니다
StoreProductInfo product = (StoreProductInfo)fixture.giveMeBuilder(ProductInfo.class)
    .set("$", new StoreProductInfo())
    .set("name", "Wireless Earbuds") // 이제 작동합니다
    .set("storeLocation", "City Center") // 이제 작동합니다
    .sample();
```

## 요약

- 인터페이스 테스팅은 의존성 주입과 인터페이스를 사용하는 실제 애플리케이션에서 흔합니다
- 빠른 테스트를 위해 간단한 익명 구현체부터 시작하세요
- 특정 구현체를 이미 가지고 있다면 Values.just를 사용하세요
- 구현체 속성을 커스터마이징해야 한다면 interfaceImplements를 사용하세요
- 구현체별 속성에 접근해야 한다면 항상 구현체를 지정하세요

