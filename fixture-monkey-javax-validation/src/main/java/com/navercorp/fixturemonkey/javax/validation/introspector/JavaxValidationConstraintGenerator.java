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

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Negative;
import javax.validation.constraints.NegativeOrZero;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;

@API(since = "0.4.0", status = Status.MAINTAINED)
public class JavaxValidationConstraintGenerator {
	private static final BigInteger BIG_INTEGER_MIN_LONG = BigInteger.valueOf(Long.MIN_VALUE);
	private static final BigInteger BIG_INTEGER_MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);
	private static final BigInteger BIG_INTEGER_MIN_INT = BigInteger.valueOf(Integer.MIN_VALUE);
	private static final BigInteger BIG_INTEGER_MAX_INT = BigInteger.valueOf(Integer.MAX_VALUE);
	private static final BigInteger BIG_INTEGER_MIN_SHORT = BigInteger.valueOf(Short.MIN_VALUE);
	private static final BigInteger BIG_INTEGER_MAX_SHORT = BigInteger.valueOf(Short.MAX_VALUE);
	private static final BigInteger BIG_INTEGER_MIN_BYTE = BigInteger.valueOf(Byte.MIN_VALUE);
	private static final BigInteger BIG_INTEGER_MAX_BYTE = BigInteger.valueOf(Byte.MAX_VALUE);

	public JavaxValidationStringConstraint generateStringConstraint(ArbitraryGeneratorContext context) {
		BigInteger min = null;
		BigInteger max = null;
		boolean digits = false;
		boolean notBlank = context.findAnnotation(NotBlank.class).isPresent();

		if (notBlank || context.findAnnotation(NotEmpty.class).isPresent()) {
			min = BigInteger.ONE;
		}

		Optional<Size> size = context.findAnnotation(Size.class);
		if (size.isPresent()) {
			int minValue = size.map(Size::min).get();
			if (min == null) {
				min = BigInteger.valueOf(minValue);
			} else if (minValue > 1) {
				min = BigInteger.valueOf(minValue);
			}

			max = BigInteger.valueOf(size.map(Size::max).get());
		}

		// TODO: support fraction
		Optional<Digits> digitsAnnotation = context.findAnnotation(Digits.class);
		if (digitsAnnotation.isPresent()) {
			digits = true;
			notBlank = true;

			BigInteger maxValue = digitsAnnotation.map(Digits::integer).map(BigInteger::valueOf).get();
			if (max == null) {
				max = maxValue;
			} else if (max.compareTo(maxValue) > 0) {
				max = maxValue;
			}
		}

		return new JavaxValidationStringConstraint(min, max, digits, notBlank);
	}

	public JavaxValidationIntegerConstraint generateIntegerConstraint(ArbitraryGeneratorContext context) {
		BigInteger min = null;
		BigInteger max = null;

		Optional<Digits> digits = context.findAnnotation(Digits.class);
		if (digits.isPresent()) {
			BigInteger value = BigInteger.ONE;
			int integer = digits.get().integer();
			if (integer > 1) {
				value = BigInteger.TEN.pow(integer - 1);
			}
			max = value.multiply(BigInteger.TEN).subtract(BigInteger.ONE);
			min = max.negate();
		}

		Optional<Min> minAnnotation = context.findAnnotation(Min.class);
		if (minAnnotation.isPresent()) {
			BigInteger minValue = minAnnotation.map(Min::value).map(BigInteger::valueOf).get();
			if (min == null) {
				min = minValue;
			} else if (min.compareTo(minValue) > 0) {
				min = minValue;
			}
		}

		Optional<DecimalMin> decimalMinAnnotation = context.findAnnotation(DecimalMin.class);
		if (decimalMinAnnotation.isPresent()) {
			BigInteger decimalMin = decimalMinAnnotation
				.map(DecimalMin::value)
				.map(BigInteger::new)
				.get();

			if (!decimalMinAnnotation.map(DecimalMin::inclusive).get()) {
				decimalMin = decimalMin.add(BigInteger.ONE);
			}

			if (min == null) {
				min = decimalMin;
			} else if (min.compareTo(decimalMin) < 0) {
				min = decimalMin;
			}
		}

		Optional<Max> maxAnnotation = context.findAnnotation(Max.class);
		if (maxAnnotation.isPresent()) {
			BigInteger maxValue = maxAnnotation.map(Max::value).map(BigInteger::valueOf).get();
			if (max == null) {
				max = maxValue;
			} else if (max.compareTo(maxValue) > 0) {
				max = maxValue;
			}
		}

		Optional<DecimalMax> decimalMaxAnnotation = context.findAnnotation(DecimalMax.class);
		if (decimalMaxAnnotation.isPresent()) {
			BigInteger decimalMax = decimalMaxAnnotation
				.map(DecimalMax::value)
				.map(BigInteger::new)
				.get();
			if (!decimalMaxAnnotation.map(DecimalMax::inclusive).get()) {
				decimalMax = decimalMax.subtract(BigInteger.ONE);
			}

			if (max == null) {
				max = decimalMax;
			} else if (max.compareTo(decimalMax) > 0) {
				max = decimalMax;
			}
		}

		if (context.findAnnotation(Negative.class).isPresent()) {
			if (max == null || max.compareTo(BigInteger.ZERO) > 0) {
				max = BigInteger.valueOf(-1);
			}
		}

		if (context.findAnnotation(NegativeOrZero.class).isPresent()) {
			if (max == null || max.compareTo(BigInteger.ZERO) > 0) {
				max = BigInteger.ZERO;
			}
		}

		if (context.findAnnotation(Positive.class).isPresent()) {
			if (min == null || min.compareTo(BigInteger.ZERO) < 0) {
				min = BigInteger.ONE;
			}
		}

		if (context.findAnnotation(PositiveOrZero.class).isPresent()) {
			if (min == null || min.compareTo(BigInteger.ZERO) < 0) {
				min = BigInteger.ZERO;
			}
		}

		Type type = context.getResolvedType();
		if (min != null) {
			if ((type == Long.class || type == long.class) && min.compareTo(BIG_INTEGER_MIN_LONG) < 0) {
				min = BIG_INTEGER_MIN_LONG;
			}
			if ((type == Integer.class || type == int.class) && min.compareTo(BIG_INTEGER_MIN_INT) < 0) {
				min = BIG_INTEGER_MIN_INT;
			}
			if ((type == Short.class || type == short.class) && min.compareTo(BIG_INTEGER_MIN_SHORT) < 0) {
				min = BIG_INTEGER_MIN_SHORT;
			}
			if ((type == Byte.class || type == byte.class) && min.compareTo(BIG_INTEGER_MIN_BYTE) < 0) {
				min = BIG_INTEGER_MIN_BYTE;
			}
		}
		if (max != null) {
			if ((type == Long.class || type == long.class) && max.compareTo(BIG_INTEGER_MAX_LONG) > 0) {
				max = BIG_INTEGER_MAX_LONG;
			}
			if ((type == Integer.class || type == int.class) && max.compareTo(BIG_INTEGER_MAX_INT) > 0) {
				max = BIG_INTEGER_MAX_INT;
			}
			if ((type == Short.class || type == short.class) && max.compareTo(BIG_INTEGER_MAX_SHORT) > 0) {
				max = BIG_INTEGER_MAX_SHORT;
			}
			if ((type == Byte.class || type == byte.class) && max.compareTo(BIG_INTEGER_MAX_BYTE) > 0) {
				max = BIG_INTEGER_MAX_BYTE;
			}
		}

		return new JavaxValidationIntegerConstraint(min, max);
	}

	public JavaxValidationDecimalConstraint generateDecimalConstraint(ArbitraryGeneratorContext context) {
		BigDecimal min = null;
		Boolean minInclusive = null;
		BigDecimal max = null;
		Boolean maxInclusive = null;
		Integer scale = null;

		Optional<Digits> digits = context.findAnnotation(Digits.class);
		if (digits.isPresent()) {
			BigDecimal value = BigDecimal.ONE;
			int integer = digits.get().integer();
			if (integer > 1) {
				value = BigDecimal.TEN.pow(integer - 1);
			}
			max = value.multiply(BigDecimal.TEN).subtract(BigDecimal.ONE);
			min = max.negate();
			scale = digits.get().fraction();
		}

		Optional<Min> minAnnotation = context.findAnnotation(Min.class);
		if (minAnnotation.isPresent()) {
			BigDecimal minValue = minAnnotation.map(Min::value).map(BigDecimal::valueOf).get();
			if (min == null) {
				min = minValue;
			} else if (min.compareTo(minValue) > 0) {
				min = minValue;
			}
		}

		Optional<DecimalMin> decimalMinAnnotation = context.findAnnotation(DecimalMin.class);
		if (decimalMinAnnotation.isPresent()) {
			BigDecimal decimalMin = decimalMinAnnotation
				.map(DecimalMin::value)
				.map(BigDecimal::new)
				.get();

			if (!decimalMinAnnotation.map(DecimalMin::inclusive).get()) {
				minInclusive = false;
			}

			if (min == null) {
				min = decimalMin;
			} else if (min.compareTo(decimalMin) < 0) {
				min = decimalMin;
			}
		}

		Optional<Max> maxAnnotation = context.findAnnotation(Max.class);
		if (maxAnnotation.isPresent()) {
			BigDecimal maxValue = maxAnnotation.map(Max::value).map(BigDecimal::valueOf).get();
			if (max == null) {
				max = maxValue;
			} else if (max.compareTo(maxValue) > 0) {
				max = maxValue;
			}
		}

		Optional<DecimalMax> decimalMaxAnnotation = context.findAnnotation(DecimalMax.class);
		if (decimalMaxAnnotation.isPresent()) {
			BigDecimal decimalMax = decimalMaxAnnotation
				.map(DecimalMax::value)
				.map(BigDecimal::new)
				.get();

			if (!decimalMaxAnnotation.map(DecimalMax::inclusive).get()) {
				maxInclusive = false;
			}

			if (max == null) {
				max = decimalMax;
			} else if (max.compareTo(decimalMax) > 0) {
				max = decimalMax;
			}
		}

		if (context.findAnnotation(Negative.class).isPresent()) {
			if (max == null || max.compareTo(BigDecimal.ZERO) > 0) {
				max = BigDecimal.ZERO;
				maxInclusive = false;
			}
		}

		if (context.findAnnotation(NegativeOrZero.class).isPresent()) {
			if (max == null || max.compareTo(BigDecimal.ZERO) > 0) {
				max = BigDecimal.ZERO;
			}
		}

		if (context.findAnnotation(Positive.class).isPresent()) {
			if (min == null || min.compareTo(BigDecimal.ZERO) < 0) {
				min = BigDecimal.ZERO;
				minInclusive = false;
			}
		}

		if (context.findAnnotation(PositiveOrZero.class).isPresent()) {
			if (min == null || min.compareTo(BigDecimal.ZERO) < 0) {
				min = BigDecimal.ZERO;
			}
		}

		if (min != null && minInclusive == null) {
			minInclusive = true;
		}

		if (max != null && maxInclusive == null) {
			maxInclusive = true;
		}

		return new JavaxValidationDecimalConstraint(min, minInclusive, max, maxInclusive, scale);
	}
}
