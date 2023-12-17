---
title: "Customization Options"
images: []
menu:
docs:
parent: "fixture-monkey-options"
identifier: "options"
weight: 53
---

Fixture Monkey also provides options through the `FixtureMonkeyBuilder` to customize objects to have the desired values or to use custom property names.

## PropertyNameResolver
> `defaultPropertyNameResolver`, `pushPropertyNameResolver`, `pushAssignableTypePropertyNameResolver`, `pushExactTypePropertyNameResolver`

Options related to the `PropertyNameResolver` allow you to customize how you refer to your properties.

The `defaultPropertyNameResolver` option is used to change the way property names are figured out for all types. If you want to make specific changes for certain types, you can use `pushPropertyNameResolver`, `pushAssignableTypePropertyNameResolver`, or `pushExactTypePropertyNameResolver`.

By default, a property will be referenced by its original name. Let's take a look at the following example to see how we can customize the property name:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

@Data // getter, setter
public class Product {
    String productName;
}

@Test
void test() {
    // given
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .pushPropertyNameResolver(MatcherOperator.exactTypeMatchOperator(String.class, (property) -> "string"))
        .build();
    String expected = "test";

    // when
    String actual = fixtureMonkey.giveMeBuilder(Product.class)
        .set("string", expected)
        .sample()
        .getProductName();

    // then
    then(actual).isEqualTo(expected);
}

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

data class Product (
    val productName: String
)

@Test
fun test() {
    // given
    val fixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .pushPropertyNameResolver(
            MatcherOperator.exactTypeMatchOperator(String::class.java, PropertyNameResolver { "string" })
        )
        .build()
        val expected = "test"

    // when
    val actual: String = fixtureMonkey.giveMeBuilder<Product>()
        .set("string", expected)
        .sample()
        .productName

    // then
    then(actual).isEqualTo(expected)
}

{{< /tab >}}
{{< /tabpane>}}

Normally, the property name will resolve to the original property name "productName".
However, with `pushPropertyNameResolver` the String type properties are now referred to by the name "string".

## Register
> `register`, `registerGroup`, `registerExactType`, `registerAssignableType`

Sometimes your class may need to consistently match certain constraints.
It can be inconvenient and result in lengthy code if you always have to modify the `ArbitraryBuilder` using customization APIs.
In such cases, you can set a default `ArbitraryBuilder` for a class that will satisfy all the basic constraints.

The `register` option helps to register an `ArbitraryBuilder` for a specific type.

For example, the following code demonstrates how to register an `ArbitraryBuilder` for a Product class.
By doing so, all Product instances created by `FixtureMonkey` will have an id value greater than or equal to "0".

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

FixtureMonkey.builder()
    .register(
        Product.class,
        fixture -> fixture.giveMeBuilder(Product.class)
            .set("id", Arbitraries.longs().greaterOrEqual(0))
    )
    .build();

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

FixtureMonkey.builder()
    .register(Product::class.java) {
        it.giveMeBuilder<Product>()
            .set("id", Arbitraries.longs().greaterOrEqual(0))
    }
    .build()

{{< /tab >}}
{{< /tabpane>}}

If you want to register several ArbitraryBuilders at once, you can use the `registerGroup` option.
This can be done using either reflection or the `ArbitraryBuilderGroup` interface.

