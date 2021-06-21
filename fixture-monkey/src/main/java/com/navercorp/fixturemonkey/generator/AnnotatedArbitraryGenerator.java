package com.navercorp.fixturemonkey.generator;

import net.jqwik.api.Arbitrary;

@FunctionalInterface
public interface AnnotatedArbitraryGenerator<T> {
	Arbitrary<T> generate(AnnotationSource annotationSource);
}
