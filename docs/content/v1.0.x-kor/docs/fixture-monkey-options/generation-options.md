---
title: "Generation Options"
images: []
menu:
docs:
parent: "fixture-monkey-options"
identifier: "options"
weight: 52
---

Fixture Monkey는 원하는 설정과 일치하는 복합 객체를 생성하기 위한 다양한 옵션을 제공합니다.

이러한 옵션은 `FixtureMonkeyBuilder` 를 통해 접근할 수 있습니다.

## Property Generation
### PropertyGenerator
> `defaultPropertyGenerator`, `pushPropertyGenerator`, `pushAssignableTypePropertyGenerator`, `pushExactTypePropertyGenerator`

`PropertyGenerator` creates child properties of the given `ObjectProperty`.
`PropertyGenerator` 는 주어진 `ObjectProperty` 의 자식 프로퍼티를 생성합니다. 
The child property can be a field, JavaBeans property, method or constructor parameter within the parent `ObjectProperty`.
자식 프로퍼티는 부모 `ObjectProperty` 내의 필드, JavaBeans 프로퍼티, 메서드, 생성자 패러미터가 될 수 있습니다. 
There are scenarios where you might want to customize how these child properties are generated.
자식 프로퍼티들이 생성되는 방식을 사용자 정의하고자 하는 시나리오들이 있습니다.

The `PropertyGenerator` options allow you to specify how child properties of each type are generated.
`PropertyGenerator` 옵션으로 각 타입의 자식 프로퍼티가 어떻게 생성되는지 지정할 수 있습니다.
This option is mainly used when you want to exclude generating some properties when the parent property has abnormal child properties.
이 옵션은 주로 부모 프로퍼티가 비정상적인 자식 프로퍼티를 가질 때 일부 프로퍼티 생성을 제외하고자 할 때 사용됩니다.

{{< alert icon="📖" text="Notable implementations: 'FieldPropertyGenerator', 'JavaBeansPropertyGenerator'" />}}

### ObjectPropertyGenerator
> `defaultObjectPropertyGenerator`, `pushObjectPropertyGenerator`, `pushAssignableTypeObjectPropertyGenerator`, `pushExactTypeObjectPropertyGenerator`

