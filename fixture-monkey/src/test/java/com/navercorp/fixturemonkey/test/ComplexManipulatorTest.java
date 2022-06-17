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

import static com.navercorp.fixturemonkey.test.ComplexManipulatorTestSpecs.IntValue;
import static com.navercorp.fixturemonkey.test.ComplexManipulatorTestSpecs.IntegerList;
import static com.navercorp.fixturemonkey.test.ComplexManipulatorTestSpecs.NestedString;
import static com.navercorp.fixturemonkey.test.ComplexManipulatorTestSpecs.SUT;
import static com.navercorp.fixturemonkey.test.ComplexManipulatorTestSpecs.StringAndInt;
import static com.navercorp.fixturemonkey.test.ComplexManipulatorTestSpecs.StringIntegerList;
import static com.navercorp.fixturemonkey.test.ComplexManipulatorTestSpecs.StringValue;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Property;
import net.jqwik.api.RandomGenerator;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.ArbitraryBuilders;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.random.Randoms;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.test.ComplexManipulatorTestSpecs.AcceptIfArbitraryGroup;
import com.navercorp.fixturemonkey.test.ComplexManipulatorTestSpecs.ApplyArbitraryGroup;
import com.navercorp.fixturemonkey.test.ComplexManipulatorTestSpecs.ArbitraryGroup;
import com.navercorp.fixturemonkey.test.ComplexManipulatorTestSpecs.Complex;
import com.navercorp.fixturemonkey.test.ComplexManipulatorTestSpecs.ComplexFlagGroup;
import com.navercorp.fixturemonkey.test.ComplexManipulatorTestSpecs.ComplexFlagValue;
import com.navercorp.fixturemonkey.test.ComplexManipulatorTestSpecs.FixedArbitraryGroup;
import com.navercorp.fixturemonkey.test.ComplexManipulatorTestSpecs.FixedSetArbitraryArbitraryGroup;
import com.navercorp.fixturemonkey.test.ComplexManipulatorTestSpecs.MapValue;
import com.navercorp.fixturemonkey.test.ComplexManipulatorTestSpecs.NestedStringList;
import com.navercorp.fixturemonkey.test.ComplexManipulatorTestSpecs.SetArbitraryAcceptGroup;
import com.navercorp.fixturemonkey.test.ComplexManipulatorTestSpecs.SetArbitraryArbitraryAcceptGroup;
import com.navercorp.fixturemonkey.test.ComplexManipulatorTestSpecs.StringAndIntGroup;
import com.navercorp.fixturemonkey.test.ComplexManipulatorTestSpecs.StringIntegerWrapper;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.IntegerArray;

class ComplexManipulatorTest {
	@Property
	void giveMeAcceptIf() {
		// when
		StringValue actual = SUT.giveMeBuilder(StringValue.class)
			.acceptIf(StringValue::isEmpty, it -> it.set("value", "test"))
			.sample();

		then(actual).satisfiesAnyOf(
			it -> then(it.getValue()).isNotNull(),
			it -> then(it.getValue()).isEqualTo("test")
		);
	}

	@Property
	void giveMeAcceptIfWithNull() {
		// when
		StringIntegerList actual = SUT.giveMeBuilder(StringIntegerList.class)
			.set("value", "test")
			.acceptIf(it -> it.getValue().equals("test"), builder -> builder.setNull("values"))
			.sample();

		then(actual.getValues()).isNull();
	}

	@Property
	void decomposedNullCollectionReturnsNull() {
		// when
		List<Integer> values = SUT.giveMeBuilder(IntegerList.class)
			.setNull("values")
			.map(IntegerList::getValues)
			.sample();

		then(values).isNull();
	}

	@Property
	void giveMeComplexApply() {
		// when
		StringAndInt actual = SUT.giveMeBuilder(StringAndInt.class)
			.setNotNull("value2")
			.apply((it, builder) -> builder.set("value1.value", String.valueOf(it.getValue2().getValue())))
			.sample();

		then(actual.getValue1().getValue()).isEqualTo(String.valueOf(actual.getValue1().getValue()));
	}

