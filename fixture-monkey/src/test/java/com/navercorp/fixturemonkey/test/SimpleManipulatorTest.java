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

import static org.assertj.core.api.BDDAssertions.then;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Property;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.customizer.ExpressionSpec;

public class SimpleManipulatorTest {
	private final FixtureMonkey sut = FixtureMonkey.builder()
		.build();

	@Property
	void giveMeSpecSet() {
		// when
		IntegerWrapperClass actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.spec(new ExpressionSpec().set("value", -1))
			.sample();

		then(actual.getValue()).isEqualTo(-1);
	}

	@Property
	void giveMeSpecSetArbitrary() {
		// when
		IntegerWrapperClass actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.spec(new ExpressionSpec().set("value", Arbitraries.just(1)))
			.sample();

		then(actual.value).isEqualTo(1);
	}

	@Property
	void giveMeListSize() {
		// when
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().size("values", 1, 1))
			.sample();

		then(actual.values).hasSize(1);
	}

	@Property
	void giveMeSetNull() {
		// when
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().setNull("values"))
			.sample();

		then(actual.values).isNull();
	}

	@Property
	void giveMeSizeAfterSetNullReturnsNull() {
		// when
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec()
				.setNull("values")
				.size("values", 1, 1)
			)
			.sample();

		then(actual.values).isNull();
	}

	@Property
	void giveMeSetAfterSetNullReturnsNotNull() {
		// when
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec()
				.setNull("values")
				.size("values", 1, 1)
				.set("values[0]", 0)
			)
			.sample();

		then(actual.values).isNotNull();
		then(actual.values).hasSize(1);
		then(actual.values.get(0)).isEqualTo(0);
	}

	@Property
	void giveMeSetNotNullAfterSetNullReturnsNotNull() {
		// when
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec()
				.setNull("values")
				.setNotNull("values")
			)
			.sample();

		then(actual.values).isNotNull();
	}

	@Property
	void giveMeSetNullAfterSetNotNullReturnsNull() {
		// when
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec()
				.setNotNull("values")
				.setNull("values")
			)
			.sample();

		then(actual.values).isNull();
	}

	@Property
	void giveMeSetNullAfterSetReturnsNull() {
		// when
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec()
				.size("values", 1, 1)
				.set("values[0]", 0)
				.setNull("values")
			)
			.sample();

		then(actual.values).isNull();
	}

	@Property
	void giveMeSpecSetPrefix() {
		// when
		StringWrapperClass actual = this.sut.giveMeBuilder(StringWrapperClass.class)
			.spec(new ExpressionSpec().setPrefix("value", "prefix"))
			.sample();

		then(actual.value).startsWith("prefix");
	}

	@Property
	void giveMeSpecSetSuffix() {
		// when
		StringWrapperClass actual = this.sut.giveMeBuilder(StringWrapperClass.class)
			.spec(new ExpressionSpec().setSuffix("value", "suffix"))
			.sample();

		then(actual.value).endsWith("suffix");
	}

	@Property
	void giveMeSpecPostCondition() {
		// when
		IntegerWrapperClass actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.spec(new ExpressionSpec().setPostCondition(
				"value",
				Integer.class,
				value -> value >= 0 && value <= 100
			))
			.sample();

		then(actual.value).isBetween(0, 100);
	}

	@Property
	void giveMeSpecPostConditionType() {
		// when
		IntegerWrapperClass actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.spec(new ExpressionSpec().setPostCondition(
				"value",
				Integer.class,
				value -> value >= 0 && value <= 100
			))
			.sample();

		then(actual.value).isBetween(0, 100);
	}

	@Property
	void giveMePostConditionIndex() {
		// when
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec()
				.setPostCondition("values[0]", Integer.class, value -> value >= 0 && value <= 100)
				.size("values", 1, 1))
			.sample();

		then(actual.values).hasSize(1);
		then(actual.values.get(0)).isBetween(0, 100);
	}

	@Property
	void giveMeObjectToBuilderSet() {
		// given
		IntegerWrapperClass expected = this.sut.giveMeOne(IntegerWrapperClass.class);

		// when
		IntegerWrapperClass actual = this.sut.giveMeBuilder(expected)
			.set("value", 1)
			.sample();

		then(actual.value).isEqualTo(1);
	}

	@Property
	void giveMeObjectToBuilderSetIndex() {
		// given
		IntegerListClass expected = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().size("values", 2, 2))
			.sample();

		// when
		IntegerListClass actual = this.sut.giveMeBuilder(expected)
			.set("values[1]", 1)
			.sample();

		then(actual.values.get(1)).isEqualTo(1);
	}

	@Property
	void giveMeListSpecMaxSize() {
		// when
		StringListClass actual = this.sut.giveMeBuilder(StringListClass.class)
			.spec(new ExpressionSpec()
				.list("values",
					it -> it.ofMaxSize(2)
				)
			)
			.sample();

		then(actual.values.size()).isLessThanOrEqualTo(2);
	}

	@Property
	void giveMeListSpecSizeBetween() {
		// when
		StringListClass actual = this.sut.giveMeBuilder(StringListClass.class)
			.spec(new ExpressionSpec()
				.list("values",
					it -> it.ofSizeBetween(1, 3)
				)
			)
			.sample();

		then(actual.values.size()).isBetween(1, 3);
	}

	@Property
	void giveMeSetAllName() {
		// when
		TwoStringClass actual = this.sut.giveMeBuilder(TwoStringClass.class)
			.set("*", "set")
			.sample();

		then(actual.value1).isEqualTo("set");
		then(actual.value2).isEqualTo("set");
	}

	@Property
	void giveMeListExactSize() {
		// when
		StringListClass actual = this.sut.giveMeBuilder(StringListClass.class)
			.size("values", 3)
			.sample();

		then(actual.values.size()).isEqualTo(3);
	}

	@Property
	void giveMeSizeMap() {
		// when
		MapKeyIntegerValueIntegerClass actual = this.sut.giveMeBuilder(MapKeyIntegerValueIntegerClass.class)
			.size("values", 2, 2)
			.sample();

		then(actual.values).hasSize(2);
	}

	@Property
	void giveMeSetRightOrder() {
		// when
		StringListClass actual = this.sut.giveMeBuilder(StringListClass.class)
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
		StringListClass actual = this.sut.giveMeBuilder(StringListClass.class)
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
		StringListClass actual = this.sut.giveMeBuilder(StringListClass.class)
			.spec(new ExpressionSpec()
				.list("values",
					it -> it.ofMinSize(1)
				)
			)
			.sample();

		then(actual.values.size()).isGreaterThanOrEqualTo(1);
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
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.specAny(specOne, specTwo)
			.sample();

		// then
		IntegerListClass expectedOne = new IntegerListClass();
		expectedOne.values = new ArrayList<>();
		expectedOne.values.add(1);

		IntegerListClass expectedTwo = new IntegerListClass();
		expectedTwo.values = new ArrayList<>();
		expectedTwo.values.add(1);
		expectedTwo.values.add(2);

		then(actual).isIn(expectedOne, expectedTwo);
	}

	@Property
	void giveMeBuilderSetNull() {
		// when
		StringWrapperClass actual = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", null)
			.sample();

		then(actual.value).isNull();
	}

	@Property
	void giveMeMinSize() {
		// when
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.minSize("values", 2)
			.sample();

		then(actual.values.size()).isGreaterThanOrEqualTo(2);
	}

	@Property
	void giveMeMaxSize() {
		// when
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.maxSize("values", 10)
			.sample();

		then(actual.values.size()).isLessThanOrEqualTo(10);
	}

	@Property(tries = 10)
	void giveMeSizeMinMaxBiggerThanDefault() {
		// when
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.size("values", 100, 150)
			.sample();

		then(actual.values.size()).isBetween(100, 150);
	}

	@Property(tries = 10)
	void giveMeSizeMinBiggerThanDefaultMax() {
		// when
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.minSize("values", 100)
			.sample();

		then(actual.values.size()).isBetween(100, 110);
	}

	@Property
	void giveMeSizeMaxSizeIsZero() {
		// when
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.maxSize("values", 0)
			.sample();

		then(actual.values).isEmpty();
	}

	@Property
	void giveMeSizeMaxSizeBeforeMinSizeIsZero() {
		// when
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.maxSize("values", 15)
			.minSize("values", 14)
			.sample();

		then(actual.values.size()).isBetween(14, 15);
	}

	@Property
	void giveMeSizeMinSizeBeforeMaxSizeIsZero() {
		// when
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.minSize("values", 14)
			.maxSize("values", 15)
			.sample();

		then(actual.values.size()).isBetween(14, 15);
	}

	@Property
	void giveMePostConditionLimitIndex() {
		// when
		StringListClass actual = this.sut.giveMeBuilder(StringListClass.class)
			.size("values", 2, 2)
			.setPostCondition("values[*]", String.class, it -> it.length() > 0)
			.setPostCondition("values[*]", String.class, it -> it.length() > 5, 1)
			.sample();

		then(actual.values).anyMatch(it -> it.length() > 5);
	}

	@Property
	void giveMePostConditionLimitIndexNotOverwriteIfLimitIsZeroReturnsNotPostCondition() {
		// when
		StringListClass actual = this.sut.giveMeBuilder(StringListClass.class)
			.setPostCondition("values[*]", String.class, it -> it.length() > 5)
			.setPostCondition("values[*]", String.class, it -> it.length() == 0, 0)
			.sample();

		then(actual.values).allMatch(it -> it.length() > 5);
	}

	@Property
	void giveMeSpecListSetSize() {
		// when
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().list("values", it -> it.ofSize(1)))
			.sample();

		then(actual.values).hasSize(1);
	}

	@Property
	void giveMeSpecListSetElement() {
		// when
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.setElement(0, 1);
			}))
			.sample();

		then(actual.values).hasSize(1);
		then(actual.values.get(0)).isEqualTo(1);
	}

	@Property
	void giveMeSpecListAnySet() {
		// when
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(3);
				it.any(1);
			}))
			.sample();

		then(actual.values).anyMatch(it -> it == 1);
	}

	@Property
	void giveMeSpecListAllSet() {
		// when
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(3);
				it.all(1);
			}))
			.sample();

		then(actual.values).allMatch(it -> it == 1);
	}

	@Property
	void giveMeSpecListPostConditionElement() {
		// when
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.setElementPostCondition(0, Integer.class, postConditioned -> postConditioned > 1);
			}))
			.sample();

		then(actual.values).hasSize(1);
		then(actual.values.get(0)).isGreaterThan(1);
	}

	@Property
	void giveMeSpecListPostConditionElementField() {
		// when
		NestedStringList actual = this.sut.giveMeBuilder(NestedStringList.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.setElementFieldPostCondition(0, "value", String.class,
					postConditioned -> postConditioned.length() > 5);
			}))
			.sample();

		then(actual.values).allMatch(it -> it.value.length() > 5);
	}

	@Property
	void giveMeSpecListListElementSet() {
		// when
		ListListString actual = this.sut.giveMeBuilder(ListListString.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.listElement(0, nestedIt -> {
					nestedIt.ofSize(1);
					nestedIt.setElement(0, "set");
				});
			}))
			.sample();

		then(actual.values).hasSize(1);
		then(actual.values.get(0)).hasSize(1);
		then(actual.values.get(0).get(0)).isEqualTo("set");
	}

	@Property
	void giveMeSpecListListElementPostCondition() {
		// when
		ListListString actual = this.sut.giveMeBuilder(ListListString.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.listElement(0, nestedIt -> {
					nestedIt.ofSize(1);
					nestedIt.setElementPostCondition(0, String.class, postConditioned -> postConditioned.length() > 5);
				});
			}))
			.sample();

		then(actual.values).hasSize(1);
		then(actual.values.get(0)).hasSize(1);
		then(actual.values.get(0).get(0).length()).isGreaterThan(5);
	}

	@Property
	void giveMeSetLimitReturnsNotSet() {
		// when
		IntegerWrapperClass actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.set("value", 1, 0)
			.sample();

		then(actual.value).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeSpecSetWithLimit() {
		// when
		IntegerWrapperClass actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.spec(new ExpressionSpec()
				.set("value", 1, 0))
			.sample();

		then(actual.value).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeSpecSetIndexWithLimitReturns() {
		// when
		StringListClass actual = this.sut.giveMeBuilder(StringListClass.class)
			.spec(new ExpressionSpec()
				.size("values", 2, 2)
				.set("values[*]", "set", 1))
			.sample();

		then(actual.values).anyMatch(it -> !it.equals("set"));
	}

	@Property
	void giveMeSetIndexWithLimitReturns() {
		// when
		StringListClass actual = this.sut.giveMeBuilder(StringListClass.class)
			.size("values", 2, 2)
			.set("values[*]", "set", 1)
			.sample();

		then(actual.values).anyMatch(it -> !it.equals("set"));
	}

	@Property
	void toExpressionSpecSet() {
		// when
		ExpressionSpec actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.set("value", "test")
			.toExpressionSpec();

		then(actual.hasSet("value")).isTrue();
	}

	@Property
	void toExpressionSpecPostCondition() {
		// when
		ExpressionSpec actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.setPostCondition("value", Integer.class, it -> true)
			.toExpressionSpec();

		then(actual.hasPostCondition("value")).isTrue();
	}

	@Property
	void toExpressionSpecMeta() {
		// when
		ExpressionSpec actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.size("value", 1)
			.toExpressionSpec();

		then(actual.hasMetadata("value")).isTrue();
	}

	@Property
	void giveMeSetArbitraryBuilder() {
		// when
		StringIntegerClass actual = this.sut.giveMeBuilder(StringIntegerClass.class)
			.setBuilder("value2", this.sut.giveMeBuilder(IntegerWrapperClass.class).set("value", 1))
			.sample();

		then(actual.value2.value).isEqualTo(1);
	}

	@Property
	void giveMeSpecSetArbitraryBuilder() {
		// when
		StringIntegerClass actual = this.sut.giveMeBuilder(StringIntegerClass.class)
			.spec(new ExpressionSpec().setBuilder("value2",
				this.sut.giveMeBuilder(IntegerWrapperClass.class).set("value", 1))
			)
			.sample();

		then(actual.value2.value).isEqualTo(1);
	}

	@Property
	void giveMeDecomposeNullSetNotNullReturnsNewValue() {
		// given
		StringWrapperClass decomposed = new StringWrapperClass();

		// when
		StringWrapperClass actual = this.sut.giveMeBuilder(decomposed)
			.setNotNull("value")
			.sample();

		then(actual.value).isNotNull();
	}

	@Property
	void giveMeDecomposeNullSetNullReturnsNull() {
		// given
		StringWrapperClass decomposed = new StringWrapperClass();

		// when
		StringWrapperClass actual = this.sut.giveMeBuilder(decomposed)
			.setNull("value")
			.sample();

		then(actual.value).isNull();
	}

	@Data
	public static class IntegerWrapperClass {
		int value;
	}

	@Data
	public static class IntegerListClass {
		List<Integer> values;
	}

	@Data
	public static class StringWrapperClass {
		private String value;
	}

	@Data
	public static class StringListClass {
		private List<String> values;
	}

	@Data
	public static class TwoStringClass {
		private String value1;
		private String value2;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MapKeyIntegerValueIntegerClass {
		private Map<Integer, Integer> values;
	}

	@Data
	public static class ListListString {
		private List<List<String>> values;
	}

	@Data
	public static class NestedStringList {
		private List<StringWrapperClass> values;
	}

	@Data
	public static class StringIntegerClass {
		StringWrapperClass value1;
		IntegerWrapperClass value2;
	}
}
