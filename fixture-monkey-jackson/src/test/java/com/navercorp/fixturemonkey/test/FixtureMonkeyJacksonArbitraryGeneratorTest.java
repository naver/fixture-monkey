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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Property;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.customizer.ExpressionSpec;
import com.navercorp.fixturemonkey.jackson.generator.JacksonArbitraryGenerator;

class FixtureMonkeyJacksonArbitraryGeneratorTest {
	private final FixtureMonkey sut = FixtureMonkey.builder()
		.defaultGenerator(JacksonArbitraryGenerator.INSTANCE)
		.build();

	@Property
	void giveMeSpecSet() {
		int expected = -1;

		IntegerWrapperClass actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.spec(new ExpressionSpec().set("value", expected))
			.build()
			.sample();

		then(actual.getInteger()).isEqualTo(expected);
	}

	@Property
	void giveMeSpecSetArbitrary() {
		IntegerWrapperClass actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.spec(new ExpressionSpec().set("value", Arbitraries.just(1)))
			.sample();

		then(actual.getInteger()).isEqualTo(1);
	}

	@Property
	void giveMeSetArbitrary() {
		Arbitrary<IntegerWrapperClass> builder = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.set("value", Arbitraries.just(1)).build();

		then(builder.sample().getInteger()).isEqualTo(1);
	}

	@Property
	void giveMeListSize() {
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().size("values", 1, 1))
			.build()
			.sample();

