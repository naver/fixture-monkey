package com.navercorp.fixturemonkey.generator;

import java.util.stream.DoubleStream;

import net.jqwik.api.Arbitrary;

public class DoubleStreamAnnotatedArbitraryGenerator implements AnnotatedArbitraryGenerator<DoubleStream> {
	public static final DoubleStreamAnnotatedArbitraryGenerator INSTANCE =
		new DoubleStreamAnnotatedArbitraryGenerator();

	@Override
	public Arbitrary<DoubleStream> generate(AnnotationSource annotationSource) {
		return DoubleAnnotatedArbitraryGenerator.INSTANCE.generate(annotationSource)
			.array(double[].class)
			.map(DoubleStream::of);
	}
}
