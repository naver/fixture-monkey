---
title: "새로운 컨테이너 추가"
weight: 10
---

## 1. ArbitraryIntrospector, Matcher 인터페이스 구현체 정의

```java
public class PairIntrospector implements ArbitraryIntrospector, Matcher {
	private static final Matcher MATCHER = new AssignableTypeMatcher(Pair.class);

	@Override
	public boolean match(Property property) {
		return MATCHER.match(property);
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		ArbitraryProperty property = context.getArbitraryProperty();
		ArbitraryContainerInfo containerInfo = property.getContainerProperty().getContainerInfo();
		if (containerInfo == null) {
			return ArbitraryIntrospectorResult.EMPTY;
		}

		List<Arbitrary<?>> childrenArbitraries = context.getChildrenArbitraryContexts().getArbitraries();
		BuilderCombinator<List<Object>> builderCombinator = Builders.withBuilder(ArrayList::new);
		for (Arbitrary<?> childArbitrary : childrenArbitraries) {
			builderCombinator = builderCombinator.use(childArbitrary).in((list, element) -> {
				list.add(element);
				return list;
			});
		}

		return new ArbitraryIntrospectorResult(
			builderCombinator.build(it -> new Pair<>(it.get(0), it.get(1)))
		);
	}
}
```

## 2. ContainerPropertyGenerator 인터페이스 구현체 정의

```java
public class PairContainerPropertyGenerator implements ContainerPropertyGenerator {
	@Override
	public ContainerProperty generate(ContainerPropertyGeneratorContext context) {
		com.navercorp.fixturemonkey.api.property.Property property = context.getProperty();

		List<AnnotatedType> elementTypes = Types.getGenericsTypes(property.getAnnotatedType());
		if (elementTypes.size() != 2) {
			throw new IllegalArgumentException(
				"Pair elementsTypes must be have 1 generics type for element. "
					+ "propertyType: " + property.getType()
					+ ", elementTypes: " + elementTypes
			);
		}

		AnnotatedType firstElementType = elementTypes.get(0);
		AnnotatedType secondElementType = elementTypes.get(1);
		List<com.navercorp.fixturemonkey.api.property.Property> elementProperties = new ArrayList<>();
		elementProperties.add(
			new ElementProperty(
				property,
				firstElementType,
				0,
				0
			)
		);
		elementProperties.add(
			new ElementProperty(
				property,
				secondElementType,
				1,
				1
			)
		);

		return new ContainerProperty(
			elementProperties,
			new ArbitraryContainerInfo(1, 1, false)
		);
	}
}
```

## 3. DecomposedContainerValueFactory 인터페이스 구현 정의

```java
public class PairDecomposedContainerValueFactory implements DecomposedContainerValueFactory {
	@Override
	public DecomposedContainerValue from(Object object) {
		Pair<?, ?> pair = (Pair<?, ?>)obj;
		List<Object> list = new ArrayList<>();
		list.add(pair.getFirst());
		list.add(pair.getSecond());
		return new DecomposableContainerValue(list, 1);
	}
}
```

## 4. 옵션 추가

```java
LabMonkey labMonkey=LabMonkey.labMonkeyBuilder()
	.addContainerType(
        Pair.class,
        new PairContainerPropertyGenerator(),
        new PairIntrospector(),
        new PairDecomposedContainerValueFactory()
	)
	.build();
```
