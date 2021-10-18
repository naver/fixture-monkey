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

import static com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.SUT;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenNoException;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import org.junit.platform.commons.util.StringUtils;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.TooManyFilterMissesException;
import net.jqwik.api.Tuple.Tuple1;
import net.jqwik.api.Tuple.Tuple2;
import net.jqwik.api.constraints.Size;
import net.jqwik.api.domains.Domain;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.DefaultArbitraryGroup;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.DefaultArbitraryGroup2;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.DuplicateArbitraryGroup;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.IntArray;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.IntWithAnnotation;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.IntegerArray;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.IntegerListWithNotEmpty;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.IntegerOptional;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.IntegerSet;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.InterfaceWrapper;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.ListWithAnnotation;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.MapEntryKeyIntegerValueString;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.MapKeyIntegerValueInteger;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.MapKeyIntegerValueString;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.MockInterface;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.NestedStringWithNotBlankList;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringAndInt;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringWithNotBlank;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringWithNullable;

class FixtureMonkeyTest {
	@Property
	@Domain(FixtureMonkeyTestSpecs.class)
	void giveMeRegisteredWrapper(@ForAll Integer actual) {
		then(actual).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	@Domain(FixtureMonkeyTestSpecs.class)
	void giveMeRegisteredPrimitive(@ForAll int actual) {
		then(actual).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	@Domain(FixtureMonkeyTestSpecs.class)
	void giveMeWithAnnotation(@ForAll IntWithAnnotation actual) {
		then(actual.getValue()).isPositive();
	}

	@Property
	@Domain(FixtureMonkeyTestSpecs.class)
	void giveMeArrayToBuilder(@ForAll IntegerArray integerArray) {
		IntegerArray actual = SUT.giveMeBuilder(integerArray).sample();

		then(actual.getValues().length).isEqualTo(integerArray.getValues().length);
		for (int i = 0; i < actual.getValues().length; i++) {
			then(actual.getValues()[i]).isEqualTo(integerArray.getValues()[i]);
		}
	}

	@Property
	@Domain(FixtureMonkeyTestSpecs.class)
	void giveMePrimitiveArrayToBuilder(@ForAll IntArray intArray) {
		IntArray actual = SUT.giveMeBuilder(intArray).sample();

		then(actual.getValues().length).isEqualTo(intArray.getValues().length);
		for (int i = 0; i < actual.getValues().length; i++) {
			then(actual.getValues()[i]).isEqualTo(intArray.getValues()[i]);
		}
	}

	@Property
	@Domain(FixtureMonkeyTestSpecs.class)
	void giveMeSameKeyValueMapToBuilder(@ForAll MapKeyIntegerValueInteger mapKeyIntegerValueInteger) {
		MapKeyIntegerValueInteger actual = SUT.giveMeBuilder(mapKeyIntegerValueInteger).sample();

		then(actual.getValues().size()).isEqualTo(mapKeyIntegerValueInteger.getValues().size());

		Map<Integer, Integer> target = mapKeyIntegerValueInteger.getValues();
		for (Entry<Integer, Integer> entry : actual.getValues().entrySet()) {
			then(entry.getValue()).isEqualTo(target.get(entry.getKey()));
		}
	}

	@Property
	@Domain(FixtureMonkeyTestSpecs.class)
	void giveMeDiffKeyValueMapToBuilder(@ForAll MapKeyIntegerValueString mapKeyIntegerValueString) {
		MapKeyIntegerValueString actual = SUT.giveMeBuilder(mapKeyIntegerValueString).sample();

		then(actual.getValues().size()).isEqualTo(mapKeyIntegerValueString.getValues().size());

		Map<Integer, String> target = mapKeyIntegerValueString.getValues();
		for (Entry<Integer, String> entry : actual.getValues().entrySet()) {
			then(entry.getValue()).isEqualTo(target.get(entry.getKey()));
		}
	}

	@Property
	@Domain(FixtureMonkeyTestSpecs.class)
	void giveMeMapEntryToBuilder(@ForAll MapEntryKeyIntegerValueString mapEntryKeyIntegerValueString) {
		MapEntryKeyIntegerValueString actual = SUT.giveMeBuilder(mapEntryKeyIntegerValueString).sample();
		then(actual.getValue().getKey()).isEqualTo(mapEntryKeyIntegerValueString.getValue().getKey());
		then(actual.getValue().getValue()).isEqualTo(mapEntryKeyIntegerValueString.getValue().getValue());
	}

	@Property
	@Domain(FixtureMonkeyTestSpecs.class)
	void giveMeSetToBuilder(@ForAll IntegerSet integerSet) {
		IntegerSet actual = SUT.giveMeBuilder(integerSet).sample();
		then(actual.getValues().size()).isEqualTo(integerSet.getValues().size());
		then(actual.getValues()).containsAll(integerSet.getValues());
	}

	@Property
	@Domain(FixtureMonkeyTestSpecs.class)
	void giveMeOptionalToBuilder(@ForAll IntegerOptional integerOptional) {
		IntegerOptional actual = SUT.giveMeBuilder(integerOptional).sample();
		then(actual.getValue()).isEqualTo(integerOptional.getValue());
	}

	@Property
	@Domain(FixtureMonkeyTestSpecs.class)
	void copy(@ForAll IntegerArray integerArray) {
		ArbitraryBuilder<IntegerArray> builder = SUT.giveMeBuilder(integerArray)
			.size("values", 5)
			.set("values[1]", 5);

		ArbitraryBuilder<IntegerArray> copiedBuilder = builder.copy()
			.set("values[1]", 3);

		IntegerArray targetSample = builder.sample();
		IntegerArray copiedSample = copiedBuilder.sample();

		then(targetSample.getValues().length).isEqualTo(5);
		then(copiedSample.getValues().length).isEqualTo(5);
		then(targetSample.getValues()[1]).isEqualTo(5);
		then(copiedSample.getValues()[1]).isEqualTo(3);
	}

	@Property
	void giveMeList() {
		List<IntWithAnnotation> actual = SUT.giveMe(IntWithAnnotation.class, 5);

		then(actual).hasSize(5);
		then(actual).allMatch(Objects::nonNull);
	}

	@Property
	@Domain(FixtureMonkeyTestSpecs.class)
	void giveMeOptional(@ForAll IntegerOptional integerOptional) {
		then(integerOptional.getValue()).isNotNull();
	}

	@Property
	@Domain(FixtureMonkeyTestSpecs.class)
	void giveMeArbitraryThenSample(@ForAll StringWithNotBlank actual) {
		then(actual.getValue()).isNotBlank();
	}

	@Property
	void giveMeArbitraryThenSampleStream() {
		Arbitrary<StringWithNotBlank> sut = SUT.giveMeArbitrary(StringWithNotBlank.class);
		List<StringWithNotBlank> actual = sut.sampleStream().limit(5).collect(toList());
		actual.forEach(it -> then(it.getValue()).isNotBlank());
	}

	@Example
	void giveMeBuilderBuildInvalidThenSampleThrowsTooManyFilterMissesException() {
		// when
		Arbitrary<StringWithNotBlank> sut = SUT.giveMeBuilder(StringWithNotBlank.class)
			.set("value", "")
			.build();

		thenThrownBy(sut::sample)
			.isExactlyInstanceOf(TooManyFilterMissesException.class);
	}

	@Example
	void giveMeBuilderBuildInvalidThenSampleStreamThrowsTooManyFilterMissesException() {
		Arbitrary<StringWithNotBlank> sut = SUT.giveMeBuilder(StringWithNotBlank.class)
			.set("value", "")
			.build();

		//noinspection ResultOfMethodCallIgnored
		thenThrownBy(() -> sut.sampleStream().limit(5).collect(toList()))
			.isExactlyInstanceOf(TooManyFilterMissesException.class);
	}

	@Example
	void giveMeBuilderBuildUniqueInvalidThenSampleThrowsTooManyFilterMissesException() {
		Arbitrary<StringWithNotBlank> sut = SUT.giveMeBuilder(StringWithNotBlank.class)
			.set("value", "")
			.build()
			.unique();

		thenThrownBy(sut::sample)
			.isExactlyInstanceOf(TooManyFilterMissesException.class);
	}

	@Example
	void giveMeArbitraryUnique() {
		// when
		Arbitrary<StringWithNotBlank> sut = SUT.giveMeArbitrary(StringWithNotBlank.class)
			.unique();

		thenNoException()
			.isThrownBy(sut::sample);
	}

	@Example
	void giveMeBuilderBuildFixGenSizeInvalidThenSampleThrowsTooManyFilterMissesException() {
		// when
		Arbitrary<StringWithNotBlank> sut = SUT.giveMeBuilder(StringWithNotBlank.class)
			.set("value", "")
			.build()
			.fixGenSize(5);

		thenThrownBy(sut::sample)
			.isExactlyInstanceOf(TooManyFilterMissesException.class);
	}

	@Example
	void giveMeBuilderBuildOptionalInvalidThenSampleThrowsTooManyFilterMissesException() {
		// when
		Arbitrary<Optional<StringWithNotBlank>> sut =
			SUT.giveMeBuilder(StringWithNotBlank.class)
				.set("value", "")
				.build()
				.optional()
				.filter(Optional::isPresent);

		thenThrownBy(sut::sample)
			.isExactlyInstanceOf(TooManyFilterMissesException.class);
	}

	@Property
	void giveMeArbitraryOptional() {
		// given
		Arbitrary<Optional<StringWithNotBlank>> sut =
			SUT.giveMeArbitrary(StringWithNotBlank.class)
				.optional();

		// when
		Optional<StringWithNotBlank> actual = sut.sample();

		then(actual).isNotNull();
	}

	@Example
	void giveMeBuilderBuildCollectInvalidThenSampleThrowsTooManyFilterMissesException() {
		// when
		Arbitrary<List<StringWithNotBlank>> sut =
			SUT.giveMeBuilder(StringWithNotBlank.class)
				.set("value", "")
				.build().collect(it -> !it.isEmpty());

		thenThrownBy(sut::sample)
			.isExactlyInstanceOf(TooManyFilterMissesException.class);
	}

	@Property
	void giveMeArbitraryCollect() {
		// given
		Arbitrary<List<StringWithNotBlank>> sut =
			SUT.giveMeArbitrary(StringWithNotBlank.class)
				.collect(List::isEmpty);

		// when
		List<StringWithNotBlank> actual = sut.sample();

		then(actual).isEmpty();
	}

	@Example
	void giveMeBuilderBuildInjectDuplicatesInvalidThenSampleThrowsTooManyFilterMissesException() {
		// when
		Arbitrary<StringWithNotBlank> sut =
			SUT.giveMeBuilder(StringWithNotBlank.class)
				.set("value", "")
				.build()
				.injectDuplicates(1);

		thenThrownBy(sut::sample)
			.isExactlyInstanceOf(TooManyFilterMissesException.class);
	}

	@Example
	void giveMeArbitraryInjectDuplicates() {
		// given
		Arbitrary<StringWithNotBlank> sut = SUT.giveMeArbitrary(StringWithNotBlank.class)
			.injectDuplicates(1);

		thenNoException()
			.isThrownBy(sut::sample);
	}

	@Example
	void giveMeBuilderBuildTuple1InvalidThenSampleThrowsTooManyFilterMissesException() {
		// when
		Arbitrary<Tuple1<StringWithNotBlank>> sut = SUT.giveMeBuilder(StringWithNotBlank.class)
			.set("value", "")
			.build()
			.tuple1();

		thenThrownBy(sut::sample)
			.isExactlyInstanceOf(TooManyFilterMissesException.class);
	}

	@Property
	void giveMeArbitraryTuple1() {
		// given
		Arbitrary<Tuple1<StringWithNotBlank>> sut = SUT.giveMeArbitrary(StringWithNotBlank.class).tuple1();

		// when
		Tuple1<StringWithNotBlank> sample = sut.sample();

		then(sample).isNotNull();
	}

	@Example
	void giveMeBuilderBuildTuple2InvalidThenSampleThrowsTooManyFilterMissesException() {
		// when
		Arbitrary<Tuple2<StringWithNotBlank, StringWithNotBlank>> sut = SUT.giveMeBuilder(StringWithNotBlank.class)
			.set("value", "")
			.build()
			.tuple2();

		thenThrownBy(sut::sample)
			.isExactlyInstanceOf(TooManyFilterMissesException.class);
	}

	@Property
	void giveMeArbitraryTuple2() {
		// given
		Arbitrary<Tuple2<StringWithNotBlank, StringWithNotBlank>> sut = SUT.giveMeArbitrary(StringWithNotBlank.class)
			.tuple2();

		// when
		Tuple2<StringWithNotBlank, StringWithNotBlank> sample = sut.sample();

		then(sample).isNotNull();
	}

	@Example
	void giveMeBuilderBuildIgnoreExceptionInvalidThenSampleThrowsTooManyFilterMissesException() {
		// when
		Arbitrary<StringWithNotBlank> sut = SUT.giveMeBuilder(StringWithNotBlank.class)
			.set("value", "")
			.build()
			.ignoreException(NullPointerException.class);

		thenThrownBy(sut::sample)
			.isExactlyInstanceOf(TooManyFilterMissesException.class);
	}

	@Property
	void giveMeArbitraryIgnoreException() {
		// given
		Arbitrary<StringWithNotBlank> sut = SUT.giveMeArbitrary(StringWithNotBlank.class)
			.ignoreException(NullPointerException.class);

		thenNoException()
			.isThrownBy(sut::sample);
	}

	@Example
	void giveMeBuilderBuildDontShrinkInvalidThenSampleThrowsTooManyFilterMissesException() {
		// when
		Arbitrary<StringWithNotBlank> sut = SUT.giveMeBuilder(StringWithNotBlank.class)
			.set("value", "")
			.build()
			.dontShrink();

		thenThrownBy(sut::sample)
			.isExactlyInstanceOf(TooManyFilterMissesException.class);
	}

	@Property
	void giveMeArbitraryDontShrink() {
		// given
		Arbitrary<StringWithNotBlank> sut = SUT.giveMeArbitrary(StringWithNotBlank.class)
			.dontShrink();

		thenNoException()
			.isThrownBy(sut::sample);
	}

	@Example
	void giveMeBuilderBuildEdgeCasesInvalidThenSampleThrowsTooManyFilterMissesException() {
		// when
		Arbitrary<StringWithNotBlank> sut = SUT.giveMeBuilder(StringWithNotBlank.class)
			.set("value", "")
			.build()
			.edgeCases(it -> {
			});

		thenThrownBy(sut::sample)
			.isExactlyInstanceOf(TooManyFilterMissesException.class);
	}

	@Property
	void giveMeArbitraryEdgeCases() {
		// given
		Arbitrary<StringWithNotBlank> sut = SUT.giveMeArbitrary(StringWithNotBlank.class)
			.edgeCases(it -> {
			});

		thenNoException()
			.isThrownBy(sut::sample);
	}

	@Example
	void giveMeBuilderBuildInjectNullInvalidThenSampleThrowsTooManyFilterMissesException() {
		// when
		Arbitrary<StringWithNotBlank> sut = SUT.giveMeBuilder(StringWithNotBlank.class)
			.set("value", "")
			.build()
			.injectNull(0);

		thenThrownBy(sut::sample)
			.isExactlyInstanceOf(TooManyFilterMissesException.class);
	}

	@Example
	void giveMeArbitraryInjectNull() {
		// given
		Arbitrary<StringWithNotBlank> sut = SUT.giveMeArbitrary(StringWithNotBlank.class)
			.injectNull(1);

		StringWithNotBlank actual = sut.sample();

		then(actual).isNull();
	}

	@Example
	void giveMeBuilderBuildFlatMapInvalidThenSampleThrowsTooManyFilterMissesException() {
		// when
		Arbitrary<String> sut = SUT.giveMeBuilder(StringWithNotBlank.class)
			.set("value", "")
			.build()
			.flatMap(it -> Arbitraries.just("String"));

		thenThrownBy(sut::sample)
			.isExactlyInstanceOf(TooManyFilterMissesException.class);
	}

	@Example
	void giveMeArbitraryFlatMap() {
		// given
		Arbitrary<String> sut = SUT.giveMeArbitrary(StringWithNotBlank.class)
			.flatMap(it -> Arbitraries.just("String"));

		String actual = sut.sample();

		then(actual).isEqualTo("String");
	}

	@Example
	void giveMeBuilderBuildMapInvalidThenSampleThrowsTooManyFilterMissesException() {
		// when
		Arbitrary<StringWithNotBlank> sut = SUT.giveMeBuilder(StringWithNotBlank.class)
			.set("value", "")
			.build()
			.map(it -> it);

		thenThrownBy(sut::sample)
			.isExactlyInstanceOf(TooManyFilterMissesException.class);
	}

	@Property
	void giveMeArbitraryMap() {
		// given
		Arbitrary<StringWithNotBlank> sut = SUT.giveMeArbitrary(StringWithNotBlank.class)
			.map(it -> it);

		// when
		StringWithNotBlank actual = sut.sample();

		then(actual).isNotNull();
	}

	@Example
	void giveMeBuilderBuildFilterInvalidThenSampleThrowsTooManyFilterMissesException() {
		// when
		Arbitrary<StringWithNotBlank> sut = SUT.giveMeBuilder(StringWithNotBlank.class)
			.set("value", "")
			.build()
			.filter(it -> true);

		thenThrownBy(sut::sample)
			.isExactlyInstanceOf(TooManyFilterMissesException.class);
	}

	@Property
	void giveMeArbitraryFilter() {
		// given
		Arbitrary<StringWithNotBlank> sut = SUT.giveMeArbitrary(StringWithNotBlank.class)
			.filter(it -> true);

		// when
		StringWithNotBlank actual = sut.sample();

		then(actual).isNotNull();
	}

	@Example
	void giveMeBuilderBuildAsGenericInvalidThenSampleThrowsTooManyFilterMissesException() {
		// when
		Arbitrary<Object> sut = SUT.giveMeBuilder(StringWithNotBlank.class)
			.set("value", "")
			.build()
			.asGeneric();

		thenThrownBy(sut::sample)
			.isExactlyInstanceOf(TooManyFilterMissesException.class);
	}

	@Property
	void giveMeArbitraryAsGeneric() {
		// given
		Arbitrary<Object> sut = SUT.giveMeArbitrary(StringWithNotBlank.class)
			.asGeneric();

		// when
		Object actual = sut.sample();

		then(actual).isNotNull();
	}

	@Property
	void setAfterBuildNotAffected() {
		// given
		ArbitraryBuilder<StringWithNotBlank> builder = SUT.giveMeBuilder(StringWithNotBlank.class);
		Arbitrary<StringWithNotBlank> build = builder.build();

		// when
		ArbitraryBuilder<StringWithNotBlank> actual = builder.set("value", "set");

		StringWithNotBlank actualSample = actual.sample();
		StringWithNotBlank buildSample = build.sample();
		then(actualSample).isNotEqualTo(buildSample);
		then(actualSample.getValue()).isEqualTo("set");
	}

	@Property
	@Domain(FixtureMonkeyTestSpecs.class)
	void giveMeNotEmpty(@ForAll IntegerListWithNotEmpty actual) {
		then(actual.getValues()).isNotEmpty();
	}

	@Example
	void addExceptGenerate() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.addExceptGeneratePackage("com.navercorp.fixturemonkey.test")
			.build();

		// when
		IntWithAnnotation actual = sut.giveMeOne(IntWithAnnotation.class);

		then(actual).isNull();
	}

	@Property
	void giveMeSizeListSmallerThanValueWhenDecomposed(@ForAll @Size(3) List<Integer> values) {
		// given
		IntegerListWithNotEmpty value = new IntegerListWithNotEmpty();
		value.setValues(values);

		// when
		IntegerListWithNotEmpty actual = SUT.giveMeBuilder(value)
			.size("values", 1)
			.sample();

		then(actual.getValues()).hasSize(1);
		then(actual.getValues().get(0)).isEqualTo(values.get(0));
	}

	@Property
	void giveMeSizeListBiggerThanValueWhenDecomposed(@ForAll @Size(3) List<Integer> values) {
		// given
		IntegerListWithNotEmpty value = new IntegerListWithNotEmpty();
		value.setValues(values);

		// when
		IntegerListWithNotEmpty actual = SUT.giveMeBuilder(value)
			.size("values", 5)
			.sample();

		then(actual.getValues()).hasSize(5);
		then(actual.getValues().get(0)).isEqualTo(values.get(0));
		then(actual.getValues().get(1)).isEqualTo(values.get(1));
		then(actual.getValues().get(2)).isEqualTo(values.get(2));
	}

	@Property
	void giveMeSizeArraySmallerThanValueWhenDecomposed(@ForAll @Size(3) Integer[] values) {
		// given
		IntegerArray value = new IntegerArray();
		value.setValues(values);

		// when
		IntegerArray actual = SUT.giveMeBuilder(value)
			.size("values", 1)
			.sample();

		then(actual.getValues()).hasSize(1);
		then(actual.getValues()[0]).isEqualTo(values[0]);
	}

	@Property
	void giveMeSizeArrayBiggerThanValueWhenDecomposed(@ForAll @Size(3) Integer[] values) {
		// given
		IntegerArray value = new IntegerArray();
		value.setValues(values);

		// when
		IntegerArray actual = SUT.giveMeBuilder(value)
			.size("values", 5)
			.sample();

		then(actual.getValues()).hasSize(5);
		then(actual.getValues()[0]).isEqualTo(values[0]);
		then(actual.getValues()[1]).isEqualTo(values[1]);
		then(actual.getValues()[2]).isEqualTo(values[2]);
	}

	@Property
	@Domain(FixtureMonkeyTestSpecs.class)
	void giveMeInterfaceIsNull(@ForAll InterfaceWrapper actual) {
		then(actual.getValue()).isNull();
	}

	@Property
	void giveMeInterfaceWithDefaultInterfaceSupplier() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.addInterfaceSupplier(MockInterface.class, (type) -> () -> "test")
			.build();

		InterfaceWrapper actual = sut.giveMeOne(InterfaceWrapper.class);

		then(actual.getValue().get()).isEqualTo("test");
	}

