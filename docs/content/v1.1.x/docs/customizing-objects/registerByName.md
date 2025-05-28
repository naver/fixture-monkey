---
title: "Registering Operations by Name"
weight: 45
menu:
  docs:
    parent: "customizing-objects"
    identifier: "registerByName"
---

The `registerByName` and `selectName` features allow you to define reusable Operations that can be applied to specific properties by name. 
This offers a powerful and organized way to manage and reuse configurations throughout your test suite.

### Registering Operations by Name

You can register a named operations using the `registerByName` method:

```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
	.registerByName(
		"test",
		String.class,
		monkey -> monkey.giveMeBuilder("foo")
	)
	.build();
```

### Applying Named Operations with `selectName`

Once registered, these named Operations can be applied to properties using `selectName`:

```java
SimpleObject actual = sut.giveMeBuilder(SimpleObject.class)
	.selectName("test")
	.sample();

// The String property of SimpleObject will be set to "foo"
```

### Combining Multiple Named Operations

You can also apply multiple named Operations together:

```java
Person person = fixtureMonkey.giveMeBuilder(Person.class)
    .selectName("foo", "bar")
    .sample();
```

### Handling Priorities

When multiple named Operations are registered or applied, priority rules determine which ones take effect.
{{< alert icon="🚨" text="If multiple Operations are registered with the same priority, the applied configuration will be chosen randomly." />}}

**Registration Priority**: Operations with lower priority values take precedence over those with higher values.

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
// The String property will be set to "test" because priority 1 overrides priority 2
```

**Default Priority**: If no priority is specified, the operation is registered with the default value of `Integer.MAX_VALUE`, which is the lowest possible priority.

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
// Both "foo" and "bar" are registered with the default priority of Integer.MAX_VALUE, so one is selected at random.
```

**Annotation**: You can specify the priority of a Group using the `@Order` annotation. If the value is not set, the default is `Integer.MAX_VALUE`.

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
