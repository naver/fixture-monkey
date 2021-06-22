package com.navercorp.fixturemonkey.generator;

import java.util.OptionalDouble;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

public class OptionalDoubleAnnotatedArbitraryGenerator implements AnnotatedArbitraryGenerator<OptionalDouble> {
	public static final OptionalDoubleAnnotatedArbitraryGenerator INSTANCE =
		new OptionalDoubleAnnotatedArbitraryGenerator();

	@Override
	public Arbitrary<OptionalDouble> generate(AnnotationSource annotationSource) {
		return DoubleAnnotatedArbitraryGenerator.INSTANCE.generate(annotationSource)
			.flatMap(it -> Arbitraries.of(OptionalDouble.of(it), OptionalDouble.empty()));
	}
}
