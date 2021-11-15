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

import java.util.List;
import java.util.regex.Pattern;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.domains.Domain;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.test.AnnotatedArbitraryGeneratorTestSpecs.StringWithDigit;
import com.navercorp.fixturemonkey.test.AnnotatedArbitraryGeneratorTestSpecs.StringWithEmail;
import com.navercorp.fixturemonkey.test.AnnotatedArbitraryGeneratorTestSpecs.StringWithNotBlank;
import com.navercorp.fixturemonkey.test.AnnotatedArbitraryGeneratorTestSpecs.StringWithNotEmpty;
import com.navercorp.fixturemonkey.test.AnnotatedArbitraryGeneratorTestSpecs.StringWithPattern;
import com.navercorp.fixturemonkey.test.AnnotatedArbitraryGeneratorTestSpecs.StringWithSize;

public class AnnotatedArbitraryGeneratorTest {
	@Property
	@Domain(AnnotatedArbitraryGeneratorTestSpecs.class)
	void string(@ForAll String actual) {
		then(actual).isNotNull();
	}

	@Property
	@Domain(AnnotatedArbitraryGeneratorTestSpecs.class)
	void stringWithPattern(@ForAll StringWithPattern actual) {
		then(actual.getValue()).matches(Pattern.compile("\\d"));
	}

	@Property
	@Domain(AnnotatedArbitraryGeneratorTestSpecs.class)
	void stringWithNotBlank(@ForAll StringWithNotBlank actual) {
		then(actual.getValue()).isNotBlank();
	}

	@Property
	@Domain(AnnotatedArbitraryGeneratorTestSpecs.class)
	void stringWithNotEmpty(@ForAll StringWithNotEmpty actual) {
		then(actual.getValue()).isNotEmpty();
	}

	@Property
	@Domain(AnnotatedArbitraryGeneratorTestSpecs.class)
	void stringWithSize(@ForAll StringWithSize actual) {
		then(actual.getValue()).hasSizeBetween(3, 7);
	}

	@Property
	@Domain(AnnotatedArbitraryGeneratorTestSpecs.class)
	void stringWithDigit(@ForAll StringWithDigit actual) {
		then(actual.getValue()).isBetween("00", "99");
	}

	@Property
	@Domain(AnnotatedArbitraryGeneratorTestSpecs.class)
	void stringWithEmail(@ForAll StringWithEmail actual) {
		then(actual.getValue()).contains("@");
	}

	@Property
	void stringWithDefaultArbitrary() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.putDefaultArbitrary(String.class, Arbitraries.of("test"))
			.build();

		List<String> actual = sut.giveMe(String.class, 5);

		then(actual).allMatch(it -> it.equals("test"));
	}
}
