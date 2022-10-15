---
title: "필드 생성 제외"
weight: 10
---

## 1. FixtureCustomizer 인터페이스 구현체 정의

### customizeProperties의 `removeArbitrary` 재정의

```java
public class CustomFixtureCustomizer implements FixtureCustomizer<CustomObject> {
	@Override
	public void customizeProperties(ChildArbitraryContext childArbitraryContext) {
		childArbitraryContext.removeArbitrary(it -> removeFieldName.equals(it.getName()));
	}

	@Nullable
	@Override
	public T customizeFixture(@Nullable T object){
		return object;
    }
}
```

## 2. 옵션 추가
```java
LabMonkey labMonkey=LabMonkey.labMonkeyBuilder()
	.pushAssignableTypeArbitraryCustomizer(
	    CustomObject.class,
		new CustomFixtureCustomizer()
	)
	.build();
```
