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

package com.navercorp.fixturemonkey.adapter;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenNoException;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.util.List;

import net.jqwik.api.Property;
import net.jqwik.api.PropertyDefaults;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.SimpleObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringWrapperList;

@PropertyDefaults(tries = 10)
class StrictModeAdapterTest {

	@Property
	void strictModeSetWrongExpressionThrows() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder().useExpressionStrictMode().build();

		// when
		// then
		thenThrownBy(() -> sut.giveMeBuilder(String.class).set("nonExistentField", 0).sample())
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("No matching results for given NodeResolvers.");
	}

	@Property
	void strictModeSizeWrongExpressionThrows() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder().useExpressionStrictMode().build();

		// when
		// then
		thenThrownBy(() -> sut.giveMeBuilder(StringWrapperList.class).size("nonExistentField", 1).sample())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("No matching results for given");
	}

	@Property
	void strictModeSizeNestedWrongExpressionThrows() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder().useExpressionStrictMode().build();

		// when
		// then
		thenThrownBy(() -> sut.giveMeBuilder(StringWrapperList.class).size("values.nonExistentField", 1).sample())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("No matching results for given");
	}

	@Property
	void strictModeSetWrongExpressionAfterPushAssignableTypePropertyNameResolverThrows() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.useExpressionStrictMode()
			.pushAssignableTypePropertyNameResolver(String.class, p -> "prop_" + p.getName())
			.build();

		// when
		// then
		thenThrownBy(() -> sut.giveMeBuilder(String.class).set("prop_non_existent_str", "test").sample())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("No matching results for given NodeResolvers.");
	}

	@Property
	void strictModeSizeWrongExpressionAfterPushAssignableTypePropertyNameResolverThrows() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.useExpressionStrictMode()
			.pushAssignableTypePropertyNameResolver(String.class, p -> "prop_" + p.getName())
			.build();

		// when
		// then
		thenThrownBy(() -> sut.giveMeBuilder(StringWrapperList.class).size("prop_non_existent_container", 1).sample())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("No matching results for given");
	}

	@Property
	void strictModeSetWrongExpressionAfterDefaultPropertyNameResolverThrows() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.useExpressionStrictMode()
			.defaultPropertyNameResolver(p -> "prop_" + p.getName())
			.build();

		// when
		// then
		thenThrownBy(() -> sut.giveMeBuilder(String.class).set("prop_non_existent_str", 1).sample())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("No matching results for given NodeResolvers.");
	}

	@Property
	void strictModeSizeWrongExpressionAfterDefaultPropertyNameResolverThrows() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.useExpressionStrictMode()
			.defaultPropertyNameResolver(p -> "prop_" + p.getName())
			.build();

		// when
		// then
		thenThrownBy(() -> sut.giveMeBuilder(StringWrapperList.class).size("prop_non_existent_container", 1).sample())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("No matching results for given");
	}

	@Property
	void notStrictModeSetWrongExpressionDoesNotThrows() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder().build();

		// when
		// then
		thenNoException().isThrownBy(() -> sut.giveMeBuilder(String.class).set("nonExistentField", 0).sample());
	}

	@Property
	void notStrictModeSizeWrongExpressionDoesNotThrows() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder().build();

		// when
		// then
		thenNoException().isThrownBy(() ->
			sut.giveMeBuilder(StringWrapperList.class).size("values.nonExistentField", 1).sample()
		);
	}

	@Property
	void strictModeMultiOperationValidExpression() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder().useExpressionStrictMode().build();

		// when
		String actual = sut
			.giveMeBuilder(new TypeReference<List<List<SimpleObject>>>() {
			})
			.size("$", 1)
			.size("$[0]", 1)
			.set("$[0][0].str", "expected")
			.sample()
			.get(0)
			.get(0)
			.getStr();

		// then
		then(actual).isEqualTo("expected");
	}
}
