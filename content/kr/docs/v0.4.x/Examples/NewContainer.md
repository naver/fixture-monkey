---
title: "새로운 컨테이너 추가"
weight: 9
---

## 1. ArbitraryIntrospector, Matcher 인터페이스 구현체 정의

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

## 2. ContainerPropertyGenerator 인터페이스 구현체 정의

```java
public class CustomContainerPropertyGenerator implements ContainerPropertyGeneartor {
	@Override
	public ContainerProperty generate(ContainerPropertyGeneratorContext context) {
        ...
	}
}

```

## 3. DecomposedContainerValueFactory 인터페이스 구현 정의

```java
public class CustomDecomposedContainerValueFactory implements DecomposedContainerValueFactory {
	@Override
	public DecomposedContainerValue from(Object object) {
        ...
	}

}

```

## 4. 옵션 추가

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
