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

import java.lang.reflect.Modifier;
import java.util.List;

import org.junit.jupiter.api.RepeatedTest;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.generator.InterfaceJavaMethodPropertyGenerator;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.javax.validation.plugin.JavaxValidationPlugin;
import com.navercorp.fixturemonkey.tests.TestEnvironment;
import com.navercorp.fixturemonkey.tests.java.AnonymousInstanceTestSpecs.AnnotatedInterface;
import com.navercorp.fixturemonkey.tests.java.AnonymousInstanceTestSpecs.ContainerInterface;
import com.navercorp.fixturemonkey.tests.java.AnonymousInstanceTestSpecs.GetterInterface;
import com.navercorp.fixturemonkey.tests.java.AnonymousInstanceTestSpecs.Interface;
import com.navercorp.fixturemonkey.tests.java.AnonymousInstanceTestSpecs.InterfaceWithConstant;
import com.navercorp.fixturemonkey.tests.java.AnonymousInstanceTestSpecs.InterfaceWithParams;

class AnonymousInstanceTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.defaultNotNull(true)
		.pushPropertyGenerator(
			new MatcherOperator<>(
				p -> Modifier.isInterface(Types.getActualType(p.getType()).getModifiers()),
				new InterfaceJavaMethodPropertyGenerator()
			)
		)
		.plugin(new JavaxValidationPlugin())
		.build();

	@RepeatedTest(TestEnvironment.TEST_COUNT)
	void sampleAnonymous() {
		Interface actual = SUT.giveMeOne(Interface.class);

		then(actual).isNotNull();
		then(actual.string()).isNotNull();
		then(actual.integer()).isNotNull();
	}

	@RepeatedTest(TestEnvironment.TEST_COUNT)
	void sampleAnonymousSetValue() {
		String expected = "test";

		String actual = SUT.giveMeBuilder(Interface.class)
			.set("string", expected)
			.sample()
			.string();

		then(actual).isEqualTo(expected);
	}

	@RepeatedTest(TestEnvironment.TEST_COUNT)
	void sampleAnonymousWithParamReturnsNullProperties() {
		InterfaceWithParams actual = SUT.giveMeOne(InterfaceWithParams.class);

		then(actual).isNotNull();
		then(actual.string("str")).isNull();
		then(actual.integer(1)).isNull();
	}

	@RepeatedTest(TestEnvironment.TEST_COUNT)
	void sampleAnonymousConstant() {
		String actual = SUT.giveMeOne(InterfaceWithConstant.class).value;

		then(actual).isEqualTo("constant");
	}

	@RepeatedTest(TestEnvironment.TEST_COUNT)
	void sampleAnonymousConstantSetNotWorks() {
		String actual = SUT.giveMeBuilder(InterfaceWithConstant.class)
			.set("value", "changed")
			.sample()
			.value;

		then(actual).isEqualTo("constant");
	}

	@RepeatedTest(TestEnvironment.TEST_COUNT)
	void sampleAnonymousContainer() {
		ContainerInterface actual = SUT.giveMeOne(ContainerInterface.class);

		then(actual.list()).isNotNull();
		then(actual.map()).isNotNull();
	}

	@RepeatedTest(TestEnvironment.TEST_COUNT)
	void sampleAnonymousContainerSetList() {
		List<String> actual = SUT.giveMeBuilder(ContainerInterface.class)
			.size("list", 3)
			.set("list[0]", "test")
			.sample()
			.list();

		then(actual).hasSize(3);
		then(actual.get(0)).isEqualTo("test");
	}

	@RepeatedTest(TestEnvironment.TEST_COUNT)
	void sampleAnonymousAnnotatedInterface() {
		String actual = SUT.giveMeOne(AnnotatedInterface.class)
			.string();

		then(actual).isNotEmpty();
	}

	@RepeatedTest(TestEnvironment.TEST_COUNT)
	void setGetterIsPropertyName() {
		String expected = "test";

		String actual = SUT.giveMeBuilder(GetterInterface.class)
			.set("value", expected)
			.sample()
			.getValue();

		then(actual).isEqualTo(expected);
	}

	@RepeatedTest(TestEnvironment.TEST_COUNT)
	void setGetterIsNotMethodName() {
		String notExpected = "test";

		String actual = SUT.giveMeBuilder(GetterInterface.class)
			.set("getValue", notExpected)
			.sample()
			.getValue();

		then(actual).isNotEqualTo(notExpected);
	}
}
