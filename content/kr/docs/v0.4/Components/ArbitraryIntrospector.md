---
title: "ArbitraryIntrospector"
weight: 3
---
```java
public interface ArbitraryIntrospector {
	ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context);
}
```
타입을 생성하는 방법을 정의합니다. 실제 타입 생성은 `ArbitraryGenerator` 에서 `ArbitraryIntrospector`를 의존하여 생성합니다.

## ArbitraryIntrospectorResult
정의한 타입 생성 방식에 따라 생성한 결과를 가지는 객체입니다.

### value
정의한 타입 생성 방식에 따라 생성한 결과를 반환합니다.

## ArbitraryGeneratorContext
### arbitraryProperty
```java
public final class ArbitraryProperty {
	private final ObjectProperty objectProperty;

	@Nullable
	private final ContainerProperty containerProperty;
}
```
#### ObjectProperty
객체에 대한 불변한 정보입니다.

##### property
생성하려고 하는 property의 정보입니다.

##### propertyNameResolver
property의 이름을 결정하는 인터페이스입니다.

##### nullInject
null을 반환하는 확률을 정의합니다.

##### elementIndex
객체가 element 일경우 몇 번째 element인지 나타냅니다.

##### childProperties
객체를 이루는 필드 Property 리스트입니다.

#### ContainerProperty
컨테이너에 대한 불변한 정보입니다.

##### elementProperties
컨테이너에 포함한 element Property 리스트입니다.

##### containerInfo
컨테이너 크기에 대한 정보입니다.

### children
생성하고자 하는 arbitraryProperty의 자식 arbitraryProperty 리스트입니다.

### childrenArbitraryContext

### ownerContext
부모 `ArbitraryGeneratorContext` 입니다.

### rootContext
최상위 `ArbitraryGeneratorContext` 여부입니다.

### arbitraryCustomizers
Arbitrary 생성에 사용하는 `FixtureCustomizer` 리스트를 반환합니다.
