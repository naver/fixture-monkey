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

import java.time.Instant;

import org.junit.jupiter.api.RepeatedTest;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.tests.java.specs.GenericTypeSpecs.GenericArrayObject;
import com.navercorp.fixturemonkey.tests.java.specs.GenericTypeSpecs.GenericImplementationObject;
import com.navercorp.fixturemonkey.tests.java.specs.GenericTypeSpecs.GenericObject;
import com.navercorp.fixturemonkey.tests.java.specs.GenericTypeSpecs.ThreeGenericObject;
import com.navercorp.fixturemonkey.tests.java.specs.GenericTypeSpecs.TwoGenericImplementationObject;
import com.navercorp.fixturemonkey.tests.java.specs.GenericTypeSpecs.TwoGenericObject;
import com.navercorp.fixturemonkey.tests.java.specs.ImmutableSpecs.SetImplementationWithoutGeneric;

@SuppressWarnings("rawtypes")
class GenericTypeTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();

	@RepeatedTest(TEST_COUNT)
	void sampleGenericObjectWithoutGeneric() {
		GenericObject actual = SUT.giveMeOne(GenericObject.class);

		then(actual).isNotNull();
		then(actual.getValue()).isInstanceOf(Object.class);
	}

	@RepeatedTest(TEST_COUNT)
	void fixedGenericObjectWithoutGeneric() {
		GenericObject actual = SUT.giveMeBuilder(GenericObject.class)
			.fixed()
			.sample();

		then(actual).isNotNull();
		then(actual.getValue()).isInstanceOf(Object.class);
	}

	@RepeatedTest(TEST_COUNT)
	void sampleGenericObject() {
		String actual = SUT.giveMeOne(new TypeReference<GenericObject<String>>() {
			})
			.getValue();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedGenericObject() {
		String actual = SUT.giveMeBuilder(new TypeReference<GenericObject<String>>() {
			})
			.fixed()
			.sample()
			.getValue();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleGenericImplementationObjectWithoutGeneric() {
		GenericImplementationObject actual = SUT.giveMeOne(GenericImplementationObject.class);

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedGenericImplementationObjectWithoutGeneric() {
		GenericImplementationObject actual = SUT.giveMeBuilder(GenericImplementationObject.class)
			.fixed()
			.sample();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleGenericImplementationObject() {
		String actual = SUT.giveMeOne(new TypeReference<GenericImplementationObject<String>>() {
			})
			.getValue();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedGenericImplementationObject() {
		String actual = SUT.giveMeBuilder(new TypeReference<GenericImplementationObject<String>>() {
			})
			.fixed()
			.sample()
			.getValue();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleTwoGenericImplementationObjectWithoutGeneric() {
		TwoGenericImplementationObject actual = SUT.giveMeOne(TwoGenericImplementationObject.class);

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedTwoGenericImplementationObjectWithoutGeneric() {
		TwoGenericImplementationObject actual = SUT.giveMeBuilder(TwoGenericImplementationObject.class)
			.fixed()
			.sample();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleTwoGenericImplementationObject() {
		TwoGenericImplementationObject<String, Integer> actual = SUT.giveMeOne(
			new TypeReference<TwoGenericImplementationObject<String, Integer>>() {
			}
		);

		then(actual.getUValue()).isNotNull();
		then(actual.getTValue()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedTwoGenericImplementationObject() {
		TwoGenericImplementationObject<String, Integer> actual = SUT.giveMeBuilder(
				new TypeReference<TwoGenericImplementationObject<String, Integer>>() {
				}
			)
			.fixed()
			.sample();

		then(actual.getUValue()).isNotNull();
		then(actual.getTValue()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleSetImplementationWithoutGeneric() {
		String actual = SUT.giveMeOne(SetImplementationWithoutGeneric.class)
			.getValue();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedSetImplementationWithoutGeneric() {
		String actual = SUT.giveMeBuilder(SetImplementationWithoutGeneric.class)
			.fixed()
			.sample()
			.getValue();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleGenericArrayType() {
		GenericImplementationObject<String>[] values = SUT.giveMeOne(new TypeReference<GenericArrayObject<String>>() {
			})
			.getValues();

		then(values).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedGenericArrayType() {
		GenericImplementationObject<String>[] values = SUT.giveMeBuilder(
				new TypeReference<GenericArrayObject<String>>() {
				})
			.fixed()
			.sample()
			.getValues();

		then(values).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleTwoGenericObject() {
		TwoGenericObject<String, Integer> actual = SUT.giveMeOne(
			new TypeReference<TwoGenericObject<String, Integer>>() {
			});

		then(actual.getValue1()).isNotNull();
		then(actual.getValue2()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedTwoGenericObject() {
		TwoGenericObject<String, Integer> actual = SUT.giveMeBuilder(
				new TypeReference<TwoGenericObject<String, Integer>>() {

				})
			.fixed()
			.sample();

		then(actual.getValue1()).isNotNull();
		then(actual.getValue2()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleTwoGenericObjectProperty() {
		TwoGenericObject<String, Integer> actual = SUT.giveMeOne(
				new TypeReference<GenericObject<TwoGenericObject<String, Integer>>>() {
				})
			.getValue();

		then(actual.getValue1()).isNotNull();
		then(actual.getValue2()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedTwoGenericObjectProperty() {
		TwoGenericObject<String, Integer> actual = SUT.giveMeOne(
				new TypeReference<GenericObject<TwoGenericObject<String, Integer>>>() {
				})
			.getValue();

		then(actual.getValue1()).isNotNull();
		then(actual.getValue2()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleThreeGenericObjectProperty() {
		ThreeGenericObject<String, Integer, Instant> actual = SUT.giveMeOne(
				new TypeReference<GenericObject<ThreeGenericObject<String, Integer, Instant>>>() {
				})
			.getValue();

		then(actual.getValue1()).isNotNull();
		then(actual.getValue2()).isNotNull();
		then(actual.getValue3()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedThreeGenericObjectProperty() {
		ThreeGenericObject<String, Integer, Instant> actual = SUT.giveMeOne(
				new TypeReference<GenericObject<ThreeGenericObject<String, Integer, Instant>>>() {
				})
			.getValue();

		then(actual.getValue1()).isNotNull();
		then(actual.getValue2()).isNotNull();
		then(actual.getValue3()).isNotNull();
	}
}
