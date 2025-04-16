---
title: "사용자 정의 객체 생성하기"
weight: 26
menu:
docs:
parent: "get-started"
identifier: "customizing objects"
---

Fixture Monkey를 사용하면 테스트 요구사항에 맞게 테스트 객체를 커스터마이즈할 수 있습니다. 실제 예제를 통해 살펴보겠습니다.

## 왜 테스트 객체를 커스터마이즈해야 할까요?

예를 들어, 1000원 이상인 상품에만 10% 할인을 적용하는 서비스를 테스트한다고 가정해봅시다. 두 가지 시나리오를 테스트해야 합니다:
- 할인이 적용되어야 하는 상품 (가격 > 1000원)
- 할인이 적용되지 않아야 하는 상품 (가격 ≤ 1000원)

Fixture Monkey를 사용하지 않는다면 다음과 같이 코드를 작성해야 합니다:
```java
// Fixture Monkey 없이
Product expensiveProduct = new Product(1, "고가 상품", 2000, ...);
Product cheapProduct = new Product(2, "저가 상품", 500, ...);
```

Fixture Monkey를 사용하면 이러한 테스트 객체를 더 쉽고 유연하게 생성할 수 있습니다.

## 단계별 가이드

먼저 간단한 Product 클래스를 살펴보겠습니다:

```java
@Value
public class Product {
    long id;
    String productName;
    long price;
    List<String> options;
    Instant createdAt;
}
```

### 1단계: FixtureMonkey 인스턴스 생성하기
먼저 적절한 introspector를 사용하여 FixtureMonkey 인스턴스를 생성합니다:

{{< tabpane >}}
{{< tab header="Java" >}}
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
    .build();
{{< /tab >}}
{{< tab header="Kotlin" >}}
val fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
    .build()
{{< /tab >}}
{{< /tabpane >}}

### 2단계: 특정 가격을 가진 Product 생성하기
이제 할인 테스트를 위해 가격이 2000원인 상품을 생성해보겠습니다:

{{< tabpane >}}
{{< tab header="Java" >}}
@Test
void testDiscountApplied() {
    // given
    Product expensiveProduct = fixtureMonkey.giveMeBuilder(Product.class)
        .set("price", 2000L)    // 가격을 2000으로 설정
        .sample();

    // when
    double discount = discountService.calculateDiscount(expensiveProduct);

    // then
    then(discount).isEqualTo(200.0);  // 2000의 10%
}
{{< /tab >}}
{{< tab header="Kotlin" >}}
@Test
fun testDiscountApplied() {
    // given
    val expensiveProduct = fixtureMonkey.giveMeBuilder(Product::class.java)
        .set("price", 2000L)    // 가격을 2000으로 설정
        .sample()

    // when
    val discount = discountService.calculateDiscount(expensiveProduct)

    // then
    then(discount).isEqualTo(200.0)  // 2000의 10%
}
{{< /tab >}}
{{< /tabpane >}}

### 3단계: 커스터마이즈된 리스트를 가진 Product 생성하기
컬렉션도 커스터마이즈할 수 있습니다. 예를 들어, 특정 옵션을 가진 상품을 테스트하려면:

{{< tabpane >}}
{{< tab header="Java" >}}
@Test
void testProductWithOptions() {
    // given
    Product actual = fixtureMonkey.giveMeBuilder(Product.class)
        .size("options", 3)          // 리스트 크기를 3으로 설정
        .set("options[1]", "red")    // 두 번째 요소를 "red"로 설정
        .sample();

    // then
    then(actual.getOptions()).hasSize(3);
    then(actual.getOptions().get(1)).isEqualTo("red");
}
{{< /tab >}}
{{< tab header="Kotlin" >}}
@Test
fun testProductWithOptions() {
    // given
    val actual = fixtureMonkey.giveMeBuilder(Product::class.java)
        .size("options", 3)          // 리스트 크기를 3으로 설정
        .set("options[1]", "red")    // 두 번째 요소를 "red"로 설정
        .sample()

    // then
    then(actual.options).hasSize(3)
    then(actual.options[1]).isEqualTo("red")
}
{{< /tab >}}
{{< /tabpane >}}

생성된 Product는 다음과 같이 보일 것입니다:
```java
Product(
    id=42,                          // 임의의 값
    productName="product-value-1",  // 임의의 값
    price=1000,                     // 임의의 값
    options=["option1", "red", "option3"], // 커스터마이즈된 리스트
    createdAt=2024-03-21T10:15:30Z // 임의의 값
)
```

## 주의사항과 팁

1. **필드 이름**
   - 클래스에 정의된 필드 이름을 정확히 사용해야 합니다
   - 잘못된 예: `set("product_name", "test")` (필드 이름 불일치)
   - 올바른 예: `set("productName", "test")`
   - 팁: IDE의 코드 완성 기능을 사용하여 필드 이름 오타를 방지하세요
   - 팁: 타입 안전한 필드 접근을 위해 `setExp` 또는 `setExpGetter`를 사용하세요
   - 팁: 향상된 코드 완성과 타입 안전성을 위해 [Fixture Monkey Helper](https://plugins.jetbrains.com/plugin/19589-fixture-monkey-helper)를 설치하세요

{{< tabpane >}}
{{< tab header="Java" >}}
// 타입 안전한 필드 접근
.set(javaGetter(Product::getProductName), "test")
{{< /tab >}}
{{< tab header="Kotlin" >}}
// 타입 안전한 필드 접근
.setExp(Product::productName, "test")
// 또는
.setExpGetter(Product::productName, { "test" })
{{< /tab >}}
{{< /tabpane >}}

2. **컬렉션 인덱싱**
   - 리스트 인덱스는 0부터 시작한다는 것을 기억하세요
   - 잘못된 예: `set("options[3]", "red")` (크기가 3인 리스트의 경우)
   - 올바른 예: `set("options[2]", "red")`
   - 팁: 특정 인덱스를 설정하기 전에 `size()`를 사용하여 리스트 크기를 먼저 설정하세요

3. **타입 안전성**
   - 값의 타입을 올바르게 사용해야 합니다
   - 잘못된 예: `set("price", "1000")` (String 대신 Long 사용)
   - 올바른 예: `set("price", 1000L)`
   - 팁: IDE의 타입 힌트를 활용하여 올바른 값 타입을 사용하세요

## Fixture Monkey 사용 전후 비교

Fixture Monkey 사용 전:
```java
// 특정 옵션을 가진 상품 생성
List<String> options = new ArrayList<>();
options.add("option1");
options.add("red");
options.add("option3");
Product product = new Product(1, "테스트 상품", 1000, options, Instant.now());
```

Fixture Monkey 사용 후:
```java
// 같은 결과를 더 적은 코드로 생성
Product product = fixtureMonkey.giveMeBuilder(Product.class)
    .size("options", 3)
    .set("options[1]", "red")
    .sample();
```

프로퍼티 선택과 값 설정에 대한 더 많은 예제는 [커스터마이징 섹션](../../customizing-objects/apis)을 참고하세요.
