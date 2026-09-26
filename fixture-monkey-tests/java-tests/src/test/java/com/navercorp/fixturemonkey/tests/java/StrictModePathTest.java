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
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;

class StrictModePathTest {
	private static final FixtureMonkey STRICT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.useExpressionStrictMode()
		.register(Child.class, fm -> fm.giveMeBuilder(Child.class).set("name", "registered"))
		.build();

	@Test
	void strictModeAcceptsIndexUnionPath() {
		// when
		List<String> actual = STRICT.giveMeBuilder(Holder.class)
			.size("first", 3)
			.set("first[0,1]", "fixed")
			.sample()
			.getFirst();

		// then
		then(actual.get(0)).isEqualTo("fixed");
		then(actual.get(1)).isEqualTo("fixed");
	}

	@Test
	void strictModeRejectsUnknownPathEvenWithRegisteredBuilder() {
		// when, then
		thenThrownBy(() -> STRICT.giveMeBuilder(Parent.class).set("child.unknown", "x").sample())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("child.unknown");
	}

	@Test
	void strictModeAcceptsPathInsideRegisteredType() {
		// when
		Child actual = STRICT.giveMeBuilder(Parent.class).set("child.name", "user").sample().getChild();

		// then
		then(actual.getName()).isEqualTo("user");
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
	}

	@Data
	public static class Holder {
		private List<String> first;
	}
}
