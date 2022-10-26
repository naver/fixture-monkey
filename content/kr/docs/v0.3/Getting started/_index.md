---
title: "시작해보기"
linkTitle: "시작해보기"
weight: 2
---

## 환경
* JDK 8+
* JUnit 5 platform
* jqwik 1.3.9

## 설치
### Gradle 
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter:0.3.1")
```


### Maven
```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-starter</artifactId>
  <version>0.3.1</version>
  <scope>test</scope>
</dependency>
```

## 간단한 예제

{{< alert color="primary" title="Tip">}}
게터와 세터는 필수가 아닙니다.
Fixture Monkey를 사용할 때 프로덕션 코드를 변경하지 않아도 됩니다. 객체마다 다른 생성 방식을 사용할 수 있습니다.
예를 들어, 불변한 객체라면 정의 방법에 따라 [BuilderArbitraryGenerator]({{< relref "/docs/v0.3/features/arbitrarygenerator#builderarbitrarygenerator" >}}) 나 [JacksonArbitraryGenerator]({{< relref "/docs/v0.3/features/arbitrarygenerator#jacksonarbitrarygenerator" >}})를 사용하시면 됩니다. 

자세한 사항은 [여기]({{< relref "/docs/v0.3/features/arbitrarygenerator" >}})를 참조해주세요.
{{< /alert >}}
```java
@Data   // lombok getter, setter
public class Order {
    @NotNull
    private Long id;

    @NotBlank
    private String orderNo;

    @Size(min = 2, max = 10)
    private String productName;

    @Min(1)
    @Max(100)
    private int quantity;

    @Min(0)
    private long price;

    @Size(max = 3)
    private List<@NotBlank @Size(max = 10) String> items = new ArrayList<>();

    @PastOrPresent
    private Instant orderedAt;
}

@Test
void test() {
    // given
    FixtureMonkey sut = FixtureMonkey.create();

    // when
    Order actual = sut.giveMeOne(Order.class);

    // then
    then(actual.getId()).isNotNull(); // @NotNull
	then(actual.getOrderNo()).isNotBlank(); // @NotBlank
	then(actual.getProductName().length()).isBetween(2, 10); // @Size(min = 2, max = 10)
	then(actual.getQuantity()).isBetween(1, 100); // @Min(1) @Max(100)
	then(actual.getPrice()).isGreaterThanOrEqualTo(0); // @Min(0)
	then(actual.getItems()).hasSizeLessThan(3); // @Size(max = 3)
	then(actual.getItems()).allMatch(it -> it.length() <= 10); // @NotBlank @Size(max = 10)
    then(actual.getOrderedAt()).isBeforeOrEqualTo(Instant.now()); // @PastOrPresent
}
```

## 더 알아두면 좋은 정보들
* [예제]({{< relref "/docs/v0.3/examples" >}})
* [기본적으로 생성해주는 객체 타입]({{< relref "/docs/v0.3/features/defaultsupportedtypes" >}})
* [ArbitraryBuilder]({{< relref "/docs/v0.3/features/arbitrarybuilder" >}})
* [Manipulator]({{< relref "/docs/v0.3/features/manipulator" >}})
* [FAQ]({{< relref "/docs/v0.3/faq" >}})