**Using reflection:**
{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

public class GenerateGroup {
    public ArbitraryBuilder<GenerateString> generateString(FixtureMonkey fixtureMonkey) {
        return fixtureMonkey.giveMeBuilder(GenerateString.class)
            .set("value", Arbitraries.strings().numeric());
    }

    public ArbitraryBuilder<GenerateInt> generateInt(FixtureMonkey fixtureMonkey) {
        return fixtureMonkey.giveMeBuilder(GenerateInt.class)
            .set("value", Arbitraries.integers().between(1, 100));
    }
}

FixtureMonkey.builder()
    .registerGroup(GenerateGroup.class)
    .build();

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

class GenerateGroup {
    fun generateString(fixtureMonkey: FixtureMonkey): ArbitraryBuilder<GenerateString> {
        return fixtureMonkey.giveMeBuilder<GenerateString>()
            .set("value", Arbitraries.strings().numeric())
    }

    fun generateInt(fixtureMonkey: FixtureMonkey): ArbitraryBuilder<GenerateInt> {
        return fixtureMonkey.giveMeBuilder<GenerateInt>()
            .set("value", Arbitraries.integers().between(1, 100))
    }
}

FixtureMonkey.builder()
    .registerGroup(GenerateGroup::class.java)
    .build()

{{< /tab >}}
{{< /tabpane>}}

**Using ArbitraryBuilderGroup interface:**

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

public class GenerateBuilderGroup implements ArbitraryBuilderGroup {
    @Override
    public ArbitraryBuilderCandidateList generateCandidateList() {
        return ArbitraryBuilderCandidateList.create()
            .add(
                ArbitraryBuilderCandidateFactory.of(GenerateString.class)
                    .builder(
                        arbitraryBuilder -> arbitraryBuilder
                            .set("value", Arbitraries.strings().numeric())
                    )
            )
            .add(
                ArbitraryBuilderCandidateFactory.of(GenerateInt.class)
                    .builder(
                        builder -> builder
                            .set("value", Arbitraries.integers().between(1, 100))
                    )
            );
    }
}

FixtureMonkey.builder()
    .registerGroup(new GenerateBuilderGroup())
    .build();

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

class GenerateBuilderGroup : ArbitraryBuilderGroup {
    override fun generateCandidateList(): ArbitraryBuilderCandidateList {
        return ArbitraryBuilderCandidateList.create()
            .add(
                ArbitraryBuilderCandidateFactory.of(GenerateString::class.java)
                    .builder { it.set("value", Arbitraries.strings().numeric()) }
            )
            .add(
                ArbitraryBuilderCandidateFactory.of(GenerateInt::class.java)
                    .builder { it.set("value", Arbitraries.integers().between(1, 100)) }
            )
    }
}

FixtureMonkey.builder()
    .registerGroup(GenerateBuilderGroup())
    .build()

{{< /tab >}}
{{< /tabpane>}}

## Expression Strict Mode
> `useExpressionStrictMode`

When using expressions (especially String Expressions), it's hard to know if the expression you've written has a matching property, and the property is correctly adjusted.
Using the `useExpressionStrictMode` option will throw an IllegalArgumentException if the expression you wrote doesn't have a matching property.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

@Test
void test() {
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder().useExpressionStrictMode().build();

    thenThrownBy(
        () -> fixtureMonkey.giveMeBuilder(String.class)
            .set("nonExistentField", 0)
            .sample()
    ).isExactlyInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("No matching results for given NodeResolvers.");
}

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

@Test
fun test() {
    val fixtureMonkey = FixtureMonkey.builder().useExpressionStrictMode().build()

    assertThatThrownBy {
        fixtureMonkey.giveMeBuilder<String>()
            .set("nonExistentField", 0)
            .sample()
    }.isExactlyInstanceOf(IllegalArgumentException::class.java)
        .hasMessageContaining("No matching results for given NodeResolvers.")
}

{{< /tab >}}
{{< /tabpane>}}

## Constraining Java types
> `javaTypeArbitraryGenerator`, `javaTimeTypeArbitraryGenerator`

You can modify the default values for Java primitive types (such as strings, integers, doubles, etc.) by implementing a custom `JavaTypeArbitraryGenerator` interface.
This option can be applied through the `JqwikPlugin`.

For example, by default, string types generated with Fixture Monkey have unreadable data because it considers edge cases such as when control blocks are contained in strings.

If you prefer to generate strings consisting only of alphabetic characters, you can override the `JavaTypeArbitraryGenerator` as demonstrated below:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

FixtureMonkey.builder()
    .plugin(
        new JqwikPlugin()
              .javaTypeArbitraryGenerator(new JavaTypeArbitraryGenerator() {
                  @Override
                  public StringArbitrary strings() {
                      return Arbitraries.strings().alpha();
                  }
              })
    )
    .build();

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

FixtureMonkey.builder()
    .plugin(
        JqwikPlugin()
            .javaTypeArbitraryGenerator(object : JavaTypeArbitraryGenerator {
                override fun strings(): StringArbitrary = Arbitraries.strings().alpha()
            })
    )
    .build()

{{< /tab >}}
{{< /tabpane>}}

For Java time types, you can use `javaTimeTypeArbitraryGenerator`.

## Constraining Java types with annotations
> `javaArbitraryResolver`, `javaTimeArbitraryResolver`

Similar to using the javax-validation plugin and adding constraints to your Java typed properties, you can apply constraints to Java types using annotations.
To do this, you can implement a `JavaArbitraryResolver` interface.
This option can be applied through the `JqwikPlugin`.

For example, if you have a custom annotation named `MaxLengthOf10`, which means that the length of a property should be limited to a maximum of 10 characters, you can create a `JavaArbitraryResolver` as shown below:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

FixtureMonkey.builder()
    .plugin(
        new JqwikPlugin()
            .javaArbitraryResolver(new JavaArbitraryResolver() {
                @Override
                public Arbitrary<String> strings(StringArbitrary stringArbitrary, ArbitraryGeneratorContext context) {
                    if (context.findAnnotation(MaxLengthof10.class).isPresent()) {
                        return stringArbitrary.ofMaxLength(10);
                    }
                    return stringArbitrary;
                }
            })
    )
    .build();

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

FixtureMonkey.builder()
    .plugin(
        JqwikPlugin()
            .javaArbitraryResolver(object : JavaArbitraryResolver {
                override fun strings(stringArbitrary: StringArbitrary, context: ArbitraryGeneratorContext): Arbitrary<String> {
                    if (context.findAnnotation(MaxLengthof10::class.java).isPresent) {
                        return stringArbitrary.ofMaxLength(10)
                    }
                return stringArbitrary
                }
            })
    )
    .build()

{{< /tab >}}
{{< /tabpane>}}

## Null Options
### defaultNotNull
> `defaultNotNull`, `nullableContainer`, `nullableElement`

When you want to ensure that the properties of your instance are not null, you can utilize the options mentioned below.

- `defaultNotNull` determines whether a null property is generated. If true, property **cannot** be null
- `nullableContainer` determines whether a container property can be null. If true, container can be null
- `nullableElement` determines whether an element within a container property can be null. If true, element can be null.

By default, these three options are set to false. You can modify them to true as needed.

{{< alert icon="🚨" text="These options only apply to properties that do not have a nullable marker, such as @Nullable in Java or ? in Kotlin." />}}

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

FixtureMonkey fixtureMonkey = FixtureMonkey.builder().defaultNotNull(true).build();

FixtureMonkey fixtureMonkey = FixtureMonkey.builder().nullableContainer(true).build();

FixtureMonkey fixtureMonkey = FixtureMonkey.builder().nullableElement(true).build();


{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val fixtureMonkey = FixtureMonkey.builder().defaultNotNull(true).build()

val fixtureMonkey = FixtureMonkey.builder().nullableContainer(true).build()

val fixtureMonkey = FixtureMonkey.builder().nullableElement(true).build()

{{< /tab >}}
{{< /tabpane>}}

---------------
### NullInjectGenerator
> `defaultNullInjectGenerator`, `pushNullInjectGenerator`, `pushExactTypeNullInjectGenerator`, `pushAssignableTypeNullInjectGenerator`

In cases where a property should be null regardless of any nullable markers, you can make use of the options associated with the `NullInjectGenerator`.

The `defaultnullInjectGenerator` option allows you to set the probability of properties being null.

By default, the probability of a property being null is set to 20%.

If you want it to always be null, you can set it to `1.0d`. There are predefined values available in the DefaultNullInjectGenerator—`NOT_NULL_INJECT(0.0d)` and `ALWAYS_NULL_INJECT(1.0d)`—which you can import and use.

Alternatively, for more customized behavior, you can implement your own NullInjectGenerator.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .defaultNullInjectGenerator((context) -> NOT_NULL_INJECT) // you can use NOT_NULL_INJECT or write your probability as 0.4
    .build()

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val fixtureMonkey = FixtureMonkey.builder()
    .plugin(KotlinPlugin())
    .defaultNullInjectGenerator { NOT_NULL_INJECT } // you can use NOT_NULL_INJECT or write your probability as 0.4
    .build()

{{< /tab >}}
{{< /tabpane>}}

If you want to specifically change the probability of a certain type being null, you can use `pushNullInjectGenerator`.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .pushNullInjectGenerator(MatcherOperator.exactTypeMatchOperator(SimpleObject.class, (context) -> NOT_NULL_INJECT))
    .build();

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val fixtureMonkey = FixtureMonkey.builder()
    .pushNullInjectGenerator(
        exactTypeMatchOperator(
            Product::class.java,
            NullInjectGenerator { context -> NOT_NULL_INJECT }
        )
    )
    .build()

{{< /tab >}}
{{< /tabpane>}}

Registering an `ArbitraryBuilder` of a specific class with `register` that has the `.setNotNull("*")` setting will have the same effect.
