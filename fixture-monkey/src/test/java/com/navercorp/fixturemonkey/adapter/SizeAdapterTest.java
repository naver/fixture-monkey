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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.jqwik.api.Property;
import net.jqwik.api.PropertyDefaults;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.ComplexObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.DoubleNestedStringListWrapper;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.SimpleObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringListWrapper;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringWrapperList;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.TwoEnum;

@PropertyDefaults(tries = 10)
class SizeAdapterTest {

	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.defaultNotNull(true)
		.plugin(new JavaNodeTreeAdapterPlugin())
		.build();

	@Property
	void sizeZero() {
		// when
		StringListWrapper actual = SUT.giveMeBuilder(StringListWrapper.class).size("values", 0).sample();

		// then
		then(actual.getValues()).hasSize(0);
	}

	@Property
	void size() {
		// when
		StringListWrapper actual = SUT.giveMeBuilder(StringListWrapper.class).size("values", 10).sample();

		// then
		then(actual.getValues()).hasSize(10);
	}

	@Property
	void sizeMinMax() {
		// when
		StringListWrapper actual = SUT.giveMeBuilder(StringListWrapper.class).size("values", 3, 8).sample();

		// then
		then(actual.getValues()).hasSizeBetween(3, 8);
	}

	@Property
	void minSize() {
		// when
		StringListWrapper actual = SUT.giveMeBuilder(StringListWrapper.class).minSize("values", 10).sample();

		// then
		then(actual.getValues()).hasSizeGreaterThanOrEqualTo(10);
	}

	@Property
	void maxSize() {
		// when
		StringListWrapper actual = SUT.giveMeBuilder(StringListWrapper.class).maxSize("values", 10).sample();

		// then
		then(actual.getValues()).hasSizeLessThanOrEqualTo(10);
	}

	@Property
	void maxSizeZero() {
		// when
		StringListWrapper actual = SUT.giveMeBuilder(StringListWrapper.class).maxSize("values", 0).sample();

		// then
		then(actual.getValues()).hasSizeLessThanOrEqualTo(0);
	}

