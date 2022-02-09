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

package com.navercorp.fixturemonkey.api.introspector;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class JavaArbitraryTypeIntrospector implements ArbitraryTypeIntrospector, Matcher {
	private final Map<Class<?>, Function<ArbitraryGeneratorContext, ArbitraryIntrospectorResult>> introspector;

	public JavaArbitraryTypeIntrospector() {
		this(
			new IntrospectorArbitraryGenerator() {
			},
			new ArbitraryIntrospector() {
			}
		);
	}

	public JavaArbitraryTypeIntrospector(
		IntrospectorArbitraryGenerator introspectorArbitraryGenerator,
		ArbitraryIntrospector arbitraryIntrospector
	) {
		this.introspector = introspectors(introspectorArbitraryGenerator, arbitraryIntrospector);
	}

	@Override
	public boolean match(ArbitraryGeneratorContext context) {
		Class<?> type = Types.getActualType(context.getType());
		return this.introspector.containsKey(type);
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		Class<?> type = Types.getActualType(context.getType());
		return this.introspector.getOrDefault(
				type,
				ctx -> ArbitraryIntrospectorResult.EMPTY
			)
			.apply(context);
	}

	private static Map<Class<?>, Function<ArbitraryGeneratorContext, ArbitraryIntrospectorResult>> introspectors(
		IntrospectorArbitraryGenerator introspectorArbitraryGenerator,
		ArbitraryIntrospector arbitraryIntrospector
	) {
		Map<Class<?>, Function<ArbitraryGeneratorContext, ArbitraryIntrospectorResult>> introspector = new HashMap<>();

		introspector.put(
			String.class,
			ctx -> new ArbitraryIntrospectorResult(
				arbitraryIntrospector.strings(
					introspectorArbitraryGenerator.strings(),
					ctx
				)
			)
		);

		introspector.put(
			char.class,
			ctx -> new ArbitraryIntrospectorResult(
				arbitraryIntrospector.characters(
					introspectorArbitraryGenerator.characters(),
					ctx
				)
			)
		);

		introspector.put(
			Character.class,
			ctx -> new ArbitraryIntrospectorResult(
				arbitraryIntrospector.characters(
					introspectorArbitraryGenerator.characters(),
					ctx
				)
			)
		);

		introspector.put(
			short.class,
			ctx -> new ArbitraryIntrospectorResult(
				arbitraryIntrospector.shorts(
					introspectorArbitraryGenerator.shorts(),
					ctx
				)
			)
		);

		introspector.put(
			Short.class,
			ctx -> new ArbitraryIntrospectorResult(
				arbitraryIntrospector.shorts(
					introspectorArbitraryGenerator.shorts(),
					ctx
				)
			)
		);

		introspector.put(
			byte.class,
			ctx -> new ArbitraryIntrospectorResult(
				arbitraryIntrospector.bytes(
					introspectorArbitraryGenerator.bytes(),
					ctx
				)
			)
		);

		introspector.put(
			Byte.class,
			ctx -> new ArbitraryIntrospectorResult(
				arbitraryIntrospector.bytes(
					introspectorArbitraryGenerator.bytes(),
					ctx
				)
			)
		);

		introspector.put(
			double.class,
			ctx -> new ArbitraryIntrospectorResult(
				arbitraryIntrospector.doubles(
					introspectorArbitraryGenerator.doubles(),
					ctx
				)
			)
		);

		introspector.put(
			Double.class,
			ctx -> new ArbitraryIntrospectorResult(
				arbitraryIntrospector.doubles(
					introspectorArbitraryGenerator.doubles(),
					ctx
				)
			)
		);

		introspector.put(
			float.class,
			ctx -> new ArbitraryIntrospectorResult(
				arbitraryIntrospector.floats(
					introspectorArbitraryGenerator.floats(),
					ctx
				)
			)
		);

		introspector.put(
			Float.class,
			ctx -> new ArbitraryIntrospectorResult(
				arbitraryIntrospector.floats(
					introspectorArbitraryGenerator.floats(),
					ctx
				)
			)
		);

		introspector.put(
			int.class,
			ctx -> new ArbitraryIntrospectorResult(
				arbitraryIntrospector.integers(
					introspectorArbitraryGenerator.integers(),
					ctx
				)
			)
		);

		introspector.put(
			Integer.class,
			ctx -> new ArbitraryIntrospectorResult(
				arbitraryIntrospector.integers(
					introspectorArbitraryGenerator.integers(),
					ctx
				)
			)
		);

		introspector.put(
			long.class,
			ctx -> new ArbitraryIntrospectorResult(
				arbitraryIntrospector.longs(
					introspectorArbitraryGenerator.longs(),
					ctx
				)
			)
		);

		introspector.put(
			Long.class,
			ctx -> new ArbitraryIntrospectorResult(
				arbitraryIntrospector.longs(
					introspectorArbitraryGenerator.longs(),
					ctx
				)
			)
		);

		introspector.put(
			BigInteger.class,
			ctx -> new ArbitraryIntrospectorResult(
				arbitraryIntrospector.bigIntegers(
					introspectorArbitraryGenerator.bigIntegers(),
					ctx
				)
			)
		);

		introspector.put(
			BigDecimal.class,
			ctx -> new ArbitraryIntrospectorResult(
				arbitraryIntrospector.bigDecimals(
					introspectorArbitraryGenerator.bigDecimals(),
					ctx
				)
			)
		);

		return Collections.unmodifiableMap(introspector);
	}
}
