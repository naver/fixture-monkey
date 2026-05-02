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

import net.jqwik.api.Property;
import net.jqwik.api.PropertyDefaults;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.GenericSimpleChild;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.GenericStringChild;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.GenericStringIntChild;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.GenericStringWrapper;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.GenericValue;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.GenericWrapper;

@PropertyDefaults(tries = 10)
class GenericTypeAdapterTest {

	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.defaultNotNull(true)
		.plugin(new JavaNodeTreeAdapterPlugin())
		.build();

	@Property
	void sampleGeneric() {
		String actual = SUT.giveMeBuilder(new TypeReference<GenericValue<String>>() {
			})
			.setNotNull("value")
			.sample()
			.getValue();

		then(actual).isNotNull();
	}

	@Property
	void sampleGenericWildcardExtends() {
		String actual = SUT.giveMeBuilder(new TypeReference<GenericValue<? extends String>>() {
			})
			.setNotNull("value")
			.sample()
			.getValue();

		then(actual).isNotNull();
	}

	@Property
	void sampleStringGenericField() {
		GenericStringWrapper actual = SUT.giveMeOne(GenericStringWrapper.class);

		then(actual).isNotNull();
	}

	@Property
	void sampleGenericField() {
		GenericWrapper<String> actual = SUT.giveMeOne(new TypeReference<GenericWrapper<String>>() {
		});

		then(actual).isNotNull();
	}

	@Property
	void sampleGenericChild() {
		GenericStringChild actual = SUT.giveMeOne(GenericStringChild.class);

		then(actual).isNotNull();
	}

	@Property
	void sampleTwoGenericChild() {
		GenericStringIntChild actual = SUT.giveMeOne(GenericStringIntChild.class);

		then(actual).isNotNull();
	}

	@Property
	void sampleGenericSimpleChild() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(new TypeReference<GenericSimpleChild>() {
		}));
	}

	@Property
	void genericTypePropertyGeneratorResolvesTypeVariables() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();

		// when
		GenericValue<String> actual = sut.giveMeBuilder(new TypeReference<GenericValue<String>>() {
		}).sample();

		// then
		then(actual).isNotNull();
		then(actual.getValue()).isNotNull().isInstanceOf(String.class);
	}

}
