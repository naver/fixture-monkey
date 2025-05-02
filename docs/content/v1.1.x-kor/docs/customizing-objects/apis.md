---
title: "커스터마이징 API"
weight: 42
menu:
docs:
  parent: "customizing-objects"
  identifier: "fixture-customization-apis"
---

## 이 문서에서 배우는 내용
- 테스트에 필요한 데이터를 쉽게 만드는 방법
- 원하는 값을 가진 객체를 자유롭게 생성하는 방법
- 실제 테스트에서 자주 필요한 데이터 생성 방법

## 시작하기 전에
이 문서에서는 테스트 데이터를 쉽게 만들 수 있는 다양한 방법을 배웁니다.
예를 들어 다음과 같은 상황에서 Fixture Monkey API를 활용할 수 있습니다:

- 회원가입 테스트를 위해 특정 나이대의 회원 데이터가 필요할 때
- 주문 테스트를 위해 여러 개의 상품이 담긴 장바구니가 필요할 때
- 결제 테스트를 위해 특정 금액 이상의 주문이 필요할 때

### 알아두면 좋은 용어
- **샘플링(sampling)**: 테스트용 데이터를 실제로 만드는 것을 의미합니다. `sample()` 메서드를 호출할 때마다 새로운 테스트 데이터가 생성됩니다.
- **빌더(builder)**: 객체를 단계적으로 만들 수 있게 도와주는 도구입니다. Fixture Monkey에서는 `giveMeBuilder()`로 빌더를 생성합니다.
- **Path Expression**: 객체의 어떤 속성을 변경할지 지정하는 방법입니다. 예를 들어 "age"는 나이 속성을, "items[0]"은 리스트의 첫 번째 아이템을, "address.city"는 주소 객체 안의 도시 속성을 의미합니다.

