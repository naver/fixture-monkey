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
import java.util.Set;

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
import com.navercorp.fixturemonkey.api.property.Property;

@API(since = "0.6.0", status = Status.EXPERIMENTAL)
public final class SingleJavaArbitraryResolver implements JavaArbitraryResolver {
	private final Set<JavaArbitraryResolver> javaArbitraryResolvers;
	private final JavaArbitraryResolver customJavaArbitraryResolver;

	public SingleJavaArbitraryResolver(
		Set<JavaArbitraryResolver> javaArbitraryResolvers,
		JavaArbitraryResolver customJavaArbitraryResolver
	) {
		this.javaArbitraryResolvers = javaArbitraryResolvers;
		this.customJavaArbitraryResolver = customJavaArbitraryResolver;
	}

	@Override
	public Arbitrary<String> strings(StringArbitrary stringArbitrary, ArbitraryGeneratorContext context) {
		return resolve(context.getResolvedProperty()).strings(stringArbitrary, context);
	}

	@Override
	public Arbitrary<Character> characters(CharacterArbitrary characterArbitrary, ArbitraryGeneratorContext context) {
		return resolve(context.getResolvedProperty()).characters(characterArbitrary, context);
	}

	@Override
	public Arbitrary<Short> shorts(ShortArbitrary shortArbitrary, ArbitraryGeneratorContext context) {
		return resolve(context.getResolvedProperty()).shorts(shortArbitrary, context);
	}

	@Override
	public Arbitrary<Byte> bytes(ByteArbitrary byteArbitrary, ArbitraryGeneratorContext context) {
		return resolve(context.getResolvedProperty()).bytes(byteArbitrary, context);
	}

	@Override
	public Arbitrary<Double> doubles(DoubleArbitrary doubleArbitrary, ArbitraryGeneratorContext context) {
		return resolve(context.getResolvedProperty()).doubles(doubleArbitrary, context);
	}

	@Override
	public Arbitrary<Float> floats(FloatArbitrary floatArbitrary, ArbitraryGeneratorContext context) {
		return resolve(context.getResolvedProperty()).floats(floatArbitrary, context);
	}

	@Override
	public Arbitrary<Integer> integers(IntegerArbitrary integerArbitrary, ArbitraryGeneratorContext context) {
		return resolve(context.getResolvedProperty()).integers(integerArbitrary, context);
	}

	@Override
	public Arbitrary<Long> longs(LongArbitrary longArbitrary, ArbitraryGeneratorContext context) {
		return resolve(context.getResolvedProperty()).longs(longArbitrary, context);
	}

	@Override
	public Arbitrary<BigInteger> bigIntegers(
		BigIntegerArbitrary bigIntegerArbitrary,
		ArbitraryGeneratorContext context
	) {
		return resolve(context.getResolvedProperty()).bigIntegers(bigIntegerArbitrary, context);
	}

	@Override
	public Arbitrary<BigDecimal> bigDecimals(
		BigDecimalArbitrary bigDecimalArbitrary,
		ArbitraryGeneratorContext context
	) {
		return resolve(context.getResolvedProperty()).bigDecimals(bigDecimalArbitrary, context);
	}

	private JavaArbitraryResolver resolve(Property property) {
		for (JavaArbitraryResolver resolver : this.javaArbitraryResolvers) {
			if (resolver instanceof Matcher) {
				if (((Matcher)resolver).match(property)) {
					return resolver;
				}
			}
		}

		return customJavaArbitraryResolver;
	}
}
