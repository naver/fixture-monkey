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
	public RandomGenerator<T> generator(int genSize) {
		return getArbitrary().generator(genSize);
	}

	@Override
	public Arbitrary<Object> asGeneric() {
		return getArbitrary().asGeneric();
	}

	@Override
	public boolean isUnique() {
		return getArbitrary().isUnique();
	}

	@Override
	public Optional<ExhaustiveGenerator<T>> exhaustive() {
		return getArbitrary().exhaustive();
	}

	@Override
	public Optional<ExhaustiveGenerator<T>> exhaustive(long maxNumberOfSamples) {
		return getArbitrary().exhaustive(maxNumberOfSamples);
	}

	@Override
	public EdgeCases<T> edgeCases() {
		return getArbitrary().edgeCases();
	}

	@Override
	public Optional<Stream<T>> allValues() {
		return getArbitrary().allValues();
	}

	@Override
	public void forEachValue(Consumer<? super T> action) {
		getArbitrary().forEachValue(action);
	}

	@Override
	public Arbitrary<T> filter(Predicate<T> filterPredicate) {
		return getArbitrary().filter(filterPredicate);
	}

	@Override
	public <U> Arbitrary<U> map(Function<T, U> mapper) {
		return getArbitrary().map(mapper);
	}

	@Override
	public <U> Arbitrary<U> flatMap(Function<T, Arbitrary<U>> mapper) {
		return getArbitrary().flatMap(mapper);
	}

	@Override
	public Arbitrary<T> injectNull(double nullProbability) {
		return getArbitrary().injectNull(nullProbability);
	}

	@Override
	public Arbitrary<T> unique() {
		return getArbitrary().unique();
	}

	@Override
	public Arbitrary<T> fixGenSize(int genSize) {
		return getArbitrary().fixGenSize(genSize);
	}

	@Override
	public ListArbitrary<T> list() {
		return getArbitrary().list();
	}

	@Override
	public SetArbitrary<T> set() {
		return getArbitrary().set();
	}

	@Override
	public StreamArbitrary<T> stream() {
		return getArbitrary().stream();
	}

	@Override
	public IteratorArbitrary<T> iterator() {
		return getArbitrary().iterator();
	}

	@Override
	public <A> StreamableArbitrary<T, A> array(Class<A> arrayClass) {
		return getArbitrary().array(arrayClass);
	}

	@Override
	public Arbitrary<Optional<T>> optional() {
		return getArbitrary().optional();
	}

	@Override
	public Arbitrary<List<T>> collect(Predicate<List<T>> until) {
		return getArbitrary().collect(until);
	}

	@Override
	public Stream<T> sampleStream() {
		return getArbitrary().sampleStream();
	}

	@SuppressWarnings("unchecked")
	@Override
	public T sample() {
		try {
			return getArbitrary()
				.filter((Predicate<T>)this.validateFilter(validOnly))
				.sample();
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
	public Arbitrary<T> injectDuplicates(double duplicateProbability) {
		return getArbitrary().injectDuplicates(duplicateProbability);
	}

	@Override
	public Arbitrary<Tuple1<T>> tuple1() {
		return getArbitrary().tuple1();
	}

	@Override
	public Arbitrary<Tuple2<T, T>> tuple2() {
		return getArbitrary().tuple2();
	}

	@Override
	public Arbitrary<Tuple3<T, T, T>> tuple3() {
		return getArbitrary().tuple3();
	}

	@Override
	public Arbitrary<Tuple4<T, T, T, T>> tuple4() {
		return getArbitrary().tuple4();
	}

	@Override
	public Arbitrary<Tuple5<T, T, T, T, T>> tuple5() {
		return getArbitrary().tuple5();
	}

	@Override
	public Arbitrary<T> ignoreException(Class<? extends Throwable> exceptionType) {
		return getArbitrary().ignoreException(exceptionType);
	}

	@Override
	public Arbitrary<T> dontShrink() {
		return getArbitrary().dontShrink();
	}

	@Override
	public Arbitrary<T> edgeCases(Consumer<Config<T>> configurator) {
		return getArbitrary().edgeCases(configurator);
	}

	private synchronized Arbitrary<T> getArbitrary() {
		if (this.arbitrary == null) {
			this.arbitrary = generateArbitrary.get();
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
