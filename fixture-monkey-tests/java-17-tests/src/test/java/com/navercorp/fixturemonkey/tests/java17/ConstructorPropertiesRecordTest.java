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

import org.junit.jupiter.api.RepeatedTest;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.tests.java17.RecordTestSpecs.ContainerRecord;
import com.navercorp.fixturemonkey.tests.java17.RecordTestSpecs.DateTimeRecord;
import com.navercorp.fixturemonkey.tests.java17.RecordTestSpecs.ComplexContainerRecord;
import com.navercorp.fixturemonkey.tests.java17.RecordTestSpecs.JavaTypeRecord;

class ConstructorPropertiesRecordTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.build();

	@RepeatedTest(TEST_COUNT)
	void sampleJavaType() {
		JavaTypeRecord actual = SUT.giveMeOne(JavaTypeRecord.class);

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleDateTime() {
		DateTimeRecord actual = SUT.giveMeOne(DateTimeRecord.class);

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleContainer() {
		ContainerRecord actual = SUT.giveMeOne(ContainerRecord.class);

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleInterfaceContainer() {
		ComplexContainerRecord actual = SUT.giveMeOne(ComplexContainerRecord.class);

		then(actual).isNotNull();
	}
}
