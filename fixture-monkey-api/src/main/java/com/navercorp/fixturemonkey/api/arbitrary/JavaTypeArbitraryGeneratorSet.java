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

package com.navercorp.fixturemonkey.api.arbitrary;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;

@API(since = "0.6.3", status = Status.MAINTAINED)
public interface JavaTypeArbitraryGeneratorSet {
	CombinableArbitrary<String> strings(ArbitraryGeneratorContext context);

	CombinableArbitrary<Character> characters(ArbitraryGeneratorContext context);

	CombinableArbitrary<Short> shorts(ArbitraryGeneratorContext context);

	CombinableArbitrary<Byte> bytes(ArbitraryGeneratorContext context);

	CombinableArbitrary<Double> doubles(ArbitraryGeneratorContext context);

	CombinableArbitrary<Float> floats(ArbitraryGeneratorContext context);

	CombinableArbitrary<Integer> integers(ArbitraryGeneratorContext context);

	CombinableArbitrary<Long> longs(ArbitraryGeneratorContext context);

	CombinableArbitrary<BigInteger> bigIntegers(ArbitraryGeneratorContext context);

	CombinableArbitrary<BigDecimal> bigDecimals(ArbitraryGeneratorContext context);
}
