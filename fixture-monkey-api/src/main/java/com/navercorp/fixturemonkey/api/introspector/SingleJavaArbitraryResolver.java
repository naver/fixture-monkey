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
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.arbitraries.BigDecimalArbitrary;
import net.jqwik.api.arbitraries.BigIntegerArbitrary;
import net.jqwik.api.arbitraries.ByteArbitrary;
import net.jqwik.api.arbitraries.CharacterArbitrary;
import net.jqwik.api.arbitraries.DoubleArbitrary;
import net.jqwik.api.arbitraries.FloatArbitrary;
import net.jqwik.api.arbitraries.IntegerArbitrary;
import net.jqwik.api.arbitraries.LongArbitrary;
import net.jqwik.api.arbitraries.ShortArbitrary;
import net.jqwik.api.arbitraries.StringArbitrary;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.matcher.Matcher;

@API(since = "0.5.6", status = Status.EXPERIMENTAL)
public final class SingleJavaArbitraryResolver implements JavaArbitraryResolver {
	private static final JavaArbitraryResolver DEFAULT_NONE_JAVA_ARBITRARY_RESOLVER = new JavaArbitraryResolver() {
	};
	private final List<JavaArbitraryResolver> javaArbitraryResolvers;

	public SingleJavaArbitraryResolver(List<JavaArbitraryResolver> javaArbitraryResolvers) {
		this.javaArbitraryResolvers = javaArbitraryResolvers;
	}

	@Override
	public Arbitrary<String> strings(StringArbitrary stringArbitrary, ArbitraryGeneratorContext context) {
		return selectResolver(context).strings(stringArbitrary, context);
	}

	@Override
	public Arbitrary<Character> characters(CharacterArbitrary characterArbitrary, ArbitraryGeneratorContext context) {
		return selectResolver(context).characters(characterArbitrary, context);
	}

	@Override
	public Arbitrary<Short> shorts(ShortArbitrary shortArbitrary, ArbitraryGeneratorContext context) {
		return selectResolver(context).shorts(shortArbitrary, context);
	}

	@Override
	public Arbitrary<Byte> bytes(ByteArbitrary byteArbitrary, ArbitraryGeneratorContext context) {
		return selectResolver(context).bytes(byteArbitrary, context);
	}

	@Override
	public Arbitrary<Double> doubles(DoubleArbitrary doubleArbitrary, ArbitraryGeneratorContext context) {
		return selectResolver(context).doubles(doubleArbitrary, context);
	}

	@Override
	public Arbitrary<Float> floats(FloatArbitrary floatArbitrary, ArbitraryGeneratorContext context) {
		return selectResolver(context).floats(floatArbitrary, context);
	}

	@Override
	public Arbitrary<Integer> integers(IntegerArbitrary integerArbitrary, ArbitraryGeneratorContext context) {
		return selectResolver(context).integers(integerArbitrary, context);
	}

	@Override
	public Arbitrary<Long> longs(LongArbitrary longArbitrary, ArbitraryGeneratorContext context) {
		return selectResolver(context).longs(longArbitrary, context);
	}

	@Override
	public Arbitrary<BigInteger> bigIntegers(
		BigIntegerArbitrary bigIntegerArbitrary,
		ArbitraryGeneratorContext context
	) {
		return selectResolver(context).bigIntegers(bigIntegerArbitrary, context);
	}

	@Override
	public Arbitrary<BigDecimal> bigDecimals(
		BigDecimalArbitrary bigDecimalArbitrary,
		ArbitraryGeneratorContext context
	) {
		return selectResolver(context).bigDecimals(bigDecimalArbitrary, context);
	}

	private JavaArbitraryResolver selectResolver(
		ArbitraryGeneratorContext context
	) {
		for (JavaArbitraryResolver resolver : this.javaArbitraryResolvers) {
			if (resolver instanceof Matcher) {
				if (((Matcher)resolver).match(context.getResolvedProperty())) {
					return resolver;
				}
			}
		}
		return javaArbitraryResolvers.isEmpty() ? DEFAULT_NONE_JAVA_ARBITRARY_RESOLVER : javaArbitraryResolvers.get(0);
	}
}
