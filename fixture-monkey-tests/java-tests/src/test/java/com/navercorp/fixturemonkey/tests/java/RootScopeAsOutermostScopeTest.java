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

import static com.navercorp.fixturemonkey.api.experimental.TypedExpressionGenerator.typedString;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;

import org.junit.jupiter.api.RepeatedTest;

import lombok.Data;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.FixtureMonkeyBuilder;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;

class RootScopeAsOutermostScopeTest {
	@RepeatedTest(10)
	void rootValueWinsOverScopeSelectingSampledRoot() {
		// given
		FixtureMonkey sut = fixtureMonkey()
			.register(Outer.class, fm -> fm.giveMeBuilder(Outer.class).set("name", "defined"))
			.build();

		// when
		String actual = sut.giveMeBuilder(Outer.class).set("name", "root").sample().getName();

		// then
		then(actual).isEqualTo("root");
	}

	@RepeatedTest(10)
	void rootValueWinsOverInnerScope() {
		// given
		FixtureMonkey sut = fixtureMonkey()
			.register(Inner.class, fm -> fm.giveMeBuilder(Inner.class).set("name", "defined"))
			.build();

		// when
		String actual = sut.giveMeBuilder(Outer.class).set("inner.name", "root").sample().getInner().getName();

		// then
		then(actual).isEqualTo("root");
	}

	@RepeatedTest(10)
	void scopeSelectingSampledRootCustomizerSkipsRootValue() {
		// given
		FixtureMonkey sut = fixtureMonkey()
			.register(
				Outer.class,
				fm -> fm.giveMeBuilder(Outer.class)
					.<String>customizeProperty(typedString("name"), it -> it.map(name -> name + "!"))
			)
			.build();

		// when
		String actual = sut.giveMeBuilder(Outer.class).set("name", "root").sample().getName();

		// then
		then(actual).isEqualTo("root");
	}

	@RepeatedTest(10)
	void innerScopeCustomizerSkipsRootValue() {
		// given
		FixtureMonkey sut = fixtureMonkey()
			.register(
				Inner.class,
				fm -> fm.giveMeBuilder(Inner.class)
					.<String>customizeProperty(typedString("name"), it -> it.map(name -> name + "!"))
			)
			.build();

		// when
		String actual = sut.giveMeBuilder(Outer.class).set("inner.name", "root").sample().getInner().getName();

		// then
		then(actual).isEqualTo("root");
	}

	@RepeatedTest(10)
	void rootCustomizerAppliesToValueOfScopeSelectingSampledRoot() {
		// given
		FixtureMonkey sut = fixtureMonkey()
			.register(Outer.class, fm -> fm.giveMeBuilder(Outer.class).set("name", "defined"))
			.build();

		// when
		String actual = sut.giveMeBuilder(Outer.class)
			.<String>customizeProperty(typedString("name"), it -> it.map(name -> name + "!"))
			.sample()
			.getName();

		// then
		then(actual).isEqualTo("defined!");
	}

	@RepeatedTest(10)
	void rootNotNullWinsOverNullOfScopeSelectingSampledRoot() {
		// given
		FixtureMonkey sut = fixtureMonkey()
			.register(Outer.class, fm -> fm.giveMeBuilder(Outer.class).setNull("name"))
			.build();

		// when
		String actual = sut.giveMeBuilder(Outer.class).setNotNull("name").sample().getName();

		// then
		then(actual).isNotNull();
	}

	@RepeatedTest(10)
	void rootNullWinsOverNotNullOfScopeSelectingSampledRoot() {
		// given
		FixtureMonkey sut = fixtureMonkey()
			.register(Outer.class, fm -> fm.giveMeBuilder(Outer.class).setNotNull("name"))
			.build();

		// when
		String actual = sut.giveMeBuilder(Outer.class).setNull("name").sample().getName();

		// then
		then(actual).isNull();
	}

	@RepeatedTest(10)
	void rootLimitAndScopeLimitAreCountedSeparately() {
		// given
		FixtureMonkey sut = fixtureMonkey()
			.register(Inner.class, fm -> fm.giveMeBuilder(Inner.class).set("name", "defined"))
			.build();

		// when
		List<Inner> actual = sut.giveMeBuilder(Outer.class)
			.size("inners", 3)
			.set("inners[*].name", "root", 1)
			.sample()
			.getInners();

		// then
		then(actual.get(0).getName()).isEqualTo("root");
		then(actual.get(1).getName()).isEqualTo("defined");
		then(actual.get(2).getName()).isEqualTo("defined");
	}

	private static FixtureMonkeyBuilder fixtureMonkey() {
		return FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true);
	}

	@Data
	public static class Outer {
		private String name;
		private Inner inner;
		private List<Inner> inners;
	}

	@Data
	public static class Inner {
		private String name;
	}
}
