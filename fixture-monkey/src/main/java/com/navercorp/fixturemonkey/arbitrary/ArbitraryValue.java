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

package com.navercorp.fixturemonkey.arbitrary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.EdgeCases;
import net.jqwik.api.EdgeCases.Config;
import net.jqwik.api.ExhaustiveGenerator;
import net.jqwik.api.RandomGenerator;
import net.jqwik.api.TooManyFilterMissesException;
import net.jqwik.api.Tuple.Tuple1;
import net.jqwik.api.Tuple.Tuple2;
import net.jqwik.api.Tuple.Tuple3;
import net.jqwik.api.Tuple.Tuple4;
import net.jqwik.api.Tuple.Tuple5;
import net.jqwik.api.arbitraries.IteratorArbitrary;
import net.jqwik.api.arbitraries.ListArbitrary;
import net.jqwik.api.arbitraries.SetArbitrary;
import net.jqwik.api.arbitraries.StreamArbitrary;
import net.jqwik.api.arbitraries.StreamableArbitrary;

import com.navercorp.fixturemonkey.validator.ArbitraryValidator;

final class ArbitraryValue<T> implements Arbitrary<T> {
	private final Supplier<Arbitrary<T>> generateArbitrary;
	private Arbitrary<T> arbitrary;
	private final boolean validOnly;
	private final ArbitraryValidator<T> validator;
	@SuppressWarnings("rawtypes")
	private final Map<String, ConstraintViolation> violations = new HashMap<>();
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private Exception lastException;

	public ArbitraryValue(
		Supplier<Arbitrary<T>> generateArbitrary,
		ArbitraryValidator<T> validator,
		boolean validOnly
	) {
		this.generateArbitrary = generateArbitrary;
		this.validator = validator;
		this.validOnly = validOnly;
	}

	@Override
	public synchronized RandomGenerator<T> generator(int genSize) {
		try {
			return getArbitrary().generator(genSize);
		} finally {
			this.arbitrary = null; // in order to getting new value whenever sampling, set arbitrary as null
		}
	}

	@Override
	public synchronized Arbitrary<Object> asGeneric() {
		try {
			return getArbitrary().asGeneric();
		} finally {
			this.arbitrary = null; // in order to getting new value whenever sampling, set arbitrary as null
		}
	}

	@Override
	public boolean isUnique() {
		return getArbitrary().isUnique();
	}

	@Override
	public synchronized Optional<ExhaustiveGenerator<T>> exhaustive() {
		try {
			return getArbitrary().exhaustive();
		} finally {
			this.arbitrary = null; // in order to getting new value whenever sampling, set arbitrary as null
		}
	}

	@Override
	public synchronized Optional<ExhaustiveGenerator<T>> exhaustive(long maxNumberOfSamples) {
		try {
			return getArbitrary().exhaustive(maxNumberOfSamples);
		} finally {
			this.arbitrary = null; // in order to getting new value whenever sampling, set arbitrary as null
		}
	}

	@Override
	public synchronized EdgeCases<T> edgeCases() {
		try {
			return getArbitrary().edgeCases();
		} finally {
			this.arbitrary = null; // in order to getting new value whenever sampling, set arbitrary as null
		}
	}

	@Override
	public synchronized Optional<Stream<T>> allValues() {
		try {
			return getArbitrary().allValues();
		} finally {
			this.arbitrary = null; // in order to getting new value whenever sampling, set arbitrary as null
		}
	}

	@Override
	public synchronized void forEachValue(Consumer<? super T> action) {
		try {
			getArbitrary().forEachValue(action);
		} finally {
			this.arbitrary = null; // in order to getting new value whenever sampling, set arbitrary as null
		}
	}

	@Override
	public synchronized Arbitrary<T> filter(Predicate<T> filterPredicate) {
		try {
			return getArbitrary().filter(filterPredicate);
		} finally {
			this.arbitrary = null; // in order to getting new value whenever sampling, set arbitrary as null
		}
	}

	@Override
	public synchronized <U> Arbitrary<U> map(Function<T, U> mapper) {
		try {
			return getArbitrary().map(mapper);
		} finally {
			this.arbitrary = null; // in order to getting new value whenever sampling, set arbitrary as null
		}
	}

	@Override
	public synchronized <U> Arbitrary<U> flatMap(Function<T, Arbitrary<U>> mapper) {
		try {
			return getArbitrary().flatMap(mapper);
		} finally {
			this.arbitrary = null; // in order to getting new value whenever sampling, set arbitrary as null
		}
	}

	@Override
	public synchronized Arbitrary<T> injectNull(double nullProbability) {
		try {
			return getArbitrary().injectNull(nullProbability);
		} finally {
			this.arbitrary = null; // in order to getting new value whenever sampling, set arbitrary as null
		}
	}

	@Override
	public synchronized Arbitrary<T> unique() {
		try {
			return getArbitrary().unique();
		} finally {
			this.arbitrary = null; // in order to getting new value whenever sampling, set arbitrary as null
		}
	}

	@Override
	public synchronized Arbitrary<T> fixGenSize(int genSize) {
		try {
			return getArbitrary().fixGenSize(genSize);
		} finally {
			this.arbitrary = null; // in order to getting new value whenever sampling, set arbitrary as null
		}
	}

