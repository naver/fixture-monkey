package com.navercorp.fixturemonkey.generator;

import java.math.BigDecimal;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.arbitraries.DoubleArbitrary;

public class DoubleAnnotatedArbitraryGenerator implements AnnotatedArbitraryGenerator<Double> {
	public static final DoubleAnnotatedArbitraryGenerator INSTANCE = new DoubleAnnotatedArbitraryGenerator();

	@Override
	public Arbitrary<Double> generate(AnnotationSource annotationSource) {
		AnnotatedGeneratorConstraint constraint =
			AnnotatedGeneratorConstraints.findConstraintByClass(Double.class, annotationSource);
		return generate(constraint);
	}

	private DoubleArbitrary generate(AnnotatedGeneratorConstraint constraint) {
		DoubleArbitrary arbitrary = Arbitraries.doubles();
		BigDecimal min = constraint.getMin();
		BigDecimal max = constraint.getMax();

		if (min != null && max != null) {
			return arbitrary.between(min.doubleValue(), max.doubleValue());
		} else if (max != null) {
			return arbitrary.lessOrEqual(max.doubleValue());
		} else if (min != null) {
			return arbitrary.greaterOrEqual(min.doubleValue());
		} else {
			return arbitrary;
		}
	}
}
