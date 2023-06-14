---
title: "기본 객체 빌더 정의"
weight: 9
---

{{< alert color="primary" title="Abstract">}}
정의한 기본 연산은 타입을 생성할 때 항상 적용합니다.
{{< /alert >}} 

## 0. 클래스

```java
public class GenerateString {
	String value;
}

public class GenerateInt {
	int value;
}
```

## 1. 단일 타입 추가

```java
FixtureMonkey fixtureMonkey=FixtureMonkey.builder()
	.register(
        GenerateString.class,
        fixture -> fixture.giveMeBuilder(GenerateString.class)
            .set("value", Arbitraries.strings().alpha())
    )
	.build();
```

## 2. 복수 타입 추가
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
#### registerGroup 정의
```java
// reflection 이용 시
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

// ArbitraryBuilderGroup 인터페이스 이용 시
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

#### 옵션 추가
```java
// reflection 이용 시
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .registerGroup(GenerateGroup.class)
	.build();

// ArbitraryBuilderGroup 인터페이스 이용 시
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
	.registerGroup(new GenerateBuilderGroup())
	.build();
```

#### 예시
```java
GenerateString generateString = fixtureMonkey.giveMeOne(GenerateString.class);
GenerateInt generateInt = fixtureMonkey.giveMeOne(GenerateInt.class);

then(generateString.getValue()).containsOnlyDigits()();
then(generateInt.getValue()).isBetween(1, 100);
```
