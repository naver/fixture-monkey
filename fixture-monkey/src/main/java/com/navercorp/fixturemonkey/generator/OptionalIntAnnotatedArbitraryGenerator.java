package com.navercorp.fixturemonkey.generator;

import java.util.OptionalInt;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

public class OptionalIntAnnotatedArbitraryGenerator implements AnnotatedArbitraryGenerator<OptionalInt> {
	public static final OptionalIntAnnotatedArbitraryGenerator INSTANCE = new OptionalIntAnnotatedArbitraryGenerator();

	@Override
	public Arbitrary<OptionalInt> generate(AnnotationSource annotationSource) {
		return IntegerAnnotatedArbitraryGenerator.INSTANCE.generate(annotationSource)
			.flatMap(it -> Arbitraries.of(OptionalInt.of(it), OptionalInt.empty()));
	}
}
