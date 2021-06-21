package com.navercorp.fixturemonkey.generator;

import static com.navercorp.fixturemonkey.generator.AnnotatedGeneratorConstraints.generateDateMillisArbitrary;

import java.time.Instant;

import net.jqwik.api.Arbitrary;

public class InstantAnnotatedArbitraryGenerator implements AnnotatedArbitraryGenerator<Instant> {
	public static final InstantAnnotatedArbitraryGenerator INSTANCE = new InstantAnnotatedArbitraryGenerator();

	@Override
	public Arbitrary<Instant> generate(AnnotationSource annotationSource) {
		AnnotatedGeneratorConstraint constraint =
			AnnotatedGeneratorConstraints.findConstraintByClass(Instant.class, annotationSource);

		return generateDateMillisArbitrary(constraint)
			.map(Instant::ofEpochMilli);
	}
}
