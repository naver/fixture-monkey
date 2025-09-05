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

import com.navercorp.fixturemonkey.api.arbitrary.JavaTypeArbitraryGeneratorSet;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class JavaArbitraryIntrospector implements ArbitraryIntrospector, Matcher {
	private final Map<Class<?>, Function<ArbitraryGeneratorContext, ArbitraryIntrospectorResult>> introspector;

	public JavaArbitraryIntrospector(JavaTypeArbitraryGeneratorSet javaTypeArbitraryGeneratorSet) {
		this.introspector = introspectors(javaTypeArbitraryGeneratorSet);
	}

	@Override
	public boolean match(Property property) {
		Class<?> actualType = Types.getActualType(property.getType());
		return this.introspector.containsKey(actualType);
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		Class<?> type = Types.getActualType(context.getResolvedType());
		return this.introspector.getOrDefault(
				type,
				ctx -> ArbitraryIntrospectorResult.NOT_INTROSPECTED
			)
			.apply(context);
	}

	private Map<Class<?>, Function<ArbitraryGeneratorContext, ArbitraryIntrospectorResult>> introspectors(
		JavaTypeArbitraryGeneratorSet javaTypeArbitraryGeneratorSet
	) {
		Map<Class<?>, Function<ArbitraryGeneratorContext, ArbitraryIntrospectorResult>> introspector = new HashMap<>();

		introspector.put(
			String.class,
			ctx -> new ArbitraryIntrospectorResult(javaTypeArbitraryGeneratorSet.strings(ctx))
		);

		introspector.put(
			char.class,
			ctx -> new ArbitraryIntrospectorResult(javaTypeArbitraryGeneratorSet.characters(ctx))
		);

		introspector.put(
			Character.class,
			ctx -> new ArbitraryIntrospectorResult(javaTypeArbitraryGeneratorSet.characters(ctx))
		);

		introspector.put(
			short.class,
			ctx -> new ArbitraryIntrospectorResult(javaTypeArbitraryGeneratorSet.shorts(ctx))
		);

		introspector.put(
			Short.class,
			ctx -> new ArbitraryIntrospectorResult(javaTypeArbitraryGeneratorSet.shorts(ctx))
		);

		introspector.put(
			byte.class,
			ctx -> new ArbitraryIntrospectorResult(javaTypeArbitraryGeneratorSet.bytes(ctx))
		);

		introspector.put(
			Byte.class,
			ctx -> new ArbitraryIntrospectorResult(javaTypeArbitraryGeneratorSet.bytes(ctx))
		);

		introspector.put(
			double.class,
			ctx -> new ArbitraryIntrospectorResult(javaTypeArbitraryGeneratorSet.doubles(ctx))
		);

		introspector.put(
			Double.class,
			ctx -> new ArbitraryIntrospectorResult(javaTypeArbitraryGeneratorSet.doubles(ctx))
		);

		introspector.put(
			float.class,
			ctx -> new ArbitraryIntrospectorResult(javaTypeArbitraryGeneratorSet.floats(ctx))
		);

		introspector.put(
			Float.class,
			ctx -> new ArbitraryIntrospectorResult(javaTypeArbitraryGeneratorSet.floats(ctx))
		);

		introspector.put(
			int.class,
			ctx -> new ArbitraryIntrospectorResult(javaTypeArbitraryGeneratorSet.integers(ctx))
		);

		introspector.put(
			Integer.class,
			ctx -> new ArbitraryIntrospectorResult(javaTypeArbitraryGeneratorSet.integers(ctx))
		);

		introspector.put(
			long.class,
			ctx -> new ArbitraryIntrospectorResult(javaTypeArbitraryGeneratorSet.longs(ctx))
		);

		introspector.put(
			Long.class,
			ctx -> new ArbitraryIntrospectorResult(javaTypeArbitraryGeneratorSet.longs(ctx))
		);

		introspector.put(
			BigInteger.class,
			ctx -> new ArbitraryIntrospectorResult(javaTypeArbitraryGeneratorSet.bigIntegers(ctx))
		);

		introspector.put(
			BigDecimal.class,
			ctx -> new ArbitraryIntrospectorResult(javaTypeArbitraryGeneratorSet.bigDecimals(ctx))
		);

		return Collections.unmodifiableMap(introspector);
	}
}