	@Property
	void sizeComplexObjectList() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class).size("list", 5).sample();

		// then
		then(actual.getList()).hasSize(5);
	}

	@Property
	void sizeComplexObjectMap() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class).size("map", 3).sample();

		// then
		then(actual.getMap()).hasSize(3);
	}

	@Property
	void sizeAndSetElement() {
		String expected = "test";

		List<String> actual = SUT.giveMeBuilder(StringListWrapper.class)
			.size("values", 2)
			.set("values[0]", expected)
			.set("values[1]", expected)
			.sample()
			.getValues();

		then(actual).hasSize(2);
		then(actual.get(0)).isEqualTo(expected);
		then(actual.get(1)).isEqualTo(expected);
	}

	@Property
	void sizeSmallerRemains() {
		String expected = "test";

		List<String> actual = SUT.giveMeBuilder(ComplexObject.class)
			.size("strList", 2)
			.set("strList[0]", expected)
			.set("strList[1]", expected)
			.size("strList", 1)
			.sample()
			.getStrList();

		then(actual).hasSize(1);
		then(actual.get(0)).isEqualTo(expected);
	}

	@Property
	void greaterSizeAfterSetReturnsSizeRemainsSet() {
		List<String> set = new ArrayList<>();
		set.add("1");

		List<String> actual = SUT.giveMeBuilder(new TypeReference<List<String>>() {
			})
			.set("$", set)
			.size("$", 3)
			.sample();

		then(actual).hasSize(3);
		then(actual.get(0)).isEqualTo("1");
	}

	@Property
	void smallerSizeAfterSetReturnsSizeRemainsSet() {
		List<String> set = new ArrayList<>();
		set.add("1");
		set.add("2");
		set.add("3");
		set.add("4");
		set.add("5");

		List<String> actual = SUT.giveMeBuilder(new TypeReference<List<String>>() {
			})
			.set("$", set)
			.size("$", 3)
			.sample();

		then(actual).hasSize(3);
		then(actual.get(0)).isEqualTo("1");
		then(actual.get(1)).isEqualTo("2");
		then(actual.get(2)).isEqualTo("3");
	}

	@Property
	void sizeAfterSetEmptyListReturnsSized() {
		List<String> actual = SUT.giveMeBuilder(new TypeReference<List<String>>() {
			})
			.set("$", new ArrayList<>())
			.size("$", 3)
			.sample();

		then(actual).hasSize(3);
	}

	@Property
	void sizeThirdNestedNested() {
		List<String> actual = SUT.giveMeBuilder(DoubleNestedStringListWrapper.class)
			.size("values", 1)
			.size("values[0].values", 1)
			.size("values[0].values[0].values", 1)
			.sample()
			.getValues()
			.get(0)
			.getValues()
			.get(0)
			.getValues();

		then(actual).hasSize(1);
	}

	@Property
	void sampleStringWrapperListWithSize() {
		// when
		StringWrapperList actual = SUT.giveMeBuilder(StringWrapperList.class).size("values", 3).sample();

		// then
		then(actual.getValues()).hasSize(3);
	}

	@Property
	void sizeEnumSetGreaterThanEnumSizeNotThrows() {
		Set<TwoEnum> actual = SUT.giveMeBuilder(new TypeReference<Set<TwoEnum>>() {
		}).size("$", 3).sample();

		then(actual).hasSize(2);
	}

	@Property
	void sizeEnumMapGreaterThanEnumSizeNotThrows() {
		Map<TwoEnum, String> actual = SUT.giveMeBuilder(new TypeReference<Map<TwoEnum, String>>() {
			})
			.size("$", 3)
			.sample();

		then(actual).hasSize(2);
	}

	@Property
	void setDecomposedMapThenGreaterSize() {
		// given
		Map<String, String> set = new HashMap<>();
		set.put("a", "1");
		set.put("b", "2");

		// when
		Map<String, String> actual = SUT.giveMeBuilder(new TypeReference<Map<String, String>>() {
			})
			.set("$", set)
			.size("$", 4)
			.sample();

		// then
		then(actual).hasSize(4);
		then(actual).containsEntry("a", "1");
		then(actual).containsEntry("b", "2");
	}

	@Property
	void setDecomposedMapThenSmallerSize() {
		// given
		Map<String, String> set = new HashMap<>();
		set.put("a", "1");
		set.put("b", "2");
		set.put("c", "3");

		// when
		Map<String, String> actual = SUT.giveMeBuilder(new TypeReference<Map<String, String>>() {
			})
			.set("$", set)
			.size("$", 2)
			.sample();

		// then
		then(actual).hasSize(2);
	}

	@Property
	void sizeThenSetDecomposedMap() {
		// given
		Map<String, String> set = new HashMap<>();
		set.put("a", "1");
		set.put("b", "2");

		// when
		Map<String, String> actual = SUT.giveMeBuilder(new TypeReference<Map<String, String>>() {
			})
			.size("$", 5)
			.set("$", set)
			.sample();

		// then
		then(actual).hasSize(2);
		then(actual).containsEntry("a", "1");
		then(actual).containsEntry("b", "2");
	}

	@Property
	void setDecomposedMapFieldThenGreaterSize() {
		// given
		Map<String, SimpleObject> set = new HashMap<>();
		set.put("a", new SimpleObject());
		set.put("b", new SimpleObject());

		// when
		Map<String, SimpleObject> actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("map", set)
			.size("map", 4)
			.sample()
			.getMap();

		// then
		then(actual).hasSize(4);
	}

	@Property
	void setEmptyMapThenSize() {
		// when
		Map<String, String> actual = SUT.giveMeBuilder(new TypeReference<Map<String, String>>() {
			})
			.set("$", new HashMap<>())
			.size("$", 3)
			.sample();

		// then
		then(actual).hasSize(3);
	}

	@Property
	void setDecomposedMapThenSizeZero() {
		// given
		Map<String, String> set = new HashMap<>();
		set.put("a", "1");
		set.put("b", "2");

		// when
		Map<String, String> actual = SUT.giveMeBuilder(new TypeReference<Map<String, String>>() {
			})
			.set("$", set)
			.size("$", 0)
			.sample();

		// then
		then(actual).hasSize(0);
	}

	@Property
	void setDecomposedSetThenGreaterSize() {
		// given
		Set<String> set = new HashSet<>();
		set.add("a");
		set.add("b");

		// when
		Set<String> actual = SUT.giveMeBuilder(new TypeReference<Set<String>>() {
		}).set("$", set).size("$", 5).sample();

		// then
		then(actual).hasSize(5);
		then(actual).contains("a", "b");
	}

	@Property
	void setDecomposedSetThenSmallerSize() {
		// given
		Set<String> set = new HashSet<>();
		set.add("a");
		set.add("b");
		set.add("c");

		// when
		Set<String> actual = SUT.giveMeBuilder(new TypeReference<Set<String>>() {
		}).set("$", set).size("$", 2).sample();

		// then
		then(actual).hasSize(2);
	}

	@Property
	void sizeThenSetDecomposedSet() {
		// given
		Set<String> set = new HashSet<>();
		set.add("a");
		set.add("b");

		// when
		Set<String> actual = SUT.giveMeBuilder(new TypeReference<Set<String>>() {
		}).size("$", 5).set("$", set).sample();

		// then
		then(actual).hasSize(2);
		then(actual).contains("a", "b");
	}

	@Property
	void setEmptySetThenSize() {
		// when
		Set<String> actual = SUT.giveMeBuilder(new TypeReference<Set<String>>() {
			})
			.set("$", new HashSet<>())
			.size("$", 3)
			.sample();

		// then
		then(actual).hasSize(3);
	}

	@Property
	void setDecomposedSetThenSizeZero() {
		// given
		Set<String> set = new HashSet<>();
		set.add("a");
		set.add("b");

		// when
		Set<String> actual = SUT.giveMeBuilder(new TypeReference<Set<String>>() {
		}).set("$", set).size("$", 0).sample();

		// then
		then(actual).hasSize(0);
	}

	@Property
	void setDecomposedArrayThenGreaterSize() {
		// given
		String[] set = {"a", "b"};

		// when
		String[] actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("strArray", set)
			.size("strArray", 5)
			.sample()
			.getStrArray();

		// then
		then(actual).hasSize(5);
		then(actual[0]).isEqualTo("a");
		then(actual[1]).isEqualTo("b");
	}

	@Property
	void setDecomposedArrayThenSmallerSize() {
		// given
		String[] set = {"a", "b", "c", "d", "e"};

		// when
		String[] actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("strArray", set)
			.size("strArray", 3)
			.sample()
			.getStrArray();

		// then
		then(actual).hasSize(3);
		then(actual[0]).isEqualTo("a");
		then(actual[1]).isEqualTo("b");
		then(actual[2]).isEqualTo("c");
	}

	@Property
	void sizeThenSetDecomposedArray() {
		// given
		String[] set = {"a", "b"};

		// when
		String[] actual = SUT.giveMeBuilder(ComplexObject.class)
			.size("strArray", 5)
			.set("strArray", set)
			.sample()
			.getStrArray();

		// then
		then(actual).hasSize(2);
		then(actual[0]).isEqualTo("a");
		then(actual[1]).isEqualTo("b");
	}

	@Property
	void setEmptyArrayThenSize() {
		// when
		String[] actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("strArray", new String[0])
			.size("strArray", 3)
			.sample()
			.getStrArray();

		// then
		then(actual).hasSize(3);
	}

	@Property
	void setDecomposedArrayThenSizeZero() {
		// given
		String[] set = {"a", "b"};

		// when
		String[] actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("strArray", set)
			.size("strArray", 0)
			.sample()
			.getStrArray();

		// then
		then(actual).hasSize(0);
	}

	@Property
	void setDecomposedIntArrayThenGreaterSize() {
		// given
		int[] set = {1, 2, 3};

		// when
		int[] actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("intArray", set)
			.size("intArray", 5)
			.sample()
			.getIntArray();

		// then
		then(actual).hasSize(5);
		then(actual[0]).isEqualTo(1);
		then(actual[1]).isEqualTo(2);
		then(actual[2]).isEqualTo(3);
	}

	@Property
	void setDecomposedIntArrayThenSmallerSize() {
		// given
		int[] set = {1, 2, 3, 4, 5};

		// when
		int[] actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("intArray", set)
			.size("intArray", 2)
			.sample()
			.getIntArray();

		// then
		then(actual).hasSize(2);
		then(actual[0]).isEqualTo(1);
		then(actual[1]).isEqualTo(2);
	}

	@Property
	void setNullListThenSize() {
		// when
		List<String> actual = SUT.giveMeBuilder(ComplexObject.class)
			.setNull("strList")
			.size("strList", 3)
			.sample()
			.getStrList();

		// then
		then(actual).hasSize(3);
	}

	@Property
	void setNullMapThenSize() {
		// when
		Map<String, SimpleObject> actual = SUT.giveMeBuilder(ComplexObject.class)
			.setNull("map")
			.size("map", 3)
			.sample()
			.getMap();

		// then
		then(actual).hasSize(3);
	}

	@Property
	void setDecomposedListThenSizeZero() {
		// given
		List<String> set = new ArrayList<>();
		set.add("a");
		set.add("b");

		// when
		List<String> actual = SUT.giveMeBuilder(new TypeReference<List<String>>() {
			})
			.set("$", set)
			.size("$", 0)
			.sample();

		// then
		then(actual).hasSize(0);
	}

	@Property
	void sizeZeroThenSetDecomposedList() {
		// given
		List<String> set = new ArrayList<>();
		set.add("a");
		set.add("b");

		// when
		List<String> actual = SUT.giveMeBuilder(new TypeReference<List<String>>() {
			})
			.size("$", 0)
			.set("$", set)
			.sample();

		// then
		then(actual).hasSize(2);
		then(actual.get(0)).isEqualTo("a");
		then(actual.get(1)).isEqualTo("b");
	}
}
