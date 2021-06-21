package com.navercorp.fixturemonkey.generator;

import java.math.BigDecimal;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.arbitraries.IntegerArbitrary;

public class IntegerAnnotatedArbitraryGenerator implements AnnotatedArbitraryGenerator<Integer> {
	public static final IntegerAnnotatedArbitraryGenerator INSTANCE = new IntegerAnnotatedArbitraryGenerator();

	@Override
	public Arbitrary<Integer> generate(AnnotationSource annotationSource) {
		AnnotatedGeneratorConstraint constraint =
			AnnotatedGeneratorConstraints.findConstraintByClass(Integer.class, annotationSource);
		return generate(constraint);
	}

	private IntegerArbitrary generate(AnnotatedGeneratorConstraint constraint) {
		IntegerArbitrary arbitrary = Arbitraries.integers();
		BigDecimal min = constraint.getMin();
		BigDecimal max = constraint.getMax();

		if (min != null && max != null) {
			return arbitrary.between(min.intValue(), max.intValue());
		} else if (max != null) {
			return arbitrary.lessOrEqual(max.intValue());
		} else if (min != null) {
			return arbitrary.greaterOrEqual(min.intValue());
		} else {
			return arbitrary;
		}
	}
}
