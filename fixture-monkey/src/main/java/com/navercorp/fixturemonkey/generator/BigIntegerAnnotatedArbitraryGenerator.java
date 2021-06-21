package com.navercorp.fixturemonkey.generator;

import java.math.BigDecimal;
import java.math.BigInteger;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.arbitraries.BigIntegerArbitrary;

public class BigIntegerAnnotatedArbitraryGenerator implements AnnotatedArbitraryGenerator<BigInteger> {
	public static final BigIntegerAnnotatedArbitraryGenerator INSTANCE = new BigIntegerAnnotatedArbitraryGenerator();

	@Override
	public Arbitrary<BigInteger> generate(AnnotationSource annotationSource) {
		AnnotatedGeneratorConstraint constraint =
			AnnotatedGeneratorConstraints.findConstraintByClass(BigInteger.class, annotationSource);
		return generate(constraint);
	}

	private BigIntegerArbitrary generate(AnnotatedGeneratorConstraint constraint) {
		BigIntegerArbitrary arbitrary = Arbitraries.bigIntegers();
		BigDecimal min = constraint.getMin();
		BigDecimal max = constraint.getMax();

		if (min != null && max != null) {
			return arbitrary.between(min.toBigInteger(), max.toBigInteger());
		} else if (max != null) {
			return arbitrary.lessOrEqual(max.toBigInteger());
		} else if (min != null) {
			return arbitrary.greaterOrEqual(min.toBigInteger());
		} else {
			return arbitrary;
		}
	}
}
