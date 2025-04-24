---
title: "빠른 시작 가이드"
weight: 40
menu:
  docs:
    parent: "customizing-objects"
    identifier: "quick-start-guide"
mermaid: true
---

## 학습 내용
- Fixture Monkey로 테스트 객체를 커스터마이징하는 핵심 방법
- 단순하고 복잡한 객체를 커스터마이징하는 기본 접근법
- 초보자가 자주 겪는 문제의 해결책

## 5분 빠른 시작

> 이 섹션은 Fixture Monkey를 시작하는 데 필요한 핵심 정보만 다룹니다.

### 꼭 알아야 할 4가지 핵심 메서드

시간이 없다면, 지금 꼭 알아야 할 내용은 다음과 같습니다:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// 1. FixtureMonkey 인스턴스 생성
FixtureMonkey fixtureMonkey = FixtureMonkey.create();

// 2. 특정 속성 값으로 상품 생성
Product product = fixtureMonkey.giveMeBuilder(Product.class)
    .set("name", "스마트폰")            // 속성 설정
    .set("price", new BigDecimal(499)) // 다른 속성 설정
    .sample();                         // 실제 객체 생성

// 3. 상품 목록이 있는 주문 생성
Order order = fixtureMonkey.giveMeBuilder(Order.class)
    .size("products", 2)               // 리스트 크기를 2로 설정
    .set("products[0].name", "노트북") // 리스트 요소 커스터마이징
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// 1. FixtureMonkey 인스턴스 생성
val fixtureMonkey = FixtureMonkey.create()

// 2. 특정 속성 값으로 상품 생성
val product = fixtureMonkey.giveMeBuilder<Product>()
    .setExp(Product::name, "스마트폰")        // 속성 설정
    .setExp(Product::price, BigDecimal(499))  // 다른 속성 설정
    .sample()                                 // 실제 객체 생성

// 3. 상품 목록이 있는 주문 생성
val order = fixtureMonkey.giveMeBuilder<Order>()
    .sizeExp(Order::products, 2)              // 리스트 크기를 2로 설정
    .set("products[0].name", "노트북")        // 리스트 요소 커스터마이징
    .sample()
{{< /tab >}}
{{< /tabpane>}}

### 시각적 개요

다음은 Fixture Monkey로 객체를 커스터마이징하는 과정을 보여주는 간단한 순서도입니다:

{{< mermaid >}}
flowchart LR
    A[FixtureMonkey 생성] --> B[Builder 가져오기]
    B --> C[속성 커스터마이징]
    C --> D[객체 생성]
    
    style A fill:#f9d5e5,stroke:#333,stroke-width:2px
    style B fill:#eeeeee,stroke:#333,stroke-width:2px
    style C fill:#d5e8f9,stroke:#333,stroke-width:2px
    style D fill:#e8f9d5,stroke:#333,stroke-width:2px
{{< /mermaid >}}

## 사전 요구사항
이 가이드는 다음을 가정합니다:
- 이미 프로젝트에 Fixture Monkey를 추가했습니다
- 기본 FixtureMonkey 인스턴스를 생성하는 방법을 알고 있습니다

아직 Fixture Monkey를 설정하지 않았다면, 먼저 [시작하기]({{< ref "/docs/get-started/requirements" >}}) 섹션을 참고하세요.

## 기본 커스터마이징 방법

> 이 섹션은 일상적으로 사용하게 될 가장 기본적인 커스터마이징 방법을 소개합니다.

### 속성 값 설정하기

객체를 커스터마이징하는 가장 기본적인 방법은 특정 속성 값을 설정하는 것입니다:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
Product product = fixtureMonkey.giveMeBuilder(Product.class)
    .set("name", "스마트폰")
    .set("price", new BigDecimal("499.99"))
    .set("available", true)
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
val product = fixtureMonkey.giveMeBuilder<Product>()
    .setExp(Product::name, "스마트폰")
    .setExp(Product::price, BigDecimal("499.99"))
    .setExp(Product::available, true)
    .sample()
{{< /tab >}}
{{< /tabpane>}}

### null 값 설정하기

null 값으로 테스트해야 할 때:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
Product nullNameProduct = fixtureMonkey.giveMeBuilder(Product.class)
    .setNull("name")  // 이름을 null로 설정
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
val nullNameProduct = fixtureMonkey.giveMeBuilder<Product>()
    .setNullExp(Product::name)  // 이름을 null로 설정
    .sample()
{{< /tab >}}
{{< /tabpane>}}

## 컬렉션 다루기

