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
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.jqwik.JavaTypeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.jqwik.JqwikJavaArbitraryResolver;
import com.navercorp.fixturemonkey.api.jqwik.JqwikJavaTypeArbitraryGeneratorSet;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptionsBuilder;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "1.0.17", status = Status.EXPERIMENTAL)
public final class SimpleValueJqwikPlugin implements Plugin {
	private static final Set<Character> ALLOWED_SPECIAL_UNRESERVED_CHARACTERS =
		new HashSet<>(Arrays.asList('.', '_', '-', '~'));
	private static final long DEFAULT_MIN_STRING_LENGTH = 0L;
	private static final long DEFAULT_MAX_STRING_LENGTH = 5L;
	private static final long DEFAULT_STRING_BUFFER_LENGTH = 3L;
	private static final long DEFAULT_MIN_NUMBER_VALUE = -10000L;
	private static final long DEFAULT_MAX_NUMBER_VALUE = 10000L;
	private static final long DEFAULT_NUMBER_BUFFER_VALUE = 100L;
	private static final int DEFAULT_MIN_CONTAINER_SIZE = 0;
	private static final int DEFAULT_MAX_CONTAINER_SIZE = 3;
	private static final int DEFAULT_CONTAINER_BUFFER_SIZE = 2;
	private static final long DEFAULT_MINUS_DAYS = 365;
	private static final long DEFAULT_PLUS_DAYS = 365;
	private static final long DEFAULT_DAYS_BUFFER = 30L;
	private static final Predicate<Character> DEFAULT_CHARACTER_PREDICATE = character -> {
		boolean isAlphabet = 48 <= character && character <= 57;
		boolean isNumeric = 65 <= character && character <= 122;

		return isAlphabet
			|| isNumeric
			|| ALLOWED_SPECIAL_UNRESERVED_CHARACTERS.contains(character);
	};

	private long minStringLength = DEFAULT_MIN_STRING_LENGTH;
	private long maxStringLength = DEFAULT_MAX_STRING_LENGTH;
	private boolean setMaxStringLength = false;
	private long minNumberValue = DEFAULT_MIN_NUMBER_VALUE;
	private long maxNumberValue = DEFAULT_MAX_NUMBER_VALUE;
	private boolean setMaxNumberValue = false;
	private int minContainerSize = DEFAULT_MIN_CONTAINER_SIZE;
	private int maxContainerSize = DEFAULT_MAX_CONTAINER_SIZE;
	private boolean setMaxContainerSize = false;
	private long minusDaysFromToday = DEFAULT_MINUS_DAYS;
	private long plusDaysFromToday = DEFAULT_PLUS_DAYS;
	private boolean setPlusDaysFromToday = false;
	private Predicate<Character> characterPredicate = DEFAULT_CHARACTER_PREDICATE;

	public SimpleValueJqwikPlugin minStringLength(long minStringLength) {
		this.minStringLength = minStringLength;
		return this;
	}

	public SimpleValueJqwikPlugin maxStringLength(long maxStringLength) {
		this.maxStringLength = maxStringLength;
		this.setMaxStringLength = true;
		return this;
	}

	public SimpleValueJqwikPlugin minNumberValue(long minNumberValue) {
		this.minNumberValue = minNumberValue;
		return this;
	}

	public SimpleValueJqwikPlugin maxNumberValue(long maxNumberValue) {
		this.maxNumberValue = maxNumberValue;
		this.setMaxNumberValue = true;
		return this;
	}

	public SimpleValueJqwikPlugin minContainerSize(int minContainerSize) {
		this.minContainerSize = minContainerSize;
		return this;
	}

	public SimpleValueJqwikPlugin maxContainerSize(int maxContainerSize) {
		this.maxContainerSize = maxContainerSize;
		this.setMaxContainerSize = true;
		return this;
	}

	public SimpleValueJqwikPlugin minusDaysFromToday(long minusDaysFromToday) {
		this.minusDaysFromToday = minusDaysFromToday;
		return this;
	}

	public SimpleValueJqwikPlugin plusDaysFromToday(long plusDaysFromToday) {
		this.plusDaysFromToday = plusDaysFromToday;
		this.setPlusDaysFromToday = true;
		return this;
	}

