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

import java.util.List;
import java.util.Map;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import net.jqwik.api.domains.DomainContextBase;

import lombok.Data;

import com.navercorp.fixturemonkey.FixtureMonkey;

class SimpleManipulatorTestSpecs extends DomainContextBase {
	public static final FixtureMonkey SUT = FixtureMonkey.create();

	@Data
	public static class IntValue {
		private int value;
	}

	@Provide
	Arbitrary<IntValue> intValue() {
		return SUT.giveMeArbitrary(IntValue.class);
	}

	@Data
	public static class IntegerList {
		private List<Integer> values;
	}

	@Provide
	Arbitrary<IntegerList> integerList() {
		return SUT.giveMeArbitrary(IntegerList.class);
	}

	@Data
	public static class StringValue {
		private String value;
	}

	@Provide
	Arbitrary<StringValue> stringValue() {
		return SUT.giveMeArbitrary(StringValue.class);
	}

	@Data
	public static class StringList {
		private List<String> values;
	}

	@Provide
	Arbitrary<StringList> stringList() {
		return SUT.giveMeArbitrary(StringList.class);
	}

	@Data
	public static class TwoString {
		private String value1;
		private String value2;
	}

	@Provide
	Arbitrary<TwoString> twoString() {
		return SUT.giveMeArbitrary(TwoString.class);
	}

	@Data
	public static class MapKeyIntegerValueInteger {
		private Map<Integer, Integer> values;
	}

	@Provide
	Arbitrary<MapKeyIntegerValueInteger> mapKeyIntegerValueInteger() {
		return SUT.giveMeArbitrary(MapKeyIntegerValueInteger.class);
	}

	@Data
	public static class NestedStringValueList {
		private List<StringValue> values;
	}

	@Provide
	Arbitrary<NestedStringValueList> nestedStringValueList() {
		return SUT.giveMeArbitrary(NestedStringValueList.class);
	}

	@Data
	public static class ListListString {
		private List<List<String>> values;
	}

	@Provide
	Arbitrary<ListListString> listListString() {
		return SUT.giveMeArbitrary(ListListString.class);
	}

	@Data
	public static class StringAndInt {
		private StringValue value1;
		private IntValue value2;
	}

	@Provide
	Arbitrary<StringAndInt> stringAndInt() {
		return SUT.giveMeArbitrary(StringAndInt.class);
	}
}