	@Property
	void acceptIfSetNull() {
		// given
		ArbitraryBuilder<NestedString> decomposedBuilder = SUT.giveMeBuilder(NestedString.class)
			.set("value.value", Arbitraries.strings())
			.acceptIf(
				s -> true,
				it -> it.setNull("value.value")
			);

		// when
		NestedString actual = decomposedBuilder.sample();

		then(actual.getValue().getValue()).isNull();
	}

	@Property
	void applySetNull() {
		// given
		ArbitraryBuilder<NestedString> decomposedBuilder = SUT.giveMeBuilder(NestedString.class)
			.set("value.value", Arbitraries.strings())
			.apply((value, it) -> it.setNull("value.value"));

		// when
		NestedString actual = decomposedBuilder.sample();

		then(actual.getValue().getValue()).isNull();
	}

	@Property
	void applySetAfterSetNull() {
		// given
		ArbitraryBuilder<NestedString> decomposedBuilder = SUT.giveMeBuilder(NestedString.class)
			.set("value.value", Arbitraries.strings())
			.apply((value, it) -> it.setNull("value.value"))
			.set("value.value", "test");

		// when
		NestedString actual = decomposedBuilder.sample();

		then(actual.getValue().getValue()).isEqualTo("test");
	}

	@Property
	void acceptIfSetAfterSetNull() {
		// given
		ArbitraryBuilder<NestedString> decomposedBuilder = SUT.giveMeBuilder(NestedString.class)
			.set("value.value", Arbitraries.strings())
			.acceptIf(
				s -> true,
				it -> it.setNull("value.value")
			)
			.set("value.value", "test");

		// when
		NestedString actual = decomposedBuilder.sample();

		then(actual.getValue().getValue()).isEqualTo("test");
	}

	@Property
	void giveMeZipList() {
		// given
		List<ArbitraryBuilder<?>> list = new ArrayList<>();
		list.add(SUT.giveMeBuilder(StringValue.class));
		list.add(SUT.giveMeBuilder(IntValue.class));

		// when
		StringAndInt actual = ArbitraryBuilders.zip(
			list,
			(l) -> {
				StringAndInt result = new StringAndInt();
				result.setValue1((StringValue)l.get(0));
				result.setValue2((IntValue)l.get(1));
				return result;
			}
		).sample();

		then(actual.getValue1()).isNotNull();
		then(actual.getValue2()).isNotNull();
	}

	@Property
	void giveMeZipEmptyListThrows() {
		// given
		List<ArbitraryBuilder<?>> list = new ArrayList<>();

		thenThrownBy(
			() -> ArbitraryBuilders.zip(
				list,
				(l) -> new StringAndInt()
			).sample()
		).isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("zip should be used in more than two ArbitraryBuilders, given size");
	}

	@Property
	void zipThree() {
		// given
		ArbitraryBuilder<StringValue> s1 = SUT.giveMeBuilder(StringValue.class)
			.set("value", "s1");
		ArbitraryBuilder<StringValue> s2 = SUT.giveMeBuilder(StringValue.class)
			.set("value", "s2");
		ArbitraryBuilder<StringValue> s3 = SUT.giveMeBuilder(StringValue.class)
			.set("value", "s3");

		// when
		NestedStringList actual = ArbitraryBuilders.zip(s1, s2, s3, (a1, a2, a3) -> {
			List<StringValue> list = new ArrayList<>();
			list.add(a1);
			list.add(a2);
			list.add(a3);

			NestedStringList result = new NestedStringList();
			result.setValues(list);
			return result;
		}).sample();

		then(actual.getValues()).hasSize(3);
		then(actual.getValues().get(0).getValue()).isEqualTo("s1");
		then(actual.getValues().get(1).getValue()).isEqualTo("s2");
		then(actual.getValues().get(2).getValue()).isEqualTo("s3");
	}

