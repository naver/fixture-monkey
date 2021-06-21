package com.navercorp.fixturemonkey.generator;

import java.math.BigDecimal;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.arbitraries.LongArbitrary;

public class LongAnnotatedArbitraryGenerator implements AnnotatedArbitraryGenerator<Long> {
	public static final LongAnnotatedArbitraryGenerator INSTANCE = new LongAnnotatedArbitraryGenerator();

	@Override
	public Arbitrary<Long> generate(AnnotationSource annotationSource) {
		AnnotatedGeneratorConstraint constraint =
			AnnotatedGeneratorConstraints.findConstraintByClass(Long.class, annotationSource);
		return generate(constraint);
	}

	private LongArbitrary generate(AnnotatedGeneratorConstraint constraint) {
		LongArbitrary arbitrary = Arbitraries.longs();
		BigDecimal min = constraint.getMin();
		BigDecimal max = constraint.getMax();

		if (min != null && max != null) {
			return arbitrary.between(min.longValue(), max.longValue());
		} else if (max != null) {
			return arbitrary.lessOrEqual(max.longValue());
		} else if (min != null) {
			return arbitrary.greaterOrEqual(min.longValue());
		} else {
			return arbitrary;
		}
	}
}
