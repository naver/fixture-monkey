---
title: "FAQ"
weight: 101
menu:
docs:
  parent: "cheat-sheet"
  identifier: "faq"
---

### How do I get started with Fixture Monkey?

Fixture Monkey provides a simple way to create test objects with random values. Here's how to get started:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// Create a FixtureMonkey instance
FixtureMonkey fixtureMonkey = FixtureMonkey.create();

// Generate a random object
Person person = fixtureMonkey.giveMeOne(Person.class);
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// Create a FixtureMonkey instance
val fixtureMonkey = FixtureMonkey.create()

// Generate a random object
val person = fixtureMonkey.giveMeOne<Person>()
{{< /tab >}}
{{< /tabpane>}}

### How do I add Fixture Monkey to my project?

You can easily add Fixture Monkey to your Maven or Gradle project:

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

### How do I specify values for certain fields while keeping others random?

You can use the `set()` method to specify values for specific fields:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
Person person = fixtureMonkey.giveMeBuilder(Person.class)
    .set("name", "John Doe")
    .set("age", 25)
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
val person = fixtureMonkey.giveMeBuilder<Person>()
    .setExpGetter(Person::getName, "John Doe")
    .setExpGetter(Person::getAge, 25)
    .sample()
{{< /tab >}}
{{< /tabpane>}}

### How do I control the size of collections like List, Set or Map?

You can control the size of collections using the `size()` method:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
Person person = fixtureMonkey.giveMeBuilder(Person.class)
    .size("friends", 5) // Sets the size of the friends list to 5
    .sample();

// Setting a range for size
Product product = fixtureMonkey.giveMeBuilder(Product.class)
    .size("tags", 2, 5) // The tags list will have between 2 and 5 elements
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
val person = fixtureMonkey.giveMeBuilder<Person>()
    .setExpSize(Person::getFriends, 5) // Sets the size of the friends list to 5
    .sample()

// Setting a range for size
val product = fixtureMonkey.giveMeBuilder<Product>()
    .setExpSize(Product::getTags, 2, 5) // The tags list will have between 2 and 5 elements
    .sample()
{{< /tab >}}
{{< /tabpane>}}

### How do I handle null values?

You can control null probability using the `nullInject` option:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// Create a FixtureMonkey with no null values
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .nullInject(0.0) // Set null probability to 0
    .build();

// Create a FixtureMonkey with 50% null probability
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .nullInject(0.5) // Set null probability to 50%
    .build();

// Set a specific field to null
Person person = fixtureMonkey.giveMeBuilder(Person.class)
    .set("address", null)
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// Create a FixtureMonkey with no null values
val fixtureMonkey = FixtureMonkey.builder()
    .nullInject(0.0) // Set null probability to 0
    .build()

// Create a FixtureMonkey with 50% null probability
val fixtureMonkey = FixtureMonkey.builder()
    .nullInject(0.5) // Set null probability to 50%
    .build()

// Set a specific field to null
val person = fixtureMonkey.giveMeBuilder<Person>()
    .setExpGetter(Person::getAddress, null)
    .sample()
{{< /tab >}}
{{< /tabpane>}}

### How do I make my tests reproducible?

You can use a fixed seed to generate the same data across test runs:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// Create a FixtureMonkey with a fixed seed
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .seed(123L)
    .build();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// Create a FixtureMonkey with a fixed seed
val fixtureMonkey = FixtureMonkey.builder()
    .seed(123L)
    .build()
{{< /tab >}}
{{< /tabpane>}}

With JUnit, you can also use the `@Seed` annotation:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
@Test
@Seed(123L)
void testWithSeed() {
    Person person = fixtureMonkey.giveMeOne(Person.class);
    // The same Person will be generated every time
}
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
@Test
@Seed(123L)
fun testWithSeed() {
    val person = fixtureMonkey.giveMeOne<Person>()
    // The same Person will be generated every time
}
{{< /tab >}}
{{< /tabpane>}}

### How do I ensure generated objects satisfy certain conditions?

You can use `setPostCondition()` to filter generated objects that don't meet your criteria:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// Ensure the person is an adult
Person adult = fixtureMonkey.giveMeBuilder(Person.class)
    .setPostCondition(person -> person.getAge() >= 18)
    .sample();

