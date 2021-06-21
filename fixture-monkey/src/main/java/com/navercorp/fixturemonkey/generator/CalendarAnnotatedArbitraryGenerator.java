package com.navercorp.fixturemonkey.generator;

import static com.navercorp.fixturemonkey.generator.AnnotatedGeneratorConstraints.generateDateMillisArbitrary;

import java.time.Instant;
import java.util.Calendar;

import net.jqwik.api.Arbitrary;

public class CalendarAnnotatedArbitraryGenerator implements AnnotatedArbitraryGenerator<Calendar> {
	public static final CalendarAnnotatedArbitraryGenerator INSTANCE = new CalendarAnnotatedArbitraryGenerator();

	@Override
	public Arbitrary<Calendar> generate(AnnotationSource annotationSource) {
		AnnotatedGeneratorConstraint constraint =
			AnnotatedGeneratorConstraints.findConstraintByClass(Instant.class, annotationSource);

		return generateDateMillisArbitrary(constraint)
			.map(it -> {
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(it);
				return calendar;
			});
	}
}
