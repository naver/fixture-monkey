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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import net.jqwik.api.domains.AbstractDomainContextBase;

import lombok.Data;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;

class FixtureMonkeyTestSpecs extends AbstractDomainContextBase {
	public static final FixtureMonkey SUT = FixtureMonkey.create();

	FixtureMonkeyTestSpecs() {
		registerArbitrary(Integer.class, integer());
		registerArbitrary(IntWithAnnotation.class, integerWithAnnotation());
		registerArbitrary(IntegerArray.class, integerArray());
		registerArbitrary(IntArray.class, intArray());
		registerArbitrary(MapKeyIntegerValueInteger.class, mapKeyIntegerValueInteger());
		registerArbitrary(MapKeyIntegerValueString.class, mapKeyIntegerValueString());
		registerArbitrary(MapEntryKeyIntegerValueString.class, mapEntryKeyIntegerValueString());
		registerArbitrary(IntegerSet.class, integerSet());
		registerArbitrary(IntegerIterable.class, integerIterable());
		registerArbitrary(IntegerIterator.class, integerIterator());
		registerArbitrary(IntegerStream.class, integerStream());
		registerArbitrary(IntegerOptional.class, integerOptional());
		registerArbitrary(StringWithNotBlank.class, stringWithNotBlank());
		registerArbitrary(IntegerListWithNotEmpty.class, integerListWithNotEmpty());
		registerArbitrary(InterfaceWrapper.class, interfaceWrapper());
		registerArbitrary(StringWithNullable.class, stringWithNullable());
		registerArbitrary(StringAndInt.class, stringAndInt());
		registerArbitrary(NestedStringWithNotBlankList.class, nestedStringWithNotBlankList());
		registerArbitrary(ListWithAnnotation.class, listWithAnnotation());
		registerArbitrary(StringQueue.class, stringQueue());
		registerArbitrary(NestedStringQueue.class, nestedStringQueue());
		registerArbitrary(IntegerListAnnotatedBySizeWithoutMax.class, integerListAnnotatedBySizeWithoutMax());
	}

	@Provide
	Arbitrary<Integer> integer() {
		return SUT.giveMeArbitrary(Integer.class);
	}

	@Data
	public static class IntWithAnnotation {
		@Positive
		private int value;
	}

	@Provide
	Arbitrary<IntWithAnnotation> integerWithAnnotation() {
		return SUT.giveMeArbitrary(IntWithAnnotation.class);
	}

	@Data
	public static class IntegerArray {
		private Integer[] values;
	}

	@Provide
	Arbitrary<IntegerArray> integerArray() {
		return SUT.giveMeArbitrary(IntegerArray.class);
	}

	@Data
	public static class IntArray {
		private int[] values;
	}

