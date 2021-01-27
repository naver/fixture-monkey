package com.navercorp.fixturemonkey;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.jqwik.api.Disabled;
import net.jqwik.api.Example;

import lombok.Value;

class FixtureMonkeyScenario {
	@Example
	@Disabled("타입에 따른 ArbitraryGeneratorContext 구현 필요")
	void specimen() {
		FixtureMonkey sut = new FixtureMonkey();

		Stream<Person> actual = sut.giveMe(Person.class);

		List<Person> persons = actual.limit(3).collect(Collectors.toList());
	}

	@Value
	static class Person {
		String name;
		int age;
	}
}
