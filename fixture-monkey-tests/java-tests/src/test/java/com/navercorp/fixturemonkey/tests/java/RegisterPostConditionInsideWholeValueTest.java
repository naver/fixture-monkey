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

import static com.navercorp.fixturemonkey.api.experimental.TypedExpressionGenerator.typedString;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.exception.FixedValueFilterMissException;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.type.TypeReference;

class RegisterPostConditionInsideWholeValueTest {
	private static final FixtureMonkey WHOLE_VALUE_SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.register(Child.class, fm -> fm.giveMeBuilder(Child.class).set("$", new Child("whole")))
		.build();

	private static final FixtureMonkey COUNT_SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.register(
			CountChild.class,
			fm -> fm.giveMeBuilder(CountChild.class).setPostCondition("count", int.class, it -> it >= 0)
		)
		.build();

	@Test
	void postConditionAfterWholeValueRejectsFieldOfIt() {
		// given
		FixtureMonkey sut = registeringChild(builder -> shortName(builder.set("$", new Child("whole"))));

		// when, then
		thenThrownBy(() -> sut.giveMeOne(Parent.class))
			.hasRootCauseInstanceOf(FixedValueFilterMissException.class);
	}

	@Test
	void postConditionBeforeWholeValueRejectsFieldOfIt() {
		// given
		FixtureMonkey sut = registeringChild(builder -> shortName(builder).set("$", new Child("whole")));

		// when, then
		thenThrownBy(() -> sut.giveMeOne(Parent.class))
			.hasRootCauseInstanceOf(FixedValueFilterMissException.class);
	}

	@Test
	void wholeValueSatisfyingPostConditionIsKept() {
		// given
		FixtureMonkey sut = registeringChild(
			builder -> builder.set("$", new Child("whole"))
				.setPostCondition("name", String.class, it -> it.startsWith("w"))
		);

		// when
		String actual = sut.giveMeOne(Parent.class).getChild().getName();

		// then
		then(actual).isEqualTo("whole");
	}

	@Test
	void registeredPostConditionRejectsFieldOfUserWholeValue() {
		// given
		FixtureMonkey sut = registeringChild(RegisterPostConditionInsideWholeValueTest::shortName);

		// when, then
		thenThrownBy(() -> sut.giveMeBuilder(Parent.class).set("child", new Child("whole")).sample())
			.hasRootCauseInstanceOf(FixedValueFilterMissException.class);
	}

	@Test
	void rootPostConditionRejectsFieldOfRegisteredWholeValue() {
		// when, then
		thenThrownBy(
			() -> WHOLE_VALUE_SUT.giveMeBuilder(ChildrenParent.class)
				.setPostCondition("child.name", String.class, it -> it.length() < 3)
				.sample()
		)
			.hasRootCauseInstanceOf(FixedValueFilterMissException.class);
	}

	@Test
	void rootWildcardPostConditionRejectsFieldOfRegisteredWholeValue() {
		// when, then
		thenThrownBy(
			() -> WHOLE_VALUE_SUT.giveMeBuilder(ChildrenParent.class)
				.size("children", 1)
				.setPostCondition("children[*].name", String.class, it -> it.length() < 3)
				.sample()
		)
			.hasRootCauseInstanceOf(FixedValueFilterMissException.class);
	}

	@Test
	void registeredWholeValueSatisfyingRootPostConditionIsKept() {
		// when
		String actual = WHOLE_VALUE_SUT.giveMeBuilder(ChildrenParent.class)
			.setPostCondition("child.name", String.class, it -> it.startsWith("w"))
			.sample()
			.getChild()
			.getName();

		// then
		then(actual).isEqualTo("whole");
	}

	@Test
	void rootCustomizerAppliesToFieldOfRegisteredWholeValue() {
		// when
		String actual = WHOLE_VALUE_SUT.giveMeBuilder(ChildrenParent.class)
			.<String>customizeProperty(typedString("child.name"), it -> it.map(name -> name + "!"))
			.sample()
			.getChild()
			.getName();

		// then
		then(actual).isEqualTo("whole!");
	}

	@Test
	void registeredPostConditionRejectsUserRootContainerElement() {
		// given
		List<CountChild> invalid = Collections.singletonList(new CountChild(-1));

		// when, then
		thenThrownBy(() -> COUNT_SUT.giveMeBuilder(new TypeReference<List<CountChild>>() {
			})
				.set("$", invalid)
				.sample()
		).hasRootCauseInstanceOf(FixedValueFilterMissException.class);
	}

	@Test
	void registeredPostConditionRejectsUserNestedContainerElement() {
		// given
		List<CountChild> invalid = Collections.singletonList(new CountChild(-1));

		// when, then
		thenThrownBy(() -> COUNT_SUT.giveMeBuilder(CountParent.class)
			.set("children", invalid)
			.sample()
		).hasRootCauseInstanceOf(FixedValueFilterMissException.class);
	}

	@Test
	void registeredPostConditionRejectsUserNestedObject() {
		// when, then
		thenThrownBy(() -> COUNT_SUT.giveMeBuilder(CountParent.class)
			.set("child", new CountChild(-1))
			.sample()
		).hasRootCauseInstanceOf(FixedValueFilterMissException.class);
	}

	@Test
	void userRootContainerElementSatisfyingRegisteredPostConditionIsKept() {
		// given
		List<CountChild> valid = Collections.singletonList(new CountChild(1));

		// when
		List<CountChild> actual = COUNT_SUT.giveMeBuilder(new TypeReference<List<CountChild>>() {
			})
			.set("$", valid)
			.sample();

		// then
		then(actual).isEqualTo(valid);
	}

	private static ArbitraryBuilder<Child> shortName(ArbitraryBuilder<Child> builder) {
		return builder.setPostCondition("name", String.class, it -> it.length() < 3);
	}

	private static FixtureMonkey registeringChild(Function<ArbitraryBuilder<Child>, ArbitraryBuilder<Child>> declare) {
		return FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(Child.class, fm -> declare.apply(fm.giveMeBuilder(Child.class)))
			.build();
	}

	@Data
	public static class Parent {
		private Child child;
	}

	@Data
	public static class ChildrenParent {
		private Child child;
		private List<Child> children;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Child {
		private String name;
	}

	@Data
	public static class CountParent {
		private CountChild child;
		private List<CountChild> children;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CountChild {
		private int count;
	}
}
