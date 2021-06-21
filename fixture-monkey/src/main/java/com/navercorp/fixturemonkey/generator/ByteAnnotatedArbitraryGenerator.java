package com.navercorp.fixturemonkey.generator;

import java.math.BigDecimal;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.arbitraries.ByteArbitrary;

public class ByteAnnotatedArbitraryGenerator implements AnnotatedArbitraryGenerator<Byte> {
	public static final ByteAnnotatedArbitraryGenerator INSTANCE = new ByteAnnotatedArbitraryGenerator();

	@Override
	public Arbitrary<Byte> generate(AnnotationSource annotationSource) {
		AnnotatedGeneratorConstraint constraint =
			AnnotatedGeneratorConstraints.findConstraintByClass(Byte.class, annotationSource);
		return generate(constraint);
	}

	private ByteArbitrary generate(AnnotatedGeneratorConstraint constraint) {
		ByteArbitrary arbitrary = Arbitraries.bytes();
		BigDecimal min = constraint.getMin();
		BigDecimal max = constraint.getMax();

		if (min != null && max != null) {
			return arbitrary.between(min.byteValue(), max.byteValue());
		} else if (max != null) {
			return arbitrary.lessOrEqual(max.byteValue());
		} else if (min != null) {
			return arbitrary.greaterOrEqual(min.byteValue());
		} else {
			return arbitrary;
		}
	}
}
