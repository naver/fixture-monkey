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
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.customizer.Values;

class JustValueOrderTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();

	@RepeatedTest(10)
	void justValueIgnoresEarlierSize() {
		// when
		List<String> actual = SUT.giveMeBuilder(Holder.class)
			.size("values", 3)
			.set("values", Values.just(Arrays.asList("a")))
			.sample()
			.getValues();

		// then
		then(actual).containsExactly("a");
	}

	@RepeatedTest(10)
	void justValueIgnoresLaterSize() {
		// when
		List<String> actual = SUT.giveMeBuilder(Holder.class)
			.set("values", Values.just(Arrays.asList("a")))
			.size("values", 3)
			.sample()
			.getValues();

		// then
		then(actual).containsExactly("a");
	}

	@Test
	void justElementSetAfterContainerWins() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.build();
		for (int i = 0; i < 10; i++) {
			sut.giveMeBuilder(Holder.class).set("values", Arrays.asList("x")).sample();
		}

		// when
		List<String> actual = sut.giveMeBuilder(Holder.class)
			.set("values", Arrays.asList("a", "b"))
			.set("values[0]", Values.just("z"))
			.sample()
			.getValues();

		// then
		then(actual).containsExactly("z", "b");
	}

	@RepeatedTest(10)
	void containerSetAfterJustIsIgnored() {
		// when
		List<String> actual = SUT.giveMeBuilder(Holder.class)
			.set("values", Values.just(Arrays.asList("a")))
			.set("values", Arrays.asList("b", "c"))
			.sample()
			.getValues();

		// then
		then(actual).containsExactly("a");
	}

	@RepeatedTest(10)
	void containerLazyAfterJustIsIgnored() {
		// when
		List<String> actual = SUT.giveMeBuilder(Holder.class)
			.set("values", Values.just(Arrays.asList("a")))
			.setLazy("values", () -> Arrays.asList("b", "c"))
			.sample()
			.getValues();

		// then
		then(actual).containsExactly("a");
	}

	@RepeatedTest(10)
	void containerSetAfterEmptyJustIsIgnored() {
		// when
		List<String> actual = SUT.giveMeBuilder(Holder.class)
			.set("values", Values.just(Collections.emptyList()))
			.set("values", Arrays.asList("b"))
			.sample()
			.getValues();

		// then
		then(actual).isEmpty();
	}

	@RepeatedTest(10)
	void objectSetAfterJustIsIgnored() {
		// when
		Inner actual = SUT.giveMeBuilder(Holder.class)
			.set("inner", Values.just(new Inner("a")))
			.set("inner", new Inner("b"))
			.sample()
			.getInner();

		// then
		then(actual.getValue()).isEqualTo("a");
	}

	@RepeatedTest(10)
	void leafSetAfterJustWins() {
		// when
		String actual = SUT.giveMeBuilder(Holder.class)
			.set("value", Values.just("a"))
			.set("value", "b")
			.sample()
			.getValue();

		// then
		then(actual).isEqualTo("b");
	}

	@RepeatedTest(10)
	void setNullAfterJustWins() {
		// when
		List<String> actual = SUT.giveMeBuilder(Holder.class)
			.set("values", Values.just(Arrays.asList("a")))
			.setNull("values")
			.sample()
			.getValues();

		// then
		then(actual).isNull();
	}

	@RepeatedTest(10)
	void justAfterJustWins() {
		// when
		List<String> actual = SUT.giveMeBuilder(Holder.class)
			.set("values", Values.just(Arrays.asList("a")))
			.set("values", Values.just(Arrays.asList("j")))
			.sample()
			.getValues();

		// then
		then(actual).containsExactly("j");
	}

	@RepeatedTest(10)
	void justValueIgnoresLaterChildValue() {
		// given
		Inner just = new Inner("just");

		// when
		Inner actual = SUT.giveMeBuilder(Holder.class)
			.set("inner", Values.just(just))
			.set("inner.value", "child")
			.sample()
			.getInner();

		// then
		then(actual).isSameAs(just);
		then(actual.getValue()).isEqualTo("just");
	}

	@Data
	public static class Holder {
		private List<String> values;
		private String value;
		private Inner inner;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Inner {
		private String value;
	}
}
