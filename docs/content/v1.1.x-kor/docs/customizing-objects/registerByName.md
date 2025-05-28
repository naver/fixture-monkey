---
title: "이름을 통한 연산 등록"
weight: 45
menu:
docs:
parent: "customizing-objects"
identifier: "registerByName"
---

`registerByName`와 `selectName` 기능을 사용하면 특정 이름으로 연산을 정의하고 재사용할 수 있습니다. 이 기능은 테스트 전반에서 설정을 체계적으로 관리하고 재활용할 수 있는 강력한 방법을 제공합니다.

### 이름으로 연산 등록하기

`registerByName` 메서드를 사용해 특정 이름으로 연산을 등록할 수 있습니다:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
	.registerByName(
		"test",
		String.class,
		monkey -> monkey.giveMeBuilder("foo")
	)
	.build();
```

### selectName으로 연산 적용하기

등록된 이름 기반 연산은 `selectName`을 통해 특정 속성에 적용할 수 있습니다:

```java
SimpleObject actual = sut.giveMeBuilder(SimpleObject.class)
	.selectName("test")
	.sample();

// SimpleObject의 String 속성이 "foo"로 설정됩니다.
```

### 여러 이름 기반 연산 결합하기

여러 개의 이름 기반 연산도 함께 사용할 수 있습니다:

```java
Person person = fixtureMonkey.giveMeBuilder(Person.class)
    .selectName("foo", "bar")
    .sample();
```

### 우선순위 처리

여러 연산이 등록되거나 적용될 경우, 우선순위에 따라 어떤 연산이 적용될지 결정됩니다.
{{< alert icon="🚨" text="같은 우선순위로 여러 연산이 등록된 경우, 어떤 연산이 적용될지는 무작위로 결정됩니다." />}}

**등록 우선순위**: 우선순위 값이 낮을수록 먼저 적용됩니다.

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
	.registerByName(
		"test",
		String.class,
		monkey -> monkey.giveMeBuilder("test"),
		1
	)
	.registerByName(
		"test2",
		String.class,
		monkey -> monkey.giveMeBuilder("test2"),
		2
	)
	.build();
// 우선순위가 더 높은(숫자가 낮은) "test"가 적용되어 String 속성은 "test"로 설정됩니다.
```

**기본 우선순위**: 우선순위를 명시하지 않으면 `Integer.MAX_VALUE`가 기본값으로 설정되며, 이는 가장 낮은 우선순위입니다.

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
	.registerByName(
		"foo",
		String.class,
		monkey -> monkey.giveMeBuilder("first")
	)
	.registerByName(
		"bar",
		String.class,
		monkey -> monkey.giveMeBuilder("second")
	)
	.build();
// "foo"와 "bar" 모두 우선순위가 기본값인 Integer.MAX_VALUE로 등록되어, 무작위로 선택됩니다.
```

**어노테이션**: `@Order` 어노테이션을 통해 Group에 우선순위를 지정할 수 있습니다. value 값을 설정하지 않으면 기본값은 `Integer.MAX_VALUE`입니다.

```java
@Order(value = 1)
public static class RegisterGroupWithPriority {
	public static final ConcreteIntValue FIXED_INT_VALUE = new ConcreteIntValue();

	public ArbitraryBuilder<String> string(FixtureMonkey fixtureMonkey) {
		return fixtureMonkey.giveMeBuilder(String.class)
			.set(Arbitraries.strings().numeric().ofMinLength(4).ofMaxLength(6));
	}

	public ArbitraryBuilder<List<String>> stringList(FixtureMonkey fixtureMonkey) {
		return fixtureMonkey.giveMeBuilder(new TypeReference<List<String>>() {
			})
			.setInner(
				new InnerSpec()
					.minSize(5)
			);
	}

	public ArbitraryBuilder<ConcreteIntValue> concreteIntValue(FixtureMonkey fixtureMonkey) {
		return fixtureMonkey.giveMeBuilder(FIXED_INT_VALUE);
	}
}
```
