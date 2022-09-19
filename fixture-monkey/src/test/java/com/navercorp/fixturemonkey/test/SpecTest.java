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

import com.navercorp.fixturemonkey.LabMonkey;
import com.navercorp.fixturemonkey.customizer.ExpressionSpec;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Property;

import java.util.ArrayList;
import java.util.List;

import static com.navercorp.fixturemonkey.test.SpecTestSpecs.IntegerList;
import static com.navercorp.fixturemonkey.test.SpecTestSpecs.IntValue;
import static com.navercorp.fixturemonkey.test.SpecTestSpecs.ListListString;
import static com.navercorp.fixturemonkey.test.SpecTestSpecs.NestedStringValueList;
import static com.navercorp.fixturemonkey.test.SpecTestSpecs.StringAndInt;
import static com.navercorp.fixturemonkey.test.SpecTestSpecs.StringList;
import static com.navercorp.fixturemonkey.test.SpecTestSpecs.StringValue;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenNoException;

public class SpecTest {
	public static final LabMonkey SUT = LabMonkey.create();

	@Property
	void specSet() {
		// when
		IntValue actual = SUT.giveMeBuilder(IntValue.class)
			.spec(new ExpressionSpec().set("value", -1))
			.sample();

		then(actual.getValue()).isEqualTo(-1);
	}

	@Property
	void specSetArbitrary() {
		// when
		IntValue actual = SUT.giveMeBuilder(IntValue.class)
			.spec(new ExpressionSpec().set("value", Arbitraries.just(1)))
			.sample();

		then(actual.getValue()).isEqualTo(1);
	}

	@Property
	void specSize() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.spec(new ExpressionSpec().size("values", 1, 1))
			.sample();

