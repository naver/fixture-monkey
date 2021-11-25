---
title: "AnnotatedArbitraryGenerator"
linkTitle: "AnnotatedArbitraryGenerator"
weight: 4
---
{{< alert color="secondary" title="Note">}}
Predefined AnnotatedArbitraryGenerator can be found [here]({{< relref "/docs/v0.3.x/features/defaultsupportedtypes" >}})
{{< /alert >}}

## Put AnnotatedArbitraryGenerator
```java
@Test
void putArbitraryGenerator() {
    FixtureMonkey fixture = FixtureMonkey.builder()
        .addAnnotatedArbitraryGenerator(Tuple.class, new TupleAnnotatedArbitraryGenerator())
        .build();

	Tuple customObject = fixture.giveMeOne(Tuple.class);
}
```

## Custom AnnotatedArbitraryGenerator
### New AnnotatedArbitraryGenerator
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

### Override Predefined AnnotatedArbitraryGenerator
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
