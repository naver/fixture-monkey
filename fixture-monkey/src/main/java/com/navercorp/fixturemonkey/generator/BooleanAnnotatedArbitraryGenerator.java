package com.navercorp.fixturemonkey.generator;

import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

public class BooleanAnnotatedArbitraryGenerator implements AnnotatedArbitraryGenerator<Boolean> {
	public static final BooleanAnnotatedArbitraryGenerator INSTANCE = new BooleanAnnotatedArbitraryGenerator();

	@Override
	public Arbitrary<Boolean> generate(AnnotationSource annotationSource) {
		if (annotationSource.findAnnotation(AssertTrue.class).isPresent()) {
			return Arbitraries.of(Boolean.TRUE);
		}

		if (annotationSource.findAnnotation(AssertFalse.class).isPresent()) {
			return Arbitraries.of(Boolean.FALSE);
		}

		return Arbitraries.of(Boolean.TRUE, Boolean.FALSE);
	}
}
