---
title: "Customizing Interface"
sidebar_position: 45
---


The `ArbitraryBuilder` API is also valid within the interface. 
You can customize the interface properties regardless of the interface type as said in [Generating Interface Type](../generating-objects/generating-interface)
The interface type refers to `interface`, `generic interface` ,`selaed interface`.

```java
public interface StringSupplier {
	String getValue();
}

FixtureMonkey fixture = FixtureMonkey.create();

String result = fixture.giveMeBuilder(StringSupplier.class)
	.set("value", "fix")
	.sample()
	.getValue();
```

The properties of the interface in `ArbitraryBuilder` differ in the implementation.
Unfortunately, there is currently no `ArbitraryBuilder` API that resolves the implementation of the interface. 
Unless the interface has only one implementation, you can customize the properties of the interface, not the implementation.

You cannot generate the randomly populated intended implementation, but you can generate the fixed implementation by using the `set` API.
There are two cases, `with the interfaceImplements option` and `without the interfaceImplements option`.

### With the option
The interface type is resolved to the implementation when you use the `set` API with the option.

```java
public interface ObjectValueSupplier {
	Object getValue();
}

public class StringValueSupplier implements ObjectValueSupplier {
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

public class IntegerValueSupplier implements ObjectValueSupplier {
	private final int value;
	private final int implementationValue;

	@ConstructorProperties({"value", "implementationValue"}) // It is not needed if you are using Lombok.
	public IntegerValueSupplier(int value, int implementationValue) {
		this.value = value;
		this.implementationValue = implementationValue;
	}

	@Override
	public Integer getValue() {
		return value;
	}

	public int getImplementationValue() {
		return implementationValue;
	}
}

FixtureMonkey fixture = FixtureMonkey.builder()
	.objectIntrospector(
		ConstructorPropertiesArbitraryIntrospector.INSTANCE) // used for instantiate implementations of ObjectValueSupplier
	.plugin(
		new InterfacePlugin()
			.interfaceImplements(
				ObjectValueSupplier.class,
				List.of(StringValueSupplier.class, IntegerValueSupplier.class)
            )
	)
	.build();

IntegerValueSupplier integerValueSupplier = (IntegerValueSupplier)fixture.giveMeBuilder(ObjectValueSupplier.class)
	.set("$", new IntegerValueSupplier(-1203))
	.sample();
```

You can also change the properties of the implementation.

```java
IntegerValueSupplier integerValueSupplier = (IntegerValueSupplier)fixture.giveMeBuilder(ObjectValueSupplier.class)
	.set("$", new IntegerValueSupplier(-1203, 1203))
	.set("implementationValue", 1) // it works.
	.sample();
```

### Without the option
You have to use the `set` API in a specific way with `Values.just`.

```java
public interface ObjectValueSupplier {
	Object getValue();
}

public class StringValueSupplier implements ObjectValueSupplier {
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

public class IntegerValueSupplier implements ObjectValueSupplier {
	private final int value;

	@ConstructorProperties("value") // It is not needed if you are using Lombok.
	public IntegerValueSupplier(int value) {
		this.value = value;
	}

	@Override
	public Integer getValue() {
		return value;
	}
}

FixtureMonkey fixture = FixtureMonkey.builder()
	.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE) // used for instantiate implementations of ObjectValueSupplier
	.build();

IntegerValueSupplier integerValueSupplier = (IntegerValueSupplier)fixture.giveMeBuilder(ObjectValueSupplier.class)
	.set("$", Values.just(new IntegerValueSupplier(-1203)))
	.sample();
```

The implementation is not used by `interfaceImplements`, Fixture Monkey does not know the properties of the implementation. You cannot set the properties of the implementation.

```java
IntegerValueSupplier integerValueSupplier = (IntegerValueSupplier)fixture.giveMeBuilder(ObjectValueSupplier.class)
    .set("$", Values.just(new IntegerValueSupplier(-1203)))
    .set("value", 1) // not works
    .sample();
```

