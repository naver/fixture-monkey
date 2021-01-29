package com.navercorp.fixturemonkey;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

class FixtureMonkeyPrimitiveTest {

	@Property
	void giveMeOneReturnsCorrectByteValue() {
		FixtureMonkey sut = new FixtureMonkey();

		byte actual = sut.giveMeOne(byte.class);

		then(actual)
			.isGreaterThanOrEqualTo(Byte.MIN_VALUE)
			.isLessThanOrEqualTo(Byte.MAX_VALUE);
	}

	@Property
	void giveMeOneReturnsCorrectShortValue() {
		FixtureMonkey sut = new FixtureMonkey();

		short actual = sut.giveMeOne(short.class);

		then(actual)
			.isGreaterThanOrEqualTo(Short.MIN_VALUE)
			.isLessThanOrEqualTo(Short.MAX_VALUE);
	}

	@Property
	void giveMeOneReturnsCorrectIntegerValue() {
		FixtureMonkey sut = new FixtureMonkey();

		int actual = sut.giveMeOne(int.class);

		then(actual)
			.isGreaterThanOrEqualTo(Integer.MIN_VALUE)
			.isLessThanOrEqualTo(Integer.MAX_VALUE);
	}

	@Property
	<T> void giveMeOneReturnsSeveralDistinctValues(@ForAll("primitiveTypes") Class<T> type) {
		FixtureMonkey sut = new FixtureMonkey();

		Stream<T> actual = IntStream
			.range(0, 100)
			.mapToObj(x -> sut.giveMeOne(type))
			.distinct();

		then(actual.count()).isGreaterThan(10);
	}

	@Provide
	@SuppressWarnings("unused")
	Arbitrary<Class<?>> primitiveTypes() {
		return Arbitraries.of(
			byte.class,
			short.class,
			int.class
		);
	}
}
