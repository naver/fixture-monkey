---
title: "사용자 정의 객체 생성하기"
weight: 25
menu:
docs:
parent: "get-started"
identifier: "customizing-objects"
---

특정 단위 테스트에 맞게 테스트 픽스처를 조정해야할 수 있습니다.
이런 경우에는 Fixture Monkey 를 사용하여 빌더를 생성하고 특정 명세를 추가할 수 있습니다.

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

예를 들면, 특정 테스트에서 id 가 1,000 인 Product 인스턴스가 필요할 수 있습니다.

이를 위해 `giveMeBuilder` 메서드를 사용하여 픽스처 몽키에서 타입 빌더를 가져올 수 있습니다.
빌더를 사용하면 추가 메서드 호출을 연결하여 픽스처를 사용자 정의할 수 있습니다.
위 예시의 경우에는 `set()` 함수를 사용하여 id를 1,000으로 설정하고, `sample()`을 사용하여 인스턴스를 생성해줄 수 있습니다.

```java
@Test
void test() {
    // given
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
        .build();
    long id = 1000;

    // when
    Product actual = fixtureMonkey.giveMeBuilder(Product.class)
        .set("id", id)
        .sample();

    // then
    then(actual.getId()).isEqualTo(1000);
}
```

위의 코드의 검증문에서 필드 `id`가 원하는 값으로 설정되어 있는 것을 볼 수 있습니다.

컬렉션을 사용하는 경우에도 Fixture Monkey 를 사용할 수 있습니다.
예를 들어, "options" 리스트가 특정 크기를 가져야 하고, 리스트의 특정 요소가 특정 값을 가져야 할 수 있습니다.

```java
@Test
void test() {
    // given
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
        .build();

    // when
    Product actual = fixtureMonkey.giveMeBuilder(Product.class)
        .size("options", 3)
        .set("options[1]", "red")
        .sample();

    // then
    then(actual.getOptions()).hasSize(3);
    then(actual.getOptions().get(1)).isEqualTo("red");
}
```

`set()` 함수를 사용하여 특정 컬렉션 (list, set, map)의 크기를 지정하고 요소를 특정 값으로 설정한 다음,
`sample()` 메서드를 호출하여 인스턴스를 생성할 수 있습니다.

표현식을 사용하여 프로퍼티를 선택하고 프로퍼티 값을 설정하는 방법에 대한 자세한 예제는 [커스터마이징 항목](../../customizing-objects/apis) 을 참고하세요.
