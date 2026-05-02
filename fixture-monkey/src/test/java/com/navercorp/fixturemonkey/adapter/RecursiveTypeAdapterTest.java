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

import net.jqwik.api.Property;
import net.jqwik.api.PropertyDefaults;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.RecursiveLeftObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.SelfRecursiveListObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.SelfRecursiveObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.SelfRecursiveSupplierObject;

@PropertyDefaults(tries = 10)
class RecursiveTypeAdapterTest {

	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.defaultNotNull(true)
		.plugin(new JavaNodeTreeAdapterPlugin())
		.build();

	@Property
	void sampleSelfRecursiveObject() {
		SelfRecursiveObject actual = SUT.giveMeOne(SelfRecursiveObject.class);

		then(actual.getValue()).isNotNull();
		then(actual.getRecursive()).isNotNull();
		then(actual.getRecursive().getValue()).isNotNull();
	}

	@Property
	void sampleSelfRecursiveList() {
		SelfRecursiveListObject actual = SUT.giveMeOne(SelfRecursiveListObject.class);

		then(actual.getValue()).isNotNull();
		then(actual.getRecursives()).isNotNull();
	}

	@Property
	void sampleRecursiveSupplier() {
		SelfRecursiveSupplierObject actual = SUT.giveMeOne(SelfRecursiveSupplierObject.class);

		then(actual.getValue()).isNotNull();
		then(actual.getRecursive()).isNotNull();
		then(actual.getRecursive().get()).isNotNull();
	}

	@Property
	void sampleRecursiveObject() {
		RecursiveLeftObject actual = SUT.giveMeOne(RecursiveLeftObject.class);

		then(actual.getValue()).isNotNull();
		then(actual.getRecursive()).isNotNull();
		then(actual.getRecursive().getValue()).isNotNull();
	}

	@Property
	void setRecursiveFieldValue() {
		// given
		SelfRecursiveObject actual = SUT.giveMeBuilder(SelfRecursiveObject.class)
			.set("recursive.value", "test")
			.sample();

		// then
		then(actual.getRecursive()).isNotNull();
		then(actual.getRecursive().getValue()).isEqualTo("test");
	}

	@Property
	void setDeepRecursiveValue() {
		// given
		SelfRecursiveObject actual = SUT.giveMeBuilder(SelfRecursiveObject.class)
			.set("recursive.recursive.value", "deep")
			.sample();

		// then
		then(actual).isNotNull();
		then(actual.getRecursive()).as("first recursive").isNotNull();
		then(actual.getRecursive().getRecursive()).as("second recursive").isNotNull();
		then(actual.getRecursive().getRecursive().getValue()).as("value").isEqualTo("deep");
	}

}
