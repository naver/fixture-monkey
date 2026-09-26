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

package com.navercorp.fixturemonkey.api.jqwik;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.EdgeCases;
import net.jqwik.api.RandomGenerator;
import net.jqwik.engine.execution.lifecycle.CurrentTestDescriptor;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.engine.EngineUtils;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.api.random.Randoms;

@SuppressWarnings("NullableProblems")
@API(since = "0.6.9", status = Status.MAINTAINED)
public abstract class ArbitraryUtils {
	private static final int GENERATION_SIZE = 1000;
	private static final int MAX_CACHED_GENERATORS = 500;
	private static final Map<Arbitrary<?>, RandomGenerator<?>> GENERATORS =
		new LinkedHashMap<Arbitrary<?>, RandomGenerator<?>>(MAX_CACHED_GENERATORS, 0.75f, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<Arbitrary<?>, RandomGenerator<?>> eldest) {
				return size() > MAX_CACHED_GENERATORS;
			}
		};

	@SuppressWarnings("return")
	public static <T> CombinableArbitrary<T> toCombinableArbitrary(Arbitrary<T> arbitrary) {
		return CombinableArbitrary.from(LazyArbitrary.lazy(
			() -> {
				if (arbitrary != null) {
					return sample(arbitrary);
				}
				return null;
			}
		));
	}

	/**
	 * Samples {@code arbitrary} from the random {@link Randoms} decides.
	 *
	 * @param arbitrary the arbitrary to sample
	 * @param <T>       the value type
	 * @return the sampled value
	 */
	public static <T> T sample(Arbitrary<T> arbitrary) {
		Random random = Randoms.current();
		return inSamplingContext(() -> generatorOf(arbitrary).next(random).value());
	}

	@SuppressWarnings("unchecked")
	private static <T> RandomGenerator<T> generatorOf(Arbitrary<T> arbitrary) {
		synchronized (GENERATORS) {
			RandomGenerator<?> generator = GENERATORS.get(arbitrary);
			if (generator == null) {
				// computeIfAbsent is avoided because creating a generator may cache other generators
				generator = newThreadSafeArbitrary(arbitrary).generator(GENERATION_SIZE);
				GENERATORS.put(arbitrary, generator);
			}
			return (RandomGenerator<T>)generator;
		}
	}

	private static <T> T inSamplingContext(Supplier<T> code) {
		return EngineUtils.useJqwikEngine() ? JqwikSamplingContext.run(code) : code.get();
	}

	public static <T> Arbitrary<T> newThreadSafeArbitrary(Arbitrary<T> delegate) {
		return new Arbitrary<T>() {
			@Override
			public RandomGenerator<T> generator(int genSize) {
				return delegate.generator(genSize);
			}

			// Removing a StoreRepository dependency, it is not useful without Jqwik engine.
			@Override
			public RandomGenerator<T> generator(int genSize, boolean withEdgeCases) {
				return delegate.generator(genSize);
			}

			@Override
			public EdgeCases<T> edgeCases(int maxEdgeCases) {
				return delegate.edgeCases(maxEdgeCases);
			}
		};
	}

	/**
	 * Runs sampling the way jqwik samples outside its own lifecycle, since some arbitraries need a test descriptor.
	 * Kept in its own class so jqwik's engine classes load only when the engine is present.
	 */
	private static final class JqwikSamplingContext {
		private static final TestDescriptor DESCRIPTOR = new AbstractTestDescriptor(
			UniqueId.root("jqwik", "fixture-monkey-samples"),
			"Sampling from fixture monkey seed scopes"
		) {
			@Override
			public Type getType() {
				return Type.TEST;
			}
		};

		private JqwikSamplingContext() {
		}

		static <T> T run(Supplier<T> code) {
			if (CurrentTestDescriptor.isEmpty()) {
				return CurrentTestDescriptor.runWithDescriptor(DESCRIPTOR, code);
			}
			return code.get();
		}
	}
}
