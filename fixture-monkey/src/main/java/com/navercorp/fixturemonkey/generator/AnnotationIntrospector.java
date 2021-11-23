package com.navercorp.fixturemonkey.generator;

import net.jqwik.api.Arbitrary;

public interface AnnotationIntrospector<T> {
	Arbitrary<T> getArbitrary(Arbitrary<T> arbitrary, AnnotationSource<T> annotationSource);
}
