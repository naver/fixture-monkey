package com.navercorp.fixturemonkey;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Assume;
import net.jqwik.api.Combinators;
import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

import lombok.Builder;
import lombok.Value;

import com.navercorp.fixturemonkey.arbitrary.ArbitraryUtils;

class FixtureMonkeyTest {
	private final FixtureMonkey monkey = new FixtureMonkey();

	@Example
	void giveMe() {
		Assume.that(false);
		Stream<Person> actual = this.monkey.giveMe(Person.class);
		// TODO: assertion
	}

	@Example
	void giveMeOne() {
		Assume.that(false);
		Person actual = this.monkey.giveMeOne(Person.class);
		// TODO: assertion
	}

	@Property
	void giveMeWithArbitrary(@ForAll @IntRange(min = 1, max = 5000) int size) {
		// given
		Arbitrary<Banana> banana = new BananaArbitrary().arbitrary();

		// when
		Stream<Banana> actual = this.monkey.giveMe(banana);

		// then
		actual.limit(size).forEach(it -> {
			then(it.id).isNotNull();
			then(it.color).isIn("GREEN", "YELLOW", "BROWN", "BLACK");
			then(it.length).isBetween(3, 50);
			then(it.rotten).isNotNull();
		});
	}

	@Property
	void giveMeWithArbitrarySize(@ForAll @IntRange(min = 1, max = 5000) int size) {
		// given
		Arbitrary<Banana> banana = new BananaArbitrary().arbitrary();

		// when
		List<Banana> actual = this.monkey.giveMe(banana, size);

		// then
		actual.forEach(it -> {
			then(it.id).isNotNull();
			then(it.color).isIn("GREEN", "YELLOW", "BROWN", "BLACK");
			then(it.length).isBetween(3, 50);
			then(it.rotten).isNotNull();
		});
	}

	@Property
	void giveMeOneArbitrary() {
		// given
		Arbitrary<Banana> banana = new BananaArbitrary().arbitrary();

		// when
		Banana actual = this.monkey.giveMeOne(banana);

		// then
		then(actual.color).isIn("GREEN", "YELLOW", "BROWN", "BLACK");
		then(actual.length).isBetween(3, 50);
		then(actual.rotten).isNotNull();
	}

	@Value
	@Builder
	public static class Person {
		UUID id;

		Long version;

		String name;

		int age;

		String email;
	}

	@Value
	@Builder
	public static class Banana {
		UUID id;
		String color;
		int length;
		Boolean rotten;
	}

	public static class BananaArbitrary {
		private final Arbitrary<UUID> id = ArbitraryUtils.uuid();
		private final Arbitrary<String> color = Arbitraries.of(
			"GREEN", "YELLOW", "BROWN", "BLACK"
		);
		private final Arbitrary<Integer> length = Arbitraries.integers().between(3, 50);
		private final Arbitrary<Boolean> rotten = Arbitraries.forType(Boolean.class);

		public Arbitrary<Banana> arbitrary() {
			return Combinators.withBuilder(Banana::builder)
				.use(this.id).in(Banana.BananaBuilder::id)
				.use(this.color).in(Banana.BananaBuilder::color)
				.use(this.length).in(Banana.BananaBuilder::length)
				.use(this.rotten).in(Banana.BananaBuilder::rotten)
				.build(Banana.BananaBuilder::build);
		}
	}
}
