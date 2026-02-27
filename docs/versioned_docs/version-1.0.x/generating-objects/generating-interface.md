---
title: "Generating Interface Type"
sidebar_position: 34
---


Fixture Monkey is able to generate complex interface objects consisting
of `interface`, `generic interface`, `sealed interface`.

Fixture Monkey provides the default implementations of certain interfaces.
For example, `ArrayList` is for the `List` interface, `HashSet` is for the `Set` interface.

Except in those cases, you should specify the implementations of the interface. 
If you do not, Fixture Monkey will generate an anonymous object for you.
You do not need to specify the implementations in the case of `sealed interface`.

Let's see how to create an interface with a detailed example.

### Simple Interface

```java
public interface StringSupplier {
	String getValue();
}

public class DefaultStringSupplier implements StringSupplier {
	private final String value;

	@ConstructorProperties("value") // It is not needed if you are using Lombok.
	public DefaultStringSupplier(String value) {
		this.value = value;
	}

	@Override
	public String getValue() {
		return "default" + value;
	}
}
```

#### Without options

Without options, Fixture Monkey will generate an anonymous object of `StringSupplier`.

```java
FixtureMonkey fixture = FixtureMonkey.create();

StringSupplier result = fixtureMonkey.giveMeOne(StringSupplier.class);
```

The generated instance `result` is an anonymous object of `StringSupplier`. The getter `getValue` returns the arbitrary
String value. It can be null just like the property of `Clsas`. You need to know that it works same
as `DefaultStringSupplier`, but it is not type of `DefaultStringSupplier`.

:::tip[notice]
Fixture Monkey only generates the properties of the anonymous object listed below.

- Methods follows the naming convention of getter
- No parameter methods
:::

The generated properties can be customized just like the properties of `Class`.

```java
FixtureMonkey fixture = FixtureMonkey.create();

String result = fixture.giveMeBuilder(StringSupplier.class)
	.set("value", "fix")
	.sample()
	.getValue();
```

The `result` is now set to `fix`. You can use all the APIs in `ArbitraryBuilder`.

#### With options

You can extend the implementations of the interface by using the `InterfacePlugin#interfaceImplements` option.

:::tip[notice]
All the options related to interface or abstract class are in `InterfacePlugin`
:::

```java
FixtureMonkey fixture = FixtureMonkey.builder()
	.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE) // used for instantiate DefaultStringSupplier
	.plugin(
		new InterfacePlugin()
			.interfaceImplements(StringSupplier.class, List.of(DefaultStringSupplier.class))
	)
	.build();

DefaultStringSupplier stringSupplier = (DefaultStringSupplier)fixture.giveMeOne(StringSupplier.class);
```

The `InterfacePlugin#interfaceImplements` option can be used multiple times. For example, the default implementation of `List`
is `ArrayList`. What if you use `Interface#interfaceImplements` to implement `LinkedList` like this?

```java
FixtureMonkey fixture = FixtureMonkey.builder()
	.plugin(
		new InterfacePlugin()
			.interfaceImplements(List.class, List.of(LinkedList.class))
	)
	.build();

List<String> list = fixture.giveMeOne(new TypeReference<List<String>>() {
});

// list will be an instance of ArrayList or LinkedList
```

The implementations of `List` are `ArrayList` and `LinkedList`.

The `InterfacePlugin#interfaceImplements` option can also be resolved as an interface.

Without options, Fixture Monkey will not generate an implementation of the `Collection` interface. 
Let `Collection` be `List` through the `InterfacePlugin#interfaceImplements` option.
The implementations of the `List` interface propagate to the `Collection` interface.
In detail, the `List` interface is generated as an implementation of `ArrayList`, the `Collection` interface is also be `ArrayList`.

```java
FixtureMonkey fixture = FixtureMonkey.builder()
	.plugin(
		new InterfacePlugin()
			.interfaceImplements(Collection.class, List.of(List.class))
	)
	.build();

ArrayList<String> collection = (ArrayList<String>)fixture.giveMeOne(new TypeReference<Collection<String>>() {
});

// collection will be an instance of ArrayList
```

