---
title: "ArbitraryIntrospector"
sidebar_position: 43
---

```java
public interface ArbitraryIntrospector {
	ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context);
}
```
`ArbitraryIntrospector` determines how to create an instance of a certain class. 

`ArbitraryGenerator` creates an instance of a certain class.
`ArbitraryGenerator` handles requests by delegating to `ArbitraryIntrospector`.

## ArbitraryIntrospectorResult

### value
an Arbitrary instance which could sample with an every different instance 

## ArbitraryGeneratorContext
### arbitraryProperty
```java
public final class ArbitraryProperty {
	private final ObjectProperty objectProperty;

	@Nullable
	private final ContainerProperty containerProperty;
}
```
#### [ObjectProperty](./objectproperty)

#### [ContainerProperty](./containerproperty)

### children
arbitraryProperty children 

### childrenArbitraryContext

### ownerContext
parent `ArbitraryGeneratorContext`

### rootContext
whether it is root `ArbitraryGeneratorContext` 

### [fixtureCustomizers](./fixturecustomizer)