	public SimpleValueJqwikPlugin characterPredicate(Predicate<Character> characterPredicate) {
		this.characterPredicate = characterPredicate;
		return this;
	}

	@Override
	public void accept(FixtureMonkeyOptionsBuilder optionsBuilder) {
		long maxStringLength = this.maxStringLength;
		if (!setMaxStringLength) {
			maxStringLength = Math.max(
				DEFAULT_MAX_STRING_LENGTH,
				minStringLength + DEFAULT_STRING_BUFFER_LENGTH
			);
		}

		long maxNumberValue = this.maxNumberValue;
		if (!setMaxNumberValue) {
			maxNumberValue = Math.max(DEFAULT_MAX_NUMBER_VALUE, minNumberValue + DEFAULT_NUMBER_BUFFER_VALUE);
		}

		int maxContainerSize = this.maxContainerSize;
		if (!setMaxContainerSize) {
			maxContainerSize = Math.max(
				DEFAULT_MAX_CONTAINER_SIZE,
				minContainerSize + DEFAULT_CONTAINER_BUFFER_SIZE
			);
		}

		long plusDaysFromToday = this.plusDaysFromToday;
		if (!setPlusDaysFromToday) {
			plusDaysFromToday = Math.max(
				DEFAULT_PLUS_DAYS,
				minusDaysFromToday + DEFAULT_DAYS_BUFFER
			);
		}

		SimpleJavaConstraintGenerator simpleJavaConstraintGenerator = new SimpleJavaConstraintGenerator(
			this.minStringLength,
			maxStringLength,
			this.minNumberValue,
			maxNumberValue,
			this.minContainerSize,
			maxContainerSize,
			this.minusDaysFromToday,
			plusDaysFromToday
		);

		optionsBuilder.insertFirstJavaConstraintGeneratorCustomizer(
				javaConstraintGenerator ->
					new CompositeJavaConstraintGenerator(
						Arrays.asList(
							javaConstraintGenerator,
							simpleJavaConstraintGenerator
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
			)
			.defaultArbitraryContainerInfoGenerator(
				containerType ->
					new ArbitraryContainerInfo(
						simpleJavaConstraintGenerator.minContainerSize,
						simpleJavaConstraintGenerator.maxContainerSize
					)
			);
	}

	private static class SimpleJavaConstraintGenerator implements JavaConstraintGenerator {
		private final long stringMinLength;
		private final long stringMaxLength;
		private final long minNumberValue;
		private final long maxNumberValue;
		private final int minContainerSize;
		private final int maxContainerSize;
		private final long minusDaysFromToday;
		private final long plusDaysFromToday;

		public SimpleJavaConstraintGenerator(
			long stringMinLength,
			long stringMaxLength,
			long minNumberValue,
			long maxNumberValue,
			int minContainerSize,
			int maxContainerSize,
			long minusDaysFromToday,
			long plusDaysFromToday
		) {
			this.stringMinLength = stringMinLength;
			this.stringMaxLength = stringMaxLength;
			this.minNumberValue = minNumberValue;
			this.maxNumberValue = maxNumberValue;
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
				false,
				null,
				false
			);
		}

		@Override
		public JavaIntegerConstraint generateIntegerConstraint(ArbitraryGeneratorContext context) {
			BigInteger lowerBound = BigInteger.valueOf(minNumberValue);
			BigInteger upperBound = BigInteger.valueOf(maxNumberValue);

			Class<?> type = Types.getActualType(context.getResolvedType());
			if (type == Byte.class || type == byte.class) {
				upperBound = upperBound.min(BIG_INTEGER_MAX_BYTE);
				lowerBound = lowerBound.max(BIG_INTEGER_MIN_BYTE);
			}

			return new JavaIntegerConstraint(lowerBound, upperBound);
		}

		@Override
		public JavaDecimalConstraint generateDecimalConstraint(ArbitraryGeneratorContext context) {
			BigDecimal lowerBound = BigDecimal.valueOf(minNumberValue);
			BigDecimal upperBound = BigDecimal.valueOf(maxNumberValue);

			return new JavaDecimalConstraint(lowerBound, true, upperBound, true, 2);
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
