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

package com.navercorp.fixturemonkey.jakarta.validation.introspector;

import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

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

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.introspector.JavaArbitraryResolver;
import com.navercorp.fixturemonkey.api.matcher.AnnotationPackageNameMatcher;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.property.Property;

@API(since = "0.4.10", status = Status.MAINTAINED)
public final class JakartaValidationJavaArbitraryResolver implements JavaArbitraryResolver, Matcher {
	private static final Matcher MATCHER = new AnnotationPackageNameMatcher("jakarta.validation.constraints");
	private static final RegexGenerator REGEX_GENERATOR = new RegexGenerator();

	private final JakartaValidationConstraintGenerator constraintGenerator;

	public JakartaValidationJavaArbitraryResolver() {
		this(new JakartaValidationConstraintGenerator());
	}

	public JakartaValidationJavaArbitraryResolver(JakartaValidationConstraintGenerator constraintGenerator) {
		this.constraintGenerator = constraintGenerator;
	}

	@Override
	public boolean match(Property property) {
		return MATCHER.match(property);
	}

	@Override
	public Arbitrary<String> strings(
		StringArbitrary stringArbitrary,
		ArbitraryGeneratorContext context
	) {
		JakartaValidationStringConstraint constraint = this.constraintGenerator.generateStringConstraint(context);
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
					.filter(it -> it != null && !it.trim().isEmpty())
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
					if (it == null) {
						return true;
					} else {
						return !containsControlCharacters(it);
					}
				}

				if (it == null || it.trim().isEmpty()) {
					return false;
				}

				return !containsControlCharacters(it);
			});
	}

	private static boolean containsControlCharacters(String value) {
		for (char c : value.toCharArray()) {
			if (Character.isISOControl(c)) {
				return true;
			}
		}

		return false;
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
		JakartaValidationIntegerConstraint constraint = this.constraintGenerator.generateIntegerConstraint(context);
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
		JakartaValidationIntegerConstraint constraint = this.constraintGenerator.generateIntegerConstraint(context);
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
	public Arbitrary<Float> floats(
		FloatArbitrary floatArbitrary,
		ArbitraryGeneratorContext context
	) {
		JakartaValidationDecimalConstraint constraint = this.constraintGenerator.generateDecimalConstraint(context);
		BigDecimal min = constraint.getMin();
		BigDecimal max = constraint.getMax();
		Boolean minInclusive = constraint.getMinInclusive();
		Boolean maxInclusive = constraint.getMaxInclusive();

		if (min != null) {
			if (minInclusive != null && !minInclusive) {
				floatArbitrary = floatArbitrary.greaterThan(min.floatValue());
			} else {
				floatArbitrary = floatArbitrary.greaterOrEqual(min.floatValue());
			}
		}
		if (max != null) {
			if (maxInclusive != null && !maxInclusive) {
				floatArbitrary = floatArbitrary.lessThan(max.floatValue());
			} else {
				floatArbitrary = floatArbitrary.lessOrEqual(max.floatValue());
			}
		}

		return floatArbitrary;
	}

	@Override
	public Arbitrary<Double> doubles(
		DoubleArbitrary doubleArbitrary,
		ArbitraryGeneratorContext context
	) {
		JakartaValidationDecimalConstraint constraint = this.constraintGenerator.generateDecimalConstraint(context);
		BigDecimal min = constraint.getMin();
		BigDecimal max = constraint.getMax();
		Boolean minInclusive = constraint.getMinInclusive();
		Boolean maxInclusive = constraint.getMaxInclusive();

		if (min != null) {
			if (minInclusive != null && !minInclusive) {
				doubleArbitrary = doubleArbitrary.greaterThan(min.doubleValue());
			} else {
				doubleArbitrary = doubleArbitrary.greaterOrEqual(min.doubleValue());
			}
		}
		if (max != null) {
			if (maxInclusive != null && !maxInclusive) {
				doubleArbitrary = doubleArbitrary.lessThan(max.doubleValue());
			} else {
				doubleArbitrary = doubleArbitrary.lessOrEqual(max.doubleValue());
			}
		}

		return doubleArbitrary;
	}

	@Override
	public Arbitrary<Integer> integers(
		IntegerArbitrary integerArbitrary,
		ArbitraryGeneratorContext context
	) {
		JakartaValidationIntegerConstraint constraint = this.constraintGenerator.generateIntegerConstraint(context);
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
		JakartaValidationIntegerConstraint constraint = this.constraintGenerator.generateIntegerConstraint(context);
		BigInteger min = constraint.getMin();
		BigInteger max = constraint.getMax();

		if (min != null) {
			longArbitrary = longArbitrary.greaterOrEqual(min.longValueExact());
		}
		if (max != null) {
			longArbitrary = longArbitrary.lessOrEqual(max.longValueExact());
		}

		return longArbitrary;
	}

	@Override
	public Arbitrary<BigInteger> bigIntegers(
		BigIntegerArbitrary bigIntegerArbitrary,
		ArbitraryGeneratorContext context
	) {
		JakartaValidationIntegerConstraint constraint = this.constraintGenerator.generateIntegerConstraint(context);
		BigInteger min = constraint.getMin();
		BigInteger max = constraint.getMax();

		if (min != null) {
			bigIntegerArbitrary = bigIntegerArbitrary.greaterOrEqual(min);
		}
		if (max != null) {
			bigIntegerArbitrary = bigIntegerArbitrary.lessOrEqual(max);
		}

		return bigIntegerArbitrary;
	}

	@Override
	public Arbitrary<BigDecimal> bigDecimals(
		BigDecimalArbitrary bigDecimalArbitrary,
		ArbitraryGeneratorContext context
	) {
		JakartaValidationDecimalConstraint constraint = this.constraintGenerator.generateDecimalConstraint(context);
		BigDecimal min = constraint.getMin();
		BigDecimal max = constraint.getMax();
		Boolean minInclusive = constraint.getMinInclusive();
		Boolean maxInclusive = constraint.getMaxInclusive();
		Integer scale = constraint.getScale();

		if (min != null) {
			if (minInclusive != null && !minInclusive) {
				bigDecimalArbitrary = bigDecimalArbitrary.greaterThan(min);
			} else {
				bigDecimalArbitrary = bigDecimalArbitrary.greaterOrEqual(min);
			}
		}
		if (max != null) {
			if (maxInclusive != null && !maxInclusive) {
				bigDecimalArbitrary = bigDecimalArbitrary.lessThan(max);
			} else {
				bigDecimalArbitrary = bigDecimalArbitrary.lessOrEqual(max);
			}
		}

		if (scale != null) {
			bigDecimalArbitrary = bigDecimalArbitrary.ofScale(scale);
		}

		return bigDecimalArbitrary;
	}
}
