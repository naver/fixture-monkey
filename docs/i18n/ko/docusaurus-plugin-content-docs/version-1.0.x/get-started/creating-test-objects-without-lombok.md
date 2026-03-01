---
title: "Lombok 없이 테스트 객체 생성하기"
sidebar_position: 23
---


:::tip
만약 프로젝트에서 Lombok 을 사용하고 있다면 다음 페이지로 넘어가주세요.
:::

아래처럼 Product 클래스를 테스트하기 위해 테스트 픽스처가 필요한 시나리오를 생각해보세요.

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

Fixture Monkey 를 사용하면, 단 몇 줄의 코드만으로도 Product 인스턴스를 생성할 수 있습니다.

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

먼저, 테스트 픽스처를 쉽게 만들 수 있는 FixtureMonkey 인스턴스를 생성합니다.
Fixture Monkey 에는 여러 사용자 정의 옵션이 있어서 특정 요구 사항을 만족하는 인스턴스를 생성할 수 있습니다.

Fixture Monkey 는 객체를 생성하기 위한 기본 방법으로 `BeanArbitraryIntrospector` 를 사용합니다.
`Introspector` 는 Fixture Monkey 가 객체를 생성하는 방법을 정의합니다.

`BeanArbitraryIntrospector` 를 사용하려면, 생성될 클래스에는 no-args 생성자와 setter 가 있어야 합니다.
(다른 Introspector를 사용할 수도 있습니다. 각각의 요구 사항은 [`Introspectors` section](../generating-objects/introspector) 을 참고하세요.)

다음으로, `giveMeOne()` 메서드를 사용하여 지정된 타입의 인스턴스를 생성합니다.

then 절에서 확인할 수 있듯이, Product 클래스의 인스턴스가 생성됩니다.

