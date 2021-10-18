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

import static com.navercorp.fixturemonkey.test.ArbitraryCustomizeTestSpecs.SUT;
import static org.assertj.core.api.BDDAssertions.then;

import javax.annotation.Nullable;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.domains.Domain;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizer;
import com.navercorp.fixturemonkey.generator.FieldArbitraries;
import com.navercorp.fixturemonkey.test.ArbitraryCustomizeTestSpecs.CustomizerInteger;
import com.navercorp.fixturemonkey.test.ArbitraryCustomizeTestSpecs.IntValue;
import com.navercorp.fixturemonkey.test.ArbitraryCustomizeTestSpecs.StringAndInt;
import com.navercorp.fixturemonkey.test.ArbitraryCustomizeTestSpecs.StringValue;

class ArbitraryCustomizerTest {
	@Property
	@Domain(ArbitraryCustomizeTestSpecs.class)
	void giveMeWithCustomizer(@ForAll CustomizerInteger actual) {
		then(actual.getValue()).isEqualTo(1);
	}

	@Property
	void giveMeCustomize() {
		// when
		IntValue actual = SUT.giveMeBuilder(IntValue.class)
			.customize(IntValue.class, new ArbitraryCustomizer<IntValue>() {
				@Override
				public void customizeFields(Class<IntValue> type, FieldArbitraries fieldArbitraries) {
					fieldArbitraries.replaceArbitrary("value", Arbitraries.just(1));
				}

				@Nullable
				@Override
				public IntValue customizeFixture(@Nullable IntValue object) {
					return object;
				}
			}).sample();

		then(actual.getValue()).isEqualTo(1);
	}

	@Property
	void nestedCustomize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.addCustomizer(StringValue.class, it -> new StringValue("test"))
			.addCustomizer(IntValue.class, it -> {
				IntValue integerWrapperClass = new IntValue();
				integerWrapperClass.setValue(-1);
				return integerWrapperClass;
			})
			.build();

		// when
		StringAndInt actual = sut.giveMeBuilder(StringAndInt.class)
			.setNotNull("value1")
			.setNotNull("value2")
			.sample();

		then(actual.getValue1().getValue()).isEqualTo("test");
		then(actual.getValue2().getValue()).isEqualTo(-1);
	}
}