	@Override
	public synchronized ListArbitrary<T> list() {
		try {
			return getArbitrary().list();
		} finally {
			this.arbitrary = null; // in order to getting new value whenever sampling, set arbitrary as null
		}
	}

	@Override
	public synchronized SetArbitrary<T> set() {
		try {
			return getArbitrary().set();
		} finally {
			this.arbitrary = null; // in order to getting new value whenever sampling, set arbitrary as null
		}
	}

	@Override
	public synchronized StreamArbitrary<T> stream() {
		try {
			return getArbitrary().stream();
		} finally {
			this.arbitrary = null; // in order to getting new value whenever sampling, set arbitrary as null
		}
	}

	@Override
	public synchronized IteratorArbitrary<T> iterator() {
		try {
			return getArbitrary().iterator();
		} finally {
			this.arbitrary = null; // in order to getting new value whenever sampling, set arbitrary as null
		}
	}

	@Override
	public synchronized <A> StreamableArbitrary<T, A> array(Class<A> arrayClass) {
		try {
			return getArbitrary().array(arrayClass);
		} finally {
			this.arbitrary = null; // in order to getting new value whenever sampling, set arbitrary as null
		}
	}

	@Override
	public synchronized Arbitrary<Optional<T>> optional() {
		try {
			return getArbitrary().optional();
		} finally {
			this.arbitrary = null; // in order to getting new value whenever sampling, set arbitrary as null
		}
	}

	@Override
	public synchronized Arbitrary<List<T>> collect(Predicate<List<T>> until) {
		try {
			return getArbitrary().collect(until);
		} finally {
			this.arbitrary = null; // in order to getting new value whenever sampling, set arbitrary as null
		}
	}

	@Override
	public synchronized Stream<T> sampleStream() {
		try {
			return getArbitrary().sampleStream();
		} finally {
			this.arbitrary = null; // in order to getting new value whenever sampling, set arbitrary as null
		}
	}

	@Override
	public synchronized T sample() {
		try {
			return getArbitrary().sample();
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
		} finally {
			this.arbitrary = null; // in order to getting new value whenever sampling, set arbitrary as null
		}
	}

	@Override
	public synchronized Arbitrary<T> injectDuplicates(double duplicateProbability) {
		try {
			return getArbitrary().injectDuplicates(duplicateProbability);
		} finally {
			this.arbitrary = null; // in order to getting new value whenever sampling, set arbitrary as null
		}
	}

	@Override
	public synchronized Arbitrary<Tuple1<T>> tuple1() {
		try {
			return getArbitrary().tuple1();
		} finally {
			this.arbitrary = null; // in order to getting new value whenever sampling, set arbitrary as null
		}
	}

	@Override
	public synchronized Arbitrary<Tuple2<T, T>> tuple2() {
		try {
			return getArbitrary().tuple2();
		} finally {
			this.arbitrary = null; // in order to getting new value whenever sampling, set arbitrary as null
		}
	}

	@Override
	public synchronized Arbitrary<Tuple3<T, T, T>> tuple3() {
		try {
			return getArbitrary().tuple3();
		} finally {
			this.arbitrary = null; // in order to getting new value whenever sampling, set arbitrary as null
		}
	}

	@Override
	public synchronized Arbitrary<Tuple4<T, T, T, T>> tuple4() {
		try {
			return getArbitrary().tuple4();
		} finally {
			this.arbitrary = null; // in order to getting new value whenever sampling, set arbitrary as null
		}
	}

	@Override
	public synchronized Arbitrary<Tuple5<T, T, T, T, T>> tuple5() {
		try {
			return getArbitrary().tuple5();
		} finally {
			this.arbitrary = null; // in order to getting new value whenever sampling, set arbitrary as null
		}
	}

	@Override
	public synchronized Arbitrary<T> ignoreException(Class<? extends Throwable> exceptionType) {
		try {
			return getArbitrary().ignoreException(exceptionType);
		} finally {
			this.arbitrary = null; // in order to getting new value whenever sampling, set arbitrary as null
		}
	}

	@Override
	public synchronized Arbitrary<T> dontShrink() {
		try {
			return getArbitrary().dontShrink();
		} finally {
			this.arbitrary = null; // in order to getting new value whenever sampling, set arbitrary as null
		}
	}

	@Override
	public synchronized Arbitrary<T> edgeCases(Consumer<Config<T>> configurator) {
		try {
			return getArbitrary().edgeCases(configurator);
		} finally {
			this.arbitrary = null; // in order to getting new value whenever sampling, set arbitrary as null
		}
	}

	@SuppressWarnings("unchecked")
	private synchronized Arbitrary<T> getArbitrary() {
		if (this.arbitrary == null) {
			this.arbitrary = generateArbitrary.get()
				.filter((Predicate<T>)this.validateFilter(validOnly));
		}
		return this.arbitrary;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private Predicate validateFilter(boolean validOnly) {
		return fixture -> {
			if (!validOnly) {
				return true;
			}

			if (fixture == null) {
				return true;
			}

			try {
				validator.validate((T)fixture);
				return true;
			} catch (ConstraintViolationException ex) {
				ex.getConstraintViolations().forEach(violation ->
					this.violations.put(violation.getRootBeanClass().getName() + violation.getPropertyPath(),
						violation));
				this.lastException = ex;
			}
			return false;
		};
	}
}
