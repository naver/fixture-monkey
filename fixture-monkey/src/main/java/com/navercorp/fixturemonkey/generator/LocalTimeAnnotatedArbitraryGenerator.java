package com.navercorp.fixturemonkey.generator;

import static com.navercorp.fixturemonkey.generator.AnnotatedGeneratorConstraints.generateDateMillisArbitrary;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

import net.jqwik.api.Arbitrary;

public class LocalTimeAnnotatedArbitraryGenerator implements AnnotatedArbitraryGenerator<LocalTime> {
	public static final LocalTimeAnnotatedArbitraryGenerator INSTANCE = new LocalTimeAnnotatedArbitraryGenerator();

	@Override
	public Arbitrary<LocalTime> generate(AnnotationSource annotationSource) {
		AnnotatedGeneratorConstraint constraint =
			AnnotatedGeneratorConstraints.findConstraintByClass(LocalTime.class, annotationSource);

		return generateDateMillisArbitrary(constraint)
			.map(it -> Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalTime());
	}
}
