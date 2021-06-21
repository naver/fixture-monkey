package com.navercorp.fixturemonkey.generator;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

public class CharacterAnnotatedArbitraryGenerator implements AnnotatedArbitraryGenerator<Character> {
	public static final CharacterAnnotatedArbitraryGenerator INSTANCE = new CharacterAnnotatedArbitraryGenerator();

	@Override
	public Arbitrary<Character> generate(AnnotationSource annotationSource) {
		return Arbitraries.chars();
	}
}
