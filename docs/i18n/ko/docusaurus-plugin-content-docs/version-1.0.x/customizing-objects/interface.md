---
title: "인터페이스 커스터마이징"
sidebar_position: 45
---


인터페이스에서 `ArbitraryBuilder`의 모든 API를 사용할 수 있습니다.
우리는 이미 [Generating Interface Type](../generating-objects/generating-interface) 에서 API를 사용한 예를 확인했었습니다.
알아본 내용을 복습하면, 인터페이스의 종류에는 `interface`, `generic interface` ,`selaed interface`이 있습니다.
인터페이스의 종류와 상관없이 모든 인터페이스의 프로퍼티를 제어할 수 있습니다.

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

인터페이스를 생성하는 `ArbitraryBuilder`가 제어할 수 있는 프로퍼티는 실제로 생성한 구현체마다 다릅니다.
하지만 아직은 구현체를 선택할 수 있는 `ArbitraryBuilder` API가 존재하지 않습니다. 
인터페이스가 하나의 구현체를 가지고 있다면 구현체 프로퍼티도 제어가 가능합니다. 하지만 구현체가 두 개 이상이라면 인터페이스의 프로퍼티만 제어가 가능합니다. 


원하는 구현체를 랜덤하게 생성할 수는 없지만 이미 구현체의 인스턴스가 있다면 원하는 구현체를 `set` API를 사용해 생성할 수 있습니다.
`interfaceImplements 옵션을 사용한 경우`와 `interfaceImplements 옵션을 사용하지 않은 경우` 두 경우로 나누어 설명을 해보겠습니다.

### 옵션을 사용한 경우
인터페이스 타입은 `set` API로 설정한 구현체로 생성합니다. 구현체는 `interfaceImplements` 옵션에 등록을 해야 합니다.

```java
public interface ObjectValueSupplier {
	Object getValue();
}

public class StringValueSupplier implements ObjectValueSupplier {
	private final String value;

	@ConstructorProperties("value") // 롬복을 사용하면 추가하지 않아도 됩니다.
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

	@ConstructorProperties({"value", "implementationValue"}) // 롬복을 사용하면 추가하지 않아도 됩니다.
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
		ConstructorPropertiesArbitraryIntrospector.INSTANCE) // 구현체를 인스턴스화할 때 사용합니다.
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

구현체의 프로퍼티도 제어할 수 있습니다. 

```java
IntegerValueSupplier integerValueSupplier = (IntegerValueSupplier)fixture.giveMeBuilder(ObjectValueSupplier.class)
	.set("$", new IntegerValueSupplier(-1203, 1203))
	.set("implementationValue", 1) // it works.
	.sample();
```

### Without the option
원하는 구현체를 설정하기 위해서는 `set` API 를 특별한 방식으로 사용해야 합니다. 구현체 인스턴스를 `Values.just`로 감싸서 입력해야 합니다.

```java
public interface ObjectValueSupplier {
	Object getValue();
}

public class StringValueSupplier implements ObjectValueSupplier {
	private final String value;

	@ConstructorProperties("value") // 롬복을 사용하면 추가하지 않아도 됩니다.
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

	@ConstructorProperties("value") // 롬복을 사용하면 추가하지 않아도 됩니다.
	public IntegerValueSupplier(int value) {
		this.value = value;
	}

	@Override
	public Integer getValue() {
		return value;
	}
}

FixtureMonkey fixture = FixtureMonkey.builder()
	.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE) // 구현체를 인스턴스화할 때 사용합니다.
	.build();

IntegerValueSupplier integerValueSupplier = (IntegerValueSupplier)fixture.giveMeBuilder(ObjectValueSupplier.class)
	.set("$", Values.just(new IntegerValueSupplier(-1203)))
	.sample();
```

픽스쳐 몽키는 `interfaceImplements` 옵션으로 등록하지 않은 구현체의 프로퍼티를 알 수 없습니다. `Values.just`를 사용한 경우 프로퍼티를 제어할 수 없습니다.

```java
IntegerValueSupplier integerValueSupplier = (IntegerValueSupplier)fixture.giveMeBuilder(ObjectValueSupplier.class)
    .set("$", Values.just(new IntegerValueSupplier(-1203)))
    .set("value", 1) // not works
    .sample();
```

