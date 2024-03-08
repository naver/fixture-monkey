---
title: "생성 옵션"
images: []
menu:
docs:
parent: "fixture-monkey-options"
identifier: "options"
weight: 52
---

Fixture Monkey는 원하는 설정과 일치하는 복잡한 객체를 생성하기 위한 다양한 옵션을 제공합니다.

이러한 옵션은 `FixtureMonkeyBuilder` 를 통해 접근할 수 있습니다.

## 프로퍼티 생성
### PropertyGenerator
> `defaultPropertyGenerator`, `pushPropertyGenerator`, `pushAssignableTypePropertyGenerator`, `pushExactTypePropertyGenerator`

`PropertyGenerator` 는 주어진 `ObjectProperty` 의 자식 프로퍼티를 생성합니다.
자식 프로퍼티는 부모 `ObjectProperty` 내의 필드, JavaBeans 프로퍼티, 메서드, 생성자 파라미터가 될 수 있습니다. 
이러한 자식 프로퍼티가 생성되는 방식을 사용자 정의하는 몇 가지 방법이 있습니다.

`PropertyGenerator` 옵션은 각 타입의 자식 프로퍼티가 생성되는 방식을 지정할 수 있습니다.
이 옵션은 주로 부모 프로퍼티가 비정상적인 자식 프로퍼티를 가질 때 일부 프로퍼티 생성을 제외하고자 할 때 사용됩니다.

{{< alert icon="📖" text="Notable implementations: 'FieldPropertyGenerator', 'JavaBeansPropertyGenerator'" />}}

### ObjectPropertyGenerator
> `defaultObjectPropertyGenerator`, `pushObjectPropertyGenerator`, `pushAssignableTypeObjectPropertyGenerator`, `pushExactTypeObjectPropertyGenerator`