	@Property
	void giveMeZipWithThree() {
		// given
		ArbitraryBuilder<StringValue> s1 = SUT.giveMeBuilder(StringValue.class)
			.set("value", "s1");
		ArbitraryBuilder<StringValue> s2 = SUT.giveMeBuilder(StringValue.class)
			.set("value", "s2");
		ArbitraryBuilder<StringValue> s3 = SUT.giveMeBuilder(StringValue.class)
			.set("value", "s3");

		// when
		NestedStringList actual = s1.zipWith(s2, s3, (a1, a2, a3) -> {
			List<StringValue> list = new ArrayList<>();
			list.add(a1);
			list.add(a2);
			list.add(a3);

			NestedStringList result = new NestedStringList();
			result.setValues(list);
			return result;
		}).sample();

		then(actual.getValues()).hasSize(3);
		then(actual.getValues().get(0).getValue()).isEqualTo("s1");
		then(actual.getValues().get(1).getValue()).isEqualTo("s2");
		then(actual.getValues().get(2).getValue()).isEqualTo("s3");
	}

	@Property
	void zipFour() {
		// given
		ArbitraryBuilder<StringValue> s1 = SUT.giveMeBuilder(StringValue.class)
			.set("value", "s1");
		ArbitraryBuilder<StringValue> s2 = SUT.giveMeBuilder(StringValue.class)
			.set("value", "s2");
		ArbitraryBuilder<StringValue> s3 = SUT.giveMeBuilder(StringValue.class)
			.set("value", "s3");
		ArbitraryBuilder<StringValue> s4 = SUT.giveMeBuilder(StringValue.class)
			.set("value", "s4");

		// when
		NestedStringList actual = ArbitraryBuilders.zip(s1, s2, s3, s4, (a1, a2, a3, a4) -> {
			List<StringValue> list = new ArrayList<>();
			list.add(a1);
			list.add(a2);
			list.add(a3);
			list.add(a4);

			NestedStringList result = new NestedStringList();
			result.setValues(list);
			return result;
		}).sample();

		then(actual.getValues()).hasSize(4);
		then(actual.getValues().get(0).getValue()).isEqualTo("s1");
		then(actual.getValues().get(1).getValue()).isEqualTo("s2");
		then(actual.getValues().get(2).getValue()).isEqualTo("s3");
		then(actual.getValues().get(3).getValue()).isEqualTo("s4");
	}

	@Property
	void giveMeZipWithFour() {
		// given
		ArbitraryBuilder<StringValue> s1 = SUT.giveMeBuilder(StringValue.class)
			.set("value", "s1");
		ArbitraryBuilder<StringValue> s2 = SUT.giveMeBuilder(StringValue.class)
			.set("value", "s2");
		ArbitraryBuilder<StringValue> s3 = SUT.giveMeBuilder(StringValue.class)
			.set("value", "s3");
		ArbitraryBuilder<StringValue> s4 = SUT.giveMeBuilder(StringValue.class)
			.set("value", "s4");

		// when
		NestedStringList actual = s1.zipWith(s2, s3, s4, (a1, a2, a3, a4) -> {
			List<StringValue> list = new ArrayList<>();
			list.add(a1);
			list.add(a2);
			list.add(a3);
			list.add(a4);

			NestedStringList result = new NestedStringList();
			result.setValues(list);
			return result;
		}).sample();

		then(actual.getValues()).hasSize(4);
		then(actual.getValues().get(0).getValue()).isEqualTo("s1");
		then(actual.getValues().get(1).getValue()).isEqualTo("s2");
		then(actual.getValues().get(2).getValue()).isEqualTo("s3");
		then(actual.getValues().get(3).getValue()).isEqualTo("s4");
	}

	@Property
	void giveMeZipWith() {
		// given
		ArbitraryBuilder<String> stringArbitraryBuilder = SUT.giveMeBuilder(String.class);

		// when
		String actual = SUT.giveMeBuilder(Integer.class)
			.zipWith(stringArbitraryBuilder, (integer, string) -> integer + "" + string)
			.sample();

		then(actual).isNotNull();
	}

