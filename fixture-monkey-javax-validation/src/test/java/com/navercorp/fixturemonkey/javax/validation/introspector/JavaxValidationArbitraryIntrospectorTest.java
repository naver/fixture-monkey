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

package com.navercorp.fixturemonkey.javax.validation.introspector;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenNoException;

import java.util.regex.Pattern;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Property;
import net.jqwik.api.arbitraries.CharacterArbitrary;
import net.jqwik.api.arbitraries.StringArbitrary;

import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorContext;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryTypeIntrospector;
import com.navercorp.fixturemonkey.api.property.PropertyCache;

class JavaxValidationArbitraryIntrospectorTest {
	private final JavaxValidationArbitraryIntrospector sut = new JavaxValidationArbitraryIntrospector();

	@Property
	void strings() {
		// given
		StringArbitrary stringArbitrary = Arbitraries.strings();
		String propertyName = "str";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(StringIntrospectorSpec.class, propertyName).get();
		ArbitraryIntrospectorContext context = new ArbitraryIntrospectorContext(
			property,
			ArbitraryTypeIntrospector.INTROSPECTORS
		);

		// when
		Arbitrary<String> actual = this.sut.strings(stringArbitrary, context);

		// then
		String value = actual.sample();
		then(value).isNotNull();
	}

	@Property
	void stringsNotBlank() {
		// given
		StringArbitrary stringArbitrary = Arbitraries.strings();
		String propertyName = "notBlank";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(StringIntrospectorSpec.class, propertyName).get();
		ArbitraryIntrospectorContext context = new ArbitraryIntrospectorContext(
			property,
			ArbitraryTypeIntrospector.INTROSPECTORS
		);

		// when
		Arbitrary<String> actual = this.sut.strings(stringArbitrary, context);

		// then
		String value = actual.sample();
		then(value).isNotBlank();
	}

	@Property
	void stringsNotEmpty() {
		// given
		StringArbitrary stringArbitrary = Arbitraries.strings();
		String propertyName = "notEmpty";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(StringIntrospectorSpec.class, propertyName).get();
		ArbitraryIntrospectorContext context = new ArbitraryIntrospectorContext(
			property,
			ArbitraryTypeIntrospector.INTROSPECTORS
		);

		// when
		Arbitrary<String> actual = this.sut.strings(stringArbitrary, context);

		// then
		String value = actual.sample();
		then(value).isNotEmpty();
	}

	@Property
	void stringsSize() {
		// given
		StringArbitrary stringArbitrary = Arbitraries.strings();
		String propertyName = "size";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(StringIntrospectorSpec.class, propertyName).get();
		ArbitraryIntrospectorContext context = new ArbitraryIntrospectorContext(
			property,
			ArbitraryTypeIntrospector.INTROSPECTORS
		);

		// when
		Arbitrary<String> actual = this.sut.strings(stringArbitrary, context);

		// then
		String value = actual.sample();
		then(value.length()).isGreaterThanOrEqualTo(5);
		then(value.length()).isLessThanOrEqualTo(10);
	}

	@Property
	void digits() {
		// given
		StringArbitrary stringArbitrary = Arbitraries.strings();
		String propertyName = "digits";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(StringIntrospectorSpec.class, propertyName).get();
		ArbitraryIntrospectorContext context = new ArbitraryIntrospectorContext(
			property,
			ArbitraryTypeIntrospector.INTROSPECTORS
		);

		// when
		Arbitrary<String> actual = this.sut.strings(stringArbitrary, context);

		// then
		String value = actual.sample();
		then(value.length()).isLessThanOrEqualTo(10);
		thenNoException().isThrownBy(() -> Long.parseLong(value));
	}

	@Property
	void pattern() {
		// given
		StringArbitrary stringArbitrary = Arbitraries.strings();
		String propertyName = "pattern";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(StringIntrospectorSpec.class, propertyName).get();
		ArbitraryIntrospectorContext context = new ArbitraryIntrospectorContext(
			property,
			ArbitraryTypeIntrospector.INTROSPECTORS
		);

		// when
		Arbitrary<String> actual = this.sut.strings(stringArbitrary, context);

		// then
		String value = actual.sample();
		Pattern pattern = Pattern.compile("[e-o]");
		then(pattern.asPredicate().test(value)).isTrue();
		for (int i = 0; i < value.length(); i++) {
			char ch = value.charAt(i);
			then(ch).isBetween('e', 'o');
		}
	}

	@Property
	void email() {
		// given
		StringArbitrary stringArbitrary = Arbitraries.strings();
		String propertyName = "email";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(StringIntrospectorSpec.class, propertyName).get();
		ArbitraryIntrospectorContext context = new ArbitraryIntrospectorContext(
			property,
			ArbitraryTypeIntrospector.INTROSPECTORS
		);

		// when
		Arbitrary<String> actual = this.sut.strings(stringArbitrary, context);

		// then
		String value = actual.sample();
		then(value).containsOnlyOnce("@");
	}

	@Property
	void characters() {
		// given
		CharacterArbitrary characterArbitrary = Arbitraries.chars();
		String propertyName = "character";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(CharacterIntrospectorSpec.class, propertyName).get();
		ArbitraryIntrospectorContext context = new ArbitraryIntrospectorContext(
			property,
			ArbitraryTypeIntrospector.INTROSPECTORS
		);

		// when
		Arbitrary<Character> actual = this.sut.characters(characterArbitrary, context);

		// then
		then(actual).isEqualTo(characterArbitrary);
	}
}
