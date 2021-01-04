package com.navercorp.fixturemonkey;

import java.util.UUID;
import java.util.stream.Stream;

import net.jqwik.api.Example;

import lombok.Builder;
import lombok.Value;

class FixtureMonkeyTest {
	private final FixtureMonkey monkey = new FixtureMonkey();

	@Example
	void giveMe() {
		Stream<Person> actual = this.monkey.giveMe(Person.class);
		// TODO: assertion
	}

	@Example
	void giveMeOne() {
		Person actual = this.monkey.giveMeOne(Person.class);
		// TODO: assertion
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
}
