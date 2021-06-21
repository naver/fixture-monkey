package com.navercorp.fixturemonkey.generator;

import static com.navercorp.fixturemonkey.generator.AnnotatedGeneratorConstraints.generateDateMillisArbitrary;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import net.jqwik.api.Arbitrary;

public class LocalDateAnnotatedArbitraryGenerator implements AnnotatedArbitraryGenerator<LocalDate> {
	public static final LocalDateAnnotatedArbitraryGenerator INSTANCE = new LocalDateAnnotatedArbitraryGenerator();

	@Override
	public Arbitrary<LocalDate> generate(AnnotationSource annotationSource) {
		AnnotatedGeneratorConstraint constraint =
			AnnotatedGeneratorConstraints.findConstraintByClass(LocalDate.class, annotationSource);

		return generateDateMillisArbitrary(constraint)
			.map(it -> Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate());
	}
}
