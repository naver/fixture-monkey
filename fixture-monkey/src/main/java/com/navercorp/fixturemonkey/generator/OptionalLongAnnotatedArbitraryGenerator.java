package com.navercorp.fixturemonkey.generator;

import java.util.OptionalLong;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

public class OptionalLongAnnotatedArbitraryGenerator implements AnnotatedArbitraryGenerator<OptionalLong> {
	public static final OptionalLongAnnotatedArbitraryGenerator INSTANCE =
		new OptionalLongAnnotatedArbitraryGenerator();

	@Override
	public Arbitrary<OptionalLong> generate(AnnotationSource annotationSource) {
		return LongAnnotatedArbitraryGenerator.INSTANCE.generate(annotationSource)
			.flatMap(it -> Arbitraries.of(OptionalLong.of(it), OptionalLong.empty()));
	}
}
