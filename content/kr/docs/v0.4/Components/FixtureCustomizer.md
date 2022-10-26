---
title: "FixtureCustomizer"
weight: 4
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
객체를 생성할 때 필요한 Property 리스트를 제어하는 방법을 변경합니다.


### ChildArbitraryContext
#### replaceArbitrary
```java
public void replaceArbitrary(Matcher matcher, Arbitrary<?> arbitrary)
```

입력한 `matcher`에 해당하는 Property에서 생성한 Arbitrary를 입력한 `arbitrary`로 변경합니다.


#### removeArbitrary
```java
public void removeArbitrary(Matcher matcher)
```

입력한 `matcher`에 해당하는 Property에서 생성한 Arbitrary를 제거합니다.

## customizerFixture
```java
@Nullable
public T customizeFixture(@Nullable T object)
```
생성한 객체를 변경합니다.
