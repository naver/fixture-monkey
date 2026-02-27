---
title: "초보자를 위한 팁"
sidebar_position: 27
---


## Fixture Monkey 사용을 위한 필수 팁

### 1. 타입 안전한 메서드 사용하기
- 문자열 기반 메서드보다 타입 안전한 메서드를 선호하세요
- 예시:
```java
// 이렇게 하지 말고
.set("price", 1000L)

// 이렇게 사용하세요
.set(javaGetter(Product::getPrice), 1000L)
```

### 2. 의미 있는 테스트 데이터 사용하기
- 테스트 맥락에서 의미가 있는 값을 사용하세요
- "test"나 "123"과 같은 임의의 값을 피하세요
- 비즈니스 규칙과 제약 조건을 고려하여 값을 설정하세요
- 장점:
  - 테스트가 더 읽기 쉽고 자체 문서화됩니다
  - 테스트 실패를 더 빠르게 파악할 수 있습니다
  - 테스트 시나리오를 이해하기 쉬워집니다
  - 추가 주석이 필요 없어집니다
- 예시:
```java
// 의미 있는 값 사용
Product product = fixtureMonkey.giveMeBuilder(Product.class)
    .set("price", 1000L)    // 비즈니스 규칙에 맞는 실제적인 가격 사용
    .set("name", "프리미엄 상품")  // 상품 유형을 나타내는 설명적인 이름 사용
    .set("category", "ELECTRONICS")  // 도메인에서 유효한 카테고리 사용
    .set("stock", 50)       // 적절한 재고 수량 사용
    .sample();
```

### 3. 테스트 가독성 유지하기
- 특정 값을 설정하는 이유를 주석으로 설명하세요
- 예시:
```java
Product product = fixtureMonkey.giveMeBuilder(Product.class)
    .set("price", 2000L)    // 할인 기준 이상의 가격
    .set("category", "PREMIUM")  // 특별한 처리가 필요한 카테고리
    .sample();
```

### 4. 컬렉션 올바르게 다루기
- 특정 인덱스를 접근하기 전에 컬렉션 크기를 먼저 설정하세요
- 예시:
```java
Product product = fixtureMonkey.giveMeBuilder(Product.class)
    .size("options", 3)          // 먼저 크기 설정
    .set("options[1]", "red")    // 그 다음 특정 인덱스 접근
    .sample();
```

### 5. FixtureMonkey 인스턴스 재사용하기
- 하나의 인스턴스를 생성하고 여러 테스트에서 재사용하세요
- 예시:
```java
public class ProductTest {
    private static final FixtureMonkey FIXTURE_MONKEY = FixtureMonkey.builder()
        .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
        .build();

    @Test
    void test1() {
        Product product = FIXTURE_MONKEY.giveMeBuilder(Product.class).sample();
        // ...
    }

    @Test
    void test2() {
        Product product = FIXTURE_MONKEY.giveMeBuilder(Product.class).sample();
        // ...
    }
}
```

### 6. ArbitraryBuilder 재사용하기
- ArbitraryBuilder 인스턴스를 재사용하여 테스트 데이터 구조의 일관성을 유지하세요
- 여러 테스트에서 공통 설정을 공유할 수 있습니다
- 테스트 데이터 설정을 중앙화하여 코드 가독성을 향상시킬 수 있습니다
- 예시:
```java
public class ProductTest {
    private static final FixtureMonkey FIXTURE_MONKEY = FixtureMonkey.builder()
        .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
        .build();
    
    // 프리미엄 상품에 대한 기본 설정
    private static final ArbitraryBuilder<Product> PREMIUM_PRODUCT_BUILDER = FIXTURE_MONKEY.giveMeBuilder(Product.class)
        .set("category", "PREMIUM")
        .set("price", 1000L);

    @Test
    void testDiscountForPremiumProduct() {
        // 할인 기준 이상의 가격으로 프리미엄 상품 테스트
        Product product = PREMIUM_PRODUCT_BUILDER
            .set("price", 2000L)  // 할인 기준 이상의 가격
            .sample();
        // 할인 로직 테스트
    }

    @Test
    void testShippingForPremiumProduct() {
        // 무료 배송 기준 이상의 가격으로 프리미엄 상품 테스트
        Product product = PREMIUM_PRODUCT_BUILDER
            .set("price", 5000L)  // 무료 배송 기준 이상의 가격
            .sample();
        // 배송 로직 테스트
    }
}
```

### 7. 간단한 객체부터 시작하기
- 복잡한 객체로 넘어가기 전에 기본적인 객체부터 시작하세요
- 예시:
```java
public class ProductTest {
    private static final FixtureMonkey FIXTURE_MONKEY = FixtureMonkey.builder()
        .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
        .build();

    @Test
    void testBasicProduct() {
        // 간단한 객체부터 시작
        Product product = FIXTURE_MONKEY.giveMeBuilder(Product.class)
            .set("name", "테스트 상품")
            .sample();
        // ...
    }
}
```

### 8. IntelliJ 플러그인 사용하기
[Fixture Monkey Helper](https://plugins.jetbrains.com/plugin/19589-fixture-monkey-helper) 플러그인을 설치하여 개발 경험을 향상시키세요:
- Fixture Monkey 메서드에 대한 스마트 코드 완성
- 메서드 참조를 사용한 타입 안전한 필드 접근 제안
- 필드 정의로 빠르게 이동
- Fixture Monkey 클래스에 대한 자동 import 제안
- 필드 이름과 타입에 대한 실시간 검증

### 9. 일반적인 사용 사례
- 검증 규칙 테스트
- 특정 조건의 비즈니스 로직 테스트
- 통합 테스트를 위한 테스트 데이터 생성
- 유효한 랜덤 테스트 데이터 생성

### 10. 모범 사례
- 테스트 데이터 생성은 사용하는 곳 가까이에 두세요
- 의미 있는 변수명을 사용하세요
- 복잡한 테스트 시나리오를 문서화하세요
- 자주 사용하는 값은 상수로 정의하세요

