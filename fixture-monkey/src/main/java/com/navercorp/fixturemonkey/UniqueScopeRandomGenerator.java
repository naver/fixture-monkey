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

package com.navercorp.fixturemonkey;

import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import net.jqwik.api.RandomGenerator;
import net.jqwik.api.Shrinkable;
import net.jqwik.api.TooManyFilterMissesException;
import net.jqwik.api.Tuple;
import net.jqwik.engine.properties.MaxTriesLoop;
import net.jqwik.engine.properties.shrinking.UniqueShrinkable;

final class UniqueScopeRandomGenerator<T> implements RandomGenerator<T> {
	private final RandomGenerator<T> toFilter;

	public UniqueScopeRandomGenerator(RandomGenerator<T> toFilter) {
		this.toFilter = toFilter;
	}

	@Override
	public Shrinkable<T> next(Random random) {
		return nextUntilAccepted(random, r -> {
			Shrinkable<T> next = toFilter.next(r);
			T value = next.value();
			if (value == null) {
				return next;
			}

			return new UniqueShrinkable<>(next, (shrinkable) -> this.shrink(shrinkable, value.getClass()));
		});
	}

	@Override
	public String toString() {
		return String.format("Unique [%s]", toFilter);
	}

	private Shrinkable<T> nextUntilAccepted(Random random, Function<Random, Shrinkable<T>> fetchShrinkable) {
		return MaxTriesLoop.loop(
			() -> true,
			next -> {
				next = fetchShrinkable.apply(random);
				T value = next.value();
				if (value == null) {
					return Tuple.of(false, next);
				}

				@SuppressWarnings("unchecked")
				Set<T> usedValues = (Set<T>) ArbitraryGeneratorThreadLocal.getUniqueValues(value.getClass());
				if (usedValues.contains(next.value())) {
					return Tuple.of(false, next);
				}

				ArbitraryGeneratorThreadLocal.addUniqueValue(value);
				return Tuple.of(true, next);
			},
			maxMisses -> {
				String message = String.format("%s missed more than %s times.", this, maxMisses);
				return new TooManyFilterMissesException(message);
			}
		);
	}

	private Stream<Shrinkable<T>> shrink(UniqueShrinkable<T> current, Class<?> type) {
		return current.toFilter.shrink().filter(s -> {
			@SuppressWarnings("unchecked")
			Set<T> usedValues = (Set<T>) ArbitraryGeneratorThreadLocal.getUniqueValues(type);
			return !usedValues.contains(s.value());
		}).map(s -> {
			// TODO: In theory the set of used values should only contain those in the current try
			// but currently it contains all values tried in this shrinking
			ArbitraryGeneratorThreadLocal.addUniqueValue(s.value());
			return new UniqueShrinkable<>(s, (shrinkable) -> this.shrink(shrinkable, type));
		});
	}
}
