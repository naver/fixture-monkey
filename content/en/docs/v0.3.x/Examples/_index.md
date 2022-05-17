---
title: "Examples"
linkTitle: "Examples"
weight: 3
---


```java
@Data
public class Order {
    @NotNull
    private Long id;

    @NotBlank
    private String orderNo;

    @Size(min = 2, max = 10)
    private String productName;

    @NotNull
    private Long productId;

    @Min(1)
    @Max(100)
    private int quantity;

    @Min(0)
    private long price;

    @Size(max = 5)
    private List<@NotBlank @Size(max = 10) String> items = new ArrayList<>();

    @PastOrPresent
    private Instant orderedAt;

    @Email
    private String sellerEmail;
}

@Test
void test() {
    FixtureMonkey fixture = FixtureMonkey.create();

    Order order = fixture.giveMeOne(Order.class);
    List<Order> orders = fixture.giveMe(Order.class, 3);
    Stream<Order> orderStream = fixture.giveMe(Order.class);
    Arbitrary<Order> orderArbitrary = fixture.giveMeArbitrary(Order.class);
}
```