		then(actual.getList()).hasSize(1);
	}

	@Property
	void giveMeSetNull() {
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().setNull("values"))
			.sample();

		then(actual.getList()).isNull();
	}

	@Property
	void giveMeSizeAfterSetNullReturnsNull() {
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec()
				.setNull("values")
				.size("values", 1, 1)
			)
			.sample();

		then(actual.getList()).isNull();
	}

	@Property
	void giveMeSetAfterSetNullReturnsNotNull() {
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec()
				.setNull("values")
				.size("values", 1, 1)
				.set("values[0]", 0)
			)
			.sample();

		then(actual.getList()).isNotNull();
		then(actual.getList()).hasSize(1);
		then(actual.getList().get(0)).isEqualTo(0);
	}

	@Property
	void giveMeSetNotNullAfterSetNullReturnsNotNull() {
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec()
				.setNull("values")
				.setNotNull("values")
			)
			.sample();

		then(actual.getList()).isNotNull();
	}

	@Property
	void giveMeSetNullAfterSetNotNullReturnsNull() {
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec()
				.setNotNull("values")
				.setNull("values")
			)
			.sample();

		then(actual.getList()).isNull();
	}

	@Property
	void giveMeSetNullAfterSetReturnsNull() {
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec()
				.size("values", 1, 1)
				.set("values[0]", 0)
				.setNull("values")
			)
			.sample();

		then(actual.getList()).isNull();
	}

	@Property
	void giveMeSpecSetPrefix() {
		StringWrapperClass actual = this.sut.giveMeBuilder(StringWrapperClass.class)
			.spec(new ExpressionSpec().setPrefix("value", "prefix"))
			.sample();

		then(actual.getStr()).startsWith("prefix");
	}

	@Property
	void giveMeSpecSetSuffix() {
		StringWrapperClass actual = this.sut.giveMeBuilder(StringWrapperClass.class)
			.spec(new ExpressionSpec().setSuffix("value", "suffix"))
			.sample();

		then(actual.getStr()).endsWith("suffix");
	}

	@Property
	void giveMeSpecFilter() {
		IntegerWrapperClass actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.spec(new ExpressionSpec().<Integer>filter(
				"value",
				value -> value >= 0 && value <= 100
			))
			.sample();

		then(actual.getInteger()).isBetween(0, 100);
	}

	@Property
	void giveMeSpecFilterType() {
		IntegerWrapperClass actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.spec(new ExpressionSpec().filterInteger(
				"value",
				value -> value >= 0 && value <= 100
			))
			.sample();

		then(actual.getInteger()).isBetween(0, 100);
	}

	@Property
	void giveMeFilterIndex() {
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec()
				.filterInteger("values[0]", value -> value >= 0 && value <= 100)
				.size("values", 1, 1))
			.sample();

		then(actual.getList()).hasSize(1);
		then(actual.getList().get(0)).isBetween(0, 100);
	}

	@Property
	void giveMeObjectToBuilderSet() {
		IntegerWrapperClass expected = this.sut.giveMeOne(IntegerWrapperClass.class);

		ArbitraryBuilder<IntegerWrapperClass> actual = this.sut.giveMeBuilder(expected)
			.set("value", 1);

		then(actual.sample().getInteger()).isEqualTo(1);
	}

	@Property
	void giveMeObjectToBuilderSetIndex() {
		IntegerListClass expected = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().size("values", 2, 2))
			.sample();

		ArbitraryBuilder<IntegerListClass> actual = this.sut.giveMeBuilder(expected)
			.set("values[1]", 1);

		then(actual.sample().getList().get(1)).isEqualTo(1);
	}

	@Property
	void giveMeArrayToBuilder() {
		IntegerArrayClass expected = new IntegerArrayClass(new Integer[] {1, 2, 3});

		IntegerArrayClass actual = this.sut.giveMeBuilder(expected).sample();

		then(actual).isEqualTo(expected);
	}

	@Property
	void giveMePrimitiveArrayToBuilder() {
		IntArrayClass expected = new IntArrayClass(new int[] {1, 2, 3});

		IntArrayClass actual = this.sut.giveMeBuilder(expected).sample();

		then(actual).isEqualTo(expected);
	}

	@Property
	void giveMeSameKeyValueMapToBuilder() {
		Map<Integer, Integer> values = new HashMap<>();
		values.put(1, 1);
		MapKeyIntegerValueIntegerClass expected = new MapKeyIntegerValueIntegerClass(values);

		MapKeyIntegerValueIntegerClass actual = this.sut.giveMeBuilder(expected).sample();

		then(actual).isEqualTo(expected);
	}

	@Property
	void giveMeSetLimitReturnsNotSet() {
		IntegerWrapperClass actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.set("value", 1, 0)
			.sample();

		then(actual.getInteger()).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeSpecSetWithLimitReturnsNotSet() {
		IntegerWrapperClass actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.spec(new ExpressionSpec()
				.set("value", 1, 0))
			.sample();

		then(actual.getInteger()).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeSpecSetIndexWithLimitReturns() {
		StringListClass actual = this.sut.giveMeBuilder(StringListClass.class)
			.spec(new ExpressionSpec()
				.size("values", 2, 2)
				.set("values[*]", "set", 1))
			.sample();

		then(actual.getStringList()).anyMatch(it -> !it.equals("set"));
	}

	@Property
	void giveMeSetIndexWithLimitReturns() {
		StringListClass actual = this.sut.giveMeBuilder(StringListClass.class)
			.size("values", 2, 2)
			.set("values[*]", "set", 1)
			.sample();

		then(actual.getStringList()).anyMatch(it -> !it.equals("set"));
	}

	@Property
	void giveMeFilterLimitIndex() {
		StringListClass actual = this.sut.giveMeBuilder(StringListClass.class)
			.size("values", 2, 2)
			.filter(String.class, "values[*]", it -> it.length() > 0)
			.filter(String.class, "values[*]", it -> it.length() > 5, 1)
			.sample();

		then(actual.getStringList()).anyMatch(it -> it.length() > 5);
	}

	@Property
	void giveMeFilterLimitIndexReturnsNotFilter() {
		StringListClass actual = this.sut.giveMeBuilder(StringListClass.class)
			.filter(String.class, "values[*]", it -> it.length() > 5)
			.filter(String.class, "values[*]", it -> it.length() == 0, 0)
			.sample();

		then(actual.getStringList()).allMatch(it -> it.length() > 5);
	}

	@Property
	void giveMeSpecListSetSize() {
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().list("values", it -> it.ofSize(1)))
			.sample();

		then(actual.getList()).hasSize(1);
	}

	@Property
	void giveMeSpecListSetElement() {
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.setElement(0, 1);
			}))
			.sample();

		then(actual.getList()).hasSize(1);
		then(actual.getList().get(0)).isEqualTo(1);
	}

	@Property
	void giveMeSpecListAnySet() {
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(3);
				it.any(1);
			}))
			.sample();

		then(actual.getList()).anyMatch(it -> it == 1);
	}

	@Property
	void giveMeSpecListAllSet() {
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(3);
				it.all(1);
			}))
			.sample();

		then(actual.getList()).allMatch(it -> it == 1);
	}

	@Property
	void giveMeSpecListFilterElement() {
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.<Integer>filterElement(0, filtered -> filtered > 1);
			}))
			.sample();

		then(actual.getList()).hasSize(1);
		then(actual.getList().get(0)).isGreaterThan(1);
	}

	@Property
	void giveMeSpecListAnyFilter() {
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(3);
				it.<Integer>any(filtered -> filtered > 1);
			}))
			.sample();

		then(actual.getList()).anyMatch(it -> it > 1);
	}

	@Property
	void giveMeSpecListAllFilter() {
		IntegerListClass actual = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(3);
				it.<Integer>all(filtered -> filtered > 1);
			}))
			.sample();

		then(actual.getList()).allMatch(it -> it > 1);
	}

	@Property
	void giveMeSpecListFilterElementField() {
		NestedStringList actual = this.sut.giveMeBuilder(NestedStringList.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.<String>filterElementField(0, "value", filtered -> filtered.length() > 5);
			}))
			.sample();

		then(actual.getStringList()).allMatch(it -> it.getStr().length() > 5);
	}

	@Property
	void giveMeSpecListListElementSet() {
		ListListString actual = this.sut.giveMeBuilder(ListListString.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.listElement(0, nestedIt -> {
					nestedIt.ofSize(1);
					nestedIt.setElement(0, "set");
				});
			}))
			.sample();

		then(actual.getStringListList()).hasSize(1);
		then(actual.getStringListList().get(0)).hasSize(1);
		then(actual.getStringListList().get(0).get(0)).isEqualTo("set");
	}

	@Property
	void giveMeSpecListListElementFilter() {
		ListListString actual = this.sut.giveMeBuilder(ListListString.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.listElement(0, nestedIt -> {
					nestedIt.ofSize(1);
					nestedIt.<String>filterElement(0, filtered -> filtered.length() > 5);
				});
			}))
			.sample();

		then(actual.getStringListList()).hasSize(1);
		then(actual.getStringListList().get(0)).hasSize(1);
		then(actual.getStringListList().get(0).get(0).length()).isGreaterThan(5);
	}

	@Property
	void giveMeMapAndSet() {
		StringWrapperClass actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.map(wrapper -> new StringWrapperClass("" + wrapper.getInteger()))
			.set("value", "test")
			.build()
			.sample();

		then(actual.getStr()).isEqualTo("test");
	}

	@Property
	void giveMeSizeMap() {
		MapKeyIntegerValueIntegerClass actual = this.sut.giveMeBuilder(MapKeyIntegerValueIntegerClass.class)
			.size("values", 2, 2)
			.sample();

		then(actual.getIntegerMap()).hasSize(2);
	}

	@Property
	void giveMeSetRightOrder() {
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

		List<String> values = actual.getStringList();
		then(values.size()).isEqualTo(3);
		then(values.get(0)).isEqualTo("field1");
		then(values.get(1)).isEqualTo("field2");
		then(values.get(2)).isEqualTo("field3");
	}

	@Property
	void giveMeFilterRightOrder() {
		StringListClass actual = this.sut.giveMeBuilder(StringListClass.class)
			.spec(new ExpressionSpec()
				.list("values",
					(it) -> it.ofSize(2)
						.filterElement(0, (Predicate<String>)s -> s.length() > 5)
						.filterElement(1, (Predicate<String>)s -> s.length() > 10)
				))
			.sample();

		List<String> values = actual.getStringList();
		then(values.size()).isEqualTo(2);
		then(values.get(0).length()).isGreaterThan(5);
		then(values.get(1).length()).isGreaterThan(10);
	}

	@Property
	void giveMeListSpecMinSize() {
		StringListClass actual = this.sut.giveMeBuilder(StringListClass.class)
			.spec(new ExpressionSpec()
				.list("values",
					it -> it.ofMinSize(1)
				)
			).sample();

		then(actual.getStringList().size()).isGreaterThanOrEqualTo(1);
	}

	@Property
	void giveMeListSpecMaxSize() {
		StringListClass actual = this.sut.giveMeBuilder(StringListClass.class)
			.spec(new ExpressionSpec()
				.list("values",
					it -> it.ofMaxSize(2)
				)
			)
			.sample();

		then(actual.getStringList().size()).isLessThanOrEqualTo(2);
	}

	@Property
	void giveMeListSpecSizeBetween() {
		StringListClass actual = this.sut.giveMeBuilder(StringListClass.class)
			.spec(new ExpressionSpec()
				.list("values",
					it -> it.ofSizeBetween(1, 3)
				)
			)
			.sample();

		then(actual.getStringList().size()).isBetween(1, 3);
	}

	@Value
	private static class IntegerWrapperClass {
		@JsonProperty("value")
		int integer;
	}

	@Value
	private static class IntegerListClass {
		@JsonProperty("values")
		List<Integer> list;
	}

	@Value
	private static class StringWrapperClass {
		@JsonProperty("value")
		String str;
	}

	@Value
	private static class IntegerArrayClass {
		@JsonProperty("value")
		Integer[] integerArray;
	}

	@Value
	private static class IntArrayClass {
		@JsonProperty("value")
		int[] intArray;
	}

	@Value
	private static class MapKeyIntegerValueIntegerClass {
		@JsonProperty("values")
		Map<Integer, Integer> integerMap;
	}

	@Value
	private static class StringListClass {
		@JsonProperty("values")
		List<String> stringList;
	}

	@Value
	private static class NestedStringList {
		@JsonProperty("values")
		List<StringWrapperClass> stringList;
	}

	@Value
	private static class ListListString {
		@JsonProperty("values")
		List<List<String>> stringListList;
	}
}
