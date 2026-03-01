---
title: "Exclude fields to generate"
sidebar_position: 31
---


## 1. Implementing FixtureCustomizer interface

### override customizeProperties using `removeArbitrary` 

```java
public class CustomFixtureCustomizer implements FixtureCustomizer<CustomObject> {
	@Override
	public void customizeProperties(ChildArbitraryContext childArbitraryContext) {
		childArbitraryContext.removeArbitrary(property -> "removePropertyName".equals(property.getName()));
	}

	@Nullable
	@Override
	public T customizeFixture(@Nullable T object){
		return object;
    }
}
```

## 2. Adding `pushAssignableTypeArbitraryCustomizer` option
```java
FixtureMonkey fixtureMonkey=FixtureMonkey.builder()
	.pushAssignableTypeArbitraryCustomizer(
	    CustomObject.class,
		new CustomFixtureCustomizer()
	)
	.build();
```

