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

package com.navercorp.fixturemonkey.datafaker;

import static org.assertj.core.api.BDDAssertions.then;

import net.jqwik.api.Property;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.datafaker.arbitrary.FakerCombinableArbitrary;
import com.navercorp.fixturemonkey.datafaker.plugin.DataFakerPlugin;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;

class FakerCombinableArbitraryTest {
	@Getter
	@Setter
	public static class StringClass {
		private String str;

		@Size(min = 5, max = 10)
		private String strSize;
	}

	@Property
	void combinedReturnFullName() {
		FakerCombinableArbitrary fakerCombinableArbitrary = new FakerCombinableArbitrary();

		String actual = fakerCombinableArbitrary.combined();

		then(actual).matches("([\\w']+\\.?( )?){2,4}");
	}

	@Property
	void rawValueReturnFullName() {
		FakerCombinableArbitrary fakerCombinableArbitrary = new FakerCombinableArbitrary();

		String actual = fakerCombinableArbitrary.rawValue();

		then(actual).matches("([\\w']+\\.?( )?){2,4}");
	}

	@Property
	void fakerCombinableArbitraryIntrospectorOption() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushExactTypeArbitraryIntrospector(
				String.class,
				(context) -> new ArbitraryIntrospectorResult(new FakerCombinableArbitrary())
			)
			.build();

		String actual = sut.giveMeOne(String.class);

		then(actual).matches("([\\w']+\\.?( )?){2,4}");
	}

	@Property
	void fakerCombinableArbitraryFirstNameOption() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushExactTypeArbitraryIntrospector(
				String.class,
				(context) -> new ArbitraryIntrospectorResult(
					new FakerCombinableArbitrary(faker -> faker.name().firstName()))
			)
			.build();

		String actual = sut.giveMeOne(String.class);

		then(actual).matches("\\w+");
	}

	@Property
	void fakerPluginFullNameGeneratorTest() {
		FixtureMonkey sut = FixtureMonkey.builder().plugin(new DataFakerPlugin()).build();

		String actual = sut.giveMeOne(String.class);

		then(actual).matches("([\\w']+\\.?( )?){2,4}");
	}

	@Property
	void fakerPluginFirstNameGeneratorTest() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new DataFakerPlugin(faker -> faker.name().firstName()))
			.build();

		String actual = sut.giveMeOne(String.class);

		then(actual).matches("\\w+");
	}

	@Property
	void fakerStringClassTest() {
		FixtureMonkey sut = FixtureMonkey.builder().plugin(new DataFakerPlugin()).build();

		String actual = sut.giveMeBuilder(StringClass.class).setNotNull("str").sample().getStr();

		then(actual).matches("([\\w']+\\.?( )?){2,4}");
	}

	@Property
	void fakerStringClassSetSizeTest() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JakartaValidationPlugin())
			.plugin(new DataFakerPlugin())
			.defaultNotNull(true)
			.build();

		String actual = sut.giveMeOne(StringClass.class).getStrSize();

		then(actual).matches("([\\w']+\\.?( )?){2,4}");
		then(actual).hasSizeBetween(5, 10);
	}

	@Property
	void fakerStringClassSetSizeTestFirstName() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JakartaValidationPlugin())
			.plugin(new DataFakerPlugin(faker -> faker.name().firstName()))
			.defaultNotNull(true)
			.build();

		String actual = sut.giveMeOne(StringClass.class).getStrSize();

		then(actual).matches("\\w+");
		then(actual).hasSizeBetween(5, 10);
	}
}

