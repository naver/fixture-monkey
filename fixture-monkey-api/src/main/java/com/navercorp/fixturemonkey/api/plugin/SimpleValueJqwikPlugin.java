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

package com.navercorp.fixturemonkey.api.plugin;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.arbitraries.StringArbitrary;

import com.navercorp.fixturemonkey.api.arbitrary.MonkeyStringArbitrary;
import com.navercorp.fixturemonkey.api.constraint.CompositeJavaConstraintGenerator;
import com.navercorp.fixturemonkey.api.constraint.JavaConstraintGenerator;
import com.navercorp.fixturemonkey.api.constraint.JavaContainerConstraint;
import com.navercorp.fixturemonkey.api.constraint.JavaDateTimeConstraint;
import com.navercorp.fixturemonkey.api.constraint.JavaDecimalConstraint;
import com.navercorp.fixturemonkey.api.constraint.JavaIntegerConstraint;
import com.navercorp.fixturemonkey.api.constraint.JavaStringConstraint;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.jqwik.JavaTypeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.jqwik.JqwikJavaArbitraryResolver;
import com.navercorp.fixturemonkey.api.jqwik.JqwikJavaTypeArbitraryGeneratorSet;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptionsBuilder;

@API(since = "1.0.17", status = Status.EXPERIMENTAL)
public final class SimpleValueJqwikPlugin implements Plugin {
	private static final Set<Character> ALLOWED_SPECIAL_UNRESERVED_CHARACTERS =
		new HashSet<>(Arrays.asList('.', '_', '-', '~'));
	private static final long DEFAULT_MIN_STRING_LENGTH = 0L;
	private static final long DEFAULT_MAX_STRING_LENGTH = 5L;
	private static final long DEFAULT_MIN_NUMBER_VALUE = -10000L;
	private static final long DEFAULT_MAX_NUMBER_VALUE = 10000L;
	private static final int DEFAULT_MIN_CONTAINER_SIZE = 0;
	private static final int DEFAULT_MAX_CONTAINER_SIZE = 3;
	private static final long DEFAULT_MINUS_DAYS = 365;
	private static final long DEFAULT_PLUS_DAYS = 365;
	private static final Predicate<Character> DEFAULT_CHARACTER_PREDICATE = character -> {
		boolean isAlphabet = 48 <= character && character <= 57;
		boolean isNumeric = 65 <= character && character <= 122;

		return isAlphabet
			|| isNumeric
			|| ALLOWED_SPECIAL_UNRESERVED_CHARACTERS.contains(character);
	};

	private long minStringLength = DEFAULT_MIN_STRING_LENGTH;
	private long maxStringLength = DEFAULT_MAX_STRING_LENGTH;
	private long positiveMinNumberValue = 0;
	private long positiveMaxNumberValue = DEFAULT_MAX_NUMBER_VALUE;
	private long negativeMinNumberValue = DEFAULT_MIN_NUMBER_VALUE;
	private long negativeMaxNumberValue = -0;
	private int minContainerSize = DEFAULT_MIN_CONTAINER_SIZE;
	private int maxContainerSize = DEFAULT_MAX_CONTAINER_SIZE;
	private long minusDaysFromToday = DEFAULT_MINUS_DAYS;
	private long plusDaysFromToday = DEFAULT_PLUS_DAYS;
	private Predicate<Character> characterPredicate = DEFAULT_CHARACTER_PREDICATE;

	public SimpleValueJqwikPlugin minStringLength(long minStringLength) {
		this.minStringLength = minStringLength;
		return this;
	}

	public SimpleValueJqwikPlugin maxStringLength(long maxStringLength) {
		this.maxStringLength = maxStringLength;
		return this;
	}

	public SimpleValueJqwikPlugin minNumberValue(long minNumberValue) {
		if (minNumberValue < 0) {
			this.negativeMinNumberValue = minNumberValue;
			return this;
		}
		this.positiveMinNumberValue = minNumberValue;
		return this;
	}

	public SimpleValueJqwikPlugin maxNumberValue(long maxNumberValue) {
		if (maxNumberValue < 0) {
			this.negativeMaxNumberValue = maxNumberValue;
			return this;
		}
		this.positiveMaxNumberValue = maxNumberValue;
		return this;
	}

	public SimpleValueJqwikPlugin minContainerSize(int minContainerSize) {
		this.minContainerSize = minContainerSize;
		return this;
	}

	public SimpleValueJqwikPlugin maxContainerSize(int maxContainerSize) {
		this.maxContainerSize = maxContainerSize;
		return this;
	}

	public SimpleValueJqwikPlugin minusDaysFromToday(long minusDaysFromToday) {
		this.minusDaysFromToday = minusDaysFromToday;
		return this;
	}

	public SimpleValueJqwikPlugin plusDaysFromToday(long plusDaysFromToday) {
		this.plusDaysFromToday = plusDaysFromToday;
		return this;
	}

