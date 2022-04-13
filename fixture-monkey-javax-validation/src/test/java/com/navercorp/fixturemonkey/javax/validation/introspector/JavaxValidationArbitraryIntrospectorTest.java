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

import java.util.Collections;
import java.util.regex.Pattern;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Property;
import net.jqwik.api.arbitraries.ByteArbitrary;
import net.jqwik.api.arbitraries.CharacterArbitrary;
import net.jqwik.api.arbitraries.IntegerArbitrary;
import net.jqwik.api.arbitraries.ShortArbitrary;
import net.jqwik.api.arbitraries.StringArbitrary;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.property.PropertyCache;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.type.TypeReference;

class JavaxValidationArbitraryIntrospectorTest {
	private final JavaxValidationArbitraryTypeIntrospector sut = new JavaxValidationArbitraryTypeIntrospector();

	@Property
	void strings() {
		// given
		StringArbitrary stringArbitrary = Arbitraries.strings();
		TypeReference<StringIntrospectorSpec> typeReference = new TypeReference<StringIntrospectorSpec>() {
		};
		String propertyName = "str";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
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
		TypeReference<StringIntrospectorSpec> typeReference = new TypeReference<StringIntrospectorSpec>() {
		};
		String propertyName = "notBlank";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
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
		TypeReference<StringIntrospectorSpec> typeReference = new TypeReference<StringIntrospectorSpec>() {
		};
		String propertyName = "notEmpty";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
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
		TypeReference<StringIntrospectorSpec> typeReference = new TypeReference<StringIntrospectorSpec>() {
		};
		String propertyName = "size";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<String> actual = this.sut.strings(stringArbitrary, context);

		// then
		String value = actual.sample();
		then(value.length()).isGreaterThanOrEqualTo(5);
		then(value.length()).isLessThanOrEqualTo(10);
	}

	@Property
	void stringDigits() {
		// given
		StringArbitrary stringArbitrary = Arbitraries.strings();
		TypeReference<StringIntrospectorSpec> typeReference = new TypeReference<StringIntrospectorSpec>() {
		};
		String propertyName = "digits";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<String> actual = this.sut.strings(stringArbitrary, context);

		// then
		String value = actual.sample();
		then(value.length()).isLessThanOrEqualTo(10);
		thenNoException().isThrownBy(() -> Long.parseLong(value));
	}

