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