`ObjectPropertyGenerator` 는 주어진 컨텍스트에 기반하여 [`ObjectProperty`](../concepts/#objectProperty)를 생성합니다.
`ObjectPropertyGenerator` 관련 옵션을 사용하면 `ObjectProperty` 가 생성되는 방식을 사용자 정의할 수 있습니다.

{{< alert icon="📖" text="Notable implementations: 'DefaultObjectPropertyGenerator'" />}}

### ContainerPropertyGenerator
> `pushContainerPropertyGenerator`, `pushAssignableTypeContainerPropertyGenerator`, `pushExactTypeContainerPropertyGenerator`

`ContainerPropertyGenerator` 는 주어진 컨텍스트에서 [`ContainerProperty`](../concepts/#containerProperty) 를 생성하는 방법을 결정합니다.
`ContainerPropertyGenerator` 관련 옵션을 사용하면 `ContainerProperty` 가 생성되는 방식을 사용자 정의할 수 있습니다.

{{< alert icon="📖" text="Notable implementations: 'ArrayContainerPropertyGenerator', 'MapContainerPropertyGenerator'" />}}

-----------------

## Arbitrary 생성
`Introspector` 는 생성된 프로퍼티에 대한 정보를 포함한 컨텍스트를 기반으로 Arbitrary 생성 전략을 선택하여 Fixture Monkey가 객체를 생성하는 방법을 결정합니다.
그 다음에 Arbitrary 생성 전략을 기반으로 객체가 생성됩니다.
사용자는 직접 `ArbitraryIntrospector` 를 구현하여 사용자 정의 `Introspector` 를 생성할 수 있는 유연성을 가지게 됩니다.

### ObjectIntrospector
> `objectIntrospcetor`

`objectIntrospector` 관련 옵션을 사용하면 객체 생성 시 기본 동작을 지정할 수 있습니다.
[introspector section](../../generating-objects/introspector)에서 언급한 대로 기본 introspector를 변경하여 Fixture Monkey에서 제공하는 미리 정의된 introspector를 사용하거나 사용자 정의 introspector를 만들 수 있습니다.

{{< alert icon="📖" text="Notable implementations: 'BeanArbitraryIntrospector', 'BuilderArbitraryIntrospector'" />}}

### ArbitraryIntrospector
> `pushArbitraryIntrospector`, `pushAssignableTypeArbitraryIntrospector`, `pushExactTypeArbitraryIntrospector`

특정 타입에 대한 `ArbitraryIntrospector` 를 변경해야 하는 경우 위의 옵션을 사용할 수 있습니다.

{{< alert icon="📖" text="Notable implementations: 'BooleanIntrospector', 'EnumIntrospector'" />}}

### ContainerIntrospector
> pushContainerIntrospector

특히 컨테이너 타입의 경우, `pushContainerIntrospector` 옵션을 사용하여 `ArbitraryIntrospector` 를 변경할 수 있습니다.

{{< alert icon="📖" text="Notable implementations: 'ListIntrospector', 'MapIntrospector'" />}}

-----------------

## Arbitrary 생성
`ArbitraryIntrospector` 는 적절한 Arbitrary 생성 전략을 선택하고 Arbitrary를 생성하여 Fixture Monkey가 객체를 생성하는 방법을 정의하는 역할을 합니다.
Arbitrary 객체에서 최종 Arbitrary 객체(`CombinableArbitrary`)를 실제로 생성하는 것은 `ArbitraryGenerator` 에 의해 이루어지며, 이 객체는 `ArbitraryIntrospcetor` 에 요청을 위임하여 요청을 처리합니다.
`defaultArbitraryGenerator` 옵션을 사용하면 `ArbitraryGenerator` 의 동작을 사용자 정의 할 수 있습니다.

예를 들어, 아래의 예시와 같이 고유한 값을 생성하는 arbitrary generator를 만들 수 있습니다:

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

public static class UniqueArbitraryGenerator implements ArbitraryGenerator {
    private static final Set<Object> UNIQUE = new HashSet<>();

    private final ArbitraryGenerator delegate;

    public UniqueArbitraryGenerator(ArbitraryGenerator delegate) {
        this.delegate = delegate;
    }

    @Override
    public CombinableArbitrary generate(ArbitraryGeneratorContext context) {
        return delegate.generate(context)
            .filter(
                obj -> {
                    if (!UNIQUE.contains(obj)) {
                        UNIQUE.add(obj);
                        return true;
                    }
                    return false;
                }
          );
    }
}

FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .defaultArbitraryGenerator(UniqueArbitraryGenerator::new)
    .build();

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

class UniqueArbitraryGenerator(private val delegate: ArbitraryGenerator) : ArbitraryGenerator {
    companion object {
    private val UNIQUE = HashSet<Any>()
    }

    override fun generate(context: ArbitraryGeneratorContext): CombinableArbitrary {
        return delegate.generate(context)
            .filter { obj ->
                if (!UNIQUE.contains(obj)) {
                    UNIQUE.add(obj)
                    true
                } else {
                    false
                }
            }
    }
}

val fixtureMonkey = FixtureMonkey.builder()
    .defaultArbitraryGenerator { UniqueArbitraryGenerator(it) }
    .build()

{{< /tab >}}
{{< /tabpane>}}

{{< alert icon="📖" text="Notable implementations: 'IntrospectedArbitraryGenerator', 'CompositeArbitraryGenerator'" />}}

-----------------

## 클래스, 패키지 생성 제외 옵션
> `pushExceptGenerateType`, `addExceptGenerateClass`, `addExceptGenerateClasses`, `addExceptGeneratePackage`, `addExceptGeneratePackages`

특정 타입이나 패키지의 생성을 제외하려면 다음 옵션을 사용할 수 있습니다.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

@Test
void testExcludeClass() {
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .addExceptGenerateClass(String.class)
        .build();

    String actual = sut.giveMeOne(Product.class)
      .getProductName();

    then(actual).isNull();
}

@Test
void testExcludePackage() {
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .addExceptGeneratePackage("java.lang")
        .build();

    String actual = sut.giveMeOne(String.class);

    then(actual).isNull();
}

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

@Test
fun testExcludeClass() {
    val fixtureMonkey = FixtureMonkey.builder()
        .addExceptGenerateClass(String::class.java)
        .build()

    val actual = fixtureMonkey.giveMeOne<Product>()
        .productName

    then(actual).isNull()
}

@Test
fun testExcludePackage() {
    val fixtureMonkey = FixtureMonkey.builder()
        .addExceptGeneratePackage("java.lang")
        .build()

    val actual = fixtureMonkey.giveMeOne<String>()

    then(actual).isNull()
}

{{< /tab >}}
{{< /tabpane>}}

-----------------

## 컨테이너 옵션

### 컨테이너 크기
> `defaultArbitraryContainerInfoGenerator`, `pushArbitraryContainerInfoGenerator`

`ArbitraryContainerInfo` 는 컨테이너 타입의 최소, 최대 크기에 대한 정보를 가집니다.
관련 옵션을 사용하여 `ArbitraryContainerInfoGenerator` 를 수정함으로써 동작 방식을 변경할 수 있습니다.
다음 예시는 `ArbitraryContainerInfo` 의 모든 컨테이너 타입의 크기를 3으로 설정하도록 사용자 정의하는 방법을 설명합니다.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

@Test
void test() {
    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .defaultArbitraryContainerInfoGenerator(context -> new ArbitraryContainerInfo(3, 3))
        .build();

    List<String> actual = fixtureMonkey.giveMeOne();

    then(actual).hasSize(3);
}

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

@Test
fun test() {
    val fixtureMonkey = FixtureMonkey.builder()
        .defaultArbitraryContainerInfoGenerator { context -> ArbitraryContainerInfo(3, 3) }
        .build()

    val actual: List<String> = fixtureMonkey.giveMeOne()

    then(actual).hasSize(3)
}

{{< /tab >}}
{{< /tabpane>}}

### 컨테이너 타입 추가
> `addContainerType`

`addContainerType` 옵션을 사용하여 새로운 사용자 정의 컨테이너 타입을 추가할 수 있습니다.

Java에서 새로운 사용자 정의 Pair 클래스를 만든다고 가정해보겠습니다.

이 컨테이너 타입은 사용자 정의 `ContainerPropertyGenerator`, `Introspector`, `DecomposedContainerValueFactory` 를 구현하여 사용할 수 있습니다.
```java
FixtureMonkey fixtureMonkey=FixtureMonkey.builder()
    .addContainerType(
        Pair.class,
        new PairContainerPropertyGenerator(),
        new PairIntrospector(),
        new PairDecomposedContainerValueFactory()
    )
	.build();
```

**사용자 정의 Introspector:**
```java
public class PairIntrospector implements ArbitraryIntrospector, Matcher {
	private static final Matcher MATCHER = new AssignableTypeMatcher(Pair.class);

	@Override
	public boolean match(Property property) {
		return MATCHER.match(property);
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		ArbitraryProperty property = context.getArbitraryProperty();
		ArbitraryContainerInfo containerInfo = property.getContainerProperty().getContainerInfo();
		if (containerInfo == null) {
			return ArbitraryIntrospectorResult.EMPTY;
		}

		List<Arbitrary<?>> childrenArbitraries = context.getChildrenArbitraryContexts().getArbitraries();
		BuilderCombinator<List<Object>> builderCombinator = Builders.withBuilder(ArrayList::new);
		for (Arbitrary<?> childArbitrary : childrenArbitraries) {
			builderCombinator = builderCombinator.use(childArbitrary).in((list, element) -> {
				list.add(element);
				return list;
			});
		}

		return new ArbitraryIntrospectorResult(
			builderCombinator.build(it -> new Pair<>(it.get(0), it.get(1)))
		);
	}
}
```

**사용자 정의 `ContainerPropertyGenerator`:**
```java
public class PairContainerPropertyGenerator implements ContainerPropertyGenerator {
	@Override
	public ContainerProperty generate(ContainerPropertyGeneratorContext context) {
		com.navercorp.fixturemonkey.api.property.Property property = context.getProperty();

		List<AnnotatedType> elementTypes = Types.getGenericsTypes(property.getAnnotatedType());
		if (elementTypes.size() != 2) {
			throw new IllegalArgumentException(
				"Pair elementsTypes must be have 1 generics type for element. "
					+ "propertyType: " + property.getType()
					+ ", elementTypes: " + elementTypes
			);
		}

		AnnotatedType firstElementType = elementTypes.get(0);
		AnnotatedType secondElementType = elementTypes.get(1);
		List<com.navercorp.fixturemonkey.api.property.Property> elementProperties = new ArrayList<>();
		elementProperties.add(
			new ElementProperty(
				property,
				firstElementType,
				0,
				0
			)
		);
		elementProperties.add(
			new ElementProperty(
				property,
				secondElementType,
				1,
				1
			)
		);

		return new ContainerProperty(
			elementProperties,
			new ArbitraryContainerInfo(1, 1, false)
		);
	}
}
```

**사용자 정의 `DecomposedContainerValueFactory`:**
```java
public class PairDecomposedContainerValueFactory implements DecomposedContainerValueFactory {
	@Override
	public DecomposedContainerValue from(Object object) {
		Pair<?, ?> pair = (Pair<?, ?>)obj;
		List<Object> list = new ArrayList<>();
		list.add(pair.getFirst());
		list.add(pair.getSecond());
		return new DecomposableContainerValue(list, 2);
	}
}
```

-----------------

## Arbitraries 유효성 검증
> `arbitraryValidator`

`arbitraryValidator` 옵션을 사용하면 기본 `arbitraryValidator` 를 사용자 정의 Arbitrary 유효성 검사기로 대체할 수 있습니다.
인스턴스가 `sampled` 되면 `arbitraryValidator` 는 Arbitrary의 유효성을 검사하고 유효하지 않은 경우 예외를 던집니다.
이 프로세스는 1,000회 반복되며, 인스턴스가 여전히 유효하지 않다면 `TooManyFilterMissesException` 예외를 던질 것입니다.

{{< alert icon="📖" text="Notable implementations: 'JakartaArbitraryValidator', 'JavaxArbitraryValidator'" />}}

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .arbitraryValidator(obj -> {
        throw new ValidationFailedException("thrown by custom ArbitraryValidator", new HashSet<>());
    })
    .build();

thenThrownBy(() -> fixtureMonkey.giveMeOne(String.class))
    .isExactlyInstanceOf(FilterMissException.class);

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val fixtureMonkey = FixtureMonkey.builder()
    .arbitraryValidator { obj ->
        throw ValidationFailedException("thrown by custom ArbitraryValidator", HashSet())
    }
    .build()

assertThatThrownBy { fixtureMonkey.giveMeOne<String>() }
    .isExactlyInstanceOf(FilterMissException::class.java)

{{< /tab >}}
{{< /tabpane>}}

-----------------

## Arbitrary 생성 재시도 제한
> `generateMaxTries`, `generateUniqueMaxTries`

`generateMaxTries` 옵션을 사용하면 Arbitrary 객체에서 유효한 객체를 생성하기 위한 최대 시도 횟수를 제한할 수 있습니다.
제한(기본값 1,000회)을 초과하여 객체를 성공적으로 생성할 수 없는 경우, `TooManyFilterMissesException` 예외를 던질 것입니다.

추가적으로, Fixture Monkey는 Map의 키(key)와 Set의 요소(element)에 대한 고유한 값 생성을 보장합니다.
`generateUniqueMaxTries` 옵션을 사용하면 이 고유한 값을 생성하기 위해 시도 최대 횟수(기본값 1,000회)를 지정할 수 있습니다.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}

FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .generateMaxTries(100)
    .generateUniqueMaxTries(100)
    .build();

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

val fixtureMonkey = FixtureMonkey.builder()
    .generateMaxTries(100)
    .generateUniqueMaxTries(100)
    .build()

{{< /tab >}}
{{< /tabpane>}}

-----------------

## 인터페이스 구현
> `interfaceImplements`

`interfaceImplements` 은 인터페이스에 사용 가능한 구현체를 지정할 때 사용되는 옵션입니다.

이 옵션을 지정하지 않으면, 인터페이스에 대한 ArbitraryBuilder가 샘플링될 때 항상 null 값을 반환합니다.
하지만 이 옵션을 지정하면 Fixture Monkey는 인터페이스에 대한 ArbitraryBuilder를 샘플링할 때마다 지정된 구현체 중 하나를 임의로 생성합니다.

{{< tabpane persist=false >}}
{{< tab header="Java" lang="java">}}
interface FixedValue {
    Object get();
}

class IntegerFixedValue implements FixedValue {
    @Override
    public Object get() {
        return 1;
    }
}

class StringFixedValue implements FixedValue {
    @Override
    public Object get() {
        return "fixed";
    }
}

class GenericFixedValue<T> {
    T value;
}

@Test
void sampleGenericInterface() {
    // given
    List<Class<? extends FixedValue>> implementations = new ArrayList<>();
    implementations.add(IntegerFixedValue.class);
    implementations.add(StringFixedValue.class);

    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .interfaceImplements(FixedValue.class, implementations)
        .build();

    // when
    Object actual = fixtureMonkey.giveMeBuilder(new TypeReference<GenericGetFixedValue<FixedValue>>() {})
        .setNotNull("value")
        .sample()
        .getValue()
        .get();

    // then
    then(actual).isIn(1, "fixed");
}

{{< /tab >}}
{{< tab header="Kotlin" lang="kotlin">}}

interface FixedValue {
    fun get(): Any
}

class IntegerFixedValue : FixedValue {
    override fun get(): Any { 
        return 1
    }
}

class StringFixedValue : FixedValue {
    override fun get(): Any {
        return "fixed"
    }
}

class GenericFixedValue<T> {
    val value: T
}

@Test
fun sampleGenericInterface() {
    // given
    val implementations: MutableList<Class<out FixedValue>> = List.of(IntegerFixedValue::class.java, StringFixedValue::class.java)

    val fixtureMonkey = FixtureMonkey.builder()
        .interfaceImplements(FixedValue::class.java, implementations)
        .build()

    // when
    val actual = fixtureMonkey.giveMeBuilder<GenericGetFixedValue<FixedValue>>()
        .sample()
        .getValue()
        .get()

    // then
    then(actual).isIn(1, "fixed")
}

{{< /tab >}}
{{< /tabpane>}}
