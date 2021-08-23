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
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.generator.BuilderArbitraryGenerator;

class FixtureMonkeyTest {
	private final FixtureMonkey sut = FixtureMonkey.builder()
		.addInterfaceSupplier(MockInterface.class, (type) -> () -> "test")
		.build();

	@Property
	void giveMeRegisteredReference() {
		Integer actual = this.sut.giveMeOne(Integer.class);

		then(actual).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeRegisteredPrimitive() {
		ArbitraryBuilder<Integer> builder = this.sut.giveMeBuilder(Integer.class);

		int actual = builder.sample();

		then(actual).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeWithAnnotation() {
		IntegerWrapperClassWithAnnotation actual = this.sut.giveMeOne(IntegerWrapperClassWithAnnotation.class);

		then(actual.getValue()).isPositive();
	}

	@Provide
	Arbitrary<IntegerArrayClass> arrayToBuilder() {
		IntegerArrayClass expected = new IntegerArrayClass(new Integer[] {1, 2, 3});

		return this.sut.giveMeBuilder(expected).build();
	}

	@Property
	void giveMeArrayToBuilder(@ForAll("arrayToBuilder") IntegerArrayClass actual) {
		then(actual.value[0]).isEqualTo(1);
		then(actual.value[1]).isEqualTo(2);
		then(actual.value[2]).isEqualTo(3);
	}

	@Provide
	Arbitrary<IntArrayClass> primitiveArrayToBuilder() {
		IntArrayClass expected = new IntArrayClass(new int[] {1, 2, 3});

		return this.sut.giveMeBuilder(expected).build();
	}

	@Property
	void giveMePrimitiveArrayToBuilder(@ForAll("primitiveArrayToBuilder") IntArrayClass actual) {
		then(actual.value[0]).isEqualTo(1);
		then(actual.value[1]).isEqualTo(2);
		then(actual.value[2]).isEqualTo(3);
	}

	@Provide
	Arbitrary<MapKeyIntegerValueIntegerClass> sameKeyValueMapToBuilder() {
		Map<Integer, Integer> values = new HashMap<>();
		values.put(1, 1);
		MapKeyIntegerValueIntegerClass expected = new MapKeyIntegerValueIntegerClass(values);

		return this.sut.giveMeBuilder(expected).build();
	}

	@Property
	void giveMeSameKeyValueMapToBuilder(
		@ForAll("sameKeyValueMapToBuilder") MapKeyIntegerValueIntegerClass actual
	) {
		then(actual.values.get(1)).isEqualTo(1);
	}

	@Provide
	Arbitrary<MapKeyIntegerValueStringClass> diffKeyValueMapToBuilder() {
		Map<Integer, String> values = new HashMap<>();
		values.put(1, "1");
		MapKeyIntegerValueStringClass expected = new MapKeyIntegerValueStringClass(values);

		return this.sut.giveMeBuilder(expected).build();
	}

	@Property
	void giveMeDiffKeyValueMapToBuilder(@ForAll("diffKeyValueMapToBuilder") MapKeyIntegerValueStringClass actual) {
		then(actual.values.get(1)).isEqualTo("1");
	}

	@Provide
	Arbitrary<MapEntryKeyIntegerValueStringClass> mapEntryToBuilder() {
		Map.Entry<Integer, String> value = new SimpleEntry<>(1, "1");
		MapEntryKeyIntegerValueStringClass expected = new MapEntryKeyIntegerValueStringClass(value);

		return this.sut.giveMeBuilder(expected).build();
	}

	@Property
	void giveMeMapEntryToBuilder(@ForAll("mapEntryToBuilder") MapEntryKeyIntegerValueStringClass actual) {
		then(actual.value.getValue()).isEqualTo("1");
	}

	@Provide
	Arbitrary<IntegerSetClass> setToBuilder() {
		Set<Integer> values = new HashSet<>();
		values.add(1);
		IntegerSetClass expected = new IntegerSetClass(values);

		return this.sut.giveMeBuilder(expected).build();
	}

	@Property
	void giveMeSetToBuilder(@ForAll("setToBuilder") IntegerSetClass actual) {
		then(actual.values).allMatch(it -> it == 1);
	}

	@Provide
	Arbitrary<IntegerIterableClass> iterableToBuilder() {
		List<Integer> values = new ArrayList<>();
		values.add(1);
		IntegerIterableClass expected = new IntegerIterableClass(values);

		return sut.giveMeBuilder(expected)
			.build();
	}

	@Property
	void giveMeIterableToBuilder(@ForAll("iterableToBuilder") IntegerIterableClass actual) {
		then(actual.values).allMatch(it -> it == 1);
	}

	@Provide
	Arbitrary<IntegerIteratorClass> iteratorToBuilder() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BuilderArbitraryGenerator.INSTANCE)
			.build();
		List<Integer> values = new ArrayList<>();
		values.add(1);
		IntegerIteratorClass expected = new IntegerIteratorClass(values.iterator());

		return sut.giveMeBuilder(expected).build();
	}

