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

package com.navercorp.fixturemonkey.tests.java;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.RepeatedTest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.type.TypeReference;

class RegisterSameTypeTwiceTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.register(Child.class, fm -> fm.giveMeBuilder(Child.class).set("name", "low").set("tag", "lowTag"), 2)
		.register(Child.class, fm -> fm.giveMeBuilder(Child.class).set("name", "high").set("count", 5), 1)
		.build();

	@RepeatedTest(10)
	void higherPriorityValueWinsWhenSameTypeIsRegisteredTwice() {
		// when
		Child actual = SUT.giveMeOne(Parent.class).getChild();

		// then
		then(actual.getName()).isEqualTo("high");
	}

	@RepeatedTest(10)
	void pathsDeclaredOnlyByOneOfSameTypeRegistrationsApply() {
		// when
		Child actual = SUT.giveMeOne(Parent.class).getChild();

		// then
		then(actual.getTag()).isEqualTo("lowTag");
		then(actual.getCount()).isEqualTo(5);
	}

	@RepeatedTest(10)
	void rootContainerValueKeepsItsElementsOverRegisteredElementType() {
		// given
		List<Child> expected = Arrays.asList(new Child("a", "t", 1), new Child("b", "u", 2));

		// when
		List<Child> actual = SUT.giveMeBuilder(new TypeReference<List<Child>>() {
			})
			.set("$", expected)
			.sample();

		// then
		then(actual).isEqualTo(expected);
	}

	@Data
	public static class Parent {
		private Child child;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Child {
		private String name;
		private String tag;
		private int count;
	}
}
