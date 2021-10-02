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

import static java.util.stream.Collectors.toList;
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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import org.junit.platform.commons.util.StringUtils;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Example;
import net.jqwik.api.Property;
import net.jqwik.api.TooManyFilterMissesException;

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
		// given
		ArbitraryBuilder<Integer> builder = this.sut.giveMeBuilder(Integer.class);

		// when
		int actual = builder.sample();

		then(actual).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeWithAnnotation() {
		IntegerWithAnnotationWrapperClass actual = this.sut.giveMeOne(IntegerWithAnnotationWrapperClass.class);

		then(actual.getValue()).isPositive();
	}

	@Property
	void giveMeArrayToBuilder() {
		// given
		IntegerArrayWrapperClass expected = new IntegerArrayWrapperClass(new Integer[] {1, 2, 3});

		// when
		IntegerArrayWrapperClass actual = this.sut.giveMeBuilder(expected).sample();

		then(actual.values[0]).isEqualTo(1);
		then(actual.values[1]).isEqualTo(2);
		then(actual.values[2]).isEqualTo(3);
	}

	@Property
	void giveMePrimitiveArrayToBuilder() {
		// given
		IntArrayWrapperClass expected = new IntArrayWrapperClass(new int[] {1, 2, 3});

		// when
		IntArrayWrapperClass actual = this.sut.giveMeBuilder(expected).sample();

		then(actual.values[0]).isEqualTo(1);
		then(actual.values[1]).isEqualTo(2);
		then(actual.values[2]).isEqualTo(3);
	}

	@Property
	void giveMeSameKeyValueMapToBuilder() {
		// given
		Map<Integer, Integer> values = new HashMap<>();
		values.put(1, 1);
		MapKeyIntegerValueIntegerWrapperClass expected = new MapKeyIntegerValueIntegerWrapperClass(values);

		// when
		MapKeyIntegerValueIntegerWrapperClass actual = this.sut.giveMeBuilder(expected).sample();

		then(actual.values.get(1)).isEqualTo(1);
	}

	@Property
	void giveMeDiffKeyValueMapToBuilder() {
		// given
		Map<Integer, String> values = new HashMap<>();
		values.put(1, "1");
		MapKeyIntegerValueStringWrapperClass expected = new MapKeyIntegerValueStringWrapperClass(values);

		// when
		MapKeyIntegerValueStringWrapperClass actual = this.sut.giveMeBuilder(expected).sample();

		then(actual.values.get(1)).isEqualTo("1");
	}

	@Property
	void giveMeMapEntryToBuilder() {
		// given
		Map.Entry<Integer, String> value = new SimpleEntry<>(1, "1");
		MapEntryKeyIntegerValueStringWrapperClass expected = new MapEntryKeyIntegerValueStringWrapperClass(value);

		// when
		MapEntryKeyIntegerValueStringWrapperClass actual = this.sut.giveMeBuilder(expected).sample();

		then(actual.value.getValue()).isEqualTo("1");
	}

	@Property
	void giveMeSetToBuilder() {
		// given
		Set<Integer> values = new HashSet<>();
		values.add(1);
		IntegerSetWrapperClass expected = new IntegerSetWrapperClass(values);

		// when
		IntegerSetWrapperClass actual = this.sut.giveMeBuilder(expected).sample();

		then(actual.values).allMatch(it -> it == 1);
	}

	@Property
	void giveMeIterableToBuilder() {
		// given
		List<Integer> values = new ArrayList<>();
		values.add(1);
		IntegerIterableWrapperClass expected = new IntegerIterableWrapperClass(values);

		// when
		IntegerIterableWrapperClass actual = sut.giveMeBuilder(expected).sample();

		then(actual.values).allMatch(it -> it == 1);
	}

	@Property
	void giveMeIteratorToBuilder() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BuilderArbitraryGenerator.INSTANCE)
			.build();
		List<Integer> values = new ArrayList<>();
		values.add(1);
		IntegerIteratorWrapperClass expected = new IntegerIteratorWrapperClass(values.iterator());

		// when
		IntegerIteratorWrapperClass actual = sut.giveMeBuilder(expected).sample();

		then(actual.values.next()).isEqualTo(1);
	}

	@Property
	void giveMeStreamToBuilder() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BuilderArbitraryGenerator.INSTANCE)
			.build();
		List<Integer> values = new ArrayList<>();
		values.add(1);
		IntegerStreamWrapperClass expected = new IntegerStreamWrapperClass(values.stream());

		// when
		IntegerStreamWrapperClass actual = sut.giveMeBuilder(expected).sample();

		then(actual.values).allMatch(it -> it == 1);
	}

	@Property
	void giveMeOptionalToBuilder() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BuilderArbitraryGenerator.INSTANCE)
			.build();

		// when
		IntegerOptionalWrapperClass actual = sut.giveMeBuilder(new IntegerOptionalWrapperClass(Optional.of(1)))
			.sample();

		//noinspection OptionalGetWithoutIsPresent
		then(actual.value.get()).isEqualTo(1);
	}

	@Property
	void giveMeOptionalEmptyToBuilder() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BuilderArbitraryGenerator.INSTANCE)
			.build();
		Optional<Integer> value = Optional.empty();
		IntegerOptionalWrapperClass expected = new IntegerOptionalWrapperClass(value);

		// when
		IntegerOptionalWrapperClass actual = sut.giveMeBuilder(expected).sample();

		then(actual.value).isEqualTo(Optional.empty());
	}

	@Property
	void copy() {
		// given
		IntegerArrayWrapperClass expected = new IntegerArrayWrapperClass(new Integer[] {1, 2, 3});
		ArbitraryBuilder<IntegerArrayWrapperClass> builder = this.sut.giveMeBuilder(expected);
		ArbitraryBuilder<IntegerArrayWrapperClass> copiedBuilder = builder.copy()
			.set("values[1]", 3);

		// when
		IntegerArrayWrapperClass actual = copiedBuilder.sample();

		then(actual.values[0]).isEqualTo(1);
		then(actual.values[1]).isNotEqualTo(2);
		then(actual.values[1]).isEqualTo(3);
		then(actual.values[2]).isEqualTo(3);
	}

	@Property
	void copyWithManipulator() {
		// given
		IntegerArrayWrapperClass expected = new IntegerArrayWrapperClass(new Integer[] {1, 2, 3});
		ArbitraryBuilder<IntegerArrayWrapperClass> builder = this.sut.giveMeBuilder(expected)
			.set("values[0]", -1);

		// when
		IntegerArrayWrapperClass actual = builder.copy().sample();

		then(actual.values[0]).isEqualTo(-1);
		then(actual.values[1]).isEqualTo(2);
		then(actual.values[2]).isEqualTo(3);
	}

	@Property
	void giveMeList() {
		// when
		List<IntegerWrapperClass> actual = this.sut.giveMe(IntegerWrapperClass.class, 5);

		then(actual).hasSize(5);
		then(actual).allMatch(Objects::nonNull);
	}

	@Property
	void giveMeOptional() {
		// when
		OptionalClass actual = this.sut.giveMeOne(OptionalClass.class);

		then(actual).isNotNull();
	}

	@Property
	void giveMeArbitraryThenSample() {
		// given
		Arbitrary<StringWithNotBlankWrapperClass> sut = this.sut.giveMeArbitrary(StringWithNotBlankWrapperClass.class);

		// when
		StringWithNotBlankWrapperClass actual = sut.sample();

		then(actual.getValue()).isNotBlank();
	}

	@Property
	void giveMeArbitraryThenSampleStream() {
		// given
		Arbitrary<StringWithNotBlankWrapperClass> sut = this.sut.giveMeArbitrary(StringWithNotBlankWrapperClass.class);

		// when
		List<StringWithNotBlankWrapperClass> actual = sut.sampleStream().limit(5).collect(toList());

		actual.forEach(it -> then(it.getValue()).isNotBlank());
	}

	@Example
	void giveMeBuilderInvalidThenSampleTooManyFilterMissesException() {
		Arbitrary<StringWithNotBlankWrapperClass> sut = this.sut.giveMeBuilder(StringWithNotBlankWrapperClass.class)
			.set("value", "")
			.build();

		thenThrownBy(sut::sample)
			.isExactlyInstanceOf(TooManyFilterMissesException.class);
	}

	@Example
	void giveMeBuilderInvalidThenSampleStreamTooManyFilterMissesException() {
		Arbitrary<StringWithNotBlankWrapperClass> sut = this.sut.giveMeBuilder(StringWithNotBlankWrapperClass.class)
			.set("value", "")
			.build();

		thenThrownBy(() -> sut.sampleStream().limit(5).collect(toList()))
			.isExactlyInstanceOf(TooManyFilterMissesException.class);
	}

	@Property
	void setAfterBuildNotAffected() {
		// given
		ArbitraryBuilder<StringWrapperClass> builder = this.sut.giveMeBuilder(StringWrapperClass.class);
		Arbitrary<StringWrapperClass> build = builder.build();

		// when
		ArbitraryBuilder<StringWrapperClass> actual = builder.set("value", "set");

		StringWrapperClass actualSample = actual.sample();
		StringWrapperClass buildSample = build.sample();
		then(actualSample).isNotEqualTo(buildSample);
		then(actualSample.value).isEqualTo("set");
	}

	@Property
	void giveMeNotEmpty() {
		// when
		IntegerListWithNotEmptyWrapperClass actual = this.sut.giveMeBuilder(IntegerListWithNotEmptyWrapperClass.class)
			.sample();

		then(actual.values).isNotEmpty();
	}

	@Property
	void addExceptGenerate() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.addExceptGeneratePackage("com.navercorp.fixturemonkey.test")
			.build();

		// when
		ExceptGenerateClass actual = sut.giveMeOne(ExceptGenerateClass.class);

		then(actual).isNull();
	}

	@Property
	void giveMeSizeListSmallerThanValueWhenDecomposed() {
		// given
		List<Integer> values = new ArrayList<>();
		values.add(1);
		values.add(2);
		values.add(3);
		IntegerListWrapperClass value = new IntegerListWrapperClass();
		value.setValues(values);

		// when
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(value)
			.size("values", 1)
			.sample();

		then(actual.values).hasSize(1);
		then(actual.values.get(0)).isEqualTo(1);
	}

	@Property
	void giveMeSizeListBiggerThanValueWhenDecomposed() {
		// given
		List<Integer> values = new ArrayList<>();
		values.add(1);
		values.add(2);
		values.add(3);
		IntegerListWrapperClass value = new IntegerListWrapperClass();
		value.setValues(values);

		// when
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(value)
			.size("values", 5)
			.sample();

		then(actual.values).hasSize(5);
		then(actual.values.get(0)).isEqualTo(1);
		then(actual.values.get(1)).isEqualTo(2);
		then(actual.values.get(2)).isEqualTo(3);
	}

	@Property
	void giveMeSizeArraySmallerThanValueWhenDecomposed() {
		// given
		Integer[] values = new Integer[] {1, 2, 3};
		IntegerArrayWrapperClass value = new IntegerArrayWrapperClass();
		value.setValues(values);

		// when
		IntegerArrayWrapperClass actual = this.sut.giveMeBuilder(value)
			.size("values", 1)
			.sample();

		then(actual.values).hasSize(1);
		then(actual.values[0]).isEqualTo(1);
	}

	@Property
	void giveMeSizeArrayBiggerThanValueWhenDecomposed() {
		// given
		Integer[] values = new Integer[] {1, 2, 3};
		IntegerArrayWrapperClass value = new IntegerArrayWrapperClass();
		value.setValues(values);

		// when
		IntegerArrayWrapperClass actual = this.sut.giveMeBuilder(value)
			.size("values", 5)
			.sample();

		then(actual.values).hasSize(5);
		then(actual.values[0]).isEqualTo(1);
		then(actual.values[1]).isEqualTo(2);
		then(actual.values[2]).isEqualTo(3);
	}

	@Property
	void giveMeInterface() {
		// when
		InterfaceWrapper actual = this.sut.giveMeBuilder(InterfaceWrapper.class)
			.setNotNull("value")
			.sample();

		then(actual.value.get()).isEqualTo("test");
	}

	@Property
	void giveMeInterfaceWithDefaultInterfaceSupplier() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder().build();

		// when
		InterfaceWrapper actual = sut.giveMeOne(InterfaceWrapper.class);

		then(actual.value).isNull();
	}

	@Property
	void defaultNullInject() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.nullInject(1.0d)
			.build();

		// when
		StringWrapperClass actual = sut.giveMeOne(StringWrapperClass.class);

		then(actual.value).isNull();
	}

	@Property
	void defaultNullInjectWithDefaultNotNull() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.nullInject(1.0d)
			.defaultNotNull(true)
			.build();

		// when
		StringWrapperClass actual = sut.giveMeOne(StringWrapperClass.class);

		then(actual.value).isNotNull();
	}

	@Property
	void giveMeNullableDefaultNullInjectWithDefaultNotNull() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.nullInject(1.0d)
			.defaultNotNull(true)
			.build();

		// when
		StringWithNullableWrapperClass actual = sut.giveMeOne(StringWithNullableWrapperClass.class);

		then(actual.value).isNull();
	}

	@Property
	void giveMeNotBlankString() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.nullInject(1.0)
			.build();

		// when
		StringWithNotBlankWrapperClass actual = sut.giveMeOne(StringWithNotBlankWrapperClass.class);

		then(actual.value).isNotNull();
	}

	@Property
	void mapIntegerListClassBiggerThanMapped() {
		// given
		IntegerListWrapperClass mapped = sut.giveMeBuilder(IntegerListWrapperClass.class)
			.size("values", 1)
			.sample();

		// when
		IntegerListWrapperClass actual = sut.giveMeBuilder(mapped)
			.size("values", 2)
			.sample();

		then(actual.values).hasSize(2);
		then(actual.values.get(1)).isNotNull();
	}

	@Property
	void decomposeNullIsNotGenerated() {
		// given
		StringWrapperIntegerWrapperClass decomposed = new StringWrapperIntegerWrapperClass();
		IntegerWrapperClass value2 = new IntegerWrapperClass();
		value2.value = 1;
		decomposed.setValue2(value2);

		// when
		StringWrapperIntegerWrapperClass actual = this.sut.giveMeBuilder(decomposed)
			.sample();

		then(actual.value1).isNull();
		then(actual.value2.value).isEqualTo(1);
	}

	@Property
	void decomposeNullSetThenGenerate() {
		// given
		StringWrapperIntegerWrapperClass decomposed = new StringWrapperIntegerWrapperClass();
		IntegerWrapperClass value2 = new IntegerWrapperClass();
		value2.value = 1;
		decomposed.setValue2(value2);

		// when
		StringWrapperIntegerWrapperClass actual = this.sut.giveMeBuilder(decomposed)
			.set("value1.value", "abc")
			.sample();

		then(actual.value1.value).isEqualTo("abc");
		then(actual.value2.value).isEqualTo(1);
	}

	@Property
	void registerGroup() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(DefaultArbitraryGroup.class)
			.build();

		// when
		StringWrapperClass actual = sut.giveMeOne(StringWrapperClass.class);

		then(actual.value).isEqualTo("definition");
	}

	@Property
	void registerGroupInField() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(DefaultArbitraryGroup.class)
			.build();

		// when
		StringWrapperIntegerWrapperClass actual = sut.giveMeOne(StringWrapperIntegerWrapperClass.class);

		then(actual.value1.value).isEqualTo("definition");
	}

	@Property
	void registerGroupInFieldSet() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(DefaultArbitraryGroup.class)
			.build();

		// when
		StringWrapperIntegerWrapperClass actual = sut.giveMeBuilder(StringWrapperIntegerWrapperClass.class)
			.set("value1.value", "set")
			.sample();

		then(actual.value1.value).isEqualTo("set");
	}

	@Property
	void registerGroupList() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(DefaultArbitraryGroup.class)
			.build();

		// when
		NestedStringWrapperListClass actual = sut.giveMeBuilder(NestedStringWrapperListClass.class)
			.sample();

		then(actual.values).allMatch(it -> it.value.equals("definition"));
	}

	@Property
	void register() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(
				StringWrapperClass.class,
				it -> it.giveMeBuilder(StringWrapperClass.class).set("value", "definition")
			)
			.build();

		// when
		StringWrapperClass actual = sut.giveMeOne(StringWrapperClass.class);

		then(actual.value).isEqualTo("definition");
	}

	@Property
	void registerInField() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(
				StringWrapperClass.class,
				it -> it.giveMeBuilder(StringWrapperClass.class).set("value", "definition")
			)
			.build();

		// when
		StringWrapperIntegerWrapperClass actual = sut.giveMeOne(StringWrapperIntegerWrapperClass.class);

		then(actual.value1.value).isEqualTo("definition");
	}

	@Property
	void registerInFieldSet() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(
				StringWrapperClass.class,
				it -> it.giveMeBuilder(StringWrapperClass.class).set("value", "definition")
			)
			.build();

		// when
		StringWrapperIntegerWrapperClass actual = sut.giveMeBuilder(StringWrapperIntegerWrapperClass.class)
			.set("value1.value", "set")
			.sample();

		then(actual.value1.value).isEqualTo("set");
	}

	@Property
	void registerList() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(
				StringWrapperClass.class,
				it -> it.giveMeBuilder(StringWrapperClass.class).set("value", "definition")
			)
			.build();

		// when
		NestedStringWrapperListClass actual = sut.giveMeBuilder(NestedStringWrapperListClass.class)
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
				.registerGroup(DuplicateArbitraryGroup.class)
				.build())
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("can not register same classes twice.");
	}

	@Property
	void registerDiffGroupSameTypeThrows() {
		thenThrownBy(() ->
			FixtureMonkey.builder()
				.registerGroup(DefaultArbitraryGroup.class)
				.registerGroup(DefaultArbitraryGroup2.class)
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

	@Property
	void isDirtyReturnsFalse() {
		// given
		ArbitraryBuilder<StringWrapperClass> arbitraryBuilder = this.sut.giveMeBuilder(StringWrapperClass.class);

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isFalse();
	}

	@Property
	void isDirtyReturnsTrue() {
		// given
		ArbitraryBuilder<StringWrapperClass> arbitraryBuilder = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "test");

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isTrue();
	}

	@Property
	void isDirtyWhenManipulatedAndSampledReturnsTrue() {
		// given
		ArbitraryBuilder<StringWrapperClass> arbitraryBuilder = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "test");
		arbitraryBuilder.sample();

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isTrue();
	}

	@Property
	void isDirtyWhenManipulatedAndFixedReturnsFalse() {
		// given
		ArbitraryBuilder<StringWrapperClass> arbitraryBuilder = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "test")
			.fixed();

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isFalse();
	}

	@Property
	void isDirtyWhenManipulatedAndFixedAndManipulatedReturnsFalse() {
		// given
		ArbitraryBuilder<StringWrapperClass> arbitraryBuilder = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "test")
			.fixed()
			.set("value", "fixed");

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isTrue();
	}

	@Property
	void isDirtyWhenManipulatedAndApplyReturnsFalse() {
		// given
		ArbitraryBuilder<StringWrapperClass> arbitraryBuilder = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "test")
			.apply((builder, it) -> {
			});

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isFalse();
	}

	@Property
	void isDirtyWhenManipulatedAndAcceptIfReturnsFalse() {
		// given
		ArbitraryBuilder<StringWrapperClass> arbitraryBuilder = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "test")
			.acceptIf(it -> true, it -> {
			});

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isFalse();
	}

	@Property
	void isDirtyWhenManipulatedAndApplyAndManipulatedReturnsTrue() {
		// given
		ArbitraryBuilder<StringWrapperClass> arbitraryBuilder = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "test")
			.apply((builder, it) -> {
			})
			.set("value", "test");

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isTrue();
	}

	@Property
	void isDirtyWhenManipulatedAndAcceptIfAndManipulatedReturnsTrue() {
		// given
		ArbitraryBuilder<StringWrapperClass> arbitraryBuilder = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "test")
			.acceptIf(it -> true, it -> {
			})
			.set("value", "test");

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isTrue();
	}

	@Property
	void isDirtyWhenApplyAndFixedAndManipulatedReturnsTrue() {
		// given
		ArbitraryBuilder<StringWrapperClass> arbitraryBuilder = this.sut.giveMeBuilder(StringWrapperClass.class)
			.apply((builder, it) -> {
			})
			.fixed()
			.set("value", "test");

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isTrue();
	}

	@Property
	void isDirtyWhenApplyAndFixedReturnsFalse() {
		// given
		ArbitraryBuilder<StringWrapperClass> arbitraryBuilder = this.sut.giveMeBuilder(StringWrapperClass.class)
			.apply((builder, it) -> {
			})
			.fixed();

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isFalse();
	}

	@Property
	void isDirtyWhenAcceptIfAndFixedReturnsFalse() {
		// given
		ArbitraryBuilder<StringWrapperClass> arbitraryBuilder = this.sut.giveMeBuilder(StringWrapperClass.class)
			.acceptIf(it -> true, it -> {
			})
			.fixed();

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isFalse();
	}

	@Property
	void isDirtyWhenFixedAndApplyReturnsFalse() {
		// given
		ArbitraryBuilder<StringWrapperClass> arbitraryBuilder = this.sut.giveMeBuilder(StringWrapperClass.class)
			.fixed()
			.apply((builder, it) -> {
			});

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isFalse();
	}

	@Property
	void isDirtyWhenFixedAndAcceptIfReturnsFalse() {
		// given
		ArbitraryBuilder<StringWrapperClass> arbitraryBuilder = this.sut.giveMeBuilder(StringWrapperClass.class)
			.fixed()
			.acceptIf(it -> true, it -> {
			});

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isFalse();
	}

	@Property
	void isDirtyWhenDecomposeReturnsFalse() {
		// given
		StringWrapperClass stringWrapperClass = new StringWrapperClass("value");
		ArbitraryBuilder<StringWrapperClass> arbitraryBuilder = this.sut.giveMeBuilder(stringWrapperClass);

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isFalse();
	}

	@Property
	void isDirtyWhenDecomposeAndManipulatedReturnsTrue() {
		// given
		StringWrapperClass stringWrapperClass = new StringWrapperClass("value");
		ArbitraryBuilder<StringWrapperClass> arbitraryBuilder = this.sut.giveMeBuilder(stringWrapperClass)
			.set("value", "test");

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isTrue();
	}

	@Property
	void isDirtyWhenRegisterReturnsFalse() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(StringWrapperClass.class, fixture -> fixture.giveMeBuilder(StringWrapperClass.class))
			.build();
		ArbitraryBuilder<StringWrapperClass> arbitraryBuilder = sut.giveMeBuilder(StringWrapperClass.class);

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isFalse();
	}

	@Property
	void isDirtyWhenRegisterWithManipulatedReturnsTrue() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(StringWrapperClass.class, fixture ->
				fixture.giveMeBuilder(StringWrapperClass.class)
					.set("value", "test")
			)
			.build();
		ArbitraryBuilder<StringWrapperClass> arbitraryBuilder = sut.giveMeBuilder(StringWrapperClass.class);

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isTrue();
	}

	@Property
	void isDirtyWhenRegisterWithManipulatedAndFixedReturnsFalse() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(StringWrapperClass.class, fixture ->
				fixture.giveMeBuilder(StringWrapperClass.class)
					.set("value", "test")
					.fixed()
			)
			.build();
		ArbitraryBuilder<StringWrapperClass> arbitraryBuilder = sut.giveMeBuilder(StringWrapperClass.class);

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isFalse();
	}

	@Property
	void isDirtyWhenRegisterWithManipulatedAndApplyReturnsFalse() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(StringWrapperClass.class, fixture ->
				fixture.giveMeBuilder(StringWrapperClass.class)
					.set("value", "test")
					.apply((it, builder) -> {
					})
			)
			.build();
		ArbitraryBuilder<StringWrapperClass> arbitraryBuilder = sut.giveMeBuilder(StringWrapperClass.class);

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isFalse();
	}

	@Property
	void isDirtyWhenRegisterWithManipulatedAndAcceptIfReturnsFalse() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(StringWrapperClass.class, fixture ->
				fixture.giveMeBuilder(StringWrapperClass.class)
					.set("value", "test")
					.acceptIf(it -> true, it -> {
					})
			)
			.build();
		ArbitraryBuilder<StringWrapperClass> arbitraryBuilder = sut.giveMeBuilder(StringWrapperClass.class);

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isFalse();
	}

	@Property
	void isDirtyWhenRegisterWithDecomposedReturnsFalse() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(StringWrapperClass.class, fixture ->
				fixture.giveMeBuilder(new StringWrapperClass("value"))
			)
			.build();
		ArbitraryBuilder<StringWrapperClass> arbitraryBuilder = sut.giveMeBuilder(StringWrapperClass.class);

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isFalse();
	}

	@Property
	void isDirtyWhenRegisterWithDecomposedAndManipulatedReturnsTrue() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(StringWrapperClass.class, fixture ->
				fixture.giveMeBuilder(new StringWrapperClass("value"))
					.set("value", "test")
			)
			.build();
		ArbitraryBuilder<StringWrapperClass> arbitraryBuilder = sut.giveMeBuilder(StringWrapperClass.class);

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isTrue();
	}

	@Property
	void giveMeListWithAnnotation() {
		// when
		ListWithAnnotationWrapperClass actual = this.sut.giveMeOne(ListWithAnnotationWrapperClass.class);

		then(actual.values).isNotEmpty();
		then(actual.values).allMatch(StringUtils::isNotBlank);
	}

	@Property
	void giveMeListWithSameAnnotation() {
		// when
		ListWithSameAnnotationWrapperClass actual = this.sut.giveMeOne(ListWithSameAnnotationWrapperClass.class);

		then(actual.values).isNotNull();
		then(actual.values).allMatch(Objects::nonNull);
	}

	@Data
	public static class IntegerWrapperClass {
		int value;
	}

	@Data
	public static class IntegerWithAnnotationWrapperClass {
		@Positive
		int value;
	}

	@Data
	public static class IntegerListWrapperClass {
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
	public static class IntegerArrayWrapperClass {
		private Integer[] values;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class IntArrayWrapperClass {
		private int[] values;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MapKeyIntegerValueIntegerWrapperClass {
		private Map<Integer, Integer> values;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MapKeyIntegerValueStringWrapperClass {
		private Map<Integer, String> values;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MapEntryKeyIntegerValueStringWrapperClass {
		private Map.Entry<Integer, String> value;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class IntegerSetWrapperClass {
		private Set<Integer> values;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class IntegerIterableWrapperClass {
		private Iterable<Integer> values;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class IntegerIteratorWrapperClass {
		private Iterator<Integer> values;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class IntegerStreamWrapperClass {
		private Stream<Integer> values;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class IntegerOptionalWrapperClass {
		@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
		private Optional<Integer> value;
	}

	@Data
	public static class NestedStringWrapperListClass {
		private List<StringWrapperClass> values;
	}

	@Data
	public static class OptionalClass {
		@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
		private Optional<Integer> value;
	}

	@Data
	public static class IntegerListWithNotEmptyWrapperClass {
		@NotEmpty
		private List<Integer> values;
	}

	@Data
	public static class ExceptGenerateClass {
		String value;
	}

	@Data
	public static class StringWrapperIntegerWrapperClass {
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
	public static class StringWithNullableWrapperClass {
		@Nullable
		String value;
	}

	@Data
	public static class StringWithNotBlankWrapperClass {
		@NotBlank
		String value;
	}

	public static class DefaultArbitraryGroup {
		public DefaultArbitraryGroup() {
		}

		public ArbitraryBuilder<StringWrapperClass> string(FixtureMonkey fixture) {
			return fixture.giveMeBuilder(StringWrapperClass.class)
				.set("value", "definition");
		}
	}

	public static class DefaultArbitraryGroup2 {
		public DefaultArbitraryGroup2() {
		}

		public ArbitraryBuilder<StringWrapperClass> string(FixtureMonkey fixture) {
			return fixture.giveMeBuilder(StringWrapperClass.class)
				.set("value", "definition");
		}
	}

	public static class DuplicateArbitraryGroup {
		public DuplicateArbitraryGroup() {
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
	public static class NestedStringWrapperClass {
		StringWrapperClass value;
	}

	@Data
	public static class ListWithAnnotationWrapperClass {
		@NotEmpty
		List<@NotBlank String> values;
	}

	@Data
	public static class ListWithSameAnnotationWrapperClass {
		@NotNull
		List<@NotNull String> values;
	}
}
