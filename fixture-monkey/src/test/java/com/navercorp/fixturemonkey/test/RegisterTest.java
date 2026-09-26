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
package com.navercorp.fixturemonkey.test;

import static com.navercorp.fixturemonkey.api.experimental.TypedExpressionGenerator.typedString;
import static org.assertj.core.api.BDDAssertions.then;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import net.jqwik.api.Arbitraries;

import lombok.Data;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.FixtureMonkeyBuilder;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.ChildBuilderGroup;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.ConcreteIntValue;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.RegisterGroup;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.ComplexObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.DoubleNestedStringListWrapper;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.NestedStringListWrapper;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.SelfRecursiveListObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.SimpleObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringListWrapper;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringPair;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringWrapper;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringWrapperList;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringWrapperPair;

class RegisterTest {
	private static final FixtureMonkey CONTAINER_SUT = FixtureMonkey.builder()
		.defaultNotNull(true)
		.register(
			new MatcherOperator<>(
				it -> it.getJvmType().getRawType().equals(Set.class),
				fixture -> fixture.giveMeBuilder(new TypeReference<Set<String>>() {
					})
					.size("$", 3)
			)
		)
		.register(
			new MatcherOperator<>(
				it -> it.getJvmType().getRawType().equals(Map.class),
				fixture -> fixture.giveMeBuilder(new TypeReference<Map<String, String>>() {
					})
					.size("$", 2)
			)
		)
		.build();

	private static final FixtureMonkey LIST_SUT = FixtureMonkey.builder()
		.defaultNotNull(true)
		.register(
			new MatcherOperator<>(
				it -> it.getJvmType().getRawType().equals(List.class)
					&& it.getJvmType().getTypeVariables().size() == 1
					&& it.getJvmType().getTypeVariables().get(0).getRawType().equals(String.class),
				fixture -> fixture.giveMeBuilder(new TypeReference<List<String>>() {
					})
					.size("$", 2)
					.set("$[0]", "x")
			)
		)
		.build();

	@Test
	void registerInstance() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(String.class, monkey -> monkey.giveMeBuilder("test"))
			.build();

		// when
		String actual = sut.giveMeOne(String.class);

