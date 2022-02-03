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

package com.navercorp.fixturemonkey.javax.validation.introspector;

import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitraries;
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
import net.jqwik.web.api.Web;

import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorContext;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public class JavaxValidationArbitraryIntrospector implements ArbitraryIntrospector {
	private static final String CONTROL_BLOCK = "\u0000-\u001f\u007f";
	private static final RegexGenerator REGEX_GENERATOR = new RegexGenerator();

	@Override
	public Arbitrary<String> strings(
		StringArbitrary stringArbitrary,
		ArbitraryIntrospectorContext context
	) {
		BigDecimal min = null;
		BigDecimal max = null;
		boolean digits = false;
		boolean notBlank = context.findAnnotation(NotBlank.class).isPresent();

		if (notBlank || context.findAnnotation(NotEmpty.class).isPresent()) {
			min = BigDecimal.ONE;
		}

		Optional<Size> size = context.findAnnotation(Size.class);
		if (size.isPresent()) {
			int minValue = size.map(Size::min).get();
			if (min == null) {
				min = BigDecimal.valueOf(minValue);
			} else if (minValue > 1) {
				min = BigDecimal.valueOf(minValue);
			}

			max = BigDecimal.valueOf(size.map(Size::max).get());
		}

		// TODO: support fraction
		Optional<Digits> digitsAnnotation = context.findAnnotation(Digits.class);
		if (digitsAnnotation.isPresent()) {
			digits = true;
			notBlank = true;

			BigDecimal maxValue = digitsAnnotation.map(Digits::integer).map(BigDecimal::valueOf).get();
			if (max == null) {
				max = maxValue;
			} else if (max.compareTo(maxValue) > 0) {
				max = maxValue;
			}
		}

		Optional<Pattern> pattern = context.findAnnotation(Pattern.class);
		if (pattern.isPresent()) {
			Integer minValue = min != null ? min.intValue() : null;
			Integer maxValue = max != null ? max.intValue() : null;
			List<String> values = REGEX_GENERATOR.generateAll(pattern.get(), minValue, maxValue);
			if (notBlank) {
				values = values.stream()
					.filter(it -> it != null && it.trim().isEmpty())
					.collect(toList());
			}

			return Arbitraries.of(values);
		}

		Arbitrary<String> arbitrary;
		if (context.findAnnotation(Email.class).isPresent()) {
			arbitrary = Web.emails().allowIpv4Host();
			if (min != null) {
				int emailMinLength = min.intValue();
				arbitrary = arbitrary.filter(it -> it != null && it.length() >= emailMinLength);
			}
			if (max != null) {
				int emailMaxLength = max.intValue();
				arbitrary = arbitrary.filter(it -> it != null && it.length() <= emailMaxLength);
			}
		} else {
			if (min != null) {
				stringArbitrary = stringArbitrary.ofMinLength(min.intValue());
			}
			if (max != null) {
				stringArbitrary = stringArbitrary.ofMaxLength(max.intValue());
			}
			if (digits) {
				stringArbitrary = stringArbitrary.numeric();
			} else {
				stringArbitrary = stringArbitrary.ascii();
			}
			arbitrary = stringArbitrary;
		}

		boolean shouldNotBlank = notBlank;
		return arbitrary
			.filter(it -> {
				if (!shouldNotBlank) {
					return true;
				}

				if (it == null || it.trim().length() == 0) {
					return false;
				}

				return !CONTROL_BLOCK.equals(it.trim());
			});
	}

	@Override
	public Arbitrary<Character> characters(
		CharacterArbitrary characterArbitrary,
		ArbitraryIntrospectorContext context
	) {
		return characterArbitrary;
	}

	@Override
	public Arbitrary<Short> shorts(
		ShortArbitrary shortArbitrary,
		ArbitraryIntrospectorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<Byte> bytes(
		ByteArbitrary byteArbitrary,
		ArbitraryIntrospectorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<Double> doubles(
		DoubleArbitrary doubleArbitrary,
		ArbitraryIntrospectorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<Float> floats(
		FloatArbitrary floatArbitrary,
		ArbitraryIntrospectorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<Integer> integers(
		IntegerArbitrary integerArbitrary,
		ArbitraryIntrospectorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<Long> longs(
		LongArbitrary longArbitrary,
		ArbitraryIntrospectorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<BigInteger> bigIntegers(
		BigIntegerArbitrary bigIntegerArbitrary,
		ArbitraryIntrospectorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<BigDecimal> bigDecimals(
		BigDecimalArbitrary bigDecimalArbitrary,
		ArbitraryIntrospectorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}
}