`ObjectPropertyGenerator` generates the [`ObjectProperty`](../concepts/#objectProperty) based on a given context.
`ObjectPropertyGenerator` 는 주어진 컨텍스트에 기반하여 [`ObjectProperty`](../concepts/#objectProperty)를 생성합니다.
With options related to `ObjectPropertyGenerator` you can customize how the `ObjectProperty` is generated.
`ObjectPropertyGenerator` 관련 옵션을 사용하면 `ObjectProperty` 가 생성되는 방식을 사용자 정의할 수 있습니다.
{{< alert icon="📖" text="Notable implementations: 'DefaultObjectPropertyGenerator'" />}}

### ContainerPropertyGenerator
> `pushContainerPropertyGenerator`, `pushAssignableTypeContainerPropertyGenerator`, `pushExactTypeContainerPropertyGenerator`

The `ContainerPropertyGenerator` determines how to generate [`ContainerProperty`](../concepts/#containerProperty) within a given context.
`ContainerPropertyGenerator` 는 주어진 컨텍스트에서 [`ContainerProperty`](../concepts/#containerProperty) 를 생성하는 방법을 결정합니다.
With options related to `ContainerPropertyGenerator` you can customize how the `ContainerProperty` is generated.
`ContainerPropertyGenerator` 관련 옵션을 사용하면 `ContainerProperty` 가 생성되는 방식을 사용자 정의할 수 있습니다.
{{< alert icon="📖" text="Notable implementations: 'ArrayContainerPropertyGenerator', 'MapContainerPropertyGenerator'" />}}

-----------------

## Arbitrary Generation
An `Introspector` determines how Fixture Monkey creates objects by selecting the appropriate arbitrary generation strategy based on the provided context(which includes information about generated properties).
`Introspector` 는 생성된 프로퍼티에 대한 정보를 포함한 컨텍스트를 기반으로 적절한 임의 생성 전략을 선택하여 Fixture Monkey가 객체를 생성하는 방법을 결정합니다.
The object is then generated based on this arbitrary generated.
이후 이 객체는 임의 생성된 기반으로 생성됩니다.
You have the flexibility to create a custom `Introspector` by implementing your own `ArbitraryIntrospector`.

### ObjectIntrospector
> `objectIntrospcetor`

The `objectIntrospector` option allows you to specify the default behavior when generating an object.
객체 생성 시 기본 동작을 지정하는 `objectIntrospector` 관련 옵션을 사용하면 객체를 생성할 때 기본 동작을 지정할 수 있습니다.
As discussed in the [introspector section](../../generating-objects/introspector), you can alter the default introspector to use predefined introspectors provided by Fixture Monkey or create your own custom introspector.
[introspector section](../../generating-objects/introspector)에서 설명한 대로 기본 introspector를 변경하여 Fixture Monkey에서 제공하는 미리 정의된 introspector를 사용하거나 사용자 정의 introspector를 만들 수 있습니다.
{{< alert icon="📖" text="Notable implementations: 'BeanArbitraryIntrospector', 'BuilderArbitraryIntrospector'" />}}

### ArbitraryIntrospector
> `pushArbitraryIntrospector`, `pushAssignableTypeArbitraryIntrospector`, `pushExactTypeArbitraryIntrospector`

If you need to change the `ArbitraryIntrospector` for a specific type you can use the above options.
특정 유형에 대한 `ArbitraryIntrospector` 를 변경해야 하는 경우 위의 옵션을 사용할 수 있습니다.
{{< alert icon="📖" text="Notable implementations: 'BooleanIntrospector', 'EnumIntrospector'" />}}

### ContainerIntrospector
> pushContainerIntrospector

Especially for container types, you can change the `ArbitraryIntrospector` using the `pushContainerIntrospector` option.
특히 컨테이너 타입의 경우, `pushContainerIntrospector` 옵션을 사용하여 `ArbitraryIntrospector` 를 변경할 수 있습니다.
{{< alert icon="📖" text="Notable implementations: 'ListIntrospector', 'MapIntrospector'" />}}

-----------------

## Arbitrary Generation
The `ArbitraryIntrospector` is responsible for defining how Fixture Monkey creates objects by selecting the appropriate arbitrary generation strategy and generating an arbitrary.
`ArbitraryIntrospector` 는 적절한 임의 생성 전략을 선택하고 임의의 개체를 생성하여 Fixture Monkey가 개체를 생성하는 방법을 정의하는 역할을 담당합니다.
The actual creation of the final arbitrary(`CombinableArbitrary`) from the arbitrary is done by the `ArbitraryGenerator`, which handles the request by delegating to the `ArbitraryIntrospcetor`.
임의 객체에서 최종 임의 객체(`CombinableArbitrary`)를 실제로 생성하는 것은 `ArbitraryGenerator`에 의해 이루어지며, 이 객체는 `ArbitraryIntrospcetor`에 요청을 위임하여 요청을 처리합니다.
By using the `defaultArbitraryGenerator` option, you have the capability to customize the behavior of the `ArbitraryGenerator`.
`defaultArbitraryGenerator` 옵션을 사용하면 `ArbitraryGenerator` 의 동작을 커스터마이징할 수 있습니다.

For instance, you can create an arbitrary generator that produces unique values, as shown in the example below:
예를 들어 예시와 같이 고유한 값을 생성하는 임의의 생성기를 만들 수 있습니다:

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

## Excluding Classes or Packages from Generation
> `pushExceptGenerateType`, `addExceptGenerateClass`, `addExceptGenerateClasses`, `addExceptGeneratePackage`, `addExceptGeneratePackages`

If you want to exclude the generation of certain types or packages, you can use these options.
특정 유형이나 패키지의 생성을 제외하려면 다음 옵션을 사용할 수 있습니다.

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

## Container options

### Container Size
> `defaultArbitraryContainerInfoGenerator`, `pushArbitraryContainerInfoGenerator`

`ArbitraryContainerInfo` holds information about the minimum and maximum sizes of a Container type.
`ArbitraryContainerInfo`은 컨테이너 타입의 최소 및 최대 크기에 대한 정보를 보유합니다.
You can change the behavior by modifying the `ArbitraryContainerInfoGenerator` using related options.
관련 옵션을 사용하여 `ArbitraryContainerInfoGenerator` 를 수정하여 동작을 변경할 수 있습니다.
The following example demonstrates how to customize `ArbitraryContainerInfo` to set the size of all container types to 3.
다음 예는 모든 컨테이너 타입의 크기를 3으로 설정하도록 `ArbitraryContainerInfo`를 사용자 정의하는 방법을 보여줍니다.

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

### Adding Container type
> `addContainerType`

You can add a new custom Container type using the `addContainerType` option.
`addContainerType` 옵션을 사용하여 새 사용자 정의 컨테이너 타입을 추가할 수 있습니다.

Let's say you made a new custom Pair class in Java.
Java에서 새로운 사용자 지정 Pair 클래스를 만들었다고 가정해 보겠습니다.

You can use this container type by implementing a custom `ContainerPropertyGenerator`, `Introspector` and `DecomposedContainerValueFactory`.
이 컨테이너 타입은 커스텀 `ContainerPropertyGenerator`, `Introspector` 및 `DecomposedContainerValueFactory`를 구현하여 사용할 수 있습니다.

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

**custom Introspector:**
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

**custom `ContainerPropertyGenerator`:**
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

**custom `DecomposedContainerValueFactory`:**
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

## Validating Arbitraries
> `arbitraryValidator`

The `arbitraryValidator` option allows you to replace the default `arbitraryValidator` with your own custom arbitrary validator.

When an instance is `sampled`, the `arbitraryValidator` validates the arbitrary, and if it is invalid, it throws an exception.
This process is repeated 1,000 times, and if the instance is still invalid, a `TooManyFilterMissesException` would be thrown.

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

## Arbitrary Generation Retry Limits
> `generateMaxTries`, `generateUniqueMaxTries`

The `generateMaxTries` option allows you to control the maximum number of attempts to generate a valid object from an arbitrary.
If an object cannot be generated successfully after exceeding this limit (default is 1,000 attempts), a `TooManyFilterMissesException` will be thrown.

Additionally, Fixture Monkey ensures the generation of unique values for map keys and set elements.
The `generateUniqueMaxTries` option allows you to specify the maximum number of attempts (also defaults to 1,000) that will be made to generate this unique value.

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

## Interface Implementations
> `interfaceImplements`

`interfaceImplements` is an option used to specify the available implementations for an interface.

When you don't specify this option, an ArbitraryBuilder for an interface will always result in a null value when sampled.
However, when you do specify this option, Fixture Monkey will randomly generate one of the specified implementations whenever an ArbitraryBuilder for the interface is sampled.

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
