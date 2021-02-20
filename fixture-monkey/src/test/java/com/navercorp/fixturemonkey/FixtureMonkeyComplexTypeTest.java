package com.navercorp.fixturemonkey;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.sampletypes.NoArgsConstructor;
import com.navercorp.fixturemonkey.sampletypes.OneArgConstructor;
import com.navercorp.fixturemonkey.sampletypes.ThreeArgsConstructor;

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

		Set<NoArgsConstructor> actual = IntStream.range(0, 10)
			.boxed()
			.map(it -> sut.giveMeOne(NoArgsConstructor.class))
			.collect(toSet());

		then(actual.size()).isEqualTo(10);
	}

	@Property
	public void giveMeOneGeneratesSeveralDistinctNoArgsConstructorValuesFromList() {
		FixtureMonkey sut = new FixtureMonkey();
		List<NoArgsConstructor> actual = sut.giveMe(NoArgsConstructor.class, 10);
		then(actual.size()).isEqualTo(10);
	}

	@Property
	public void giveMeOneReturnsCorrectOneArgConstructorValue() {
		FixtureMonkey sut = new FixtureMonkey();
		OneArgConstructor actual = sut.giveMeOne(OneArgConstructor.class);
		then(actual).isNotNull();
	}

	@Property
	public void giveMeOneReturnsCorrectTwoArgsConstructorValue() {
		FixtureMonkey sut = new FixtureMonkey();
		OneArgConstructor actual = sut.giveMeOne(OneArgConstructor.class);
		then(actual).isNotNull();
	}

	@Property
	public void giveMeOneReturnsCorrectThreeArgsConstructorValue() {
		FixtureMonkey sut = new FixtureMonkey();
		ThreeArgsConstructor actual = sut.giveMeOne(ThreeArgsConstructor.class);
		then(actual).isNotNull();
	}

	@Property
	public void giveMeOneGeneratesSeveralDistinctParameterizedConstructorValues() {
		FixtureMonkey sut = new FixtureMonkey();

		Set<ThreeArgsConstructor> actual = IntStream.range(0, 20)
			.boxed()
			.map(it -> sut.giveMeOne(ThreeArgsConstructor.class))
			.collect(toSet());

		then(actual.size()).isGreaterThan(10);
	}
}