	public SimpleValueJqwikPlugin characterPredicate(Predicate<Character> characterPredicate) {
		this.characterPredicate = characterPredicate;
		return this;
	}

	@Override
	public void accept(FixtureMonkeyOptionsBuilder optionsBuilder) {
		optionsBuilder.insertFirstJavaConstraintGeneratorCustomizer(
				javaConstraintGenerator ->
					new CompositeJavaConstraintGenerator(
						Arrays.asList(
							javaConstraintGenerator,
							new SimpleJavaConstraintGenerator(
								this.minStringLength,
								this.maxStringLength,
								this.positiveMinNumberValue,
								this.positiveMaxNumberValue,
								this.negativeMinNumberValue,
								this.negativeMaxNumberValue,
								this.minContainerSize,
								this.maxContainerSize,
								this.minusDaysFromToday,
								this.plusDaysFromToday
							)
						)
					)
			)
			.javaTypeArbitraryGeneratorSet(
				javaConstraintGenerator -> new JqwikJavaTypeArbitraryGeneratorSet(
					new JavaTypeArbitraryGenerator() {
						@Override
						public StringArbitrary strings() {
							return new MonkeyStringArbitrary()
								.filterCharacter(characterPredicate);
						}
					},
					new JqwikJavaArbitraryResolver(javaConstraintGenerator)
				)
			);
	}

	private static class SimpleJavaConstraintGenerator implements JavaConstraintGenerator {
		private final long stringMinLength;
		private final long stringMaxLength;
		private final long positiveMinNumberValue;
		private final long positiveMaxNumberValue;
		private final long negativeMinNumberValue;
		private final long negativeMaxNumberValue;
		private final int minContainerSize;
		private final int maxContainerSize;
		private final long minusDaysFromToday;
		private final long plusDaysFromToday;

		public SimpleJavaConstraintGenerator(
			long stringMinLength,
			long stringMaxLength,
			long positiveMinNumberValue,
			long positiveMaxNumberValue,
			long negativeMinNumberValue,
			long negativeMaxNumberValue,
			int minContainerSize,
			int maxContainerSize,
			long minusDaysFromToday,
			long plusDaysFromToday
		) {
			this.stringMinLength = stringMinLength;
			this.stringMaxLength = stringMaxLength;
			this.positiveMinNumberValue = positiveMinNumberValue;
			this.positiveMaxNumberValue = positiveMaxNumberValue;
			this.negativeMinNumberValue = negativeMinNumberValue;
			this.negativeMaxNumberValue = negativeMaxNumberValue;
			this.minContainerSize = minContainerSize;
			this.maxContainerSize = maxContainerSize;
			this.minusDaysFromToday = minusDaysFromToday;
			this.plusDaysFromToday = plusDaysFromToday;
		}

		@Override
		public JavaStringConstraint generateStringConstraint(ArbitraryGeneratorContext context) {
			return new JavaStringConstraint(
				BigInteger.valueOf(this.stringMinLength),
				BigInteger.valueOf(this.stringMaxLength),
				false,
				false,
				false,
				null,
				false
			);
		}

		@Override
		public JavaIntegerConstraint generateIntegerConstraint(ArbitraryGeneratorContext context) {
			BigInteger positiveMin = BigInteger.valueOf(this.positiveMinNumberValue);
			BigInteger positiveMax = BigInteger.valueOf(this.positiveMaxNumberValue);
			BigInteger negativeMin = BigInteger.valueOf(this.negativeMinNumberValue);
			BigInteger negativeMax = BigInteger.valueOf(this.negativeMaxNumberValue);

			Type type = context.getResolvedType();

			if (type == Byte.class || type == byte.class) {
				positiveMax = positiveMax.min(BIG_INTEGER_MAX_BYTE);
				negativeMin = negativeMin.max(BIG_INTEGER_MIN_BYTE);
			}

			return new JavaIntegerConstraint(positiveMin, positiveMax, negativeMin, negativeMax);
		}

		@Override
		public JavaDecimalConstraint generateDecimalConstraint(ArbitraryGeneratorContext context) {
			return new JavaDecimalConstraint(
				BigDecimal.valueOf(this.positiveMinNumberValue),
				true,
				BigDecimal.valueOf(this.positiveMaxNumberValue),
				true,
				BigDecimal.valueOf(this.negativeMinNumberValue),
				true,
				BigDecimal.valueOf(this.negativeMaxNumberValue),
				true,
				2
			);
		}

		@Override
		public JavaContainerConstraint generateContainerConstraint(ArbitraryGeneratorContext context) {
			return new JavaContainerConstraint(
				this.minContainerSize,
				this.maxContainerSize,
				false
			);
		}

		@Override
		public JavaDateTimeConstraint generateDateTimeConstraint(ArbitraryGeneratorContext context) {
			return new JavaDateTimeConstraint(
				() -> LocalDateTime.now().minusDays(this.minusDaysFromToday),
				() -> LocalDateTime.now().plusDays(this.plusDaysFromToday)
			);
		}
	}
}
