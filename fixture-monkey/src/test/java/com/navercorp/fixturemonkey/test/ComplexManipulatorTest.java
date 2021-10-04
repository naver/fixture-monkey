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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Disabled;
import net.jqwik.api.Property;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.ArbitraryBuilders;
import com.navercorp.fixturemonkey.FixtureMonkey;

public class ComplexManipulatorTest {
	private final FixtureMonkey sut = FixtureMonkey.builder()
		.build();

	@Property
	void giveMeAcceptIf() {
		// when
		StringWrapperClassWithPredicate actual = this.sut.giveMeBuilder(StringWrapperClassWithPredicate.class)
			.acceptIf(StringWrapperClassWithPredicate::isEmpty, it -> it.set("value", "test"))
			.sample();

		then(actual).satisfiesAnyOf(
			it -> then(it.value).isNotNull(),
			it -> then(it.value).isEqualTo("test")
		);
	}

	@Property
	void giveMeAcceptIfWithNull() {
		// when
		StringIntegerListWrapperClass actual = this.sut.giveMeBuilder(StringIntegerListWrapperClass.class)
			.set("value", "test")
			.acceptIf(it -> it.value.equals("test"), builder -> builder.setNull("values"))
			.sample();

		then(actual.values).isNull();
	}

	@Property
	void decomposedNullCollectionReturnsNull() {
		// when
		List<Integer> values = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
			.setNull("values")
			.map(it -> it.values)
			.sample();

		then(values).isNull();
	}

	@Property
	void giveMeComplexApply() {
		// when
		StringWrapperIntegerWrapperClass actual = this.sut.giveMeBuilder(StringWrapperIntegerWrapperClass.class)
			.setNotNull("value2")
			.apply((it, builder) -> builder.set("value1.value", String.valueOf(it.value2.value)))
			.sample();

		then(actual.value1.value).isEqualTo(String.valueOf(actual.value2.value));
	}

	@Property
	void acceptIfSetNull() {
		// given
		ArbitraryBuilder<NestedStringClass> decomposedBuilder = this.sut.giveMeBuilder(NestedStringClass.class)
			.set("value.value", Arbitraries.strings())
			.acceptIf(
				s -> true,
				it -> it.setNull("value.value")
			);

		// when
		NestedStringClass actual = decomposedBuilder.sample();

		then(actual.value.value).isNull();
	}

	@Property
	void applySetNull() {
		// given
		ArbitraryBuilder<NestedStringClass> decomposedBuilder = this.sut.giveMeBuilder(NestedStringClass.class)
			.set("value.value", Arbitraries.strings())
			.apply((value, it) -> it.setNull("value.value"));

		// when
		NestedStringClass actual = decomposedBuilder.sample();

		then(actual.value.value).isNull();
	}

	@Property
	void applySetAfterSetNull() {
		// given
		ArbitraryBuilder<NestedStringClass> decomposedBuilder = this.sut.giveMeBuilder(NestedStringClass.class)
			.set("value.value", Arbitraries.strings())
			.apply((value, it) -> it.setNull("value.value"))
			.set("value.value", "test");

		// when
		NestedStringClass actual = decomposedBuilder.sample();

		then(actual.value.value).isEqualTo("test");
	}

	@Property
	void acceptIfSetAfterSetNull() {
		// given
		ArbitraryBuilder<NestedStringClass> decomposedBuilder = this.sut.giveMeBuilder(NestedStringClass.class)
			.set("value.value", Arbitraries.strings())
			.acceptIf(
				s -> true,
				it -> it.setNull("value.value")
			)
			.set("value.value", "test");

		// when
		NestedStringClass actual = decomposedBuilder.sample();

		then(actual.value.value).isEqualTo("test");
	}

