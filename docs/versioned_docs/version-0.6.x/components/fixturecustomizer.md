---
title: "FixtureCustomizer"
sidebar_position: 45
---

```java
public interface FixtureCustomizer<T> {
	default void customizeProperties(ChildArbitraryContext childArbitraryContext) {
	}

	@Nullable
	T customizeFixture(@Nullable T object);
}
```

## customizeProperties
```java
public void customizeProperties(ChildArbitraryContext childArbitraryContext)
```
customize child properties


### ChildArbitraryContext
#### replaceArbitrary
```java
public void replaceArbitrary(Matcher matcher, Arbitrary<?> arbitrary)
```

replaces by `arbitrary` if given property matches by `matcher`


#### removeArbitrary
```java
public void removeArbitrary(Matcher matcher)
```

removes by `arbitrary` if given property matches by `matcher`

## customizerFixture
```java
@Nullable
public T customizeFixture(@Nullable T object)
```

generated object is customized by `customizeFixture`

