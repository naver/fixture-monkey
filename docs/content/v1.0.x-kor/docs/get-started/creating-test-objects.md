---
title: "테스트 객체 생성하기"
weight: 22
menu:
docs:
  parent: "get-started"
  identifier: "creating-test-objects"
---

> Fixture Monkey 는 Java 와 Kotlin 모두에서 사용할 수 있습니다.
> 각 환경에 맞는 '시작하기' 페이지가 있습니다: [Java](../creating-test-objects), [Java without Lombok](../creating-test-objects-without-lombok), [Kotlin](../creating-objects-in-kotlin).
> 
> 이 페이지는 Java 환경을 기준으로 설명합니다. 사용 중인 환경에 맞는 페이지를 참고해주세요.

아래처럼 Product 클래스를 테스트하기 위해 테스트 픽스처가 필요한 시나리오를 생각해보세요.

{{< alert icon="💡" text="lombok.anyConstructor.addConstructorProperties=true 가 lombok.config 파일에 추가되어 있어야 합니다." />}}

```java
@Value
public class Product {
    long id;

    String productName;

    long price;

    List<String> options;

    Instant createdAt;

    ProductType productType;

    Map<Integer, String> merchantInfo;
}
```

(Lombok 의 어노테이션인 `@Value` 는 불변 클래스를 만들기 위해 사용됩니다. 만약 Lombok 을 사용하지 않는다면, [Lombok 없이 테스트 객체 생성하기](../creating-test-objects-without-lombok) 으로 이동하세요.)

Fixture Monkey 를 사용하면, 단 몇 줄의 코드만으로도 Product 인스턴스를 생성할 수 있습니다.

```java
@Test
void test() {
    // given
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
        .build();

    // when
    Product actual = fixtureMonkey.giveMeOne(Product.class);

    // then
    then(actual).isNotNull();
}
```

먼저, 테스트 픽스처를 쉽게 만들 수 있는 FixtureMonkey 인스턴스를 생성합니다.
Fixture Monkey 에는 여러 사용자 정의 옵션이 있어서 특정 요구 사항을 만족하는 인스턴스를 생성할 수 있습니다.

여기서는 `objectIntrospector` 를 `ConstructorPropertiesArbitraryIntrospector` 로 설정했습니다. 이는 @ConstructorProperties 어노테이션이 달린 생성자를 사용하여 객체를 생성한다는 것을 의미합니다.
`Introspector` 는 Fixture Monkey 가 객체를 생성하는 방법을 정의합니다.

`ConstructorPropertiesArbitraryIntrospector` 를 사용하려면, 생성될 클래스에는 @ConstructorProperties 가 달린 생성자가 있거나, lombok.config 파일에 `lombok.anyConstructor.addConstructorProperties=true` 가 추가되어 있어야 합니다.
(다른 Introspector를 사용할 수도 있습니다. 각각의 요구 사항은 [`Introspectors` section](../../generating-objects/introspector) 을 참고하세요.)

다음으로, `giveMeOne()` 메서드를 사용하여 지정된 타입의 인스턴스를 생성합니다.

then 절에서 확인할 수 있듯이, Product 클래스의 인스턴스가 생성됩니다.
