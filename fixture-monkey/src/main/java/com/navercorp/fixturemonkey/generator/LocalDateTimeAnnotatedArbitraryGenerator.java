package com.navercorp.fixturemonkey.generator;

import static com.navercorp.fixturemonkey.generator.AnnotatedGeneratorConstraints.generateDateMillisArbitrary;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import net.jqwik.api.Arbitrary;

public class LocalDateTimeAnnotatedArbitraryGenerator implements AnnotatedArbitraryGenerator<LocalDateTime> {
	public static final LocalDateTimeAnnotatedArbitraryGenerator INSTANCE = new LocalDateTimeAnnotatedArbitraryGenerator();

	@Override
	public Arbitrary<LocalDateTime> generate(AnnotationSource annotationSource) {
		AnnotatedGeneratorConstraint constraint =
			AnnotatedGeneratorConstraints.findConstraintByClass(Instant.class, annotationSource);

		return generateDateMillisArbitrary(constraint)
			.map(it -> Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime());
	}
}
