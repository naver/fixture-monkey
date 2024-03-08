---
title: "커스터마이징 옵션"
images: []
menu:
docs:
parent: "fixture-monkey-options"
identifier: "options"
weight: 53
---

Fixture Monkey는 `FixtureMonkeyBuilder` 를 통해 원하는 값을 가지도록 객체를 사용자 정의하거나 사용자 정의 프로퍼티 명을 사용할 수 있는 옵션도 제공합니다.

## 프로퍼티네임 리졸버
> `defaultPropertyNameResolver`, `pushPropertyNameResolver`, `pushAssignableTypePropertyNameResolver`, `pushExactTypePropertyNameResolver`

`PropertyNameResolver` 관련 옵션을 사용하면 프로퍼티를 참조하는 방법을 사용자 정의할 수 있습니다.

`defaultPropertyNameResolver` 옵션은 모든 타입에 대해 프로퍼티 명을 알아내는 방식을 변경하는 데 사용됩니다.
만약 특정 타입에 대해 변경을 수행하려면 `pushPropertyNameResolver` , `pushAssignableTypePropertyNameResolver` 또는 `pushExactTypePropertyNameResolver` 를 사용할 수 있습니다.

기본적으로 프로퍼티는 원래 이름으로 참조됩니다. 다음 예시를 통해 프로퍼티 명을 사용자 정의하는 방법을 살펴봅시다:

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

일반적으로, 프로퍼티 명은 기존 프로퍼티 명인 "productName" 으로 해석됩니다.
그러나 `pushPropertyNameResolver` 를 사용하면 String 타입의 프로퍼티는 이제 "string"이라는 이름으로 참조됩니다.

## 등록 옵션
> `register`, `registerGroup`, `registerExactType`, `registerAssignableType`

때로는 클래스가 특정 제약 조건을 항상 지켜야할 수 있습니다.
사용자 정의 API를 사용해 항상 `ArbitraryBuilder` 를 수정해야 한다면 번거로울 수 있고 코드가 길어질 수 있습니다.
이 경우, 기본 제약 조건을 충족하는 클래스들에 대해서 기본 `ArbitraryBuilder` 를 설정할 수 있습니다.

`register` 옵션은 특정 타입에 대한 `ArbitraryBuilder` 를 등록하는 것을 돕습니다.

예시의 다음 코드는 Product 클래스에 대한 `ArbitraryBuilder` 를 등록하는 방법을 보여줍니다.
이를 등록함으로써 `FixtureMonkey` 에 의해 생성된 모든 Product 인스턴스는 "0"보다 크거나 같은 id 값을 가질 것입니다.

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

ArbitraryBuilders들을 한 번에 등록하려면 `registerGroup` 옵션을 사용할 수 있습니다.
이 작업은 리플렉션 또는 `ArbitraryBuilderGroup` 인터페이스를 사용하여 수행할 수 있습니다.

**리플렉션 사용:**
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

**ArbitraryBuilderGroup 인터페이스 사용:**

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

## 표현식 엄격 모드
> `useExpressionStrictMode`

표현식(특히 문자열 표현식)을 사용할 때 작성한 표현식이 일치하는 프로퍼티를 가지는지, 프로퍼티가 올바르게 선택되었는지를 파악하기 어려울 수 있습니다.
`useExpressionStrictMode` 옵션을 사용하면 작성한 표현식이 일치하는 프로퍼티를 가지고 있지 않으면 IllegalArgumentException 예외를 던집니다.

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

## Java 타입 제한
> `javaTypeArbitraryGenerator`, `javaTimeTypeArbitraryGenerator`

사용자 정의 `JavaTypeArbitraryGenerator` 인터페이스를 구현하여 Java 기본 타입(string, integer, double 등)의 기본값을 수정할 수 있습니다.
이 옵션은 `JqwikPlugin` 을 통해 적용할 수 있습니다.

