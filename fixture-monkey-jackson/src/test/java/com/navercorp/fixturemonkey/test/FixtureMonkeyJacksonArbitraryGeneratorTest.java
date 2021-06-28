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

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Value;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.customizer.ExpressionSpec;
import com.navercorp.fixturemonkey.generator.BeanArbitraryGenerator;
import com.navercorp.fixturemonkey.jackson.generator.JacksonArbitraryGenerator;

class FixtureMonkeyJacksonArbitraryGeneratorTest {
	private final FixtureMonkey sut = FixtureMonkey.builder()
		.defaultGenerator(JacksonArbitraryGenerator.INSTANCE)
		.build();

	@Provide
	Arbitrary<IntegerWrapperClass> specSet() {
		return this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.spec(new ExpressionSpec().set("value", -1))
			.build();
	}

	@Property
	void giveMeSpecSet(@ForAll("specSet") IntegerWrapperClass actual) {
		then(actual.getInteger()).isEqualTo(-1);
	}

	@Provide
	Arbitrary<IntegerWrapperClass> specSetArbitrary() {
		return this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.spec(new ExpressionSpec().set("value", Arbitraries.just(1)))
			.build();
	}

	@Property
	void giveMeSpecSetArbitrary(@ForAll("specSetArbitrary") IntegerWrapperClass actual) {
		then(actual.getInteger()).isEqualTo(1);
	}

	@Provide
	Arbitrary<IntegerWrapperClass> setArbitrary() {
		return this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.set("value", Arbitraries.just(1))
			.build();
	}

	@Property
	void giveMeSetArbitrary(@ForAll("setArbitrary") IntegerWrapperClass actual) {
		then(actual.getInteger()).isEqualTo(1);
	}

