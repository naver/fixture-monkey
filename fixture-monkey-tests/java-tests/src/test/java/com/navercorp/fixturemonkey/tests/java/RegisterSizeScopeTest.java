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

import java.beans.ConstructorProperties;
import java.util.List;

import org.junit.jupiter.api.RepeatedTest;

import lombok.Data;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.type.TypeReference;

class RegisterSizeScopeTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.register(Child.class, fm -> fm.giveMeBuilder(Child.class).size("values", 5))
		.build();

	private static final FixtureMonkey OUTER_SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.register(Outer.class, fm -> fm.giveMeBuilder(Outer.class).size("holders", 2).size("holders[*].first", 4))
		.build();

	private static final FixtureMonkey LIST_SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.register(
			List.class,
			fm -> fm.giveMeBuilder(new TypeReference<List<String>>() {
				})
				.size("$", 2)
				.set("$[0]", "first")
		)
		.build();

	private static final FixtureMonkey NESTED_SUT = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.register(
			Nested.class,
			fm -> fm.giveMeBuilder(Nested.class).size("values", 2).size("values[*].values", 3)
		)
		.build();

	@RepeatedTest(10)
	void registeredSizeAppliesToRegisteredType() {
		// when
		List<String> actual = SUT.giveMeOne(Parent.class).getChild().getValues();

		// then
		then(actual).hasSize(5);
	}

	@RepeatedTest(10)
	void registeredSizeDoesNotApplyToSameNamedPropertyOfEnclosingType() {
		// when
		List<String> actual = SUT.giveMeOne(Parent.class).getValues();

		// then
		then(actual).hasSizeLessThanOrEqualTo(3);
	}

	@RepeatedTest(10)
	void rootTypeRegisteredSizeAppliesWhenUserDeclaresNone() {
		// when
		Outer actual = OUTER_SUT.giveMeOne(Outer.class);

		// then
		then(actual.getHolders()).hasSize(2);
		then(actual.getHolders().get(0).getFirst()).hasSize(4);
		then(actual.getHolders().get(1).getFirst()).hasSize(4);
	}

	@RepeatedTest(10)
	void userSizeAtSamePathWinsOverRootTypeRegisteredSize() {
		// when
		Outer actual = OUTER_SUT.giveMeBuilder(Outer.class).size("holders", 3).sample();

		// then
		then(actual.getHolders()).hasSize(3);
	}

	@RepeatedTest(10)
	void userExactSizeWinsOverRootTypeRegisteredWildcardSizeAtItsPath() {
		// when
		Outer actual = OUTER_SUT.giveMeBuilder(Outer.class).size("holders[0].first", 1).sample();

		// then
		then(actual.getHolders().get(0).getFirst()).hasSize(1);
		then(actual.getHolders().get(1).getFirst()).hasSize(4);
	}

	@RepeatedTest(10)
	void registeredSizeAlsoAppliesWhenTypeIsNested() {
		// when
		List<Holder> actual = OUTER_SUT.giveMeOne(Wrapper.class).getOuter().getHolders();

		// then
		then(actual).hasSize(2);
		then(actual.get(0).getFirst()).hasSize(4);
	}

	@RepeatedTest(10)
	void userWildcardSizeWinsOverRootTypeRegisteredExactSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(Outer.class, fm -> fm.giveMeBuilder(Outer.class).size("holders", 2).size("holders[0].first", 4))
			.build();

		// when
		Outer actual = sut.giveMeBuilder(Outer.class).size("holders[*].first", 1).sample();

		// then
		then(actual.getHolders()).hasSize(2);
		then(actual.getHolders().get(0).getFirst()).hasSize(1);
		then(actual.getHolders().get(1).getFirst()).hasSize(1);
	}

	@RepeatedTest(10)
	void registeredSizeOfSampledContainerItselfApplies() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				List.class,
				fm -> fm.giveMeBuilder(new TypeReference<List<String>>() {
					})
					.size("$", 2)
			)
			.build();

		// when
		List<String> actual = sut.giveMeOne(new TypeReference<List<String>>() {
		});

		// then
		then(actual).hasSize(2);
	}

	@RepeatedTest(10)
	void userSizeOfSampledContainerWinsOverRegisteredSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				List.class,
				fm -> fm.giveMeBuilder(new TypeReference<List<String>>() {
					})
					.size("$", 2)
			)
			.build();

		// when
		List<String> actual = sut.giveMeBuilder(new TypeReference<List<String>>() {
			})
			.size("$", 4)
			.sample();

		// then
		then(actual).hasSize(4);
	}

	@RepeatedTest(10)
	void registeredSizeOfContainerTypeItselfApplies() {
		// when
		List<String> actual = LIST_SUT.giveMeOne(NamesHolder.class).getNames();

		// then
		then(actual).hasSize(2);
	}

	@RepeatedTest(10)
	void registeredElementValueOfContainerTypeApplies() {
		// when
		List<String> actual = LIST_SUT.giveMeOne(NamesHolder.class).getNames();

		// then
		then(actual.get(0)).isEqualTo("first");
	}

	@RepeatedTest(5)
	void registeredNestedPathSizeAppliesAtNestedPosition() {
		// when
		Nested actual = NESTED_SUT.giveMeOne(NestedHolder.class).getNested();

		// then
		then(actual.getValues()).hasSize(2);
		then(actual.getValues()).allSatisfy(inner -> then(inner.getValues()).hasSize(3));
	}

	@RepeatedTest(5)
	void registeredNestedPathSizeAppliesWhenSamplingScopeRoot() {
		// when
		Nested actual = NESTED_SUT.giveMeOne(Nested.class);

		// then
		then(actual.getValues()).hasSize(2);
		then(actual.getValues()).allSatisfy(inner -> then(inner.getValues()).hasSize(3));
	}

	@RepeatedTest(5)
	void laterSizeWinsWithinRegisteredBuilder() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(Inner.class, fm -> fm.giveMeBuilder(Inner.class).size("values", 5).size("values", 1))
			.build();

		// when
		Nested actual = sut.giveMeBuilder(Nested.class).size("values", 2).sample();

		// then
		then(actual.getValues()).allSatisfy(inner -> then(inner.getValues()).hasSize(1));
	}

	@Data
	public static class Parent {
		private Child child;
		private List<String> values;
	}

	@Data
	public static class Child {
		private List<String> values;
	}

	@Data
	public static class Wrapper {
		private Outer outer;
	}

	@Data
	public static class Outer {
		private List<Holder> holders;
	}

	@Data
	public static class Holder {
		private List<String> first;
	}

	@Data
	public static class NamesHolder {
		private List<String> names;
	}

	public static class Inner {
		private final List<String> values;

		@ConstructorProperties("values")
		public Inner(List<String> values) {
			this.values = values;
		}

		public List<String> getValues() {
			return values;
		}
	}

	public static class Nested {
		private final List<Inner> values;

		@ConstructorProperties("values")
		public Nested(List<Inner> values) {
			this.values = values;
		}

		public List<Inner> getValues() {
			return values;
		}
	}

	public static class NestedHolder {
		private final Nested nested;

		@ConstructorProperties("nested")
		public NestedHolder(Nested nested) {
			this.nested = nested;
		}

		public Nested getNested() {
			return nested;
		}
	}
}