// Ensure a specific field meets a condition
Product product = fixtureMonkey.giveMeBuilder(Product.class)
    .setPostCondition("price", Double.class, price -> price > 0 && price < 1000)
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
// Ensure the person is an adult
val adult = fixtureMonkey.giveMeBuilder<Person>()
    .setPostCondition { it.age >= 18 }
    .sample()

// Ensure a specific field meets a condition
val product = fixtureMonkey.giveMeBuilder<Product>()
    .setPostConditionExpGetter(Product::getPrice, Double::class.java) { it > 0 && it < 1000 }
    .sample()
{{< /tab >}}
{{< /tabpane>}}

### How can I exclude certain values from being generated?
You can use `set()` with a filter to exclude certain values:

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

Or you can use `setPostCondition()` which works like a filter:

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

Please note that using `setPostCondition()` can incur higher costs for narrow conditions because it filters after the Product instance has been created.
In such cases, it's recommended to use `set()` instead.

### How do I handle nested objects?

Fixture Monkey automatically generates nested objects. You can customize them using a property path:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
Order order = fixtureMonkey.giveMeBuilder(Order.class)
    .set("customer.name", "John Doe")
    .set("customer.address.city", "New York")
    .size("items", 3)
    .set("items[0].productName", "Laptop")
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
val order = fixtureMonkey.giveMeBuilder<Order>()
    .setExp("customer.name", "John Doe")
    .setExp("customer.address.city", "New York")
    .sizeExp("items", 3)
    .setExp("items[0].productName", "Laptop")
    .sample()
{{< /tab >}}
{{< /tabpane>}}

### One of my fields depends on the value of another field. How can I customize my fixture?

The `thenApply()` method comes in handy when you need to customize a field that relies on another field:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
Money money = fixtureMonkey.giveMeBuilder(Money.class)
    .set("currency", Currency.getInstance("USD"))
    .thenApply((m, builder) -> 
        builder.set("amount", m.getCurrency().equals(Currency.getInstance("USD")) ? 100.0 : 120.0))
    .sample();
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
val money = fixtureMonkey.giveMeBuilder<Money>()
    .setExpGetter(Money::getCurrency, Currency.getInstance("USD"))
    .thenApply { money, builder -> 
        builder.setExpGetter(Money::getAmount, if (money.currency == Currency.getInstance("USD")) 100.0 else 120.0)
    }
    .sample()
{{< /tab >}}
{{< /tabpane>}}

For more information, check the [`thenApply()` section](../../customizing-objects/apis/#thenapply).

### How can I limit the range of characters for my generated Strings?
> Related - How can I constrain the range of my generated Instant values?

If you want each generated primitive type to adhere to specific constraints, you can use the [`javaTypeArbitaryGenerator`
and `javaTimeTypeArbitraryGenerator`](../../fixture-monkey-options/customization-options/#constraining-java-types) options.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
// Configure String generation with specific character ranges
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .javaTypeArbitraryGenerator(new JavaTypeArbitraryGenerator() {
        @Override
        public StringArbitrary strings() {
            return Arbitraries.strings().alpha().ofLength(5, 10);
        }
    })
    .build();

// Configure time generation with specific ranges
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
// Configure String generation with specific character ranges
val fixtureMonkey = FixtureMonkey.builder()
    .javaTypeArbitraryGenerator(object : JavaTypeArbitraryGenerator() {
        override fun strings(): StringArbitrary {
            return Arbitraries.strings().alpha().ofLength(5, 10)
        }
    })
    .build()

// Configure time generation with specific ranges
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

### Throws an exception when generating a certain type

If you encounter exceptions when generating certain types, try using [PriorityConstructorArbitraryIntrospector](../../generating-objects/introspector/#PriorityConstructorArbitraryIntrospector):

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushExactTypeArbitraryIntrospector(ProblematicType.class, PriorityConstructorArbitraryIntrospector.INSTANCE)
    .build();

// Now generation should work
ProblematicType instance = fixtureMonkey.giveMeOne(ProblematicType.class);
{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}
val fixtureMonkey = FixtureMonkey.builder()
    .pushExactTypeArbitraryIntrospector(ProblematicType::class.java, PriorityConstructorArbitraryIntrospector.INSTANCE)
    .build()

// Now generation should work
val instance = fixtureMonkey.giveMeOne<ProblematicType>()
{{< /tab >}}
{{< /tabpane>}}

If it does not work, please try to make your own `ArbitraryIntrospector` or create [an issue](https://github.com/naver/fixture-monkey/issues) on GitHub and ask for help.