---
title: "인터페이스 생성하기"
images: [ ]
menu:
docs:
parent: "generating-objects"
identifier: "generating-interface-type"
weight: 34
---

Fixture Monkey는 복잡한 인터페이스 객체를 생성할 수 있습니다.
생성하는 인터페이스 종류는 다음 세 가지로 분류할 수 있습니다. `interface`, `generic interface`, `sealed interface`.

Fixture Monkey에서 기본적으로 구현체를 정의해둔 인터페이스가 있습니다.
예를 들면, `List` 인터페이스는 `ArrayList`, `Set` 인터페이스는 `HashSet` 를 생성합니다.

그 외의 인터페이스는 모두 구현체를 명시해주어야 합니다. 명시하지 않으면 Fixture Monkey는 인터페이스의 익명 객체를 생성합니다.
예외적으로 `sealed interface`를 생성할 때는 구현체를 명시할 필요 없습니다.

인터페이스를 어떻게 생성하는지 자세한 예제를 보면서 알아보겠습니다.

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

#### 옵션을 사용하지 않는 경우

아무런 옵션을 사용하지 않으면, Fixture Monkey는 `StringSupplier`의 익명객체를 생성합니다.

```java
FixtureMonkey fixture = FixtureMonkey.create();

StringSupplier result = fixtureMonkey.giveMeOne(StringSupplier.class);
```

생성된 인스턴스 `result`는 `StringSupplier`의 익명객체입니다. getter인 `getValue`는 임의의 String 값을 반환합니다.
일반적인 클래스 생성할 때와 동일하게 일정한 확률로 null이 될 수 있습니다.
겉보기에는 앞서 정의한 `DefaultStringSupplier`와 동작이 동일하지만 `DefaultStringSupplier`의 인스턴스는 아닙니다.

{{< alert icon="💡" title="notice">}}

Fixture Monkey는 익명 객체의 아래와 같은 기준을 만족하는 프로퍼티들만 생성하고 있습니다.

- Getter의 이름 컨벤션을 만족하는 메서드들
- 파라미터가 존재하지 않는 메서드들

{{</ alert>}}

익명 객체에서 생성한 프로퍼티들은 일반 클래스 생성할 때와 동일하게 모두 제어가 가능합니다.

```java
FixtureMonkey fixture = FixtureMonkey.create();

String result = fixture.giveMeBuilder(StringSupplier.class)
	.set("value", "fix")
	.sample()
	.getValue();
```

`result` 는 `fix`로 값이 고정됩니다. `set` 외에도 `ArbitraryBuilder`에서 정의한 모든 API를 사용할 수 있습니다.

#### 옵션을 사용하는 경우

`InterfacePlugin#interfaceImplements` 옵션을 사용해서 인터페이스의 새로운 구현체를 추가할 수 있습니다.

{{< alert icon="💡" title="notice">}}
`InterfacePlugin` 의 옵션들은 모두 인터페이스와 추상 클래스에 필요한 기능입니다.
{{</ alert>}}

```java
FixtureMonkey fixture = FixtureMonkey.builder()
	.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE) // DefaultStringSupplier를 인스턴스화할 때 필요합니다
	.plugin(
		new InterfacePlugin()
			.interfaceImplements(StringSupplier.class, List.of(DefaultStringSupplier.class))
	)
	.build();

DefaultStringSupplier stringSupplier = (DefaultStringSupplier)fixture.giveMeOne(StringSupplier.class);
```

`InterfacePlugin#interfaceImplements` 옵션을 반복해서 사용하면 새로운 구현체를 추가할 수 있습니다. 예를 들면, `List`의 기본 구현체는 `ArrayList`입니다.
만약 `Interface#interfaceImplements`를 사용해 `LinkedList`를 추가하면 어떻게 될까요?

```java
FixtureMonkey fixture = FixtureMonkey.builder()
	.plugin(
		new InterfacePlugin()
			.interfaceImplements(List.class, List.of(LinkedList.class))
	)
	.build();

List<String> list = fixture.giveMeOne(new TypeReference<List<String>>() {
});

// list 는 ArrayList 혹은 LinkedList의 인스턴스입니다.
```

`List` 의 구현체는 `ArrayList` 혹은 `LinkedList`로 생성합니다.

`InterfacePlugin#interfaceImplements` 옵션을 인터페이스 확장에도 사용할 수 있습니다.

아무런 설정이 없다면 `Collection` 인터페이스는 구현체를 생성하지 않습니다. 다음과 같이 `Collection` 인터페이스를 `List` 인터페이스를 생성하도록 확장해보겠습니다.
이렇게 설정하면 `List` 인터페이스의 설정이 `Collection` 인터페이스에 영향을 줍니다. 
구체적으로 이야기하면 `List` 인터페이스는 기본 설정으로 구현체 `ArrayList`를 생성하므로 `Collection` 인터페이스는 `ArrayList`를 생성합니다. 

```java
FixtureMonkey fixture = FixtureMonkey.builder()
	.plugin(
		new InterfacePlugin()
			.interfaceImplements(Collection.class, List.of(List.class))
	)
	.build();

ArrayList<String> collection = (ArrayList<String>)fixture.giveMeOne(new TypeReference<Collection<String>>() {
});

// collection 은 ArrayList의 인스턴스입니다.
```

### Generic Interfaces

만약 복잡한 인터페이스, 예를 들어 제네릭을 가지는 인터페이스를 생성하면 어떻게 해야할까요? 위에서 간단한 인터페이스를 생성한 실습을 완전 똑같이 하면 됩니다.

```java
public interface ObjectValueSupplier<T> {
	T getValue();
}

public class StringValueSupplier implements ObjectValueSupplier<String> {
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

FixtureMonkey fixture = FixtureMonkey.builder()
	.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE) // StringValueSupplier를 인스턴스화할 때 사용합니다.
	.plugin(
		new InterfacePlugin()
			.interfaceImplements(ObjectValueSupplier.class, List.of(StringValueSupplier.class))
	)
	.build();

StringValueSupplier stringSupplier = (StringValueSupplier)fixture.giveMeOne(ObjectValueSupplier.class);

```

### Sealed Interface

Sealed interface는 더 간단합니다. 옵션도 필요없습니다.

```java
sealed interface SealedStringSupplier {
	String getValue();
}

public static final class SealedDefaultStringSupplier implements SealedStringSupplier {
	private final String value;

	@ConstructorProperties("value") // 롬복을 사용하면 추가하지 않아도 됩니다.
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
		ConstructorPropertiesArbitraryIntrospector.INSTANCE) // SealedDefaultStringSupplier를 인스턴스화할 때 사용합니다.
	.build();

SealedDefaultStringSupplier stringSupplier = (SealedDefaultStringSupplier)fixture.giveMeOne(SealedStringSupplier.class);
```

이번 장에서는 인터페이스 타입을 생성하는 방법을 간단한 예제를 보며 배웠습니다. 인터페이스를 생성하는 데 문제가 있다면 `InterfacePlugin` 옵션들을 살펴보세요.
그래도 문제가 해결되지 않는다면 GitHub에 재현 가능한 예제를 포함한 이슈를 올려주세요.