		// then
		then(actual).isEqualTo("test");
	}

	@Test
	void registerSizeWithCustomMatcher() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(
				new com.navercorp.fixturemonkey.api.matcher.MatcherOperator<>(
					it -> it.getJvmType().getRawType().equals(List.class)
						&& it.getJvmType().getTypeVariables().size() == 1
						&& it.getJvmType().getTypeVariables().get(0).getRawType().equals(String.class),
					fixture -> fixture.giveMeBuilder(new TypeReference<List<String>>() {
					}).size("$", 5)
				)
			)
			.build();

		// when
		List<String> actual = sut.giveMeOne(new TypeReference<List<String>>() {
		});

		// then
		then(actual).hasSize(5);
	}

	@Test
	void registerFieldSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(StringListWrapper.class, fixture ->
				fixture.giveMeBuilder(StringListWrapper.class).size("values", 1)
			)
			.build();

		// when
		List<StringListWrapper> actual = sut.giveMeOne(NestedStringListWrapper.class).getValues();

		// then
		then(actual).allMatch(it -> it.getValues().size() == 1);
	}

	@Test
	void registerNestedListSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(StringListWrapper.class, fixture ->
				fixture.giveMeBuilder(StringListWrapper.class).size("values", 2)
			)
			.build();

		// when
		List<StringListWrapper> actual = sut.giveMeOne(NestedStringListWrapper.class).getValues();

		// then
		then(actual).allMatch(it -> it.getValues().size() == 2);
	}

	@Test
	void registerThirdNestedListSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(StringListWrapper.class, fixture ->
				fixture.giveMeBuilder(StringListWrapper.class).size("values", 1)
			)
			.build();

		// when
		DoubleNestedStringListWrapper actual = sut.giveMeOne(DoubleNestedStringListWrapper.class);

		// then
		for (NestedStringListWrapper nested : actual.getValues()) {
			for (StringListWrapper list : nested.getValues()) {
				then(list.getValues()).hasSize(1);
			}
		}
	}

	@Test
	void registerRecursiveTypeSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(SelfRecursiveListObject.class, fixture ->
				fixture.giveMeBuilder(SelfRecursiveListObject.class).size("recursives", 1)
			)
			.build();

		// when
		SelfRecursiveListObject actual = sut.giveMeOne(SelfRecursiveListObject.class);

		// then
		then(actual.getRecursives()).hasSize(1);
	}

	@Test
	void registerMultipleFieldSizes() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(ComplexObject.class, fixture ->
				fixture.giveMeBuilder(ComplexObject.class).size("strList", 2).size("list", 3)
			)
			.build();

		// when
		ComplexObject actual = sut.giveMeOne(ComplexObject.class);

		// then
		then(actual.getStrList()).hasSize(2);
		then(actual.getList()).hasSize(3);
	}

	@Test
	void builderSizeOverridesRegisterSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(StringListWrapper.class, fixture ->
				fixture.giveMeBuilder(StringListWrapper.class).size("values", 1)
			)
			.build();

		// when
		StringListWrapper actual = sut.giveMeBuilder(StringListWrapper.class).size("values", 5).sample();

		// then
		then(actual.getValues()).hasSize(5);
	}

	@Test
	void registerTypeAffectsNestedPaths() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(StringListWrapper.class, fixture ->
				fixture.giveMeBuilder(StringListWrapper.class).size("values", 2)
			)
			.build();

		// when
		NestedStringListWrapper actual = sut.giveMeOne(NestedStringListWrapper.class);

		// then
		then(actual.getValues()).allMatch(it -> it.getValues().size() == 2);
	}

	@Test
	void registerWithSizeRange() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(StringListWrapper.class, fixture ->
				fixture.giveMeBuilder(StringListWrapper.class).minSize("values", 2).maxSize("values", 4)
			)
			.build();

		// when
		List<StringListWrapper> samples = Stream.generate(
				() -> sut.giveMeOne(NestedStringListWrapper.class).getValues())
			.limit(10)
			.flatMap(List::stream)
			.collect(Collectors.toList());

		// then
		then(samples).allMatch(it -> it.getValues().size() <= 4);
	}

	@Test
	void registerNestedFieldPathSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(NestedStringListWrapper.class, fixture ->
				fixture.giveMeBuilder(NestedStringListWrapper.class).size("values[*].values", 2)
			)
			.build();

		// when
		NestedStringListWrapper actual = sut.giveMeOne(NestedStringListWrapper.class);

		// then
		then(actual.getValues()).allMatch(it -> it.getValues().size() == 2);
	}

	@Test
	void registerObjectFieldSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(NestedStringListWrapper.class, fixture ->
				fixture
					.giveMeBuilder(NestedStringListWrapper.class)
					.size("values", 2)
					.size("values[0].values", 3)
					.size("values[1].values", 4)
			)
			.build();

		// when
		NestedStringListWrapper actual = sut.giveMeOne(NestedStringListWrapper.class);

		// then
		then(actual.getValues()).hasSize(2);
		then(actual.getValues().get(0).getValues()).hasSize(3);
		then(actual.getValues().get(1).getValues()).hasSize(4);
	}

	@Test
	void registerTypeSizeAndPathSizeCombined() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(StringListWrapper.class, fixture ->
				fixture.giveMeBuilder(StringListWrapper.class).size("values", 1)
			)
			.build();

		// when
		NestedStringListWrapper actual = sut
			.giveMeBuilder(NestedStringListWrapper.class)
			.size("values", 1)
			.size("values[0].values", 5)
			.sample();

		// then
		then(actual.getValues().get(0).getValues()).hasSize(5);
		for (int i = 1; i < actual.getValues().size(); i++) {
			then(actual.getValues().get(i).getValues()).hasSize(1);
		}
	}

	@Test
	void registerDeepNestedPathSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(DoubleNestedStringListWrapper.class, fixture ->
				fixture
					.giveMeBuilder(DoubleNestedStringListWrapper.class)
					.size("values", 1)
					.size("values[0].values", 1)
					.size("values[0].values[0].values", 2)
			)
			.build();

		// when
		DoubleNestedStringListWrapper actual = sut.giveMeOne(DoubleNestedStringListWrapper.class);

		// then
		then(actual.getValues()).hasSize(1);
		then(actual.getValues().get(0).getValues()).hasSize(1);
		then(actual.getValues().get(0).getValues().get(0).getValues()).hasSize(2);
	}

	@Test
	void registerOverlappingTypesInnerTypeHasPriority() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(NestedStringListWrapper.class, fixture ->
				fixture.giveMeBuilder(NestedStringListWrapper.class).size("values[*].values", 3)
			)
			.register(StringListWrapper.class, fixture ->
				fixture.giveMeBuilder(StringListWrapper.class).size("values", 5)
			)
			.build();

		// when
		NestedStringListWrapper actual = sut.giveMeOne(NestedStringListWrapper.class);

		// then
		then(actual.getValues()).allMatch(it -> it.getValues().size() == 5);
	}

	@Test
	void registerOverlappingTypesInnerTypeHasPriorityRegardlessOfOrder() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(StringListWrapper.class, fixture ->
				fixture.giveMeBuilder(StringListWrapper.class).size("values", 5)
			)
			.register(NestedStringListWrapper.class, fixture ->
				fixture.giveMeBuilder(NestedStringListWrapper.class).size("values[*].values", 3)
			)
			.build();

		// when
		NestedStringListWrapper actual = sut.giveMeOne(NestedStringListWrapper.class);

		// then
		then(actual.getValues()).allMatch(it -> it.getValues().size() == 5);
	}

	@Test
	void registerTypeSetValue() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(StringListWrapper.class, fixture ->
				fixture.giveMeBuilder(StringListWrapper.class).size("values", 1).set("values[0]", "registered")
			)
			.build();

		// when
		StringListWrapper actual = sut.giveMeOne(StringListWrapper.class);

		// then
		then(actual.getValues().get(0)).isEqualTo("registered");
	}

	@Test
	void registerOverlappingTypesSetOuterTypeHasPriority() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(NestedStringListWrapper.class, fixture ->
				fixture
					.giveMeBuilder(NestedStringListWrapper.class)
					.size("values", 2)
					.set("values[*].values[0]", "fromOuter")
			)
			.register(StringListWrapper.class, fixture ->
				fixture.giveMeBuilder(StringListWrapper.class).size("values", 1).set("values[0]", "fromInner")
			)
			.build();

		// when
		NestedStringListWrapper actual = sut.giveMeOne(NestedStringListWrapper.class);

		// then
		then(actual.getValues()).allMatch(it -> it.getValues().get(0).equals("fromOuter"));
	}

	@Test
	void registerOverlappingTypesSetOuterTypeHasPriorityRegardlessOfOrder() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(StringListWrapper.class, fixture ->
				fixture.giveMeBuilder(StringListWrapper.class).size("values", 1).set("values[0]", "fromInner")
			)
			.register(NestedStringListWrapper.class, fixture ->
				fixture
					.giveMeBuilder(NestedStringListWrapper.class)
					.size("values", 2)
					.set("values[*].values[0]", "fromOuter")
			)
			.build();

		// when
		NestedStringListWrapper actual = sut.giveMeOne(NestedStringListWrapper.class);

		// then
		then(actual.getValues()).allMatch(it -> it.getValues().get(0).equals("fromOuter"));
	}

	@Test
	void registerField() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(String.class, monkey -> monkey.giveMeBuilder("test"))
			.build();

		// when
		String actual = sut.giveMeOne(SimpleObject.class).getStr();

		// then
		then(actual).isEqualTo("test");
	}

	@Test
	void registerGroup() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.registerGroup(RegisterGroup.class)
			.build();

		// when
		String actual = sut.giveMeOne(SimpleObject.class).getStr();
		List<String> actual2 = sut.giveMeOne(new TypeReference<List<String>>() {
		});
		ConcreteIntValue actual3 = sut.giveMeOne(ConcreteIntValue.class);

		// then
		then(actual).hasSizeBetween(1, 3);
		then(actual2).hasSizeLessThan(5);
		then(actual3.getIntValue()).isEqualTo(RegisterGroup.FIXED_INT_VALUE.getIntValue());
	}

	@Test
	void registerBuilderGroup() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.registerGroup(new ChildBuilderGroup())
			.build();

		// when
		String actual = sut.giveMeOne(SimpleObject.class).getStr();
		List<String> actual2 = sut.giveMeOne(new TypeReference<List<String>>() {
		});
		ConcreteIntValue actual3 = sut.giveMeOne(ConcreteIntValue.class);

		// then
		then(actual).hasSizeBetween(1, 3);
		then(actual2).hasSizeLessThan(5);
		then(actual3.getIntValue()).isEqualTo(ChildBuilderGroup.FIXED_INT_VALUE.getIntValue());
	}

	@Test
	void registerSetFirst() {
		// given
		String expected = "test2";
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(String.class, monkey -> monkey.giveMeBuilder("test"))
			.build();

		// when
		String actual = sut.giveMeBuilder(String.class).set("$", expected).sample();

		// then
		then(actual).isEqualTo(expected);
	}

	@Test
	void registerWithPriority() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(String.class, monkey -> monkey.giveMeBuilder("test2"), 2)
			.register(String.class, monkey -> monkey.giveMeBuilder("test"), 1)
			.build();

		// when
		String actual = sut.giveMeBuilder(String.class).sample();

		// then
		then(actual).isEqualTo("test");
	}

	@Test
	void registerMultipleTimesWithHierarchyReturnsCorrectOrder() {
		// given
		String expected = "test";
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(String.class, fixture -> fixture.giveMeBuilder(String.class).set(expected))
			.register(SimpleObject.class, fixture -> fixture.giveMeBuilder(SimpleObject.class).set("integer", 1))
			.build();

		// when
		String actual = sut.giveMeBuilder(SimpleObject.class).setNotNull("str").sample().getStr();

		// then
		then(actual).isEqualTo(expected);
	}

	@Test
	void registerRootAndChildElementGeneratingRoot() {
		// given
		String expected = "test";
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(ComplexObject.class, fixture -> fixture.giveMeBuilder(ComplexObject.class))
			.register(String.class, fixture -> fixture.giveMeBuilder(String.class).set(expected))
			.build();

		// when
		List<String> actual = sut
			.giveMeBuilder(ComplexObject.class)
			.size("map", 1)
			.sample()
			.getList()
			.stream()
			.map(SimpleObject::getStr)
			.collect(Collectors.toList());

		// then
		then(actual).allMatch(expected::equals);
	}

	@Test
	void registerSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(ComplexObject.class, fixture -> fixture.giveMeBuilder(ComplexObject.class).size("strList", 1))
			.build();

		// when
		List<String> actual = sut.giveMeOne(ComplexObject.class).getStrList();

		// then
		then(actual).hasSize(1);
	}

	@Test
	void registerFieldSet() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(StringWrapper.class, fixture -> fixture.giveMeBuilder(StringWrapper.class).set("value", "test"))
			.build();

		// when
		List<StringWrapper> actual = sut.giveMeOne(StringWrapperList.class).getValues();

		// then
		then(actual).allMatch(it -> "test".equals(it.getValue()));
	}

	@Test
	void sizeBiggerThanRegisterSized() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(ComplexObject.class, fixture -> fixture.giveMeBuilder(ComplexObject.class).size("strList", 3))
			.build();

		// when
		List<String> actual = sut.giveMeBuilder(ComplexObject.class).size("strList", 10).sample().getStrList();

		// then
		then(actual).hasSize(10);
	}

	@Test
	void registerObjectNotFixed() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(String.class, it -> it.giveMeBuilder(String.class).set("$", Arbitraries.strings().ofLength(10)))
			.build();

		// when
		List<String> sampled = sut.giveMeBuilder(new TypeReference<List<String>>() {
		}).minSize("$", 3).sample();

		Set<String> actual = new HashSet<>(sampled);

		// then
		then(actual).hasSizeGreaterThan(1);
	}

	@Test
	void registerParentSetNullChildAndChildRegistered() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(SimpleObject.class, fixture -> fixture.giveMeBuilder(SimpleObject.class).set("str", "test"))
			.register(ComplexObject.class, fixture -> fixture.giveMeBuilder(ComplexObject.class).setNull("object"))
			.build();

		// when
		SimpleObject actual = sut
			.giveMeBuilder(new TypeReference<List<ComplexObject>>() {
			})
			.size("$", 1)
			.sample()
			.get(0)
			.getObject();

		// then
		then(actual).isNull();
	}

	@Test
	void registerAffectsAllSameTypeFields() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(String.class, monkey -> monkey.giveMeBuilder("registered"))
			.build();

		// when
		StringPair actual = sut.giveMeOne(StringPair.class);

		// then
		then(actual.getValue1()).isEqualTo("registered");
		then(actual.getValue2()).isEqualTo("registered");
	}

	@Test
	void registerSameTypeFieldWithBuilderOverride() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(String.class, monkey -> monkey.giveMeBuilder("registered"))
			.build();

		// when
		StringPair actual = sut.giveMeBuilder(StringPair.class).set("value1", "overridden").sample();

		// then
		then(actual.getValue1()).isEqualTo("overridden");
		then(actual.getValue2()).isEqualTo("registered");
	}

	@Test
	void registerComplexTypeAffectsAllSameTypeFields() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(StringWrapper.class,
				monkey -> monkey.giveMeBuilder(StringWrapper.class).set("value", "registered"))
			.build();

		// when
		StringWrapperPair actual = sut.giveMeOne(StringWrapperPair.class);

		// then
		then(actual.getValue1().getValue()).isEqualTo("registered");
		then(actual.getValue2().getValue()).isEqualTo("registered");
	}

	@Test
	void registerSameTypeFieldWithThenApply() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(StringWrapper.class, monkey ->
				monkey.giveMeBuilder(StringWrapper.class).thenApply((it, builder) -> builder.set("value", "applied"))
			)
			.build();

		// when
		StringWrapperPair actual = sut.giveMeOne(StringWrapperPair.class);

		// then
		then(actual.getValue1().getValue()).isEqualTo("applied");
		then(actual.getValue2().getValue()).isEqualTo("applied");
	}

	@Test
	void applySizeWhenRegisteredWithSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(StringListWrapper.class, fixture ->
				fixture.giveMeBuilder(StringListWrapper.class).size("values", 5)
			)
			.build();

		// when
		List<String> actual = sut
			.giveMeBuilder(StringListWrapper.class)
			.thenApply((it, builder) -> builder.size("values", 10))
			.sample()
			.getValues();

		// then
		then(actual).hasSize(10);
	}

	@Test
	void sizeWhenRegisterSizeInApply() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(StringListWrapper.class, fixture ->
				fixture.giveMeBuilder(StringListWrapper.class).thenApply((it, builder) -> builder.size("values", 1))
			)
			.build();

		// when
		List<String> actual = sut.giveMeBuilder(StringListWrapper.class).size("values", 2).sample().getValues();

		// then
		then(actual).hasSize(2);
	}

	@Test
	void sizeWhenRegisterApply() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(StringListWrapper.class, fixture ->
				fixture
					.giveMeBuilder(StringListWrapper.class)
					.size("values", 1)
					.thenApply((it, builder) -> {
					})
			)
			.build();

		// when
		List<String> actual = sut.giveMeBuilder(StringListWrapper.class).size("values", 2).sample().getValues();

		// then
		then(actual).hasSize(2);
	}

	@Test
	void sizeElementWhenRegisteredSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(NestedStringListWrapper.class, fixture ->
				fixture.giveMeBuilder(new TypeReference<NestedStringListWrapper>() {
				}).size("values", 5)
			)
			.build();

		// when
		List<StringListWrapper> actual = sut
			.giveMeBuilder(NestedStringListWrapper.class)
			.size("values[*].values", 3, 5)
			.sample()
			.getValues();

		// then
		then(actual).allMatch(it -> it.getValues().size() >= 3 && it.getValues().size() <= 5);
	}

	@Test
	void sizeRegisteredElement() {
		String expected = "test";
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(String.class, fixture -> fixture.giveMeBuilder(String.class).set(expected))
			.build();

		List<String> actual = sut.giveMeBuilder(StringListWrapper.class).size("values", 5).sample().getValues();

		then(actual).allMatch(expected::equals);
	}

	@Test
	void registerSetThenSetNull() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(SimpleObject.class, fixture -> fixture.giveMeBuilder(SimpleObject.class).set("str", "registered"))
			.build();

		// when
		String actual = sut.giveMeBuilder(SimpleObject.class)
			.setNull("str")
			.sample()
			.getStr();

		// then
		then(actual).isNull();
	}

	@Test
	void registerSetThenSetNotNull() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(SimpleObject.class, fixture -> fixture.giveMeBuilder(SimpleObject.class).set("str", "registered"))
			.build();

		// when
		String actual = sut.giveMeBuilder(SimpleObject.class)
			.setNotNull("str")
			.sample()
			.getStr();

		// then
		then(actual).isNotNull();
	}

	@Test
	void registerSetNullThenDirectSet() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(SimpleObject.class, fixture -> fixture.giveMeBuilder(SimpleObject.class).setNull("str"))
			.build();

		// when
		String actual = sut.giveMeBuilder(SimpleObject.class)
			.set("str", "direct")
			.sample()
			.getStr();

		// then
		then(actual).isEqualTo("direct");
	}

	@Test
	void registerSetNullThenDirectSetNotNull() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(SimpleObject.class, fixture -> fixture.giveMeBuilder(SimpleObject.class).setNull("str"))
			.build();

		// when
		String actual = sut.giveMeBuilder(SimpleObject.class)
			.setNotNull("str")
			.sample()
			.getStr();

		// then
		then(actual).isNotNull();
	}

	@Test
	void registerContainerSizeThenSetNull() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(ComplexObject.class, fixture -> fixture.giveMeBuilder(ComplexObject.class).size("strList", 3))
			.build();

		// when
		List<String> actual = sut.giveMeBuilder(ComplexObject.class)
			.setNull("strList")
			.sample()
			.getStrList();

		// then
		then(actual).isNull();
	}

	@Test
	void registerSetNotNullThenDirectSetNull() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.register(SimpleObject.class, fixture -> fixture.giveMeBuilder(SimpleObject.class).setNotNull("str"))
			.build();

		// when
		String actual = sut.giveMeBuilder(SimpleObject.class)
			.setNull("str")
			.sample()
			.getStr();

		// then
		then(actual).isNull();
	}

	@RepeatedTest(10)
	void customMatcherSeesDeclaredTypeOfSampledRoot() {
		// when
		List<String> actual = LIST_SUT.giveMeOne(new TypeReference<List<String>>() {
		});

		// then
		then(actual).hasSize(2);
		then(actual.get(0)).isEqualTo("x");
	}

	@RepeatedTest(10)
	void customMatcherSeesDeclaredTypeOfField() {
		// when
		List<String> actual = LIST_SUT.giveMeOne(ListHolder.class).getNames();

		// then
		then(actual).hasSize(2);
		then(actual.get(0)).isEqualTo("x");
	}

	@RepeatedTest(10)
	void customMatcherSeesDeclaredTypeOfContainerElement() {
		// when
		List<List<String>> actual = LIST_SUT.giveMeBuilder(ListHolder.class).size("nested", 2).sample().getNested();

		// then
		then(actual).hasSize(2).allSatisfy(it -> {
			then(it).hasSize(2);
			then(it.get(0)).isEqualTo("x");
		});
	}

	@RepeatedTest(10)
	void customMatcherSeesDeclaredSetTypeOfField() {
		// when
		Set<String> actual = CONTAINER_SUT.giveMeOne(ContainerHolder.class).getSet();

		// then
		then(actual).hasSize(3);
	}

	@RepeatedTest(10)
	void customMatcherSeesDeclaredMapTypeOfField() {
		// when
		Map<String, String> actual = CONTAINER_SUT.giveMeOne(ContainerHolder.class).getMap();

		// then
		then(actual).hasSize(2);
	}

	@RepeatedTest(10)
	void customMatcherSeesDeclaredMapTypeOfSampledRoot() {
		// when
		Map<String, String> actual = CONTAINER_SUT.giveMeOne(new TypeReference<Map<String, String>>() {
		});

		// then
		then(actual).hasSize(2);
	}

	@RepeatedTest(10)
	void customMatcherSeesDeclaredInterfaceTypeOfField() {
		// given
		FixtureMonkey sut = interfaceFixtureMonkey()
			.register(
				new MatcherOperator<>(
					it -> it.getJvmType().getRawType().equals(Named.class),
					fixture -> fixture.giveMeBuilder(NamedImpl.class).set("name", "fixed")
				)
			)
			.build();

		// when
		Named actual = sut.giveMeOne(InterfaceHolder.class).getNamed();

		// then
		then(actual.getName()).isEqualTo("fixed");
	}

	@RepeatedTest(10)
	void exactTypeRegisterOfInterfaceAppliesToInterfaceField() {
		// given
		FixtureMonkey sut = interfaceFixtureMonkey()
			.registerExactType(Named.class, fixture -> fixture.giveMeBuilder(NamedImpl.class).set("name", "fixed"))
			.build();

		// when
		Named actual = sut.giveMeOne(InterfaceHolder.class).getNamed();

		// then
		then(actual.getName()).isEqualTo("fixed");
	}

	@RepeatedTest(10)
	void exactTypeRegisterOfInterfaceAppliesToInterfaceElements() {
		// given
		FixtureMonkey sut = interfaceFixtureMonkey()
			.registerExactType(Named.class, fixture -> fixture.giveMeBuilder(NamedImpl.class).set("name", "fixed"))
			.build();

		// when
		List<Named> actual = sut.giveMeBuilder(InterfaceHolder.class).size("names", 2).sample().getNames();

		// then
		then(actual).hasSize(2).allSatisfy(it -> then(it.getName()).isEqualTo("fixed"));
	}

	@RepeatedTest(10)
	void exactTypeRegisterOfImplementationAppliesToInterfaceField() {
		// given
		FixtureMonkey sut = interfaceFixtureMonkey()
			.registerExactType(NamedImpl.class, fixture -> fixture.giveMeBuilder(NamedImpl.class).set("name", "fixed"))
			.build();

		// when
		Named actual = sut.giveMeOne(InterfaceHolder.class).getNamed();

		// then
		then(actual.getName()).isEqualTo("fixed");
	}

	private static FixtureMonkeyBuilder interfaceFixtureMonkey() {
		return FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new InterfacePlugin().interfaceImplements(Named.class, Collections.singletonList(NamedImpl.class)));
	}

	@RepeatedTest(10)
	void laterRegisterOfSameTypeWins() {
		// given
		FixtureMonkey sut = fixtureMonkey()
			.register(Order.class, fm -> fm.giveMeBuilder(Order.class).set("name", "first").size("tags", 1))
			.register(Order.class, fm -> fm.giveMeBuilder(Order.class).set("name", "second").size("tags", 2))
			.build();

		// when
		Order actual = sut.giveMeOne(Customer.class).getOrder();

		// then
		then(actual.getName()).isEqualTo("second");
		then(actual.getTags()).hasSize(2);
	}

	@RepeatedTest(10)
	void laterAnnotationMatcherRegisterWinsOverTypeRegister() {
		// given
		FixtureMonkey sut = fixtureMonkey()
			.register(Order.class, fm -> fm.giveMeBuilder(Order.class).set("name", "type").size("tags", 3))
			.register(vipRegister())
			.build();

		// when
		Customer actual = sut.giveMeOne(Customer.class);

		// then
		then(actual.getVipOrder().getName()).isEqualTo("vip");
		then(actual.getVipOrder().getTags()).hasSize(1);
		then(actual.getOrder().getName()).isEqualTo("type");
		then(actual.getOrder().getTags()).hasSize(3);
	}

	@RepeatedTest(10)
	void laterTypeRegisterWinsOverAnnotationMatcherRegister() {
		// given
		FixtureMonkey sut = fixtureMonkey()
			.register(vipRegister())
			.register(Order.class, fm -> fm.giveMeBuilder(Order.class).set("name", "type").size("tags", 3))
			.build();

		// when
		Order actual = sut.giveMeOne(Customer.class).getVipOrder();

		// then
		then(actual.getName()).isEqualTo("type");
		then(actual.getTags()).hasSize(3);
	}

	@RepeatedTest(10)
	void higherPriorityRegisterWinsRegardlessOfDeclarationOrder() {
		// given
		FixtureMonkey sut = fixtureMonkey()
			.register(Order.class, fm -> fm.giveMeBuilder(Order.class).set("name", "prior").size("tags", 1), 1)
			.register(Order.class, fm -> fm.giveMeBuilder(Order.class).set("name", "later").size("tags", 2))
			.build();

		// when
		Order actual = sut.giveMeOne(Customer.class).getOrder();

		// then
		then(actual.getName()).isEqualTo("prior");
		then(actual.getTags()).hasSize(1);
	}

	@RepeatedTest(10)
	void higherPriorityValueSkipsLowerPriorityCustomizer() {
		// given
		FixtureMonkey sut = fixtureMonkey()
			.register(Order.class, fm -> fm.giveMeBuilder(Order.class).set("name", "prior"), 1)
			.register(
				Order.class,
				fm -> fm.giveMeBuilder(Order.class)
					.<String>customizeProperty(typedString("name"), it -> it.map(name -> name + "!")),
				2
			)
			.build();

		// when
		String actual = sut.giveMeOne(Customer.class).getOrder().getName();

		// then
		then(actual).isEqualTo("prior");
	}

	@RepeatedTest(10)
	void lowerPriorityValueTakesHigherPriorityCustomizer() {
		// given
		FixtureMonkey sut = fixtureMonkey()
			.register(
				Order.class,
				fm -> fm.giveMeBuilder(Order.class)
					.<String>customizeProperty(typedString("name"), it -> it.map(name -> name + "!")),
				1
			)
			.register(Order.class, fm -> fm.giveMeBuilder(Order.class).set("name", "later"), 2)
			.build();

		// when
		String actual = sut.giveMeOne(Customer.class).getOrder().getName();

		// then
		then(actual).isEqualTo("later!");
	}

	private static FixtureMonkeyBuilder fixtureMonkey() {
		return FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true);
	}

	private static MatcherOperator<Function<FixtureMonkey, ? extends ArbitraryBuilder<?>>> vipRegister() {
		return new MatcherOperator<>(
			property -> property.getAnnotation(Vip.class).isPresent(),
			fm -> fm.giveMeBuilder(Order.class).set("name", "vip").size("tags", 1)
		);
	}

	@Data
	public static class ListHolder {
		private List<String> names;
		private List<List<String>> nested;
	}

	@Data
	public static class ContainerHolder {
		private Set<String> set;
		private Map<String, String> map;
	}

	@Data
	public static class InterfaceHolder {
		private Named named;
		private List<Named> names;
	}

	public interface Named {
		String getName();
	}

	@Data
	public static class NamedImpl implements Named {
		private String name;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Vip {
	}

	@Data
	public static class Customer {
		@Vip
		private Order vipOrder;
		private Order order;
	}

	@Data
	public static class Order {
		private String name;
		private List<String> tags;
	}
}
