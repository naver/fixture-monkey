package com.navercorp.fixturemonkey.arbitrary;

import static com.navercorp.fixturemonkey.ArbitrarySupports.uniqueAndGet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
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
	private final Arbitrary<T> arbitrary;
	private final boolean validOnly;
	private final ArbitraryValidator<T> validator;
	@SuppressWarnings("rawtypes")
	private final Map<String, ConstraintViolation> violations = new HashMap<>();
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private Exception lastException;

	public ArbitraryValue(
		Arbitrary<T> arbitrary,
		ArbitraryValidator<T> validator,
		boolean validOnly
	) {
		this.arbitrary = arbitrary;
		this.validator = validator;
		this.validOnly = validOnly;
	}

	@Override
	public RandomGenerator<T> generator(int genSize) {
		return arbitrary.generator(genSize);
	}

	@Override
	public Arbitrary<Object> asGeneric() {
		return arbitrary.asGeneric();
	}

	@Override
	public boolean isUnique() {
		return arbitrary.isUnique();
	}

	@Override
	public Optional<ExhaustiveGenerator<T>> exhaustive() {
		return arbitrary.exhaustive();
	}

	@Override
	public Optional<ExhaustiveGenerator<T>> exhaustive(long maxNumberOfSamples) {
		return arbitrary.exhaustive(maxNumberOfSamples);
	}

	@Override
	public EdgeCases<T> edgeCases() {
		return arbitrary.edgeCases();
	}

	@Override
	public Optional<Stream<T>> allValues() {
		return arbitrary.allValues();
	}

	@Override
	public void forEachValue(Consumer<? super T> action) {
		arbitrary.forEachValue(action);
	}

	@Override
	public Arbitrary<T> filter(Predicate<T> filterPredicate) {
		return arbitrary.filter(filterPredicate);
	}

	@Override
	public <U> Arbitrary<U> map(Function<T, U> mapper) {
		return arbitrary.map(mapper);
	}

	@Override
	public <U> Arbitrary<U> flatMap(Function<T, Arbitrary<U>> mapper) {
		return arbitrary.flatMap(mapper);
	}

	@Override
	public Arbitrary<T> injectNull(double nullProbability) {
		return arbitrary.injectNull(nullProbability);
	}

	@Override
	public Arbitrary<T> unique() {
		return arbitrary.unique();
	}

	@Override
	public Arbitrary<T> fixGenSize(int genSize) {
		return arbitrary.fixGenSize(genSize);
	}

	@Override
	public ListArbitrary<T> list() {
		return arbitrary.list();
	}

	@Override
	public SetArbitrary<T> set() {
		return arbitrary.set();
	}

	@Override
	public StreamArbitrary<T> stream() {
		return arbitrary.stream();
	}

	@Override
	public IteratorArbitrary<T> iterator() {
		return arbitrary.iterator();
	}

	@Override
	public <A> StreamableArbitrary<T, A> array(Class<A> arrayClass) {
		return arbitrary.array(arrayClass);
	}

	@Override
	public Arbitrary<Optional<T>> optional() {
		return arbitrary.optional();
	}

	@Override
	public Arbitrary<List<T>> collect(Predicate<List<T>> until) {
		return arbitrary.collect(until);
	}

	@Override
	public Stream<T> sampleStream() {
		return arbitrary.sampleStream();
	}

	@SuppressWarnings("unchecked")
	@Override
	public T sample() {
		return uniqueAndGet(
			() -> {
				try {
					return arbitrary
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
				}
			}
		);
	}

	@Override
	public Arbitrary<T> injectDuplicates(double duplicateProbability) {
		return arbitrary.injectDuplicates(duplicateProbability);
	}

	@Override
	public Arbitrary<Tuple1<T>> tuple1() {
		return arbitrary.tuple1();
	}

	@Override
	public Arbitrary<Tuple2<T, T>> tuple2() {
		return arbitrary.tuple2();
	}

	@Override
	public Arbitrary<Tuple3<T, T, T>> tuple3() {
		return arbitrary.tuple3();
	}

	@Override
	public Arbitrary<Tuple4<T, T, T, T>> tuple4() {
		return arbitrary.tuple4();
	}

	@Override
	public Arbitrary<Tuple5<T, T, T, T, T>> tuple5() {
		return arbitrary.tuple5();
	}

	@Override
	public Arbitrary<T> ignoreException(Class<? extends Throwable> exceptionType) {
		return arbitrary.ignoreException(exceptionType);
	}

	@Override
	public Arbitrary<T> dontShrink() {
		return arbitrary.dontShrink();
	}

	@Override
	public Arbitrary<T> edgeCases(Consumer<Config<T>> configurator) {
		return arbitrary.edgeCases(configurator);
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
