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

package com.navercorp.fixturemonkey.arbitrary;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;

class ComplexArbitraryGenerator<T> implements ArbitraryGenerator<T> {
	@SuppressWarnings("unchecked")
	@Override
	public Arbitrary<T> generate(
		ArbitraryGeneratorContext context, ArbitraryBuilder<T> builder
	) {
		// TODO: Address selecting a target constructor.
		ParameterArbitraryGenerator parameterArbitraryGenerator =
			new ParameterArbitraryGenerator(context);

		Constructor<T> constructor = (Constructor<T>)builder
			.getTargetClass().getConstructors()[0];

		List<Arbitrary<?>> parameterArbitraires = Arrays
			.stream(constructor.getParameterTypes())
			.map(parameterArbitraryGenerator::generate)
			.collect(toList());

		Combinators.BuilderCombinator<List<Object>> combinator =
			Combinators.withBuilder(ArrayList::new);

		for (Arbitrary<?> parameterArbitrary : parameterArbitraires) {
			combinator = combinator.use(parameterArbitrary).in((list, value) -> {
				list.add(value);
				return list;
			});
		}

		return combinator.build(args -> newInstance(constructor, args.toArray()));
	}

	private T newInstance(Constructor<T> constructor, Object[] args) {
		try {
			return constructor.newInstance(args);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			// TODO: Address these exceptions.
			throw new RuntimeException("TODO");
		}
	}

	private static class ParameterArbitraryGenerator {
		private final ArbitraryGeneratorContext context;

		public ParameterArbitraryGenerator(ArbitraryGeneratorContext context) {
			this.context = context;
		}

		public <T> Arbitrary<T> generate(Class<T> clazz) {
			return this.context.get(clazz).generate(context, new ArbitraryBuilder<>(clazz));
		}
	}
}
