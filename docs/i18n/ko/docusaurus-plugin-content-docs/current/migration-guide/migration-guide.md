---
title: "1.0.x"
sidebar_position: 111
---


# 1.0.x에서 1.1.x로 마이그레이션하기

이 가이드는 Fixture Monkey 1.0.x에서 1.1.x로 코드를 업데이트하는 데 도움을 줍니다. 가능한 한 이전 버전과의 호환성을 유지하면서 라이브러리를 더 쉽게 사용할 수 있도록 여러 개선 사항을 적용했습니다.

## 주요 변경 사항 개요

1. 코틀린 타입 처리 방식 개선
2. 자바와 코틀린을 위한 별도 API 제공
3. 추상 타입과 인터페이스 처리 방법 간소화

## 코틀린 타입 처리 방식 개선

### 변경된 내용
- **이전 (1.0.x)**: `KotlinPlugin`을 사용할 때 자바와 코틀린 타입 모두 코틀린의 기본 생성자 방식을 사용했습니다. 이로 인해 자바 타입을 생성할 때 오류가 발생했습니다.
- **현재 (1.1.x)**: 각 언어에 적합한 객체 생성 전략을 사용합니다:
  - 자바 타입 → 빈 속성(getter/setter)을 사용하여 생성
  - 코틀린 타입 → 코틀린 기본 생성자를 사용하여 생성

### 필요한 조치
별도의 코드 변경이 필요하지 않습니다. `KotlinPlugin`을 사용할 때 자바 타입이 이제 올바르게 작동합니다.

## 자바와 코틀린을 위한 별도의 API 제공

### 변경된 내용
- **이전 (1.0.x)**: 자바와 코틀린에 동일한 ArbitraryBuilder API 사용
- **현재 (1.1.x)**: 각 언어에 최적화된 API로 더 자연스러운 개발 경험 제공

### 자바 API
자바에 최적화된 빌더를 얻으려면 다음 방법 중 하나를 사용하세요:
```java
// 자바 스타일 API
ArbitraryBuilder<User> userBuilder = fixtureMonkey.giveMeBuilder(User.class);
// 또는 명시적으로 자바 빌더 요청
ArbitraryBuilder<User> userBuilder = fixtureMonkey.giveMeJavaBuilder(User.class);
```

### 코틀린 API
코틀린 확장 함수를 사용하여 보다 자연스러운 코틀린 경험을 누리세요:
```kotlin
// 확장 함수를 사용한 코틀린 스타일 API
val userBuilder = fixtureMonkey.giveMeKotlinBuilder<User>()
```

> **참고**: 필요한 경우 코틀린 타입에 자바 API를 사용하거나 그 반대도 가능합니다.

## 추상 타입 처리 방법 간소화

### 변경된 내용
- **이전 (1.0.x)**: 추상 클래스나 인터페이스를 구현하기 위해 복잡한 `ObjectPropertyGenerator` 설정이 필요했습니다.
- **현재 (1.1.x)**: 더 간단한 `CandidateConcretePropertyResolver`를 사용하여 어떤 구현체를 사용할지에만 집중할 수 있습니다.

### 예시: sealed 클래스 처리하기

#### 이전 (1.0.x) - 복잡한 설정
`ObjectProperty`에 대한 많은 세부 사항을 이해해야 했습니다:

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

#### 현재 (1.1.x) - 더 간단한 방식
어떤 구현 클래스를 사용할지에만 집중하면 됩니다:

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

## 개선 사항 요약

1. **향상된 언어 지원**: 각 언어(자바/코틀린)가 자연스러운 생성 방식을 사용합니다.
2. **더 직관적인 API**: 각 언어에 맞는 더 자연스러운 API를 제공합니다.
3. **복잡한 타입 처리 간소화**: 인터페이스, 추상 클래스, sealed 타입 작업 시 불필요한 코드가 줄어듭니다.

이러한 변경 사항으로 Fixture Monkey 1.1.x는 더 쉽게 사용할 수 있으면서도 기존 코드와의 호환성을 대부분 유지합니다.

