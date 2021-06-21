package com.navercorp.fixturemonkey.generator;

import java.util.stream.IntStream;

import net.jqwik.api.Arbitrary;

public class IntStreamAnnotatedArbitraryGenerator implements AnnotatedArbitraryGenerator<IntStream> {
	public static final IntStreamAnnotatedArbitraryGenerator INSTANCE = new IntStreamAnnotatedArbitraryGenerator();

	@Override
	public Arbitrary<IntStream> generate(AnnotationSource annotationSource) {
		return IntegerAnnotatedArbitraryGenerator.INSTANCE.generate(annotationSource)
			.array(int[].class)
			.map(IntStream::of);
	}
}
