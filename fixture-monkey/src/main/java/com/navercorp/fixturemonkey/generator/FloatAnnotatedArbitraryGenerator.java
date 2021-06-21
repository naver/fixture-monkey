package com.navercorp.fixturemonkey.generator;

import java.math.BigDecimal;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.arbitraries.FloatArbitrary;

public class FloatAnnotatedArbitraryGenerator implements AnnotatedArbitraryGenerator<Float> {
	public static final FloatAnnotatedArbitraryGenerator INSTANCE = new FloatAnnotatedArbitraryGenerator();

	@Override
	public Arbitrary<Float> generate(AnnotationSource annotationSource) {
		AnnotatedGeneratorConstraint constraint =
			AnnotatedGeneratorConstraints.findConstraintByClass(Float.class, annotationSource);
		return generate(constraint);
	}

	private FloatArbitrary generate(AnnotatedGeneratorConstraint constraint) {
		FloatArbitrary arbitrary = Arbitraries.floats();
		BigDecimal min = constraint.getMin();
		BigDecimal max = constraint.getMax();

		if (min != null && max != null) {
			return arbitrary.between(min.floatValue(), max.floatValue());
		} else if (max != null) {
			return arbitrary.lessOrEqual(max.floatValue());
		} else if (min != null) {
			return arbitrary.greaterOrEqual(min.floatValue());
		} else {
			return arbitrary;
		}
	}
}
