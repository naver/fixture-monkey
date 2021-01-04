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
