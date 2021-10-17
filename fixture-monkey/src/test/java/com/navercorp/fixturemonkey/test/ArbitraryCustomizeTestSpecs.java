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

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import net.jqwik.api.domains.AbstractDomainContextBase;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizer;
import com.navercorp.fixturemonkey.generator.FieldArbitraries;

class ArbitraryCustomizeTestSpecs extends AbstractDomainContextBase {
	public static final FixtureMonkey SUT = FixtureMonkey.builder()
		.addCustomizer(CustomizerInteger.class, new ArbitraryCustomizer<CustomizerInteger>() {
			@Override
			public void customizeFields(Class<CustomizerInteger> type, FieldArbitraries fieldArbitraries) {
				fieldArbitraries.replaceArbitrary("value", Arbitraries.just(1));
			}

			@Override
			public CustomizerInteger customizeFixture(CustomizerInteger object) {
				return object;
			}
		})
		.build();

	ArbitraryCustomizeTestSpecs() {
		registerArbitrary(CustomizerInteger.class, customizerInteger());
		registerArbitrary(StringAndInt.class, stringAndInt());
		registerArbitrary(StringValue.class, stringValue());
		registerArbitrary(IntValue.class, intValue());
	}

	@Data
	public static class CustomizerInteger {
		private Integer value;
	}

	@Provide
	Arbitrary<CustomizerInteger> customizerInteger() {
		return SUT.giveMeArbitrary(CustomizerInteger.class);
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
	@NoArgsConstructor
	@AllArgsConstructor
	public static class StringValue {
		private String value;
	}

	@Provide
	Arbitrary<StringValue> stringValue() {
		return SUT.giveMeArbitrary(StringValue.class);
	}

	@Data
	public static class IntValue {
		private int value;
	}

	@Provide
	Arbitrary<IntValue> intValue() {
		return SUT.giveMeArbitrary(IntValue.class);
	}
}
