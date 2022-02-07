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

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public class JavaxValidationConstraintGenerator {

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

		return new JavaxValidationIntegerConstraint(min, max);
	}
}
