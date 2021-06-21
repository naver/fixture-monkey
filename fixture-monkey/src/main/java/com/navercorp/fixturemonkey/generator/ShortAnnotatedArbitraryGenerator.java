package com.navercorp.fixturemonkey.generator;

import java.math.BigDecimal;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.arbitraries.ShortArbitrary;

public class ShortAnnotatedArbitraryGenerator implements AnnotatedArbitraryGenerator<Short> {
	public static final ShortAnnotatedArbitraryGenerator INSTANCE = new ShortAnnotatedArbitraryGenerator();

	@Override
	public Arbitrary<Short> generate(AnnotationSource annotationSource) {
		AnnotatedGeneratorConstraint constraint =
			AnnotatedGeneratorConstraints.findConstraintByClass(Short.class, annotationSource);
		return generate(constraint);
	}

	private ShortArbitrary generate(AnnotatedGeneratorConstraint constraint) {
		ShortArbitrary arbitrary = Arbitraries.shorts();
		BigDecimal min = constraint.getMin();
		BigDecimal max = constraint.getMax();

		if (min != null && max != null) {
			return arbitrary.between(min.shortValue(), max.shortValue());
		} else if (max != null) {
			return arbitrary.lessOrEqual(max.shortValue());
		} else if (min != null) {
			return arbitrary.greaterOrEqual(min.shortValue());
		} else {
			return arbitrary;
		}
	}
}
