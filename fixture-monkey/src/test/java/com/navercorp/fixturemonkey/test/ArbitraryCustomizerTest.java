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

import javax.annotation.Nullable;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Property;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizer;
import com.navercorp.fixturemonkey.generator.FieldArbitraries;

public class ArbitraryCustomizerTest {
	private final FixtureMonkey sut = FixtureMonkey.builder()
		.addCustomizer(CustomizerIntegerClass.class, new ArbitraryCustomizer<CustomizerIntegerClass>() {
			@Override
			public void customizeFields(Class<CustomizerIntegerClass> type, FieldArbitraries fieldArbitraries) {
				fieldArbitraries.replaceArbitrary("value", Arbitraries.just(1));
			}

			@Override
			public CustomizerIntegerClass customizeFixture(CustomizerIntegerClass object) {
				return object;
			}
		})
		.build();

	@Property
	void giveMeWithCustomizer() {
		// when
		CustomizerIntegerClass actual = this.sut.giveMeBuilder(CustomizerIntegerClass.class).sample();

		then(actual.value).isEqualTo(1);
	}

	@Property
	void giveMeCustomize() {
		// when
		IntegerWrapperClass actual = this.sut.giveMeBuilder(IntegerWrapperClass.class)
			.customize(IntegerWrapperClass.class, new ArbitraryCustomizer<IntegerWrapperClass>() {
				@Override
				public void customizeFields(Class<IntegerWrapperClass> type, FieldArbitraries fieldArbitraries) {
					fieldArbitraries.replaceArbitrary("value", Arbitraries.just(1));
				}

				@Nullable
				@Override
				public IntegerWrapperClass customizeFixture(@Nullable IntegerWrapperClass object) {
					return object;
				}
			}).sample();

		then(actual.value).isEqualTo(1);
	}

	@Property
	void nestedCustomize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.addCustomizer(StringWrapperClass.class, it -> new StringWrapperClass("test"))
			.addCustomizer(IntegerWrapperClass.class, it -> {
				IntegerWrapperClass integerWrapperClass = new IntegerWrapperClass();
				integerWrapperClass.value = -1;
				return integerWrapperClass;
			})
			.build();

		// when
		StringIntegerClass actual = sut.giveMeBuilder(StringIntegerClass.class)
			.setNotNull("value1")
			.setNotNull("value2")
			.sample();

		then(actual.value1.value).isEqualTo("test");
		then(actual.value2.value).isEqualTo(-1);
	}

	@Data
	public static class CustomizerIntegerClass {
		Integer value;
	}

	@Data
	public static class StringIntegerClass {
		StringWrapperClass value1;
		IntegerWrapperClass value2;
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
}
