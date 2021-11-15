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

import javax.validation.constraints.Digits;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import net.jqwik.api.domains.AbstractDomainContextBase;

import lombok.Data;

import com.navercorp.fixturemonkey.FixtureMonkey;

public class AnnotatedArbitraryGeneratorTestSpecs extends AbstractDomainContextBase {
	public static final FixtureMonkey SUT = FixtureMonkey.builder().build();

	public AnnotatedArbitraryGeneratorTestSpecs() {
		registerArbitrary(String.class, string());
		registerArbitrary(StringWithPattern.class, stringWithPattern());
		registerArbitrary(StringWithNotBlank.class, stringWithNotBlank());
		registerArbitrary(StringWithNotEmpty.class, stringWithNotEmpty());
		registerArbitrary(StringWithSize.class, stringWithSize());
		registerArbitrary(StringWithDigit.class, stringWithDigit());
		registerArbitrary(StringWithEmail.class, stringWithEmail());
	}

	@Provide
	Arbitrary<String> string() {
		return SUT.giveMeArbitrary(String.class);
	}

	@Provide
	Arbitrary<StringWithPattern> stringWithPattern() {
		return SUT.giveMeArbitrary(StringWithPattern.class);
	}

	@Provide
	Arbitrary<StringWithNotBlank> stringWithNotBlank() {
		return SUT.giveMeArbitrary(StringWithNotBlank.class);
	}

	@Provide
	Arbitrary<StringWithNotEmpty> stringWithNotEmpty() {
		return SUT.giveMeArbitrary(StringWithNotEmpty.class);
	}

	@Provide
	Arbitrary<StringWithSize> stringWithSize() {
		return SUT.giveMeArbitrary(StringWithSize.class);
	}

	@Provide
	Arbitrary<StringWithDigit> stringWithDigit() {
		return SUT.giveMeArbitrary(StringWithDigit.class);
	}

	@Provide
	Arbitrary<StringWithEmail> stringWithEmail() {
		return SUT.giveMeArbitrary(StringWithEmail.class);
	}

	@Data
	public static class StringWithPattern {
		@Pattern(regexp = "\\d")
		@NotBlank
		private String value;
	}

	@Data
	public static class StringWithNotBlank {
		@NotBlank
		private String value;
	}

	@Data
	public static class StringWithNotEmpty {
		@NotEmpty
		private String value;
	}

	@Data
	public static class StringWithSize {
		@Size(min = 3, max = 7)
		@NotNull
		private String value;
	}

	@Data
	public static class StringWithDigit {
		@Digits(integer = 2, fraction = 0)
		@NotNull
		private String value;
	}

	@Data
	public static class StringWithEmail {
		@Email
		@NotNull
		private String value;
	}
}
