---
title: "fixture-monkey-mockito"
weight: 4
---
## 기능
인터페이스와 추상 클래스를 [Mockito](https://site.mockito.org/) 객체로 생성할 수 있도록 플러그인을 지원합니다.

## 설정
### 1. 의존성 추가
```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-mockito:{{< param version >}}")
```

```xml
<dependency>
  <groupId>com.navercorp.fixturemonkey</groupId>
  <artifactId>fixture-monkey-mockito</artifactId>
  <version>{{< param version >}}</version>
  <scope>test</scope>
</dependency>
```

### 2. 옵션 변경
```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .plugin(new MockitoPlugin())
    .build();
```

## 사용 예시
```java
@Data   // lombok getter, setter
public class Order {
    @NotNull
    private Long id;
    
    private String productName;

    private int quantity;
    
    @NotNull
    private Item item;
}

public interface Item {
    String getName();
}

@Test
void test() {
    // given
    FixtureMonkey sut = FixtureMonkey.builder()
        .plugin(new MockitoPlugin())
        .build();

    // when
    Order actual = sut.giveMeOne(Order.class);

    // then
    then(actual.getItem()).isNotNull();
    
    when(actual.getItem().getName()).thenReturn("ring");
    then(actual.getItem().getName()).isEqualTo("ring");
}
```