## 목차
- [API 요약 표](#api-요약-표)
- [기본 API 사용하기](#기본-api-사용하기)
  - [set() - 원하는 값 지정하기](#set)
  - [size() - 리스트 크기 조절하기](#size-minsize-maxsize)
  - [setNull() - null 값 다루기](#setnull-setnotnull)
- [활용 API 배우기](#활용-api-배우기)
  - [setInner() - 재사용 가능한 설정 만들기](#setinner)
  - [setLazy() - 동적으로 값 생성하기](#setlazy)
  - [setPostCondition() - 조건에 맞는 값 만들기](#setpostcondition)
  - [fixed() - 항상 같은 값 생성하기](#fixed)
  - [limit - 일부만 값 설정하기](#limit)
- [고급 API 활용하기](#고급-api-활용하기)
  - [thenApply() - 연관된 값 설정하기](#thenapply)
- [자주 묻는 질문 (FAQ)](#자주-묻는-질문-faq)

## API 요약 표

### 기본 API (처음 사용하시는 분들을 위한 필수 API)
| API | 설명 | 예시 상황 |
|-----|------|----------|
| set() | 원하는 값 직접 지정하기 | 회원의 나이를 20살로 지정 |
| size() | 리스트 크기 지정하기 | 장바구니에 상품 3개 담기 |
| setNull() | null 값 지정하기 | 탈퇴한 회원의 이메일을 null로 설정 |

### 활용 API (기본 기능에 익숙해진 후 사용하세요)
| API | 설명 | 예시 상황 |
|-----|------|----------|
| setInner() | 재사용 가능한 설정 만들기 | 여러 테스트에서 같은 형태의 회원정보 사용 |
| setLazy() | 동적으로 값 생성하기 | 순차적인 주문번호 생성 |
| setPostCondition() | 조건에 맞는 값 만들기 | 성인만 가입 가능한 서비스 테스트 |
| fixed() | 항상 같은 값 생성하기 | 테스트마다 동일한 테스트 데이터 사용 |
| limit | 일부만 값 설정하기 | 장바구니의 일부 상품만 할인 적용 |

### 고급 API (복잡한 테스트 상황에서 사용하세요)
| API | 설명 | 예시 상황 |
|-----|------|----------|
| thenApply() | 연관된 값 설정하기 | 주문 총액을 주문 상품 가격의 합으로 설정 |

## 기본 API 사용하기

### set()
`set()` 메서드는 객체의 특정 속성에 원하는 값을 설정할 때 사용합니다.
가장 기본적이고 많이 사용되는 API입니다.

#### 기본 사용법

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// 회원 데이터 생성 예제
Member member = fixtureMonkey.giveMeBuilder(Member.class)
    .set("name", "홍길동")        // 이름 설정
    .set("age", 25)             // 나이 설정
    .set("email", "hong@test.com") // 이메일 설정
    .sample();

// 주문 데이터 생성 예제
Order order = fixtureMonkey.giveMeBuilder(Order.class)
    .set("orderId", "ORDER-001")           // 주문번호 설정
    .set("totalAmount", BigDecimal.valueOf(15000)) // 주문금액 설정
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// 회원 데이터 생성 예제
val member = fixtureMonkey.giveMeBuilder<Member>()
    .setExp(Member::name, "홍길동")        // 이름 설정
    .setExp(Member::age, 25)             // 나이 설정
    .setExp(Member::email, "hong@test.com") // 이메일 설정
    .sample()

// 주문 데이터 생성 예제
val order = fixtureMonkey.giveMeBuilder<Order>()
    .setExp(Order::orderId, "ORDER-001")           // 주문번호 설정
    .setExp(Order::totalAmount, BigDecimal.valueOf(15000)) // 주문금액 설정
    .sample()
{{< /tab >}}
{{< /tabpane>}}

### size(), minSize(), maxSize()
`size()` 메서드는 리스트나 배열같은 컬렉션의 크기를 지정할 때 사용합니다.
정확한 크기를 설정하거나, 최소/최대 크기를 지정할 수 있습니다.

#### 기본 사용법

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// 장바구니에 상품 3개 담기
Cart cart = fixtureMonkey.giveMeBuilder(Cart.class)
    .size("items", 3)  // 장바구니에 3개 상품
    .sample();

// 2~4개 사이의 리뷰가 있는 상품 만들기
Product product = fixtureMonkey.giveMeBuilder(Product.class)
    .size("reviews", 2, 4)  // 최소 2개, 최대 4개 리뷰
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// 장바구니에 상품 3개 담기
val cart = fixtureMonkey.giveMeBuilder<Cart>()
    .sizeExp(Cart::items, 3)  // 장바구니에 3개 상품
    .sample()

// 2~4개 사이의 리뷰가 있는 상품 만들기
val product = fixtureMonkey.giveMeBuilder<Product>()
    .sizeExp(Product::reviews, 2, 4)  // 최소 2개, 최대 4개 리뷰
    .sample()
{{< /tab >}}
{{< /tabpane>}}

### setNull(), setNotNull()
`setNull()`과 `setNotNull()`은 특정 속성을 null로 만들거나, 반드시 값이 있도록 만들 때 사용합니다.

#### 기본 사용법

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// 탈퇴한 회원 데이터 생성 (이메일은 null)
Member withdrawnMember = fixtureMonkey.giveMeBuilder(Member.class)
    .set("name", "홍길동")
    .setNull("email")      // 이메일은 null로 설정
    .sample();

// 필수 입력 정보가 있는 주문 생성
Order validOrder = fixtureMonkey.giveMeBuilder(Order.class)
    .setNotNull("orderId")     // 주문번호는 반드시 있어야 함
    .setNotNull("orderDate")   // 주문일자도 반드시 있어야 함
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// 탈퇴한 회원 데이터 생성 (이메일은 null)
val withdrawnMember = fixtureMonkey.giveMeBuilder<Member>()
    .setExp(Member::name, "홍길동")
    .setNullExp(Member::email)      // 이메일은 null로 설정
    .sample()

// 필수 입력 정보가 있는 주문 생성
val validOrder = fixtureMonkey.giveMeBuilder<Order>()
    .setNotNullExp(Order::orderId)     // 주문번호는 반드시 있어야 함
    .setNotNullExp(Order::orderDate)   // 주문일자도 반드시 있어야 함
    .sample()
{{< /tab >}}
{{< /tabpane>}}

## 활용 API 배우기

### setInner()
`setInner()`는 여러 테스트에서 재사용할 수 있는 설정을 만들 때 사용합니다.
예를 들어, 여러 테스트에서 동일한 형태의 회원 정보나 주문 정보가 필요할 때 유용합니다.

#### 기본 사용법

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// VIP 회원 정보 설정
InnerSpec vipMemberSpec = new InnerSpec()
    .property("grade", "VIP")
    .property("point", 10000)
    .property("joinDate", LocalDate.now().minusYears(1));

// VIP 회원 생성에 재사용
Member vipMember = fixtureMonkey.giveMeBuilder(Member.class)
    .setInner(vipMemberSpec)
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// VIP 회원 정보 설정
val vipMemberSpec = InnerSpec()
    .property("grade", "VIP")
    .property("point", 10000)
    .property("joinDate", LocalDate.now().minusYears(1))

// VIP 회원 생성에 재사용
val vipMember = fixtureMonkey.giveMeBuilder<Member>()
    .setInner(vipMemberSpec)
    .sample()
{{< /tab >}}
{{< /tabpane>}}

### setLazy()
`setLazy()`는 매번 다른 값이나 순차적인 값을 생성할 때 사용합니다.
예를 들어, 순차적인 주문번호나 현재 시간을 사용할 때 유용합니다.

#### 기본 사용법

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// 순차적인 주문번호 생성
AtomicInteger orderCounter = new AtomicInteger(1);
Order order = fixtureMonkey.giveMeBuilder(Order.class)
    .setLazy("orderId", () -> "ORDER-" + orderCounter.getAndIncrement())
    .sample();  // ORDER-1

Order nextOrder = fixtureMonkey.giveMeBuilder(Order.class)
    .setLazy("orderId", () -> "ORDER-" + orderCounter.getAndIncrement())
    .sample();  // ORDER-2
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// 순차적인 주문번호 생성
var orderCounter = AtomicInteger(1)
val order = fixtureMonkey.giveMeBuilder<Order>()
    .setLazy("orderId") { "ORDER-${orderCounter.getAndIncrement()}" }
    .sample()  // ORDER-1

val nextOrder = fixtureMonkey.giveMeBuilder<Order>()
    .setLazy("orderId") { "ORDER-${orderCounter.getAndIncrement()}" }
    .sample()  // ORDER-2
{{< /tab >}}
{{< /tabpane>}}

### setPostCondition()
`setPostCondition()`은 특정 조건을 만족하는 값을 생성할 때 사용합니다.
예를 들어, 성인 회원만 가입 가능한 서비스를 테스트할 때 유용합니다.

{{< alert icon="🚨" text="조건이 너무 까다로우면 값을 찾는 데 시간이 오래 걸릴 수 있습니다. 가능하면 set()을 사용하세요." />}}

#### 기본 사용법

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// 성인 회원만 생성
Member adultMember = fixtureMonkey.giveMeBuilder(Member.class)
    .setPostCondition("age", Integer.class, age -> age >= 19)
    .sample();

// 10만원 이상의 주문만 생성
Order largeOrder = fixtureMonkey.giveMeBuilder(Order.class)
    .setPostCondition("totalAmount", BigDecimal.class, 
        amount -> amount.compareTo(BigDecimal.valueOf(100000)) >= 0)
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// 성인 회원만 생성
val adultMember = fixtureMonkey.giveMeBuilder<Member>()
    .setPostConditionExp(Member::age, Int::class.java) { it >= 19 }
    .sample()

// 10만원 이상의 주문만 생성
val largeOrder = fixtureMonkey.giveMeBuilder<Order>()
    .setPostConditionExp(Order::totalAmount, BigDecimal::class.java) { 
        it >= BigDecimal.valueOf(100000) 
    }
    .sample()
{{< /tab >}}
{{< /tabpane>}}

### fixed()
`fixed()`는 테스트를 실행할 때마다 동일한 테스트 데이터가 필요할 때 사용합니다.

#### 기본 사용법

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// 항상 동일한 회원 정보로 테스트
Member member = fixtureMonkey.giveMeBuilder(Member.class)
    .set("name", "홍길동")
    .set("age", 30)
    .fixed()  // 항상 동일한 데이터 생성
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// 항상 동일한 회원 정보로 테스트
val member = fixtureMonkey.giveMeBuilder<Member>()
    .setExp(Member::name, "홍길동")
    .setExp(Member::age, 30)
    .fixed()  // 항상 동일한 데이터 생성
    .sample()
{{< /tab >}}
{{< /tabpane>}}

### limit
`limit`는 컬렉션의 일부 요소만 특정 값으로 설정하고 싶을 때 사용합니다.

#### 기본 사용법

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// 장바구니의 일부 상품만 할인 적용
Cart cart = fixtureMonkey.giveMeBuilder(Cart.class)
    .size("items", 5)                    // 5개 상품
    .set("items[*].onSale", true, 2)    // 2개 상품만 할인
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// 장바구니의 일부 상품만 할인 적용
val cart = fixtureMonkey.giveMeBuilder<Cart>()
    .sizeExp(Cart::items, 5)                    // 5개 상품
    .set("items[*].onSale", true, 2)    // 2개 상품만 할인
    .sample()
{{< /tab >}}
{{< /tabpane>}}

## 고급 API 활용하기

### thenApply()
`thenApply()`는 이미 생성된 객체의 값을 기반으로 다른 값을 설정해야 할 때 사용합니다.
예를 들어, 주문의 총액을 주문 상품들의 가격 합계로 설정할 때 유용합니다.

#### 기본 사용법

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// 주문 상품 가격의 합계로 총액 설정
Order order = fixtureMonkey.giveMeBuilder(Order.class)
    .size("items", 3)  // 3개 상품
    .thenApply((tempOrder, orderBuilder) -> {
        // 총액 계산
        BigDecimal total = tempOrder.getItems().stream()
            .map(item -> item.getPrice())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        // 계산된 총액 설정
        orderBuilder.set("totalAmount", total);
    })
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// 주문 상품 가격의 합계로 총액 설정
val order = fixtureMonkey.giveMeBuilder<Order>()
    .sizeExp(Order::items, 3)  // 3개 상품
    .thenApply { tempOrder, orderBuilder ->
        // 총액 계산
        val total = tempOrder.items
            .map { it.price }
            .fold(BigDecimal.ZERO, BigDecimal::add)
        // 계산된 총액 설정
        orderBuilder.set("totalAmount", total)
    }
    .sample()
{{< /tab >}}
{{< /tabpane>}}

## 자주 묻는 질문 (FAQ)

### Q: 어떤 API부터 배워야 하나요?

처음에는 다음 순서로 배우시는 것을 추천합니다:
1. `set()` - 가장 기본적이고 많이 사용되는 API입니다.
2. `size()` - 리스트나 배열을 다룰 때 필요합니다.
3. `setNull()`, `setNotNull()` - null 값을 다룰 때 사용합니다.

이후 테스트 작성에 익숙해지면 다른 API들을 하나씩 배워가시면 됩니다.

### Q: 테스트마다 같은 데이터가 필요하면 어떻게 하나요?

`fixed()`를 사용하면 됩니다. 예를 들어:

```java
// 테스트마다 동일한 회원 정보 사용
ArbitraryBuilder<Member> memberBuilder = fixtureMonkey.giveMeBuilder(Member.class)
    .set("name", "홍길동")
    .set("age", 30)
    .fixed();  // 항상 동일한 데이터 생성

Member member1 = memberBuilder.sample(); // 항상 같은 데이터
Member member2 = memberBuilder.sample(); // member1과 동일
```

### Q: 실수로 잘못된 값이 생성되는 것을 방지하려면 어떻게 하나요?

`setPostCondition()`을 사용하여 값의 범위나 조건을 지정할 수 있습니다:

```java
// 나이는 반드시 1-100 사이
Member member = fixtureMonkey.giveMeBuilder(Member.class)
    .setPostCondition("age", Integer.class, age -> age >= 1 && age <= 100)
    .sample();
```