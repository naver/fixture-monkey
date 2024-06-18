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
import static org.assertj.core.api.BDDAssertions.thenNoException;

import org.junit.jupiter.api.RepeatedTest;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.tests.java17.RecordTestSpecs.EnumClass;
import com.navercorp.fixturemonkey.tests.java17.SealedClassTestSpecs.SealedClass;
import com.navercorp.fixturemonkey.tests.java17.SealedClassTestSpecs.SealedClassProperty;
import com.navercorp.fixturemonkey.tests.java17.SealedClassTestSpecs.SealedInterface;
import com.navercorp.fixturemonkey.tests.java17.SealedClassTestSpecs.SealedClassImpl;
import com.navercorp.fixturemonkey.tests.java17.SealedClassTestSpecs.SealedInterfaceImpl;

class SealedClassTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();

	@RepeatedTest(TEST_COUNT)
	void sampleSealedClass() {
		// when
		SealedClass actual = SUT.giveMeOne(SealedClass.class);

		then(actual).isInstanceOf(SealedClassImpl.class);
	}

	@RepeatedTest(TEST_COUNT)
	void sampleSealedInterface() {
		// when
		SealedInterface actual = SUT.giveMeOne(SealedInterface.class);

		then(actual).isInstanceOf(SealedInterfaceImpl.class);
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

	@RepeatedTest(TEST_COUNT)
	void sampleEnum() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(EnumClass.class));
	}
}
