package com.navercorp.fixturemonkey.arbitrary;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

class ComplexArbitraryGenerator<T> implements ArbitraryGenerator<T> {
	@SuppressWarnings("unchecked")
	@Override
	public Arbitrary<T> generate(
		ArbitraryGeneratorContext context, ArbitraryBuilder<T> builder
	) {
		// TODO: Address selecting a target constructor.
		Constructor<T> constructor = (Constructor<T>)builder
			.getTargetClass().getConstructors()[0];

		// TODO: Address parameterized constructors.
		return Arbitraries.randoms().map(it -> newInstance(constructor));
	}

	private T newInstance(Constructor<T> constructor) {
		try {
			return constructor.newInstance();
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			// TODO: Address these exceptions.
			throw new RuntimeException("TODO");
		}
	}
}
