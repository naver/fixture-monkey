package com.navercorp.fixturemonkey.generator;

import java.math.BigDecimal;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.arbitraries.BigDecimalArbitrary;

public class BigDecimalAnnotatedArbitraryGenerator implements AnnotatedArbitraryGenerator<BigDecimal> {
	public static final BigDecimalAnnotatedArbitraryGenerator INSTANCE = new BigDecimalAnnotatedArbitraryGenerator();

	@Override
	public Arbitrary<BigDecimal> generate(AnnotationSource annotationSource) {
		AnnotatedGeneratorConstraint constraint =
			AnnotatedGeneratorConstraints.findConstraintByClass(Short.class, annotationSource);
		return generate(constraint);
	}

	private BigDecimalArbitrary generate(AnnotatedGeneratorConstraint constraint) {
		BigDecimalArbitrary arbitrary = Arbitraries.bigDecimals();
		BigDecimal min = constraint.getMin();
		BigDecimal max = constraint.getMax();

		if (min != null && max != null) {
			return arbitrary.between(min, max);
		} else if (max != null) {
			return arbitrary.lessOrEqual(max);
		} else if (min != null) {
			return arbitrary.greaterOrEqual(min);
		} else {
			return arbitrary;
		}
	}
}