	@Property
	void giveMeIteratorToBuilder(@ForAll("iteratorToBuilder") IntegerIteratorClass actual) {
		then(actual.values.next()).isEqualTo(1);
	}

	@Provide
	Arbitrary<IntegerStreamClass> streamToBuilder() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BuilderArbitraryGenerator.INSTANCE)
			.build();
		List<Integer> values = new ArrayList<>();
		values.add(1);
		IntegerStreamClass expected = new IntegerStreamClass(values.stream());

		return sut.giveMeBuilder(expected).build();
	}

	@Property
	void giveMeStreamToBuilder(@ForAll("streamToBuilder") IntegerStreamClass actual) {
		then(actual.values).allMatch(it -> it == 1);
	}

	@Provide
	Arbitrary<IntegerOptionalClass> optionalToBuilder() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BuilderArbitraryGenerator.INSTANCE)
			.build();

		return sut.giveMeBuilder(new IntegerOptionalClass(Optional.of(1))).build();
	}

	@Property
	void giveMeOptionalToBuilder(@ForAll("optionalToBuilder") IntegerOptionalClass actual) {
		//noinspection OptionalGetWithoutIsPresent
		then(actual.value.get()).isEqualTo(1);
	}

	@Provide
	Arbitrary<IntegerOptionalClass> optionalEmptyToBuilder() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BuilderArbitraryGenerator.INSTANCE)
			.build();
		Optional<Integer> value = Optional.empty();
		IntegerOptionalClass expected = new IntegerOptionalClass(value);

		return sut.giveMeBuilder(expected).build();
	}

	@Property
	void giveMeOptionalEmptyToBuilder(@ForAll("optionalEmptyToBuilder") IntegerOptionalClass actual) {
		then(actual.value).isEqualTo(Optional.empty());
	}

	@Provide
	Arbitrary<IntegerArrayClass> copyArbitrary() {
		IntegerArrayClass expected = new IntegerArrayClass(new Integer[] {1, 2, 3});

		ArbitraryBuilder<IntegerArrayClass> builder = this.sut.giveMeBuilder(expected);
		ArbitraryBuilder<IntegerArrayClass> copiedBuilder = builder.copy()
			.set("value[1]", 3);

		return copiedBuilder.build();
	}

	@Property
	void copy(@ForAll("copyArbitrary") IntegerArrayClass actual) {
		then(actual.value[0]).isEqualTo(1);
		then(actual.value[1]).isNotEqualTo(2);
		then(actual.value[1]).isEqualTo(3);
		then(actual.value[2]).isEqualTo(3);
	}

	@Provide
	Arbitrary<IntegerArrayClass> copyArbitraryWithManipulator() {
		IntegerArrayClass expected = new IntegerArrayClass(new Integer[] {1, 2, 3});
		ArbitraryBuilder<IntegerArrayClass> builder = this.sut.giveMeBuilder(expected)
			.set("value[0]", -1);

		return builder.copy().build();
	}

	@Property
	void copyWithManipulator(@ForAll("copyArbitraryWithManipulator") IntegerArrayClass actual) {
		then(actual.value[0]).isEqualTo(-1);
		then(actual.value[1]).isEqualTo(2);
		then(actual.value[2]).isEqualTo(3);
	}

	@Property
	void giveMeList() {
		List<IntegerWrapperClass> actual = this.sut.giveMe(IntegerWrapperClass.class, 5);

		then(actual).hasSize(5);
		then(actual).allMatch(Objects::nonNull);
	}

	@Property
	void giveMeOptional() {
		OptionalClass actual = this.sut.giveMeOne(OptionalClass.class);

		then(actual).isNotNull();
	}

	@Property
	void setAfterBuildNotAffected() {
		ArbitraryBuilder<StringWrapperClass> builder = this.sut.giveMeBuilder(StringWrapperClass.class);
		Arbitrary<StringWrapperClass> build = builder.build();
		ArbitraryBuilder<StringWrapperClass> actual = builder.set("value", "set");

		StringWrapperClass actualSample = actual.sample();
		StringWrapperClass buildSample = build.sample();
		then(actualSample).isNotEqualTo(buildSample);
		then(actualSample.value).isEqualTo("set");
	}

	@Provide
	Arbitrary<IntegerListClassNotEmpty> notEmpty() {
		return this.sut.giveMeBuilder(IntegerListClassNotEmpty.class)
			.build();
	}

	@Property
	void giveMeNotEmpty(@ForAll("notEmpty") IntegerListClassNotEmpty actual) {
		then(actual.values).isNotEmpty();
	}

	@Property
	void addExceptGenerate() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.addExceptGeneratePackage("com.navercorp.fixturemonkey.test")
			.build();

		ExceptGenerateClass actual = sut.giveMeOne(ExceptGenerateClass.class);

		then(actual).isNull();
	}

	@Provide
	Arbitrary<IntegerListClass> sizeListSmallerThanValueWhenDecomposed() {
		List<Integer> values = new ArrayList<>();
		values.add(1);
		values.add(2);
		values.add(3);
		IntegerListClass value = new IntegerListClass();
		value.setValues(values);
		return this.sut.giveMeBuilder(value)
			.size("values", 1)
			.build();
	}

	@Property
	void giveMeSizeListSmallerThanValueWhenDecomposed(
		@ForAll("sizeListSmallerThanValueWhenDecomposed") IntegerListClass actual
	) {
		then(actual.values).hasSize(1);
		then(actual.values.get(0)).isEqualTo(1);
	}

	@Provide
	Arbitrary<IntegerListClass> sizeListBiggerThanValueWhenDecomposed() {
		List<Integer> values = new ArrayList<>();
		values.add(1);
		values.add(2);
		values.add(3);
		IntegerListClass value = new IntegerListClass();
		value.setValues(values);
		return this.sut.giveMeBuilder(value)
			.size("values", 5)
			.build();
	}

	@Property
	void giveMeSizeListBiggerThanValueWhenDecomposed(
		@ForAll("sizeListBiggerThanValueWhenDecomposed") IntegerListClass actual
	) {
		then(actual.values).hasSize(5);
		then(actual.values.get(0)).isEqualTo(1);
		then(actual.values.get(1)).isEqualTo(2);
		then(actual.values.get(2)).isEqualTo(3);
	}

	@Provide
	Arbitrary<IntegerArrayClass> sizeArraySmallerThanValueWhenDecomposed() {
		Integer[] values = new Integer[] {1, 2, 3};
		IntegerArrayClass value = new IntegerArrayClass();
		value.setValue(values);
		return this.sut.giveMeBuilder(value)
			.size("value", 1)
			.build();
	}

	@Property
	void giveMeSizeArraySmallerThanValueWhenDecomposed(
		@ForAll("sizeArraySmallerThanValueWhenDecomposed") IntegerArrayClass actual
	) {
		then(actual.value).hasSize(1);
		then(actual.value[0]).isEqualTo(1);
	}

	@Provide
	Arbitrary<IntegerArrayClass> sizeArrayBiggerThanValueWhenDecomposed() {
		Integer[] values = new Integer[] {1, 2, 3};
		IntegerArrayClass value = new IntegerArrayClass();
		value.setValue(values);
		return this.sut.giveMeBuilder(value)
			.size("value", 5)
			.build();
	}

	@Property
	void giveMeSizeArrayBiggerThanValueWhenDecomposed(
		@ForAll("sizeArrayBiggerThanValueWhenDecomposed") IntegerArrayClass actual
	) {
		then(actual.value).hasSize(5);
		then(actual.value[0]).isEqualTo(1);
		then(actual.value[1]).isEqualTo(2);
		then(actual.value[2]).isEqualTo(3);
	}

	@Property
	void giveMeInterface() {
		InterfaceWrapper actual = this.sut.giveMeBuilder(InterfaceWrapper.class)
			.setNotNull("value")
			.sample();

		then(actual.value.get()).isEqualTo("test");
	}

	@Property
	void giveMeInterfaceWithDefaultInterfaceSupplier() {
		FixtureMonkey sut = FixtureMonkey.builder().build();

		InterfaceWrapper actual = sut.giveMeOne(InterfaceWrapper.class);

		then(actual.value).isNull();
	}

	@Property
	void defaultNullInject() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.nullInject(1.0d)
			.build();

		StringWrapperClass actual = sut.giveMeOne(StringWrapperClass.class);

		then(actual.value).isNull();
	}

	@Property
	void defaultNullInjectWithDefaultNotNull() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.nullInject(1.0d)
			.defaultNotNull(true)
			.build();

		StringWrapperClass actual = sut.giveMeOne(StringWrapperClass.class);

		then(actual.value).isNotNull();
	}

	@Property
	void giveMeNullableDefaultNullInjectWithDefaultNotNull() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.nullInject(1.0d)
			.defaultNotNull(true)
			.build();

		StringWrapperWithNullableClass actual = sut.giveMeOne(StringWrapperWithNullableClass.class);

		then(actual.value).isNull();
	}

	@Property
	void giveMeNotBlankString() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.nullInject(1.0)
			.build();

		StringWrapperWithNotBlankClass actual = sut.giveMeOne(StringWrapperWithNotBlankClass.class);

		then(actual.value).isNotNull();
	}

	@Property
	void mapIntegerListClassBiggerThanMapped() {
		IntegerListClass mapped = sut.giveMeBuilder(IntegerListClass.class)
			.size("values", 1)
			.sample();

		IntegerListClass actual = sut.giveMeBuilder(mapped)
			.size("values", 2)
			.sample();

		then(actual.values).hasSize(2);
		then(actual.values.get(1)).isNotNull();
	}

	@Property
	void decomposeNullIsNotGenerated() {
		StringIntegerClass decomposed = new StringIntegerClass();
		IntegerWrapperClass value2 = new IntegerWrapperClass();
		value2.value = 1;
		decomposed.setValue2(value2);

		StringIntegerClass actual = this.sut.giveMeBuilder(decomposed)
			.sample();

		then(actual.value1).isNull();
		then(actual.value2.value).isEqualTo(1);
	}

	@Property
	void decomposeNullSetThenGenerate() {
		StringIntegerClass decomposed = new StringIntegerClass();
		IntegerWrapperClass value2 = new IntegerWrapperClass();
		value2.value = 1;
		decomposed.setValue2(value2);

		StringIntegerClass actual = this.sut.giveMeBuilder(decomposed)
			.set("value1.value", "abc")
			.sample();

		then(actual.value1.value).isEqualTo("abc");
		then(actual.value2.value).isEqualTo(1);
	}

	@Property
	void registerGroup() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(DefaultArbitraryHolder.class)
			.build();

		StringWrapperClass actual = sut.giveMeOne(StringWrapperClass.class);

		then(actual.value).isEqualTo("definition");
	}

	@Property
	void registerGroupInField() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(DefaultArbitraryHolder.class)
			.build();

		StringIntegerClass actual = sut.giveMeOne(StringIntegerClass.class);

		then(actual.value1.value).isEqualTo("definition");
	}

	@Property
	void registerGroupInFieldSet() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(DefaultArbitraryHolder.class)
			.build();

		StringIntegerClass actual = sut.giveMeBuilder(StringIntegerClass.class)
			.set("value1.value", "set")
			.sample();

		then(actual.value1.value).isEqualTo("set");
	}

	@Property
	void registerGroupList() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(DefaultArbitraryHolder.class)
			.build();

		NestedStringList actual = sut.giveMeBuilder(NestedStringList.class)
			.sample();

		then(actual.values).allMatch(it -> it.value.equals("definition"));
	}

	@Property
	void register() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(
				StringWrapperClass.class,
				it -> it.giveMeBuilder(StringWrapperClass.class).set("value", "definition")
			)
			.build();

		StringWrapperClass actual = sut.giveMeOne(StringWrapperClass.class);

		then(actual.value).isEqualTo("definition");
	}

	@Property
	void registerInField() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(
				StringWrapperClass.class,
				it -> it.giveMeBuilder(StringWrapperClass.class).set("value", "definition")
			)
			.build();

		StringIntegerClass actual = sut.giveMeOne(StringIntegerClass.class);

		then(actual.value1.value).isEqualTo("definition");
	}

	@Property
	void registerInFieldSet() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(
				StringWrapperClass.class,
				it -> it.giveMeBuilder(StringWrapperClass.class).set("value", "definition")
			)
			.build();

		StringIntegerClass actual = sut.giveMeBuilder(StringIntegerClass.class)
			.set("value1.value", "set")
			.sample();

		then(actual.value1.value).isEqualTo("set");
	}

	@Property
	void registerList() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(
				StringWrapperClass.class,
				it -> it.giveMeBuilder(StringWrapperClass.class).set("value", "definition")
			)
			.build();

		NestedStringList actual = sut.giveMeBuilder(NestedStringList.class)
			.sample();

		then(actual.values).allMatch(it -> it.value.equals("definition"));
	}

	@Property
	void registerSameTypeThrows() {
		thenThrownBy(() ->
			FixtureMonkey.builder()
				.register(
					StringWrapperClass.class,
					it -> it.giveMeBuilder(StringWrapperClass.class).set("value", "definition")
				)
				.register(
					StringWrapperClass.class,
					it -> it.giveMeBuilder(StringWrapperClass.class).set("value", "error")
				)
				.build())
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("can not register same classes twice.");
	}

	@Property
	void registerGroupSameTypeThrows() {
		thenThrownBy(() ->
			FixtureMonkey.builder()
				.registerGroup(DuplicateArbitraryHolder.class)
				.build())
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("can not register same classes twice.");
	}

	@Property
	void registerDiffGroupSameTypeThrows() {
		thenThrownBy(() ->
			FixtureMonkey.builder()
				.registerGroup(DefaultArbitraryHolder.class)
				.registerGroup(DefaultArbitraryHolder2.class)
				.build())
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("can not register same classes twice.");
	}

	@Property
	void exceptGenerateClass() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.addExceptGenerateClass(String.class)
			.build();

		// when
		StringWrapperClass actual = sut.giveMeOne(StringWrapperClass.class);

		then(actual.value).isNull();
	}

	@Data
	public static class IntegerWrapperClass {
		int value;
	}

	@Data
	public static class IntegerWrapperClassWithAnnotation {
		@Positive
		int value;
	}

	@Data
	public static class IntegerListClass {
		List<Integer> values;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class StringWrapperClass {
		private String value;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class IntegerArrayClass {
		private Integer[] value;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class IntArrayClass {
		private int[] value;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MapKeyIntegerValueIntegerClass {
		private Map<Integer, Integer> values;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MapKeyIntegerValueStringClass {
		private Map<Integer, String> values;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MapEntryKeyIntegerValueStringClass {
		private Map.Entry<Integer, String> value;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class IntegerSetClass {
		private Set<Integer> values;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class IntegerIterableClass {
		private Iterable<Integer> values;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class IntegerIteratorClass {
		private Iterator<Integer> values;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class IntegerStreamClass {
		private Stream<Integer> values;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class IntegerOptionalClass {
		@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
		private Optional<Integer> value;
	}

	@Data
	public static class NestedStringList {
		private List<StringWrapperClass> values;
	}

	@Data
	public static class OptionalClass {
		@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
		private Optional<Integer> value;
	}

	@Data
	public static class IntegerListClassNotEmpty {
		@NotEmpty
		private List<Integer> values;
	}

	@Data
	public static class ExceptGenerateClass {
		String value;
	}

	@Data
	public static class StringIntegerClass {
		StringWrapperClass value1;
		IntegerWrapperClass value2;
	}

	public interface MockInterface {
		String get();
	}

	@Data
	public static class InterfaceWrapper {
		MockInterface value;
	}

	@Data
	public static class StringWrapperWithNullableClass {
		@Nullable
		String value;
	}

	@Data
	public static class StringWrapperWithNotBlankClass {
		@NotBlank
		String value;
	}

	public static class DefaultArbitraryHolder {
		public DefaultArbitraryHolder() {
		}

		public ArbitraryBuilder<StringWrapperClass> string(FixtureMonkey fixture) {
			return fixture.giveMeBuilder(StringWrapperClass.class)
				.set("value", "definition");
		}
	}

	public static class DefaultArbitraryHolder2 {
		public DefaultArbitraryHolder2() {
		}

		public ArbitraryBuilder<StringWrapperClass> string(FixtureMonkey fixture) {
			return fixture.giveMeBuilder(StringWrapperClass.class)
				.set("value", "definition");
		}
	}

	public static class DuplicateArbitraryHolder {
		public DuplicateArbitraryHolder() {
		}

		public ArbitraryBuilder<StringWrapperClass> string(FixtureMonkey fixture) {
			return fixture.giveMeBuilder(StringWrapperClass.class)
				.set("value", "definition");
		}

		public ArbitraryBuilder<StringWrapperClass> string2(FixtureMonkey fixture) {
			return fixture.giveMeBuilder(StringWrapperClass.class)
				.set("value", "error");
		}
	}

	@Data
	public static class NestedStringWrapper {
		StringWrapperClass value;
	}
}
