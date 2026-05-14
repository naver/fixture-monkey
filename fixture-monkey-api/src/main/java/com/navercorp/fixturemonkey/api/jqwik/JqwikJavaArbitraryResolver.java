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
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.arbitraries.BigDecimalArbitrary;
import net.jqwik.api.arbitraries.BigIntegerArbitrary;
import net.jqwik.api.arbitraries.ByteArbitrary;
import net.jqwik.api.arbitraries.DoubleArbitrary;
import net.jqwik.api.arbitraries.FloatArbitrary;
import net.jqwik.api.arbitraries.IntegerArbitrary;
import net.jqwik.api.arbitraries.LongArbitrary;
import net.jqwik.api.arbitraries.ShortArbitrary;
import net.jqwik.api.arbitraries.StringArbitrary;
import net.jqwik.web.api.Web;

import com.navercorp.fixturemonkey.api.constraint.JavaConstraintGenerator;
import com.navercorp.fixturemonkey.api.constraint.JavaDecimalConstraint;
import com.navercorp.fixturemonkey.api.constraint.JavaIntegerConstraint;
import com.navercorp.fixturemonkey.api.constraint.JavaStringConstraint;
import com.navercorp.fixturemonkey.api.constraint.JavaStringConstraint.PatternConstraint;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.random.RegexGenerator;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.6.9", status = Status.MAINTAINED)
public final class JqwikJavaArbitraryResolver implements JavaArbitraryResolver {
	private static final RegexGenerator REGEX_GENERATOR = new RegexGenerator();
	private final JavaConstraintGenerator constraintGenerator;

	public JqwikJavaArbitraryResolver(JavaConstraintGenerator constraintGenerator) {
		this.constraintGenerator = constraintGenerator;
	}

	@Override
	public Arbitrary<String> strings(
		StringArbitrary stringArbitrary,
		ArbitraryGeneratorContext context
	) {
		JavaStringConstraint constraint = this.constraintGenerator.generateStringConstraint(context);
		if (constraint == null) {
			return stringArbitrary;
		}

		BigInteger min = constraint.getMinSize();
		BigInteger max = constraint.getMaxSize();
		boolean digits = constraint.isDigits();
		boolean notBlank = constraint.isNotBlank();
		boolean email = constraint.isEmail();
		PatternConstraint pattern = constraint.getPattern();

		Arbitrary<String> arbitrary = stringArbitrary;
		if (pattern != null) {
			int minLength = min == null ? 0 : min.intValue();
			int maxLength = max == null ? Integer.MAX_VALUE : max.intValue();

			Predicate<String> lengthCondition = it -> it.length() >= minLength && it.length() <= maxLength;
			Predicate<String> notBlankCondition = it -> !(notBlank && it.trim().isEmpty());

			return Arbitraries.ofSuppliers(() -> REGEX_GENERATOR.generate(
				pattern.getRegexp(),
				pattern.getFlags(),
				lengthCondition.and(notBlankCondition)
			));
		}

		if (email) {
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
			}
			arbitrary = stringArbitrary;
		}