	@Property
	void giveMeZipList() {
		// given
		List<ArbitraryBuilder<?>> list = new ArrayList<>();
		list.add(this.sut.giveMeBuilder(StringWrapperClass.class));
		list.add(this.sut.giveMeBuilder(IntegerWrapperClass.class));

		// when
		StringWrapperIntegerWrapperClass actual = ArbitraryBuilders.zip(
			list,
			(l) -> {
				StringWrapperIntegerWrapperClass result = new StringWrapperIntegerWrapperClass();
				result.setValue1((StringWrapperClass)l.get(0));
				result.setValue2((IntegerWrapperClass)l.get(1));
				return result;
			}
		).sample();

		then(actual.value1).isNotNull();
		then(actual.value2).isNotNull();
	}

	@Property
	void giveMeZipEmptyListThrows() {
		// given
		List<ArbitraryBuilder<?>> list = new ArrayList<>();

		thenThrownBy(
			() -> ArbitraryBuilders.zip(
				list,
				(l) -> new StringWrapperIntegerWrapperClass()
			).sample()
		).isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("zip should be used in more than two ArbitraryBuilders, given size");
	}

	@Property
	void zipThree() {
		// given
		ArbitraryBuilder<StringWrapperClass> s1 = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "s1");
		ArbitraryBuilder<StringWrapperClass> s2 = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "s2");
		ArbitraryBuilder<StringWrapperClass> s3 = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "s3");

		// when
		NestedStringListClass actual = ArbitraryBuilders.zip(s1, s2, s3, (a1, a2, a3) -> {
			List<StringWrapperClass> list = new ArrayList<>();
			list.add(a1);
			list.add(a2);
			list.add(a3);

			NestedStringListClass result = new NestedStringListClass();
			result.setValues(list);
			return result;
		}).sample();

		then(actual.values).hasSize(3);
		then(actual.values.get(0).value).isEqualTo("s1");
		then(actual.values.get(1).value).isEqualTo("s2");
		then(actual.values.get(2).value).isEqualTo("s3");
	}

	@Property
	void giveMeZipWithThree() {
		// given
		ArbitraryBuilder<StringWrapperClass> s1 = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "s1");
		ArbitraryBuilder<StringWrapperClass> s2 = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "s2");
		ArbitraryBuilder<StringWrapperClass> s3 = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "s3");

		// when
		NestedStringListClass actual = s1.zipWith(s2, s3, (a1, a2, a3) -> {
			List<StringWrapperClass> list = new ArrayList<>();
			list.add(a1);
			list.add(a2);
			list.add(a3);

			NestedStringListClass result = new NestedStringListClass();
			result.setValues(list);
			return result;
		}).sample();

		then(actual.values).hasSize(3);
		then(actual.values.get(0).value).isEqualTo("s1");
		then(actual.values.get(1).value).isEqualTo("s2");
		then(actual.values.get(2).value).isEqualTo("s3");
	}

	@Property
	void zipFour() {
		// given
		ArbitraryBuilder<StringWrapperClass> s1 = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "s1");
		ArbitraryBuilder<StringWrapperClass> s2 = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "s2");
		ArbitraryBuilder<StringWrapperClass> s3 = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "s3");
		ArbitraryBuilder<StringWrapperClass> s4 = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "s4");

		// when
		NestedStringListClass actual = ArbitraryBuilders.zip(s1, s2, s3, s4, (a1, a2, a3, a4) -> {
			List<StringWrapperClass> list = new ArrayList<>();
			list.add(a1);
			list.add(a2);
			list.add(a3);
			list.add(a4);

			NestedStringListClass result = new NestedStringListClass();
			result.setValues(list);
			return result;
		}).sample();

		then(actual.values).hasSize(4);
		then(actual.values.get(0).value).isEqualTo("s1");
		then(actual.values.get(1).value).isEqualTo("s2");
		then(actual.values.get(2).value).isEqualTo("s3");
		then(actual.values.get(3).value).isEqualTo("s4");
	}

	@Property
	void giveMeZipWithFour() {
		// given
		ArbitraryBuilder<StringWrapperClass> s1 = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "s1");
		ArbitraryBuilder<StringWrapperClass> s2 = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "s2");
		ArbitraryBuilder<StringWrapperClass> s3 = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "s3");
		ArbitraryBuilder<StringWrapperClass> s4 = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "s4");

		// when
		NestedStringListClass actual = s1.zipWith(s2, s3, s4, (a1, a2, a3, a4) -> {
			List<StringWrapperClass> list = new ArrayList<>();
			list.add(a1);
			list.add(a2);
			list.add(a3);
			list.add(a4);

			NestedStringListClass result = new NestedStringListClass();
			result.setValues(list);
			return result;
		}).sample();

		then(actual.values).hasSize(4);
		then(actual.values.get(0).value).isEqualTo("s1");
		then(actual.values.get(1).value).isEqualTo("s2");
		then(actual.values.get(2).value).isEqualTo("s3");
		then(actual.values.get(3).value).isEqualTo("s4");
	}

	@Property
	void giveMeZipWith() {
		// given
		ArbitraryBuilder<String> stringArbitraryBuilder = this.sut.giveMeBuilder(String.class);

		// when
		String actual = this.sut.giveMeBuilder(Integer.class)
			.zipWith(stringArbitraryBuilder, (integer, string) -> integer + "" + string)
			.sample();

		then(actual).isNotNull();
	}

	@Property
	void giveMeZipTwoElement() {
		// given
		ArbitraryBuilder<String> stringArbitraryBuilder = this.sut.giveMeBuilder(String.class);
		ArbitraryBuilder<Integer> integerArbitraryBuilder = this.sut.giveMeBuilder(Integer.class);

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
		ArbitraryBuilder<String> stringArbitraryBuilder = this.sut.giveMeBuilder(String.class);
		ArbitraryBuilder<Integer> integerArbitraryBuilder = this.sut.giveMeBuilder(Integer.class);

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
		StringWrapperClass actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.map(wrapper -> new StringWrapperClass("" + wrapper.value))
			.sample();

		then(actual).isNotNull();
	}

	@Property
	void giveMeMapAndSet() {
		// when
		StringWrapperClass actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.map(wrapper -> new StringWrapperClass("" + wrapper.value))
			.set("value", "test")
			.sample();

		then(actual.getValue()).isEqualTo("test");
	}

	@Property
	void giveMeMapAndSetAndMap() {
		// when
		String actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.map(wrapper -> new StringWrapperClass("" + wrapper.value))
			.set("value", "test")
			.map(StringWrapperClass::getValue)
			.sample();

		then(actual).isEqualTo("test");
	}

	@Property
	void applySetWithDefault() {
		// given
		ArbitraryBuilder<StringIntegerListWrapperClass> defaultBuilder = this.sut.giveMeBuilder(
			StringIntegerListWrapperClass.class)
			.set("value", Arbitraries.integers().map(String::valueOf))
			.minSize("values", 1);

		// when
		StringIntegerListWrapperClass actual = defaultBuilder.apply(
			(value, builder) -> builder.set("values[" + (value.values.size() - 1) + "]", Integer.parseInt(value.value))
		).sample();

		then(actual.values.get(actual.values.size() - 1)).isEqualTo(Integer.parseInt(actual.value));
	}

	@Property
	void applyWithGroupNotSetAsRegisteredArbitrary() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(ArbitraryGroup.class)
			.build();

		// when
		NestedStringClass actual = sut.giveMeBuilder(NestedStringClass.class)
			.setNotNull("value.value")
			.apply((value, builder) -> builder.set("value.value", "APPLY" + value.getValue().getValue()))
			.sample();

		then(actual.value.value).contains("APPLY");
	}

	@Property
	void acceptIfWithGroupNotSetAsRegisteredArbitrary() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(ArbitraryGroup.class)
			.build();

		// when
		NestedStringClass actual = sut.giveMeBuilder(NestedStringClass.class)
			.setNotNull("value.value")
			.acceptIf(
				it -> it.getValue() != null,
				builder -> builder.set("value.value", "ACCEPTIF")
			)
			.sample();

		then(actual.value.value).contains("ACCEPTIF");
	}

	@Disabled("jqwik 1.6.0 버전업으로 인한 테스트 실패")
	@Property
	void registerAcceptIfReturnsDiff() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(AcceptIfArbitraryGroup.class)
			.build();

		// when
		NestedStringListClass actual = sut.giveMeBuilder(NestedStringListClass.class)
			.size("values", 10)
			.setNotNull("values[*].value")
			.sample();

		// then
		List<StringWrapperClass> uniqueList = actual.values.stream().distinct()
			.collect(Collectors.toList());
		then(uniqueList).hasSizeGreaterThan(1);
	}

	@Property
	void registerApplyReturnsDiff() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(ApplyArbitraryGroup.class)
			.build();

		// when
		NestedStringListClass actual = sut.giveMeBuilder(NestedStringListClass.class)
			.size("values", 10)
			.setNotNull("values[*].value")
			.sample();

		// then
		List<StringWrapperClass> uniqueList = actual.values.stream().distinct()
			.collect(Collectors.toList());
		then(uniqueList).hasSizeGreaterThan(1);
	}

	@Disabled("jqwik 1.6.0 버전업으로 인한 테스트 실패")
	@Property
	void applyReturnsDiff() {
		// given
		ArbitraryBuilder<ComplexClass> decomposedArbitraryBuilder =
			this.sut.giveMeBuilder(ComplexClass.class)
				.apply((it, builder) -> builder.set("value1", "FIXED"));

		// when
		ComplexClass actual1 = decomposedArbitraryBuilder.sample();
		ComplexClass actual2 = decomposedArbitraryBuilder.sample();

		then(actual1).isNotEqualTo(actual2);
	}

	@Disabled("jqwik 1.6.0 버전업으로 인한 테스트 실패")
	@Property
	void acceptIfReturnsDiff() {
		// given
		ArbitraryBuilder<ComplexClass> decomposedArbitraryBuilder =
			this.sut.giveMeBuilder(ComplexClass.class)
				.acceptIf(it -> true, builder -> builder.set("value2", 2));

		// when
		ComplexClass actual1 = decomposedArbitraryBuilder.sample();
		ComplexClass actual2 = decomposedArbitraryBuilder.sample();

		then(actual1).isNotEqualTo(actual2);
	}

	@Property
	void applyTwice() {
		// when
		NestedStringClass actual = this.sut.giveMeBuilder(NestedStringClass.class)
			.setNotNull("value")
			.apply((value, builder) -> builder.set("value.value", "APPLY" + value.getValue().getValue()))
			.apply((value, builder) -> {
			})
			.sample();

		then(actual.value.value).contains("APPLY");
	}

	@Property
	void fixed() {
		// given
		ArbitraryBuilder<StringWrapperClass> arbitraryBuilder = this.sut.giveMeBuilder(StringWrapperClass.class)
			.fixed();

		// when
		StringWrapperClass sampled1 = arbitraryBuilder.sample();
		StringWrapperClass sampled2 = arbitraryBuilder.sample();

		then(sampled1).isEqualTo(sampled2);
	}

	@Property
	void fixedSet() {
		// given
		ArbitraryBuilder<StringWrapperClass> arbitraryBuilder = this.sut.giveMeBuilder(StringWrapperClass.class)
			.fixed();

		// when
		StringWrapperClass actual = arbitraryBuilder
			.set("value", "set")
			.sample();

		then(actual.value).isEqualTo("set");
	}

	@Property
	void fixedRegister() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(FixedArbitraryGroup.class)
			.build();

		// when
		StringWrapperClass actual1 = sut.giveMeOne(StringWrapperClass.class);
		StringWrapperClass actual2 = sut.giveMeOne(StringWrapperClass.class);

		then(actual1).isEqualTo(actual2);
	}

	@Property
	void fixedRegisterList() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(FixedArbitraryGroup.class)
			.build();

		// when
		NestedStringListClass actual = sut.giveMeOne(NestedStringListClass.class);

		// then
		List<StringWrapperClass> distinct = actual.values.stream()
			.distinct()
			.collect(Collectors.toList());
		then(distinct).hasSizeBetween(0, 1);
	}

	@Property
	void acceptIfWithSetWithRegister() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(FixedSetArbitraryArbitraryGroup.class)
			.build();

		// when
		StringWrapperClass actual = sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "test")
			.acceptIf(it -> it.value.equals("test"), it -> it.set("value", "value"))
			.sample();

		then(actual.value).isEqualTo("value");
	}

	@Property
	void fixedRegisterAcceptIf() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(ApplyArbitraryGroup.class)
			.build();

		// when
		StringWrapperClass actual = sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", "test")
			.acceptIf(it -> it.value.equals("test"), it -> it.set("value", "value"))
			.fixed()
			.sample();

		then(actual.value).isEqualTo("value");
	}

	@Property
	void fixedWithSetArbitrary() {
		// given
		ArbitraryBuilder<StringWrapperClass> arbitraryBuilder = this.sut.giveMeBuilder(StringWrapperClass.class)
			.set("value", Arbitraries.strings().numeric())
			.fixed();

		// when
		StringWrapperClass actual1 = arbitraryBuilder.sample();
		StringWrapperClass actual2 = arbitraryBuilder.sample();

		then(actual1).isEqualTo(actual2);
	}

	@Property
	void acceptIfWithSetWithFixedOverride() {
		// when
		StringWrapperClass actual = this.sut.giveMeBuilder(StringWrapperClass.class)
			.acceptIf(it -> true, it -> it.set("value", "value"))
			.set("value", "fixed")
			.fixed()
			.sample();

		then(actual.value).isEqualTo("fixed");
	}

	@Property
	void acceptIfWithFixed() {
		// when
		StringWrapperIntegerWrapperClass actual = this.sut.giveMeBuilder(StringWrapperIntegerWrapperClass.class)
			.acceptIf(it -> true, it -> it.set("value1.value", "value"))
			.fixed()
			.sample();

		then(actual.value1.value).isEqualTo("value");
	}

	@Property
	void acceptIfWithSetWithFixed() {
		// when
		StringWrapperIntegerWrapperClass actual = this.sut.giveMeBuilder(StringWrapperIntegerWrapperClass.class)
			.acceptIf(it -> true, it -> it.set("value1.value", "value"))
			.set("value2.value", 5)
			.fixed()
			.sample();

		then(actual.value1.value).isEqualTo("value");
		then(actual.value2.value).isEqualTo(5);
	}

	@Property
	void registerSetArbitraryApply() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(SetArbitraryArbitraryAcceptGroup.class)
			.build();

		// when
		StringWrapperClass actual1 = sut.giveMeOne(StringWrapperClass.class);
		StringWrapperClass actual2 = sut.giveMeOne(StringWrapperClass.class);

		then(actual1).isEqualTo(actual2);
	}

	@Property
	void registerSetApply() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(SetArbitraryAcceptGroup.class)
			.build();

		// when
		StringWrapperClass actual1 = sut.giveMeOne(StringWrapperClass.class);
		StringWrapperClass actual2 = sut.giveMeOne(StringWrapperClass.class);

		then(actual1).isEqualTo(actual2);
	}

	@Property
	void nullFixedSize() {
		// when
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
			.setNull("values")
			.fixed()
			.size("values", 1)
			.sample();

		then(actual.values).hasSize(1);
	}

	@Property
	void nullFixedSet() {
		// when
		NestedStringClass actual = this.sut.giveMeBuilder(NestedStringClass.class)
			.setNull("value")
			.fixed()
			.set("value.value", "set")
			.sample();

		then(actual.value.value).isEqualTo("set");
	}

	@Property
	void nullFixedSizeZeroReturnsEmpty() {
		// when
		IntegerListWrapperClass actual = this.sut.giveMeBuilder(IntegerListWrapperClass.class)
			.setNull("values")
			.fixed()
			.size("values", 0)
			.sample();

		then(actual.values).isEmpty();
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class StringWrapperClass {
		private String value;
	}

	@Data
	public static class IntegerWrapperClass {
		int value;
	}

	@Data
	public static class NestedStringClass {
		StringWrapperClass value;
	}

	@Data
	public static class StringIntegerListWrapperClass {
		String value;
		List<Integer> values;
	}

	@Data
	public static class IntegerListWrapperClass {
		List<Integer> values;
	}

	@Data
	public static class StringWrapperIntegerWrapperClass {
		StringWrapperClass value1;
		IntegerWrapperClass value2;
	}

	@Data
	public static class NestedStringListClass {
		private List<StringWrapperClass> values;
	}

	@Data
	public static class StringWrapperClassWithPredicate {
		private String value;

		public boolean isEmpty() {
			return value == null;
		}
	}

	@Data
	public static class ComplexClass {
		private String value1;
		private int value2;
		private float value3;
		private String value4;
	}

	public static class ArbitraryGroup {
		public ArbitraryBuilder<NestedStringClass> nestedStringWrapper(FixtureMonkey fixtureMonkey) {
			return fixtureMonkey.giveMeBuilder(NestedStringClass.class)
				.set("value.value", "group");
		}
	}

	public static class AcceptIfArbitraryGroup {
		public AcceptIfArbitraryGroup() {
		}

		public ArbitraryBuilder<StringWrapperClass> string(FixtureMonkey fixture) {
			return fixture.giveMeBuilder(StringWrapperClass.class)
				.acceptIf(it -> true, it -> {
				});
		}
	}

	public static class ApplyArbitraryGroup {
		public ApplyArbitraryGroup() {
		}

		public ArbitraryBuilder<StringWrapperClass> string(FixtureMonkey fixture) {
			return fixture.giveMeBuilder(StringWrapperClass.class)
				.apply((it, builder) -> {
				});
		}
	}

	public static class FixedArbitraryGroup {
		public FixedArbitraryGroup() {
		}

		public ArbitraryBuilder<StringWrapperClass> string(FixtureMonkey fixture) {
			return fixture.giveMeBuilder(StringWrapperClass.class)
				.fixed();
		}
	}

	public static class FixedSetArbitraryArbitraryGroup {
		public FixedSetArbitraryArbitraryGroup() {
		}

		public ArbitraryBuilder<StringWrapperClass> string(FixtureMonkey fixture) {
			return fixture.giveMeBuilder(StringWrapperClass.class)
				.set("value", Arbitraries.strings())
				.fixed();
		}
	}

	public static class SetArbitraryArbitraryAcceptGroup {
		public SetArbitraryArbitraryAcceptGroup() {
		}

		public ArbitraryBuilder<StringWrapperClass> string(FixtureMonkey fixture) {
			return fixture.giveMeBuilder(StringWrapperClass.class)
				.set("value", Arbitraries.strings())
				.apply((it, builder) -> builder.set("value", "set"));
		}
	}

	public static class SetArbitraryAcceptGroup {
		public SetArbitraryAcceptGroup() {
		}

		public ArbitraryBuilder<StringWrapperClass> string(FixtureMonkey fixture) {
			return fixture.giveMeBuilder(StringWrapperClass.class)
				.set("value", Arbitraries.strings().sample())
				.apply((it, builder) -> builder.set("value", "set"));
		}
	}
}
