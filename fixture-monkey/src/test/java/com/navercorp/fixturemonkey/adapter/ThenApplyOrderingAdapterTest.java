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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.jqwik.api.Property;
import net.jqwik.api.PropertyDefaults;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.ComplexObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.DoubleNestedStringListWrapper;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.ListListStringObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.NestedStringArrayWrapper;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.NestedStringListWrapper;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringArrayWrapper;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringListWrapper;

@PropertyDefaults(tries = 10)
class ThenApplyOrderingAdapterTest {

	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.defaultNotNull(true)
		.plugin(new JavaNodeTreeAdapterPlugin())
		.build();

	@Property
	void nestedThenApply() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("str", "outer")
			.thenApply((it, builder) ->
				builder
					.set("object.str", it.getStr())
					.thenApply((inner, innerBuilder) ->
						innerBuilder.size("strList", 1).set("strList[0]", inner.getObject().getStr())
					)
			)
			.sample();

		// then
		then(actual.getStr()).isEqualTo("outer");
		then(actual.getObject().getStr()).isEqualTo("outer");
		then(actual.getStrList().get(0)).isEqualTo("outer");
	}

	@Property
	void nestedThenApplyRandomValuePreserved() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.setNotNull("str")
			.thenApply((outer, outerBuilder) ->
				outerBuilder.thenApply((inner, innerBuilder) -> innerBuilder.set("object.str", inner.getStr()))
			)
			.sample();

		// then
		then(actual.getObject().getStr()).isEqualTo(actual.getStr());
	}

	@Property
	void tripleNestedThenApply() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("str", "level1")
			.thenApply((l1, b1) ->
				b1
					.set("object.str", l1.getStr() + "-level2")
					.thenApply((l2, b2) ->
						b2
							.size("strList", 1)
							.thenApply((l3, b3) -> b3.set("strList[0]", l3.getObject().getStr() + "-level3"))
					)
			)
			.sample();

		// then
		then(actual.getStr()).isEqualTo("level1");
		then(actual.getObject().getStr()).isEqualTo("level1-level2");
		then(actual.getStrList().get(0)).isEqualTo("level1-level2-level3");
	}

	@Property
	void nestedThenApplyInnerOverwritesOuter() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("str", "outer")
			.thenApply((outer, outerBuilder) ->
				outerBuilder.thenApply((inner, innerBuilder) -> innerBuilder.set("str", "inner"))
			)
			.sample();

		// then
		then(actual.getStr()).isEqualTo("inner");
	}

	@Property
	void nestedThenApplyWithSizeAndElement() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.thenApply((outer, outerBuilder) ->
				outerBuilder
					.size("strList", 2)
					.thenApply((inner, innerBuilder) ->
						innerBuilder.set("strList[0]", "first").set("strList[1]", "second")
					)
			)
			.sample();

		// then
		then(actual.getStrList()).hasSize(2);
		then(actual.getStrList().get(0)).isEqualTo("first");
		then(actual.getStrList().get(1)).isEqualTo("second");
	}

	@Property
	void nestedThenApplySetAfterNestedApply() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.thenApply((outer, outerBuilder) ->
				outerBuilder.thenApply((inner, innerBuilder) -> innerBuilder.set("str", "fromInner"))
			)
			.set("object.str", "afterApply")
			.sample();

		// then
		then(actual.getStr()).isEqualTo("fromInner");
		then(actual.getObject().getStr()).isEqualTo("afterApply");
	}

	@Property
	void multipleThenApplyLaterOverwritesEarlier() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.thenApply((obj1, builder1) -> builder1.set("str", "first"))
			.thenApply((obj2, builder2) -> builder2.set("str", "second"))
			.sample();

		// then
		then(actual.getStr()).isEqualTo("second");
	}

	@Property
	void multipleThenApplySizeOrderPreserved() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.thenApply((obj1, builder1) -> builder1.size("strList", 3))
			.thenApply((obj2, builder2) -> builder2.size("strList", 1))
			.sample();

		// then
		then(actual.getStrList()).hasSize(1);
	}

	@Property
	void setElementThenSize() {
		// given
		String expected = "test";

		// when
		List<String> actual = SUT.giveMeBuilder(StringListWrapper.class)
			.set("values[0]", expected)
			.size("values", 3)
			.sample()
			.getValues();

		// then
		then(actual).hasSize(3);
		then(actual.get(0)).isEqualTo(expected);
	}

	@Property
	void setElementAtIndexThenSize() {
		// given
		String expected = "test";

		// when
		List<String> actual = SUT.giveMeBuilder(StringListWrapper.class)
			.set("values[2]", expected)
			.size("values", 5)
			.sample()
			.getValues();

		// then
		then(actual).hasSize(5);
		then(actual.get(2)).isEqualTo(expected);
	}

	@Property
	void setElementBeyondSizeThenSize() {
		// given
		String expected = "test";

		// when
		List<String> actual = SUT.giveMeBuilder(StringListWrapper.class)
			.set("values[5]", expected)
			.size("values", 3)
			.sample()
			.getValues();

		// then
		then(actual).hasSize(3);
	}

	@Property
	void setDecomposedThenSizeGreater() {
		// given
		List<String> decomposed = new ArrayList<>();
		decomposed.add("1");
		decomposed.add("2");
		decomposed.add("3");

		// when
		List<String> actual = SUT.giveMeBuilder(StringListWrapper.class)
			.set("values", decomposed)
			.size("values", 5)
			.sample()
			.getValues();

		// then
		then(actual).hasSize(5);
		then(actual.get(0)).isEqualTo("1");
		then(actual.get(1)).isEqualTo("2");
		then(actual.get(2)).isEqualTo("3");
	}

	@Property
	void setDecomposedThenSizeSmaller() {
		// given
		List<String> decomposed = new ArrayList<>();
		decomposed.add("1");
		decomposed.add("2");
		decomposed.add("3");

		// when
		List<String> actual = SUT.giveMeBuilder(StringListWrapper.class)
			.set("values", decomposed)
			.size("values", 2)
			.sample()
			.getValues();

		// then
		then(actual).hasSize(2);
		then(actual.get(0)).isEqualTo("1");
		then(actual.get(1)).isEqualTo("2");
	}

	@Property
	void sizeThenSetDecomposed() {
		// given
		List<String> decomposed = new ArrayList<>();
		decomposed.add("1");
		decomposed.add("2");
		decomposed.add("3");

		// when
		List<String> actual = SUT.giveMeBuilder(StringListWrapper.class)
			.size("values", 5)
			.set("values", decomposed)
			.sample()
			.getValues();

		// then
		then(actual).hasSize(3);
		then(actual.get(0)).isEqualTo("1");
		then(actual.get(1)).isEqualTo("2");
		then(actual.get(2)).isEqualTo("3");
	}

	@Property
	void setWildcardThenSize() {
		// given
		String expected = "test";

		// when
		List<String> actual = SUT.giveMeBuilder(StringListWrapper.class)
			.set("values[*]", expected)
			.size("values", 3)
			.sample()
			.getValues();

		// then
		then(actual).hasSize(3);
		then(actual.get(0)).isEqualTo(expected);
		then(actual.get(1)).isEqualTo(expected);
		then(actual.get(2)).isEqualTo(expected);
	}

	@Property
	void setDecomposedNestedThenSize() {
		// given
		List<StringListWrapper> decomposed = new ArrayList<>();
		StringListWrapper inner1 = new StringListWrapper();
		List<String> inner1Values = new ArrayList<>();
		inner1Values.add("a");
		inner1Values.add("b");
		inner1.setValues(inner1Values);
		decomposed.add(inner1);
		StringListWrapper inner2 = new StringListWrapper();
		List<String> inner2Values = new ArrayList<>();
		inner2Values.add("c");
		inner2.setValues(inner2Values);
		decomposed.add(inner2);

		// when
		List<StringListWrapper> actual = SUT.giveMeBuilder(NestedStringListWrapper.class)
			.set("values", decomposed)
			.size("values", 3)
			.sample()
			.getValues();

		// then
		then(actual).hasSize(3);
		then(actual.get(0).getValues()).containsExactly("a", "b");
		then(actual.get(1).getValues()).containsExactly("c");
	}

	@Property
	void setNestedElementThenSizes() {
		// given
		List<String> innerList = new ArrayList<>();
		innerList.add("1");
		innerList.add("2");
		innerList.add("3");

		// when
		List<StringListWrapper> actual = SUT.giveMeBuilder(NestedStringListWrapper.class)
			.set("values[0].values", innerList)
			.size("values", 2)
			.size("values[*].values", 2)
			.sample()
			.getValues();

		// then
		then(actual).hasSize(2);
		then(actual.get(0).getValues()).hasSize(2);
		then(actual.get(0).getValues().get(0)).isEqualTo("1");
		then(actual.get(0).getValues().get(1)).isEqualTo("2");
		then(actual.get(1).getValues()).hasSize(2);
	}

	@Property
	void setNestedElementThenSizeAtSamePath() {
		// given
		List<String> innerList = new ArrayList<>();
		innerList.add("1");
		innerList.add("2");
		innerList.add("3");

		// when
		List<StringListWrapper> actual = SUT.giveMeBuilder(NestedStringListWrapper.class)
			.set("values[0].values", innerList)
			.size("values", 2)
			.size("values[0].values", 2)
			.sample()
			.getValues();

		// then
		then(actual).hasSize(2);
		then(actual.get(0).getValues()).hasSize(2);
		then(actual.get(0).getValues().get(0)).isEqualTo("1");
		then(actual.get(0).getValues().get(1)).isEqualTo("2");
	}

	@Property
	void sizeThenApplySetDecomposed() {
		// given
		List<String> decomposed = new ArrayList<>();
		decomposed.add("1");
		decomposed.add("2");
		decomposed.add("3");

		// when
		List<String> actual = SUT.giveMeBuilder(StringListWrapper.class)
			.size("values", 5)
			.thenApply((obj, builder) -> builder.set("values", decomposed))
			.sample()
			.getValues();

		// then
		then(actual).hasSize(3);
		then(actual.get(0)).isEqualTo("1");
		then(actual.get(1)).isEqualTo("2");
		then(actual.get(2)).isEqualTo("3");
	}

	@Property
	void setDecomposedThenApplySize() {
		// given
		List<String> decomposed = new ArrayList<>();
		decomposed.add("1");
		decomposed.add("2");
		decomposed.add("3");

		// when
		List<String> actual = SUT.giveMeBuilder(StringListWrapper.class)
			.set("values", decomposed)
			.thenApply((obj, builder) -> builder.size("values", 5))
			.sample()
			.getValues();

		// then
		then(actual).hasSize(5);
		then(actual.get(0)).isEqualTo("1");
		then(actual.get(1)).isEqualTo("2");
		then(actual.get(2)).isEqualTo("3");
	}

	@Property
	void thenApplySetDecomposedThenSize() {
		// given
		List<String> decomposed = new ArrayList<>();
		decomposed.add("1");
		decomposed.add("2");
		decomposed.add("3");

		// when
		List<String> actual = SUT.giveMeBuilder(StringListWrapper.class)
			.thenApply((obj, builder) -> builder.set("values", decomposed))
			.size("values", 5)
			.sample()
			.getValues();

		// then
		then(actual).hasSize(5);
		then(actual.get(0)).isEqualTo("1");
		then(actual.get(1)).isEqualTo("2");
		then(actual.get(2)).isEqualTo("3");
	}

	@Property
	void thenApplySizeThenSetDecomposed() {
		// given
		List<String> decomposed = new ArrayList<>();
		decomposed.add("1");
		decomposed.add("2");
		decomposed.add("3");

		// when
		List<String> actual = SUT.giveMeBuilder(StringListWrapper.class)
			.thenApply((obj, builder) -> builder.size("values", 5))
			.set("values", decomposed)
			.sample()
			.getValues();

		// then
		then(actual).hasSize(3);
		then(actual.get(0)).isEqualTo("1");
		then(actual.get(1)).isEqualTo("2");
		then(actual.get(2)).isEqualTo("3");
	}

	@Property
	void sizeThenApplySetElement() {
		// given
		String expected = "test";

		// when
		List<String> actual = SUT.giveMeBuilder(StringListWrapper.class)
			.size("values", 3)
			.thenApply((obj, builder) -> builder.set("values[0]", expected))
			.sample()
			.getValues();

		// then
		then(actual).hasSize(3);
		then(actual.get(0)).isEqualTo(expected);
	}

	@Property
	void setElementThenApplySize() {
		// given
		String expected = "test";

		// when
		List<String> actual = SUT.giveMeBuilder(StringListWrapper.class)
			.set("values[0]", expected)
			.thenApply((obj, builder) -> builder.size("values", 3))
			.sample()
			.getValues();

		// then
		then(actual).hasSize(3);
		then(actual.get(0)).isEqualTo(expected);
	}

	@Property
	void nestedThenApplySizeThenSetDecomposed() {
		// given
		List<String> decomposed = new ArrayList<>();
		decomposed.add("1");
		decomposed.add("2");

		// when
		List<String> actual = SUT.giveMeBuilder(StringListWrapper.class)
			.thenApply((outer, outerBuilder) ->
				outerBuilder
					.size("values", 3)
					.thenApply((inner, innerBuilder) -> innerBuilder.set("values", decomposed))
			)
			.sample()
			.getValues();

		// then
		then(actual).hasSize(2);
		then(actual.get(0)).isEqualTo("1");
		then(actual.get(1)).isEqualTo("2");
	}

	@Property
	void nestedThenApplySetDecomposedThenSize() {
		// given
		List<String> decomposed = new ArrayList<>();
		decomposed.add("1");
		decomposed.add("2");
		decomposed.add("3");

		// when
		List<String> actual = SUT.giveMeBuilder(StringListWrapper.class)
			.thenApply((outer, outerBuilder) ->
				outerBuilder
					.set("values", decomposed)
					.thenApply((inner, innerBuilder) -> innerBuilder.size("values", 2))
			)
			.sample()
			.getValues();

		// then
		then(actual).hasSize(2);
		then(actual.get(0)).isEqualTo("1");
		then(actual.get(1)).isEqualTo("2");
	}

	@Property
	void setDecomposedThenApplySizeWithWildcard() {
		// given
		List<String> innerList = new ArrayList<>();
		innerList.add("1");
		innerList.add("2");
		innerList.add("3");

		// when
		List<StringListWrapper> actual = SUT.giveMeBuilder(NestedStringListWrapper.class)
			.set("values[0].values", innerList)
			.thenApply((obj, builder) -> builder.size("values", 2).size("values[*].values", 2))
			.sample()
			.getValues();

		// then
		then(actual).hasSize(2);
		then(actual.get(0).getValues()).hasSize(2);
		then(actual.get(0).getValues().get(0)).isEqualTo("1");
		then(actual.get(1).getValues()).hasSize(2);
	}

	@Property
	void setDecomposedThenSizeWithWildcardWithoutThenApply() {
		// given
		List<String> innerList = new ArrayList<>();
		innerList.add("1");
		innerList.add("2");
		innerList.add("3");

		// when
		List<StringListWrapper> actual = SUT.giveMeBuilder(NestedStringListWrapper.class)
			.set("values[0].values", innerList)
			.size("values", 2)
			.size("values[*].values", 2)
			.sample()
			.getValues();

		// then
		then(actual).hasSize(2);
		then(actual.get(0).getValues()).hasSize(2);
		then(actual.get(0).getValues().get(0)).isEqualTo("1");
		then(actual.get(1).getValues()).hasSize(2);
	}

	@Property
	void wildcardSizeInThenApply() {
		// when
		List<StringListWrapper> actual = SUT.giveMeBuilder(NestedStringListWrapper.class)
			.thenApply((obj, builder) -> builder.size("values", 2).size("values[*].values", 2))
			.sample()
			.getValues();

		// then
		then(actual).hasSize(2);
		then(actual.get(0).getValues()).isNotNull();
		then(actual.get(1).getValues()).isNotNull();
	}

	@Property
	void wildcardSizeOutsideThenApply() {
		// when
		List<StringListWrapper> actual = SUT.giveMeBuilder(NestedStringListWrapper.class)
			.size("values", 2)
			.size("values[*].values", 2)
			.sample()
			.getValues();

		// then
		then(actual).hasSize(2);
		then(actual.get(0).getValues()).hasSize(2);
		then(actual.get(1).getValues()).hasSize(2);
	}

	@Property
	void simpleWildcardSizeInThenApply() {
		// when
		List<String> actual = SUT.giveMeBuilder(StringListWrapper.class)
			.thenApply((obj, builder) -> builder.size("values", 3).set("values[*]", "test"))
			.sample()
			.getValues();

		// then
		then(actual).hasSize(3);
		then(actual.get(0)).isEqualTo("test");
		then(actual.get(1)).isEqualTo("test");
		then(actual.get(2)).isEqualTo("test");
	}

	@Property
	void sizeWithIndexInThenApply() {
		// when
		List<StringListWrapper> actual = SUT.giveMeBuilder(NestedStringListWrapper.class)
			.thenApply((obj, builder) ->
				builder.size("values", 2).size("values[0].values", 2).size("values[1].values", 2)
			)
			.sample()
			.getValues();

		// then
		then(actual).hasSize(2);
		then(actual.get(0).getValues()).hasSize(2);
		then(actual.get(1).getValues()).hasSize(2);
	}

	@Property
	void thenApplySetDecomposedThenSizeSmaller() {
		// given
		List<String> decomposed = new ArrayList<>();
		decomposed.add("1");
		decomposed.add("2");
		decomposed.add("3");
		decomposed.add("4");
		decomposed.add("5");

		// when
		List<String> actual = SUT.giveMeBuilder(StringListWrapper.class)
			.thenApply((obj, builder) -> builder.set("values", decomposed))
			.size("values", 2)
			.sample()
			.getValues();

		// then
		then(actual).hasSize(2);
		then(actual.get(0)).isEqualTo("1");
		then(actual.get(1)).isEqualTo("2");
	}

	@Property
	void multipleThenApplySetDecomposedThenSize() {
		// given
		List<String> decomposed = new ArrayList<>();
		decomposed.add("1");
		decomposed.add("2");
		decomposed.add("3");

		// when
		List<String> actual = SUT.giveMeBuilder(StringListWrapper.class)
			.thenApply((obj1, builder1) -> builder1.set("values", decomposed))
			.thenApply((obj2, builder2) -> builder2.size("values", 5))
			.sample()
			.getValues();

		// then
		then(actual).hasSize(5);
		then(actual.get(0)).isEqualTo("1");
		then(actual.get(1)).isEqualTo("2");
		then(actual.get(2)).isEqualTo("3");
	}

	@Property
	void setDecomposedMultipleIndicesThenApplyWildcardSize() {
		// given
		List<String> innerList0 = new ArrayList<>();
		innerList0.add("a");
		innerList0.add("b");
		innerList0.add("c");

		List<String> innerList1 = new ArrayList<>();
		innerList1.add("x");
		innerList1.add("y");
		innerList1.add("z");
		innerList1.add("w");

		// when
		List<StringListWrapper> actual = SUT.giveMeBuilder(NestedStringListWrapper.class)
			.set("values[0].values", innerList0)
			.set("values[1].values", innerList1)
			.thenApply((obj, builder) -> builder.size("values", 2).size("values[*].values", 2))
			.sample()
			.getValues();

		// then
		then(actual).hasSize(2);
		then(actual.get(0).getValues()).hasSize(2);
		then(actual.get(0).getValues().get(0)).isEqualTo("a");
		then(actual.get(0).getValues().get(1)).isEqualTo("b");
		then(actual.get(1).getValues()).hasSize(2);
		then(actual.get(1).getValues().get(0)).isEqualTo("x");
		then(actual.get(1).getValues().get(1)).isEqualTo("y");
	}

	@Property
	void setDecomposedThenApplyWildcardSetOverride() {
		// given
		List<String> innerList = new ArrayList<>();
		innerList.add("1");
		innerList.add("2");
		innerList.add("3");

		// when
		List<StringListWrapper> actual = SUT.giveMeBuilder(NestedStringListWrapper.class)
			.set("values[0].values", innerList)
			.set("values[1].values", Arrays.asList("a", "b"))
			.thenApply((obj, builder) ->
				builder.size("values", 2).size("values[*].values", 2).set("values[*].values[*]", "override")
			)
			.sample()
			.getValues();

		// then
		then(actual).hasSize(2);
		then(actual.get(0).getValues()).hasSize(2);
		then(actual.get(0).getValues().get(0)).isEqualTo("override");
		then(actual.get(0).getValues().get(1)).isEqualTo("override");
		then(actual.get(1).getValues()).hasSize(2);
		then(actual.get(1).getValues().get(0)).isEqualTo("override");
		then(actual.get(1).getValues().get(1)).isEqualTo("override");
	}

	@Property
	void setDecomposedThenApplyWildcardSizeLarger() {
		// given
		List<String> innerList = new ArrayList<>();
		innerList.add("1");
		innerList.add("2");

		// when
		List<StringListWrapper> actual = SUT.giveMeBuilder(NestedStringListWrapper.class)
			.set("values[0].values", innerList)
			.thenApply((obj, builder) -> builder.size("values", 2).size("values[*].values", 5))
			.sample()
			.getValues();

		// then
		then(actual).hasSize(2);
		then(actual.get(0).getValues()).hasSize(5);
		then(actual.get(0).getValues().get(0)).isEqualTo("1");
		then(actual.get(0).getValues().get(1)).isEqualTo("2");
		then(actual.get(1).getValues()).hasSize(5);
	}

	@Property
	void thirdNestedSetDecomposedThenApplyWildcardSize() {
		// given
		List<String> innerList = new ArrayList<>();
		innerList.add("deep1");
		innerList.add("deep2");
		innerList.add("deep3");

		// when
		DoubleNestedStringListWrapper actual = SUT.giveMeBuilder(DoubleNestedStringListWrapper.class)
			.set("values[0].values[0].values", innerList)
			.thenApply((obj, builder) ->
				builder.size("values", 1).size("values[*].values", 1).size("values[*].values[*].values", 2)
			)
			.sample();

		// then
		then(actual.getValues()).hasSize(1);
		then(actual.getValues().get(0).getValues()).hasSize(1);
		then(actual.getValues().get(0).getValues().get(0).getValues()).hasSize(2);
		then(actual.getValues().get(0).getValues().get(0).getValues().get(0)).isEqualTo("deep1");
	}

	@Property
	void setDecomposedObjectListThenApplyWildcardSet() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.size("strList", 3)
			.thenApply((obj, builder) -> builder.set("strList[*]", "wildcard"))
			.sample();

		// then
		then(actual.getStrList()).hasSize(3);
		then(actual.getStrList().get(0)).isEqualTo("wildcard");
		then(actual.getStrList().get(1)).isEqualTo("wildcard");
		then(actual.getStrList().get(2)).isEqualTo("wildcard");
	}

	@Property
	void setDecomposedThenApplyIndexSizeAndWildcardSize() {
		// given
		List<String> innerList = new ArrayList<>();
		innerList.add("1");
		innerList.add("2");
		innerList.add("3");

		// when
		List<StringListWrapper> actual = SUT.giveMeBuilder(NestedStringListWrapper.class)
			.set("values[0].values", innerList)
			.thenApply((obj, builder) ->
				builder.size("values", 2).size("values[0].values", 1).size("values[*].values", 3)
			)
			.sample()
			.getValues();

		// then
		then(actual).hasSize(2);
		then(actual.get(0).getValues()).hasSize(1);
		then(actual.get(0).getValues().get(0)).isEqualTo("1");
		then(actual.get(1).getValues()).hasSize(3);
	}

	@Property
	void multipleThenApplySizeThenSetDecomposed() {
		// given
		List<String> decomposed = new ArrayList<>();
		decomposed.add("1");
		decomposed.add("2");
		decomposed.add("3");

		// when
		List<String> actual = SUT.giveMeBuilder(StringListWrapper.class)
			.thenApply((obj1, builder1) -> builder1.size("values", 5))
			.thenApply((obj2, builder2) -> builder2.set("values", decomposed))
			.sample()
			.getValues();

		// then
		then(actual).hasSize(3);
		then(actual.get(0)).isEqualTo("1");
		then(actual.get(1)).isEqualTo("2");
		then(actual.get(2)).isEqualTo("3");
	}

	@Property
	void setNullThenThenApply() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.setNull("str")
			.thenApply((obj, builder) -> builder.set("str", "applied"))
			.sample();

		// then
		then(actual.getStr()).isEqualTo("applied");
	}

	@Property
	void thenApplyThenSetNull() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.thenApply((obj, builder) -> builder.set("str", "applied"))
			.setNull("str")
			.sample();

		// then
		then(actual.getStr()).isNull();
	}

	@Property
	void setNotNullThenThenApply() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.setNotNull("str")
			.thenApply((obj, builder) -> builder.setNull("str"))
			.sample();

		// then
		then(actual.getStr()).isNull();
	}

	@Property
	void thenApplyThenSetNotNull() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.thenApply((obj, builder) -> builder.setNull("str"))
			.setNotNull("str")
			.sample();

		// then
		then(actual.getStr()).isNotNull();
	}

	@Property
	void thenApplySizeThenSetNullContainer() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.thenApply((obj, builder) -> builder.size("strList", 5))
			.setNull("strList")
			.sample();

		// then
		then(actual.getStrList()).isNull();
	}

	@Property
	void setDecomposedThenApplyWildcardSetOverrideWithoutSizeChange() {
		// given
		List<String> innerList = Arrays.asList("1", "2", "3");

		// when
		List<StringListWrapper> actual = SUT.giveMeBuilder(NestedStringListWrapper.class)
			.size("values", 2)
			.size("values[*].values", 3)
			.set("values[0].values", innerList)
			.set("values[1].values", innerList)
			.thenApply((obj, builder) ->
				builder.set("values[*].values[*]", "override")
			)
			.sample()
			.getValues();

		// then
		then(actual).hasSize(2);
		for (int j = 0; j < 2; j++) {
			then(actual.get(j).getValues()).hasSize(3);
			for (int k = 0; k < 3; k++) {
				then(actual.get(j).getValues().get(k)).isEqualTo("override");
			}
		}
	}

	@Property
	void setDecomposedArrayThenApplyWildcardSetOverride() {
		// given
		StringArrayWrapper wrapper = new StringArrayWrapper();
		wrapper.setValues(new String[]{"1", "2", "3"});

		// when
		List<StringArrayWrapper> actual = SUT.giveMeBuilder(NestedStringArrayWrapper.class)
			.set("values", Collections.singletonList(wrapper))
			.thenApply((obj, builder) ->
				builder.size("values", 2)
					.size("values[*].values", 2)
					.set("values[*].values[*]", "override")
			)
			.sample()
			.getValues();

		// then
		then(actual).hasSize(2);
		for (int j = 0; j < 2; j++) {
			then(actual.get(j).getValues()).hasSize(2);
			for (int k = 0; k < 2; k++) {
				then(actual.get(j).getValues()[k]).isEqualTo("override");
			}
		}
	}

	@Property
	void setDecomposedAfterThenApplyWildcardSetOverride() {
		// given
		List<String> innerList = Arrays.asList("a", "b");

		// when
		List<StringListWrapper> actual = SUT.giveMeBuilder(NestedStringListWrapper.class)
			.thenApply((obj, builder) ->
				builder.set("values[*].values[*]", "override")
			)
			.size("values", 2)
			.size("values[*].values", 2)
			.set("values[0].values", innerList)
			.sample()
			.getValues();

		// then
		then(actual).hasSize(2);
		then(actual.get(0).getValues()).hasSize(2);
		then(actual.get(0).getValues().get(0)).isEqualTo("a");
		then(actual.get(0).getValues().get(1)).isEqualTo("b");
	}

	@Property
	void setDecomposedObjectThenApplyWildcardSetOverride() {
		// when
		StringListWrapper stringListWrapper = new StringListWrapper();
		stringListWrapper.setValues(Collections.singletonList("1"));
		List<StringListWrapper> actual = SUT.giveMeBuilder(NestedStringListWrapper.class)
			.set("values", Collections.singletonList(stringListWrapper))
			.thenApply((obj, builder) ->
				builder.size("values", 2)
					.size("values[*].values", 2)
					.set("values[*].values[*]", "override")
			)
			.sample()
			.getValues();

		// then
		then(actual).hasSize(2);
		then(actual.get(0).getValues()).hasSize(2);
		then(actual.get(0).getValues().get(0)).isEqualTo("override");
		then(actual.get(0).getValues().get(1)).isEqualTo("override");
		then(actual.get(1).getValues()).hasSize(2);
		then(actual.get(1).getValues().get(0)).isEqualTo("override");
		then(actual.get(1).getValues().get(1)).isEqualTo("override");
	}

	@Property
	void setDecomposedNestedContainerThenApplyWildcardSetOverride() {
		// given
		List<List<String>> values = Arrays.asList(
			Arrays.asList("a", "b"),
			Arrays.asList("c", "d")
		);

		// when
		List<List<String>> actual = SUT.giveMeBuilder(ListListStringObject.class)
			.set("values", values)
			.thenApply((obj, builder) ->
				builder.size("values", 2)
					.size("values[*]", 2)
					.set("values[*][*]", "override")
			)
			.sample()
			.getValues();

		// then
		then(actual).hasSize(2);
		for (int i = 0; i < 2; i++) {
			then(actual.get(i)).hasSize(2);
			then(actual.get(i).get(0)).isEqualTo("override");
			then(actual.get(i).get(1)).isEqualTo("override");
		}
	}

	@Property
	void listListStringSizeAndSet() {
		// when
		List<List<String>> actual = SUT.giveMeBuilder(ListListStringObject.class)
			.size("values", 2)
			.size("values[*]", 2)
			.set("values[*][*]", "test")
			.sample()
			.getValues();

		// then
		then(actual).hasSize(2);
		for (int i = 0; i < 2; i++) {
			then(actual.get(i)).hasSize(2);
			then(actual.get(i).get(0)).isEqualTo("test");
			then(actual.get(i).get(1)).isEqualTo("test");
		}
	}

	@Property
	void listListStringSetThenSize() {
		// given
		FixtureMonkey fm = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();

		List<List<String>> values = Arrays.asList(
			Arrays.asList("a", "b"),
			Arrays.asList("c", "d")
		);

		// when
		List<List<String>> actual = fm.giveMeBuilder(ListListStringObject.class)
			.set("values", values)
			.size("values", 2)
			.size("values[*]", 2)
			.set("values[*][*]", "override")
			.sample()
			.getValues();

		// then
		then(actual).hasSize(2);
		for (int i = 0; i < 2; i++) {
			then(actual.get(i)).hasSize(2);
			then(actual.get(i).get(0)).isEqualTo("override");
			then(actual.get(i).get(1)).isEqualTo("override");
		}
	}

}
