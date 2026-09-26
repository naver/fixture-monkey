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

import java.util.List;

import org.junit.jupiter.api.RepeatedTest;

import lombok.Data;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;

class WildcardPathTest {
	private static final FixtureMonkey USER = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();

	@RepeatedTest(10)
	void setFieldWildcardAppliesToEveryField() {
		// when
		Child actual = USER.giveMeBuilder(Parent.class).set("child.*", "fixed").sample().getChild();

		// then
		then(actual.getName()).isEqualTo("fixed");
		then(actual.getNick()).isEqualTo("fixed");
	}

	@RepeatedTest(10)
	void registeredSetFieldWildcardAppliesToEveryField() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(Child.class, fm -> fm.giveMeBuilder(Child.class).set("*", "fixed"))
			.build();

		// when
		Child actual = sut.giveMeOne(Parent.class).getChild();

		// then
		then(actual.getName()).isEqualTo("fixed");
		then(actual.getNick()).isEqualTo("fixed");
	}

	@RepeatedTest(10)
	void setIndexUnionAppliesToEachIndex() {
		// when
		List<String> actual = USER.giveMeBuilder(Outer.class)
			.size("holder.first", 3)
			.set("holder.first[0,1]", "fixed")
			.sample()
			.getHolder()
			.getFirst();

		// then
		then(actual).hasSize(3);
		then(actual.get(0)).isEqualTo("fixed");
		then(actual.get(1)).isEqualTo("fixed");
	}

	@RepeatedTest(10)
	void registeredSetIndexUnionAppliesToEachIndex() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				Holder.class,
				fm -> fm.giveMeBuilder(Holder.class).size("first", 3).set("first[0,1]", "fixed")
			)
			.build();

		// when
		List<String> actual = sut.giveMeOne(Outer.class).getHolder().getFirst();

		// then
		then(actual).hasSize(3);
		then(actual.get(0)).isEqualTo("fixed");
		then(actual.get(1)).isEqualTo("fixed");
	}

	@RepeatedTest(10)
	void sizeFieldWildcardAppliesToEveryContainer() {
		// when
		Holder actual = USER.giveMeBuilder(Outer.class).size("holder.*", 4).sample().getHolder();

		// then
		then(actual.getFirst()).hasSize(4);
		then(actual.getSecond()).hasSize(4);
	}

	@RepeatedTest(10)
	void registeredSizeFieldWildcardAppliesToEveryContainer() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(Holder.class, fm -> fm.giveMeBuilder(Holder.class).size("*", 4))
			.build();

		// when
		Holder actual = sut.giveMeOne(Outer.class).getHolder();

		// then
		then(actual.getFirst()).hasSize(4);
		then(actual.getSecond()).hasSize(4);
	}

	@RepeatedTest(10)
	void userWildcardPostConditionAppliesToEveryElement() {
		// when
		List<String> actual = USER.giveMeBuilder(ValuesHolder.class)
			.size("values", 5)
			.setPostCondition("values[*]", String.class, it -> it.length() < 2)
			.sample()
			.getValues();

		// then
		then(actual).hasSize(5).allSatisfy(it -> then(it).hasSizeLessThan(2));
	}

	@RepeatedTest(10)
	void registeredWildcardPostConditionAppliesToEveryElement() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				ValuesHolder.class,
				fm -> fm.giveMeBuilder(ValuesHolder.class)
					.size("values", 5)
					.setPostCondition("values[*]", String.class, it -> it.length() < 2)
			)
			.build();

		// when
		List<String> actual = sut.giveMeOne(ValuesWrapper.class).getHolder().getValues();

		// then
		then(actual).hasSize(5).allSatisfy(it -> then(it).hasSizeLessThan(2));
	}

	@Data
	public static class Parent {
		private Child child;
	}

	@Data
	public static class Child {
		private String name;
		private String nick;
	}

	@Data
	public static class Outer {
		private Holder holder;
	}

	@Data
	public static class Holder {
		private List<String> first;
		private List<String> second;
	}

	@Data
	public static class ValuesWrapper {
		private ValuesHolder holder;
	}

	@Data
	public static class ValuesHolder {
		private List<String> values;
	}
}
