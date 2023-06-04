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

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Date;

import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import com.navercorp.fixturemonkey.javax.validation.plugin.JavaxValidationPlugin;

public class ValidationPluginCompatibilityTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.plugin(new JavaxValidationPlugin())
		.plugin(new JakartaValidationPlugin())
		.defaultNotNull(true)
		.build();

	@Property(tries = 100)
	void sampleInt() {
		JavaxValidationTestSpecs.IntObject javaxIntObject = SUT.giveMeOne(JavaxValidationTestSpecs.IntObject.class);
		JakartaValidationTestSpecs.IntObject jakartaIntObject = SUT.giveMeOne(
			JakartaValidationTestSpecs.IntObject.class
		);

		then(javaxIntObject.getMinValue()).isGreaterThanOrEqualTo(100);
		then(jakartaIntObject.getMinValue()).isGreaterThanOrEqualTo(100);
	}

	@Property(tries = 100)
	void sampleDate() {
		JavaxValidationTestSpecs.DateObject javaxDateObject = SUT.giveMeOne(JavaxValidationTestSpecs.DateObject.class);
		JakartaValidationTestSpecs.DateObject jakartaDateObject = SUT.giveMeOne(
			JakartaValidationTestSpecs.DateObject.class
		);

		Date now = new Date();
		then(javaxDateObject.getDatePast().getTime()).isLessThan(now.getTime());
		then(jakartaDateObject.getDatePast().getTime()).isLessThan(now.getTime());
	}

	@Property(tries = 100)
	void sampleContainer() {
		JavaxValidationTestSpecs.ContainerObject javaxContainerObject = SUT.giveMeOne(
			JavaxValidationTestSpecs.ContainerObject.class
		);
		JakartaValidationTestSpecs.ContainerObject jakartaContainerObject = SUT.giveMeOne(
			JakartaValidationTestSpecs.ContainerObject.class
		);

		then(javaxContainerObject.getStrList()).hasSizeBetween(5, 10);
		then(jakartaContainerObject.getStrList()).hasSizeBetween(5, 10);
	}

	@Property(tries = 100)
	void sampleBoolean() {
		JavaxValidationTestSpecs.BooleanObject javaxBooleanObject = SUT.giveMeOne(
			JavaxValidationTestSpecs.BooleanObject.class
		);
		JakartaValidationTestSpecs.BooleanObject jakartaBooleanObject = SUT.giveMeOne(
			JakartaValidationTestSpecs.BooleanObject.class
		);

		then(javaxBooleanObject.getBool()).isTrue();
		then(jakartaBooleanObject.getBool()).isTrue();
	}
}
