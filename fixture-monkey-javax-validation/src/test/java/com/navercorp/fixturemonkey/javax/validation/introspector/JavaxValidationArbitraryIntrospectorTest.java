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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.regex.Pattern;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Property;
import net.jqwik.api.arbitraries.StringArbitrary;

import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorContext;

class JavaxValidationArbitraryIntrospectorTest {
	private final JavaxValidationArbitraryIntrospector sut = new JavaxValidationArbitraryIntrospector();

	@Property
	void strings() throws NoSuchFieldException {
		// given
		StringArbitrary stringArbitrary = Arbitraries.strings();
		String propertyName = "str";
		Field field = StringIntrospectorSpec.class.getDeclaredField(propertyName);
		ArbitraryIntrospectorContext context = new ArbitraryIntrospectorContext(
			String.class,
			propertyName,
			field.getAnnotatedType(),
			Arrays.asList(field.getAnnotations())
		);

		// when
		Arbitrary<String> actual = this.sut.strings(stringArbitrary, context);

		// then
		String value = actual.sample();
		then(value).isNotNull();
	}

	@Property
	void stringsNotBlank() throws NoSuchFieldException {
		// given
		StringArbitrary stringArbitrary = Arbitraries.strings();
		String propertyName = "notBlank";
		Field field = StringIntrospectorSpec.class.getDeclaredField(propertyName);
		ArbitraryIntrospectorContext context = new ArbitraryIntrospectorContext(
			String.class,
			propertyName,
			field.getAnnotatedType(),
			Arrays.asList(field.getAnnotations())
		);

		// when
		Arbitrary<String> actual = this.sut.strings(stringArbitrary, context);

		// then
		String value = actual.sample();
		then(value).isNotBlank();
	}

	@Property
	void stringsNotEmpty() throws NoSuchFieldException {
		// given
		StringArbitrary stringArbitrary = Arbitraries.strings();
		String propertyName = "notEmpty";
		Field field = StringIntrospectorSpec.class.getDeclaredField(propertyName);
		ArbitraryIntrospectorContext context = new ArbitraryIntrospectorContext(
			String.class,
			propertyName,
			field.getAnnotatedType(),
			Arrays.asList(field.getAnnotations())
		);

		// when
		Arbitrary<String> actual = this.sut.strings(stringArbitrary, context);

		// then
		String value = actual.sample();
		then(value).isNotEmpty();
	}

	@Property
	void stringsSize() throws NoSuchFieldException {
		// given
		StringArbitrary stringArbitrary = Arbitraries.strings();
		String propertyName = "size";
		Field field = StringIntrospectorSpec.class.getDeclaredField(propertyName);
		ArbitraryIntrospectorContext context = new ArbitraryIntrospectorContext(
			String.class,
			propertyName,
			field.getAnnotatedType(),
			Arrays.asList(field.getAnnotations())
		);

		// when
		Arbitrary<String> actual = this.sut.strings(stringArbitrary, context);

		// then
		String value = actual.sample();
		then(value.length()).isGreaterThanOrEqualTo(5);
		then(value.length()).isLessThanOrEqualTo(10);
	}

	@Property
	void digits() throws NoSuchFieldException {
		// given
		StringArbitrary stringArbitrary = Arbitraries.strings();
		String propertyName = "digits";
		Field field = StringIntrospectorSpec.class.getDeclaredField(propertyName);
		ArbitraryIntrospectorContext context = new ArbitraryIntrospectorContext(
			String.class,
			propertyName,
			field.getAnnotatedType(),
			Arrays.asList(field.getAnnotations())
		);

		// when
		Arbitrary<String> actual = this.sut.strings(stringArbitrary, context);

		// then
		String value = actual.sample();
		then(value.length()).isLessThanOrEqualTo(10);
		thenNoException().isThrownBy(() -> Long.parseLong(value));
	}

	@Property
	void pattern() throws NoSuchFieldException {
		// given
		StringArbitrary stringArbitrary = Arbitraries.strings();
		String propertyName = "pattern";
		Field field = StringIntrospectorSpec.class.getDeclaredField(propertyName);
		ArbitraryIntrospectorContext context = new ArbitraryIntrospectorContext(
			String.class,
			propertyName,
			field.getAnnotatedType(),
			Arrays.asList(field.getAnnotations())
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
	void email() throws NoSuchFieldException {
		// given
		StringArbitrary stringArbitrary = Arbitraries.strings();
		String propertyName = "email";
		Field field = StringIntrospectorSpec.class.getDeclaredField(propertyName);
		ArbitraryIntrospectorContext context = new ArbitraryIntrospectorContext(
			String.class,
			propertyName,
			field.getAnnotatedType(),
			Arrays.asList(field.getAnnotations())
		);

		// when
		Arbitrary<String> actual = this.sut.strings(stringArbitrary, context);

		// then
		String value = actual.sample();
		then(value).containsOnlyOnce("@");
	}
}
