/*
 * Fixture Monkey
 *
 * Copyright (c) 2021-present NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.fixturemonkey.api.unique;

import java.util.Optional;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.EdgeCases;
import net.jqwik.api.ExhaustiveGenerator;
import net.jqwik.api.RandomGenerator;
import net.jqwik.api.support.LambdaSupport;
import net.jqwik.engine.ArbitraryDelegator;
import net.jqwik.engine.properties.arbitraries.EdgeCasesSupport;
import net.jqwik.engine.properties.arbitraries.JustArbitrary;

@SuppressWarnings("NullableProblems")
@API(since = "0.4.3", status = Status.EXPERIMENTAL)
public final class FilteredMonkeyArbitrary<T> extends ArbitraryDelegator<T> {
	private final Predicate<T> filterPredicate;
	private final int maxMisses;

	public FilteredMonkeyArbitrary(Arbitrary<T> self, Predicate<T> filterPredicate, int maxMisses) {
		super(self);
		this.filterPredicate = filterPredicate;
		this.maxMisses = maxMisses;
	}

	@Override
	public RandomGenerator<T> generator(int genSize) {
		if (isFixed()) {
			return super.generator(genSize);
		}
		return super.generator(genSize).filter(filterPredicate, maxMisses);
	}

	@Override
	public RandomGenerator<T> generatorWithEmbeddedEdgeCases(int genSize) {
		if (isFixed()) {
			return super.generatorWithEmbeddedEdgeCases(genSize);
		}
		return super.generatorWithEmbeddedEdgeCases(genSize).filter(filterPredicate, maxMisses);
	}

	@Override
	public Optional<ExhaustiveGenerator<T>> exhaustive(long maxNumberOfSamples) {
		if (isFixed()) {
			return super.exhaustive(maxNumberOfSamples);
		}

		return super.exhaustive(maxNumberOfSamples)
			.map(generator -> generator.filter(filterPredicate, maxMisses));
	}

	@Override
	public EdgeCases<T> edgeCases(int maxEdgeCases) {
		if (isFixed()) {
			return super.edgeCases(maxEdgeCases);
		}

		return EdgeCasesSupport.filter(super.edgeCases(maxEdgeCases), filterPredicate);
	}

	private boolean isFixed() {
		return arbitrary() instanceof JustArbitrary;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}

		FilteredMonkeyArbitrary<?> that = (FilteredMonkeyArbitrary<?>)obj;
		if (maxMisses != that.maxMisses) {
			return false;
		}
		return LambdaSupport.areEqual(filterPredicate, that.filterPredicate);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + maxMisses;
		return result;
	}
}
