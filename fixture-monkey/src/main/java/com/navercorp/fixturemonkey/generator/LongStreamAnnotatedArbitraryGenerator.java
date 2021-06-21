package com.navercorp.fixturemonkey.generator;

import java.util.stream.LongStream;

import net.jqwik.api.Arbitrary;

public class LongStreamAnnotatedArbitraryGenerator implements AnnotatedArbitraryGenerator<LongStream> {
	public static final LongStreamAnnotatedArbitraryGenerator INSTANCE = new LongStreamAnnotatedArbitraryGenerator();

	@Override
	public Arbitrary<LongStream> generate(AnnotationSource annotationSource) {
		return LongAnnotatedArbitraryGenerator.INSTANCE.generate(annotationSource)
			.array(long[].class)
			.map(LongStream::of);
	}
}
