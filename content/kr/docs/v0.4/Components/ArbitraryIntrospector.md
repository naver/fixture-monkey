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
#### [ObjectProperty]({{< relref "/docs/v0.4/components/objectproperty" >}})

#### [ContainerProperty]({{< relref "/docs/v0.4/components/containerproperty" >}})

### children
생성하고자 하는 arbitraryProperty의 자식 arbitraryProperty 리스트입니다.

### childrenArbitraryContext

### ownerContext
부모 `ArbitraryGeneratorContext` 입니다.

### rootContext
최상위 `ArbitraryGeneratorContext` 여부입니다.

### [fixtureCustomizers]({{< relref "/docs/v0.4/components/fixturecustomizer" >}})