	@Property
	void stringPattern() {
		// given
		StringArbitrary stringArbitrary = Arbitraries.strings();
		TypeReference<StringIntrospectorSpec> typeReference = new TypeReference<StringIntrospectorSpec>() {
		};
		String propertyName = "pattern";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
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
	void stringEmail() {
		// given
		StringArbitrary stringArbitrary = Arbitraries.strings();
		TypeReference<StringIntrospectorSpec> typeReference = new TypeReference<StringIntrospectorSpec>() {
		};
		String propertyName = "email";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
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
		TypeReference<CharacterIntrospectorSpec> typeReference = new TypeReference<CharacterIntrospectorSpec>() {
		};
		String propertyName = "character";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Character> actual = this.sut.characters(characterArbitrary, context);

		// then
		then(actual).isEqualTo(characterArbitrary);
	}

	@Property
	void shorts() {
		// given
		ShortArbitrary shortArbitrary = Arbitraries.shorts();
		TypeReference<ShortIntrospectorSpec> typeReference = new TypeReference<ShortIntrospectorSpec>() {
		};
		String propertyName = "shortValue";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Short> actual = this.sut.shorts(shortArbitrary, context);

		// then
		short value = actual.sample();
		then(value).isNotNull();
	}

	@Property
	void shortDigitsValue() {
		// given
		ShortArbitrary shortArbitrary = Arbitraries.shorts();
		TypeReference<ShortIntrospectorSpec> typeReference = new TypeReference<ShortIntrospectorSpec>() {
		};
		String propertyName = "digitsValue";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Short> actual = this.sut.shorts(shortArbitrary, context);

		// then
		short value = actual.sample();
		then(value).isBetween((short)-10000, (short)10000);
	}

	@Property
	void shortMinValue() {
		// given
		ShortArbitrary shortArbitrary = Arbitraries.shorts();
		TypeReference<ShortIntrospectorSpec> typeReference = new TypeReference<ShortIntrospectorSpec>() {
		};
		String propertyName = "minValue";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Short> actual = this.sut.shorts(shortArbitrary, context);

		// then
		short value = actual.sample();
		then(value).isGreaterThanOrEqualTo((short)100);
	}

	@Property
	void shortMaxValue() {
		// given
		ShortArbitrary shortArbitrary = Arbitraries.shorts();
		TypeReference<ShortIntrospectorSpec> typeReference = new TypeReference<ShortIntrospectorSpec>() {
		};
		String propertyName = "maxValue";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Short> actual = this.sut.shorts(shortArbitrary, context);

		// then
		short value = actual.sample();
		then(value).isLessThanOrEqualTo((short)100);
	}

	@Property
	void shortDecimalMin() {
		// given
		ShortArbitrary shortArbitrary = Arbitraries.shorts();
		TypeReference<ShortIntrospectorSpec> typeReference = new TypeReference<ShortIntrospectorSpec>() {
		};
		String propertyName = "decimalMin";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Short> actual = this.sut.shorts(shortArbitrary, context);

		// then
		short value = actual.sample();
		then(value).isGreaterThanOrEqualTo((short)100);
	}

	@Property
	void shortDecimalMinExclusive() {
		// given
		ShortArbitrary shortArbitrary = Arbitraries.shorts();
		TypeReference<ShortIntrospectorSpec> typeReference = new TypeReference<ShortIntrospectorSpec>() {
		};
		String propertyName = "decimalMinExclusive";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Short> actual = this.sut.shorts(shortArbitrary, context);

		// then
		short value = actual.sample();
		then(value).isGreaterThanOrEqualTo((short)101);
	}

	@Property
	void shortDecimalMax() {
		// given
		ShortArbitrary shortArbitrary = Arbitraries.shorts();
		TypeReference<ShortIntrospectorSpec> typeReference = new TypeReference<ShortIntrospectorSpec>() {
		};
		String propertyName = "decimalMax";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Short> actual = this.sut.shorts(shortArbitrary, context);

		// then
		short value = actual.sample();
		then(value).isLessThanOrEqualTo((short)100);
	}

	@Property
	void shortDecimalMaxExclusive() {
		// given
		ShortArbitrary shortArbitrary = Arbitraries.shorts();
		TypeReference<ShortIntrospectorSpec> typeReference = new TypeReference<ShortIntrospectorSpec>() {
		};
		String propertyName = "decimalMaxExclusive";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Short> actual = this.sut.shorts(shortArbitrary, context);

		// then
		short value = actual.sample();
		then(value).isLessThanOrEqualTo((short)99);
	}

	@Property
	void shortNegative() {
		// given
		ShortArbitrary shortArbitrary = Arbitraries.shorts();
		TypeReference<ShortIntrospectorSpec> typeReference = new TypeReference<ShortIntrospectorSpec>() {
		};
		String propertyName = "negative";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Short> actual = this.sut.shorts(shortArbitrary, context);

		// then
		short value = actual.sample();
		then(value).isLessThan((short)0);
	}

	@Property
	void shortNegativeOrZero() {
		// given
		ShortArbitrary shortArbitrary = Arbitraries.shorts();
		TypeReference<ShortIntrospectorSpec> typeReference = new TypeReference<ShortIntrospectorSpec>() {
		};
		String propertyName = "negativeOrZero";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Short> actual = this.sut.shorts(shortArbitrary, context);

		// then
		short value = actual.sample();
		then(value).isLessThanOrEqualTo((short)0);
	}

	@Property
	void shortPositive() {
		// given
		ShortArbitrary shortArbitrary = Arbitraries.shorts();
		TypeReference<ShortIntrospectorSpec> typeReference = new TypeReference<ShortIntrospectorSpec>() {
		};
		String propertyName = "positive";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Short> actual = this.sut.shorts(shortArbitrary, context);

		// then
		short value = actual.sample();
		then(value).isGreaterThan((short)0);
	}

	@Property
	void shortPositiveOrZero() {
		// given
		ShortArbitrary shortArbitrary = Arbitraries.shorts();
		TypeReference<ShortIntrospectorSpec> typeReference = new TypeReference<ShortIntrospectorSpec>() {
		};
		String propertyName = "positiveOrZero";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Short> actual = this.sut.shorts(shortArbitrary, context);

		// then
		short value = actual.sample();
		then(value).isGreaterThanOrEqualTo((short)0);
	}

	@Property
	void bytes() {
		// given
		ByteArbitrary byteArbitrary = Arbitraries.bytes();
		TypeReference<ByteIntrospectorSpec> typeReference = new TypeReference<ByteIntrospectorSpec>() {
		};
		String propertyName = "byteValue";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Byte> actual = this.sut.bytes(byteArbitrary, context);

		// then
		byte value = actual.sample();
		then(value).isNotNull();
	}

	@Property
	void byteDigitsValue() {
		// given
		ByteArbitrary byteArbitrary = Arbitraries.bytes();
		TypeReference<ByteIntrospectorSpec> typeReference = new TypeReference<ByteIntrospectorSpec>() {
		};
		String propertyName = "digitsValue";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Byte> actual = this.sut.bytes(byteArbitrary, context);

		// then
		byte value = actual.sample();
		then(value).isBetween((byte)-100, (byte)100);
	}

	@Property
	void byteMinValue() {
		// given
		ByteArbitrary byteArbitrary = Arbitraries.bytes();
		TypeReference<ByteIntrospectorSpec> typeReference = new TypeReference<ByteIntrospectorSpec>() {
		};
		String propertyName = "minValue";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Byte> actual = this.sut.bytes(byteArbitrary, context);

		// then
		byte value = actual.sample();
		then(value).isGreaterThanOrEqualTo((byte)100);
	}

	@Property
	void byteMaxValue() {
		// given
		ByteArbitrary byteArbitrary = Arbitraries.bytes();
		TypeReference<ByteIntrospectorSpec> typeReference = new TypeReference<ByteIntrospectorSpec>() {
		};
		String propertyName = "maxValue";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Byte> actual = this.sut.bytes(byteArbitrary, context);

		// then
		byte value = actual.sample();
		then(value).isLessThanOrEqualTo((byte)100);
	}

	@Property
	void byteDecimalMin() {
		// given
		ByteArbitrary byteArbitrary = Arbitraries.bytes();
		TypeReference<ByteIntrospectorSpec> typeReference = new TypeReference<ByteIntrospectorSpec>() {
		};
		String propertyName = "decimalMin";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Byte> actual = this.sut.bytes(byteArbitrary, context);

		// then
		byte value = actual.sample();
		then(value).isGreaterThanOrEqualTo((byte)100);
	}

	@Property
	void byteDecimalMinExclusive() {
		// given
		ByteArbitrary byteArbitrary = Arbitraries.bytes();
		TypeReference<ByteIntrospectorSpec> typeReference = new TypeReference<ByteIntrospectorSpec>() {
		};
		String propertyName = "decimalMinExclusive";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Byte> actual = this.sut.bytes(byteArbitrary, context);

		// then
		byte value = actual.sample();
		then(value).isGreaterThanOrEqualTo((byte)101);
	}

	@Property
	void byteDecimalMax() {
		// given
		ByteArbitrary byteArbitrary = Arbitraries.bytes();
		TypeReference<ByteIntrospectorSpec> typeReference = new TypeReference<ByteIntrospectorSpec>() {
		};
		String propertyName = "decimalMax";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Byte> actual = this.sut.bytes(byteArbitrary, context);

		// then
		byte value = actual.sample();
		then(value).isLessThanOrEqualTo((byte)100);
	}

	@Property
	void byteDecimalMaxExclusive() {
		// given
		ByteArbitrary byteArbitrary = Arbitraries.bytes();
		TypeReference<ByteIntrospectorSpec> typeReference = new TypeReference<ByteIntrospectorSpec>() {
		};
		String propertyName = "decimalMaxExclusive";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Byte> actual = this.sut.bytes(byteArbitrary, context);

		// then
		byte value = actual.sample();
		then(value).isLessThanOrEqualTo((byte)99);
	}

	@Property
	void byteNegative() {
		// given
		ByteArbitrary byteArbitrary = Arbitraries.bytes();
		TypeReference<ByteIntrospectorSpec> typeReference = new TypeReference<ByteIntrospectorSpec>() {
		};
		String propertyName = "negative";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Byte> actual = this.sut.bytes(byteArbitrary, context);

		// then
		byte value = actual.sample();
		then(value).isLessThan((byte)0);
	}

	@Property
	void byteNegativeOrZero() {
		// given
		ByteArbitrary byteArbitrary = Arbitraries.bytes();
		TypeReference<ByteIntrospectorSpec> typeReference = new TypeReference<ByteIntrospectorSpec>() {
		};
		String propertyName = "negativeOrZero";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Byte> actual = this.sut.bytes(byteArbitrary, context);

		// then
		byte value = actual.sample();
		then(value).isLessThanOrEqualTo((byte)0);
	}

	@Property
	void bytePositive() {
		// given
		ByteArbitrary byteArbitrary = Arbitraries.bytes();
		TypeReference<ByteIntrospectorSpec> typeReference = new TypeReference<ByteIntrospectorSpec>() {
		};
		String propertyName = "positive";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Byte> actual = this.sut.bytes(byteArbitrary, context);

		// then
		byte value = actual.sample();
		then(value).isGreaterThan((byte)0);
	}

	@Property
	void bytePositiveOrZero() {
		// given
		ByteArbitrary byteArbitrary = Arbitraries.bytes();
		TypeReference<ByteIntrospectorSpec> typeReference = new TypeReference<ByteIntrospectorSpec>() {
		};
		String propertyName = "positiveOrZero";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Byte> actual = this.sut.bytes(byteArbitrary, context);

		// then
		byte value = actual.sample();
		then(value).isGreaterThanOrEqualTo((byte)0);
	}

	@Property
	void integers() {
		// given
		IntegerArbitrary integerArbitrary = Arbitraries.integers();
		TypeReference<IntIntrospectorSpec> typeReference = new TypeReference<IntIntrospectorSpec>() {
		};
		String propertyName = "intValue";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Integer> actual = this.sut.integers(integerArbitrary, context);

		// then
		Integer value = actual.sample();
		then(value).isNotNull();
	}

	@Property
	void integersDigitsValue() {
		// given
		IntegerArbitrary integerArbitrary = Arbitraries.integers();
		TypeReference<IntIntrospectorSpec> typeReference = new TypeReference<IntIntrospectorSpec>() {
		};
		String propertyName = "digitsValue";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Integer> actual = this.sut.integers(integerArbitrary, context);

		// then
		Integer value = actual.sample();
		then(value).isBetween(-10000, 10000);
	}

	@Property
	void integersMinValue() {
		// given
		IntegerArbitrary integerArbitrary = Arbitraries.integers();
		TypeReference<IntIntrospectorSpec> typeReference = new TypeReference<IntIntrospectorSpec>() {
		};
		String propertyName = "minValue";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Integer> actual = this.sut.integers(integerArbitrary, context);

		// then
		Integer value = actual.sample();
		then(value).isGreaterThanOrEqualTo(100);
	}

	@Property
	void integersMaxValue() {
		// given
		IntegerArbitrary integerArbitrary = Arbitraries.integers();
		TypeReference<IntIntrospectorSpec> typeReference = new TypeReference<IntIntrospectorSpec>() {
		};
		String propertyName = "maxValue";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Integer> actual = this.sut.integers(integerArbitrary, context);

		// then
		Integer value = actual.sample();
		then(value).isLessThanOrEqualTo(100);
	}

	@Property
	void integersDecimalMin() {
		// given
		IntegerArbitrary integerArbitrary = Arbitraries.integers();
		TypeReference<IntIntrospectorSpec> typeReference = new TypeReference<IntIntrospectorSpec>() {
		};
		String propertyName = "decimalMin";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Integer> actual = this.sut.integers(integerArbitrary, context);

		// then
		Integer value = actual.sample();
		then(value).isGreaterThanOrEqualTo(100);
	}

	@Property
	void integersDecimalMinExclusive() {
		// given
		IntegerArbitrary integerArbitrary = Arbitraries.integers();
		TypeReference<IntIntrospectorSpec> typeReference = new TypeReference<IntIntrospectorSpec>() {
		};
		String propertyName = "decimalMinExclusive";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Integer> actual = this.sut.integers(integerArbitrary, context);

		// then
		Integer value = actual.sample();
		then(value).isGreaterThanOrEqualTo(101);
	}

	@Property
	void integersDecimalMax() {
		// given
		IntegerArbitrary integerArbitrary = Arbitraries.integers();
		TypeReference<IntIntrospectorSpec> typeReference = new TypeReference<IntIntrospectorSpec>() {
		};
		String propertyName = "decimalMax";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Integer> actual = this.sut.integers(integerArbitrary, context);

		// then
		Integer value = actual.sample();
		then(value).isLessThanOrEqualTo(100);
	}

	@Property
	void integersDecimalMaxExclusive() {
		// given
		IntegerArbitrary integerArbitrary = Arbitraries.integers();
		TypeReference<IntIntrospectorSpec> typeReference = new TypeReference<IntIntrospectorSpec>() {
		};
		String propertyName = "decimalMaxExclusive";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Integer> actual = this.sut.integers(integerArbitrary, context);

		// then
		Integer value = actual.sample();
		then(value).isLessThanOrEqualTo(99);
	}

	@Property
	void integersNegative() {
		// given
		IntegerArbitrary integerArbitrary = Arbitraries.integers();
		TypeReference<IntIntrospectorSpec> typeReference = new TypeReference<IntIntrospectorSpec>() {
		};
		String propertyName = "negative";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Integer> actual = this.sut.integers(integerArbitrary, context);

		// then
		Integer value = actual.sample();
		then(value).isLessThan(0);
	}

	@Property
	void integersNegativeOrZero() {
		// given
		IntegerArbitrary integerArbitrary = Arbitraries.integers();
		TypeReference<IntIntrospectorSpec> typeReference = new TypeReference<IntIntrospectorSpec>() {
		};
		String propertyName = "negativeOrZero";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Integer> actual = this.sut.integers(integerArbitrary, context);

		// then
		Integer value = actual.sample();
		then(value).isLessThanOrEqualTo(0);
	}

	@Property
	void integersPositive() {
		// given
		IntegerArbitrary integerArbitrary = Arbitraries.integers();
		TypeReference<IntIntrospectorSpec> typeReference = new TypeReference<IntIntrospectorSpec>() {
		};
		String propertyName = "positive";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Integer> actual = this.sut.integers(integerArbitrary, context);

		// then
		Integer value = actual.sample();
		then(value).isGreaterThan(0);
	}

	@Property
	void integersPositiveOrZero() {
		// given
		IntegerArbitrary integerArbitrary = Arbitraries.integers();
		TypeReference<IntIntrospectorSpec> typeReference = new TypeReference<IntIntrospectorSpec>() {
		};
		String propertyName = "positiveOrZero";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		Arbitrary<Integer> actual = this.sut.integers(integerArbitrary, context);

		// then
		Integer value = actual.sample();
		then(value).isGreaterThanOrEqualTo(0);
	}
}
