---
title: "ArbitraryValidator"
sidebar_position: 46
---

```java
public interface ArbitraryValidator {
	void validate(Object arbitrary);
}
```

Invalid instance could not be generated.
If object is invalid which sampled over `10000` times from `Arbitrary`, `TooManyFilterMissesException` would be thrown. ([MaxTriesLoop](https://github.com/jlink/jqwik/blob/master/engine/src/main/java/net/jqwik/engine/properties/MaxTriesLoop.java))

## DefaultArbitraryValidator
`javax.validation.Validator` would validate instance, should keep `javax.validation.constraints`

## CompositeFixtureValidator (default)
- Combined `ArbitraryValidator`which consists of `DefaultArbitraryValidator` and user-defined `ArbitraryValidator`

