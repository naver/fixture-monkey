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
import java.time.LocalDateTime;
import java.util.Optional;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Email;
import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Negative;
import javax.validation.constraints.NegativeOrZero;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.constraint.JavaConstraintGenerator;
import com.navercorp.fixturemonkey.api.constraint.JavaContainerConstraint;
import com.navercorp.fixturemonkey.api.constraint.JavaDateTimeConstraint;
import com.navercorp.fixturemonkey.api.constraint.JavaDecimalConstraint;
import com.navercorp.fixturemonkey.api.constraint.JavaIntegerConstraint;
import com.navercorp.fixturemonkey.api.constraint.JavaStringConstraint;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class JavaxValidationConstraintGenerator implements JavaConstraintGenerator {
	public JavaStringConstraint generateStringConstraint(ArbitraryGeneratorContext context) {
		BigInteger min = null;
		BigInteger max = null;
		boolean digits = false;
		boolean notNull = context.findAnnotation(NotNull.class).isPresent();
		boolean notBlank = context.findAnnotation(NotBlank.class).isPresent();
		String pattern = context.findAnnotation(Pattern.class)
			.map(Pattern::regexp).orElse(null);
		boolean email = context.findAnnotation(Email.class).isPresent();

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

		return new JavaStringConstraint(min, max, digits, notNull, notBlank, pattern, email);
	}

	public JavaIntegerConstraint generateIntegerConstraint(ArbitraryGeneratorContext context) {
		BigInteger positiveMin = null;
		BigInteger positiveMax = null;
		BigInteger negativeMax = null;
		BigInteger negativeMin = null;

		Optional<Digits> digits = context.findAnnotation(Digits.class);
		if (digits.isPresent()) {
			BigInteger value = BigInteger.ONE;
			int integer = digits.get().integer();
			if (integer > 1) {
				value = BigInteger.TEN.pow(integer - 1);
			}
			positiveMax = value.multiply(BigInteger.TEN).subtract(BigInteger.ONE);
			positiveMin = value;
			negativeMax = positiveMin.negate();
			negativeMin = positiveMax.negate();
		}

		Optional<Min> minAnnotation = context.findAnnotation(Min.class);
		if (minAnnotation.isPresent()) {
			BigInteger minValue = minAnnotation.map(Min::value).map(BigInteger::valueOf).get();
			if (minValue.compareTo(BigInteger.ZERO) > 0) {
				if (positiveMin == null) {
					positiveMin = minValue;
				} else {
					positiveMin = positiveMin.min(minValue);
				}
			} else {
				if (negativeMin == null) {
					negativeMin = minValue;
				} else {
					negativeMin = negativeMin.min(minValue);
				}
			}
		}

		Optional<DecimalMin> decimalMinAnnotation = context.findAnnotation(DecimalMin.class);
		if (decimalMinAnnotation.isPresent()) {
			BigInteger decimalMin = new BigInteger(
				decimalMinAnnotation
					.get()
					.value()
			);

			if (!decimalMinAnnotation.map(DecimalMin::inclusive).get()) {
				decimalMin = decimalMin.add(BigInteger.ONE);
			}

			if (decimalMin.compareTo(BigInteger.ZERO) > 0) {
				if (positiveMin == null) {
					positiveMin = decimalMin;
				} else {
					positiveMin = positiveMin.min(decimalMin);
				}
			} else {
				if (negativeMin == null) {
					negativeMin = decimalMin;
				} else {
					negativeMin = negativeMin.min(decimalMin);
				}
			}
		}

		Optional<Max> maxAnnotation = context.findAnnotation(Max.class);
		if (maxAnnotation.isPresent()) {
			BigInteger maxValue = maxAnnotation.map(Max::value).map(BigInteger::valueOf).get();
			if (maxValue.compareTo(BigInteger.ZERO) > 0) {
				if (positiveMax == null) {
					positiveMax = maxValue;
				} else {
					positiveMax = positiveMax.max(maxValue);
				}
			} else {
				if (negativeMax == null) {
					negativeMax = maxValue;
				} else {
					negativeMax = negativeMax.max(maxValue);
				}
			}
		}

		Optional<DecimalMax> decimalMaxAnnotation = context.findAnnotation(DecimalMax.class);
		if (decimalMaxAnnotation.isPresent()) {
			BigInteger decimalMax = new BigInteger(
				decimalMaxAnnotation
					.get()
					.value()
			);

			if (!decimalMaxAnnotation.map(DecimalMax::inclusive).get()) {
				decimalMax = decimalMax.subtract(BigInteger.ONE);
			}

			if (decimalMax.compareTo(BigInteger.ZERO) > 0) {
				if (positiveMax == null) {
					positiveMax = decimalMax;
				} else {
					positiveMax = positiveMax.max(decimalMax);
				}
			} else {
				if (negativeMax == null) {
					negativeMax = decimalMax;
				} else {
					negativeMax = negativeMax.max(decimalMax);
				}
			}
		}

		if (context.findAnnotation(Negative.class).isPresent()) {
			if (negativeMax == null || negativeMax.compareTo(BigInteger.ZERO) > 0) {
				negativeMax = BigInteger.valueOf(-1);
			}
		}

		if (context.findAnnotation(NegativeOrZero.class).isPresent()) {
			if (negativeMax == null || negativeMax.compareTo(BigInteger.ZERO) > 0) {
				negativeMax = BigInteger.ZERO;
			}
		}

		if (context.findAnnotation(Positive.class).isPresent()) {
			if (positiveMin == null || positiveMin.compareTo(BigInteger.ZERO) < 0) {
				positiveMin = BigInteger.ONE;
			}
		}

		if (context.findAnnotation(PositiveOrZero.class).isPresent()) {
			if (positiveMin == null || positiveMin.compareTo(BigInteger.ZERO) < 0) {
				positiveMin = BigInteger.ZERO;
			}
		}

		Type type = context.getResolvedType();
		if (negativeMin != null) {
			if ((type == Long.class || type == long.class) && negativeMin.compareTo(BIG_INTEGER_MIN_LONG) < 0) {
				negativeMin = BIG_INTEGER_MIN_LONG;
			}
			if ((type == Integer.class || type == int.class) && negativeMin.compareTo(BIG_INTEGER_MIN_INT) < 0) {
				negativeMin = BIG_INTEGER_MIN_INT;
			}
			if ((type == Short.class || type == short.class) && negativeMin.compareTo(BIG_INTEGER_MIN_SHORT) < 0) {
				negativeMin = BIG_INTEGER_MIN_SHORT;
			}
			if ((type == Byte.class || type == byte.class) && negativeMin.compareTo(BIG_INTEGER_MIN_BYTE) < 0) {
				negativeMin = BIG_INTEGER_MIN_BYTE;
			}
		}
		if (positiveMax != null) {
			if ((type == Long.class || type == long.class) && positiveMax.compareTo(BIG_INTEGER_MAX_LONG) > 0) {
				positiveMax = BIG_INTEGER_MAX_LONG;
			}
			if ((type == Integer.class || type == int.class) && positiveMax.compareTo(BIG_INTEGER_MAX_INT) > 0) {
				positiveMax = BIG_INTEGER_MAX_INT;
			}
			if ((type == Short.class || type == short.class) && positiveMax.compareTo(BIG_INTEGER_MAX_SHORT) > 0) {
				positiveMax = BIG_INTEGER_MAX_SHORT;
			}
			if ((type == Byte.class || type == byte.class) && positiveMax.compareTo(BIG_INTEGER_MAX_BYTE) > 0) {
				positiveMax = BIG_INTEGER_MAX_BYTE;
			}
		}

		return new JavaIntegerConstraint(positiveMin, positiveMax, negativeMin, negativeMax);
	}

	public JavaDecimalConstraint generateDecimalConstraint(ArbitraryGeneratorContext context) {
		BigDecimal positiveMin = null;
		Boolean positiveMinInclusive = null;
		BigDecimal positiveMax = null;
		Boolean positiveMaxInclusive = null;
		BigDecimal negativeMin = null;
		Boolean negativeMinInclusive = null;
		BigDecimal negativeMax = null;
		boolean negativeMaxInclusive = false;
		Integer scale = null;

		Optional<Digits> digits = context.findAnnotation(Digits.class);
		if (digits.isPresent()) {
			BigDecimal value = BigDecimal.ONE;
			int integer = digits.get().integer();
			if (integer > 1) {
				value = BigDecimal.TEN.pow(integer - 1);
			}
			positiveMax = value.multiply(BigDecimal.TEN).subtract(BigDecimal.ONE);
			positiveMin = value;
			negativeMax = positiveMin.negate();
			negativeMin = positiveMax.negate();
			positiveMinInclusive = false;
			negativeMinInclusive = false;
			scale = digits.get().fraction();
		}

		Optional<Min> minAnnotation = context.findAnnotation(Min.class);
		if (minAnnotation.isPresent()) {
			BigDecimal minValue = minAnnotation.map(Min::value).map(BigDecimal::valueOf).get();
			if (minValue.compareTo(BigDecimal.ZERO) > 0) {
				if (positiveMin == null) {
					positiveMin = minValue;
				} else {
					positiveMin = positiveMin.min(minValue);
				}
				negativeMax = null;
				negativeMin = null;
			} else {
				if (negativeMin == null) {
					negativeMin = minValue;
				} else {
					negativeMin = negativeMin.min(minValue);
				}
				negativeMinInclusive = true;
			}
		}

		Optional<DecimalMin> decimalMinAnnotation = context.findAnnotation(DecimalMin.class);
		if (decimalMinAnnotation.isPresent()) {
			BigDecimal decimalMin = new BigDecimal(
				decimalMinAnnotation
					.get()
					.value()
			);

			if (decimalMin.compareTo(BigDecimal.ZERO) > 0) {
				if (positiveMin == null) {
					positiveMin = decimalMin;
				} else {
					positiveMin = positiveMin.min(decimalMin);
				}
				if (!decimalMinAnnotation.map(DecimalMin::inclusive).get()) {
					positiveMinInclusive = false;
				}
				negativeMax = null;
				negativeMin = null;
			} else {
				if (negativeMin == null) {
					negativeMin = decimalMin;
				} else {
					negativeMin = negativeMin.min(negativeMin);
				}
				if (!decimalMinAnnotation.map(DecimalMin::inclusive).get()) {
					negativeMinInclusive = false;
				}
			}
		}

		Optional<Max> maxAnnotation = context.findAnnotation(Max.class);
		if (maxAnnotation.isPresent()) {
			BigDecimal maxValue = maxAnnotation.map(Max::value).map(BigDecimal::valueOf).get();
			if (maxValue.compareTo(BigDecimal.ZERO) > 0) {
				if (positiveMax == null) {
					positiveMax = maxValue;
				} else {
					positiveMax = positiveMax.max(maxValue);
				}
			} else {
				if (negativeMax == null) {
					negativeMax = maxValue;
				} else {
					negativeMax = negativeMax.max(maxValue);
				}
			}
		}

		Optional<DecimalMax> decimalMaxAnnotation = context.findAnnotation(DecimalMax.class);
		if (decimalMaxAnnotation.isPresent()) {
			BigDecimal decimalMax = new BigDecimal(
				decimalMaxAnnotation
					.get()
					.value()
			);

			if (decimalMax.compareTo(BigDecimal.ZERO) > 0) {
				if (positiveMax == null) {
					positiveMax = decimalMax;
				} else {
					positiveMax = positiveMax.max(decimalMax);
				}
				positiveMaxInclusive = decimalMaxAnnotation.map(DecimalMax::inclusive).get();
			} else {
				if (negativeMax == null) {
					negativeMax = decimalMax;
				} else {
					negativeMax = negativeMax.max(decimalMax);
				}
				negativeMaxInclusive = decimalMaxAnnotation.map(DecimalMax::inclusive).get();
			}

			if (!decimalMaxAnnotation.map(DecimalMax::inclusive).get()) {
				positiveMaxInclusive = false;
			}

			if (positiveMax == null) {
				positiveMax = decimalMax;
			} else if (positiveMax.compareTo(decimalMax) > 0) {
				positiveMax = decimalMax;
			}
		}

		if (context.findAnnotation(Negative.class).isPresent()) {
			if (negativeMax == null || negativeMax.compareTo(BigDecimal.ZERO) > 0) {
				negativeMax = BigDecimal.ZERO;
				negativeMaxInclusive = false;
			}
		}

		if (context.findAnnotation(NegativeOrZero.class).isPresent()) {
			if (negativeMax == null || negativeMax.compareTo(BigDecimal.ZERO) > 0) {
				negativeMax = BigDecimal.ZERO;
				negativeMaxInclusive = true;
			}
		}

		if (context.findAnnotation(Positive.class).isPresent()) {
			if (positiveMin == null || positiveMin.compareTo(BigDecimal.ZERO) < 0) {
				positiveMin = BigDecimal.ZERO;
				positiveMinInclusive = false;
			}
		}

		if (context.findAnnotation(PositiveOrZero.class).isPresent()) {
			if (positiveMin == null || positiveMin.compareTo(BigDecimal.ZERO) < 0) {
				positiveMin = BigDecimal.ZERO;
				positiveMinInclusive = true;
			}
		}

		return new JavaDecimalConstraint(
			positiveMin,
			positiveMinInclusive,
			positiveMax,
			positiveMaxInclusive,
			negativeMin,
			negativeMinInclusive,
			negativeMax,
			negativeMaxInclusive,
			scale
		);
	}

	@Override
	public JavaContainerConstraint generateContainerConstraint(ArbitraryGeneratorContext context) {
		Integer minSize = null;
		Integer maxSize = null;
		boolean notEmpty = context.findAnnotation(NotEmpty.class).isPresent();

		Optional<Size> sizeAnnotation = context.findAnnotation(Size.class);
		if (sizeAnnotation.isPresent()) {
			Size size = sizeAnnotation.get();
			minSize = size.min();
			maxSize = size.max();
		}

		return new JavaContainerConstraint(
			minSize,
			maxSize,
			notEmpty
		);
	}

	public JavaDateTimeConstraint generateDateTimeConstraint(ArbitraryGeneratorContext context) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime min = null;
		if (context.findAnnotation(Future.class).isPresent()) {
			min = now.plusSeconds(3);    // 3000 is buffer for future time
		} else if (context.findAnnotation(FutureOrPresent.class).isPresent()) {
			min = now.plusSeconds(2);    // 2000 is buffer for future time
		}

		LocalDateTime max = null;
		if (context.findAnnotation(Past.class).isPresent()) {
			max = now.minusSeconds(1);
		} else if (context.findAnnotation(PastOrPresent.class).isPresent()) {
			max = now;
		}

		return new JavaDateTimeConstraint(min, max);
	}
}
