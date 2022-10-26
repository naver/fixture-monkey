---
title: "AnnotatedArbitraryGenerator"
linkTitle: "AnnotatedArbitraryGenerator"
weight: 4
---
{{< alert color="secondary" title="Note">}}
기본적으로 제공하는 AnnotatedArbitraryGenerator는 [여기]({{< relref "/docs/v0.3.x/features/defaultsupportedtypes" >}})에서 확인할 수 있습니다.
{{< /alert >}}

## AnnotatedArbitraryGenerator 추가하기
```java
@Test
void putArbitraryGenerator() {
    FixtureMonkey fixture = FixtureMonkey.builder()
        .addAnnotatedArbitraryGenerator(Tuple.class, new TupleAnnotatedArbitraryGenerator())
        .build();

	Tuple customObject = this.fixture.giveMeOne(Tuple.class);
}
```

## 나만의 AnnotatedArbitraryGenerator
### 새로운 AnnotatedArbitraryGenerator 추가하기
```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .addAnnotatedArbitraryGenerator(Tuple.class, new TupleAnnotatedArbitraryGenerator() {
        @Override
        public Arbitrary<Tuple> generate(AnnotationSource annotationSource) {
        	Arbitrary<Tuple> result = ...
            return result;
        }
    })
    .build();
```

### 기본 제공하는 AnnotatedArbitraryGenerator 재정의 하기
```java
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .addAnnotatedArbitraryGenerator(BigDecimal.class, new BigDecimalAnnotatedArbitraryGenerator() {
        @Override
        public Arbitrary<BigDecimal> generate(AnnotationSource annotationSource) {
        	return super.generate(annotationSource)
                    .filter(it -> it.compareTo(minValue) >= 0 && it.compareTo(maxValue) <= 0);
        }
    })
    .build();
```