	@Provide
	Arbitrary<IntArray> intArray() {
		return SUT.giveMeArbitrary(IntArray.class);
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
	public static class MapKeyIntegerValueString {
		private Map<Integer, String> values;
	}

	@Provide
	Arbitrary<MapKeyIntegerValueString> mapKeyIntegerValueString() {
		return SUT.giveMeArbitrary(MapKeyIntegerValueString.class);
	}

	@Data
	public static class MapEntryKeyIntegerValueString {
		private Map.Entry<Integer, String> value;
	}

	@Provide
	Arbitrary<MapEntryKeyIntegerValueString> mapEntryKeyIntegerValueString() {
		return SUT.giveMeArbitrary(MapEntryKeyIntegerValueString.class);
	}

	@Data
	public static class IntegerListWithNotEmpty {
		@NotEmpty
		private List<Integer> values;
	}

	@Provide
	Arbitrary<IntegerListWithNotEmpty> integerListWithNotEmpty() {
		return SUT.giveMeArbitrary(IntegerListWithNotEmpty.class);
	}

	@Data
	public static class IntegerSet {
		private Set<Integer> values;
	}

	@Provide
	Arbitrary<IntegerSet> integerSet() {
		return SUT.giveMeArbitrary(IntegerSet.class);
	}

	@Data
	public static class IntegerIterable {
		private Iterable<Integer> values;
	}

	@Provide
	Arbitrary<IntegerIterable> integerIterable() {
		return SUT.giveMeArbitrary(IntegerIterable.class);
	}

	@Data
	public static class IntegerIterator {
		private Iterator<Integer> values;
	}

	@Provide
	Arbitrary<IntegerIterator> integerIterator() {
		return SUT.giveMeArbitrary(IntegerIterator.class);
	}

	@Data
	public static class IntegerStream {
		private Stream<Integer> values;
	}

	@Provide
	Arbitrary<IntegerStream> integerStream() {
		return SUT.giveMeArbitrary(IntegerStream.class);
	}

	@Data
	public static class IntegerOptional {
		@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
		private Optional<Integer> value;
	}

	@Provide
	Arbitrary<IntegerOptional> integerOptional() {
		return SUT.giveMeArbitrary(IntegerOptional.class);
	}

	@Data
	public static class StringWithNotBlank {
		@NotBlank
		private String value;
	}

	@Provide
	Arbitrary<StringWithNotBlank> stringWithNotBlank() {
		return SUT.giveMeArbitrary(StringWithNotBlank.class);
	}

	@Data
	public static class StringWithNullable {
		@Nullable
		private String value;
	}

	@Provide
	Arbitrary<StringWithNullable> stringWithNullable() {
		return SUT.giveMeArbitrary(StringWithNullable.class);
	}

	@Data
	public static class InterfaceWrapper {
		@NotNull
		private MockInterface value;
	}

	public interface MockInterface {
		String get();
	}

	@Provide
	Arbitrary<InterfaceWrapper> interfaceWrapper() {
		return SUT.giveMeArbitrary(InterfaceWrapper.class);
	}

	@Data
	public static class StringAndInt {
		private StringWithNotBlank value1;
		private IntWithAnnotation value2;
	}

	@Provide
	Arbitrary<StringAndInt> stringAndInt() {
		return SUT.giveMeArbitrary(StringAndInt.class);
	}

	@Data
	public static class NestedStringWithNotBlankList {
		private List<StringWithNotBlank> values;
	}

	@Provide
	Arbitrary<NestedStringWithNotBlankList> nestedStringWithNotBlankList() {
		return SUT.giveMeArbitrary(NestedStringWithNotBlankList.class);
	}

	@Data
	public static class ListWithAnnotation {
		@NotEmpty
		private List<@NotBlank String> values;
	}

	@Provide
	Arbitrary<ListWithAnnotation> listWithAnnotation() {
		return SUT.giveMeArbitrary(ListWithAnnotation.class);
	}

	@Data
	public static class StringQueue {
		private Queue<String> values;
	}

	@Provide
	Arbitrary<StringQueue> stringQueue() {
		return SUT.giveMeArbitrary(StringQueue.class);
	}

	@Data
	public static class NestedStringQueue {
		private Queue<StringQueue> values;
	}

	@Provide
	Arbitrary<NestedStringQueue> nestedStringQueue() {
		return SUT.giveMeArbitrary(NestedStringQueue.class);
	}

	@Data
	public static class IntegerListAnnotatedBySizeWithoutMax {
		@Size(min = 1)
		private List<Integer> values;
	}

	@Provide
	Arbitrary<IntegerListAnnotatedBySizeWithoutMax> integerListAnnotatedBySizeWithoutMax() {
		return SUT.giveMeArbitrary(IntegerListAnnotatedBySizeWithoutMax.class);
	}

	public static class DefaultArbitraryGroup {
		public DefaultArbitraryGroup() {
		}

		public ArbitraryBuilder<StringWithNotBlank> string(FixtureMonkey fixture) {
			return fixture.giveMeBuilder(StringWithNotBlank.class)
				.set("value", "definition");
		}
	}

	public static class DefaultArbitraryGroup2 {
		public DefaultArbitraryGroup2() {
		}

		public ArbitraryBuilder<StringWithNotBlank> string(FixtureMonkey fixture) {
			return fixture.giveMeBuilder(StringWithNotBlank.class)
				.set("value", "definition");
		}
	}

	public static class DuplicateArbitraryGroup {
		public DuplicateArbitraryGroup() {
		}

		public ArbitraryBuilder<StringWithNotBlank> string(FixtureMonkey fixture) {
			return fixture.giveMeBuilder(StringWithNotBlank.class)
				.set("value", "definition");
		}

		public ArbitraryBuilder<StringWithNotBlank> string2(FixtureMonkey fixture) {
			return fixture.giveMeBuilder(StringWithNotBlank.class)
				.set("value", "error");
		}
	}
}
