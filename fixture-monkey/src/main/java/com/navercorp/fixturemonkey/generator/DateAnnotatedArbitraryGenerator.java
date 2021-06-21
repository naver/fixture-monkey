package com.navercorp.fixturemonkey.generator;

import static com.navercorp.fixturemonkey.generator.AnnotatedGeneratorConstraints.generateDateMillisArbitrary;

import java.time.Instant;
import java.util.Date;

import net.jqwik.api.Arbitrary;

public class DateAnnotatedArbitraryGenerator implements AnnotatedArbitraryGenerator<Date> {
	public static final DateAnnotatedArbitraryGenerator INSTANCE = new DateAnnotatedArbitraryGenerator();

	@Override
	public Arbitrary<Date> generate(AnnotationSource annotationSource) {
		AnnotatedGeneratorConstraint constraint =
			AnnotatedGeneratorConstraints.findConstraintByClass(Instant.class, annotationSource);

		return generateDateMillisArbitrary(constraint)
			.map(it -> Date.from(Instant.ofEpochMilli(it)));
	}
}
