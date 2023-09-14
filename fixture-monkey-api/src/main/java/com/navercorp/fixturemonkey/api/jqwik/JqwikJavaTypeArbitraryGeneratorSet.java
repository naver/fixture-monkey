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

import java.math.BigDecimal;
import java.math.BigInteger;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.arbitrary.JavaTypeArbitraryGeneratorSet;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.introspector.JavaArbitraryResolver;
import com.navercorp.fixturemonkey.api.introspector.JavaTypeArbitraryGenerator;

/**
 * It would be moved into jqwik module in 0.7.0.
 */
@API(since = "0.6.3", status = Status.EXPERIMENTAL)
public final class JqwikJavaTypeArbitraryGeneratorSet implements JavaTypeArbitraryGeneratorSet {
	private final JavaTypeArbitraryGenerator arbitraryGenerator;
	private final JavaArbitraryResolver arbitraryResolver;

	public JqwikJavaTypeArbitraryGeneratorSet(
		JavaTypeArbitraryGenerator arbitraryGenerator,
		JavaArbitraryResolver arbitraryResolver
	) {
		this.arbitraryGenerator = arbitraryGenerator;
		this.arbitraryResolver = arbitraryResolver;
	}

	@Override
	public CombinableArbitrary<String> strings(ArbitraryGeneratorContext context) {
		return ArbitraryUtils.toCombinableArbitrary(arbitraryResolver.strings(arbitraryGenerator.strings(), context));
	}

	@Override
	public CombinableArbitrary<Character> characters(ArbitraryGeneratorContext context) {
		return ArbitraryUtils.toCombinableArbitrary(
			arbitraryResolver.characters(arbitraryGenerator.characters(), context)
		);
	}

	@Override
	public CombinableArbitrary<Short> shorts(ArbitraryGeneratorContext context) {
		return ArbitraryUtils.toCombinableArbitrary(arbitraryResolver.shorts(arbitraryGenerator.shorts(), context));
	}

	@Override
	public CombinableArbitrary<Byte> bytes(ArbitraryGeneratorContext context) {
		return ArbitraryUtils.toCombinableArbitrary(arbitraryResolver.bytes(arbitraryGenerator.bytes(), context));
	}

	@Override
	public CombinableArbitrary<Double> doubles(ArbitraryGeneratorContext context) {
		return ArbitraryUtils.toCombinableArbitrary(arbitraryResolver.doubles(arbitraryGenerator.doubles(), context));
	}

	@Override
	public CombinableArbitrary<Float> floats(ArbitraryGeneratorContext context) {
		return ArbitraryUtils.toCombinableArbitrary(arbitraryResolver.floats(arbitraryGenerator.floats(), context));
	}

	@Override
	public CombinableArbitrary<Integer> integers(ArbitraryGeneratorContext context) {
		return ArbitraryUtils.toCombinableArbitrary(arbitraryResolver.integers(arbitraryGenerator.integers(), context));
	}

	@Override
	public CombinableArbitrary<Long> longs(ArbitraryGeneratorContext context) {
		return ArbitraryUtils.toCombinableArbitrary(arbitraryResolver.longs(arbitraryGenerator.longs(), context));
	}

	@Override
	public CombinableArbitrary<BigInteger> bigIntegers(ArbitraryGeneratorContext context) {
		return ArbitraryUtils.toCombinableArbitrary(
			arbitraryResolver.bigIntegers(arbitraryGenerator.bigIntegers(), context)
		);
	}

	@Override
	public CombinableArbitrary<BigDecimal> bigDecimals(ArbitraryGeneratorContext context) {
		return ArbitraryUtils.toCombinableArbitrary(
			arbitraryResolver.bigDecimals(arbitraryGenerator.bigDecimals(), context)
		);
	}
}