You can use this option below if there are too many implementations to add using the options, but they have a similar pattern.

```java
interface ObjectValueSupplier {
    Object getValue();
}

interface StringValueSupplier extends ObjectValueSupplier {
    String getValue();
}

public class DefaultStringValueSupplier implements StringValueSupplier {
    private final String value;

    @ConstructorProperties("value") // It is not needed if you are using Lombok.
    public DefaultStringValueSupplier(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return this.value;
    }
}

interface IntegerValueSupplier extends ObjectValueSupplier {
    Integer getValue();
}

public class DefaultIntegerValueSupplier implements IntegerValueSupplier {
    private final Integer value;

    @ConstructorProperties("value") // It is not needed if you are using Lombok.
    public DefaultIntegerValueSupplier(Integer value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return this.value;
    }
}

FixtureMonkey fixture = FixtureMonkey.builder()
	.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE) // used for instantiate implementations of ObjectValueSupplier
	.plugin(
		new InterfacePlugin()
			.interfaceImplements(
				new AssignableTypeMatcher(ObjectValueSupplier.class),
				property -> {
					Class<?> actualType = Types.getActualType(property.getType());
					if (StringValueSupplier.class.isAssignableFrom(actualType)) {
						return List.of(PropertyUtils.toProperty(DefaultStringValueSupplier.class));
					}

					if (IntegerValueSupplier.class.isAssignableFrom(actualType)) {
						return List.of(PropertyUtils.toProperty(DefaultIntegerValueSupplier.class));
					}
					return List.of();
				}
			)
	)
	.build();

DefaultStringValueSupplier stringValueSupplier = (DefaultStringValueSupplier)fixture.giveMeOne(StringValueSupplier.class);
DefaultIntegerValueSupplier integerValueSupplier = (DefaultIntegerValueSupplier)fixture.giveMeOne(IntegerValueSupplier.class);
```

### Generic Interfaces

What if we need to generate some complex generic interface? All you have to do is do what you did with the simple
interface above.

```java
public interface ObjectValueSupplier<T> {
	T getValue();
}

public class StringValueSupplier implements ObjectValueSupplier<String> {
	private final String value;

	@ConstructorProperties("value") // It is not needed if you are using Lombok.
	public StringValueSupplier(String value) {
		this.value = value;
	}

	@Override
	public String getValue() {
		return value;
	}
}

FixtureMonkey fixture = FixtureMonkey.builder()
	.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE) // used for instantiate StringValueSupplier
	.plugin(
		new InterfacePlugin()
			.interfaceImplements(ObjectValueSupplier.class, List.of(StringValueSupplier.class))
	)
	.build();

StringValueSupplier stringSupplier = (StringValueSupplier)fixture.giveMeOne(ObjectValueSupplier.class);

```

### Sealed Interface

Sealed interface is simpler. You do not need options.

```java
sealed interface SealedStringSupplier {
	String getValue();
}

public static final class SealedDefaultStringSupplier implements SealedStringSupplier {
	private final String value;

	@ConstructorProperties("value") // It is not needed if you are using Lombok.
	public SealedDefaultStringSupplier(String value) {
		this.value = value;
	}

	@Override
	public String getValue() {
		return "sealed" + value;
	}
}

FixtureMonkey fixture = FixtureMonkey.builder()
	.objectIntrospector(
		ConstructorPropertiesArbitraryIntrospector.INSTANCE) // used for instantiate SealedDefaultStringSupplier
	.build();

SealedDefaultStringSupplier stringSupplier = (SealedDefaultStringSupplier)fixture.giveMeOne(SealedStringSupplier.class);
```

This chapter illustrates how to create an interface type. If you get stuck, all you need to remember is the `InterfacePlugin' plugin.
If the plugin doesn't solve your problem, please post a bug with a reproducible example.

