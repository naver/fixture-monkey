---
title: "ArbitraryValidator"
weight: 5
---
```java
public interface ArbitraryValidator {
	void validate(Object arbitrary);
}
```

생성한 arbitrary가 유효한지 검사를 하는 인터페이스입니다. 
유효성 검사를 통과하지 못한 객체는 생성하지 않습니다.
`10000` 번을 시도했음에도 유효성 검사를 통과하지 못하면 `TooManyFilterMissesException`가 발생합니다. ([MaxTriesLoop](https://github.com/jlink/jqwik/blob/master/engine/src/main/java/net/jqwik/engine/properties/MaxTriesLoop.java))

## DefaultArbitraryValidator
`javax.validation.Validator`가 객체의 유효성 검사를 진행합니다. `javax.validation.constraints`를 준수해야 합니다.

## CompositeFixtureValidator (default)
`DefaultArbitraryValidator`와 새로 정의한 `ArbitraryValidator`를 조합해서 만들어집니다.


## 사용자 정의 ArbitraryValidator

`ArbitraryValidator` 구현체에서 유효하지 않은 경우를 정의하고 `ConstraintViolationException`를 throw 하도록 구현하면 됩니다.
