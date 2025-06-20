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

import java.util.List;

import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.NestedSimpleObject;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.StringPair;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.type.TypeReference;

class ValueProjectionPropertySelectorTest {

	private static final long SEED = 12345L;

	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.plugin(new JavaNodeTreeAdapterPlugin())
		.build();

	@Property
	void setAllFieldsWithWildcard() {
		StringPair actual = SUT.giveMeBuilder(StringPair.class).set("*", "str").sample();

		then(actual.getValue1()).isEqualTo("str");
		then(actual.getValue2()).isEqualTo("str");
	}

	@Property
	void wildcardDoesNotOverrideExplicitSet() {
		StringPair actual = SUT.giveMeBuilder(StringPair.class).set("*", "wildcard").set("value1", "explicit").sample();

		then(actual.getValue1()).isEqualTo("explicit");
		then(actual.getValue2()).isEqualTo("wildcard");
	}

	@Property
	void setNestedWildcard() {
		NestedSimpleObject actual = SUT.giveMeBuilder(NestedSimpleObject.class)
			.set("object.*", "nested-wildcard")
			.sample();

		then(actual.getObject().getStr()).isEqualTo("nested-wildcard");
	}

	@Property
	void wildcardWithContainerElements() {
		List<String> actual = SUT.giveMeBuilder(new TypeReference<List<String>>() {
			})
			.size("$", 3)
			.set("$[*]", "element")
			.sample();

		then(actual).hasSize(3);
		then(actual).containsOnly("element");
	}
}
