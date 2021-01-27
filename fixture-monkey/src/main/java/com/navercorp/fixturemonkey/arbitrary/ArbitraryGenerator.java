package com.navercorp.fixturemonkey.arbitrary;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.specimen.SpecimenBuilder;

public interface ArbitraryGenerator<T> {
	Arbitrary<T> generate(ArbitraryGeneratorContext context, SpecimenBuilder<T> builder);
}
