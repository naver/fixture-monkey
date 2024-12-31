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
import static org.assertj.core.api.BDDAssertions.thenCode;

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
	void testNonBooleanFieldWithIsPrefix() {
		thenCode(SUT.giveMeBuilder(JavaGetterObject.class)
			.set(javaGetter(JavaGetterObject::getIsStatus), "javaGetterStringStatus")::sample)
			.doesNotThrowAnyException();
	}

	@RepeatedTest(TEST_COUNT)
	void testPrimitiveTypeBooleanFieldWithIsPrefix() {
		thenCode(SUT.giveMeBuilder(JavaGetterObject.class)
			.set(javaGetter(JavaGetterObject::isActive), true)::sample)
			.doesNotThrowAnyException();
	}

	@RepeatedTest(TEST_COUNT)
	void testBooleanFieldWithoutIsPrefix() {
		thenCode(SUT.giveMeBuilder(JavaGetterObject.class)
			.set(javaGetter(JavaGetterObject::isEnabled), true)::sample)
			.doesNotThrowAnyException();
	}

	@RepeatedTest(TEST_COUNT)
	void testNonBooleanFieldWithoutIsPrefix() {
		thenCode(SUT.giveMeBuilder(JavaGetterObject.class)
			.set(javaGetter(JavaGetterObject::getName), "javaGetterObjectName")::sample)
			.doesNotThrowAnyException();
	}

	@RepeatedTest(TEST_COUNT)
	void testWrapperTypeBooleanFieldWithIsPrefix() {
		thenCode(SUT.giveMeBuilder(JavaGetterObject.class)
			.set(javaGetter(JavaGetterObject::getIsDeleted), true)::sample)
			.doesNotThrowAnyException();
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