	@Property
	void defaultNullInject() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.nullInject(1.0d)
			.build();

		// when
		StringWithNullable actual = sut.giveMeOne(StringWithNullable.class);

		then(actual.getValue()).isNull();
	}

	@Property
	void defaultNullInjectWithDefaultNotNull() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.nullInject(1.0d)
			.defaultNotNull(true)
			.build();

		// when
		StringWithNullable actual = sut.giveMeOne(StringWithNullable.class);

		then(actual.getValue()).isNull();
	}

	@Property
	void giveMeNullableDefaultNullInjectWithDefaultNotNull() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.nullInject(1.0d)
			.defaultNotNull(true)
			.build();

		// when
		StringWithNullable actual = sut.giveMeOne(StringWithNullable.class);

		then(actual.getValue()).isNull();
	}

	@Property
	void giveMeNotBlankString() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.nullInject(1.0d)
			.build();

		// when
		StringWithNotBlank actual = sut.giveMeOne(StringWithNotBlank.class);

		then(actual.getValue()).isNotNull();
	}

	@Property
	@Domain(FixtureMonkeyTestSpecs.class)
	void mapIntegerListClassBiggerThanMapped(@ForAll @Size(1) List<Integer> values) {
		// given
		IntegerListWithNotEmpty mapped = new IntegerListWithNotEmpty();
		mapped.setValues(values);

		// when
		IntegerListWithNotEmpty actual = SUT.giveMeBuilder(mapped)
			.size("values", 2)
			.sample();

		then(actual.getValues()).hasSize(2);
		then(actual.getValues().get(1)).isNotNull();
	}

	@Property
	void decomposeNullIsNotGenerated() {
		// given
		StringAndInt decomposed = new StringAndInt();
		decomposed.setValue1(null);
		IntWithAnnotation value2 = new IntWithAnnotation();
		value2.setValue(1);
		decomposed.setValue2(value2);

		// when
		StringAndInt actual = SUT.giveMeBuilder(decomposed).sample();

		then(actual.getValue1()).isNull();
		then(actual.getValue2().getValue()).isEqualTo(1);
	}

	@Property
	void decomposeNullSetThenGenerate() {
		// given
		StringAndInt decomposed = new StringAndInt();
		decomposed.setValue1(null);
		IntWithAnnotation value2 = new IntWithAnnotation();
		value2.setValue(1);
		decomposed.setValue2(value2);

		// when
		StringAndInt actual = SUT.giveMeBuilder(decomposed)
			.set("value1.value", "abc")
			.sample();

		then(actual.getValue1().getValue()).isEqualTo("abc");
		then(actual.getValue2().getValue()).isEqualTo(1);
	}

	@Property
	void registerGroup() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(DefaultArbitraryGroup.class)
			.build();

		// when
		StringWithNotBlank actual = sut.giveMeOne(StringWithNotBlank.class);

		then(actual.getValue()).isEqualTo("definition");
	}

	@Property
	void registerGroupInField() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(DefaultArbitraryGroup.class)
			.build();

		// when
		StringAndInt actual = sut.giveMeOne(StringAndInt.class);

		then(actual.getValue1().getValue()).isEqualTo("definition");
	}

	@Property
	void registerGroupInFieldSet() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(DefaultArbitraryGroup.class)
			.build();

		// when
		StringAndInt actual = sut.giveMeBuilder(StringAndInt.class)
			.set("value1.value", "set")
			.sample();

		then(actual.getValue1().getValue()).isEqualTo("set");
	}

	@Property
	void registerGroupList() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(DefaultArbitraryGroup.class)
			.build();

		// when
		NestedStringWithNotBlankList actual = sut.giveMeBuilder(NestedStringWithNotBlankList.class)
			.sample();

		then(actual.getValues()).allMatch(it -> it.getValue().equals("definition"));
	}

	@Property
	void register() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(
				StringWithNotBlank.class,
				it -> it.giveMeBuilder(StringWithNotBlank.class).set("value", "definition")
			)
			.build();

		// when
		StringWithNotBlank actual = sut.giveMeOne(StringWithNotBlank.class);

		then(actual.getValue()).isEqualTo("definition");
	}

	@Property
	void registerInField() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(
				StringWithNotBlank.class,
				it -> it.giveMeBuilder(StringWithNotBlank.class).set("value", "definition")
			)
			.build();

		// when
		StringAndInt actual = sut.giveMeOne(StringAndInt.class);

		then(actual.getValue1().getValue()).isEqualTo("definition");
	}

	@Property
	void registerInFieldSet() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(
				StringWithNotBlank.class,
				it -> it.giveMeBuilder(StringWithNotBlank.class).set("value", "definition")
			)
			.build();

		// when
		StringAndInt actual = sut.giveMeBuilder(StringAndInt.class)
			.set("value1.value", "set")
			.sample();

		then(actual.getValue1().getValue()).isEqualTo("set");
	}

	@Property
	void registerList() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(
				StringWithNotBlank.class,
				it -> it.giveMeBuilder(StringWithNotBlank.class).set("value", "definition")
			)
			.build();

		// when
		NestedStringWithNotBlankList actual = sut.giveMeBuilder(NestedStringWithNotBlankList.class)
			.sample();

		then(actual.getValues()).allMatch(it -> it.getValue().equals("definition"));
	}

	@Property
	void registerSameTypeThrows() {
		thenThrownBy(() ->
			FixtureMonkey.builder()
				.register(
					StringWithNotBlank.class,
					it -> it.giveMeBuilder(StringWithNotBlank.class).set("value", "definition")
				)
				.register(
					StringWithNotBlank.class,
					it -> it.giveMeBuilder(StringWithNotBlank.class).set("value", "error")
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
		StringWithNullable actual = sut.giveMeOne(StringWithNullable.class);

		then(actual.getValue()).isNull();
	}

	@Property
	void isDirtyReturnsFalse() {
		// given
		ArbitraryBuilder<StringWithNotBlank> arbitraryBuilder = SUT.giveMeBuilder(StringWithNotBlank.class);

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isFalse();
	}

	@Property
	void isDirtyReturnsTrue() {
		// given
		ArbitraryBuilder<StringWithNotBlank> arbitraryBuilder = SUT.giveMeBuilder(StringWithNotBlank.class)
			.set("value", "test");

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isTrue();
	}

	@Property
	void isDirtyWhenManipulatedAndSampledReturnsTrue() {
		// given
		ArbitraryBuilder<StringWithNotBlank> arbitraryBuilder = SUT.giveMeBuilder(StringWithNotBlank.class)
			.set("value", "test");
		arbitraryBuilder.sample();

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isTrue();
	}

	@Property
	void isDirtyWhenManipulatedAndFixedReturnsFalse() {
		// given
		ArbitraryBuilder<StringWithNotBlank> arbitraryBuilder = SUT.giveMeBuilder(StringWithNotBlank.class)
			.set("value", "test")
			.fixed();

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isFalse();
	}

	@Property
	void isDirtyWhenManipulatedAndFixedAndManipulatedReturnsFalse() {
		// given
		ArbitraryBuilder<StringWithNotBlank> arbitraryBuilder = SUT.giveMeBuilder(StringWithNotBlank.class)
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
		ArbitraryBuilder<StringWithNotBlank> arbitraryBuilder = SUT.giveMeBuilder(StringWithNotBlank.class)
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
		ArbitraryBuilder<StringWithNotBlank> arbitraryBuilder = SUT.giveMeBuilder(StringWithNotBlank.class)
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
		ArbitraryBuilder<StringWithNotBlank> arbitraryBuilder = SUT.giveMeBuilder(StringWithNotBlank.class)
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
		ArbitraryBuilder<StringWithNotBlank> arbitraryBuilder = SUT.giveMeBuilder(StringWithNotBlank.class)
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
		ArbitraryBuilder<StringWithNotBlank> arbitraryBuilder = SUT.giveMeBuilder(StringWithNotBlank.class)
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
		ArbitraryBuilder<StringWithNotBlank> arbitraryBuilder = SUT.giveMeBuilder(StringWithNotBlank.class)
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
		ArbitraryBuilder<StringWithNotBlank> arbitraryBuilder = SUT.giveMeBuilder(StringWithNotBlank.class)
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
		ArbitraryBuilder<StringWithNotBlank> arbitraryBuilder = SUT.giveMeBuilder(StringWithNotBlank.class)
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
		ArbitraryBuilder<StringWithNotBlank> arbitraryBuilder = SUT.giveMeBuilder(StringWithNotBlank.class)
			.fixed()
			.acceptIf(it -> true, it -> {
			});

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isFalse();
	}

	@Property
	@Domain(FixtureMonkeyTestSpecs.class)
	void isDirtyWhenDecomposeReturnsFalse(@ForAll StringWithNotBlank stringWithNotBlank) {
		// given
		ArbitraryBuilder<StringWithNotBlank> arbitraryBuilder = SUT.giveMeBuilder(stringWithNotBlank);

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isFalse();
	}

	@Property
	void isDirtyWhenDecomposeAndManipulatedReturnsTrue() {
		// given
		ArbitraryBuilder<StringWithNotBlank> arbitraryBuilder = SUT.giveMeBuilder(StringWithNotBlank.class)
			.set("value", "test");

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isTrue();
	}

	@Property
	void isDirtyWhenRegisterReturnsFalse() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(StringWithNotBlank.class, fixture -> fixture.giveMeBuilder(StringWithNotBlank.class))
			.build();
		ArbitraryBuilder<StringWithNotBlank> arbitraryBuilder = sut.giveMeBuilder(StringWithNotBlank.class);

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isFalse();
	}

	@Property
	void isDirtyWhenRegisterWithManipulatedReturnsTrue() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(StringWithNotBlank.class, fixture ->
				fixture.giveMeBuilder(StringWithNotBlank.class)
					.set("value", "test")
			)
			.build();
		ArbitraryBuilder<StringWithNotBlank> arbitraryBuilder = sut.giveMeBuilder(StringWithNotBlank.class);

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isTrue();
	}

	@Property
	void isDirtyWhenRegisterWithManipulatedAndFixedReturnsFalse() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(StringWithNotBlank.class, fixture ->
				fixture.giveMeBuilder(StringWithNotBlank.class)
					.set("value", "test")
					.fixed()
			)
			.build();
		ArbitraryBuilder<StringWithNotBlank> arbitraryBuilder = sut.giveMeBuilder(StringWithNotBlank.class);

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isFalse();
	}

	@Property
	void isDirtyWhenRegisterWithManipulatedAndApplyReturnsFalse() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(StringWithNotBlank.class, fixture ->
				fixture.giveMeBuilder(StringWithNotBlank.class)
					.set("value", "test")
					.apply((it, builder) -> {
					})
			)
			.build();
		ArbitraryBuilder<StringWithNotBlank> arbitraryBuilder = sut.giveMeBuilder(StringWithNotBlank.class);

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isFalse();
	}

	@Property
	void isDirtyWhenRegisterWithManipulatedAndAcceptIfReturnsFalse() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(StringWithNotBlank.class, fixture ->
				fixture.giveMeBuilder(StringWithNotBlank.class)
					.set("value", "test")
					.acceptIf(it -> true, it -> {
					})
			)
			.build();
		ArbitraryBuilder<StringWithNotBlank> arbitraryBuilder = sut.giveMeBuilder(StringWithNotBlank.class);

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isFalse();
	}

	@Property
	@Domain(FixtureMonkeyTestSpecs.class)
	void isDirtyWhenRegisterWithDecomposedReturnsFalse(@ForAll StringWithNotBlank stringWithNotBlank) {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(StringWithNotBlank.class, fixture ->
				fixture.giveMeBuilder(stringWithNotBlank)
			)
			.build();
		ArbitraryBuilder<StringWithNotBlank> arbitraryBuilder = sut.giveMeBuilder(StringWithNotBlank.class);

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isFalse();
	}

	@Property
	@Domain(FixtureMonkeyTestSpecs.class)
	void isDirtyWhenRegisterWithDecomposedAndManipulatedReturnsTrue(@ForAll StringWithNotBlank stringWithNotBlank) {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(StringWithNotBlank.class, fixture ->
				fixture.giveMeBuilder(stringWithNotBlank)
					.set("value", "test")
			)
			.build();
		ArbitraryBuilder<StringWithNotBlank> arbitraryBuilder = sut.giveMeBuilder(StringWithNotBlank.class);

		// when
		boolean changed = arbitraryBuilder.isDirty();

		then(changed).isTrue();
	}

	@Property
	@Domain(FixtureMonkeyTestSpecs.class)
	void giveMeListWithAnnotation(@ForAll ListWithAnnotation actual) {
		then(actual.getValues()).isNotEmpty();
		then(actual.getValues()).allMatch(StringUtils::isNotBlank);
	}

	@Property
	void copyValidOnly() {
		thenNoException()
			.isThrownBy(() -> SUT.giveMeBuilder(ListWithAnnotation.class)
				.size("values", 0)
				.validOnly(false)
				.copy()
				.sample());
	}
}
