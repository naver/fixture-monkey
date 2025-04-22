---
title: "자주 묻는 질문"
weight: 101
menu:
docs:
  parent: "cheat-sheet"
  identifier: "faq"
---

### 픽스쳐 몽키를 어떻게 시작하나요?

픽스쳐 몽키는 랜덤 값을 가진 테스트 객체를 쉽게 생성할 수 있는 방법을 제공합니다. 다음과 같이 시작할 수 있습니다:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// FixtureMonkey 인스턴스 생성
FixtureMonkey fixtureMonkey = FixtureMonkey.create();

// 랜덤 객체 생성
Person person = fixtureMonkey.giveMeOne(Person.class);
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// FixtureMonkey 인스턴스 생성
val fixtureMonkey = FixtureMonkey.create()

// 랜덤 객체 생성
val person = fixtureMonkey.giveMeOne<Person>()
{{< /tab >}}
{{< /tabpane>}}

### 픽스쳐 몽키를 프로젝트에 어떻게 추가하나요?

Maven이나 Gradle 프로젝트에 쉽게 픽스쳐 몽키를 추가할 수 있습니다:

{{< tabpane persist=false >}}
{{< tab header="Gradle (Kotlin)" lang="kotlin">}}
implementation("com.navercorp.fixturemonkey:fixture-monkey:1.1.x")
{{< /tab >}}
{{< tab header="Gradle (Groovy)" lang="groovy">}}
implementation 'com.navercorp.fixturemonkey:fixture-monkey:1.1.x'
{{< /tab >}}
{{< tab header="Maven" lang="xml">}}
<dependency>
    <groupId>com.navercorp.fixturemonkey</groupId>
    <artifactId>fixture-monkey</artifactId>
    <version>1.1.x</version>
</dependency>
{{< /tab >}}
{{< /tabpane>}}

### 특정 필드만 값을 지정하고 나머지는 랜덤으로 생성하려면 어떻게 하나요?

`set()` 메서드를 사용하여 특정 필드의 값을 지정할 수 있습니다:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
Person person = fixtureMonkey.giveMeBuilder(Person.class)
    .set("name", "홍길동")
    .set("age", 25)
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
val person = fixtureMonkey.giveMeBuilder<Person>()
    .setExpGetter(Person::getName, "홍길동")
    .setExpGetter(Person::getAge, 25)
    .sample()
{{< /tab >}}
{{< /tabpane>}}

### List, Set, Map 같은 컬렉션의 크기를 어떻게 제어하나요?

`size()` 메서드를 사용하여 컬렉션의 크기를 제어할 수 있습니다:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
Person person = fixtureMonkey.giveMeBuilder(Person.class)
    .size("friends", 5) // friends 리스트의 크기를 5로 설정
    .sample();

// 크기 범위 설정
Product product = fixtureMonkey.giveMeBuilder(Product.class)
    .size("tags", 2, 5) // tags 리스트의 크기가 2~5개가 됨
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
val person = fixtureMonkey.giveMeBuilder<Person>()
    .setExpSize(Person::getFriends, 5) // friends 리스트의 크기를 5로 설정
    .sample()

// 크기 범위 설정
val product = fixtureMonkey.giveMeBuilder<Product>()
    .setExpSize(Product::getTags, 2, 5) // tags 리스트의 크기가 2~5개가 됨
    .sample()
{{< /tab >}}
{{< /tabpane>}}

### null 값은 어떻게 처리하나요?

`nullInject` 옵션을 사용하여 null 확률을 제어할 수 있습니다:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// null 값이 없는 FixtureMonkey 생성
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .nullInject(0.0) // null 확률을 0으로 설정
    .build();

// null 확률이 50%인 FixtureMonkey 생성
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .nullInject(0.5) // null 확률을 50%로 설정
    .build();

// 특정 필드를 null로 설정
Person person = fixtureMonkey.giveMeBuilder(Person.class)
    .set("address", null)
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// null 값이 없는 FixtureMonkey 생성
val fixtureMonkey = FixtureMonkey.builder()
    .nullInject(0.0) // null 확률을 0으로 설정
    .build()

// null 확률이 50%인 FixtureMonkey 생성
val fixtureMonkey = FixtureMonkey.builder()
    .nullInject(0.5) // null 확률을 50%로 설정
    .build()

// 특정 필드를 null로 설정
val person = fixtureMonkey.giveMeBuilder<Person>()
    .setExpGetter(Person::getAddress, null)
    .sample()
{{< /tab >}}
{{< /tabpane>}}

