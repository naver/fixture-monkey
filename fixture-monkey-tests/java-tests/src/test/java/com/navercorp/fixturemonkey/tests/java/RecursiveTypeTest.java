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

import static com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.RepeatedTest;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.tests.java.specs.RecursiveTypeSpecs.SelfRecursiveListObject;
import com.navercorp.fixturemonkey.tests.java.specs.RecursiveTypeSpecs.SelfRecursiveMapObject;
import com.navercorp.fixturemonkey.tests.java.specs.RecursiveTypeSpecs.SelfRecursiveObject;

class RecursiveTypeTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();

	@RepeatedTest(TEST_COUNT)
	void sampleSelfRecursiveObject() {
		SelfRecursiveObject actual = SUT.giveMeOne(SelfRecursiveObject.class);

		then(actual.getValue()).isNotNull();
		then(actual.getSelfRecursiveObject()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedSelfRecursiveObject() {
		SelfRecursiveObject actual = SUT.giveMeBuilder(SelfRecursiveObject.class)
			.fixed()
			.sample();

		then(actual.getValue()).isNotNull();
		then(actual.getSelfRecursiveObject()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleSelfRecursiveListObject() {
		SelfRecursiveListObject actual = SUT.giveMeOne(SelfRecursiveListObject.class);

		then(actual.getValue()).isNotNull();
		then(actual.getSelfRecursiveListObjects()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedSelfRecursiveListObject() {
		SelfRecursiveListObject actual = SUT.giveMeBuilder(SelfRecursiveListObject.class)
			.fixed()
			.sample();

		then(actual.getValue()).isNotNull();
		then(actual.getSelfRecursiveListObjects()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleSelfRecursiveMapObject() {
		Map<Integer, SelfRecursiveMapObject> actual = SUT.giveMeOne(
			new TypeReference<Map<Integer, SelfRecursiveMapObject>>() {
			});

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedSelfRecursiveMapObject() {
		Map<Integer, SelfRecursiveMapObject> actual = SUT.giveMeBuilder(
				new TypeReference<Map<Integer, SelfRecursiveMapObject>>() {
				})
			.fixed()
			.sample();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void setSelfRecursiveObjectList() {
		List<SelfRecursiveListObject> expected = SUT.giveMeOne(
			new TypeReference<List<SelfRecursiveListObject>>() {
			});

		List<SelfRecursiveListObject> actual = SUT.giveMeBuilder(new TypeReference<List<SelfRecursiveListObject>>() {
			})
			.size("$", 1)
			.set("$[0].selfRecursiveListObjects", expected)
			.sample()
			.get(0)
			.getSelfRecursiveListObjects();

		then(actual).isEqualTo(expected);
	}

	@RepeatedTest(TEST_COUNT)
	void setNestedSelfRecursiveObjectList() {
		List<SelfRecursiveListObject> expected = SUT.giveMeBuilder(
				new TypeReference<List<SelfRecursiveListObject>>() {
				}
			)
			.size("$", 1)
			.set("$[0].selfRecursiveListObjects", SUT.giveMeOne(new TypeReference<List<SelfRecursiveListObject>>() {
			}))
			.sample();

		List<SelfRecursiveListObject> actual = SUT.giveMeBuilder(new TypeReference<List<SelfRecursiveListObject>>() {
			})
			.size("$", 1)
			.set("$[0].selfRecursiveListObjects", expected)
			.sample()
			.get(0)
			.getSelfRecursiveListObjects();

		then(actual).isEqualTo(expected);
	}

	@RepeatedTest(TEST_COUNT)
	void setSelfRecursiveObject() {
		SelfRecursiveObject actual = SUT.giveMeOne(SelfRecursiveObject.class);

		SelfRecursiveObject expected = SUT.giveMeBuilder(SelfRecursiveObject.class)
			.set("selfRecursiveObject", actual)
			.sample()
			.getSelfRecursiveObject();

		then(actual).isEqualTo(expected);
	}
}
