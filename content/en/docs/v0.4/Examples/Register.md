---
title: "registering default ArbitraryBuilder"
weight: 9
---

{{< alert color="primary" title="Abstract">}}
Registered `ArbitraryBuilder` would be used as default `ArbitraryBuilder`
{{< /alert >}} 

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
LabMonkey labMonkey=LabMonkey.labMonkeyBuilder()
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
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
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
		ArbitraryBuilderCandidateList.create()
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
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
    .registerGroup(GenerateGroup.class)
	.build();

// using ArbitraryBuilderGroup interface
LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
	.registerGroup(new GenerateBuilderGroup())
	.build();
```

#### Example
```java
GenerateString generateString = labMonkey.giveMeOne(GenerateString.class);
GenerateInt generateInt = labMonkey.giveMeOne(GenerateInt.class);

then(generateString.getValue()).containsOnlyDigits()();
then(generateInt.getValue()).isBetween(1, 100);
```
