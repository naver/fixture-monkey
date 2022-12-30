---
title: "fixture-monkey-jackson"
weight: 1
---

## 기능
[Jackson](https://github.com/FasterXML/jackson)의 ObjectMapper를 활용해 객체 생성을 할 수 있도록 플러그인을 지원합니다.
- JacksonPlugin 을 사용할 때 사용할 ObjectMapper를 입력할 수 있습니다.
- `@JsonIgnore`, `@JsonProperty` 어노테이션을 지원합니다.


## 설정
### 1. 의존성 추가
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-jackson:{{< param version >}}")
```

```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-jackson</artifactId>
  <version>{{< param version >}}</version>
  <scope>test</scope>
</dependency>
```

### 2. 옵션 변경
```java
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
    .plugin(new JacksonPlugin(objectMapper))
    .build();
```

## 사용 예시
```java
@Data   // lombok getter, setter
public class Order {
    @NotNull
    private Long id;

    @JsonProperty("name")
    private String productName;

    private int quantity;
    
    @JsonIgnore
    private String sample;
}

@Test
void test() {
    // given
    LabMonkey sut = LabMonkey.labMonkeyBuilder()
        .plugin(new JacksonPlugin(objectMapper))
        .build();

    // when
    Order actual = sut.giveMeBuilder(Order.class)
        .set("name", "factory")
        .set("quantity", Arbitraries.integers().between(5, 10))
        .sample();

    // then
    then(actual.getId()).isNotNull();    // @NotNull
    then(actual.getProductName()).isEqualTo("factory");
    then(actual.getQuantity()).isBetween(5, 10);
    then(actual.getSample()).isNull();  // @JsonIgnore
}
```
