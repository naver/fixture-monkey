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

package com.navercorp.fixturemonkey.generator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Negative;
import javax.validation.constraints.NegativeOrZero;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Past;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.arbitraries.LongArbitrary;

import com.navercorp.fixturemonkey.TypeSupports;

final class AnnotatedGeneratorConstraints {
	public static AnnotatedGeneratorConstraint findConstraintByClass(
		Class<?> clazz,
		AnnotationSource annotationSource
	) {
		if (TypeSupports.isNumberType(clazz)) {
			return findNumberConstraint(clazz, annotationSource);
		} else if (String.class == clazz) {
			return findStringConstraint(clazz, annotationSource);
		} else if (TypeSupports.isDateType(clazz)) {
			return findDateConstraint(clazz, annotationSource);
		}

		return AnnotatedGeneratorConstraint.builder().build();
	}

	public static LongArbitrary generateDateMillisArbitrary(AnnotatedGeneratorConstraint constraint) {
		Instant now = Instant.now();
		long range = 365L;
		ChronoUnit unit = ChronoUnit.DAYS;

		LongArbitrary dateArbitrary = Arbitraries.longs()
			.between(
				now.minus(range, unit).toEpochMilli(),
				now.plus(range, unit).toEpochMilli()
			);

		BigDecimal min = constraint.getMin();
		BigDecimal max = constraint.getMax();

		if (min != null && max != null) {
			dateArbitrary = dateArbitrary.between(min.longValue(), max.longValue());
		} else if (max != null) {
			dateArbitrary = dateArbitrary.lessOrEqual(max.longValue());
		} else if (min != null) {
			dateArbitrary = dateArbitrary.greaterOrEqual(min.longValue());
		}

		return dateArbitrary;
	}

	private static AnnotatedGeneratorConstraint findNumberConstraint(
		Class<?> clazz,
		AnnotationSource annotationSource
	) {
		BigDecimal min = null;
		BigDecimal max = null;
		boolean minInclusive = true;
		boolean maxInclusive = true;

		Optional<Digits> digitsAnnotation = annotationSource.findAnnotation(Digits.class);
		if (digitsAnnotation.isPresent()) {
			BigDecimal value = BigDecimal.ONE;
			int integer = digitsAnnotation.get().integer();
			if (integer > 1) {
				value = BigDecimal.TEN.pow(integer - 1);
			}
			max = value.multiply(BigDecimal.TEN).subtract(BigDecimal.valueOf(1));
			min = max.negate();
		}

		Optional<Min> minAnnotation = annotationSource.findAnnotation(Min.class);
		if (minAnnotation.isPresent()) {
			BigDecimal minValue = minAnnotation.map(Min::value).map(BigDecimal::valueOf).get();
			if (min == null) {
				min = minValue;
			} else if (min.compareTo(minValue) > 0) {
				min = minValue;
			}
		}

		Optional<DecimalMin> decimalMinAnnotation = annotationSource.findAnnotation(DecimalMin.class);
		if (decimalMinAnnotation.isPresent()) {
			BigDecimal decimalMin = decimalMinAnnotation
				.map(DecimalMin::value)
				.map(BigDecimal::new)
				.get();
			if (min == null) {
				min = decimalMin;
			} else if (min.compareTo(decimalMin) < 0) {
				min = decimalMin;
			}
		}

		Optional<Max> maxAnnotation = annotationSource.findAnnotation(Max.class);
		if (maxAnnotation.isPresent()) {
			BigDecimal maxValue = maxAnnotation.map(Max::value).map(BigDecimal::valueOf).get();
			if (max == null) {
				max = maxValue;
			} else if (max.compareTo(maxValue) > 0) {
				max = maxValue;
			}
		}

		Optional<DecimalMax> decimalMaxAnnotation = annotationSource.findAnnotation(DecimalMax.class);
		if (decimalMaxAnnotation.isPresent()) {
			BigDecimal decimalMax = decimalMaxAnnotation
				.map(DecimalMax::value)
				.map(BigDecimal::new)
				.get();
			if (max == null) {
				max = decimalMax;
			} else if (max.compareTo(decimalMax) > 0) {
				max = decimalMax;
			}
		}

		if (annotationSource.findAnnotation(Negative.class).isPresent()) {
			if (max == null || max.compareTo(BigDecimal.valueOf(0)) > 0) {
				if (TypeSupports.isFloatType(clazz)) {
					max = BigDecimal.ZERO;
					maxInclusive = false;
				} else {
					max = BigDecimal.valueOf(-1);
				}
			}
		}

		if (annotationSource.findAnnotation(NegativeOrZero.class).isPresent()) {
			if (max == null || max.compareTo(BigDecimal.ZERO) > 0) {
				max = BigDecimal.ZERO;
			}
		}

		if (annotationSource.findAnnotation(Positive.class).isPresent()) {
			if (min == null || min.compareTo(BigDecimal.ZERO) < 0) {
				if (TypeSupports.isFloatType(clazz)) {
					min = BigDecimal.ZERO;
					minInclusive = false;
				} else {
					min = BigDecimal.ONE;
				}
			}
		}

		if (annotationSource.findAnnotation(PositiveOrZero.class).isPresent()) {
			if (min == null || min.compareTo(BigDecimal.ZERO) < 0) {
				min = BigDecimal.ZERO;
			}
		}
		return AnnotatedGeneratorConstraint.builder()
			.min(min)
			.max(max)
			.maxInclusive(maxInclusive)
			.minInclusive(minInclusive)
			.build();
	}

	private static AnnotatedGeneratorConstraint findStringConstraint(
		Class<?> clazz,
		AnnotationSource annotationSource
	) {
		BigDecimal min = null;
		BigDecimal max = null;
		if (annotationSource.findAnnotation(NotBlank.class).isPresent()
			|| annotationSource.findAnnotation(NotEmpty.class).isPresent()) {
			min = BigDecimal.ONE;
		}

		Optional<Size> size = annotationSource.findAnnotation(Size.class);
		if (size.isPresent()) {
			BigDecimal minValue = BigDecimal.valueOf(size.map(Size::min).get());
			if (min == null) {
				min = minValue;
			} else if (min.compareTo(minValue) < 0) {
				min = minValue;
			}

			max = BigDecimal.valueOf(size.map(Size::max).get());
		}
		return AnnotatedGeneratorConstraint.builder()
			.max(max)
			.min(min)
			.build();
	}

	private static AnnotatedGeneratorConstraint findDateConstraint(
		Class<?> clazz,
		AnnotationSource annotationSource
	) {
		BigDecimal now = BigDecimal.valueOf(Instant.now().toEpochMilli());
		BigDecimal min = null;
		BigDecimal max = null;

		if (annotationSource.findAnnotation(Past.class).isPresent()) {
			max = now.subtract(BigDecimal.valueOf(1000));
		}

		if (annotationSource.findAnnotation(PastOrPresent.class).isPresent()) {
			if (max == null) {
				max = now;
			} else if (max.compareTo(now) > 0) {
				max = now;
			}
		}

		if (annotationSource.findAnnotation(Future.class).isPresent()) {
			min = now.add(BigDecimal.valueOf(2000));   // 1000 is buffer
		}

		if (annotationSource.findAnnotation(FutureOrPresent.class).isPresent()) {
			if (min == null) {
				min = now.add(BigDecimal.valueOf(2000));    // 1000 is buffer
			} else if (min.compareTo(now) > 0) {
				min = now.add(BigDecimal.valueOf(2000));  // 1000 is buffer
			}
		}

		return AnnotatedGeneratorConstraint.builder()
			.min(min)
			.max(max)
			.build();
	}
}
