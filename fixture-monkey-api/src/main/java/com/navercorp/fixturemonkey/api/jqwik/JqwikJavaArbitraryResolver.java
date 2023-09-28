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

import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.function.Supplier;

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
import com.navercorp.fixturemonkey.api.introspector.JavaArbitraryResolver;
import com.navercorp.fixturemonkey.api.random.RegexGenerator;

@API(since = "0.6.9", status = Status.EXPERIMENTAL)
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
			Integer minValue = min != null ? min.intValue() : null;
			Integer maxValue = max != null ? max.intValue() : null;
			List<String> values = REGEX_GENERATOR.generateAll(
				pattern.getRegexp(),
				pattern.getFlags(),
				minValue,
				maxValue
			);
			if (notBlank) {
				values = values.stream()
					.filter(it -> it != null && !it.trim().isEmpty())
					.collect(toList());
			}

			return Arbitraries.of(values);
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
					if (it == null) {
						return true;
					}
				}

				if (it == null || it.trim().isEmpty()) {
					return false;
				}

				return true;
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
			positiveArbitrary = defaultIfNull(positiveArbitrary, () -> shortArbitrary);
			positiveArbitrary = positiveArbitrary.greaterOrEqual(positiveMin.shortValueExact());
		}
		if (positiveMax != null) {
			positiveArbitrary = defaultIfNull(positiveArbitrary, () -> shortArbitrary);
			positiveArbitrary = positiveArbitrary.lessOrEqual(positiveMax.shortValueExact());
		}
		if (negativeMin != null) {
			negativeArbitrary = defaultIfNull(negativeArbitrary, () -> shortArbitrary);
			negativeArbitrary = negativeArbitrary.greaterOrEqual(negativeMin.shortValueExact());
		}
		if (negativeMax != null) {
			negativeArbitrary = defaultIfNull(negativeArbitrary, () -> shortArbitrary);
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
			positiveArbitrary = defaultIfNull(positiveArbitrary, () -> byteArbitrary);
			positiveArbitrary = positiveArbitrary.greaterOrEqual(positiveMin.byteValueExact());
		}
		if (positiveMax != null) {
			positiveArbitrary = defaultIfNull(positiveArbitrary, () -> byteArbitrary);
			positiveArbitrary = positiveArbitrary.lessOrEqual(positiveMax.byteValueExact());
		}
		if (negativeMin != null) {
			negativeArbitrary = defaultIfNull(negativeArbitrary, () -> byteArbitrary);
			negativeArbitrary = negativeArbitrary.greaterOrEqual(negativeMin.byteValueExact());
		}
		if (negativeMax != null) {
			negativeArbitrary = defaultIfNull(negativeArbitrary, () -> byteArbitrary);
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

		BigDecimal positiveMin = constraint.getPositiveMin();
		BigDecimal positiveMax = constraint.getPositiveMax();
		Boolean positiveMinInclusive = constraint.getPositiveMinInclusive();
		Boolean positiveMaxInclusive = constraint.getPositiveMaxInclusive();
		BigDecimal negativeMin = constraint.getNegativeMin();
		BigDecimal negativeMax = constraint.getNegativeMax();
		Boolean negativeMinInclusive = constraint.getNegativeMinInclusive();
		Boolean negativeMaxInclusive = constraint.getNegativeMaxInclusive();
		Integer scale = constraint.getScale();

		FloatArbitrary positiveArbitrary = null;
		FloatArbitrary negativeArbitrary = null;
		if (positiveMin != null) {
			positiveArbitrary = defaultIfNull(positiveArbitrary, () -> floatArbitrary);
			if (positiveMinInclusive != null && positiveMinInclusive) {
				positiveArbitrary = positiveArbitrary.greaterOrEqual(positiveMin.floatValue());
			} else {
				positiveArbitrary = positiveArbitrary.greaterThan(positiveMin.floatValue());
			}
		}
		if (positiveMax != null) {
			positiveArbitrary = defaultIfNull(positiveArbitrary, () -> floatArbitrary);
			if (positiveMaxInclusive != null && positiveMaxInclusive) {
				positiveArbitrary = positiveArbitrary.lessOrEqual(positiveMax.floatValue());
			} else {
				positiveArbitrary = positiveArbitrary.lessThan(positiveMax.floatValue());
			}
		}
		if (negativeMin != null) {
			negativeArbitrary = defaultIfNull(negativeArbitrary, () -> floatArbitrary);
			if (negativeMinInclusive != null && negativeMinInclusive) {
				negativeArbitrary = negativeArbitrary.greaterOrEqual(negativeMin.floatValue());
			} else {
				negativeArbitrary = negativeArbitrary.greaterThan(negativeMin.floatValue());
			}
		}
		if (negativeMax != null) {
			negativeArbitrary = defaultIfNull(negativeArbitrary, () -> floatArbitrary);
			if (negativeMaxInclusive != null && negativeMaxInclusive) {
				negativeArbitrary = negativeArbitrary.lessOrEqual(negativeMax.floatValue());
			} else {
				negativeArbitrary = negativeArbitrary.lessThan(negativeMax.floatValue());
			}
		}

		if (scale != null) {
			if (positiveArbitrary != null) {
				positiveArbitrary = positiveArbitrary.ofScale(scale);
			}
			if (negativeArbitrary != null) {
				negativeArbitrary = negativeArbitrary.ofScale(scale);
			}
		}

		return resolveArbitrary(floatArbitrary, positiveArbitrary, negativeArbitrary);
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

		BigDecimal positiveMin = constraint.getPositiveMin();
		BigDecimal positiveMax = constraint.getPositiveMax();
		Boolean positiveMinInclusive = constraint.getPositiveMinInclusive();
		Boolean positiveMaxInclusive = constraint.getPositiveMaxInclusive();
		BigDecimal negativeMin = constraint.getNegativeMin();
		BigDecimal negativeMax = constraint.getNegativeMax();
		Boolean negativeMinInclusive = constraint.getNegativeMinInclusive();
		Boolean negativeMaxInclusive = constraint.getNegativeMaxInclusive();
		Integer scale = constraint.getScale();

		DoubleArbitrary positiveArbitrary = null;
		DoubleArbitrary negativeArbitrary = null;
		if (positiveMin != null) {
			positiveArbitrary = defaultIfNull(positiveArbitrary, () -> doubleArbitrary);
			if (positiveMinInclusive != null && positiveMinInclusive) {
				positiveArbitrary = positiveArbitrary.greaterOrEqual(positiveMin.doubleValue());
			} else {
				positiveArbitrary = positiveArbitrary.greaterThan(positiveMin.doubleValue());
			}
		}
		if (positiveMax != null) {
			positiveArbitrary = defaultIfNull(positiveArbitrary, () -> doubleArbitrary);
			if (positiveMaxInclusive != null && positiveMaxInclusive) {
				positiveArbitrary = positiveArbitrary.lessOrEqual(positiveMax.doubleValue());
			} else {
				positiveArbitrary = positiveArbitrary.lessThan(positiveMax.doubleValue());
			}
		}
		if (negativeMin != null) {
			negativeArbitrary = defaultIfNull(negativeArbitrary, () -> doubleArbitrary);
			if (negativeMinInclusive != null && negativeMinInclusive) {
				negativeArbitrary = negativeArbitrary.greaterOrEqual(negativeMin.floatValue());
			} else {
				negativeArbitrary = negativeArbitrary.greaterThan(negativeMin.floatValue());
			}
		}
		if (negativeMax != null) {
			negativeArbitrary = defaultIfNull(negativeArbitrary, () -> doubleArbitrary);
			if (negativeMaxInclusive != null && negativeMaxInclusive) {
				negativeArbitrary = negativeArbitrary.lessOrEqual(negativeMax.doubleValue());
			} else {
				negativeArbitrary = negativeArbitrary.lessThan(negativeMax.doubleValue());
			}
		}

		if (scale != null) {
			if (positiveArbitrary != null) {
				positiveArbitrary = positiveArbitrary.ofScale(scale);
			}
			if (negativeArbitrary != null) {
				negativeArbitrary = negativeArbitrary.ofScale(scale);
			}
		}

		return resolveArbitrary(doubleArbitrary, positiveArbitrary, negativeArbitrary);
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
			positiveArbitrary = defaultIfNull(positiveArbitrary, () -> integerArbitrary);
			positiveArbitrary = positiveArbitrary.greaterOrEqual(positiveMin.intValueExact());
		}
		if (positiveMax != null) {
			positiveArbitrary = defaultIfNull(positiveArbitrary, () -> integerArbitrary);
			positiveArbitrary = positiveArbitrary.lessOrEqual(positiveMax.intValueExact());
		}
		if (negativeMin != null) {
			negativeArbitrary = defaultIfNull(negativeArbitrary, () -> integerArbitrary);
			negativeArbitrary = negativeArbitrary.greaterOrEqual(negativeMin.intValueExact());
		}
		if (negativeMax != null) {
			negativeArbitrary = defaultIfNull(negativeArbitrary, () -> integerArbitrary);
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
			positiveArbitrary = defaultIfNull(positiveArbitrary, () -> longArbitrary);
			positiveArbitrary = positiveArbitrary.greaterOrEqual(positiveMin.longValueExact());
		}
		if (positiveMax != null) {
			positiveArbitrary = defaultIfNull(positiveArbitrary, () -> longArbitrary);
			positiveArbitrary = positiveArbitrary.lessOrEqual(positiveMax.longValueExact());
		}
		if (negativeMin != null) {
			negativeArbitrary = defaultIfNull(negativeArbitrary, () -> longArbitrary);
			negativeArbitrary = negativeArbitrary.greaterOrEqual(negativeMin.longValueExact());
		}
		if (negativeMax != null) {
			negativeArbitrary = defaultIfNull(negativeArbitrary, () -> longArbitrary);
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
			positiveArbitrary = defaultIfNull(positiveArbitrary, () -> bigIntegerArbitrary);
			positiveArbitrary = positiveArbitrary.greaterOrEqual(positiveMin);
		}
		if (positiveMax != null) {
			positiveArbitrary = defaultIfNull(positiveArbitrary, () -> bigIntegerArbitrary);
			positiveArbitrary = positiveArbitrary.lessOrEqual(positiveMax);
		}
		if (negativeMin != null) {
			negativeArbitrary = defaultIfNull(negativeArbitrary, () -> bigIntegerArbitrary);
			negativeArbitrary = negativeArbitrary.greaterOrEqual(negativeMin);
		}
		if (negativeMax != null) {
			negativeArbitrary = defaultIfNull(negativeArbitrary, () -> bigIntegerArbitrary);
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

		BigDecimal positiveMin = constraint.getPositiveMin();
		BigDecimal positiveMax = constraint.getPositiveMax();
		Boolean positiveMinInclusive = constraint.getPositiveMinInclusive();
		Boolean positiveMaxInclusive = constraint.getPositiveMaxInclusive();
		BigDecimal negativeMin = constraint.getNegativeMin();
		BigDecimal negativeMax = constraint.getNegativeMax();
		Boolean negativeMinInclusive = constraint.getNegativeMinInclusive();
		Boolean negativeMaxInclusive = constraint.getNegativeMaxInclusive();
		Integer scale = constraint.getScale();

		BigDecimalArbitrary positiveArbitrary = null;
		BigDecimalArbitrary negativeArbitrary = null;
		if (positiveMin != null) {
			positiveArbitrary = defaultIfNull(positiveArbitrary, () -> bigDecimalArbitrary);
			if (positiveMinInclusive != null && positiveMinInclusive) {
				positiveArbitrary = positiveArbitrary.greaterOrEqual(positiveMin);
			} else {
				positiveArbitrary = positiveArbitrary.greaterThan(positiveMin);
			}
		}
		if (positiveMax != null) {
			positiveArbitrary = defaultIfNull(positiveArbitrary, () -> bigDecimalArbitrary);
			if (positiveMaxInclusive != null && positiveMaxInclusive) {
				positiveArbitrary = positiveArbitrary.lessOrEqual(positiveMax);
			} else {
				positiveArbitrary = positiveArbitrary.lessThan(positiveMax);
			}
		}

		if (negativeMin != null) {
			negativeArbitrary = defaultIfNull(negativeArbitrary, () -> bigDecimalArbitrary);
			if (negativeMinInclusive != null && negativeMinInclusive) {
				negativeArbitrary = negativeArbitrary.greaterOrEqual(negativeMin);
			} else {
				negativeArbitrary = negativeArbitrary.greaterThan(negativeMin);
			}
		}
		if (negativeMax != null) {
			negativeArbitrary = defaultIfNull(negativeArbitrary, () -> bigDecimalArbitrary);
			if (negativeMaxInclusive != null && negativeMaxInclusive) {
				negativeArbitrary = negativeArbitrary.lessOrEqual(negativeMax);
			} else {
				negativeArbitrary = negativeArbitrary.lessThan(negativeMax);
			}
		}

		if (scale != null) {
			if (positiveArbitrary != null) {
				positiveArbitrary = positiveArbitrary.ofScale(scale);
			}
			if (negativeArbitrary != null) {
				negativeArbitrary = negativeArbitrary.ofScale(scale);
			}
		}

		return resolveArbitrary(bigDecimalArbitrary, positiveArbitrary, negativeArbitrary);
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

	private static <T> T defaultIfNull(@Nullable T obj, Supplier<T> defaultValue) {
		return obj != null ? obj : defaultValue.get();
	}
}
