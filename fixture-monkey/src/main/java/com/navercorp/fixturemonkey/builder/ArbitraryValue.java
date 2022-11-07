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

package com.navercorp.fixturemonkey.builder;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.EdgeCases;
import net.jqwik.api.RandomGenerator;
import net.jqwik.api.Shrinkable;
import net.jqwik.api.TooManyFilterMissesException;

import com.navercorp.fixturemonkey.validator.ArbitraryValidator;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
final class ArbitraryValue<T> implements Arbitrary<T> {
	private final MonkeyRandomGenerator<T> monkeyRandomGenerator;

	public ArbitraryValue(
		Supplier<Arbitrary<T>> generateArbitrary,
		ArbitraryValidator validator,
		boolean validOnly
	) {
		this.monkeyRandomGenerator = new MonkeyRandomGenerator<>(generateArbitrary, validator, validOnly);
	}

	@Override
	public RandomGenerator<T> generator(int genSize) {
		return this.monkeyRandomGenerator;
	}

	@Override
	public EdgeCases<T> edgeCases(int maxEdgeCases) {
		return EdgeCases.none();
	}

	@Override
	public boolean isGeneratorMemoizable() {
		return false;
	}

	private static final class MonkeyRandomGenerator<T> implements RandomGenerator<T> {
		private final Logger log = LoggerFactory.getLogger(this.getClass());

		private final Supplier<Arbitrary<T>> generateArbitrary;
		private final boolean validOnly;
		private final ArbitraryValidator validator;
		@SuppressWarnings("rawtypes")
		private final Map<String, ConstraintViolation> violations = new ConcurrentHashMap<>();
		private Exception lastException;

		private MonkeyRandomGenerator(
			Supplier<Arbitrary<T>> generateArbitrary,
			ArbitraryValidator validator,
			boolean validOnly
		) {
			this.generateArbitrary = generateArbitrary;
			this.validator = validator;
			this.validOnly = validOnly;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Shrinkable<T> next(Random random) {
			try {
				return getArbitrary()
					.filter((Predicate<T>)this.validateFilter(validOnly))
					.generator(1000)
					.next(random);
			} catch (TooManyFilterMissesException ex) {
				StringBuilder builder = new StringBuilder();
				this.violations.values().forEach(violation -> builder
					.append("- violation: ").append(violation.getMessage())
					.append(", type: ").append(violation.getRootBeanClass())
					.append(", property: ").append(violation.getPropertyPath())
					.append(", invalidValue: ").append(violation.getInvalidValue())
					.append("\n"));

				log.error("Fail to create valid arbitrary."
					+ "\n\nFixture factory Constraint Violation messages. \n\n" + builder, lastException);

				throw ex;
			}
		}

		@SuppressWarnings("rawtypes")
		private Predicate validateFilter(boolean validOnly) {
			return fixture -> {
				if (!validOnly) {
					return true;
				}

				if (fixture == null) {
					return true;
				}

				try {
					this.validator.validate(fixture);
					return true;
				} catch (ConstraintViolationException ex) {
					ex.getConstraintViolations().forEach(violation ->
						this.violations.put(
							violation.getRootBeanClass().getName() + violation.getPropertyPath(),
							violation
						)
					);
					this.lastException = ex;
				}
				return false;
			};
		}

		private synchronized Arbitrary<T> getArbitrary() {
			return generateArbitrary.get();
		}
	}
}
