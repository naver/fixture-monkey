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

import static com.navercorp.fixturemonkey.api.experimental.JavaGetterMethodPropertySelector.javaGetter;
import static com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.tests.java.specs.ImmutableSpecs.ContainerObject;
import com.navercorp.fixturemonkey.tests.java.specs.ImmutableSpecs.JavaTypeObject;
import com.navercorp.fixturemonkey.tests.java.specs.ImmutableSpecs.RootJavaTypeObject;
import com.navercorp.fixturemonkey.tests.java.specs.NestedClassSpecs.Inner;

class PropertySelectorTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();

	@RepeatedTest(TEST_COUNT)
	void nestedObject() {
		Inner actual = SUT.giveMeOne(Inner.class);

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void setJavaGetter() {
		String actual = SUT.giveMeBuilder(JavaTypeObject.class)
			.set(javaGetter(JavaTypeObject::getString), "test")
			.sample()
			.getString();

		then(actual).isEqualTo("test");
	}

	@RepeatedTest(TEST_COUNT)
	void setJavaGetterInto() {
		String actual = SUT.giveMeBuilder(RootJavaTypeObject.class)
			.set(javaGetter(RootJavaTypeObject::getValue).into(JavaTypeObject::getString), "test")
			.sample()
			.getValue()
			.getString();

		then(actual).isEqualTo("test");
	}

	@RepeatedTest(TEST_COUNT)
	void setJavaGetterCollection() {
		String actual = SUT.giveMeBuilder(ContainerObject.class)
			.size("list", 1)
			.set(javaGetter(ContainerObject::getList).index(String.class, 0), "test")
			.sample()
			.getList()
			.get(0);

		then(actual).isEqualTo("test");
	}

	@RepeatedTest(TEST_COUNT)
	void setJavaGetterCollectionElement() {
		String actual = SUT.giveMeBuilder(ContainerObject.class)
			.size("complexList", 1)
			.set(
				javaGetter(ContainerObject::getComplexList)
					.index(JavaTypeObject.class, 0)
					.into(JavaTypeObject::getString), "test"
			)
			.sample()
			.getComplexList()
			.get(0)
			.getString();

		then(actual).isEqualTo("test");
	}

	@RepeatedTest(TEST_COUNT)
	void setJavaGetterCollectionAllElement() {
		String expected = "test";

		List<String> actual = SUT.giveMeBuilder(ContainerObject.class)
			.size("complexList", 3)
			.set(
				javaGetter(ContainerObject::getComplexList)
					.allIndex(JavaTypeObject.class)
					.into(JavaTypeObject::getString), expected
			)
			.sample()
			.getComplexList()
			.stream()
			.map(JavaTypeObject::getString)
			.collect(Collectors.toList());

		then(actual).allMatch(expected::equals);
	}

	@Test
	void typedJavaGetter() {
		String expected = "expected";

		String actual = SUT.giveMeBuilder(JavaTypeObject.class)
			.customizeProperty(javaGetter(JavaTypeObject::getString), arb -> arb.map(it -> expected))
			.sample()
			.getString();

		then(actual).isEqualTo(expected);
	}

	@Test
	void nestedTypedJavaGetter() {
		String expected = "expected";

		String actual = SUT.giveMeBuilder(RootJavaTypeObject.class)
			.customizeProperty(javaGetter(RootJavaTypeObject::getValue).into(JavaTypeObject::getString),
				arb -> arb.map(it -> expected))
			.sample()
			.getValue()
			.getString();

		then(actual).isEqualTo(expected);
	}

	@Test
	void indexTypedJavaGetter() {
		String expected = "expected";

		String actual = SUT.giveMeBuilder(ContainerObject.class)
			.size("list", 1)
			.customizeProperty(javaGetter(ContainerObject::getList).index(String.class, 0),
				arb -> arb.map(it -> expected))
			.sample()
			.getList()
			.get(0);

		then(actual).isEqualTo(expected);
	}

	@RepeatedTest(TEST_COUNT)
	void setExp() {
		String actual = SUT.giveMeJavaBuilder(JavaTypeObject.class)
			.setExpGetter(JavaTypeObject::getString, "test")
			.sample()
			.getString();

		then(actual).isEqualTo("test");
	}

	@RepeatedTest(TEST_COUNT)
	void setExpCollectionElement() {
		String actual = SUT.giveMeJavaBuilder(ContainerObject.class)
			.size("complexList", 1)
			.setExpGetter(
				javaGetter(ContainerObject::getComplexList)
					.index(JavaTypeObject.class, 0)
					.into(JavaTypeObject::getString),
				"test"
			)
			.sample()
			.getComplexList()
			.get(0)
			.getString();

		then(actual).isEqualTo("test");
	}
}
