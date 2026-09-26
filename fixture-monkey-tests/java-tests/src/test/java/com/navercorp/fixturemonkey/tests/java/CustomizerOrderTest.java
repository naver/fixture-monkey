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

import static com.navercorp.fixturemonkey.api.expression.TypedExpressionGenerator.typedString;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.RepeatedTest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.FixtureMonkeyBuilder;
import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;

class CustomizerOrderTest {
	private static final FixtureMonkey SUT = base().build();

	@RepeatedTest(10)
	void customizerAfterSetAppliesToSetValue() {
		// when
		String actual = SUT.giveMeBuilder(Child.class).set("name", "v").customizeProperty(typedString("name"), BANG)
			.sample()
			.getName();

		// then
		then(actual).isEqualTo("v!");
	}

	@RepeatedTest(10)
	void setAfterCustomizerReplacesIt() {
		// when
		String actual = customizeName(SUT.giveMeBuilder(Child.class)).set("name", "v").sample().getName();

		// then
		then(actual).isEqualTo("v");
	}

	@RepeatedTest(10)
	void customizerWithoutSetAppliesToGeneratedValue() {
		// when
		String actual = customizeName(SUT.giveMeBuilder(Child.class)).sample().getName();

		// then
		then(actual).endsWith("!");
	}

	@RepeatedTest(10)
	void laterSetReplacesValueCustomizedAfterEarlierSet() {
		// when
		String actual = customizeName(SUT.giveMeBuilder(Child.class).set("name", "v")).set("name", "w")
			.sample()
			.getName();

		// then
		then(actual).isEqualTo("w");
	}

	@RepeatedTest(10)
	void setNullAfterCustomizerLeavesNull() {
		// when
		String actual = customizeName(SUT.giveMeBuilder(Child.class)).setNull("name").sample().getName();

		// then
		then(actual).isNull();
	}

	@RepeatedTest(10)
	void setLazyAfterCustomizerReplacesIt() {
		// when
		String actual = customizeName(SUT.giveMeBuilder(Child.class)).setLazy("name", () -> "lz").sample().getName();

		// then
		then(actual).isEqualTo("lz");
	}

	@RepeatedTest(10)
	void customizerAfterSetLazyAppliesToLazyValue() {
		// when
		String actual = customizeName(SUT.giveMeBuilder(Child.class).setLazy("name", () -> "lz")).sample().getName();

		// then
		then(actual).isEqualTo("lz!");
	}

	@RepeatedTest(10)
	void setOfParentReplacesEarlierChildCustomizer() {
		// when
		String actual = SUT.giveMeBuilder(Parent.class)
			.customizeProperty(typedString("child.name"), BANG)
			.set("child", new Child("obj"))
			.sample()
			.getChild()
			.getName();

		// then
		then(actual).isEqualTo("obj");
	}

	@RepeatedTest(10)
	void childCustomizerAfterParentSetAppliesInsideParentValue() {
		// when
		String actual = SUT.giveMeBuilder(Parent.class)
			.set("child", new Child("obj"))
			.customizeProperty(typedString("child.name"), BANG)
			.sample()
			.getChild()
			.getName();

		// then
		then(actual).isEqualTo("obj!");
	}

	@RepeatedTest(10)
	void wildcardCustomizerBeforeElementSetSkipsOnlyThatElement() {
		// when
		List<String> actual = SUT.giveMeBuilder(Holder.class)
			.size("values", 2)
			.customizeProperty(typedString("values[*]"), BANG)
			.set("values[0]", "v")
			.sample()
			.getValues();

		// then
		then(actual.get(0)).isEqualTo("v");
		then(actual.get(1)).endsWith("!");
	}

	@RepeatedTest(10)
	void wildcardCustomizerAfterElementSetAppliesToEveryElement() {
		// when
		List<String> actual = SUT.giveMeBuilder(Holder.class)
			.size("values", 2)
			.set("values[0]", "v")
			.customizeProperty(typedString("values[*]"), BANG)
			.sample()
			.getValues();

		// then
		then(actual.get(0)).isEqualTo("v!");
		then(actual.get(1)).endsWith("!");
	}

