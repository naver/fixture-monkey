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

import static com.navercorp.fixturemonkey.api.expression.JavaGetterMethodPropertySelector.javaGetter;
import static com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT;
import static org.assertj.core.api.BDDAssertions.then;

import org.junit.jupiter.api.RepeatedTest;

import lombok.AllArgsConstructor;
import lombok.Getter;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;

class JavaGetterPropertyFieldNameResolverTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.build();

	@RepeatedTest(TEST_COUNT)
	void nonBooleanFieldWithIsPrefixReturns() {
		JavaGetterObject actual = SUT.giveMeBuilder(JavaGetterObject.class)
			.set(javaGetter(JavaGetterObject::getIsStatus), "javaGetterStringStatus")
			.sample();

		then(actual.getIsStatus()).isEqualTo("javaGetterStringStatus");
	}

	@RepeatedTest(TEST_COUNT)
	void primitiveTypeBooleanFieldWithIsPrefixReturns() {
		JavaGetterObject actual = SUT.giveMeBuilder(JavaGetterObject.class)
			.set(javaGetter(JavaGetterObject::isActive), true)
			.sample();

		then(actual.isActive).isTrue();
	}

	@RepeatedTest(TEST_COUNT)
	void booleanFieldWithoutIsPrefixReturns() {
		JavaGetterObject actual = SUT.giveMeBuilder(JavaGetterObject.class)
			.set(javaGetter(JavaGetterObject::isEnabled), true)
			.sample();

		then(actual.isEnabled()).isTrue();
	}

	@RepeatedTest(TEST_COUNT)
	void nonBooleanFieldWithoutIsPrefixReturns() {
		JavaGetterObject actual = SUT.giveMeBuilder(JavaGetterObject.class)
			.set(javaGetter(JavaGetterObject::getName), "javaGetterObjectName")
			.sample();

		then(actual.getName()).isEqualTo("javaGetterObjectName");
	}

	@RepeatedTest(TEST_COUNT)
	void wrapperTypeBooleanFieldWithIsPrefixReturns() {
		JavaGetterObject actual = SUT.giveMeBuilder(JavaGetterObject.class)
			.set(javaGetter(JavaGetterObject::getIsDeleted), true)
			.sample();

		then(actual.isDeleted).isTrue();
	}

	@AllArgsConstructor
	@Getter
	private static class JavaGetterObject {
		private String isStatus;
		private boolean isActive;
		private boolean enabled;
		private String name;
		private Boolean isDeleted;
	}
}