컬렉션을 다룰 때 가장 중요한 것은 먼저 크기를 설정하는 것입니다:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// 2개의 상품이 있는 주문 생성
Order orderWith2Products = fixtureMonkey.giveMeBuilder(Order.class)
    .size("products", 2)                // 먼저 크기 설정
    .set("products[0].name", "노트북")  // 그 다음 요소 커스터마이징
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// 2개의 상품이 있는 주문 생성
val orderWith2Products = fixtureMonkey.giveMeBuilder<Order>()
    .sizeExp(Order::products, 2)        // 먼저 크기 설정
    .set("products[0].name", "노트북")  // 그 다음 요소 커스터마이징
    .sample()
{{< /tab >}}
{{< /tabpane>}}

더 고급 컬렉션 커스터마이징은 [경로 표현식]({{< ref "/docs/customizing-objects/path-expressions" >}}) 문서를 확인하세요.

## 중첩 객체 커스터마이징

점 표기법을 사용하여 중첩된 속성에 접근할 수 있습니다:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// 주소가 있는 고객 생성
Customer customer = fixtureMonkey.giveMeBuilder(Customer.class)
    .set("name", "홍길동")
    .set("address.street", "123 메인 스트리트")  // 중첩 속성
    .set("address.city", "서울")                // 중첩 속성
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// 주소가 있는 고객 생성
val customer = fixtureMonkey.giveMeBuilder<Customer>()
    .setExp(Customer::name, "홍길동")
    .set("address.street", "123 메인 스트리트")  // 중첩 속성
    .set("address.city", "서울")                // 중첩 속성
    .sample()
{{< /tab >}}
{{< /tabpane>}}

더 복잡한 중첩 객체 커스터마이징은 [InnerSpec]({{< ref "/docs/customizing-objects/innerspec" >}}) 가이드를 확인하세요.

## 자주 묻는 질문

> 초보자가 자주 겪는 가장 일반적인 문제들입니다.

### 요소를 커스터마이징하려고 했는데 컬렉션이 비어있는 이유는 무엇인가요?

가장 흔한 실수는 먼저 컬렉션 크기를 설정하지 않는 것입니다:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// 잘못된 방법 - 컬렉션이 비어있을 수 있음
Order orderWrong = fixtureMonkey.giveMeBuilder(Order.class)
    .set("products[0].name", "노트북")  // 이것은 작동하지 않을 수 있습니다!
    .sample();

// 올바른 방법 - 먼저 크기 설정
Order orderCorrect = fixtureMonkey.giveMeBuilder(Order.class)
    .size("products", 1)                // 먼저 크기 설정!
    .set("products[0].name", "노트북")  // 이제 작동합니다
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// 잘못된 방법 - 컬렉션이 비어있을 수 있음
val orderWrong = fixtureMonkey.giveMeBuilder<Order>()
    .set("products[0].name", "노트북")  // 이것은 작동하지 않을 수 있습니다!
    .sample()

// 올바른 방법 - 먼저 크기 설정
val orderCorrect = fixtureMonkey.giveMeBuilder<Order>()
    .sizeExp(Order::products, 1)        // 먼저 크기 설정!
    .set("products[0].name", "노트북")  // 이제 작동합니다
    .sample()
{{< /tab >}}
{{< /tabpane>}}

### null로 설정하지 않았는데 왜 null 값이 생성되나요?

기본적으로 Fixture Monkey는 일부 속성에 대해 null 값을 생성할 수 있습니다. 값이 null이 아니도록 하려면:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
Product nonNullProduct = fixtureMonkey.giveMeBuilder(Product.class)
    .setNotNull("name")        // 이름이 null이 아님을 보장
    .setNotNull("price")       // 가격이 null이 아님을 보장
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
val nonNullProduct = fixtureMonkey.giveMeBuilder<Product>()
    .setNotNullExp(Product::name)    // 이름이 null이 아님을 보장
    .setNotNullExp(Product::price)   // 가격이 null이 아님을 보장
    .sample()
{{< /tab >}}
{{< /tabpane>}}

## 다음 단계

이제 기본을 배웠으니, 더 고급 사용법을 위해 다음 주제를 살펴보세요:

1. **[경로 표현식]({{< ref "/docs/customizing-objects/path-expressions" >}})** - 중첩 속성 접근 및 커스터마이징
2. **[커스터마이징 API]({{< ref "/docs/customizing-objects/apis" >}})** - 커스터마이징 메서드 전체 목록
3. **[인터페이스 테스트]({{< ref "/docs/customizing-objects/interface" >}})** - 인터페이스 작업 방법
4. **[InnerSpec]({{< ref "/docs/customizing-objects/innerspec" >}})** - 복잡한 객체를 위한 고급 커스터마이징
5. **[Arbitrary]({{< ref "/docs/customizing-objects/arbitrary" >}})** - 특정 제약 조건이 있는 테스트 데이터 생성
