---
title: "Lombok 없이 테스트 객체 생성하기"
sidebar_position: 23
---


:::tip
만약 프로젝트에서 Lombok 을 사용하고 있다면 다음 페이지로 넘어가주세요.
:::

Fixture Monkey를 사용하면 Lombok이 없어도 테스트 객체를 아주 쉽게 생성할 수 있습니다. 다음과 같이 사용해보세요:

```java
@Test
void test() {
    // given
    FixtureMonkey fixtureMonkey = FixtureMonkey.create();

    // when
    Product actual = fixtureMonkey.giveMeOne(Product.class);

    // then
    then(actual).isNotNull();
}
```

이 코드를 실행하면, Fixture Monkey는 임의의 값을 가진 Product 인스턴스를 생성합니다.
아래는 예시일 뿐이며, 실제로는 매번 다른 임의의 값들이 생성됩니다:

```java
Product(
    id=42,
    productName="product-value-1",
    price=1000,
    options=["option1", "option2"],
    createdAt=2024-03-21T10:15:30Z,
    productType=ELECTRONICS,
    merchantInfo={1="merchant1", 2="merchant2"}
)
```

먼저, 테스트 픽스처를 쉽게 만들 수 있는 FixtureMonkey 인스턴스를 생성합니다. `create()`를 사용하면 기본 옵션이 설정된 Fixture Monkey 인스턴스가 생성됩니다.
Fixture Monkey에는 여러 사용자 정의 옵션이 있어서 특정 요구 사항을 만족하는 인스턴스를 생성할 수 있습니다.

Fixture Monkey는 객체를 생성하기 위한 기본 방법으로 `BeanArbitraryIntrospector`를 사용합니다.
`Introspector`는 Fixture Monkey가 객체를 생성하는 방법을 정의합니다.

예를 들어, 다음과 같은 Product 클래스가 있다고 할 때:

```java
public class Product {
    private long id;
    private String productName;
    private long price;
    private List<String> options;
    private Instant createdAt;
    private ProductType productType;
    private Map<Integer, String> merchantInfo;

    public Product() {
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }

    public void setMerchantInfo(Map<Integer, String> merchantInfo) {
        this.merchantInfo = merchantInfo;
    }
}
```

`BeanArbitraryIntrospector`를 사용하려면, 위의 Product 클래스처럼 생성될 클래스에 기본 생성자(no-args constructor)와 setter 메서드들이 있어야 합니다.
이 introspector는 기본 생성자로 인스턴스를 생성한 후, setter 메서드들을 사용하여 임의의 값들을 설정합니다.
(다른 Introspector를 사용할 수도 있습니다. 각각의 요구 사항은 [`Introspectors` section](../generating-objects/introspector)을 참고하세요.)