	@RepeatedTest(10)
	void registeredCustomizerAfterSetAppliesToSetValue() {
		// given
		FixtureMonkey sut = registeringChild(builder -> customizeName(builder.set("name", "v")));

		// when
		String actual = sut.giveMeOne(Parent.class).getChild().getName();

		// then
		then(actual).isEqualTo("v!");
	}

	@RepeatedTest(10)
	void registeredSetAfterCustomizerReplacesIt() {
		// given
		FixtureMonkey sut = registeringChild(builder -> customizeName(builder).set("name", "v"));

		// when
		String actual = sut.giveMeOne(Parent.class).getChild().getName();

		// then
		then(actual).isEqualTo("v");
	}

	@RepeatedTest(10)
	void registeredFieldCustomizerAfterWholeValueAppliesInsideIt() {
		// given
		FixtureMonkey sut = registeringChild(builder -> customizeName(builder.set("$", new Child("whole"))));

		// when
		String actual = sut.giveMeOne(Parent.class).getChild().getName();

		// then
		then(actual).isEqualTo("whole!");
	}

	@RepeatedTest(10)
	void registeredWholeValueReplacesEarlierFieldCustomizer() {
		// given
		FixtureMonkey sut = registeringChild(builder -> customizeName(builder).set("$", new Child("whole")));

		// when
		String actual = sut.giveMeOne(Parent.class).getChild().getName();

		// then
		then(actual).isEqualTo("whole");
	}

	@RepeatedTest(10)
	void registeredParentValueReplacesEarlierChildCustomizer() {
		// given
		FixtureMonkey sut = base()
			.register(
				Parent.class,
				fm -> fm.giveMeBuilder(Parent.class)
					.customizeProperty(typedString("child.name"), BANG)
					.set("child", new Child("obj"))
			)
			.build();

		// when
		String actual = sut.giveMeOne(Wrapper.class).getParent().getChild().getName();

		// then
		then(actual).isEqualTo("obj");
	}

	@RepeatedTest(10)
	void registeredCustomizerDoesNotApplyToUserValue() {
		// given
		FixtureMonkey sut = registeringChild(CustomizerOrderTest::customizeName);

		// when
		String actual = sut.giveMeBuilder(Parent.class).set("child.name", "u").sample().getChild().getName();

		// then
		then(actual).isEqualTo("u");
	}

	@RepeatedTest(10)
	void innerRegisteredCustomizerDoesNotApplyToWholeValueOfOuterRegisteredScope() {
		// given
		FixtureMonkey sut = base()
			.register(Parent.class, fm -> fm.giveMeBuilder(Parent.class).set("child", new Child("whole")))
			.register(Child.class, fm -> customizeName(fm.giveMeBuilder(Child.class)))
			.build();

		// when
		String actual = sut.giveMeOne(Wrapper.class).getParent().getChild().getName();

		// then
		then(actual).isEqualTo("whole");
	}

	@RepeatedTest(10)
	void innerRegisteredCustomizerDoesNotApplyToFieldValueOfOuterRegisteredScope() {
		// given
		FixtureMonkey sut = base()
			.register(Parent.class, fm -> fm.giveMeBuilder(Parent.class).set("child.name", "outer"))
			.register(Child.class, fm -> customizeName(fm.giveMeBuilder(Child.class)))
			.build();

		// when
		String actual = sut.giveMeOne(Wrapper.class).getParent().getChild().getName();

		// then
		then(actual).isEqualTo("outer");
	}

	private static final Function<CombinableArbitrary<? extends String>, CombinableArbitrary<? extends String>> BANG =
		arbitrary -> arbitrary.map(it -> it + "!");

	private static <T> ArbitraryBuilder<T> customizeName(ArbitraryBuilder<T> builder) {
		return builder.customizeProperty(typedString("name"), BANG);
	}

	private static FixtureMonkey registeringChild(Function<ArbitraryBuilder<Child>, ArbitraryBuilder<Child>> declare) {
		return base().register(Child.class, fm -> declare.apply(fm.giveMeBuilder(Child.class))).build();
	}

	private static FixtureMonkeyBuilder base() {
		return FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true);
	}

	@Data
	public static class Holder {
		private List<String> values;
	}

	@Data
	public static class Wrapper {
		private Parent parent;
	}

	@Data
	public static class Parent {
		private Child child;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Child {
		private String name;
	}
}
