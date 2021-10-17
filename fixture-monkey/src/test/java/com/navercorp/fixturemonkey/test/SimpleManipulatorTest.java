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
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
			.spec(new ExpressionSpec().size("values", 1, 1))
			.sample();

		then(actual.values).hasSize(1);
	}

	@Property
	void giveMeSetNull() {
		// when
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
			.spec(new ExpressionSpec().setNull("values"))
			.sample();

		then(actual.values).isNull();
	}

	@Property
	void giveMeSizeAfterSetNullReturnsNull() {
		// when
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
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
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
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
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
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
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
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
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
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
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
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
	void giveMeObjectToBuilderSetWithExpressionGenerator() {
		// given
		IntegerWrapperClass expected = this.sut.giveMeOne(IntegerWrapperClass.class);

		// when
		IntegerWrapperClass actual = this.sut.giveMeBuilder(expected)
			.set((resolver) -> "value", 1)
			.sample();

		then(actual.value).isEqualTo(1);
	}

	@Property
	void giveMeObjectToBuilderSetIndex() {
		// given
		IntegerListWrapperClass expected = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
			.spec(new ExpressionSpec().size("values", 2, 2))
			.sample();

		// when
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(expected)
			.set("values[1]", 1)
			.sample();

		then(actual.values.get(1)).isEqualTo(1);
	}

	@Property
	void giveMeListSpecMaxSize() {
		// when
		StringListWrapperClass actual = this.sut.giveMeBuilder(StringListWrapperClass.class)
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
		StringListWrapperClass actual = this.sut.giveMeBuilder(StringListWrapperClass.class)
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
		TwoStringWrapperClass actual = this.sut.giveMeBuilder(TwoStringWrapperClass.class)
			.set("*", "set")
			.sample();

		then(actual.value1).isEqualTo("set");
		then(actual.value2).isEqualTo("set");
	}

	@Property
	void giveMeListExactSize() {
		// when
		StringListWrapperClass actual = this.sut.giveMeBuilder(StringListWrapperClass.class)
			.size("values", 3)
			.sample();

		then(actual.values.size()).isEqualTo(3);
	}

	@Property
	void giveMeListExactSizeWithExpressionGenerator() {
		// when
		StringListWrapperClass actual = this.sut.giveMeBuilder(StringListWrapperClass.class)
			.size((resolver) -> "values", 3)
			.sample();

		then(actual.values.size()).isEqualTo(3);
	}

	@Property
	void giveMeSizeMap() {
		// when
		MapKeyIntegerValueIntegerWrapperClass actual =
			this.sut.giveMeBuilder(MapKeyIntegerValueIntegerWrapperClass.class)
				.size("values", 2, 2)
				.sample();

		then(actual.values).hasSize(2);
	}

	@Property
	void giveMeSizeMapWithExpressionGenerator() {
		// when
		MapKeyIntegerValueIntegerWrapperClass actual =
			this.sut.giveMeBuilder(MapKeyIntegerValueIntegerWrapperClass.class)
				.size((resolver) -> "values", 2, 2)
				.sample();

		then(actual.values).hasSize(2);
	}

	@Property
	void giveMeSetRightOrder() {
		// when
		StringListWrapperClass actual = this.sut.giveMeBuilder(StringListWrapperClass.class)
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
		StringListWrapperClass actual = this.sut.giveMeBuilder(StringListWrapperClass.class)
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
		StringListWrapperClass actual = this.sut.giveMeBuilder(StringListWrapperClass.class)
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
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
			.specAny(specOne, specTwo)
			.sample();

		// then
		IntegerListWrapperClass expectedOne = new IntegerListWrapperClass();
		expectedOne.values = new ArrayList<>();
		expectedOne.values.add(1);

		IntegerListWrapperClass expectedTwo = new IntegerListWrapperClass();
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
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
			.minSize("values", 2)
			.sample();

		then(actual.values.size()).isGreaterThanOrEqualTo(2);
	}

	@Property
	void giveMeMinSizeWithExpressionGenerator() {
		// when
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
			.minSize((resolver) -> "values", 2)
			.sample();

		then(actual.values.size()).isGreaterThanOrEqualTo(2);
	}

	@Property
	void giveMeMaxSize() {
		// when
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
			.maxSize("values", 10)
			.sample();

		then(actual.values.size()).isLessThanOrEqualTo(10);
	}

	@Property
	void giveMeMaxSizeWithExpressionGenerator() {
		// when
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
			.maxSize((resolver) -> "values", 10)
			.sample();

		then(actual.values.size()).isLessThanOrEqualTo(10);
	}

	@Property(tries = 10)
	void giveMeSizeMinMaxBiggerThanDefault() {
		// when
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
			.size("values", 100, 150)
			.sample();

		then(actual.values.size()).isBetween(100, 150);
	}

	@Property(tries = 10)
	void giveMeSizeMinBiggerThanDefaultMax() {
		// when
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
			.minSize("values", 100)
			.sample();

		then(actual.values.size()).isBetween(100, 110);
	}

	@Property
	void giveMeSizeMaxSizeIsZero() {
		// when
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
			.maxSize("values", 0)
			.sample();

		then(actual.values).isEmpty();
	}

	@Property
	void giveMeSizeMaxSizeBeforeMinSizeThenMinSizeWorks() {
		// when
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
			.maxSize("values", 15)
			.minSize("values", 14)
			.sample();

		then(actual.values).hasSizeGreaterThanOrEqualTo(14);
	}

	@Property
	void giveMeSizeMinSizeBeforeMaxSizeThenMaxSizeWorks() {
		// when
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
			.minSize("values", 14)
			.maxSize("values", 15)
			.sample();

		then(actual.values).hasSizeLessThanOrEqualTo(15);
	}

	@Property
	void giveMePostConditionLimitIndex() {
		// when
		StringListWrapperClass actual = this.sut.giveMeBuilder(StringListWrapperClass.class)
			.size("values", 2, 2)
			.setPostCondition("values[*]", String.class, it -> it.length() > 0)
			.setPostCondition("values[*]", String.class, it -> it.length() > 5, 1)
			.sample();

		then(actual.values).anyMatch(it -> it.length() > 5);
	}

	@Property
	void giveMePostConditionLimitIndexWithExpressionGenerator() {
		// when
		StringListWrapperClass actual = this.sut.giveMeBuilder(StringListWrapperClass.class)
			.size("values", 2, 2)
			.setPostCondition((resolver) -> "values[*]", String.class, it -> it.length() > 0)
			.setPostCondition((resolver) -> "values[*]", String.class, it -> it.length() > 5, 1)
			.sample();

		then(actual.values).anyMatch(it -> it.length() > 5);
	}

	@Property
	void giveMePostConditionLimitIndexNotOverwriteIfLimitIsZeroReturnsNotPostCondition() {
		// when
		StringListWrapperClass actual = this.sut.giveMeBuilder(StringListWrapperClass.class)
			.setPostCondition("values[*]", String.class, it -> it.length() > 5)
			.setPostCondition("values[*]", String.class, it -> it.length() == 0, 0)
			.sample();

		then(actual.values).allMatch(it -> it.length() > 5);
	}

	@Property
	void giveMeSpecListSetSize() {
		// when
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
			.spec(new ExpressionSpec().list("values", it -> it.ofSize(1)))
			.sample();

		then(actual.values).hasSize(1);
	}

	@Property
	void giveMeSpecListSetElement() {
		// when
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
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
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
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
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
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
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
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
		NestedStringWrapperListClass actual = this.sut.giveMeBuilder(NestedStringWrapperListClass.class)
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
		StringListWrapperClass actual = this.sut.giveMeBuilder(StringListWrapperClass.class)
			.spec(new ExpressionSpec()
				.size("values", 2, 2)
				.set("values[*]", "set", 1))
			.sample();

		then(actual.values).anyMatch(it -> !it.equals("set"));
	}

	@Property
	void giveMeSetIndexWithLimitReturns() {
		// when
		StringListWrapperClass actual = this.sut.giveMeBuilder(StringListWrapperClass.class)
			.size("values", 2, 2)
			.set("values[*]", "set", 1)
			.sample();

		then(actual.values).anyMatch(it -> !it.equals("set"));
	}

	@Property
	void giveMeSetArbitraryBuilder() {
		// when
		StringWrapperIntegerWrapperClass actual = this.sut.giveMeBuilder(StringWrapperIntegerWrapperClass.class)
			.setBuilder("value2", this.sut.giveMeBuilder(IntegerWrapperClass.class).set("value", 1))
			.sample();

		then(actual.value2.value).isEqualTo(1);
	}

	@Property
	void giveMeSetArbitraryBuilderWithExpressionGenerator() {
		// when
		StringWrapperIntegerWrapperClass actual = this.sut.giveMeBuilder(StringWrapperIntegerWrapperClass.class)
			.setBuilder((resolver) -> "value2", this.sut.giveMeBuilder(IntegerWrapperClass.class).set("value", 1))
			.sample();

		then(actual.value2.value).isEqualTo(1);
	}

	@Property
	void giveMeSpecSetArbitraryBuilder() {
		// when
		StringWrapperIntegerWrapperClass actual = this.sut.giveMeBuilder(StringWrapperIntegerWrapperClass.class)
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
	void giveMeDecomposeNullSetNotNullReturnsNewValueWithExpressionGenerator() {
		// given
		StringWrapperClass decomposed = new StringWrapperClass();

		// when
		StringWrapperClass actual = this.sut.giveMeBuilder(decomposed)
			.setNotNull((resolver) -> "value")
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

	@Property
	void giveMeDecomposeNullSetNullReturnsNullWithExpressionGenerator() {
		// given
		StringWrapperClass decomposed = new StringWrapperClass();

		// when
		StringWrapperClass actual = this.sut.giveMeBuilder(decomposed)
			.setNull((resolver) -> "value")
			.sample();

		then(actual.value).isNull();
	}

	@Property
	void giveMeSetPostConditionForRoot() {
		// when
		String actual = this.sut.giveMeBuilder(String.class)
			.setPostCondition(it -> it.length() > 5)
			.sample();

		then(actual).hasSizeGreaterThan(5);
	}

	@Property
	void giveMeSetPostConditionForRootWithJsonPathRootExpression() {
		// when
		String actual = this.sut.giveMeBuilder(String.class)
			.setPostCondition("$", String.class, it -> it.length() > 5)
			.sample();

		then(actual).hasSizeGreaterThan(5);
	}

	@Property
	void giveMeSetForRoot() {
		// when
		String actual = this.sut.giveMeBuilder(String.class)
			.set("test")
			.sample();

		then(actual).isEqualTo("test");
	}

	@Property
	void giveMeSetForRootWithJsonPathRootExpression() {
		// when
		String actual = this.sut.giveMeBuilder(String.class)
			.set("$", "test")
			.sample();

		then(actual).isEqualTo("test");
	}

	@Property
	void giveMeSetNullForRoot() {
		// when
		String actual = this.sut.giveMeBuilder(String.class)
			.set(null)
			.sample();

		then(actual).isNull();
	}

	@Property
	void giveMeSetArbitraryForRoot() {
		// when
		String actual = this.sut.giveMeBuilder(String.class)
			.set(Arbitraries.strings().ofLength(2))
			.sample();

		then(actual).hasSize(2);
	}

	@Property
	void giveMeSizeZeroReturnsEmpty() {
		// when
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
			.size("values", 0)
			.sample();

		then(actual.values).isEmpty();
	}

	@Property
	void giveMeSizeMinSizeBiggerThanDefaultMaxSize() {
		// given
		int minSize = DEFAULT_ELEMENT_MAX_SIZE + 1;

		// when
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
			.minSize("values", minSize)
			.sample();

		then(actual.values).hasSizeGreaterThanOrEqualTo(minSize);
	}

	@Property
	void giveMeMaxSizeLessThanFixedMinSize() {
		// when
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
			.minSize("values", 2)
			.maxSize("values", 3)
			.fixed()
			.maxSize("values", 1)
			.sample();

		then(actual.values).hasSizeLessThanOrEqualTo(1);
	}

	@Property
	void giveMeMaxSizeLessThanFixedMaxSize() {
		// when
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
			.minSize("values", 1)
			.maxSize("values", 3)
			.fixed()
			.maxSize("values", 2)
			.sample();

		then(actual.values).hasSizeLessThanOrEqualTo(2);
	}

	@Property
	void giveMeSizeInFixedArbitrarySize() {
		// when
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
			.minSize("values", 1)
			.maxSize("values", 3)
			.fixed()
			.size("values", 2)
			.sample();

		then(actual.values).hasSize(2);
	}

	@Property
	void giveMeMinSizeLessThenFixedMaxSize() {
		// when
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
			.minSize("values", 1)
			.maxSize("values", 3)
			.fixed()
			.minSize("values", 2)
			.sample();

		then(actual.values).hasSizeGreaterThanOrEqualTo(2);
	}

	@Property
	void giveMeMinSizeGreaterThanFixedMaxSize() {
		// when
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
			.minSize("values", 1)
			.maxSize("values", 3)
			.fixed()
			.minSize("values", 4)
			.sample();

		then(actual.values).hasSizeGreaterThanOrEqualTo(4);
	}

	@Property
	void giveMeIncludeFixedArbitrarySize() {
		// when
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
			.minSize("values", 1)
			.maxSize("values", 3)
			.fixed()
			.minSize("values", 0)
			.maxSize("values", 4)
			.sample();

		then(actual.values).hasSizeBetween(0, 4);
	}

	@Data
	public static class IntegerWrapperClass {
		int value;
	}

	@Data
	public static class IntegerListWrapperClass {
		List<Integer> values;
	}

	@Data
	public static class StringWrapperClass {
		private String value;
	}

	@Data
	public static class StringListWrapperClass {
		private List<String> values;
	}

	@Data
	public static class TwoStringWrapperClass {
		private String value1;
		private String value2;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MapKeyIntegerValueIntegerWrapperClass {
		private Map<Integer, Integer> values;
	}

	@Data
	public static class ListListString {
		private List<List<String>> values;
	}

	@Data
	public static class NestedStringWrapperListClass {
		private List<StringWrapperClass> values;
	}

	@Data
	public static class StringWrapperIntegerWrapperClass {
		StringWrapperClass value1;
		IntegerWrapperClass value2;
	}
}
