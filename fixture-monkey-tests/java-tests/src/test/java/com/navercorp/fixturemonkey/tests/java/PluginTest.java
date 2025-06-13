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
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin;
import com.navercorp.fixturemonkey.api.plugin.SimpleValueJqwikPlugin;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.javax.validation.plugin.JavaxValidationPlugin;
import com.navercorp.fixturemonkey.tests.java.specs.ConstructorSpecs.JavaxValidationObject;
import com.navercorp.fixturemonkey.tests.java.specs.InterfaceSpecs.AbstractClassObject;
import com.navercorp.fixturemonkey.tests.java.specs.InterfaceSpecs.AbstractClassStringChildObject;
import com.navercorp.fixturemonkey.tests.java.specs.InterfaceSpecs.InterfaceIntegerObject;
import com.navercorp.fixturemonkey.tests.java.specs.InterfaceSpecs.InterfaceListObject;
import com.navercorp.fixturemonkey.tests.java.specs.InterfaceSpecs.InterfaceObject;
import com.navercorp.fixturemonkey.tests.java.specs.InterfaceSpecs.InterfaceStringObject;
import com.navercorp.fixturemonkey.tests.java.specs.InterfaceSpecs.InterfaceWrapperObject;

class PluginTest {
	@RepeatedTest(TEST_COUNT)
	void setListRecursiveImplementations() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.plugin(new InterfacePlugin()
				.interfaceImplements(
					InterfaceObject.class,
					Arrays.asList(
						InterfaceStringObject.class,
						InterfaceIntegerObject.class,
						InterfaceListObject.class)))
			.build();

		List<InterfaceObject> element = sut.giveMeOne(
				new com.navercorp.fixturemonkey.api.type.TypeReference<List<InterfaceStringObject>>() {
				}).stream()
			.map(InterfaceObject.class::cast)
			.collect(Collectors.toList());
		InterfaceListObject expected = new InterfaceListObject(element);

		// when
		InterfaceWrapperObject actual = sut.giveMeBuilder(InterfaceWrapperObject.class)
			.set("value", expected)
			.sample();

		then(actual.getValue()).isEqualTo(expected);
	}

	@RepeatedTest(TEST_COUNT)
	void constructorValidator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JavaxValidationPlugin())
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.build();

		JavaxValidationObject actual = sut.giveMeOne(JavaxValidationObject.class);

		then(actual.getValue()).isEqualTo(100);
	}

	@RepeatedTest(TEST_COUNT)
	void abstractClassExtends() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(
				new InterfacePlugin()
					.abstractClassExtends(
						AbstractClassObject.class,
						Collections.singletonList(AbstractClassStringChildObject.class)))
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.build();

		AbstractClassObject actual = sut.giveMeOne(AbstractClassObject.class);

		then(actual).isExactlyInstanceOf(AbstractClassStringChildObject.class);
	}

	@Test
	void abstractExtendsInterfaceThrows() {
		thenThrownBy(
			() -> FixtureMonkey.builder()
				.plugin(
					new InterfacePlugin()
						.abstractClassExtends(
							InterfaceObject.class,
							Collections.singletonList(InterfaceStringObject.class)))
				.build())
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("should be abstract class");
	}

	@Test
	void interfaceImplementsAbstractClassThrows() {
		thenThrownBy(
			() -> FixtureMonkey.builder()
				.plugin(
					new InterfacePlugin()
						.interfaceImplements(
							AbstractClassObject.class,
							Collections.singletonList(AbstractClassStringChildObject.class)))
				.build())
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("should be interface");
	}

	@Test
	void simpleValueJqwikPluginMinNumberValueGreaterThanDefault() {
		// given
		long expected = 10001L;
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new SimpleValueJqwikPlugin()
				.minNumberValue(expected))
			.build();

		// when
		long actual = sut.giveMeOne(Long.class);

		then(actual).isGreaterThanOrEqualTo(expected);
	}

	@Test
	void simpleValueJqwikPluginMinStringLengthGreaterThanDefault() {
		// given
		long expected = 10L;
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new SimpleValueJqwikPlugin()
				.minStringLength(expected))
			.build();

		// when
		String actual = sut.giveMeOne(String.class);

		// then
		then(actual.length()).isGreaterThanOrEqualTo((int)expected);
	}

	@Test
	void simpleValueJqwikPluginMinContainerSizeGreaterThanDefault() {
		// given
		int expected = 5;
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new SimpleValueJqwikPlugin()
				.minContainerSize(expected))
			.build();

		// when
		List<String> actual = sut.giveMeOne(new TypeReference<List<String>>() {
		});

		// then
		then(actual.size()).isGreaterThanOrEqualTo(expected);
	}

	@Test
	void simpleValueJqwikPluginMinusDaysFromTodayGreaterThanDefault() {
		// given
		long expected = 400L;
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new SimpleValueJqwikPlugin()
				.minusDaysFromToday(expected))
			.build();

		// when
		java.time.LocalDateTime actual = sut.giveMeOne(java.time.LocalDateTime.class);

		// then
		java.time.LocalDateTime expectedMinDate = java.time.LocalDateTime.now().minusDays(expected);
		java.time.LocalDateTime expectedMaxDate = java.time.LocalDateTime.now().plusDays(expected + 30L);
		then(actual).isBetween(expectedMinDate, expectedMaxDate);
	}
}