		then(actual.getValues()).hasSize(1);
	}

	@Property
	void specMinSize() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.spec(new ExpressionSpec().minSize("values", 10))
			.sample();

		then(actual.getValues()).hasSizeGreaterThanOrEqualTo(10);
	}


	@Property
	void specMaxSize() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.spec(new ExpressionSpec().maxSize("values", 10))
			.sample();

		then(actual.getValues()).hasSizeLessThanOrEqualTo(10);
	}


	@Property
	void specSetNull() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.spec(new ExpressionSpec().setNull("values"))
			.sample();

		then(actual.getValues()).isNull();
	}

	@Property
	void specSizeAfterSetNullReturnsNull() {
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
	void specSetAfterSetNullReturnsNotNull() {
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
	void specSetNotNullAfterSetNullReturnsNotNull() {
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
	void specSetNullAfterSetNotNullReturnsNull() {
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
	void specSetNullAfterSetReturnsNull() {
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
	void specPostCondition() {
		// when
		IntValue actual = SUT.giveMeBuilder(IntValue.class)
			.spec(new ExpressionSpec().setPostCondition(
				"value",
				int.class,
				value -> value >= 0 && value <= 100
			))
			.sample();

		then(actual.getValue()).isBetween(0, 100);
	}

	@Property
	void specPostConditionType() {
		// when
		IntValue actual = SUT.giveMeBuilder(IntValue.class)
			.spec(new ExpressionSpec().setPostCondition(
				"value",
				int.class,
				value -> value >= 0 && value <= 100
			))
			.sample();

		then(actual.getValue()).isBetween(0, 100);
	}

	@Property
	void specPostConditionIndex() {
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
	void specObjectToBuilderSetIndex() {
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
	void specListMaxSize() {
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
	void specListSizeBetween() {
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
	void specSetRightOrder() {
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
	void specPostConditionRightOrder() {
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
	void specListMinSize() {
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
	void specAny() {
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
	void specAnyWithEmpty() {
		thenNoException().isThrownBy(
			() -> SUT.giveMeBuilder(StringList.class)
				.specAny()
				.sample()
		);
	}

	@Property
	void specAnyWithNull() {
		thenNoException().isThrownBy(
			() -> SUT.giveMeBuilder(StringList.class)
				.specAny((ExpressionSpec[])null)
				.sample()
		);
	}

	@Property(tries = 2)
	void specAnyReturnsDiff() {
		// given
		Arbitrary<StringValue> complex = SUT.giveMeBuilder(
				StringValue.class)
			.specAny(
				new ExpressionSpec().set("value", "test1"),
				new ExpressionSpec().set("value", "test2"),
				new ExpressionSpec().set("value", "test3"),
				new ExpressionSpec().set("value", "test4"),
				new ExpressionSpec().set("value", "test5"),
				new ExpressionSpec().set("value", "test6"),
				new ExpressionSpec().set("value", "test7"),
				new ExpressionSpec().set("value", "test8"),
				new ExpressionSpec().set("value", "test9"),
				new ExpressionSpec().set("value", "test10")
			)
			.build();

		// when
		List<StringValue> sampled = complex.list().ofSize(100).sample();

		// then
		List<StringValue> distinct = sampled.stream().distinct().collect(toList());
		then(distinct.size()).isNotEqualTo(sampled.size());
	}

	@Property
	void specAnyFirstWithMetadataManipulatorReturnsGivenOrder() {
		// when
		StringList actual = SUT.giveMeBuilder(StringList.class)
			.specAny(new ExpressionSpec().size("values", 2))
			.size("values", 1)
			.sample();

		then(actual.getValues()).hasSize(1);
	}

	@Property
	void specAnyLastWithMetadataManipulatorReturnsGivenOrder() {
		// when
		StringList actual = SUT.giveMeBuilder(StringList.class)
			.size("values", 1)
			.specAny(new ExpressionSpec().size("values", 2))
			.sample();

		then(actual.getValues()).hasSize(2);
	}

	@Property
	void specListSetSize() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.spec(new ExpressionSpec().list("values", it -> it.ofSize(1)))
			.sample();

		then(actual.getValues()).hasSize(1);
	}

	@Property
	void specListSetElement() {
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
	void specListAnySet() {
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
	void specListAnyWithoutSize() {
		// when
		StringList actual = SUT.giveMeBuilder(StringList.class)
			.spec(new ExpressionSpec().list("values", it -> it.any("set")))
			.sample();

		then(actual.getValues()).anyMatch(it -> it.equals("set"));
	}

	@Property
	void specListAnyWithoutMaxSize() {
		// when
		StringList actual = SUT.giveMeBuilder(StringList.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofMinSize(5);
				it.any("set");
			}))
			.sample();

		then(actual.getValues()).anyMatch(it -> it.equals("set"));
	}

	@Property
	void specListAnyWithoutMinSize() {
		// when
		StringList actual = SUT.giveMeBuilder(StringList.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofMaxSize(2);
				it.any("set");
			}))
			.sample();

		then(actual.getValues()).anyMatch(it -> it.equals("set"));
	}

	@Property
	void specListAllSet() {
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
	void specListPostConditionElement() {
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
	void specListPostConditionElementField() {
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
	void specListListElementSet() {
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
	void specListListElementPostCondition() {
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
	void specSetWithLimit() {
		// when
		IntValue actual = SUT.giveMeBuilder(IntValue.class)
			.spec(new ExpressionSpec()
				.set("value", 1, 0))
			.sample();

		then(actual.getValue()).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void specSetIndexWithLimitReturns() {
		// when
		StringList actual = SUT.giveMeBuilder(StringList.class)
			.spec(new ExpressionSpec()
				.size("values", 2, 2)
				.set("values[*]", "set", 1))
			.sample();

		then(actual.getValues()).anyMatch(it -> !it.equals("set"));
	}

	@Property
	void specSetArbitraryBuilder() {
		// when
		StringAndInt actual = SUT.giveMeBuilder(StringAndInt.class)
			.spec(new ExpressionSpec().set("value2",
				SUT.giveMeBuilder(IntValue.class).set("value", 1))
			)
			.sample();

		then(actual.getValue2().getValue()).isEqualTo(1);
	}
}
