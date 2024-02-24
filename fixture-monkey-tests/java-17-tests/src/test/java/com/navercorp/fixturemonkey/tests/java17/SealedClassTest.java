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

package com.navercorp.fixturemonkey.tests.java17;

import static com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT;
import static org.assertj.core.api.BDDAssertions.then;

import java.beans.ConstructorProperties;

import org.junit.jupiter.api.RepeatedTest;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;

class SealedClassTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();

	private static sealed class SealedClass permits SealedClassImpl {
	}

	private static final class SealedClassImpl extends SealedClass {
		private final String value;

		@ConstructorProperties("value")
		public SealedClassImpl(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	@RepeatedTest(TEST_COUNT)
	void sampleSealedClass() {
		// when
		SealedClass actual = SUT.giveMeOne(SealedClass.class);

		then(actual).isInstanceOf(SealedClassImpl.class);
	}

	private sealed interface SealedInterface permits SealedInterfaceImpl {
	}

	private record SealedInterfaceImpl(String value) implements SealedInterface {
	}

	@RepeatedTest(TEST_COUNT)
	void sampleSealedInterface() {
		// when
		SealedInterface actual = SUT.giveMeOne(SealedInterface.class);

		then(actual).isInstanceOf(SealedInterface.class);
	}

	private record SealedClassProperty(
		SealedClass sealedClass,
		SealedInterface sealedInterface
	) {
	}

	@RepeatedTest(TEST_COUNT)
	void sampleSealedClassProperty() {
		// when
		SealedClassProperty actual = SUT.giveMeOne(SealedClassProperty.class);

		then(actual.sealedInterface()).isInstanceOf(SealedInterfaceImpl.class);
		then(actual.sealedClass()).isInstanceOf(SealedClassImpl.class);
	}

	@RepeatedTest(TEST_COUNT)
	void fixedSealedClassProperty() {
		// when
		SealedClassProperty actual = SUT.giveMeBuilder(SealedClassProperty.class)
			.fixed()
			.sample();

		then(actual.sealedInterface()).isInstanceOf(SealedInterfaceImpl.class);
		then(actual.sealedClass()).isInstanceOf(SealedClassImpl.class);
	}
}