	@Property
	void giveMeZipTwoElement() {
		// given
		ArbitraryBuilder<String> stringArbitraryBuilder = SUT.giveMeBuilder(String.class);
		ArbitraryBuilder<Integer> integerArbitraryBuilder = SUT.giveMeBuilder(Integer.class);

		// when
		String actual = ArbitraryBuilders.zip(
			stringArbitraryBuilder,
			integerArbitraryBuilder,
			(integer, string) -> integer + "" + string
		).sample();

		then(actual).isNotNull();
	}

	@Property
	void giveMeZipReturnsNew() {
		// given
		ArbitraryBuilder<String> stringArbitraryBuilder = SUT.giveMeBuilder(String.class);
		ArbitraryBuilder<Integer> integerArbitraryBuilder = SUT.giveMeBuilder(Integer.class);

		// when
		Arbitrary<String> zippedArbitraryBuilder = ArbitraryBuilders.zip(
			stringArbitraryBuilder,
			integerArbitraryBuilder,
			(integer, string) -> integer + "" + string
		).build();

		// then
		String result1 = zippedArbitraryBuilder.sample();
		String result2 = zippedArbitraryBuilder.sample();
		then(result1).isNotEqualTo(result2);
	}

	@Property
	void giveMeMap() {
		// when
		StringValue actual = SUT.giveMeBuilder(StringValue.class)
			.map(wrapper -> new StringValue("" + wrapper.getValue()))
			.sample();

		then(actual).isNotNull();
	}

	@Property
	void giveMeMapAndSet() {
		// when
		StringValue actual = SUT.giveMeBuilder(StringValue.class)
			.map(wrapper -> new StringValue("" + wrapper.getValue()))
			.set("value", "test")
			.sample();

		then(actual.getValue()).isEqualTo("test");
	}

	@Property
	void giveMeMapAndSetAndMap() {
		// when
		String actual = SUT.giveMeBuilder(IntValue.class)
			.map(wrapper -> new StringValue("" + wrapper.getValue()))
			.set("value", "test")
			.map(StringValue::getValue)
			.sample();

		then(actual).isEqualTo("test");
	}

	@Property
	void applySetWithDefault() {
		// given
		ArbitraryBuilder<StringIntegerList> defaultBuilder = SUT.giveMeBuilder(StringIntegerList.class)
			.set("value", Arbitraries.integers().map(String::valueOf))
			.minSize("values", 1);

		// when
		StringIntegerList actual = defaultBuilder.apply(
			(value, builder) -> builder
				.set("values[" + (value.getValues().size() - 1) + "]", Integer.parseInt(value.getValue()))
		).sample();

		then(actual.getValues().get(actual.getValues().size() - 1)).isEqualTo(Integer.parseInt(actual.getValue()));
	}

	@Property
	void applyWithGroupNotSetAsRegisteredArbitrary() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(ArbitraryGroup.class)
			.build();

		// when
		NestedString actual = sut.giveMeBuilder(NestedString.class)
			.setNotNull("value.value")
			.apply((value, builder) -> builder.set("value.value", "APPLY" + value.getValue().getValue()))
			.sample();

