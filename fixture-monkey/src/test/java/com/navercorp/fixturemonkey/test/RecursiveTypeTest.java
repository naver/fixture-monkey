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

package com.navercorp.fixturemonkey.test;

import static org.assertj.core.api.BDDAssertions.then;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.RecursiveLeftObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.SelfRecursiveListObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.SelfRecursiveObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.SelfRecursiveSupplierObject;

class RecursiveTypeTest {

	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.defaultNotNull(true)
		.build();

	@Test
	void sampleSelfRecursiveObject() {
		SelfRecursiveObject actual = SUT.giveMeOne(SelfRecursiveObject.class);

		then(actual.getValue()).isNotNull();
		then(actual.getRecursive()).isNotNull();
		then(actual.getRecursive().getValue()).isNotNull();
	}

	@Test
	void sampleSelfRecursiveList() {
		SelfRecursiveListObject actual = SUT.giveMeOne(SelfRecursiveListObject.class);

		then(actual.getValue()).isNotNull();
		then(actual.getRecursives()).isNotNull();
	}

	@Test
	void sampleRecursiveSupplier() {
		SelfRecursiveSupplierObject actual = SUT.giveMeOne(SelfRecursiveSupplierObject.class);

		then(actual.getValue()).isNotNull();
		then(actual.getRecursive()).isNotNull();
		then(actual.getRecursive().get()).isNotNull();
	}

	@Test
	void sampleRecursiveObject() {
		RecursiveLeftObject actual = SUT.giveMeOne(RecursiveLeftObject.class);

		then(actual.getValue()).isNotNull();
		then(actual.getRecursive()).isNotNull();
		then(actual.getRecursive().getValue()).isNotNull();
	}

	@Test
	void setRecursiveFieldValue() {
		// given
		SelfRecursiveObject actual = SUT.giveMeBuilder(SelfRecursiveObject.class)
			.set("recursive.value", "test")
			.sample();

		// then
		then(actual.getRecursive()).isNotNull();
		then(actual.getRecursive().getValue()).isEqualTo("test");
	}

	@Test
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
