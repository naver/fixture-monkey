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

package com.navercorp.fixturemonkey.adapter;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Property;
import net.jqwik.api.PropertyDefaults;

import com.navercorp.fixturemonkey.FixtureMonkey;
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

@PropertyDefaults(tries = 10)
class RegisterAdapterTest {

	@Property
	void registerInstance() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(String.class, monkey -> monkey.giveMeBuilder("test"))
			.build();

		// when
		String actual = sut.giveMeOne(String.class);

		// then
		then(actual).isEqualTo("test");
	}

	@Property
	void registerSizeLessThanThree() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(
				new com.navercorp.fixturemonkey.api.matcher.MatcherOperator<>(
					it -> it.getType().equals(new TypeReference<List<String>>() {
					}.getType()),
					fixture -> fixture.giveMeBuilder(new TypeReference<List<String>>() {
					}).maxSize("$", 2)
				)
			)
			.build();

		// when
		List<String> actual = sut.giveMeOne(new TypeReference<List<String>>() {
		});

		// then
		then(actual).hasSizeLessThan(3);
	}

	@Property
	void registerFieldSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(StringListWrapper.class, fixture ->
				fixture.giveMeBuilder(StringListWrapper.class).size("values", 1)
			)
			.build();

		// when
		List<StringListWrapper> actual = sut.giveMeOne(NestedStringListWrapper.class).getValues();

		// then
		then(actual).allMatch(it -> it.getValues().size() == 1);
	}

	@Property
	void registerNestedListSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(StringListWrapper.class, fixture ->
				fixture.giveMeBuilder(StringListWrapper.class).size("values", 2)
			)
			.build();

		// when
		List<StringListWrapper> actual = sut.giveMeOne(NestedStringListWrapper.class).getValues();

		// then
		then(actual).allMatch(it -> it.getValues().size() == 2);
	}

	@Property
	void registerThirdNestedListSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
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

	@Property
	void registerRecursiveTypeSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(SelfRecursiveListObject.class, fixture ->
				fixture.giveMeBuilder(SelfRecursiveListObject.class).size("recursives", 1)
			)
			.build();

		// when
		SelfRecursiveListObject actual = sut.giveMeOne(SelfRecursiveListObject.class);

		// then
		then(actual.getRecursives()).hasSize(1);
	}

	@Property
	void registerMultipleFieldSizes() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
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

	@Property
	void builderSizeOverridesRegisterSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(StringListWrapper.class, fixture ->
				fixture.giveMeBuilder(StringListWrapper.class).size("values", 1)
			)
			.build();

		// when
		StringListWrapper actual = sut.giveMeBuilder(StringListWrapper.class).size("values", 5).sample();

		// then
		then(actual.getValues()).hasSize(5);
	}

	@Property
	void registerTypeAffectsNestedPaths() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(StringListWrapper.class, fixture ->
				fixture.giveMeBuilder(StringListWrapper.class).size("values", 2)
			)
			.build();

		// when
		NestedStringListWrapper actual = sut.giveMeOne(NestedStringListWrapper.class);

		// then
		then(actual.getValues()).allMatch(it -> it.getValues().size() == 2);
	}

	@Property
	void registerWithSizeRange() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
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
		then(samples).allMatch(it -> {
			int size = it.getValues().size();
			return size >= 2 && size <= 4;
		});
	}

	@Property
	void registerNestedFieldPathSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(NestedStringListWrapper.class, fixture ->
				fixture.giveMeBuilder(NestedStringListWrapper.class).size("values[*].values", 2)
			)
			.build();

		// when
		NestedStringListWrapper actual = sut.giveMeOne(NestedStringListWrapper.class);

		// then
		then(actual.getValues()).allMatch(it -> it.getValues().size() == 2);
	}

	@Property
	void registerObjectFieldSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
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

	@Property
	void registerTypeSizeAndPathSizeCombined() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
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

	@Property
	void registerDeepNestedPathSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
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

	@Property
	void registerOverlappingTypesInnerTypeHasPriority() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
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

	@Property
	void registerOverlappingTypesInnerTypeHasPriorityRegardlessOfOrder() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
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

	@Property
	void registerTypeSetValue() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(StringListWrapper.class, fixture ->
				fixture.giveMeBuilder(StringListWrapper.class).size("values", 1).set("values[0]", "registered")
			)
			.build();

		// when
		StringListWrapper actual = sut.giveMeOne(StringListWrapper.class);

		// then
		then(actual.getValues().get(0)).isEqualTo("registered");
	}

	@Property
	void registerOverlappingTypesSetInnerTypeHasPriority() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
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
		then(actual.getValues()).allMatch(it -> it.getValues().get(0).equals("fromInner"));
	}

	@Property
	void registerOverlappingTypesSetInnerTypeHasPriorityRegardlessOfOrder() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
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
		then(actual.getValues()).allMatch(it -> it.getValues().get(0).equals("fromInner"));
	}

	@Property
	void registerField() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(String.class, monkey -> monkey.giveMeBuilder("test"))
			.build();

		// when
		String actual = sut.giveMeOne(SimpleObject.class).getStr();

		// then
		then(actual).isEqualTo("test");
	}

	@Property
	void registerGroup() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
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

	@Property
	void registerBuilderGroup() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
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

	@Property
	void registerSetFirst() {
		// given
		String expected = "test2";
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(String.class, monkey -> monkey.giveMeBuilder("test"))
			.build();

		// when
		String actual = sut.giveMeBuilder(String.class).set("$", expected).sample();

		// then
		then(actual).isEqualTo(expected);
	}

	@Property
	void registerWithPriority() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(String.class, monkey -> monkey.giveMeBuilder("test2"), 2)
			.register(String.class, monkey -> monkey.giveMeBuilder("test"), 1)
			.build();

		// when
		String actual = sut.giveMeBuilder(String.class).sample();

		// then
		then(actual).isEqualTo("test");
	}

	@Property
	void registerMultipleTimesWithHierarchyReturnsCorrectOrder() {
		// given
		String expected = "test";
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(String.class, fixture -> fixture.giveMeBuilder(String.class).set(expected))
			.register(SimpleObject.class, fixture -> fixture.giveMeBuilder(SimpleObject.class).set("integer", 1))
			.build();

		// when
		String actual = sut.giveMeBuilder(SimpleObject.class).setNotNull("str").sample().getStr();

		// then
		then(actual).isEqualTo(expected);
	}

	@Property
	void registerRootAndChildElementGeneratingRoot() {
		// given
		String expected = "test";
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
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

	@Property
	void registerSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ComplexObject.class, fixture -> fixture.giveMeBuilder(ComplexObject.class).size("strList", 1))
			.build();

		// when
		List<String> actual = sut.giveMeOne(ComplexObject.class).getStrList();

		// then
		then(actual).hasSize(1);
	}

	@Property
	void registerFieldSet() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(StringWrapper.class, fixture -> fixture.giveMeBuilder(StringWrapper.class).set("value", "test"))
			.build();

		// when
		List<StringWrapper> actual = sut.giveMeOne(StringWrapperList.class).getValues();

		// then
		then(actual).allMatch(it -> "test".equals(it.getValue()));
	}

	@Property
	void sizeBiggerThanRegisterSized() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ComplexObject.class, fixture -> fixture.giveMeBuilder(ComplexObject.class).size("strList", 3))
			.build();

		// when
		List<String> actual = sut.giveMeBuilder(ComplexObject.class).size("strList", 10).sample().getStrList();

		// then
		then(actual).hasSize(10);
	}

	@Property
	void registerObjectNotFixed() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(String.class, it -> it.giveMeBuilder(String.class).set("$", Arbitraries.strings().ofLength(10)))
			.build();

		// when
		List<String> sampled = sut.giveMeBuilder(new TypeReference<List<String>>() {
		}).minSize("$", 3).sample();

		Set<String> actual = new HashSet<>(sampled);

		// then
		then(actual).hasSizeGreaterThan(1);
	}

	@Property
	void registerParentSetNullChildAndChildRegistered() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
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

	@Property
	void registerAffectsAllSameTypeFields() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(String.class, monkey -> monkey.giveMeBuilder("registered"))
			.build();

		// when
		StringPair actual = sut.giveMeOne(StringPair.class);

		// then
		then(actual.getValue1()).isEqualTo("registered");
		then(actual.getValue2()).isEqualTo("registered");
	}

	@Property
	void registerSameTypeFieldWithBuilderOverride() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(String.class, monkey -> monkey.giveMeBuilder("registered"))
			.build();

		// when
		StringPair actual = sut.giveMeBuilder(StringPair.class).set("value1", "overridden").sample();

		// then
		then(actual.getValue1()).isEqualTo("overridden");
		then(actual.getValue2()).isEqualTo("registered");
	}

	@Property
	void registerComplexTypeAffectsAllSameTypeFields() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(StringWrapper.class,
				monkey -> monkey.giveMeBuilder(StringWrapper.class).set("value", "registered"))
			.build();

		// when
		StringWrapperPair actual = sut.giveMeOne(StringWrapperPair.class);

		// then
		then(actual.getValue1().getValue()).isEqualTo("registered");
		then(actual.getValue2().getValue()).isEqualTo("registered");
	}

	@Property
	void registerSameTypeFieldWithThenApply() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
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

	@Property
	void applySizeWhenRegisteredWithSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
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

	@Property
	void sizeWhenRegisterSizeInApply() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(StringListWrapper.class, fixture ->
				fixture.giveMeBuilder(StringListWrapper.class).thenApply((it, builder) -> builder.size("values", 1))
			)
			.build();

		// when
		List<String> actual = sut.giveMeBuilder(StringListWrapper.class).size("values", 2).sample().getValues();

		// then
		then(actual).hasSize(2);
	}

	@Property
	void sizeWhenRegisterApply() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
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

	@Property
	void sizeElementWhenRegisteredSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
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

	@Property
	void sizeRegisteredElement() {
		String expected = "test";
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(String.class, fixture -> fixture.giveMeBuilder(String.class).set(expected))
			.build();

		List<String> actual = sut.giveMeBuilder(StringListWrapper.class).size("values", 5).sample().getValues();

		then(actual).allMatch(expected::equals);
	}

	@Property
	void registerSetThenSetNull() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
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

	@Property
	void registerSetThenSetNotNull() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
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

	@Property
	void registerSetNullThenDirectSet() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
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

	@Property
	void registerSetNullThenDirectSetNotNull() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
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

	@Property
	void registerContainerSizeThenSetNull() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
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

	@Property
	void registerSetNotNullThenDirectSetNull() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
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

}
