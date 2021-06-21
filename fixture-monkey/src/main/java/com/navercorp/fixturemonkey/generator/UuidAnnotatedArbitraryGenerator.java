package com.navercorp.fixturemonkey.generator;

import java.util.UUID;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

public class UuidAnnotatedArbitraryGenerator implements AnnotatedArbitraryGenerator<UUID> {
	public static final UuidAnnotatedArbitraryGenerator INSTANCE = new UuidAnnotatedArbitraryGenerator();

	@Override
	public Arbitrary<UUID> generate(AnnotationSource annotationSource) {
		return Arbitraries.create(UUID::randomUUID);
	}
}