### 테스트를 재현 가능하게 만들려면 어떻게 해야 하나요?

고정된 시드를 사용하여 테스트 실행 간에 동일한 데이터를 생성할 수 있습니다:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// 고정된 시드로 FixtureMonkey 생성
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .seed(123L)
    .build();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// 고정된 시드로 FixtureMonkey 생성
val fixtureMonkey = FixtureMonkey.builder()
    .seed(123L)
    .build()
{{< /tab >}}
{{< /tabpane>}}

JUnit에서는 `@Seed` 어노테이션을 사용할 수도 있습니다:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
@Seed(123L)
void testWithSeed() {
    Person person = fixtureMonkey.giveMeOne(Person.class);
    // 매번 동일한 Person이 생성됨
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
@Seed(123L)
fun testWithSeed() {
    val person = fixtureMonkey.giveMeOne<Person>()
    // 매번 동일한 Person이 생성됨
}
{{< /tab >}}
{{< /tabpane>}}

### 생성된 객체가 특정 조건을 만족하도록 하려면 어떻게 해야 하나요?

`setPostCondition()`을 사용하여 조건을 만족하지 않는 생성된 객체를 필터링할 수 있습니다:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// 성인만 생성되도록 함
Person adult = fixtureMonkey.giveMeBuilder(Person.class)
    .setPostCondition(person -> person.getAge() >= 18)
    .sample();

// 특정 필드가 조건을 만족하도록 함
Product product = fixtureMonkey.giveMeBuilder(Product.class)
    .setPostCondition("price", Double.class, price -> price > 0 && price < 1000)
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// 성인만 생성되도록 함
val adult = fixtureMonkey.giveMeBuilder<Person>()
    .setPostCondition { it.age >= 18 }
    .sample()

// 특정 필드가 조건을 만족하도록 함
val product = fixtureMonkey.giveMeBuilder<Product>()
    .setPostConditionExpGetter(Product::getPrice, Double::class.java) { it > 0 && it < 1000 }
    .sample()
{{< /tab >}}
{{< /tabpane>}}

### 특정 값이 생성되지 않도록 하려면 어떻게 해야 하나요?
필터를 사용하여 `set()`으로 특정 값을 제외할 수 있습니다:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

Product product = sut.giveMeBuilder(Product.class)
    .set("productType", ArbitraryUtils.toCombinableArbitrary(Arbitraries.of(ProductType)).filter(it -> it != CLOTHING && it != ELECTRONICS))
    .sample();

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val product = sut.giveMeBuilder<Product>()
    .setExpGetter(Product::getProductType, ArbitraryUtils.toCombinableArbitrary(Arbitraries.of(ProductType::class.java)).filter { it != ProductType.CLOTHING && it != ProductType.ELECTRONICS })
    .sample()

{{< /tab >}}
{{< /tabpane>}}

또는 필터처럼 동작하는 `setPostCondition()`을 사용할 수 있습니다:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

Product product = sut.giveMeBuilder(Product.class)
    .setPostCondition("productType", ProductType.class, it -> it != CLOTHING && it != ELECTRONICS)
    .sample();

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val product = sut.giveMeBuilder<Product>()
    .setPostConditionExpGetter(Product::getProductType, ProductType::class.java) { it != ProductType.CLOTHING && it != ProductType.ELECTRONICS }
    .sample()

{{< /tab >}}
{{< /tabpane>}}

`setPostCondition()`을 사용하면 Product 인스턴스가 생성된 후 필터링되므로 제한적인 조건에서 더 높은 비용이 발생할 수 있습니다. 이러한 경우 `set()`을 대신 사용하는 것이 권장됩니다.

### 중첩된 객체는 어떻게 처리하나요?

픽스쳐 몽키는 중첩된 객체를 자동으로 생성합니다. 속성 경로를 사용하여 이러한 객체를 커스터마이징할 수 있습니다:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
Order order = fixtureMonkey.giveMeBuilder(Order.class)
    .set("customer.name", "홍길동")
    .set("customer.address.city", "서울")
    .size("items", 3)
    .set("items[0].productName", "노트북")
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
val order = fixtureMonkey.giveMeBuilder<Order>()
    .setExp("customer.name", "홍길동")
    .setExp("customer.address.city", "서울")
    .sizeExp("items", 3)
    .setExp("items[0].productName", "노트북")
    .sample()
{{< /tab >}}
{{< /tabpane>}}

### 필드 중 하나가 다른 필드의 값에 의존하고 있습니다. 이러한 픽스처를 커스터마이징 하려면 어떻게 해야 하나요?

`thenApply()` 메서드는 다른 필드에 의존하는 필드를 커스터마이징 할 때 유용합니다:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
Money money = fixtureMonkey.giveMeBuilder(Money.class)
    .set("currency", Currency.getInstance("KRW"))
    .thenApply((m, builder) -> 
        builder.set("amount", m.getCurrency().equals(Currency.getInstance("KRW")) ? 10000.0 : 100.0))
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
val money = fixtureMonkey.giveMeBuilder<Money>()
    .setExpGetter(Money::getCurrency, Currency.getInstance("KRW"))
    .thenApply { money, builder -> 
        builder.setExpGetter(Money::getAmount, if (money.currency == Currency.getInstance("KRW")) 10000.0 else 100.0)
    }
    .sample()
{{< /tab >}}
{{< /tabpane>}}

자세한 내용은 [`thenApply()`](../../customizing-objects/apis/#thenapply)를 확인해주세요.

### 생성된 문자열의 문자 범위를 제한하려면 어떻게 해야 할까요?
> 관련 질문 - 생성된 인스턴트 값의 범위를 제한할 수 있나요?

각 생성된 기본 타입이 특정 제약 조건을 지키길 원하는 경우에는 [`javaTypeArbitaryGenerator`와 `javaTimeTypeArbitraryGenerator`](../../fixture-monkey-options/customization-options/#constraining-java-types) 옵션을 사용할 수 있습니다.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// 특정 문자 범위로 문자열 생성 설정
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .javaTypeArbitraryGenerator(new JavaTypeArbitraryGenerator() {
        @Override
        public StringArbitrary strings() {
            return Arbitraries.strings().alpha().ofLength(5, 10);
        }
    })
    .build();

// 특정 범위로 시간 생성 설정
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .javaTimeTypeArbitraryGenerator(new JavaTimeTypeArbitraryGenerator() {
        @Override
        public Arbitrary<Instant> instant() {
            Instant start = Instant.parse("2023-01-01T00:00:00Z");
            Instant end = Instant.parse("2023-12-31T23:59:59Z");
            return Arbitraries.instants().between(start, end);
        }
    })
    .build();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// 특정 문자 범위로 문자열 생성 설정
val fixtureMonkey = FixtureMonkey.builder()
    .javaTypeArbitraryGenerator(object : JavaTypeArbitraryGenerator() {
        override fun strings(): StringArbitrary {
            return Arbitraries.strings().alpha().ofLength(5, 10)
        }
    })
    .build()

// 특정 범위로 시간 생성 설정
val fixtureMonkey = FixtureMonkey.builder()
    .javaTimeTypeArbitraryGenerator(object : JavaTimeTypeArbitraryGenerator() {
        override fun instant(): Arbitrary<Instant> {
            val start = Instant.parse("2023-01-01T00:00:00Z")
            val end = Instant.parse("2023-12-31T23:59:59Z")
            return Arbitraries.instants().between(start, end)
        }
    })
    .build()
{{< /tab >}}
{{< /tabpane>}}

### 특정 타입을 생성할 때 예외가 발생해요

특정 타입을 생성할 때 예외가 발생하면 [PriorityConstructorArbitraryIntrospector](../../generating-objects/introspector/#PriorityConstructorArbitraryIntrospector)를 시도해보세요:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushExactTypeArbitraryIntrospector(ProblematicType.class, PriorityConstructorArbitraryIntrospector.INSTANCE)
    .build();

// 이제 생성이 잘 될 것입니다
ProblematicType instance = fixtureMonkey.giveMeOne(ProblematicType.class);
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
val fixtureMonkey = FixtureMonkey.builder()
    .pushExactTypeArbitraryIntrospector(ProblematicType::class.java, PriorityConstructorArbitraryIntrospector.INSTANCE)
    .build()

// 이제 생성이 잘 될 것입니다
val instance = fixtureMonkey.giveMeOne<ProblematicType>()
{{< /tab >}}
{{< /tabpane>}}

위 옵션을 추가했는데도 동작하지 않는다면, `ArbitraryIntrospector`을 직접 만들거나 깃허브에 [이슈](https://github.com/naver/fixture-monkey/issues)를 만들어주시면 도움을 드리겠습니다.
