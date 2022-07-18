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

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;

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

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.introspector.JavaArbitraryResolver;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class JavaxValidationJavaArbitraryResolver implements JavaArbitraryResolver {
	private static final String CONTROL_BLOCK = "\u0000-\u001f\u007f";
	private static final RegexGenerator REGEX_GENERATOR = new RegexGenerator();

	private final JavaxValidationConstraintGenerator constraintGenerator;

	public JavaxValidationJavaArbitraryResolver() {
		this(new JavaxValidationConstraintGenerator());
	}

	public JavaxValidationJavaArbitraryResolver(JavaxValidationConstraintGenerator constraintGenerator) {
		this.constraintGenerator = constraintGenerator;
	}

	@Override
	public Arbitrary<String> strings(
		StringArbitrary stringArbitrary,
		ArbitraryGeneratorContext context
	) {
		JavaxValidationStringConstraint constraint = this.constraintGenerator.generateStringConstraint(context);
		BigInteger min = constraint.getMinSize();
		BigInteger max = constraint.getMaxSize();
		boolean digits = constraint.isDigits();
		boolean notBlank = constraint.isNotBlank();

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

		return arbitrary
			.filter(it -> {
				if (!notBlank) {
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
		ArbitraryGeneratorContext context
	) {
		return characterArbitrary;
	}

	@Override
	public Arbitrary<Short> shorts(
		ShortArbitrary shortArbitrary,
		ArbitraryGeneratorContext context
	) {
		JavaxValidationIntegerConstraint constraint = this.constraintGenerator.generateIntegerConstraint(context);
		BigInteger min = constraint.getMin();
		BigInteger max = constraint.getMax();

		if (min != null) {
			shortArbitrary = shortArbitrary.greaterOrEqual(min.shortValueExact());
		}
		if (max != null) {
			shortArbitrary = shortArbitrary.lessOrEqual(max.shortValueExact());
		}

		return shortArbitrary;
	}

	@Override
	public Arbitrary<Byte> bytes(
		ByteArbitrary byteArbitrary,
		ArbitraryGeneratorContext context
	) {
		JavaxValidationIntegerConstraint constraint = this.constraintGenerator.generateIntegerConstraint(context);
		BigInteger min = constraint.getMin();
		BigInteger max = constraint.getMax();

		if (min != null) {
			byteArbitrary = byteArbitrary.greaterOrEqual(min.byteValueExact());
		}
		if (max != null) {
			byteArbitrary = byteArbitrary.lessOrEqual(max.byteValueExact());
		}

		return byteArbitrary;
	}

	@Override
	public Arbitrary<Double> doubles(
		DoubleArbitrary doubleArbitrary,
		ArbitraryGeneratorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<Float> floats(
		FloatArbitrary floatArbitrary,
		ArbitraryGeneratorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<Integer> integers(
		IntegerArbitrary integerArbitrary,
		ArbitraryGeneratorContext context
	) {
		JavaxValidationIntegerConstraint constraint = this.constraintGenerator.generateIntegerConstraint(context);
		BigInteger min = constraint.getMin();
		BigInteger max = constraint.getMax();

		if (min != null) {
			integerArbitrary = integerArbitrary.greaterOrEqual(min.intValueExact());
		}
		if (max != null) {
			integerArbitrary = integerArbitrary.lessOrEqual(max.intValueExact());
		}

		return integerArbitrary;
	}

	@Override
	public Arbitrary<Long> longs(
		LongArbitrary longArbitrary,
		ArbitraryGeneratorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<BigInteger> bigIntegers(
		BigIntegerArbitrary bigIntegerArbitrary,
		ArbitraryGeneratorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<BigDecimal> bigDecimals(
		BigDecimalArbitrary bigDecimalArbitrary,
		ArbitraryGeneratorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}
}
