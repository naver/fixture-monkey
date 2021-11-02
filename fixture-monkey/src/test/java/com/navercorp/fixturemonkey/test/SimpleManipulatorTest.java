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

import static com.navercorp.fixturemonkey.Constants.DEFAULT_ELEMENT_MAX_SIZE;
import static com.navercorp.fixturemonkey.test.SimpleManipulatorTestSpecs.SUT;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.ArrayList;
import java.util.List;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.domains.Domain;

import com.navercorp.fixturemonkey.customizer.ExpressionSpec;
import com.navercorp.fixturemonkey.test.SimpleManipulatorTestSpecs.IntValue;
import com.navercorp.fixturemonkey.test.SimpleManipulatorTestSpecs.IntegerList;
import com.navercorp.fixturemonkey.test.SimpleManipulatorTestSpecs.ListListString;
import com.navercorp.fixturemonkey.test.SimpleManipulatorTestSpecs.MapKeyIntegerValueInteger;
import com.navercorp.fixturemonkey.test.SimpleManipulatorTestSpecs.NestedStringValueList;
import com.navercorp.fixturemonkey.test.SimpleManipulatorTestSpecs.StringAndInt;
import com.navercorp.fixturemonkey.test.SimpleManipulatorTestSpecs.StringList;
import com.navercorp.fixturemonkey.test.SimpleManipulatorTestSpecs.StringValue;
import com.navercorp.fixturemonkey.test.SimpleManipulatorTestSpecs.TwoString;

class SimpleManipulatorTest {
	@Property
	void giveMeSpecSet() {
		// when
		IntValue actual = SUT.giveMeBuilder(IntValue.class)
			.spec(new ExpressionSpec().set("value", -1))
			.sample();

		then(actual.getValue()).isEqualTo(-1);
	}

	@Property
	void giveMeSpecSetArbitrary() {
		// when
		IntValue actual = SUT.giveMeBuilder(IntValue.class)
			.spec(new ExpressionSpec().set("value", Arbitraries.just(1)))
			.sample();

		then(actual.getValue()).isEqualTo(1);
	}

	@Property
	void giveMeListSize() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.spec(new ExpressionSpec().size("values", 1, 1))
			.sample();

