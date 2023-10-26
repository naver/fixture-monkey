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

import org.junit.jupiter.api.RepeatedTest;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.javax.validation.plugin.JavaxValidationPlugin;
import com.navercorp.fixturemonkey.tests.java.ValidationAnnotationTestSpecs.CustomAnnotationStringObject;
import com.navercorp.fixturemonkey.tests.java.ValidationAnnotationTestSpecs.StringNotNullAnnotationObject;

class AnnotationTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.plugin(new JavaxValidationPlugin())
		.build();

	@RepeatedTest(TEST_COUNT)
	void sampleCustomAnnotation() {
		String actual = SUT.giveMeOne(CustomAnnotationStringObject.class)
			.getNullOrLessThan5String();

		then(actual).matches(it -> it == null || it.length() < 5);
	}

	@RepeatedTest(TEST_COUNT)
	void sampleNotValidAnnotation() {
		String actual = SUT.giveMeBuilder(StringNotNullAnnotationObject.class)
			.set("value", null)
			.validOnly(false)
			.sample()
			.getValue();

		then(actual).isNull();
	}
}
