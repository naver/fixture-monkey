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

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import net.jqwik.api.domains.DomainContextBase;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;

class ComplexManipulatorTestSpecs extends DomainContextBase {
	public static final FixtureMonkey SUT = FixtureMonkey.create();

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class StringValue {
		private String value;

		public boolean isEmpty() {
			return value == null;
		}
	}

	@Provide
	Arbitrary<StringValue> stringValue() {
		return SUT.giveMeArbitrary(StringValue.class);
	}

	@Data
	public static class StringIntegerList {
		private String value;
		private List<Integer> values;
	}

	@Provide
	Arbitrary<StringIntegerList> stringIntegerList() {
		return SUT.giveMeArbitrary(StringIntegerList.class);
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
	public static class IntValue {
		private int value;
	}

	@Provide
	Arbitrary<IntValue> intValue() {
		return SUT.giveMeArbitrary(IntValue.class);
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

	@Data
	public static class NestedString {
		private StringValue value;
	}

	@Provide
	Arbitrary<NestedString> nestedString() {
		return SUT.giveMeArbitrary(NestedString.class);
	}

	@Data
	public static class NestedStringList {
		private List<StringValue> values;
	}

	@Provide
	Arbitrary<NestedStringList> nestedStringList() {
		return SUT.giveMeArbitrary(NestedStringList.class);
	}

	@Data
	public static class Complex {
		private String value1;
		private int value2;
		private float value3;
		private String value4;
	}

	@Provide
	Arbitrary<Complex> complex() {
		return SUT.giveMeArbitrary(Complex.class);
	}

	public static class ArbitraryGroup {
		public ArbitraryBuilder<NestedString> nestedString(FixtureMonkey fixtureMonkey) {
			return fixtureMonkey.giveMeBuilder(NestedString.class)
				.set("value.value", "group");
		}
	}

	public static class AcceptIfArbitraryGroup {
		public AcceptIfArbitraryGroup() {
		}

		public ArbitraryBuilder<StringValue> string(FixtureMonkey fixture) {
			return fixture.giveMeBuilder(StringValue.class)
				.acceptIf(it -> true, it -> {
				});
		}
	}

	public static class ApplyArbitraryGroup {
		public ApplyArbitraryGroup() {
		}

		public ArbitraryBuilder<StringValue> string(FixtureMonkey fixture) {
			return fixture.giveMeBuilder(StringValue.class)
				.apply((it, builder) -> {
				});
		}
	}

	public static class FixedArbitraryGroup {
		public FixedArbitraryGroup() {
		}

		public ArbitraryBuilder<StringValue> string(FixtureMonkey fixture) {
			return fixture.giveMeBuilder(StringValue.class)
				.fixed();
		}
	}

	public static class FixedSetArbitraryArbitraryGroup {
		public FixedSetArbitraryArbitraryGroup() {
		}

		public ArbitraryBuilder<StringValue> string(FixtureMonkey fixture) {
			return fixture.giveMeBuilder(StringValue.class)
				.set("value", Arbitraries.strings())
				.fixed();
		}
	}

	public static class SetArbitraryArbitraryAcceptGroup {
		public SetArbitraryArbitraryAcceptGroup() {
		}

		public ArbitraryBuilder<StringValue> string(FixtureMonkey fixture) {
			return fixture.giveMeBuilder(StringValue.class)
				.set("value", Arbitraries.strings())
				.apply((it, builder) -> builder.set("value", "set"));
		}
	}

	public static class SetArbitraryAcceptGroup {
		public SetArbitraryAcceptGroup() {
		}

		public ArbitraryBuilder<StringValue> string(FixtureMonkey fixture) {
			return fixture.giveMeBuilder(StringValue.class)
				.set("value", Arbitraries.strings())
				.apply((it, builder) -> builder.set("value", "set"));
		}
	}
}
