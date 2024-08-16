---
title: "1.0.x"
weight: 11
menu:
docs:
  parent: "migration-guide"
  identifier: "1.0.x"
---

## 코틀린 타입 생성 방법
1.0.x에서 `KotlinPlugin`을 추가하면 자바 타입과 코틀린 타입 모두 `PrimaryConstructorArbitraryIntrospector` 를 사용해서 생성합니다. 코틀린의 primary 생성자를 사용해서 객체를 생성하기 때문에 자바 타입이 들어오면 문제가 발생합니다. 

1.1.x부터 `KotlinPlugin`을 추가하면 자바 타입은 `BeanArbitraryIntrospector`으로 생성하고 코틀린 타입은 `PrimaryConstructorArbitraryIntrospector`으로 생성합니다.

## 자바와 코틀린의 ArbitraryBuilder API 다른 점

1.0.x에서는 자바와 코틀린 모두 동일한 ArbitraryBuilder 인터페이스에서 노출하는 API를 사용합니다.

1.1.x부터 픽스쳐 몽키는 자바 특화 ArbitrayBuilder API와 코틀린 특화 ArbitraryBuilder API를 제공합니다. 물론 자바 특화 ArbitraryBuilder API를 사용해도 코틀린 타입을 생성할 수 있습니다. 반대의 경우도 가능합니다.

### 자바 ArbitraryBuilder API 사용법
자바 특화된 API를 사용하려면 ArbitraryBuilder를 다음과 같이 생성하면 됩니다.
`FixtureMonkey.giveMeBuilder(Class)` 혹은 `FixtureMonkey.giveMeJavaBuilder(Class)`.

### 코틀린 ArbitraryBuilder API 사용법
코틀린 특화된 API를 다음 코틀린 확장함수를 사용하면 됩니다. `FixtureMonkey.giveMeBuilder<Class>()`

## 추상 타입의 구현체 확장하는 방법

1.0.x 버전에서는 추상 타입의 구현체를 확장하려면 `ObjectPropertyGenerator` 옵션을 사용해야 했습니다. 

1.1.x 버전부터는 `CandidateConcretePropertyResolver` 옵션을 사용하면 됩니다. `ObjectPropertyGenerator`보다 간단하고 직관적으로 사용 가능합니다.

예제를 통해 더 자세히 알아보겠습니다.
JDK17부터 도입된 sealed class를 생성하려면 `SealedTypeObjectPropertyGenerator`를 사용했어야 했습니다. "설정한 적이 없는데?"라고 생각하실 수 있지만 픽스쳐 몽키가 대신 설정해주었습니다.
아래와 같이 구현체 설정과 관련이 없는 여러 `ObjectProperty`의 특성들을 알아야하는 문제가 있었습니다.  

```java
public final class SealedTypeObjectPropertyGenerator implements ObjectPropertyGenerator {
	@Override
	public ObjectProperty generate(ObjectPropertyGeneratorContext context) {
		Property sealedTypeProperty = context.getProperty();
		double nullInject = context.getNullInjectGenerator().generate(context);
		Class<?> actualType = Types.getActualType(sealedTypeProperty.getType());
		Set<Class<?>> permittedSubclasses = collectPermittedSubclasses(actualType);

		Map<Property, List<Property>> childPropertiesByProperty =
			permittedSubclasses.stream()
				.collect(
					toUnmodifiableMap(
						Function.identity(),
						it -> context.getPropertyGenerator().generateChildProperties(it)
					)
				);

		return new ObjectProperty(
			sealedTypeProperty,
			context.getPropertyNameResolver(),
			nullInject,
			context.getElementIndex(),
			childPropertiesByProperty
		);
	}

	private static Set<Class<?>> collectPermittedSubclasses(Class<?> type) {
		Set<Class<?>> subclasses = new HashSet<>();
		doCollectPermittedSubclasses(type, subclasses);
		return subclasses;
	}

	private static void doCollectPermittedSubclasses(Class<?> type, Set<Class<?>> subclasses) {
		if (type.isSealed()) {
			for (Class<?> subclass : type.getPermittedSubclasses()) {
				doCollectPermittedSubclasses(subclass, subclasses);
			}
		} else {
			subclasses.add(type);
		}
	}
}
```

1.1.x 부터는 아래와 같이 입력한 sealed class에서 생성하고 싶은 구현체만 반환하면 됩니다.

```java
public final class SealedTypeCandidateConcretePropertyResolver implements CandidateConcretePropertyResolver {
	@Override
	public List<Property> resolve(Property property) {
		Class<?> actualType = Types.getActualType(property.getType());
		Set<Class<?>> permittedSubclasses = collectPermittedSubclasses(actualType);

		return permittedSubclasses.stream()
			.map(PropertyUtils::toProperty)
			.toList();
	}

	private static Set<Class<?>> collectPermittedSubclasses(Class<?> type) {
		Set<Class<?>> subclasses = new HashSet<>();
		doCollectPermittedSubclasses(type, subclasses);
		return subclasses;
	}

	private static void doCollectPermittedSubclasses(Class<?> type, Set<Class<?>> subclasses) {
		if (type.isSealed()) {
			for (Class<?> subclass : type.getPermittedSubclasses()) {
				doCollectPermittedSubclasses(subclass, subclasses);
			}
		} else {
			subclasses.add(type);
		}
	}
}
```
