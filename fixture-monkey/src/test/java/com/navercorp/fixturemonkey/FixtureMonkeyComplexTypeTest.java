package com.navercorp.fixturemonkey;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;
import java.util.stream.IntStream;

import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.sampletypes.NoArgsConstructor;

public class FixtureMonkeyComplexTypeTest {
	@Property
	public void giveMeOneReturnsCorrectNoArgsConstructorValue() {
		FixtureMonkey sut = new FixtureMonkey();
		NoArgsConstructor actual = sut.giveMeOne(NoArgsConstructor.class);
		then(actual).isNotNull();
	}

	@Property
	public void giveMeOneGeneratesSeveralDistinctNoArgsConstructorValues() {
		FixtureMonkey sut = new FixtureMonkey();

		List<NoArgsConstructor> actual = IntStream.range(0, 10)
			.boxed()
			.map(it -> sut.giveMeOne(NoArgsConstructor.class))
			.distinct()
			.collect(toList());

		then(actual.size()).isEqualTo(10);
	}

	@Property
	public void giveMeOneGeneratesSeveralDistinctNoArgsConstructorValuesFromList() {
		FixtureMonkey sut = new FixtureMonkey();
		List<NoArgsConstructor> actual = sut.giveMe(NoArgsConstructor.class, 10);
		then(actual.size()).isEqualTo(10);
	}
}
