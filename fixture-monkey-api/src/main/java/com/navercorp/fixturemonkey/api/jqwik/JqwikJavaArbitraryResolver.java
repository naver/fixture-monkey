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

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

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

		BigInteger positiveMin = constraint.getPositiveMin();
		BigInteger positiveMax = constraint.getPositiveMax();
		BigInteger negativeMin = constraint.getNegativeMin();
		BigInteger negativeMax = constraint.getNegativeMax();

		ShortArbitrary positiveArbitrary = null;
		ShortArbitrary negativeArbitrary = null;
		if (positiveMin != null) {
			positiveArbitrary = Types.defaultIfNull(positiveArbitrary, () -> shortArbitrary);
			positiveArbitrary = positiveArbitrary.greaterOrEqual(positiveMin.shortValueExact());
		}
		if (positiveMax != null) {
			positiveArbitrary = Types.defaultIfNull(positiveArbitrary, () -> shortArbitrary);
			positiveArbitrary = positiveArbitrary.lessOrEqual(positiveMax.shortValueExact());
		}
		if (negativeMin != null) {
			negativeArbitrary = Types.defaultIfNull(negativeArbitrary, () -> shortArbitrary);
			negativeArbitrary = negativeArbitrary.greaterOrEqual(negativeMin.shortValueExact());
		}
		if (negativeMax != null) {
			negativeArbitrary = Types.defaultIfNull(negativeArbitrary, () -> shortArbitrary);
			negativeArbitrary = negativeArbitrary.lessOrEqual(negativeMax.shortValueExact());
		}
		return resolveArbitrary(shortArbitrary, positiveArbitrary, negativeArbitrary);
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

		BigInteger positiveMin = constraint.getPositiveMin();
		BigInteger positiveMax = constraint.getPositiveMax();
		BigInteger negativeMin = constraint.getNegativeMin();
		BigInteger negativeMax = constraint.getNegativeMax();

		ByteArbitrary positiveArbitrary = null;
		ByteArbitrary negativeArbitrary = null;
		if (positiveMin != null) {
			positiveArbitrary = Types.defaultIfNull(positiveArbitrary, () -> byteArbitrary);
			positiveArbitrary = positiveArbitrary.greaterOrEqual(positiveMin.byteValueExact());
		}
		if (positiveMax != null) {
			positiveArbitrary = Types.defaultIfNull(positiveArbitrary, () -> byteArbitrary);
			positiveArbitrary = positiveArbitrary.lessOrEqual(positiveMax.byteValueExact());
		}
		if (negativeMin != null) {
			negativeArbitrary = Types.defaultIfNull(negativeArbitrary, () -> byteArbitrary);
			negativeArbitrary = negativeArbitrary.greaterOrEqual(negativeMin.byteValueExact());
		}
		if (negativeMax != null) {
			negativeArbitrary = Types.defaultIfNull(negativeArbitrary, () -> byteArbitrary);
			negativeArbitrary = negativeArbitrary.lessOrEqual(negativeMax.byteValueExact());
		}
		return resolveArbitrary(byteArbitrary, positiveArbitrary, negativeArbitrary);
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

		BigInteger positiveMin = constraint.getPositiveMin();
		BigInteger positiveMax = constraint.getPositiveMax();
		BigInteger negativeMin = constraint.getNegativeMin();
		BigInteger negativeMax = constraint.getNegativeMax();

		IntegerArbitrary positiveArbitrary = null;
		IntegerArbitrary negativeArbitrary = null;
		if (positiveMin != null) {
			positiveArbitrary = Types.defaultIfNull(positiveArbitrary, () -> integerArbitrary);
			positiveArbitrary = positiveArbitrary.greaterOrEqual(positiveMin.intValueExact());
		}
		if (positiveMax != null) {
			positiveArbitrary = Types.defaultIfNull(positiveArbitrary, () -> integerArbitrary);
			positiveArbitrary = positiveArbitrary.lessOrEqual(positiveMax.intValueExact());
		}
		if (negativeMin != null) {
			negativeArbitrary = Types.defaultIfNull(negativeArbitrary, () -> integerArbitrary);
			negativeArbitrary = negativeArbitrary.greaterOrEqual(negativeMin.intValueExact());
		}
		if (negativeMax != null) {
			negativeArbitrary = Types.defaultIfNull(negativeArbitrary, () -> integerArbitrary);
			negativeArbitrary = negativeArbitrary.lessOrEqual(negativeMax.intValueExact());
		}
		return resolveArbitrary(integerArbitrary, positiveArbitrary, negativeArbitrary);
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

		BigInteger positiveMin = constraint.getPositiveMin();
		BigInteger positiveMax = constraint.getPositiveMax();
		BigInteger negativeMin = constraint.getNegativeMin();
		BigInteger negativeMax = constraint.getNegativeMax();

		LongArbitrary positiveArbitrary = null;
		LongArbitrary negativeArbitrary = null;
		if (positiveMin != null) {
			positiveArbitrary = Types.defaultIfNull(positiveArbitrary, () -> longArbitrary);
			positiveArbitrary = positiveArbitrary.greaterOrEqual(positiveMin.longValueExact());
		}
		if (positiveMax != null) {
			positiveArbitrary = Types.defaultIfNull(positiveArbitrary, () -> longArbitrary);
			positiveArbitrary = positiveArbitrary.lessOrEqual(positiveMax.longValueExact());
		}
		if (negativeMin != null) {
			negativeArbitrary = Types.defaultIfNull(negativeArbitrary, () -> longArbitrary);
			negativeArbitrary = negativeArbitrary.greaterOrEqual(negativeMin.longValueExact());
		}
		if (negativeMax != null) {
			negativeArbitrary = Types.defaultIfNull(negativeArbitrary, () -> longArbitrary);
			negativeArbitrary = negativeArbitrary.lessOrEqual(negativeMax.longValueExact());
		}
		return resolveArbitrary(longArbitrary, positiveArbitrary, negativeArbitrary);
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

		BigInteger positiveMin = constraint.getPositiveMin();
		BigInteger positiveMax = constraint.getPositiveMax();
		BigInteger negativeMin = constraint.getNegativeMin();
		BigInteger negativeMax = constraint.getNegativeMax();

		BigIntegerArbitrary positiveArbitrary = null;
		BigIntegerArbitrary negativeArbitrary = null;
		if (positiveMin != null) {
			positiveArbitrary = Types.defaultIfNull(positiveArbitrary, () -> bigIntegerArbitrary);
			positiveArbitrary = positiveArbitrary.greaterOrEqual(positiveMin);
		}
		if (positiveMax != null) {
			positiveArbitrary = Types.defaultIfNull(positiveArbitrary, () -> bigIntegerArbitrary);
			positiveArbitrary = positiveArbitrary.lessOrEqual(positiveMax);
		}
		if (negativeMin != null) {
			negativeArbitrary = Types.defaultIfNull(negativeArbitrary, () -> bigIntegerArbitrary);
			negativeArbitrary = negativeArbitrary.greaterOrEqual(negativeMin);
		}
		if (negativeMax != null) {
			negativeArbitrary = Types.defaultIfNull(negativeArbitrary, () -> bigIntegerArbitrary);
			negativeArbitrary = negativeArbitrary.lessOrEqual(negativeMax);
		}

		return resolveArbitrary(bigIntegerArbitrary, positiveArbitrary, negativeArbitrary);
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