	@Provide
	Arbitrary<IntegerListClass> listSize() {
		return this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().size("values", 1, 1))
			.build();
	}

	@Property
	void giveMeListSize(@ForAll("listSize") IntegerListClass actual) {
		then(actual.getList()).hasSize(1);
	}

	@Provide
	Arbitrary<IntegerListClass> setNull() {
		return this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().setNull("values"))
			.build();
	}

	@Property
	void giveMeSetNull(@ForAll("setNull") IntegerListClass actual) {
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

	@Provide
	Arbitrary<IntegerListClass> setAfterSetNull() {
		return this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec()
				.setNull("values")
				.size("values", 1, 1)
				.set("values[0]", 0)
			)
			.build();
	}

	@Property
	void giveMeSetAfterSetNullReturnsNotNull(@ForAll("setAfterSetNull") IntegerListClass actual) {
		then(actual.getList()).isNotNull();
		then(actual.getList()).hasSize(1);
		then(actual.getList().get(0)).isEqualTo(0);
	}

	@Provide
	Arbitrary<IntegerListClass> setNotNullAfterSetNullReturnsNotNull() {
		return this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec()
				.setNull("values")
				.setNotNull("values")
			)
			.build();
	}

	@Property
	void giveMeSetNotNullAfterSetNullReturnsNotNull(
		@ForAll("setNotNullAfterSetNullReturnsNotNull") IntegerListClass actual
	) {
		then(actual.getList()).isNotNull();
	}

	@Provide
	Arbitrary<IntegerListClass> setNullAfterSetNotNull() {
		return this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec()
				.setNotNull("values")
				.setNull("values")
			)
			.build();
	}

	@Property
	void giveMeSetNullAfterSetNotNullReturnsNull(@ForAll("setNullAfterSetNotNull") IntegerListClass actual) {
		then(actual.getList()).isNull();
	}

	@Provide
	Arbitrary<IntegerListClass> setNullAfterSet() {
		return this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec()
				.size("values", 1, 1)
				.set("values[0]", 0)
				.setNull("values")
			)
			.build();
	}

	@Property
	void giveMeSetNullAfterSetReturnsNull(@ForAll("setNullAfterSet") IntegerListClass actual) {
		then(actual.getList()).isNull();
	}

	@Provide
	Arbitrary<StringWrapperClass> specSetPrefix() {
		return this.sut.giveMeBuilder(StringWrapperClass.class)
			.spec(new ExpressionSpec().setPrefix("value", "prefix"))
			.build();
	}

	@Property
	void giveMeSpecSetPrefix(@ForAll("specSetPrefix") StringWrapperClass actual) {
		then(actual.getStr()).startsWith("prefix");
	}

	@Provide
	Arbitrary<StringWrapperClass> specSetSuffix() {
		return this.sut.giveMeBuilder(StringWrapperClass.class)
			.spec(new ExpressionSpec().setSuffix("value", "suffix"))
			.build();
	}

	@Property
	void giveMeSpecSetSuffix(@ForAll("specSetSuffix") StringWrapperClass actual) {
		then(actual.getStr()).endsWith("suffix");
	}

	@Provide
	Arbitrary<IntegerWrapperClass> specPostCondition() {
		return this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.spec(new ExpressionSpec().setPostCondition(
				"value",
				Integer.class,
				value -> value >= 0 && value <= 100
			))
			.build();
	}

	@Property
	void giveMeSpecPostCondition(@ForAll("specPostCondition") IntegerWrapperClass actual) {
		then(actual.getInteger()).isBetween(0, 100);
	}

	@Provide
	Arbitrary<IntegerWrapperClass> specPostConditionType() {
		return this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.spec(new ExpressionSpec().setPostCondition(
				"value",
				Integer.class,
				value -> value >= 0 && value <= 100
			))
			.build();
	}

	@Property
	void giveMeSpecPostConditionType(@ForAll("specPostConditionType") IntegerWrapperClass actual) {
		then(actual.getInteger()).isBetween(0, 100);
	}

	@Provide
	Arbitrary<IntegerListClass> postConditionIndex() {
		return this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec()
				.setPostCondition("values[0]", Integer.class, value -> value >= 0 && value <= 100)
				.size("values", 1, 1))
			.build();
	}

	@Property
	void giveMePostConditionIndex(@ForAll("postConditionIndex") IntegerListClass actual) {
		then(actual.getList()).hasSize(1);
		then(actual.getList().get(0)).isBetween(0, 100);
	}

	@Provide
	Arbitrary<IntegerWrapperClass> objectToBuilderSet() {
		IntegerWrapperClass expected = this.sut.giveMeOne(IntegerWrapperClass.class);

		return this.sut.giveMeBuilder(expected)
			.set("value", 1)
			.build();
	}

	@Property
	void giveMeObjectToBuilderSet(@ForAll("objectToBuilderSet") IntegerWrapperClass actual) {
		then(actual.getInteger()).isEqualTo(1);
	}

	@Provide
	Arbitrary<IntegerListClass> objectToBuilderSetIndex() {
		IntegerListClass expected = this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().size("values", 2, 2))
			.sample();

		return this.sut.giveMeBuilder(expected)
			.set("values[1]", 1)
			.build();
	}

	@Property
	void giveMeObjectToBuilderSetIndex(@ForAll("objectToBuilderSetIndex") IntegerListClass actual) {
		then(actual.getList().get(1)).isEqualTo(1);
	}

	@Provide
	Arbitrary<IntegerArrayClass> arrayToBuilder() {
		IntegerArrayClass expected = new IntegerArrayClass(new Integer[] {1, 2, 3});

		return this.sut.giveMeBuilder(expected).build();
	}

	@Property
	void giveMeArrayToBuilder(@ForAll("arrayToBuilder") IntegerArrayClass actual) {
		Integer[] actualArray = actual.integerArray;
		then(actualArray[0]).isEqualTo(1);
		then(actualArray[1]).isEqualTo(2);
		then(actualArray[2]).isEqualTo(3);
	}

	@Provide
	Arbitrary<IntArrayClass> primitiveArrayToBuilder() {
		IntArrayClass expected = new IntArrayClass(new int[] {1, 2, 3});

		return this.sut.giveMeBuilder(expected).build();
	}

	@Property
	void giveMePrimitiveArrayToBuilder(@ForAll("primitiveArrayToBuilder") IntArrayClass actual) {
		int[] actualArray = actual.intArray;
		then(actualArray[0]).isEqualTo(1);
		then(actualArray[1]).isEqualTo(2);
		then(actualArray[2]).isEqualTo(3);
	}

	@Provide
	Arbitrary<MapKeyIntegerValueIntegerClass> sameKeyValueMapToBuilder() {
		Map<Integer, Integer> values = new HashMap<>();
		values.put(1, 1);
		MapKeyIntegerValueIntegerClass expected = new MapKeyIntegerValueIntegerClass(values);

		return this.sut.giveMeBuilder(expected).build();
	}

	@Property
	void giveMeSameKeyValueMapToBuilder(@ForAll("sameKeyValueMapToBuilder") MapKeyIntegerValueIntegerClass actual) {
		then(actual.integerMap.get(1)).isEqualTo(1);
	}

	@Provide
	Arbitrary<IntegerWrapperClass> setLimitReturnsNotSet() {
		return this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.set("value", 1, 0)
			.build();
	}

	@Property
	void giveMeSetLimitReturnsNotSet(@ForAll("setLimitReturnsNotSet") IntegerWrapperClass actual) {
		then(actual.getInteger()).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Provide
	Arbitrary<IntegerWrapperClass> specSetWithLimit() {
		return this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.spec(new ExpressionSpec()
				.set("value", 1, 0))
			.build();
	}

	@Property
	void giveMeSpecSetWithLimitReturnsNotSet(@ForAll("specSetWithLimit") IntegerWrapperClass actual) {
		then(actual.getInteger()).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Provide
	Arbitrary<StringListClass> specSetIndexWithLimit() {
		return this.sut.giveMeBuilder(StringListClass.class)
			.spec(new ExpressionSpec()
				.size("values", 2, 2)
				.set("values[*]", "set", 1))
			.build();
	}

	@Property
	void giveMeSpecSetIndexWithLimitReturns(@ForAll("specSetIndexWithLimit") StringListClass actual) {
		then(actual.getStringList()).anyMatch(it -> !it.equals("set"));
	}

	@Provide
	Arbitrary<StringListClass> setIndexWithLimit() {
		return this.sut.giveMeBuilder(StringListClass.class)
			.size("values", 2, 2)
			.set("values[*]", "set", 1)
			.build();
	}

	@Property
	void giveMeSetIndexWithLimitReturns(@ForAll("setIndexWithLimit") StringListClass actual) {
		then(actual.getStringList()).anyMatch(it -> !it.equals("set"));
	}

	@Provide
	Arbitrary<StringListClass> postConditionLimitIndex() {
		return this.sut.giveMeBuilder(StringListClass.class)
			.size("values", 2, 2)
			.setPostCondition("values[*]", String.class, it -> it.length() > 0)
			.setPostCondition("values[*]", String.class, it -> it.length() > 5, 1)
			.build();
	}

	@Property
	void giveMePostConditionLimitIndex(@ForAll("postConditionLimitIndex") StringListClass actual) {
		then(actual.getStringList()).anyMatch(it -> it.length() > 5);
	}

	@Provide
	Arbitrary<StringListClass> postConditionLimitIndexZero() {
		return this.sut.giveMeBuilder(StringListClass.class)
			.setPostCondition("values[*]", String.class, it -> it.length() > 5)
			.setPostCondition("values[*]", String.class, it -> it.length() == 0, 0)
			.build();
	}

	@Property
	void giveMePostConditionLimitIndexZeroReturnsNotPostCondition(
		@ForAll("postConditionLimitIndexZero") StringListClass actual) {
		then(actual.getStringList()).allMatch(it -> it.length() > 5);
	}

	@Provide
	Arbitrary<IntegerListClass> specListSetSize() {
		return this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().list("values", it -> it.ofSize(1)))
			.build();
	}

	@Property
	void giveMeSpecListSetSize(@ForAll("specListSetSize") IntegerListClass actual) {
		then(actual.getList()).hasSize(1);
	}

	@Provide
	Arbitrary<IntegerListClass> specListSetElement() {
		return this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.setElement(0, 1);
			}))
			.build();
	}

	@Property
	void giveMeSpecListSetElement(@ForAll("specListSetElement") IntegerListClass actual) {
		then(actual.getList()).hasSize(1);
		then(actual.getList().get(0)).isEqualTo(1);
	}

	@Provide
	Arbitrary<IntegerListClass> specListAnySet() {
		return this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(3);
				it.any(1);
			}))
			.build();
	}

	@Property
	void giveMeSpecListAnySet(@ForAll("specListAnySet") IntegerListClass actual) {
		then(actual.getList()).anyMatch(it -> it == 1);
	}

	@Provide
	Arbitrary<IntegerListClass> specListAllSet() {
		return this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(3);
				it.all(1);
			}))
			.build();
	}

	@Property
	void giveMeSpecListAllSet(@ForAll("specListAllSet") IntegerListClass actual) {
		then(actual.getList()).allMatch(it -> it == 1);
	}

	@Provide
	Arbitrary<IntegerListClass> specListPostConditionElement() {
		return this.sut.giveMeBuilder(IntegerListClass.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.setElementPostCondition(0, Integer.class, postConditioned -> postConditioned > 1);
			}))
			.build();
	}

	@Property
	void giveMeSpecListPostConditionElement(@ForAll("specListPostConditionElement") IntegerListClass actual) {
		then(actual.getList()).hasSize(1);
		then(actual.getList().get(0)).isGreaterThan(1);
	}

	@Provide
	Arbitrary<NestedStringList> specListPostConditionElementField() {
		return this.sut.giveMeBuilder(NestedStringList.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.setElementFieldPostCondition(0, "value", String.class,
					postConditioned -> postConditioned.length() > 5);
			}))
			.build();
	}

	@Property
	void giveMeSpecListPostConditionElementField(@ForAll("specListPostConditionElementField") NestedStringList actual) {
		then(actual.getStringList()).allMatch(it -> it.getStr().length() > 5);
	}

	@Provide
	Arbitrary<ListListString> specListListElementSet() {
		return this.sut.giveMeBuilder(ListListString.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.listElement(0, nestedIt -> {
					nestedIt.ofSize(1);
					nestedIt.setElement(0, "set");
				});
			}))
			.build();
	}

	@Property
	void giveMeSpecListListElementSet(@ForAll("specListListElementSet") ListListString actual) {
		then(actual.getStringListList()).hasSize(1);
		then(actual.getStringListList().get(0)).hasSize(1);
		then(actual.getStringListList().get(0).get(0)).isEqualTo("set");
	}

	@Provide
	Arbitrary<ListListString> specListListElementPostCondition() {
		return this.sut.giveMeBuilder(ListListString.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.listElement(0, nestedIt -> {
					nestedIt.ofSize(1);
					nestedIt.setElementPostCondition(0, String.class, postConditioned -> postConditioned.length() > 5);
				});
			}))
			.build();
	}

	@Property
	void giveMeSpecListListElementPostCondition(@ForAll("specListListElementPostCondition") ListListString actual) {
		then(actual.getStringListList()).hasSize(1);
		then(actual.getStringListList().get(0)).hasSize(1);
		then(actual.getStringListList().get(0).get(0).length()).isGreaterThan(5);
	}

	@Provide
	Arbitrary<StringWrapperClass> mapAndSet() {
		return this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.map(wrapper -> new StringWrapperClass("" + wrapper.getInteger()))
			.set("value", "test")
			.build();
	}

	@Property
	void giveMeMapAndSet(@ForAll("mapAndSet") StringWrapperClass actual) {
		then(actual.getStr()).isEqualTo("test");
	}

	@Provide
	Arbitrary<MapKeyIntegerValueIntegerClass> sizeMap() {
		return this.sut.giveMeBuilder(MapKeyIntegerValueIntegerClass.class)
			.size("values", 2, 2)
			.build();
	}

	@Property
	void giveMeSizeMap(@ForAll("sizeMap") MapKeyIntegerValueIntegerClass actual) {
		then(actual.getIntegerMap()).hasSize(2);
	}

	@Provide
	Arbitrary<StringListClass> setRightOrder() {
		return this.sut.giveMeBuilder(StringListClass.class)
			.spec(new ExpressionSpec()
				.list("values",
					it -> it.ofSize(3)
						.setElement(0, "field1")
						.setElement(1, "field2")
						.setElement(2, "field3")
				)
			)
			.build();
	}

	@Property
	void giveMeSetRightOrder(@ForAll("setRightOrder") StringListClass actual) {
		List<String> values = actual.getStringList();
		then(values.size()).isEqualTo(3);
		then(values.get(0)).isEqualTo("field1");
		then(values.get(1)).isEqualTo("field2");
		then(values.get(2)).isEqualTo("field3");
	}

	@Provide
	Arbitrary<StringListClass> postConditionRightOrder() {
		return this.sut.giveMeBuilder(StringListClass.class)
			.spec(new ExpressionSpec()
				.list("values",
					(it) -> it.ofSize(2)
						.setElementPostCondition(0, String.class, s -> s.length() > 5)
						.setElementPostCondition(1, String.class, s -> s.length() > 10)
				))
			.build();
	}

	@Property
	void giveMePostConditionRightOrder(@ForAll("postConditionRightOrder") StringListClass actual) {
		List<String> values = actual.getStringList();
		then(values.size()).isEqualTo(2);
		then(values.get(0).length()).isGreaterThan(5);
		then(values.get(1).length()).isGreaterThan(10);
	}

	@Provide
	Arbitrary<StringListClass> listSpecMinSize() {
		return this.sut.giveMeBuilder(StringListClass.class)
			.spec(new ExpressionSpec()
				.list("values",
					it -> it.ofMinSize(1)
				)
			)
			.build();
	}

	@Property
	void giveMeListSpecMinSize(@ForAll("listSpecMinSize") StringListClass actual) {
		then(actual.getStringList().size()).isGreaterThanOrEqualTo(1);
	}

	@Provide
	Arbitrary<StringListClass> listSpecMaxSize() {
		return this.sut.giveMeBuilder(StringListClass.class)
			.spec(new ExpressionSpec()
				.list("values",
					it -> it.ofMaxSize(2)
				)
			)
			.build();
	}

	@Property
	void giveMeListSpecMaxSize(@ForAll("listSpecMaxSize") StringListClass actual) {
		then(actual.getStringList().size()).isLessThanOrEqualTo(2);
	}

	@Provide
	Arbitrary<StringListClass> listSpecSizeBetween() {
		return this.sut.giveMeBuilder(StringListClass.class)
			.spec(new ExpressionSpec()
				.list("values",
					it -> it.ofSizeBetween(1, 3)
				)
			)
			.build();
	}

	@Property
	void giveMeListSpecSizeBetween(@ForAll("listSpecSizeBetween") StringListClass actual) {
		then(actual.getStringList().size()).isBetween(1, 3);
	}

	@Property
	void giveMeJsonPropertySet() {
		JsonPropertyClass actual = this.sut.giveMeBuilder(JsonPropertyClass.class)
			.set("jsonValue", "set")
			.sample();

		then(actual.value).isEqualTo("set");
	}

	@Provide
	Arbitrary<JsonPropertyClass> jsonPropertyWithBeanArbitraryGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BeanArbitraryGenerator.INSTANCE)
			.build();

		return sut.giveMeBuilder(JsonPropertyClass.class)
			.set("jsonValue", "set")
			.build();
	}

	@Property
	void giveMeJsonPropertyWithBeanArbitraryGenerator(
		@ForAll("jsonPropertyWithBeanArbitraryGenerator") JsonPropertyClass actual
	) {
		then(actual.value).isNotEqualTo("set");
	}

	@Provide
	Arbitrary<JsonPropertyClass> jsonPropertySetGenerator() {
		return this.sut.giveMeBuilder(JsonPropertyClass.class)
			.generator(BeanArbitraryGenerator.INSTANCE)
			.set("jsonValue", "set")
			.build();
	}

	@Property
	void giveMeJsonPropertySetGenerator(@ForAll("jsonPropertySetGenerator") JsonPropertyClass actual) {
		then(actual.value).isNotEqualTo("set");
	}

	@Provide
	Arbitrary<JsonPropertyClass> jsonPropertySetGeneratorToJackson() {
		return this.sut.giveMeBuilder(JsonPropertyClass.class)
			.generator(BeanArbitraryGenerator.INSTANCE)
			.generator(JacksonArbitraryGenerator.INSTANCE)
			.set("jsonValue", "set")
			.build();
	}

	@Property
	void giveMeJsonPropertySetGeneratorToJackson(
		@ForAll("jsonPropertySetGeneratorToJackson") JsonPropertyClass actual) {
		then(actual.value).isEqualTo("set");
	}

	@Provide
	Arbitrary<JsonPropertyClass> jsonPropertySetFailedAfterDecompose() {
		JsonPropertyClass jsonPropertyClass = new JsonPropertyClass();
		jsonPropertyClass.setValue("jsonValue");

		return this.sut.giveMeBuilder(jsonPropertyClass)
			.generator(BeanArbitraryGenerator.INSTANCE)
			.set("jsonValue", "set")
			.build();
	}

	@Property
	void giveMeJsonPropertySetFailedAfterDecompose(
		@ForAll("jsonPropertySetFailedAfterDecompose") JsonPropertyClass actual
	) {
		then(actual.value).isEqualTo("jsonValue");
	}

	@Provide
	Arbitrary<JsonPropertyClass> jsonPropertySetAfterDecompose() {
		JsonPropertyClass jsonPropertyClass = new JsonPropertyClass();
		jsonPropertyClass.setValue("jsonValue");

		return this.sut.giveMeBuilder(jsonPropertyClass)
			.generator(BeanArbitraryGenerator.INSTANCE)
			.set("value", "set")
			.build();
	}

	@Property
	void giveMeJsonPropertySetAfterDecompose(@ForAll("jsonPropertySetAfterDecompose") JsonPropertyClass actual) {
		then(actual.value).isEqualTo("set");
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

	@Data
	public static class JsonPropertyClass {
		@JsonProperty("jsonValue")
		private String value;
	}
}
