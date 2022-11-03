---
title: "Add new container"
weight: 10
---

## 1. Implementing ArbitraryIntrospector, Matcher interface

```java
public class CustomIntrospector implements ArbitraryIntrospector, Matcher {
	private static final Matcher MATCHER = new AssignableTypeMatcher(CustomContainerType.class);

	@Override
	public boolean match(Property property) {
		return MATCHER.match(property);
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
        ...
	}
}
```

## 2. Implementing ContainerPropertyGenerator interface

```java
public class CustomContainerPropertyGenerator implements ContainerPropertyGeneartor {
	@Override
	public ContainerProperty generate(ContainerPropertyGeneratorContext context) {
        ...
	}
}

```

## 3. Implementing DecomposedContainerValueFactory interface

```java
public class CustomDecomposedContainerValueFactory implements DecomposedContainerValueFactory {
	@Override
	public DecomposedContainerValue from(Object object) {
        ...
	}

}

```

## 4. Adding `addContainerType` option

```java
LabMonkey labMonkey=LabMonkey.labMonkeyBuilder()
	.addContainerType(
        CustomContainerType.class,
		new CustomContainerPropertyGenerator(),
        new CustomIntrospector(),
        new CustomDecomposedContainerValueFactory()
	)
	.build();
```
