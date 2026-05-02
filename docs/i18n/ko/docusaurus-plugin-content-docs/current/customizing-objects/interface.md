---
title: "인터페이스 테스트하기"
sidebar_position: 44
---


## 이 문서에서 배울 내용
- 인터페이스 인스턴스의 속성을 커스터마이징하는 방법
- 다양한 인터페이스 커스터마이징 접근법 중 선택하는 방법
- 구현체별 속성을 다루는 방법

:::tip
Fixture Monkey가 **인터페이스 타입을 생성하는 방법**(InterfacePlugin 설정, 제네릭 인터페이스, sealed 인터페이스, 고급 해석 등)에 대한 종합 가이드는 [인터페이스 타입 생성하기](../generating-objects/generating-interface)를 참조하세요.

이 문서는 인터페이스 인스턴스가 생성된 후 **커스터마이징**하는 방법에 초점을 맞춥니다.
:::

## 빠른 시작: 인터페이스 속성 커스터마이징

Fixture Monkey를 사용하면 일반 클래스와 마찬가지로 인터페이스 속성을 커스터마이징할 수 있습니다:

```java
// 하나의 메서드가 있는 간단한 인터페이스
public interface StringSupplier {
	String getValue(); // 이 메서드는 문자열을 반환합니다
}

// Fixture Monkey 인스턴스 생성
FixtureMonkey fixture = FixtureMonkey.create();

// StringSupplier 생성 및 커스터마이징
String result = fixture.giveMeBuilder(StringSupplier.class)
	.set("value", "Hello World") // value 속성 설정
    .sample()                    // 인스턴스 생성
	.getValue();                 // 메서드 호출

// result는 "Hello World"가 됩니다
```

Fixture Monkey는 자동으로 익명 구현체를 생성하고 `value` 속성을 설정합니다. getter 메서드가 반환할 속성을 설정할 수 있습니다:

```java
public interface StringProvider {
    String getValue();    // 문자열 값을 가져오는 메서드
    int getNumber();      // 정수 값을 가져오는 메서드
}

StringProvider provider = fixture.giveMeBuilder(StringProvider.class)
    .set("value", "Hello World")  // getValue()가 반환할 값 설정
    .set("number", 42)            // getNumber()가 반환할 값 설정
    .sample();

String value = provider.getValue();     // "Hello World" 반환
int number = provider.getNumber();      // 42 반환
```

## 커스터마이징 접근법

인터페이스로 작업할 때, 생성된 인스턴스를 커스터마이징하는 세 가지 접근법이 있습니다:

| 접근법 | 설명 | 속성 커스터마이징 가능? |
|--------|------|----------------------|
| **익명 구현체** | Fixture Monkey가 익명 클래스를 생성 | 가능, `set()` 사용 |
| **Values.just** | 기존 구현체 인스턴스를 그대로 사용 | 불가능 |
| **interfaceImplements** | 구현체를 등록하고 자유롭게 커스터마이징 | 가능, 구현체별 속성 포함 |

### 접근법 1: 익명 구현체 (가장 간단)

Fixture Monkey가 익명 구현체를 생성하도록 하고 `set()`으로 커스터마이징합니다:

```java
// 상품 정보 인터페이스
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

가장 간단한 접근법입니다. Fixture Monkey가 모든 구현 세부사항을 처리합니다.

### 접근법 2: Values.just 사용

이미 사용할 구현체 인스턴스가 있다면, `Values.just`로 그대로 사용할 수 있습니다:

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
    .set("$", Values.just(originalProduct))  // 기존 인스턴스 사용
    .sample();
```

:::danger
`Values.just` 사용 후에는 속성을 추가로 커스터마이징할 수 **없습니다**:

```java
// 작동하지 않음 - set("price", ...)는 효과가 없습니다
ProductInfo product = fixture.giveMeBuilder(ProductInfo.class)
    .set("$", Values.just(originalProduct))
    .set("price", new BigDecimal("1499.99")) // 효과 없음!
    .sample();
```
:::

### 접근법 3: interfaceImplements 사용 (가장 유연)

구현체별 속성을 커스터마이징해야 하는 경우, `InterfacePlugin`을 통해 구현체를 등록합니다:

```java
public class StoreProductInfo implements ProductInfo {
    private String name;
    private BigDecimal price;
    private String category;
    private boolean available;
    private int stockQuantity;
    private String storeLocation; // 구현체별 속성

    // 생성자, getter, setter
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

// 인터페이스 속성과 구현체별 속성 모두 커스터마이징
StoreProductInfo product = (StoreProductInfo) fixture.giveMeBuilder(ProductInfo.class)
    .set("$", new StoreProductInfo())              // 구현체 지정
    .set("name", "Coffee Maker")                   // 인터페이스 속성
    .set("price", new BigDecimal("89.99"))         // 인터페이스 속성
    .set("storeLocation", "America Mall")          // 구현체별 속성
	.sample();
```

`InterfacePlugin`과 `interfaceImplements` 설정에 대한 자세한 내용은 [인터페이스 타입 생성하기](../generating-objects/generating-interface)를 참조하세요.

## 올바른 접근법 선택하기

| 시나리오 | 권장 접근법 |
|---------|-----------|
| 빠른 테스트, 구현 세부사항 무관 | 익명 구현체 |
| 이미 인스턴스가 있고 추가 커스터마이징 불필요 | Values.just |
| 구현체별 속성을 커스터마이징해야 할 때 | interfaceImplements |
| Fixture Monkey가 구현체를 무작위 선택하도록 할 때 | interfaceImplements |
| 여러 테스트에서 동일한 구현체를 재사용할 때 | interfaceImplements |

## 일반적인 문제와 해결책

### 문제: 캐스팅 시 ClassCastException 발생

```java
// 실패할 수 있음 - 기본 구현체가 StoreProductInfo가 아닐 수 있음
StoreProductInfo product = (StoreProductInfo) fixture.giveMeBuilder(ProductInfo.class)
    .sample();
```

**해결책**: 캐스팅 시 항상 구현체를 지정하세요:

```java
StoreProductInfo product = (StoreProductInfo) fixture.giveMeBuilder(ProductInfo.class)
    .set("$", new StoreProductInfo())
	.sample();
```

### 문제: 속성이 설정되지 않음

**해결책**: 특정 구현체의 속성을 커스터마이징해야 한다면 `interfaceImplements`를 사용하세요:

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
    .set("name", "Wireless Earbuds")     // 정상 작동
    .set("storeLocation", "City Center") // 정상 작동
	.sample();
```

## 요약

- `set()`으로 일반 클래스처럼 인터페이스 속성을 커스터마이징할 수 있습니다
- 간단하고 빠른 테스트에는 **익명 구현체**를 사용하세요
- 기존 인스턴스가 있을 때는 **Values.just**를 사용하세요 (추가 커스터마이징 불가)
- 구현체별 속성을 다뤄야 할 때는 **interfaceImplements**를 사용하세요
- 인터페이스 생성 전략과 고급 설정은 [인터페이스 타입 생성하기](../generating-objects/generating-interface)를 참조하세요