		return arbitrary
			.filter(it -> {
				if (!notBlank) {
					if (it == null || it.isEmpty()) {
						return true;
					}
				}

				return it != null && !it.trim().isEmpty();
			});
	}

	@Override
	public Arbitrary<Short> shorts(
		ShortArbitrary shortArbitrary,
		ArbitraryGeneratorContext context
	) {
		JavaIntegerConstraint constraint = this.constraintGenerator.generateIntegerConstraint(context);
		if (constraint == null) {
			return shortArbitrary;
		}

		BigInteger min = constraint.getMin();
		BigInteger max = constraint.getMax();

		ShortArbitrary arbitrary = null;

		if (min != null) {
			arbitrary = Types.defaultIfNull(arbitrary, () -> shortArbitrary);
			arbitrary = arbitrary.greaterOrEqual(min.shortValueExact());
		}

		if (max != null) {
			arbitrary = Types.defaultIfNull(arbitrary, () -> shortArbitrary);
			arbitrary = arbitrary.lessOrEqual(max.shortValueExact());
		}

		return arbitrary != null ? arbitrary : shortArbitrary;
	}

	@Override
	public Arbitrary<Byte> bytes(
		ByteArbitrary byteArbitrary,
		ArbitraryGeneratorContext context
	) {
		JavaIntegerConstraint constraint = this.constraintGenerator.generateIntegerConstraint(context);
		if (constraint == null) {
			return byteArbitrary;
		}

		BigInteger min = constraint.getMin();
		BigInteger max = constraint.getMax();

		ByteArbitrary arbitrary = null;

		if (min != null) {
			arbitrary = Types.defaultIfNull(arbitrary, () -> byteArbitrary);
			arbitrary = arbitrary.greaterOrEqual(min.byteValueExact());
		}

		if (max != null) {
			arbitrary = Types.defaultIfNull(arbitrary, () -> byteArbitrary);
			arbitrary = arbitrary.lessOrEqual(max.byteValueExact());
		}

		return arbitrary != null ? arbitrary : byteArbitrary;
	}

	@Override
	public Arbitrary<Float> floats(
		FloatArbitrary floatArbitrary,
		ArbitraryGeneratorContext context
	) {
		JavaDecimalConstraint constraint = this.constraintGenerator.generateDecimalConstraint(context);
		if (constraint == null) {
			return floatArbitrary;
		}

		BigDecimal min = constraint.getMin();
		Boolean minInclusive = constraint.getMinInclusive();
		BigDecimal max = constraint.getMax();
		Boolean maxInclusive = constraint.getMaxInclusive();
		Integer scale = constraint.getScale();

		FloatArbitrary arbitrary = null;

		if (min != null) {
			arbitrary = Types.defaultIfNull(arbitrary, () -> floatArbitrary);
			if (minInclusive == null || minInclusive) {
				arbitrary = arbitrary.greaterOrEqual(min.floatValue());
			} else {
				arbitrary = arbitrary.greaterThan(min.floatValue());
			}
		}
		if (max != null) {
			arbitrary = Types.defaultIfNull(arbitrary, () -> floatArbitrary);
			if (maxInclusive == null || maxInclusive) {
				arbitrary = arbitrary.lessOrEqual(max.floatValue());
			} else {
				arbitrary = arbitrary.lessThan(max.floatValue());
			}
		}
		if (scale != null) {
			if (arbitrary != null) {
				arbitrary = arbitrary.ofScale(scale);
			}
		}

		if (arbitrary == null) {
			return floatArbitrary;
		}
		return arbitrary;
	}

	@Override
	public Arbitrary<Double> doubles(
		DoubleArbitrary doubleArbitrary,
		ArbitraryGeneratorContext context
	) {
		JavaDecimalConstraint constraint = this.constraintGenerator.generateDecimalConstraint(context);
		if (constraint == null) {
			return doubleArbitrary;
		}

		BigDecimal min = constraint.getMin();
		Boolean minInclusive = constraint.getMinInclusive();
		BigDecimal max = constraint.getMax();
		Boolean maxInclusive = constraint.getMaxInclusive();
		Integer scale = constraint.getScale();

		DoubleArbitrary arbitrary = null;

		if (min != null) {
			arbitrary = Types.defaultIfNull(arbitrary, () -> doubleArbitrary);
			if (minInclusive == null || minInclusive) {
				arbitrary = arbitrary.greaterOrEqual(min.doubleValue());
			} else {
				arbitrary = arbitrary.greaterThan(min.doubleValue());
			}
		}
		if (max != null) {
			arbitrary = Types.defaultIfNull(arbitrary, () -> doubleArbitrary);
			if (maxInclusive == null || maxInclusive) {
				arbitrary = arbitrary.lessOrEqual(max.doubleValue());
			} else {
				arbitrary = arbitrary.lessThan(max.doubleValue());
			}
		}
		if (scale != null) {
			if (arbitrary != null) {
				arbitrary = arbitrary.ofScale(scale);
			}
		}

		if (arbitrary == null) {
			return doubleArbitrary;
		}
		return arbitrary;
	}

	@Override
	public Arbitrary<Integer> integers(
		IntegerArbitrary integerArbitrary,
		ArbitraryGeneratorContext context
	) {
		JavaIntegerConstraint constraint = this.constraintGenerator.generateIntegerConstraint(context);
		if (constraint == null) {
			return integerArbitrary;
		}

		BigInteger min = constraint.getMin();
		BigInteger max = constraint.getMax();

		IntegerArbitrary arbitrary = null;

		if (min != null) {
			arbitrary = Types.defaultIfNull(arbitrary, () -> integerArbitrary);
			arbitrary = arbitrary.greaterOrEqual(min.intValueExact());
		}

		if (max != null) {
			arbitrary = Types.defaultIfNull(arbitrary, () -> integerArbitrary);
			arbitrary = arbitrary.lessOrEqual(max.intValueExact());
		}

		return arbitrary != null ? arbitrary : integerArbitrary;
	}

	@Override
	public Arbitrary<Long> longs(
		LongArbitrary longArbitrary,
		ArbitraryGeneratorContext context
	) {
		JavaIntegerConstraint constraint = this.constraintGenerator.generateIntegerConstraint(context);
		if (constraint == null) {
			return longArbitrary;
		}

		BigInteger min = constraint.getMin();
		BigInteger max = constraint.getMax();

		LongArbitrary arbitrary = null;

		if (min != null) {
			arbitrary = Types.defaultIfNull(arbitrary, () -> longArbitrary);
			arbitrary = arbitrary.greaterOrEqual(min.longValueExact());
		}

		if (max != null) {
			arbitrary = Types.defaultIfNull(arbitrary, () -> longArbitrary);
			arbitrary = arbitrary.lessOrEqual(max.longValueExact());
		}

		return arbitrary != null ? arbitrary : longArbitrary;
	}

	@Override
	public Arbitrary<BigInteger> bigIntegers(
		BigIntegerArbitrary bigIntegerArbitrary,
		ArbitraryGeneratorContext context
	) {
		JavaIntegerConstraint constraint = this.constraintGenerator.generateIntegerConstraint(context);
		if (constraint == null) {
			return bigIntegerArbitrary;
		}

		BigInteger min = constraint.getMin();
		BigInteger max = constraint.getMax();

		BigIntegerArbitrary arbitrary = null;

		if (min != null) {
			arbitrary = Types.defaultIfNull(arbitrary, () -> bigIntegerArbitrary);
			arbitrary = arbitrary.greaterOrEqual(min);
		}

		if (max != null) {
			arbitrary = Types.defaultIfNull(arbitrary, () -> bigIntegerArbitrary);
			arbitrary = arbitrary.lessOrEqual(max);
		}

		return arbitrary != null ? arbitrary : bigIntegerArbitrary;
	}

	@Override
	public Arbitrary<BigDecimal> bigDecimals(
		BigDecimalArbitrary bigDecimalArbitrary,
		ArbitraryGeneratorContext context
	) {
		JavaDecimalConstraint constraint = this.constraintGenerator.generateDecimalConstraint(context);
		if (constraint == null) {
			return bigDecimalArbitrary;
		}

		BigDecimal min = constraint.getMin();
		Boolean minInclusive = constraint.getMinInclusive();
		BigDecimal max = constraint.getMax();
		Boolean maxInclusive = constraint.getMaxInclusive();
		Integer scale = constraint.getScale();

		BigDecimalArbitrary arbitrary = null;

		if (min != null) {
			arbitrary = Types.defaultIfNull(arbitrary, () -> bigDecimalArbitrary);
			if (minInclusive == null || minInclusive) {
				arbitrary = arbitrary.greaterOrEqual(min);
			} else {
				arbitrary = arbitrary.greaterThan(min);
			}
		}
		if (max != null) {
			arbitrary = Types.defaultIfNull(arbitrary, () -> bigDecimalArbitrary);
			if (maxInclusive == null || maxInclusive) {
				arbitrary = arbitrary.lessOrEqual(max);
			} else {
				arbitrary = arbitrary.lessThan(max);
			}
		}
		if (scale != null) {
			if (arbitrary != null) {
				arbitrary = arbitrary.ofScale(scale);
			}
		}

		if (arbitrary == null) {
			return bigDecimalArbitrary;
		}
		return arbitrary;
	}

	private static <T> Arbitrary<T> resolveArbitrary(
		Arbitrary<T> defaultArbitrary,
		@Nullable Arbitrary<T> positiveArbitrary,
		@Nullable Arbitrary<T> negativeArbitrary
	) {
		if (positiveArbitrary != null && negativeArbitrary != null) {
			return Arbitraries.oneOf(positiveArbitrary, negativeArbitrary);
		}

		if (positiveArbitrary != null) {
			return positiveArbitrary;
		}

		if (negativeArbitrary != null) {
			return negativeArbitrary;
		}

		return defaultArbitrary;
	}
}
