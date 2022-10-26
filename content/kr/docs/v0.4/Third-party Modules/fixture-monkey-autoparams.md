---
title: "fixture-monkey-autoparams"
weight: 6
---
{{< alert color="warning" title="Warning">}}
해당 모듈은 아직 0.4 버젼의 LabMonkey를 지원하지 않습니다.
{{< /alert >}}

### 기능
테스트 파라미터에 대해 객체를 생성해주는 [AutoParams](https://github.com/AutoParams/AutoParams)의 ParameterizedTest 지원 기능을 확장합니다.  
AutoParams의 `@AutoSource` 대신 `@FixtureMonkeyAutoSource`를 사용할 수 있습니다.

### 설정
#### 1. 의존성 추가
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-autoparams:0.4.2")
```

```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-autoparams</artifactId>
  <version>0.4.2</version>
  <scope>test</scope>
</dependency>
```

### 사용 예시
```java
@Data   // lombok getter, setter
public class Order {
	@NotNull
    private Long id;

    private String productName;

	private int quantity;

	@Nullable
	private String sample;
}

@ParameterizedTest
@FixtureMonkeyAutoSource
void test(Order order, ArbitraryBuilder<Order> orderBuilder) {
    then(order).isNotNull();

    Order actual = orderBuilder
		.generator(JacksonArbitraryGenerator.INSTANCE)
        .set("name", "factory")
        .set("quantity", Arbitraries.integers().between(5, 10))
        .sample();

    // then
    then(actual.getId()).isNotNull();    // @NotNull
    then(actual.getProductName()).isEqualTo("factory");
    then(actual.getQuantity()).isBetween(5, 10);
}
```