		then(actual.getValues()).hasSize(1);
	}

	@Property
	void giveMeSetNull() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.spec(new ExpressionSpec().setNull("values"))
			.sample();

		then(actual.getValues()).isNull();
	}

	@Property
	void giveMeSizeAfterSetNullReturnsNull() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.spec(new ExpressionSpec()
				.setNull("values")
				.size("values", 1, 1)
			)
			.sample();

		then(actual.getValues()).isNull();
	}

	@Property
	void giveMeSetAfterSetNullReturnsNotNull() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.spec(new ExpressionSpec()
				.setNull("values")
				.size("values", 1, 1)
				.set("values[0]", 0)
			)
			.sample();

		then(actual.getValues()).isNotNull();
		then(actual.getValues()).hasSize(1);
		then(actual.getValues().get(0)).isEqualTo(0);
	}

	@Property
	void giveMeSetNotNullAfterSetNullReturnsNotNull() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.spec(new ExpressionSpec()
				.setNull("values")
				.setNotNull("values")
			)
			.sample();

		then(actual.getValues()).isNotNull();
	}

	@Property
	void giveMeSetNullAfterSetNotNullReturnsNull() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.spec(new ExpressionSpec()
				.setNotNull("values")
				.setNull("values")
			)
			.sample();

		then(actual.getValues()).isNull();
	}

	@Property
	void giveMeSetNullAfterSetReturnsNull() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.spec(new ExpressionSpec()
				.size("values", 1, 1)
				.set("values[0]", 0)
				.setNull("values")
			)
			.sample();

		then(actual.getValues()).isNull();
	}

	@Property
	void giveMeSpecPostCondition() {
		// when
		IntValue actual = SUT.giveMeBuilder(IntValue.class)
			.spec(new ExpressionSpec().setPostCondition(
				"value",
				Integer.class,
				value -> value >= 0 && value <= 100
			))
			.sample();

		then(actual.getValue()).isBetween(0, 100);
	}

	@Property
	void giveMeSpecPostConditionType() {
		// when
		IntValue actual = SUT.giveMeBuilder(IntValue.class)
			.spec(new ExpressionSpec().setPostCondition(
				"value",
				Integer.class,
				value -> value >= 0 && value <= 100
			))
			.sample();

		then(actual.getValue()).isBetween(0, 100);
	}

	@Property
	void giveMePostConditionIndex() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.spec(new ExpressionSpec()
				.setPostCondition("values[0]", Integer.class, value -> value >= 0 && value <= 100)
				.size("values", 1, 1))
			.sample();

		then(actual.getValues()).hasSize(1);
		then(actual.getValues().get(0)).isBetween(0, 100);
	}

	@Property
	@Domain(SimpleManipulatorTestSpecs.class)
	void giveMeObjectToBuilderSet(@ForAll IntValue expected) {
		IntValue actual = SUT.giveMeBuilder(expected)
			.set("value", 1)
			.sample();

		then(actual.getValue()).isEqualTo(1);
	}

	@Property
	@Domain(SimpleManipulatorTestSpecs.class)
	void giveMeObjectToBuilderSetWithExpressionGenerator(@ForAll IntValue expected) {
		IntValue actual = SUT.giveMeBuilder(expected)
			.set((resolver) -> "value", 1)
			.sample();

		then(actual.getValue()).isEqualTo(1);
	}

	@Property
	void giveMeObjectToBuilderSetIndex() {
		// given
		IntegerList expected = SUT.giveMeBuilder(IntegerList.class)
			.spec(new ExpressionSpec().size("values", 2, 2))
			.sample();

		// when
		IntegerList actual = SUT.giveMeBuilder(expected)
			.set("values[1]", 1)
			.sample();

		then(actual.getValues().get(1)).isEqualTo(1);
	}

	@Property
	void giveMeListSpecMaxSize() {
		// when
		StringList actual = SUT.giveMeBuilder(StringList.class)
			.spec(new ExpressionSpec()
				.list("values",
					it -> it.ofMaxSize(2)
				)
			)
			.sample();

		then(actual.getValues().size()).isLessThanOrEqualTo(2);
	}

	@Property
	void giveMeListSpecSizeBetween() {
		// when
		StringList actual = SUT.giveMeBuilder(StringList.class)
			.spec(new ExpressionSpec()
				.list("values",
					it -> it.ofSizeBetween(1, 3)
				)
			)
			.sample();

		then(actual.getValues().size()).isBetween(1, 3);
	}

	@Property
	void giveMeSetAllName() {
		// when
		TwoString actual = SUT.giveMeBuilder(TwoString.class)
			.set("*", "set")
			.sample();

		then(actual.getValue1()).isEqualTo("set");
		then(actual.getValue2()).isEqualTo("set");
	}

	@Property
	void giveMeListExactSize() {
		// when
		StringList actual = SUT.giveMeBuilder(StringList.class)
			.size("values", 3)
			.sample();

		then(actual.getValues().size()).isEqualTo(3);
	}

	@Property
	void giveMeListExactSizeWithExpressionGenerator() {
		// when
		StringList actual = SUT.giveMeBuilder(StringList.class)
			.size((resolver) -> "values", 3)
			.sample();

		then(actual.getValues().size()).isEqualTo(3);
	}

	@Property
	void giveMeSizeMap() {
		// when
		MapKeyIntegerValueInteger actual = SUT.giveMeBuilder(MapKeyIntegerValueInteger.class)
			.size("values", 2, 2)
			.sample();

		then(actual.getValues()).hasSize(2);
	}

	@Property
	void giveMeSizeMapWithExpressionGenerator() {
		// when
		MapKeyIntegerValueInteger actual = SUT.giveMeBuilder(MapKeyIntegerValueInteger.class)
			.size((resolver) -> "values", 2, 2)
			.sample();

		then(actual.getValues()).hasSize(2);
	}

	@Property
	void giveMeSetRightOrder() {
		// when
		StringList actual = SUT.giveMeBuilder(StringList.class)
			.spec(new ExpressionSpec()
				.list("values",
					it -> it.ofSize(3)
						.setElement(0, "field1")
						.setElement(1, "field2")
						.setElement(2, "field3")
				)
			)
			.sample();

		// then
		List<String> values = actual.getValues();
		then(values.size()).isEqualTo(3);
		then(values.get(0)).isEqualTo("field1");
		then(values.get(1)).isEqualTo("field2");
		then(values.get(2)).isEqualTo("field3");
	}

	@Property
	void giveMePostConditionRightOrder() {
		// when
		StringList actual = SUT.giveMeBuilder(StringList.class)
			.spec(new ExpressionSpec()
				.list("values",
					(it) -> it.ofSize(2)
						.setElementPostCondition(0, String.class, s -> s.length() > 5)
						.setElementPostCondition(1, String.class, s -> s.length() > 10)
				))
			.sample();

		// then
		List<String> values = actual.getValues();
		then(values.size()).isEqualTo(2);
		then(values.get(0).length()).isGreaterThan(5);
		then(values.get(1).length()).isGreaterThan(10);
	}

	@Property
	void giveMeListSpecMinSize() {
		// when
		StringList actual = SUT.giveMeBuilder(StringList.class)
			.spec(new ExpressionSpec()
				.list("values",
					it -> it.ofMinSize(1)
				)
			)
			.sample();

		then(actual.getValues().size()).isGreaterThanOrEqualTo(1);
	}

	@Property
	void giveMeSpecAny() {
		// given
		ExpressionSpec specOne = new ExpressionSpec()
			.list("values", it -> it
				.ofSize(1)
				.setElement(0, 1)
			);
		ExpressionSpec specTwo = new ExpressionSpec()
			.list("values", it -> it
				.ofSize(2)
				.setElement(0, 1)
				.setElement(1, 2)
			);

		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.specAny(specOne, specTwo)
			.sample();

		// then
		IntegerList expectedOne = new IntegerList();
		expectedOne.setValues(new ArrayList<>());
		expectedOne.getValues().add(1);

		IntegerList expectedTwo = new IntegerList();
		expectedTwo.setValues(new ArrayList<>());
		expectedTwo.getValues().add(1);
		expectedTwo.getValues().add(2);

		then(actual).isIn(expectedOne, expectedTwo);
	}

	@Property
	void giveMeBuilderSetNull() {
		// when
		StringValue actual = SUT.giveMeBuilder(StringValue.class)
			.set("value", null)
			.sample();

		then(actual.getValue()).isNull();
	}

	@Property
	void giveMeMinSize() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.minSize("values", 2)
			.sample();

		then(actual.getValues().size()).isGreaterThanOrEqualTo(2);
	}

	@Property
	void giveMeMinSizeWithExpressionGenerator() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.minSize((resolver) -> "values", 2)
			.sample();

		then(actual.getValues().size()).isGreaterThanOrEqualTo(2);
	}

	@Property
	void giveMeMaxSize() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.maxSize("values", 10)
			.sample();

		then(actual.getValues().size()).isLessThanOrEqualTo(10);
	}

	@Property
	void giveMeMaxSizeWithExpressionGenerator() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.maxSize((resolver) -> "values", 10)
			.sample();

		then(actual.getValues().size()).isLessThanOrEqualTo(10);
	}

	@Property(tries = 10)
	void giveMeSizeMinMaxBiggerThanDefault() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.size("values", 100, 150)
			.sample();

		then(actual.getValues().size()).isBetween(100, 150);
	}

	@Property(tries = 10)
	void giveMeSizeMinBiggerThanDefaultMax() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.minSize("values", 100)
			.sample();

		then(actual.getValues().size()).isBetween(100, 110);
	}

	@Property
	void giveMeSizeMaxSizeIsZero() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.maxSize("values", 0)
			.sample();

		then(actual.getValues()).isEmpty();
	}

	@Property
	void giveMeSizeMaxSizeBeforeMinSizeThenMinSizeWorks() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.maxSize("values", 15)
			.minSize("values", 14)
			.sample();

		then(actual.getValues()).hasSizeGreaterThanOrEqualTo(14);
	}

	@Property
	void giveMeSizeMinSizeBeforeMaxSizeThenMaxSizeWorks() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.minSize("values", 14)
			.maxSize("values", 15)
			.sample();

		then(actual.getValues()).hasSizeLessThanOrEqualTo(15);
	}

	@Property
	void giveMePostConditionLimitIndex() {
		// when
		StringList actual = SUT.giveMeBuilder(StringList.class)
			.size("values", 2, 2)
			.setPostCondition("values[*]", String.class, it -> it.length() > 0)
			.setPostCondition("values[*]", String.class, it -> it.length() > 5, 1)
			.sample();

		then(actual.getValues()).anyMatch(it -> it.length() > 5);
	}

	@Property
	void giveMePostConditionLimitIndexWithExpressionGenerator() {
		// when
		StringList actual = SUT.giveMeBuilder(StringList.class)
			.size("values", 2, 2)
			.setPostCondition((resolver) -> "values[*]", String.class, it -> it.length() > 0)
			.setPostCondition((resolver) -> "values[*]", String.class, it -> it.length() > 5, 1)
			.sample();

		then(actual.getValues()).anyMatch(it -> it.length() > 5);
	}

	@Property
	void giveMePostConditionLimitIndexNotOverwriteIfLimitIsZeroReturnsNotPostCondition() {
		// when
		StringList actual = SUT.giveMeBuilder(StringList.class)
			.setPostCondition("values[*]", String.class, it -> it.length() > 5)
			.setPostCondition("values[*]", String.class, it -> it.length() == 0, 0)
			.sample();

		then(actual.getValues()).allMatch(it -> it.length() > 5);
	}

	@Property
	void giveMeSpecListSetSize() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.spec(new ExpressionSpec().list("values", it -> it.ofSize(1)))
			.sample();

		then(actual.getValues()).hasSize(1);
	}

	@Property
	void giveMeSpecListSetElement() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.setElement(0, 1);
			}))
			.sample();

		then(actual.getValues()).hasSize(1);
		then(actual.getValues().get(0)).isEqualTo(1);
	}

	@Property
	void giveMeSpecListAnySet() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(3);
				it.any(1);
			}))
			.sample();

		then(actual.getValues()).anyMatch(it -> it == 1);
	}

	@Property
	void giveMeSpecListAllSet() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(3);
				it.all(1);
			}))
			.sample();

		then(actual.getValues()).allMatch(it -> it == 1);
	}

	@Property
	void giveMeSpecListPostConditionElement() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.setElementPostCondition(0, Integer.class, postConditioned -> postConditioned > 1);
			}))
			.sample();

		then(actual.getValues()).hasSize(1);
		then(actual.getValues().get(0)).isGreaterThan(1);
	}

	@Property
	void giveMeSpecListPostConditionElementField() {
		// when
		NestedStringValueList actual = SUT.giveMeBuilder(NestedStringValueList.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.setElementFieldPostCondition(0, "value", String.class,
					postConditioned -> postConditioned.length() > 5);
			}))
			.sample();

		then(actual.getValues()).allMatch(it -> it.getValue().length() > 5);
	}

	@Property
	void giveMeSpecListListElementSet() {
		// when
		ListListString actual = SUT.giveMeBuilder(ListListString.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.listElement(0, nestedIt -> {
					nestedIt.ofSize(1);
					nestedIt.setElement(0, "set");
				});
			}))
			.sample();

		then(actual.getValues()).hasSize(1);
		then(actual.getValues().get(0)).hasSize(1);
		then(actual.getValues().get(0).get(0)).isEqualTo("set");
	}

	@Property
	void giveMeSpecListListElementPostCondition() {
		// when
		ListListString actual = SUT.giveMeBuilder(ListListString.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.listElement(0, nestedIt -> {
					nestedIt.ofSize(1);
					nestedIt.setElementPostCondition(0, String.class, postConditioned -> postConditioned.length() > 5);
				});
			}))
			.sample();

		then(actual.getValues()).hasSize(1);
		then(actual.getValues().get(0)).hasSize(1);
		then(actual.getValues().get(0).get(0).length()).isGreaterThan(5);
	}

	@Property
	void giveMeSetLimitReturnsNotSet() {
		// when
		IntValue actual = SUT.giveMeBuilder(IntValue.class)
			.set("value", 1, 0)
			.sample();

		then(actual.getValue()).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeSpecSetWithLimit() {
		// when
		IntValue actual = SUT.giveMeBuilder(IntValue.class)
			.spec(new ExpressionSpec()
				.set("value", 1, 0))
			.sample();

		then(actual.getValue()).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeSpecSetIndexWithLimitReturns() {
		// when
		StringList actual = SUT.giveMeBuilder(StringList.class)
			.spec(new ExpressionSpec()
				.size("values", 2, 2)
				.set("values[*]", "set", 1))
			.sample();

		then(actual.getValues()).anyMatch(it -> !it.equals("set"));
	}

	@Property
	void giveMeSetIndexWithLimitReturns() {
		// when
		StringList actual = SUT.giveMeBuilder(StringList.class)
			.size("values", 2, 2)
			.set("values[*]", "set", 1)
			.sample();

		then(actual.getValues()).anyMatch(it -> !it.equals("set"));
	}

	@Property
	void giveMeSetArbitraryBuilder() {
		// when
		StringAndInt actual = SUT.giveMeBuilder(StringAndInt.class)
			.setBuilder("value2", SUT.giveMeBuilder(IntValue.class).set("value", 1))
			.sample();

		then(actual.getValue2().getValue()).isEqualTo(1);
	}

	@Property
	void giveMeSetArbitraryBuilderWithExpressionGenerator() {
		// when
		StringAndInt actual = SUT.giveMeBuilder(StringAndInt.class)
			.setBuilder((resolver) -> "value2", SUT.giveMeBuilder(IntValue.class).set("value", 1))
			.sample();

		then(actual.getValue2().getValue()).isEqualTo(1);
	}

	@Property
	void giveMeSpecSetArbitraryBuilder() {
		// when
		StringAndInt actual = SUT.giveMeBuilder(StringAndInt.class)
			.spec(new ExpressionSpec().setBuilder("value2",
				SUT.giveMeBuilder(IntValue.class).set("value", 1))
			)
			.sample();

		then(actual.getValue2().getValue()).isEqualTo(1);
	}

	@Property
	void giveMeDecomposeNullSetNotNullReturnsNewValue() {
		// given
		StringValue decomposed = new StringValue();

		// when
		StringValue actual = SUT.giveMeBuilder(decomposed)
			.setNotNull("value")
			.sample();

		then(actual.getValue()).isNotNull();
	}

	@Property
	void giveMeDecomposeNullSetNotNullReturnsNewValueWithExpressionGenerator() {
		// given
		StringValue decomposed = new StringValue();

		// when
		StringValue actual = SUT.giveMeBuilder(decomposed)
			.setNotNull((resolver) -> "value")
			.sample();

		then(actual.getValue()).isNotNull();
	}

	@Property
	void giveMeDecomposeNullSetNullReturnsNull() {
		// given
		StringValue decomposed = new StringValue();

		// when
		StringValue actual = SUT.giveMeBuilder(decomposed)
			.setNull("value")
			.sample();

		then(actual.getValue()).isNull();
	}

	@Property
	void giveMeDecomposeNullSetNullReturnsNullWithExpressionGenerator() {
		// given
		StringValue decomposed = new StringValue();

		// when
		StringValue actual = SUT.giveMeBuilder(decomposed)
			.setNull((resolver) -> "value")
			.sample();

		then(actual.getValue()).isNull();
	}

	@Property
	void giveMeSetPostConditionForRoot() {
		// when
		String actual = SUT.giveMeBuilder(String.class)
			.setPostCondition(it -> it.length() > 5)
			.sample();

		then(actual).hasSizeGreaterThan(5);
	}

	@Property
	void giveMeSetPostConditionForRootWithJsonPathRootExpression() {
		// when
		String actual = SUT.giveMeBuilder(String.class)
			.setPostCondition("$", String.class, it -> it.length() > 5)
			.sample();

		then(actual).hasSizeGreaterThan(5);
	}

	@Property
	void giveMeSetForRoot() {
		// when
		String actual = SUT.giveMeBuilder(String.class)
			.set("test")
			.sample();

		then(actual).isEqualTo("test");
	}

	@Property
	void giveMeSetForRootWithJsonPathRootExpression() {
		// when
		String actual = SUT.giveMeBuilder(String.class)
			.set("$", "test")
			.sample();

		then(actual).isEqualTo("test");
	}

	@Property
	void giveMeSetNullForRoot() {
		// when
		String actual = SUT.giveMeBuilder(String.class)
			.set(null)
			.sample();

		then(actual).isNull();
	}

	@Property
	void giveMeSetArbitraryForRoot() {
		// when
		String actual = SUT.giveMeBuilder(String.class)
			.set(Arbitraries.strings().ofLength(2))
			.sample();

		then(actual).hasSize(2);
	}

	@Property
	void giveMeSizeZeroReturnsEmpty() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.size("values", 0)
			.sample();

		then(actual.getValues()).isEmpty();
	}

	@Property
	void giveMeSizeMinSizeBiggerThanDefaultMaxSize() {
		// given
		int minSize = DEFAULT_ELEMENT_MAX_SIZE + 1;

		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.minSize("values", minSize)
			.sample();

		then(actual.getValues()).hasSizeGreaterThanOrEqualTo(minSize);
	}

	@Property
	void giveMeMaxSizeLessThanFixedMinSize() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.minSize("values", 2)
			.maxSize("values", 3)
			.fixed()
			.maxSize("values", 1)
			.sample();

		then(actual.getValues()).hasSizeLessThanOrEqualTo(1);
	}

	@Property
	void giveMeMaxSizeLessThanFixedMaxSize() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.minSize("values", 1)
			.maxSize("values", 3)
			.fixed()
			.maxSize("values", 2)
			.sample();

		then(actual.getValues()).hasSizeLessThanOrEqualTo(2);
	}

	@Property
	void giveMeSizeInFixedArbitrarySize() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.minSize("values", 1)
			.maxSize("values", 3)
			.fixed()
			.size("values", 2)
			.sample();

		then(actual.getValues()).hasSize(2);
	}

	@Property
	void giveMeMinSizeLessThenFixedMaxSize() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.minSize("values", 1)
			.maxSize("values", 3)
			.fixed()
			.minSize("values", 2)
			.sample();

		then(actual.getValues()).hasSizeGreaterThanOrEqualTo(2);
	}

	@Property
	void giveMeMinSizeGreaterThanFixedMaxSize() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.minSize("values", 1)
			.maxSize("values", 3)
			.fixed()
			.minSize("values", 4)
			.sample();

		then(actual.getValues()).hasSizeGreaterThanOrEqualTo(4);
	}

	@Property
	void giveMeIncludeFixedArbitrarySize() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.minSize("values", 1)
			.maxSize("values", 3)
			.fixed()
			.minSize("values", 0)
			.maxSize("values", 4)
			.sample();

		then(actual.getValues()).hasSizeBetween(0, 4);
	}
}
