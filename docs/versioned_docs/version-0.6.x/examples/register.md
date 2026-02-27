---
title: "registering default ArbitraryBuilder"
sidebar_position: 29
---


:::tip[Abstract]
Registered `ArbitraryBuilder` would be used as default `ArbitraryBuilder`
::: 

## 0. Class

```java
public class GenerateString {
	String value;
}

public class GenerateInt {
	int value;
}
```

## 1. Register one type

```java
FixtureMonkey fixtureMonkey=FixtureMonkey.builder()
	.register(
        GenerateString.class,
        fixture -> fixture.giveMeBuilder(GenerateString.class)
            .set("value", Arbitraries.strings().alpha())
    )
	.build();
```

## 2. Register multiple types
### register
```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
	.register(
	    GenerateString.class,
	    fixture -> fixture.giveMeBuilder(GenerateString.class)
            .set("value", Arbitraries.strings().alpha())
    )
	.register(
        GenerateInt.class,
        fixture -> fixture.giveMeBuilder(GenerateInt.class)
            .set("value", Arbitraries.integers().between(1, 100))
	)
	.build();
```


### registerGroup
#### Defining registerGroup
```java
// using reflection
public class GenerateGroup {
	public ArbitraryBuilder<GenerateString> generateString(FixtureMonkey fixtureMonkey){
		return fixtureMonkey.giveMeBuilder(GenerateString.class)
			.set("value", Arbitraries.strings().numeric());
    }
	
	public ArbitraryBuilder<GenerateInt> generateInt(FixtureMonkey fixtureMonkey){
		return fixture.giveMeBuilder(GenerateInt.class)
			.set("value", Arbitraries.integers().between(1, 100));
    }
}

// using ArbitraryBuilderGroup interface
public class GenerateBuilderGroup implements ArbitraryBuilderGroup {
	@Override
	public ArbitraryBuilderCandidateList generateCandidateList() {
		return ArbitraryBuilderCandidateList.create()
			.add(
				ArbitraryBuilderCandidateFactory.of(GenerateString.class)
					.builder(
						arbitraryBuilder -> arbitraryBuilder
							.set("value", Arbitraries.strings().numeric())
					)
			)
			.add(
				ArbitraryBuilderCandidateFactory.of(GenerateInt.class)
					.builder(
						builder -> builder
							.set("value", Arbitraries.integers().between(1, 100))
					)
			);
	}
}
```

#### Add `registerGroup` option
```java
// using reflection
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .registerGroup(GenerateGroup.class)
	.build();

// using ArbitraryBuilderGroup interface
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
	.registerGroup(new GenerateBuilderGroup())
	.build();
```

#### Example
```java
GenerateString generateString = fixtureMonkey.giveMeOne(GenerateString.class);
GenerateInt generateInt = fixtureMonkey.giveMeOne(GenerateInt.class);

then(generateString.getValue()).containsOnlyDigits()();
then(generateInt.getValue()).isBetween(1, 100);
```

