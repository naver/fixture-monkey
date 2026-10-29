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

import static org.assertj.core.api.BDDAssertions.entry;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.customizer.InnerSpec;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.ListListStringObject;
import com.navercorp.fixturemonkey.test.ValueProjectionAssembleSpecs.AbstractValue;
import com.navercorp.fixturemonkey.test.ValueProjectionAssembleSpecs.ConcreteIntValue;
import com.navercorp.fixturemonkey.test.ValueProjectionAssembleSpecs.ConcreteStringValue;

class NestedContainerDecompositionTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.defaultNotNull(true)
		.build();

	private static final TypeReference<Map<String, Map<String, String>>> NESTED_MAP_TYPE =
		new TypeReference<Map<String, Map<String, String>>>() {
		};

	private static final TypeReference<Map<String, List<String>>> MAP_OF_LISTS_TYPE =
		new TypeReference<Map<String, List<String>>>() {
		};

	private static final TypeReference<List<Map<String, String>>> LIST_OF_MAPS_TYPE =
		new TypeReference<List<Map<String, String>>>() {
		};

	private static final FixtureMonkey ABSTRACT_VALUE_SUT = FixtureMonkey.builder()
		.defaultNotNull(true)
		.plugin(new InterfacePlugin().abstractClassExtends(
			AbstractValue.class,
			Arrays.asList(ConcreteStringValue.class, ConcreteIntValue.class)
		))
		.build();

	private static final TypeReference<List<AbstractValue>> ABSTRACT_VALUE_LIST_TYPE =
		new TypeReference<List<AbstractValue>>() {
		};

	private static final TypeReference<List<List<AbstractValue>>> NESTED_ABSTRACT_VALUE_LIST_TYPE =
		new TypeReference<List<List<AbstractValue>>>() {
		};

	@Test
	void setNestedListThenSetInnerElement() {
		// when
		List<List<String>> actual = SUT.giveMeBuilder(ListListStringObject.class)
			.set("values", nestedValues())
			.set("values[0][0]", "x")
			.sample()
			.getValues();

		// then
		then(actual).containsExactly(Arrays.asList("x", "b"), Arrays.asList("c", "d"));
	}

	@Test
	void setNestedListThenGrowInnerLists() {
		// when
		List<List<String>> actual = SUT.giveMeBuilder(ListListStringObject.class)
			.set("values", nestedValues())
			.size("values[*]", 3)
			.sample()
			.getValues();

		// then
		then(actual).hasSize(2);
		then(actual.get(0)).hasSize(3).startsWith("a", "b");
		then(actual.get(1)).hasSize(3).startsWith("c", "d");
	}

	@Test
	void setNestedListThenGrowOuterListWithWildcardOverride() {
		// when
		List<List<String>> actual = SUT.giveMeBuilder(ListListStringObject.class)
			.set("values", nestedValues())
			.size("values", 3)
			.size("values[*]", 2)
			.set("values[*][*]", "o")
			.sample()
			.getValues();

		// then
		then(actual).hasSize(3);
		then(actual).allMatch(it -> it.equals(Arrays.asList("o", "o")));
	}

	@Test
	void setNestedListThenWildcardOverrideAndExactInnerElement() {
		// when
		List<List<String>> actual = SUT.giveMeBuilder(ListListStringObject.class)
			.set("values", nestedValues())
			.size("values[*]", 2)
			.set("values[*][*]", "o")
			.set("values[1][1]", "z")
			.sample()
			.getValues();

		// then
		then(actual).containsExactly(Arrays.asList("o", "o"), Arrays.asList("o", "z"));
	}

	@Test
	void setNestedUnmodifiableListThenSetInnerElement() {
		// given
		List<List<String>> values = Collections.unmodifiableList(
			Arrays.asList(
				Collections.unmodifiableList(new ArrayList<>(Arrays.asList("a", "b"))),
				Collections.unmodifiableList(new ArrayList<>(Arrays.asList("c", "d")))
			)
		);

		// when
		List<List<String>> actual = SUT.giveMeBuilder(ListListStringObject.class)
			.set("values", values)
			.set("values[1][0]", "x")
			.sample()
			.getValues();

		// then
		then(actual).containsExactly(Arrays.asList("a", "b"), Arrays.asList("x", "d"));
	}

	@Test
	void setNestedMapThenSetInnerValue() {
		// when
		Map<String, Map<String, String>> actual = SUT.giveMeBuilder(NESTED_MAP_TYPE)
			.set("$", nestedMap())
			.set("$[0][value][0][value]", "x")
			.sample();

		// then
		then(actual).containsOnlyKeys("k");
		then(actual.get("k")).containsExactly(entry("ik", "x"));
	}

	@Test
	void setNestedMapThenGrowInnerMap() {
		// when
		Map<String, Map<String, String>> actual = SUT.giveMeBuilder(NESTED_MAP_TYPE)
			.set("$", nestedMap())
			.setInner(new InnerSpec().value(inner -> inner.size(2)))
			.sample();

		// then
		then(actual).containsOnlyKeys("k");
		then(actual.get("k")).hasSize(2).containsEntry("ik", "iv");
	}

	@Test
	void setNestedMapThenSetInnerEntry() {
		// when
		Map<String, Map<String, String>> actual = SUT.giveMeBuilder(NESTED_MAP_TYPE)
			.set("$", nestedMap())
			.setInner(new InnerSpec().value(inner -> inner.size(2).entry("ik2", "iv2")))
			.sample();

		// then
		then(actual).containsOnlyKeys("k");
		then(actual.get("k")).hasSize(2).containsEntry("ik2", "iv2");
	}

	@Test
	void setNestedArrayThenSetInnerElement() {
		// given
		String[][] values = {{"a", "b"}, {"c", "d"}};

		// when
		String[][] actual = SUT.giveMeBuilder(NestedArrayObject.class)
			.set("values", values)
			.set("values[0][0]", "x")
			.sample()
			.getValues();

		// then
		then(actual).hasDimensions(2, 2);
		then(actual[0]).containsExactly("x", "b");
		then(actual[1]).containsExactly("c", "d");
	}

	@Test
	void setMapOfListsThenSetInnerElement() {
		// given
		Map<String, List<String>> map = new HashMap<>();
		map.put("k", Arrays.asList("a", "b"));

		// when
		Map<String, List<String>> actual = SUT.giveMeBuilder(MAP_OF_LISTS_TYPE)
			.set("$", map)
			.set("$[0][value][0]", "x")
			.sample();

		// then
		then(actual).containsOnlyKeys("k");
		then(actual.get("k")).containsExactly("x", "b");
	}

	@Test
	void setListOfMapsThenSetInnerValue() {
		// given
		Map<String, String> inner = new HashMap<>();
		inner.put("k", "v");
		List<Map<String, String>> list = Collections.singletonList(inner);

		// when
		List<Map<String, String>> actual = SUT.giveMeBuilder(LIST_OF_MAPS_TYPE)
			.set("$", list)
			.set("$[0][0][value]", "x")
			.sample();

		// then
		then(actual).hasSize(1);
		then(actual.get(0)).containsExactly(entry("k", "x"));
	}

	@Test
	void setMixedImplementationListThenSetSecondElementProperty() {
		// given
		List<AbstractValue> values = Arrays.asList(stringValue("s"), intValue(1));

		// when
		List<AbstractValue> actual = ABSTRACT_VALUE_SUT.giveMeBuilder(ABSTRACT_VALUE_LIST_TYPE)
			.set("$", values)
			.set("$[1].intValue", 7)
			.sample();

		// then
		then(actual).hasSize(2);
		then(actual.get(0)).isEqualTo(stringValue("s"));
		then(actual.get(1)).isEqualTo(intValue(7));
	}

	@Test
	void setMixedImplementationListThenGrowList() {
		// given
		List<AbstractValue> values = Arrays.asList(stringValue("s"), intValue(1));

		// when
		List<AbstractValue> actual = ABSTRACT_VALUE_SUT.giveMeBuilder(ABSTRACT_VALUE_LIST_TYPE)
			.set("$", values)
			.size("$", 3)
			.sample();

		// then
		then(actual).hasSize(3).startsWith(stringValue("s"), intValue(1));
	}

	@Test
	void setNestedMixedImplementationListThenSetInnerElementProperty() {
		// given
		List<List<AbstractValue>> values = Arrays.asList(
			new ArrayList<>(Collections.singletonList(stringValue("s"))),
			new ArrayList<>(Collections.singletonList(intValue(1)))
		);

		// when
		List<List<AbstractValue>> actual = ABSTRACT_VALUE_SUT.giveMeBuilder(NESTED_ABSTRACT_VALUE_LIST_TYPE)
			.set("$", values)
			.set("$[1][0].intValue", 7)
			.sample();

		// then
		then(actual).containsExactly(
			Collections.singletonList(stringValue("s")),
			Collections.singletonList(intValue(7))
		);
	}

	public static class NestedArrayObject {
		private String[][] values;

		public String[][] getValues() {
			return values;
		}

		public void setValues(String[][] values) {
			this.values = values;
		}
	}

	private static List<List<String>> nestedValues() {
		return Arrays.asList(Arrays.asList("a", "b"), Arrays.asList("c", "d"));
	}

	private static Map<String, Map<String, String>> nestedMap() {
		Map<String, String> inner = new HashMap<>();
		inner.put("ik", "iv");
		Map<String, Map<String, String>> map = new HashMap<>();
		map.put("k", inner);
		return map;
	}

	private static ConcreteStringValue stringValue(String stringValue) {
		ConcreteStringValue value = new ConcreteStringValue();
		value.setValue("v");
		value.setStringValue(stringValue);
		return value;
	}

	private static ConcreteIntValue intValue(int intValue) {
		ConcreteIntValue value = new ConcreteIntValue();
		value.setValue("v");
		value.setIntValue(intValue);
		return value;
	}
}
