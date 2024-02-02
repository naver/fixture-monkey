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
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin;
import com.navercorp.fixturemonkey.javax.validation.plugin.JavaxValidationPlugin;
import com.navercorp.fixturemonkey.tests.java.AnonymousInstanceTestSpecs.AnnotatedInterface;
import com.navercorp.fixturemonkey.tests.java.AnonymousInstanceTestSpecs.ContainerInterface;
import com.navercorp.fixturemonkey.tests.java.AnonymousInstanceTestSpecs.GetterInterface;
import com.navercorp.fixturemonkey.tests.java.AnonymousInstanceTestSpecs.InheritedInterface;
import com.navercorp.fixturemonkey.tests.java.AnonymousInstanceTestSpecs.InheritedInterfaceWithSameNameMethod;
import com.navercorp.fixturemonkey.tests.java.AnonymousInstanceTestSpecs.InheritedTwoInterface;
import com.navercorp.fixturemonkey.tests.java.AnonymousInstanceTestSpecs.Interface;
import com.navercorp.fixturemonkey.tests.java.AnonymousInstanceTestSpecs.InterfaceWithConstant;
import com.navercorp.fixturemonkey.tests.java.AnonymousInstanceTestSpecs.InterfaceWithParams;
import com.navercorp.fixturemonkey.tests.java.AnonymousInstanceTestSpecs.NestedInheritedInterface;

class AnonymousInstanceTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.defaultNotNull(true)
		.plugin(new JavaxValidationPlugin())
		.plugin(
			new InterfacePlugin()
				.useAnonymousArbitraryIntrospector(true)
		)
		.build();

	@RepeatedTest(TEST_COUNT)
	void sampleInterface() {
		Interface actual = SUT.giveMeOne(Interface.class);

		then(actual).isNotNull();
		then(actual.string()).isNotNull();
		then(actual.integer()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void setInterface() {
		String expected = "test";

		String actual = SUT.giveMeBuilder(Interface.class)
			.set("string", expected)
			.sample()
			.string();

		then(actual).isEqualTo(expected);
	}

	@RepeatedTest(TEST_COUNT)
	void sampleInterfaceWithParamReturnsNullProperties() {
		InterfaceWithParams actual = SUT.giveMeOne(InterfaceWithParams.class);

		then(actual).isNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleInterfaceWithConstant() {
		String actual = SUT.giveMeOne(InterfaceWithConstant.class).value;

		then(actual).isEqualTo("constant");
	}

	@RepeatedTest(TEST_COUNT)
	void setConstantNotWorks() {
		String actual = SUT.giveMeBuilder(InterfaceWithConstant.class)
			.set("value", "changed")
			.sample()
			.value;

		then(actual).isEqualTo("constant");
	}

	@RepeatedTest(TEST_COUNT)
	void sampleContainerInterface() {
		ContainerInterface actual = SUT.giveMeOne(ContainerInterface.class);

		then(actual.list()).isNotNull();
		then(actual.map()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void setContainerInterfaceList() {
		List<String> actual = SUT.giveMeBuilder(ContainerInterface.class)
			.size("list", 3)
			.set("list[0]", "test")
			.sample()
			.list();

		then(actual).hasSize(3);
		then(actual.get(0)).isEqualTo("test");
	}

	@RepeatedTest(TEST_COUNT)
	void sampleAnnotatedInterface() {
		String actual = SUT.giveMeOne(AnnotatedInterface.class)
			.string();

		then(actual).isNotEmpty();
	}

	@RepeatedTest(TEST_COUNT)
	void setPropertyName() {
		String expected = "test";

		String actual = SUT.giveMeBuilder(GetterInterface.class)
			.set("value", expected)
			.sample()
			.getValue();

		then(actual).isEqualTo(expected);
	}

	@RepeatedTest(TEST_COUNT)
	void setMethodNameNotWorks() {
		String notExpected = "test";

		String actual = SUT.giveMeBuilder(GetterInterface.class)
			.set("getValue", notExpected)
			.sample()
			.getValue();

		then(actual).isNotEqualTo(notExpected);
	}

	@RepeatedTest(TEST_COUNT)
	void sampleInheritedInterface() {
		InheritedInterface actual = SUT.giveMeOne(InheritedInterface.class);

		then(actual.value()).isNotNull();
		then(actual.string()).isNotNull();
		then(actual.integer()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void setInheritedInterface() {
		String expected = "test";

		String actual = SUT.giveMeBuilder(InheritedInterface.class)
			.set("value", expected)
			.sample()
			.value();

		then(actual).isEqualTo(expected);
	}

	@RepeatedTest(TEST_COUNT)
	void sampleInheritedInterfaceWithSameNameMethod() {
		String actual = SUT.giveMeOne(InheritedInterfaceWithSameNameMethod.class).string();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void setInheritedInterfaceWithSameNameMethod() {
		String expected = "test";

		String actual = SUT.giveMeBuilder(InheritedInterfaceWithSameNameMethod.class)
			.set("string", expected)
			.sample()
			.string();

		then(actual).isEqualTo(expected);
	}

	@RepeatedTest(TEST_COUNT)
	void sampleInheritedTwoInterface() {
		InheritedTwoInterface actual = SUT.giveMeOne(InheritedTwoInterface.class);

		then(actual.integer()).isNotNull();
		then(actual.string()).isNotNull();
		then(actual.list()).isNotNull();
		then(actual.map()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleNestedInheritedInterface() {
		String actual = SUT.giveMeOne(NestedInheritedInterface.class).string();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void setNestedInheritedInterface() {
		String expected = "test";

		String actual = SUT.giveMeBuilder(NestedInheritedInterface.class)
			.set("string", expected)
			.sample()
			.string();

		then(actual).isEqualTo(expected);
	}

	@Test
	void sampleListWouldReturnDiff() {
		Set<Integer> actual = SUT.giveMeBuilder(Interface.class)
			.sampleList(2)
			.stream()
			.map(Interface::integer)
			.collect(Collectors.toSet());

		then(actual).hasSize(2);
	}
}
