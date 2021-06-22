package com.navercorp.fixturemonkey.generator;

import static com.navercorp.fixturemonkey.generator.AnnotatedGeneratorConstraints.generateDateMillisArbitrary;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import net.jqwik.api.Arbitrary;

public class ZonedDateTimeAnnotatedArbitraryGenerator implements AnnotatedArbitraryGenerator<ZonedDateTime> {
	public static final ZonedDateTimeAnnotatedArbitraryGenerator INSTANCE =
		new ZonedDateTimeAnnotatedArbitraryGenerator();

	@Override
	public Arbitrary<ZonedDateTime> generate(AnnotationSource annotationSource) {
		AnnotatedGeneratorConstraint constraint =
			AnnotatedGeneratorConstraints.findConstraintByClass(Instant.class, annotationSource);

		return generateDateMillisArbitrary(constraint)
			.map(it -> Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()));
	}
}