		then(actual.getValue().getValue()).contains("APPLY");
	}

	@Property
	void acceptIfWithGroupNotSetAsRegisteredArbitrary() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(ArbitraryGroup.class)
			.build();

		// when
		NestedString actual = sut.giveMeBuilder(NestedString.class)
			.setNotNull("value.value")
			.acceptIf(
				it -> it.getValue() != null,
				builder -> builder.set("value.value", "ACCEPTIF")
			)
			.sample();

		then(actual.getValue().getValue()).contains("ACCEPTIF");
	}

	@Property
	void registerAcceptIfReturnsDiff() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(AcceptIfArbitraryGroup.class)
			.build();

		// when
		NestedStringList actual = sut.giveMeBuilder(NestedStringList.class)
			.size("values", 10)
			.setNotNull("values[*].value")
			.sample();

		// then
		List<StringValue> uniqueList = actual.getValues().stream()
			.distinct()
			.collect(toList());
		then(uniqueList).hasSizeGreaterThan(1);
	}

	@Property
	void registerApplyReturnsDiff() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(ApplyArbitraryGroup.class)
			.build();

		// when
		NestedStringList actual = sut.giveMeBuilder(NestedStringList.class)
			.size("values", 10)
			.setNotNull("values[*].value")
			.sample();

		// then
		List<StringValue> uniqueList = actual.getValues().stream()
			.distinct()
			.collect(toList());
		then(uniqueList).hasSizeGreaterThan(1);
	}

	@Property
	void applyReturnsDiff() {
		// given
		ArbitraryBuilder<Complex> decomposedArbitraryBuilder = SUT.giveMeBuilder(Complex.class)
			.apply((it, builder) -> builder.set("value1", "FIXED"));

		// when
		Complex actual1 = decomposedArbitraryBuilder.sample();
		Complex actual2 = decomposedArbitraryBuilder.sample();

		then(actual1).isNotEqualTo(actual2);
	}

	@Property
	void acceptIfReturnsDiff() {
		// given
		ArbitraryBuilder<Complex> decomposedArbitraryBuilder = SUT.giveMeBuilder(Complex.class)
			.acceptIf(it -> true, builder -> builder.set("value2", 2));

		// when
		Complex actual1 = decomposedArbitraryBuilder.sample();
		Complex actual2 = decomposedArbitraryBuilder.sample();

		then(actual1).isNotEqualTo(actual2);
	}

	@Property
	void applyTwice() {
		// when
		NestedString actual = SUT.giveMeBuilder(NestedString.class)
			.setNotNull("value")
			.apply((value, builder) -> builder.set("value.value", "APPLY" + value.getValue().getValue()))
			.apply((value, builder) -> {
			})
			.sample();

		then(actual.getValue().getValue()).contains("APPLY");
	}

	@Property
	void fixed() {
		// given
		ArbitraryBuilder<StringValue> arbitraryBuilder = SUT.giveMeBuilder(StringValue.class)
			.fixed();

		// when
		StringValue sampled1 = arbitraryBuilder.sample();
		StringValue sampled2 = arbitraryBuilder.sample();

		then(sampled1).isEqualTo(sampled2);
	}

	@Property
	void fixedSet() {
		// given
		ArbitraryBuilder<StringValue> arbitraryBuilder = SUT.giveMeBuilder(StringValue.class)
			.fixed();

		// when
		StringValue actual = arbitraryBuilder
			.set("value", "set")
			.sample();

		then(actual.getValue()).isEqualTo("set");
	}

	@Property
	void fixedRegister() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(FixedArbitraryGroup.class)
			.build();

		// when
		StringValue actual1 = sut.giveMeOne(StringValue.class);
		StringValue actual2 = sut.giveMeOne(StringValue.class);

		then(actual1).isEqualTo(actual2);
	}

	@Property
	void fixedRegisterList() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(FixedArbitraryGroup.class)
			.build();

		// when
		NestedStringList actual = sut.giveMeOne(NestedStringList.class);

		// then
		List<StringValue> distinct = actual.getValues().stream()
			.distinct()
			.collect(toList());
		then(distinct).hasSizeBetween(0, 1);
	}

	@Property
	void acceptIfWithSetWithRegister() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(FixedSetArbitraryArbitraryGroup.class)
			.build();

		// when
		StringValue actual = sut.giveMeBuilder(StringValue.class)
			.set("value", "test")
			.acceptIf(it -> it.getValue().equals("test"), it -> it.set("value", "value"))
			.sample();

		then(actual.getValue()).isEqualTo("value");
	}

	@Property
	void fixedRegisterAcceptIf() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(ApplyArbitraryGroup.class)
			.build();

		// when
		StringValue actual = sut.giveMeBuilder(StringValue.class)
			.set("value", "test")
			.acceptIf(it -> it.getValue().equals("test"), it -> it.set("value", "value"))
			.fixed()
			.sample();

		then(actual.getValue()).isEqualTo("value");
	}

	@Property
	void fixedWithSetArbitrary() {
		// given
		ArbitraryBuilder<StringValue> arbitraryBuilder = SUT.giveMeBuilder(StringValue.class)
			.set("value", Arbitraries.strings().numeric())
			.fixed();

		// when
		StringValue actual1 = arbitraryBuilder.sample();
		StringValue actual2 = arbitraryBuilder.sample();

		then(actual1).isEqualTo(actual2);
	}

	@Property
	void acceptIfWithSetWithFixedOverride() {
		// when
		StringValue actual = SUT.giveMeBuilder(StringValue.class)
			.acceptIf(it -> true, it -> it.set("value", "value"))
			.set("value", "fixed")
			.fixed()
			.sample();

		then(actual.getValue()).isEqualTo("fixed");
	}

	@Property
	void acceptIfWithFixed() {
		// when
		StringAndInt actual = SUT.giveMeBuilder(StringAndInt.class)
			.acceptIf(it -> true, it -> it.set("value1.value", "value"))
			.fixed()
			.sample();

		then(actual.getValue1().getValue()).isEqualTo("value");
	}

	@Property
	void acceptIfWithSetWithFixed() {
		// when
		StringAndInt actual = SUT.giveMeBuilder(StringAndInt.class)
			.acceptIf(it -> true, it -> it.set("value1.value", "value"))
			.set("value2.value", 5)
			.fixed()
			.sample();

		then(actual.getValue1().getValue()).isEqualTo("value");
		then(actual.getValue2().getValue()).isEqualTo(5);
	}

	@Property
	void registerSetArbitraryApply() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(SetArbitraryArbitraryAcceptGroup.class)
			.build();

		// when
		StringValue actual1 = sut.giveMeOne(StringValue.class);
		StringValue actual2 = sut.giveMeOne(StringValue.class);

		then(actual1).isEqualTo(actual2);
	}

	@Property
	void registerSetApply() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(SetArbitraryAcceptGroup.class)
			.build();

		// when
		StringValue actual1 = sut.giveMeOne(StringValue.class);
		StringValue actual2 = sut.giveMeOne(StringValue.class);

		then(actual1).isEqualTo(actual2);
	}

	@Property
	void nullFixedSize() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.setNull("values")
			.fixed()
			.size("values", 1)
			.sample();

		then(actual.getValues()).hasSize(1);
	}

	@Property
	void nullFixedSet() {
		// when
		NestedString actual = SUT.giveMeBuilder(NestedString.class)
			.setNull("value")
			.fixed()
			.set("value.value", "set")
			.sample();

		then(actual.getValue().getValue()).isEqualTo("set");
	}

	@Property
	void nullFixedSizeZeroReturnsEmpty() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.setNull("values")
			.fixed()
			.size("values", 0)
			.sample();

		then(actual.getValues()).isEmpty();
	}

	@Property
	void zipWithinRegister() {
		// given
		FixtureMonkey fixture = FixtureMonkey.builder()
			.registerGroup(StringAndIntGroup.class)
			.build();
		StringAndInt value = fixture.giveMeOne(StringAndInt.class);

		// when
		StringAndInt actual = fixture.giveMeBuilder(value)
			.sample();

		then(actual.getValue1().getValue()).isNull();
		then(actual.getValue2().getValue()).isEqualTo(-1);
	}

	@Property
	void decomposeMap() {
		// given
		Map<String, Integer> map = new HashMap<>();
		map.put("test", -1);
		MapValue mapValue = SUT.giveMeBuilder(MapValue.class)
			.set("value", map)
			.sample();

		// when
		MapValue actual = SUT.giveMeBuilder(mapValue)
			.sample();

		then(actual.getValue().containsValue(-1)).isTrue();
		then(actual.getValue().containsKey("test")).isTrue();
	}

	@Property
	void buildWithGeneratorReturnsDiff() {
		// given
		Arbitrary<Complex> complex = SUT.giveMeBuilder(Complex.class)
			.apply((it, builder) ->
				builder.set("value2", 1234)
					.set("value1", "test")
			)
			.build();

		RandomGenerator<Complex> generator = complex.generator(1000);

		// when
		Complex actual = generator.next(Randoms.current()).value();

		// then
		Complex notExpected = generator.next(Randoms.current()).value();
		then(actual).isNotEqualTo(notExpected);
	}

	@Property
	void setSizeAndSetEmptyListFixed() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.size("values", 2)
			.set("values", new ArrayList<>())
			.fixed()
			.sample();

		then(actual.getValues()).isEmpty();
	}

	@Property
	void setSizeAndSetEmptyArrayFixed() {
		// when
		IntegerArray actual = SUT.giveMeBuilder(IntegerArray.class)
			.size("values", 2)
			.set("values", new Integer[] {})
			.fixed()
			.sample();

		then(actual.getValues()).isEmpty();
	}

	@Property
	void applyNested() {
		// when
		StringValue actual = SUT.giveMeBuilder(StringValue.class)
			.set("value", "test")
			.apply((outerValue, outerBuilder) ->
				outerBuilder.set("value", "0" + outerValue.getValue())
					.apply((innerValue, innerBuilder) ->
						innerBuilder.set("value", "1" + innerValue.getValue())
					)
			)
			.sample();

		then(actual.getValue()).isEqualTo("10test");
	}

	@Property
	void sizeApplySize() {
		// when
		IntegerList actual = SUT.giveMeBuilder(IntegerList.class)
			.size("values", 2)
			.apply((it, builder) -> {
			})
			.size("values", 1)
			.sample();

		then(actual.getValues()).hasSize(1);
	}

	@Property
	void registerComplexFlag() {
		// given
		FixtureMonkey fixture = FixtureMonkey.builder()
			.registerGroup(ComplexFlagGroup.class)
			.defaultNotNull(true)
			.build();

		// when
		ComplexFlagValue actual = fixture.giveMeOne(ComplexFlagValue.class);

		then(actual).satisfiesAnyOf(
			it -> {
				then(it.flag).isTrue();
				then(it.flagTrueValue).isNotNull();
				then(it.flagFalseValue).isNull();
			},
			it -> {
				then(it.flag).isFalse();
				then(it.flagTrueValue).isNull();
				then(it.flagFalseValue).isNotNull();
			}
		);
	}

	@Property
	void giveMeListTypeApply() {
		Complex actual = FixtureMonkeyTestSpecs.SUT.giveMeBuilder(new TypeReference<List<Complex>>() {
			})
			.size("$", 1)
			.apply((it, builder) -> builder.set("$[0].value1", it.get(0).getValue2() + ""))
			.sample()
			.get(0);

		then(actual.getValue1()).isEqualTo(actual.getValue2() + "");
	}

	@Property
	void giveMeRegisteredListType() {
		FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
			.register(StringValue.class, it -> it.giveMeBuilder(StringValue.class)
				.set("value", "test")
			)
			.build();

		List<StringValue> actual = fixtureMonkey.giveMeOne(new TypeReference<List<StringValue>>() {
		});

		then(actual).allMatch(it -> it.getValue().equals("test"));
	}

	@Property
	void giveMeRegisteredNotGenericTypeWhenTypeReference() {
		FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
			.register(StringValue.class, it -> it.giveMeBuilder(StringValue.class)
				.set("value", "test")
			)
			.build();

		StringValue actual = fixtureMonkey.giveMeOne(new TypeReference<StringValue>() {
		});

		then(actual.getValue()).isEqualTo("test");
	}

	@Property
	void giveMeNestedFieldSizeAndApplySize() {
		StringIntegerWrapper actual = SUT.giveMeBuilder(StringIntegerWrapper.class)
			.size("value.values", 1)
			.apply((it, builder) -> {
			})
			.size("value.values", 2)
			.sample();

		then(actual.getValue().getValues()).hasSize(2);
	}

	@Property
	void setNullFixedReturnsNull() {
		Complex actual = SUT.giveMeBuilder(Complex.class)
			.setNull("$")
			.fixed()
			.sample();

		then(actual).isNull();
	}

	@Property
	void setAllFieldNullFixedReturnsNotNull() {
		Complex actual = SUT.giveMeBuilder(Complex.class)
			.setNull("*")
			.fixed()
			.sample();

		then(actual).isNotNull();
	}
}
