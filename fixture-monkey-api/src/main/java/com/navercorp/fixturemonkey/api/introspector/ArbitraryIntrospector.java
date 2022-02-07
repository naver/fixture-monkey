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

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public interface ArbitraryIntrospector {
	Arbitrary<String> strings(StringArbitrary stringArbitrary, ArbitraryGeneratorContext context);

	Arbitrary<Character> characters(CharacterArbitrary characterArbitrary, ArbitraryGeneratorContext context);

	Arbitrary<Short> shorts(ShortArbitrary shortArbitrary, ArbitraryGeneratorContext context);

	Arbitrary<Byte> bytes(ByteArbitrary byteArbitrary, ArbitraryGeneratorContext context);

	Arbitrary<Double> doubles(DoubleArbitrary doubleArbitrary, ArbitraryGeneratorContext context);

	Arbitrary<Float> floats(FloatArbitrary floatArbitrary, ArbitraryGeneratorContext context);

	Arbitrary<Integer> integers(IntegerArbitrary integerArbitrary, ArbitraryGeneratorContext context);

	Arbitrary<Long> longs(LongArbitrary longArbitrary, ArbitraryGeneratorContext context);

	Arbitrary<BigInteger> bigIntegers(BigIntegerArbitrary bigIntegerArbitrary, ArbitraryGeneratorContext context);

	Arbitrary<BigDecimal> bigDecimals(BigDecimalArbitrary bigDecimalArbitrary, ArbitraryGeneratorContext context);
}