예를 들어, Fixture Monkey로 생성된 string 타입은 기본적으로 제어 블록이 문자열에 포함되어 있는 극단적인 경우를 고려하여 생성하기 때문에 읽기 어렵습니다.

알파벳 문자로만 구성된 문자열을 생성하려면 아래 예시처럼 `JavaTypeArbitraryGenerator` 를 재정의하면 됩니다:

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

Java time 타입의 경우, `javaTimeTypeArbitraryGenerator` 를 사용할 수 있습니다.

## 어노테이션을 사용한 Java 타입 제한
> `javaArbitraryResolver`, `javaTimeArbitraryResolver`

javax-validation 플러그인을 사용하여 Java 타입 프로퍼티에 제약 조건을 추가하는 것과 유사하게, 어노테이션을 사용하여 Java 타입에 제약 조건을 적용할 수 있습니다.
이는 `JavaArbitraryResolver` 인터페이스를 구현하면 됩니다.
이 옵션은 `JqwikPlugin` 을 통해 적용할 수 있습니다.

예를 들어, 프로퍼티의 길이를 최대 10자로 제한해야 한다는 의미의 `MaxLengthOf10` 이라는 사용자 지정 어노테이션이 있는 경우 아래와 같이 `JavaArbitraryResolver` 를 생성할 수 있습니다:

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

## Null 옵션
### defaultNotNull
> `defaultNotNull`, `nullableContainer`, `nullableElement`

인스턴스의 프로퍼티가 null이 아닌지 확인하려는 경우 아래에 언급된 옵션을 활용할 수 있습니다.

- `defaultNotNull` null 프로퍼티의 생성 여부를 결정합니다. true라면 프로퍼티가 null이 **될 수 없습니다**.
- `nullableContainer` 컨테이너 프로퍼티가 null이 될 수 있는지 여부를 결정합니다. true라면 컨테이너가 null이 될 수 있습니다.
- `nullableElement` 컨테이너 프로퍼티 내의 요소가 null이 될 수 있는지 여부를 결정합니다. true라면 요소가 null이 될 수 있습니다.

기본적으로 이 세 옵션은 false로 설정되어 있습니다. 필요에 따라 true로 변경할 수 있습니다.

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

특정 프로퍼티가 nullable 표시와 관계없이 null이어야 하는 경우, `NullInjectGenerator` 와 관련된 옵션을 사용할 수 있습니다.

`defaultnullInjectGenerator` 옵션을 사용하면 프로퍼티가 null이 될 확률을 설정할 수 있습니다.

기본적으로 프로퍼티가 null일 확률은 20%로 설정되어 있습니다.

항상 null이 되길 원한다면 `1.0d` 로 설정할 수 있습니다. DefaultNullInjectGenerator에는 `NOT_NULL_INJECT(0.0d)` 와 `ALWAYS_NULL_INJECT(1.0d)` 와 같은 미리 정의된 값이 있습니다. 이를 가져와서 사용할 수 있습니다.

사용자 정의된 동작을 더 추가하길 원하는 경우, 자체적으로 NullInjectGenerator를 구현할 수도 있습니다.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .defaultNullInjectGenerator((context) -> NOT_NULL_INJECT) // NOT_NULL_INJECT를 사용하거나 확률을 0.4로 설정할 수 있습니다.
    .build()

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val fixtureMonkey = FixtureMonkey.builder()
    .plugin(KotlinPlugin())
    .defaultNullInjectGenerator { NOT_NULL_INJECT } // NOT_NULL_INJECT를 사용하거나 확률을 0.4로 설정할 수 있습니다.
    .build()

{{< /tab >}}
{{< /tabpane>}}

특정 타입이 null이 될 확률을 구체적으로 변경하려면 `pushNullInjectGenerator` 를 사용하면 됩니다.

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

특정 클래스의 `ArbitraryBuilder` 를 `register` 로 등록하면 `.setNotNull("*")` 설정과 동일한 결과를 얻을 수 있습니다.
