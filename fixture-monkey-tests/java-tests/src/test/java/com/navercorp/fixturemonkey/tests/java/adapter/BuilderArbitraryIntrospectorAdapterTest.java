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

package com.navercorp.fixturemonkey.tests.java.adapter;

import static com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import net.jqwik.api.Arbitraries;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.adapter.JavaNodeTreeAdapterPlugin;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FailoverIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.tests.java.specs.ImmutableSpecs.ContainerObject;
import com.navercorp.fixturemonkey.tests.java.specs.ImmutableSpecs.DateTimeObject;
import com.navercorp.fixturemonkey.tests.java.specs.ImmutableSpecs.JavaTypeObject;
import com.navercorp.fixturemonkey.tests.java.specs.ImmutableSpecs.RootJavaTypeObject;
import com.navercorp.fixturemonkey.tests.java.specs.MixedIntrospectorsSpecs.BuilderWithMutableContainerObject;
import com.navercorp.fixturemonkey.tests.java.specs.MixedIntrospectorsSpecs.BuilderWithMutableFieldObject;
import com.navercorp.fixturemonkey.tests.java.specs.MutableSpecs;

class BuilderArbitraryIntrospectorAdapterTest {

	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(BuilderArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.plugin(new JavaNodeTreeAdapterPlugin())
		.build();

	@RepeatedTest(TEST_COUNT)
	void sampleJavaType() {
		JavaTypeObject actual = SUT.giveMeOne(JavaTypeObject.class);

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleDateTime() {
		DateTimeObject actual = SUT.giveMeOne(DateTimeObject.class);

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleContainer() {
		ContainerObject actual = SUT.giveMeOne(ContainerObject.class);

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleNestedObject() {
		RootJavaTypeObject actual = SUT.giveMeOne(RootJavaTypeObject.class);

		then(actual).isNotNull();
		then(actual.getValue()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void setString() {
		// given
		String expected = "test";

		// when
		String actual = SUT.giveMeBuilder(JavaTypeObject.class).set("string", expected).sample().getString();

		// then
		then(actual).isEqualTo(expected);
	}

	@RepeatedTest(TEST_COUNT)
	void setPostCondition() {
		String actual = SUT.giveMeBuilder(JavaTypeObject.class)
			.setPostCondition("string", String.class, str -> str.length() > 5)
			.sample()
			.getString();

		then(actual).hasSizeGreaterThan(5);
	}

	@RepeatedTest(TEST_COUNT)
	void setContainerSize() {
		// when
		ContainerObject actual = SUT.giveMeBuilder(ContainerObject.class).size("list", 3).sample();

		// then
		then(actual.getList()).hasSize(3);
	}

	@RepeatedTest(TEST_COUNT)
	void setContainerElement() {
		// given
		String expected = "fixed";

		// when
		ContainerObject actual = SUT.giveMeBuilder(ContainerObject.class)
			.size("list", 3)
			.set("list[0]", expected)
			.sample();

		// then
		then(actual.getList().get(0)).isEqualTo(expected);
	}

	@RepeatedTest(TEST_COUNT)
	void sampleTwiceResultNotMutated() {
		// given
		JavaTypeObject first = SUT.giveMeOne(JavaTypeObject.class);
		String firstString = first.getString();

		// when
		SUT.giveMeOne(JavaTypeObject.class);

		// then
		then(first.getString()).isEqualTo(firstString);
	}

	@RepeatedTest(TEST_COUNT)
	void thenApply() {
		// when
		JavaTypeObject actual = SUT.giveMeBuilder(JavaTypeObject.class)
			.thenApply((it, builder) -> builder.set("string", "applied"))
			.sample();

		// then
		then(actual.getString()).isEqualTo("applied");
	}

	@RepeatedTest(TEST_COUNT)
	void setRootObject() {
		// given
		JavaTypeObject expected = SUT.giveMeOne(JavaTypeObject.class);

		// when
		JavaTypeObject actual = SUT.giveMeBuilder(JavaTypeObject.class).set("$", expected).sample();

		// then
		then(actual).isEqualTo(expected);
	}

	@RepeatedTest(TEST_COUNT)
	void setNestedObjectField() {
		// given
		JavaTypeObject expected = SUT.giveMeOne(JavaTypeObject.class);

		// when
		RootJavaTypeObject actual = SUT.giveMeBuilder(RootJavaTypeObject.class).set("value", expected).sample();

		// then
		then(actual.getValue()).isEqualTo(expected);
	}

	@RepeatedTest(TEST_COUNT)
	void setNestedObjectInnerField() {
		// given
		String expected = "innerValue";

		// when
		RootJavaTypeObject actual = SUT.giveMeBuilder(RootJavaTypeObject.class).set("value.string", expected).sample();

		// then
		then(actual.getValue().getString()).isEqualTo(expected);
	}

	@RepeatedTest(TEST_COUNT)
	void setContainerObjectField() {
		// given
		JavaTypeObject expected = SUT.giveMeOne(JavaTypeObject.class);

		// when
		ContainerObject actual = SUT.giveMeBuilder(ContainerObject.class)
			.size("complexList", 2)
			.set("complexList[0]", expected)
			.sample();

		// then
		then(actual.getComplexList().get(0)).isEqualTo(expected);
	}

	@RepeatedTest(TEST_COUNT)
	void failoverWithBuilderAndFieldReflection() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(
				new FailoverIntrospector(
					Arrays.asList(BuilderArbitraryIntrospector.INSTANCE, FieldReflectionArbitraryIntrospector.INSTANCE)
				)
			)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();

		// when
		BuilderWithMutableFieldObject actual = sut.giveMeOne(BuilderWithMutableFieldObject.class);

		// then
		then(actual).isNotNull();
		then(actual.getName()).isNotNull();
		then(actual.getMutableObject()).isNotNull();
		then(actual.getMutableObject().getString()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void failoverSetMutableFieldInBuilderObject() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(
				new FailoverIntrospector(
					Arrays.asList(BuilderArbitraryIntrospector.INSTANCE, FieldReflectionArbitraryIntrospector.INSTANCE)
				)
			)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();
		String expected = "mutableString";

		// when
		BuilderWithMutableFieldObject actual = sut
			.giveMeBuilder(BuilderWithMutableFieldObject.class)
			.set("mutableObject.string", expected)
			.sample();

		// then
		then(actual.getMutableObject().getString()).isEqualTo(expected);
	}

	@RepeatedTest(TEST_COUNT)
	void failoverSetMutableObjectInBuilderObject() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(
				new FailoverIntrospector(
					Arrays.asList(BuilderArbitraryIntrospector.INSTANCE, FieldReflectionArbitraryIntrospector.INSTANCE)
				)
			)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();
		MutableSpecs.JavaTypeObject expected = sut.giveMeOne(MutableSpecs.JavaTypeObject.class);

		// when
		BuilderWithMutableFieldObject actual = sut
			.giveMeBuilder(BuilderWithMutableFieldObject.class)
			.set("mutableObject", expected)
			.sample();

		// then
		then(actual.getMutableObject()).isEqualTo(expected);
	}

	@RepeatedTest(TEST_COUNT)
	void failoverWithMutableContainerInBuilderObject() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(
				new FailoverIntrospector(
					Arrays.asList(BuilderArbitraryIntrospector.INSTANCE, FieldReflectionArbitraryIntrospector.INSTANCE)
				)
			)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();

		// when
		BuilderWithMutableContainerObject actual = sut
			.giveMeBuilder(BuilderWithMutableContainerObject.class)
			.size("mutableList", 2)
			.sample();

		// then
		then(actual.getMutableList()).hasSize(2);
		then(actual.getMutableList().get(0)).isNotNull();
		then(actual.getMutableList().get(0).getString()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void failoverSetMutableContainerElementField() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(
				new FailoverIntrospector(
					Arrays.asList(BuilderArbitraryIntrospector.INSTANCE, FieldReflectionArbitraryIntrospector.INSTANCE)
				)
			)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();
		String expected = "elementField";

		// when
		BuilderWithMutableContainerObject actual = sut
			.giveMeBuilder(BuilderWithMutableContainerObject.class)
			.size("mutableList", 2)
			.set("mutableList[0].string", expected)
			.sample();

		// then
		then(actual.getMutableList().get(0).getString()).isEqualTo(expected);
	}

	@RepeatedTest(TEST_COUNT)
	void pushExactTypeIntrospectorForNestedField() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BuilderArbitraryIntrospector.INSTANCE)
			.pushExactTypeArbitraryIntrospector(
				MutableSpecs.JavaTypeObject.class,
				FieldReflectionArbitraryIntrospector.INSTANCE
			)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();

		// when
		BuilderWithMutableFieldObject actual = sut
			.giveMeBuilder(BuilderWithMutableFieldObject.class)
			.set("mutableObject.string", "perType")
			.sample();

		// then
		then(actual.getName()).isNotNull();
		then(actual.getMutableObject().getString()).isEqualTo("perType");
	}

	@Test
	void complexDualRegisterWithAllOperations() {
		// given
		JavaTypeObject element1Object = JavaTypeObject.builder()
			.string("element1Whole")
			.primitiveInteger(999)
			.primitiveFloat(1.5f)
			.primitiveLong(100L)
			.primitiveDouble(2.5)
			.primitiveByte((byte)1)
			.primitiveCharacter('X')
			.primitiveShort((short)10)
			.primitiveBoolean(true)
			.wrapperInteger(888)
			.wrapperFloat(3.14f)
			.wrapperLong(200L)
			.wrapperDouble(6.28)
			.wrapperByte((byte)2)
			.wrapperCharacter('Y')
			.wrapperShort((short)20)
			.wrapperBoolean(false)
			.build();

		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BuilderArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			// register ContainerObject: size + set + thenApply
			.register(ContainerObject.class, fm ->
				fm
					.giveMeBuilder(ContainerObject.class)
					.size("complexList", 3)
					.size("list", 2)
					.set("list[0]", "registeredFirst")
					.set("set", new java.util.HashSet<>(Arrays.asList("regA", "regB")))
					.thenApply((it, builder) -> builder.set("list[1]", "registeredSecondViaThenApply"))
			)
			// register JavaTypeObject: set + setLazy + thenApply
			.register(JavaTypeObject.class, fm ->
				fm
					.giveMeBuilder(JavaTypeObject.class)
					.set("string", "fromInnerRegister")
					.setLazy("wrapperInteger", () -> 42)
					.thenApply((it, builder) -> builder.set("primitiveInteger", 777))
			)
			.build();

		// when
		ContainerObject actual = sut
			.giveMeBuilder(ContainerObject.class)
			// user overrides: size overrides register's size
			.size("complexList", 5)
			// user overrides: specific element's field overrides register's JavaTypeObject.string
			.set("complexList[0].string", "userOverrideElement0")
			// user overrides: whole object set on element[1]
			.set("complexList[1]", element1Object)
			// user overrides: setNull on element[2]'s wrapperInteger (overrides register's setLazy)
			.setNull("complexList[2].wrapperInteger")
			// user overrides: Arbitrary on element[3]'s string
			.set("complexList[3].string", Arbitraries.of("arbA", "arbB", "arbC"))
			// user overrides: list[0] overrides register's set
			.set("list[0]", "userFirst")
			// user does NOT override list[1] → register's thenApply value should apply
			// user overrides: map size
			.size("map", 2)
			// user: setPostCondition on map values
			.setPostCondition("map", Map.class, m -> !m.isEmpty())
			// user: thenApply sets last element's string
			.thenApply((it, builder) -> builder.set("complexList[4].string", "fromUserThenApply"))
			.sample();

		// then

		// complexList: user size(5) overrides register size(3)
		then(actual.getComplexList()).hasSize(5);

		// complexList[0]: user set field overrides register's JavaTypeObject.string
		then(actual.getComplexList().get(0).getString()).isEqualTo("userOverrideElement0");
		// complexList[0]: register's setLazy + thenApply should still apply (user only overrode string)
		then(actual.getComplexList().get(0).getWrapperInteger()).isEqualTo(42);
		then(actual.getComplexList().get(0).getPrimitiveInteger()).isEqualTo(777);

		// complexList[1]: user set whole object
		then(actual.getComplexList().get(1)).isEqualTo(element1Object);

		// complexList[2]: user setNull overrides register's setLazy
		then(actual.getComplexList().get(2).getWrapperInteger()).isNull();
		// complexList[2]: register's string + thenApply should still apply
		then(actual.getComplexList().get(2).getString()).isEqualTo("fromInnerRegister");
		then(actual.getComplexList().get(2).getPrimitiveInteger()).isEqualTo(777);

		// complexList[3]: user set Arbitrary
		then(actual.getComplexList().get(3).getString()).isIn("arbA", "arbB", "arbC");
		// complexList[3]: register's setLazy + thenApply should still apply
		then(actual.getComplexList().get(3).getWrapperInteger()).isEqualTo(42);
		then(actual.getComplexList().get(3).getPrimitiveInteger()).isEqualTo(777);

		// complexList[4]: user thenApply sets string
		then(actual.getComplexList().get(4).getString()).isEqualTo("fromUserThenApply");

		// list: register size(2), user did not override size
		then(actual.getList()).hasSize(2);
		// list[0]: user overrides register's value
		then(actual.getList().get(0)).isEqualTo("userFirst");
		// list[1]: register's thenApply value (user only overrode list[0], not list[1])
		then(actual.getList().get(1)).isEqualTo("registeredSecondViaThenApply");

		// set: register's value (user didn't override)
		then(actual.getSet()).containsExactlyInAnyOrder("regA", "regB");

		// map: user size(2) + postCondition
		then(actual.getMap()).hasSize(2);
	}
}
