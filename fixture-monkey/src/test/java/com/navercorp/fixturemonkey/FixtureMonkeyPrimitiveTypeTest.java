package com.navercorp.fixturemonkey;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

class FixtureMonkeyPrimitiveTypeTest {
	@Property
	void giveMeOneReturnsCorrectByteValue() {
		FixtureMonkey sut = new FixtureMonkey();
		byte actual = sut.giveMeOne(byte.class);
		then(actual).isBetween(Byte.MIN_VALUE, Byte.MAX_VALUE);
	}

	@Property
	void giveMeOneReturnsCorrectShortValue() {
		FixtureMonkey sut = new FixtureMonkey();
		short actual = sut.giveMeOne(short.class);
		then(actual).isBetween(Short.MIN_VALUE, Short.MAX_VALUE);
	}

	@Property
	void giveMeOneReturnsCorrectIntegerValue() {
		FixtureMonkey sut = new FixtureMonkey();
		int actual = sut.giveMeOne(int.class);
		then(actual).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeOneReturnsCorrectLongValue() {
		FixtureMonkey sut = new FixtureMonkey();
		long actual = sut.giveMeOne(long.class);
		then(actual).isBetween(Long.MIN_VALUE, Long.MAX_VALUE);
	}

	@Property
	void giveMeOneReturnsCorrectFloatValue() {
		FixtureMonkey sut = new FixtureMonkey();
		float actual = sut.giveMeOne(float.class);
		then(actual).isBetween(-Float.MAX_VALUE, Float.MAX_VALUE);
	}

	@Property
	void giveMeOneReturnsCorrectDoubleValue() {
		FixtureMonkey sut = new FixtureMonkey();
		double actual = sut.giveMeOne(double.class);
		then(actual).isBetween(-Double.MAX_VALUE, Double.MAX_VALUE);
	}

	@Property
	void giveMeOneReturnsCorrectCharValue() {
		FixtureMonkey sut = new FixtureMonkey();
		char actual = sut.giveMeOne(char.class);
		then(actual).isBetween(Character.MIN_VALUE, Character.MAX_VALUE);
	}

	@Property
	void giveMeOneGeneratesDistinctBooleanValues() {
		FixtureMonkey sut = new FixtureMonkey();

		Stream<Boolean> actual = IntStream
			.range(0, 20)
			.boxed()
			.map(x -> sut.giveMeOne(boolean.class))
			.distinct();

		then(actual.count()).isEqualTo(2);
	}

	@Property
	<T> void giveMeOneGeneratesSeveralPrimitiveValues(
		@ForAll("primitiveTypes") Class<T> type
	) {
		FixtureMonkey sut = new FixtureMonkey();

		Stream<T> actual = IntStream
			.range(0, 100)
			.boxed()
			.map(x -> sut.giveMeOne(type))
			.distinct();

		then(actual.count()).isGreaterThan(10);
	}

	@Provide
	@SuppressWarnings("unused")
	Arbitrary<Class<?>> primitiveTypes() {
		return Arbitraries.of(
			byte.class,
			short.class,
			int.class,
			long.class,
			float.class,
			double.class,
			char.class
		);
	}

	@Property
	<T> void giveMeOneGeneratesSeveralPrimitiveWrappedValues(
		@ForAll("primitiveWrappedTypes") Class<T> type
	) {
		FixtureMonkey sut = new FixtureMonkey();

		Stream<T> actual = IntStream
			.range(0, 100)
			.boxed()
			.map(x -> sut.giveMeOne(type))
			.distinct();

		then(actual.count()).isGreaterThan(10);
	}

	@Provide
	@SuppressWarnings("unused")
	Arbitrary<Class<?>> primitiveWrappedTypes() {
		return Arbitraries.of(
			Byte.class,
			Short.class,
			Integer.class,
			Long.class,
			Float.class,
			Double.class,
			Character.class
		);
	}
}
