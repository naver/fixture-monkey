package com.navercorp.fixturemonkey;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.EdgeCases;
import net.jqwik.api.RandomGenerator;
import net.jqwik.api.Shrinkable;

final class ArbitraryRandomGeneratorFacade extends RandomGenerator.RandomGeneratorFacade {
	private final RandomGenerator.RandomGeneratorFacade delegate;

	public ArbitraryRandomGeneratorFacade(RandomGenerator.RandomGeneratorFacade delegate) {
		this.delegate = delegate;
	}

	@Override
	public <T, U> Shrinkable<U> flatMap(Shrinkable<T> self, Function<T, RandomGenerator<U>> mapper, long nextLong) {
		return this.delegate.flatMap(self, mapper, nextLong);
	}

	@Override
	public <T, U> Shrinkable<U> flatMap(Shrinkable<T> wrappedShrinkable, Function<T, Arbitrary<U>> mapper, int genSize,
		long nextLong) {
		return this.delegate.flatMap(wrappedShrinkable, mapper, genSize, nextLong);
	}

	@Override
	public <T> RandomGenerator<T> filter(RandomGenerator<T> self, Predicate<T> filterPredicate) {
		return this.delegate.filter(self, filterPredicate);
	}

	@Override
	public <T> RandomGenerator<T> withEdgeCases(RandomGenerator<T> self, int genSize, EdgeCases<T> edgeCases) {
		return this.delegate.withEdgeCases(self, genSize, edgeCases);
	}

	@Override
	public <T> RandomGenerator<T> unique(RandomGenerator<T> self) {
		if (ArbitraryGeneratorThreadLocal.isUniqueScope()) {
			return new UniqueScopeRandomGenerator<>(self);
		}
		return this.delegate.unique(self);
	}

	@Override
	public <T> RandomGenerator<List<T>> collect(RandomGenerator<T> self, Predicate<List<T>> until) {
		return this.delegate.collect(self, until);
	}

	@Override
	public <T> RandomGenerator<T> injectDuplicates(RandomGenerator<T> self, double duplicateProbability) {
		return this.delegate.injectDuplicates(self, duplicateProbability);
	}

	@Override
	public <T> RandomGenerator<T> ignoreException(RandomGenerator<T> self, Class<? extends Throwable> exceptionType) {
		return this.delegate.ignoreException(self, exceptionType);
	}
}
