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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.regex.Pattern;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Property;
import net.jqwik.api.arbitraries.BigDecimalArbitrary;
import net.jqwik.api.arbitraries.BigIntegerArbitrary;
import net.jqwik.api.arbitraries.ByteArbitrary;
import net.jqwik.api.arbitraries.CharacterArbitrary;
import net.jqwik.api.arbitraries.DoubleArbitrary;
import net.jqwik.api.arbitraries.FloatArbitrary;
import net.jqwik.api.arbitraries.IntegerArbitrary;
import net.jqwik.api.arbitraries.LongArbitrary;
import net.jqwik.api.arbitraries.ShortArbitrary;
import net.jqwik.api.arbitraries.StringArbitrary;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.generator.ObjectProperty;
import com.navercorp.fixturemonkey.api.property.PropertyCache;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.type.TypeReference;

@SuppressWarnings("OptionalGetWithoutIsPresent")
class JavaxValidationJavaArbitraryResolverTest {
	private final JavaxValidationJavaArbitraryResolver sut = new JavaxValidationJavaArbitraryResolver();

	@Property
	void strings() {
		// given
		StringArbitrary stringArbitrary = Arbitraries.strings();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<StringIntrospectorSpec>() {
			},
			"str"
		);

		// when
		String actual = this.sut.strings(stringArbitrary, context).sample();

		// then
		then(actual).isNotNull();
	}

	@Property
	void stringsNotBlank() {
		// given
		StringArbitrary stringArbitrary = Arbitraries.strings();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<StringIntrospectorSpec>() {
			},
			"notBlank"
		);

		// when
		String actual = this.sut.strings(stringArbitrary, context).sample();

		// then
		then(actual).isNotBlank();
	}

	@Property
	void stringsNotEmpty() {
		// given
		StringArbitrary stringArbitrary = Arbitraries.strings();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<StringIntrospectorSpec>() {
			},
			"notEmpty"
		);

		// when
		String actual = this.sut.strings(stringArbitrary, context).sample();

		// then
		then(actual).isNotEmpty();
	}

	@Property
	void stringsSize() {
		// given
		StringArbitrary stringArbitrary = Arbitraries.strings();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<StringIntrospectorSpec>() {
			},
			"size"
		);

		// when
		String actual = this.sut.strings(stringArbitrary, context).sample();

		// then
		then(actual.length()).isGreaterThanOrEqualTo(5);
		then(actual.length()).isLessThanOrEqualTo(10);
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Property
	void stringDigits() {
		// given
		StringArbitrary stringArbitrary = Arbitraries.strings();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<StringIntrospectorSpec>() {
			},
			"digits"
		);

		// when
		String actual = this.sut.strings(stringArbitrary, context).sample();

		// then
		then(actual.length()).isLessThanOrEqualTo(10);
		thenNoException().isThrownBy(() -> Long.parseLong(actual));
	}

	@Property
	void stringPattern() {
		// given
		StringArbitrary stringArbitrary = Arbitraries.strings();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<StringIntrospectorSpec>() {
			},
			"pattern"
		);

		// when
		String actual = this.sut.strings(stringArbitrary, context).sample();

		// then
		Pattern pattern = Pattern.compile("[e-o]");
		then(pattern.asPredicate().test(actual)).isTrue();
		for (int i = 0; i < actual.length(); i++) {
			char ch = actual.charAt(i);
			then(ch).isBetween('e', 'o');
		}
	}

	@Property
	void stringEmail() {
		// given
		StringArbitrary stringArbitrary = Arbitraries.strings();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<StringIntrospectorSpec>() {
			},
			"email"
		);

		// when
		String actual = this.sut.strings(stringArbitrary, context).sample();

		// then
		then(actual).containsOnlyOnce("@");
	}

	@Property
	void characters() {
		// given
		CharacterArbitrary characterArbitrary = Arbitraries.chars();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<CharacterIntrospectorSpec>() {
			},
			"character"
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
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<ShortIntrospectorSpec>() {
			},
			"shortValue"
		);

		// when
		Short actual = this.sut.shorts(shortArbitrary, context).sample();

		// then
		then(actual).isNotNull();
	}

	@Property
	void shortDigitsValue() {
		// given
		ShortArbitrary shortArbitrary = Arbitraries.shorts();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<ShortIntrospectorSpec>() {
			},
			"digitsValue"
		);

		// when
		Short actual = this.sut.shorts(shortArbitrary, context).sample();

		// then
		then(actual).isBetween((short)-10000, (short)10000);
	}

	@Property
	void shortMinValue() {
		// given
		ShortArbitrary shortArbitrary = Arbitraries.shorts();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<ShortIntrospectorSpec>() {
			},
			"minValue"
		);

		// when
		Short actual = this.sut.shorts(shortArbitrary, context).sample();

		// then
		then(actual).isGreaterThanOrEqualTo((short)100);
	}

	@Property
	void shortMaxValue() {
		// given
		ShortArbitrary shortArbitrary = Arbitraries.shorts();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<ShortIntrospectorSpec>() {
			},
			"maxValue"
		);

		// when
		Short actual = this.sut.shorts(shortArbitrary, context).sample();

		// then
		then(actual).isLessThanOrEqualTo((short)100);
	}

	@Property
	void shortDecimalMin() {
		// given
		ShortArbitrary shortArbitrary = Arbitraries.shorts();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<ShortIntrospectorSpec>() {
			},
			"decimalMin"
		);

		// when
		Short actual = this.sut.shorts(shortArbitrary, context).sample();

		// then
		then(actual).isGreaterThanOrEqualTo((short)100);
	}

	@Property
	void shortDecimalMinExclusive() {
		// given
		ShortArbitrary shortArbitrary = Arbitraries.shorts();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<ShortIntrospectorSpec>() {
			},
			"decimalMinExclusive"
		);

		// when
		Short actual = this.sut.shorts(shortArbitrary, context).sample();

		// then
		then(actual).isGreaterThanOrEqualTo((short)101);
	}

	@Property
	void shortDecimalMax() {
		// given
		ShortArbitrary shortArbitrary = Arbitraries.shorts();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<ShortIntrospectorSpec>() {
			},
			"decimalMax"
		);

		// when
		Short actual = this.sut.shorts(shortArbitrary, context).sample();

		// then
		then(actual).isLessThanOrEqualTo((short)100);
	}

	@Property
	void shortDecimalMaxExclusive() {
		// given
		ShortArbitrary shortArbitrary = Arbitraries.shorts();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<ShortIntrospectorSpec>() {
			},
			"decimalMaxExclusive"
		);

		// when
		Short actual = this.sut.shorts(shortArbitrary, context).sample();

		// then
		then(actual).isLessThanOrEqualTo((short)99);
	}

	@Property
	void shortNegative() {
		// given
		ShortArbitrary shortArbitrary = Arbitraries.shorts();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<ShortIntrospectorSpec>() {
			},
			"negative"
		);

		// when
		Short actual = this.sut.shorts(shortArbitrary, context).sample();

		// then
		then(actual).isLessThan((short)0);
	}

	@Property
	void shortNegativeOrZero() {
		// given
		ShortArbitrary shortArbitrary = Arbitraries.shorts();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<ShortIntrospectorSpec>() {
			},
			"negativeOrZero"
		);

		// when
		Short actual = this.sut.shorts(shortArbitrary, context).sample();

		// then
		then(actual).isLessThanOrEqualTo((short)0);
	}

	@Property
	void shortPositive() {
		// given
		ShortArbitrary shortArbitrary = Arbitraries.shorts();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<ShortIntrospectorSpec>() {
			},
			"positive"
		);

		// when
		Short actual = this.sut.shorts(shortArbitrary, context).sample();

		// then
		then(actual).isGreaterThan((short)0);
	}

	@Property
	void shortPositiveOrZero() {
		// given
		ShortArbitrary shortArbitrary = Arbitraries.shorts();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<ShortIntrospectorSpec>() {
			},
			"positiveOrZero"
		);

		// when
		Short actual = this.sut.shorts(shortArbitrary, context).sample();

		// then
		then(actual).isGreaterThanOrEqualTo((short)0);
	}

	@Property
	void bytes() {
		// given
		ByteArbitrary byteArbitrary = Arbitraries.bytes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<ByteIntrospectorSpec>() {
			},
			"byteValue"
		);

		// when
		Byte actual = this.sut.bytes(byteArbitrary, context).sample();

		// then
		then(actual).isNotNull();
	}

	@Property
	void byteDigitsValue() {
		// given
		ByteArbitrary byteArbitrary = Arbitraries.bytes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<ByteIntrospectorSpec>() {
			},
			"digitsValue"
		);

		// when
		Byte actual = this.sut.bytes(byteArbitrary, context).sample();

		// then
		then(actual).isBetween((byte)-100, (byte)100);
	}

	@Property
	void byteMinValue() {
		// given
		ByteArbitrary byteArbitrary = Arbitraries.bytes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<ByteIntrospectorSpec>() {
			},
			"minValue"
		);

		// when
		Byte actual = this.sut.bytes(byteArbitrary, context).sample();

		// then
		then(actual).isGreaterThanOrEqualTo((byte)100);
	}

	@Property
	void byteMaxValue() {
		// given
		ByteArbitrary byteArbitrary = Arbitraries.bytes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<ByteIntrospectorSpec>() {
			},
			"maxValue"
		);

		// when
		Byte actual = this.sut.bytes(byteArbitrary, context).sample();

		// then
		then(actual).isLessThanOrEqualTo((byte)100);
	}

	@Property
	void byteDecimalMin() {
		// given
		ByteArbitrary byteArbitrary = Arbitraries.bytes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<ByteIntrospectorSpec>() {
			},
			"decimalMin"
		);

		// when
		Byte actual = this.sut.bytes(byteArbitrary, context).sample();

		// then
		then(actual).isGreaterThanOrEqualTo((byte)100);
	}

	@Property
	void byteDecimalMinExclusive() {
		// given
		ByteArbitrary byteArbitrary = Arbitraries.bytes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<ByteIntrospectorSpec>() {
			},
			"decimalMinExclusive"
		);

		// when
		Byte actual = this.sut.bytes(byteArbitrary, context).sample();

		// then
		then(actual).isGreaterThanOrEqualTo((byte)101);
	}

	@Property
	void byteDecimalMax() {
		// given
		ByteArbitrary byteArbitrary = Arbitraries.bytes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<ByteIntrospectorSpec>() {
			},
			"decimalMax"
		);

		// when
		Byte actual = this.sut.bytes(byteArbitrary, context).sample();

		// then
		then(actual).isLessThanOrEqualTo((byte)100);
	}

	@Property
	void byteDecimalMaxExclusive() {
		// given
		ByteArbitrary byteArbitrary = Arbitraries.bytes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<ByteIntrospectorSpec>() {
			},
			"decimalMaxExclusive"
		);

		// when
		Byte actual = this.sut.bytes(byteArbitrary, context).sample();

		// then
		then(actual).isLessThanOrEqualTo((byte)99);
	}

	@Property
	void byteNegative() {
		// given
		ByteArbitrary byteArbitrary = Arbitraries.bytes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<ByteIntrospectorSpec>() {
			},
			"negative"
		);

		// when
		Byte actual = this.sut.bytes(byteArbitrary, context).sample();

		// then
		then(actual).isLessThan((byte)0);
	}

	@Property
	void byteNegativeOrZero() {
		// given
		ByteArbitrary byteArbitrary = Arbitraries.bytes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<ByteIntrospectorSpec>() {
			},
			"negativeOrZero"
		);

		// when
		Byte actual = this.sut.bytes(byteArbitrary, context).sample();

		// then
		then(actual).isLessThanOrEqualTo((byte)0);
	}

	@Property
	void bytePositive() {
		// given
		ByteArbitrary byteArbitrary = Arbitraries.bytes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<ByteIntrospectorSpec>() {
			},
			"positive"
		);

		// when
		Byte actual = this.sut.bytes(byteArbitrary, context).sample();

		// then
		then(actual).isGreaterThan((byte)0);
	}

	@Property
	void bytePositiveOrZero() {
		// given
		ByteArbitrary byteArbitrary = Arbitraries.bytes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<ByteIntrospectorSpec>() {
			},
			"positiveOrZero"
		);

		// when
		Byte actual = this.sut.bytes(byteArbitrary, context).sample();

		// then
		then(actual).isGreaterThanOrEqualTo((byte)0);
	}

	@Property
	void floats() {
		// given
		FloatArbitrary floatArbitrary = Arbitraries.floats();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<FloatIntrospectorSpec>() {
			},
			"floatValue"
		);

		// when
		Float actual = this.sut.floats(floatArbitrary, context).sample();

		// then
		then(actual).isNotNull();
	}

	@Property
	void floatsDigitsValue() {
		// given
		FloatArbitrary floatArbitrary = Arbitraries.floats();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<FloatIntrospectorSpec>() {
			},
			"digitsValue"
		);

		// when
		Float actual = this.sut.floats(floatArbitrary, context).sample();

		// then
		then(actual).isBetween(-10000F, 10000F);
	}

	@Property
	void floatsMinValue() {
		// given
		FloatArbitrary floatArbitrary = Arbitraries.floats();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<FloatIntrospectorSpec>() {
			},
			"minValue"
		);

		// when
		Float actual = this.sut.floats(floatArbitrary, context).sample();

		// then
		then(actual).isGreaterThanOrEqualTo(100);
	}

	@Property
	void floatsMaxValue() {
		// given
		FloatArbitrary floatArbitrary = Arbitraries.floats();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<FloatIntrospectorSpec>() {
			},
			"maxValue"
		);

		// when
		Float actual = this.sut.floats(floatArbitrary, context).sample();

		// then
		then(actual).isLessThanOrEqualTo(100);
	}

	@Property
	void floatsDecimalMin() {
		// given
		FloatArbitrary floatArbitrary = Arbitraries.floats();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<FloatIntrospectorSpec>() {
			},
			"decimalMin"
		);

		// when
		Float actual = this.sut.floats(floatArbitrary, context).sample();

		// then
		then(actual).isGreaterThanOrEqualTo(100.1F);
	}

	@Property
	void floatsDecimalMinExclusive() {
		// given
		FloatArbitrary floatArbitrary = Arbitraries.floats();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<FloatIntrospectorSpec>() {
			},
			"decimalMinExclusive"
		);

		// when
		Float actual = this.sut.floats(floatArbitrary, context).sample();

		// then
		then(actual).isGreaterThanOrEqualTo(100.1F);
	}

	@Property
	void floatsDecimalMax() {
		// given
		FloatArbitrary floatArbitrary = Arbitraries.floats();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<FloatIntrospectorSpec>() {
			},
			"decimalMax"
		);

		// when
		Float actual = this.sut.floats(floatArbitrary, context).sample();

		// then
		then(actual).isLessThanOrEqualTo(100.1F);
	}

	@Property
	void floatsDecimalMaxExclusive() {
		// given
		FloatArbitrary floatArbitrary = Arbitraries.floats();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<FloatIntrospectorSpec>() {
			},
			"decimalMaxExclusive"
		);

		// when
		Float actual = this.sut.floats(floatArbitrary, context).sample();

		// then
		then(actual).isLessThanOrEqualTo(100.1F);
	}

	@Property
	void floatsNegative() {
		// given
		FloatArbitrary floatArbitrary = Arbitraries.floats();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<FloatIntrospectorSpec>() {
			},
			"negative"
		);

		// when
		Float actual = this.sut.floats(floatArbitrary, context).sample();

		// then
		then(actual).isLessThan(0);
	}

	@Property
	void floatsNegativeOrZero() {
		// given
		FloatArbitrary floatArbitrary = Arbitraries.floats();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<FloatIntrospectorSpec>() {
			},
			"negativeOrZero"
		);

		// when
		Float actual = this.sut.floats(floatArbitrary, context).sample();

		// then
		then(actual).isLessThanOrEqualTo(0);
	}

	@Property
	void floatsPositive() {
		// given
		FloatArbitrary floatArbitrary = Arbitraries.floats();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<FloatIntrospectorSpec>() {
			},
			"positive"
		);

		// when
		Float actual = this.sut.floats(floatArbitrary, context).sample();

		// then
		then(actual).isGreaterThan(0);
	}

	@Property
	void floatsPositiveOrZero() {
		// given
		FloatArbitrary floatArbitrary = Arbitraries.floats();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<FloatIntrospectorSpec>() {
			},
			"positiveOrZero"
		);

		// when
		Float actual = this.sut.floats(floatArbitrary, context).sample();

		// then
		then(actual).isGreaterThanOrEqualTo(0);
	}

	@Property
	void doubles() {
		// given
		DoubleArbitrary doubleArbitrary = Arbitraries.doubles();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<DoubleIntrospectorSpec>() {
			},
			"doubleValue"
		);

		// when
		Double actual = this.sut.doubles(doubleArbitrary, context).sample();

		// then
		then(actual).isNotNull();
	}

	@Property
	void doublesDigitsValue() {
		// given
		DoubleArbitrary doubleArbitrary = Arbitraries.doubles();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<DoubleIntrospectorSpec>() {
			},
			"digitsValue"
		);

		// when
		Double actual = this.sut.doubles(doubleArbitrary, context).sample();

		// then
		then(actual).isBetween(-10000D, 10000D);
	}

	@Property
	void doublesMinValue() {
		// given
		DoubleArbitrary doubleArbitrary = Arbitraries.doubles();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<DoubleIntrospectorSpec>() {
			},
			"minValue"
		);

		// when
		Double actual = this.sut.doubles(doubleArbitrary, context).sample();

		// then
		then(actual).isGreaterThanOrEqualTo(100);
	}

	@Property
	void doublesMaxValue() {
		// given
		DoubleArbitrary doubleArbitrary = Arbitraries.doubles();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<DoubleIntrospectorSpec>() {
			},
			"maxValue"
		);

		// when
		Double actual = this.sut.doubles(doubleArbitrary, context).sample();

		// then
		then(actual).isLessThanOrEqualTo(100);
	}

	@Property
	void doublesDecimalMin() {
		// given
		DoubleArbitrary doubleArbitrary = Arbitraries.doubles();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<DoubleIntrospectorSpec>() {
			},
			"decimalMin"
		);

		// when
		Double actual = this.sut.doubles(doubleArbitrary, context).sample();

		// then
		then(actual).isGreaterThanOrEqualTo(100.1D);
	}

	@Property
	void doublesDecimalMinExclusive() {
		// given
		DoubleArbitrary doubleArbitrary = Arbitraries.doubles();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<DoubleIntrospectorSpec>() {
			},
			"decimalMinExclusive"
		);

		// when
		Double actual = this.sut.doubles(doubleArbitrary, context).sample();

		// then
		then(actual).isGreaterThanOrEqualTo(100.1D);
	}

	@Property
	void doublesDecimalMax() {
		// given
		DoubleArbitrary doubleArbitrary = Arbitraries.doubles();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<DoubleIntrospectorSpec>() {
			},
			"decimalMax"
		);

		// when
		Double actual = this.sut.doubles(doubleArbitrary, context).sample();

		// then
		then(actual).isLessThanOrEqualTo(100.1D);
	}

	@Property
	void doublesDecimalMaxExclusive() {
		// given
		DoubleArbitrary doubleArbitrary = Arbitraries.doubles();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<DoubleIntrospectorSpec>() {
			},
			"decimalMaxExclusive"
		);

		// when
		Double actual = this.sut.doubles(doubleArbitrary, context).sample();

		// then
		then(actual).isLessThanOrEqualTo(100.1D);
	}

	@Property
	void doublesNegative() {
		// given
		DoubleArbitrary doubleArbitrary = Arbitraries.doubles();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<DoubleIntrospectorSpec>() {
			},
			"negative"
		);

		// when
		Double actual = this.sut.doubles(doubleArbitrary, context).sample();

		// then
		then(actual).isLessThan(0);
	}

	@Property
	void doublesNegativeOrZero() {
		// given
		DoubleArbitrary doubleArbitrary = Arbitraries.doubles();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<DoubleIntrospectorSpec>() {
			},
			"negativeOrZero"
		);

		// when
		Double actual = this.sut.doubles(doubleArbitrary, context).sample();

		// then
		then(actual).isLessThanOrEqualTo(0);
	}

	@Property
	void doublesPositive() {
		// given
		DoubleArbitrary doubleArbitrary = Arbitraries.doubles();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<DoubleIntrospectorSpec>() {
			},
			"positive"
		);

		// when
		Double actual = this.sut.doubles(doubleArbitrary, context).sample();

		// then
		then(actual).isGreaterThan(0);
	}

	@Property
	void doublesPositiveOrZero() {
		// given
		DoubleArbitrary doubleArbitrary = Arbitraries.doubles();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<DoubleIntrospectorSpec>() {
			},
			"positiveOrZero"
		);

		// when
		Double actual = this.sut.doubles(doubleArbitrary, context).sample();

		// then
		then(actual).isGreaterThanOrEqualTo(0);
	}

	@Property
	void integers() {
		// given
		IntegerArbitrary integerArbitrary = Arbitraries.integers();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<IntIntrospectorSpec>() {
			},
			"intValue"
		);

		// when
		Integer actual = this.sut.integers(integerArbitrary, context).sample();

		// then
		then(actual).isNotNull();
	}

	@Property
	void integersDigitsValue() {
		// given
		IntegerArbitrary integerArbitrary = Arbitraries.integers();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<IntIntrospectorSpec>() {
			},
			"digitsValue"
		);

		// when
		Integer actual = this.sut.integers(integerArbitrary, context).sample();

		// then
		then(actual).isBetween(-10000, 10000);
	}

	@Property
	void integersMinValue() {
		// given
		IntegerArbitrary integerArbitrary = Arbitraries.integers();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<IntIntrospectorSpec>() {
			},
			"minValue"
		);

		// when
		Integer actual = this.sut.integers(integerArbitrary, context).sample();

		// then
		then(actual).isGreaterThanOrEqualTo(100);
	}

	@Property
	void integersMaxValue() {
		// given
		IntegerArbitrary integerArbitrary = Arbitraries.integers();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<IntIntrospectorSpec>() {
			},
			"maxValue"
		);

		// when
		Integer actual = this.sut.integers(integerArbitrary, context).sample();

		// then
		then(actual).isLessThanOrEqualTo(100);
	}

	@Property
	void integersDecimalMin() {
		// given
		IntegerArbitrary integerArbitrary = Arbitraries.integers();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<IntIntrospectorSpec>() {
			},
			"decimalMin"
		);

		// when
		Integer actual = this.sut.integers(integerArbitrary, context).sample();

		// then
		then(actual).isGreaterThanOrEqualTo(100);
	}

	@Property
	void integersDecimalMinExclusive() {
		// given
		IntegerArbitrary integerArbitrary = Arbitraries.integers();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<IntIntrospectorSpec>() {
			},
			"decimalMinExclusive"
		);

		// when
		Integer actual = this.sut.integers(integerArbitrary, context).sample();

		// then
		then(actual).isGreaterThanOrEqualTo(101);
	}

	@Property
	void integersDecimalMax() {
		// given
		IntegerArbitrary integerArbitrary = Arbitraries.integers();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<IntIntrospectorSpec>() {
			},
			"decimalMax"
		);

		// when
		Integer actual = this.sut.integers(integerArbitrary, context).sample();

		// then
		then(actual).isLessThanOrEqualTo(100);
	}

	@Property
	void integersDecimalMaxExclusive() {
		// given
		IntegerArbitrary integerArbitrary = Arbitraries.integers();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<IntIntrospectorSpec>() {
			},
			"decimalMaxExclusive"
		);

		// when
		Integer actual = this.sut.integers(integerArbitrary, context).sample();

		// then
		then(actual).isLessThanOrEqualTo(99);
	}

	@Property
	void integersNegative() {
		// given
		IntegerArbitrary integerArbitrary = Arbitraries.integers();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<IntIntrospectorSpec>() {
			},
			"negative"
		);

		// when
		Integer actual = this.sut.integers(integerArbitrary, context).sample();

		// then
		then(actual).isLessThan(0);
	}

	@Property
	void integersNegativeOrZero() {
		// given
		IntegerArbitrary integerArbitrary = Arbitraries.integers();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<IntIntrospectorSpec>() {
			},
			"negativeOrZero"
		);

		// when
		Integer actual = this.sut.integers(integerArbitrary, context).sample();

		// then
		then(actual).isLessThanOrEqualTo(0);
	}

	@Property
	void integersPositive() {
		// given
		IntegerArbitrary integerArbitrary = Arbitraries.integers();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<IntIntrospectorSpec>() {
			},
			"positive"
		);

		// when
		Integer actual = this.sut.integers(integerArbitrary, context).sample();

		// then
		then(actual).isGreaterThan(0);
	}

	@Property
	void integersPositiveOrZero() {
		// given
		IntegerArbitrary integerArbitrary = Arbitraries.integers();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<IntIntrospectorSpec>() {
			},
			"positiveOrZero"
		);

		// when
		Integer actual = this.sut.integers(integerArbitrary, context).sample();

		// then
		then(actual).isGreaterThanOrEqualTo(0);
	}

	@Property
	void longs() {
		// given
		LongArbitrary longArbitrary = Arbitraries.longs();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<LongIntrospectorSpec>() {
			},
			"longValue"
		);

		// when
		Long actual = this.sut.longs(longArbitrary, context).sample();

		// then
		then(actual).isNotNull();
	}

	@Property
	void longsDigitsValue() {
		// given
		LongArbitrary longArbitrary = Arbitraries.longs();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<LongIntrospectorSpec>() {
			},
			"digitsValue"
		);

		// when
		Long actual = this.sut.longs(longArbitrary, context).sample();

		// then
		then(actual).isBetween(-10000L, 10000L);
	}

	@Property
	void longsMinValue() {
		// given
		LongArbitrary longArbitrary = Arbitraries.longs();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<LongIntrospectorSpec>() {
			},
			"minValue"
		);

		// when
		Long actual = this.sut.longs(longArbitrary, context).sample();

		// then
		then(actual).isGreaterThanOrEqualTo(100);
	}

	@Property
	void longsMaxValue() {
		// given
		LongArbitrary longArbitrary = Arbitraries.longs();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<LongIntrospectorSpec>() {
			},
			"maxValue"
		);

		// when
		Long actual = this.sut.longs(longArbitrary, context).sample();

		// then
		then(actual).isLessThanOrEqualTo(100);
	}

	@Property
	void longsDecimalMin() {
		// given
		LongArbitrary longArbitrary = Arbitraries.longs();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<LongIntrospectorSpec>() {
			},
			"decimalMin"
		);

		// when
		Long actual = this.sut.longs(longArbitrary, context).sample();

		// then
		then(actual).isGreaterThanOrEqualTo(100);
	}

	@Property
	void longsDecimalMinExclusive() {
		// given
		LongArbitrary longArbitrary = Arbitraries.longs();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<LongIntrospectorSpec>() {
			},
			"decimalMinExclusive"
		);

		// when
		Long actual = this.sut.longs(longArbitrary, context).sample();

		// then
		then(actual).isGreaterThanOrEqualTo(101);
	}

	@Property
	void longsDecimalMax() {
		// given
		LongArbitrary longArbitrary = Arbitraries.longs();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<LongIntrospectorSpec>() {
			},
			"decimalMax"
		);

		// when
		Long actual = this.sut.longs(longArbitrary, context).sample();

		// then
		then(actual).isLessThanOrEqualTo(100);
	}

	@Property
	void longsDecimalMaxExclusive() {
		// given
		LongArbitrary longArbitrary = Arbitraries.longs();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<LongIntrospectorSpec>() {
			},
			"decimalMaxExclusive"
		);

		// when
		Long actual = this.sut.longs(longArbitrary, context).sample();

		// then
		then(actual).isLessThanOrEqualTo(99);
	}

	@Property
	void longsNegative() {
		// given
		LongArbitrary longArbitrary = Arbitraries.longs();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<LongIntrospectorSpec>() {
			},
			"negative"
		);

		// when
		Long actual = this.sut.longs(longArbitrary, context).sample();

		// then
		then(actual).isLessThan(0);
	}

	@Property
	void longsNegativeOrZero() {
		// given
		LongArbitrary longArbitrary = Arbitraries.longs();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<LongIntrospectorSpec>() {
			},
			"negativeOrZero"
		);

		// when
		Long actual = this.sut.longs(longArbitrary, context).sample();

		// then
		then(actual).isLessThanOrEqualTo(0);
	}

	@Property
	void longsPositive() {
		// given
		LongArbitrary longArbitrary = Arbitraries.longs();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<LongIntrospectorSpec>() {
			},
			"positive"
		);

		// when
		Long actual = this.sut.longs(longArbitrary, context).sample();

		// then
		then(actual).isGreaterThan(0);
	}

	@Property
	void longsPositiveOrZero() {
		// given
		LongArbitrary longArbitrary = Arbitraries.longs();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<LongIntrospectorSpec>() {
			},
			"positiveOrZero"
		);

		// when
		Long actual = this.sut.longs(longArbitrary, context).sample();

		// then
		then(actual).isGreaterThanOrEqualTo(0);
	}

	@Property
	void bigIntegers() {
		// given
		BigIntegerArbitrary bigIntegerArbitrary = Arbitraries.bigIntegers();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<BigIntegerIntrospectorSpec>() {
			},
			"bigIntegerValue"
		);

		// when
		BigInteger actual = this.sut.bigIntegers(bigIntegerArbitrary, context).sample();

		// then
		then(actual).isNotNull();
	}

	@Property
	void bigIntegersDigitsValue() {
		// given
		BigIntegerArbitrary bigIntegerArbitrary = Arbitraries.bigIntegers();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<BigIntegerIntrospectorSpec>() {
			},
			"digitsValue"
		);

		// when
		BigInteger actual = this.sut.bigIntegers(bigIntegerArbitrary, context).sample();

		// then
		then(actual).isBetween(BigInteger.valueOf(-10000), BigInteger.valueOf(10000));
	}

	@Property
	void bigIntegersMinValue() {
		// given
		BigIntegerArbitrary bigIntegerArbitrary = Arbitraries.bigIntegers();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<BigIntegerIntrospectorSpec>() {
			},
			"minValue"
		);

		// when
		BigInteger actual = this.sut.bigIntegers(bigIntegerArbitrary, context).sample();

		// then
		then(actual).isGreaterThanOrEqualTo(BigInteger.valueOf(100));
	}

	@Property
	void bigIntegersMaxValue() {
		// given
		BigIntegerArbitrary bigIntegerArbitrary = Arbitraries.bigIntegers();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<BigIntegerIntrospectorSpec>() {
			},
			"maxValue"
		);

		// when
		BigInteger actual = this.sut.bigIntegers(bigIntegerArbitrary, context).sample();

		// then
		then(actual).isLessThanOrEqualTo(BigInteger.valueOf(100));
	}

	@Property
	void bigIntegersDecimalMin() {
		// given
		BigIntegerArbitrary bigIntegerArbitrary = Arbitraries.bigIntegers();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<BigIntegerIntrospectorSpec>() {
			},
			"decimalMin"
		);

		// when
		BigInteger actual = this.sut.bigIntegers(bigIntegerArbitrary, context).sample();

		// then
		then(actual).isGreaterThanOrEqualTo(BigInteger.valueOf(100));
	}

	@Property
	void bigIntegersDecimalMinExclusive() {
		// given
		BigIntegerArbitrary bigIntegerArbitrary = Arbitraries.bigIntegers();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<BigIntegerIntrospectorSpec>() {
			},
			"decimalMinExclusive"
		);

		// when
		BigInteger actual = this.sut.bigIntegers(bigIntegerArbitrary, context).sample();

		// then
		then(actual).isGreaterThanOrEqualTo(BigInteger.valueOf(101));
	}

	@Property
	void bigIntegersDecimalMax() {
		// given
		BigIntegerArbitrary bigIntegerArbitrary = Arbitraries.bigIntegers();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<BigIntegerIntrospectorSpec>() {
			},
			"decimalMax"
		);

		// when
		BigInteger actual = this.sut.bigIntegers(bigIntegerArbitrary, context).sample();

		// then
		then(actual).isLessThanOrEqualTo(BigInteger.valueOf(100));
	}

	@Property
	void bigIntegersDecimalMaxExclusive() {
		// given
		BigIntegerArbitrary bigIntegerArbitrary = Arbitraries.bigIntegers();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<BigIntegerIntrospectorSpec>() {
			},
			"decimalMaxExclusive"
		);

		// when
		BigInteger actual = this.sut.bigIntegers(bigIntegerArbitrary, context).sample();

		// then
		then(actual).isLessThanOrEqualTo(BigInteger.valueOf(99));
	}

	@Property
	void bigIntegersNegative() {
		// given
		BigIntegerArbitrary bigIntegerArbitrary = Arbitraries.bigIntegers();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<BigIntegerIntrospectorSpec>() {
			},
			"negative"
		);

		// when
		BigInteger actual = this.sut.bigIntegers(bigIntegerArbitrary, context).sample();

		// then
		then(actual).isLessThan(BigInteger.valueOf(0));
	}

	@Property
	void bigIntegersNegativeOrZero() {
		// given
		BigIntegerArbitrary bigIntegerArbitrary = Arbitraries.bigIntegers();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<BigIntegerIntrospectorSpec>() {
			},
			"negativeOrZero"
		);

		// when
		BigInteger actual = this.sut.bigIntegers(bigIntegerArbitrary, context).sample();

		// then
		then(actual).isLessThanOrEqualTo(BigInteger.valueOf(0));
	}

	@Property
	void bigIntegersPositive() {
		// given
		BigIntegerArbitrary bigIntegerArbitrary = Arbitraries.bigIntegers();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<BigIntegerIntrospectorSpec>() {
			},
			"positive"
		);

		// when
		BigInteger actual = this.sut.bigIntegers(bigIntegerArbitrary, context).sample();

		// then
		then(actual).isGreaterThan(BigInteger.valueOf(0));
	}

	@Property
	void bigIntegersPositiveOrZero() {
		// given
		BigIntegerArbitrary bigIntegerArbitrary = Arbitraries.bigIntegers();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<BigIntegerIntrospectorSpec>() {
			},
			"positiveOrZero"
		);

		// when
		BigInteger actual = this.sut.bigIntegers(bigIntegerArbitrary, context).sample();

		// then
		then(actual).isGreaterThanOrEqualTo(BigInteger.valueOf(0));
	}

	@Property
	void bigDecimals() {
		// given
		BigDecimalArbitrary bigDecimalArbitrary = Arbitraries.bigDecimals();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<BigDecimalIntrospectorSpec>() {
			},
			"bigDecimalValue"
		);

		// when
		BigDecimal actual = this.sut.bigDecimals(bigDecimalArbitrary, context).sample();

		// then
		then(actual).isNotNull();
	}

	@Property
	void bigDecimalsDigitsValue() {
		// given
		BigDecimalArbitrary bigDecimalArbitrary = Arbitraries.bigDecimals();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<BigDecimalIntrospectorSpec>() {
			},
			"digitsValue"
		);

		// when
		BigDecimal actual = this.sut.bigDecimals(bigDecimalArbitrary, context).sample();

		// then
		then(actual).isBetween(BigDecimal.valueOf(-10000), BigDecimal.valueOf(10000));
	}

	@Property
	void bigDecimalsMinValue() {
		// given
		BigDecimalArbitrary bigDecimalArbitrary = Arbitraries.bigDecimals();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<BigDecimalIntrospectorSpec>() {
			},
			"minValue"
		);

		// when
		BigDecimal actual = this.sut.bigDecimals(bigDecimalArbitrary, context).sample();

		// then
		then(actual).isGreaterThanOrEqualTo(BigDecimal.valueOf(100));
	}

	@Property
	void bigDecimalsMaxValue() {
		// given
		BigDecimalArbitrary bigDecimalArbitrary = Arbitraries.bigDecimals();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<BigDecimalIntrospectorSpec>() {
			},
			"maxValue"
		);

		// when
		BigDecimal actual = this.sut.bigDecimals(bigDecimalArbitrary, context).sample();

		// then
		then(actual).isLessThanOrEqualTo(BigDecimal.valueOf(100));
	}

	@Property
	void bigDecimalsDecimalMin() {
		// given
		BigDecimalArbitrary bigDecimalArbitrary = Arbitraries.bigDecimals();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<BigDecimalIntrospectorSpec>() {
			},
			"decimalMin"
		);

		// when
		BigDecimal actual = this.sut.bigDecimals(bigDecimalArbitrary, context).sample();

		// then
		then(actual).isGreaterThanOrEqualTo(BigDecimal.valueOf(100.1));
	}

	@Property
	void bigDecimalsDecimalMinExclusive() {
		// given
		BigDecimalArbitrary bigDecimalArbitrary = Arbitraries.bigDecimals();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<BigDecimalIntrospectorSpec>() {
			},
			"decimalMinExclusive"
		);

		// when
		BigDecimal actual = this.sut.bigDecimals(bigDecimalArbitrary, context).sample();

		// then
		then(actual).isGreaterThanOrEqualTo(BigDecimal.valueOf(100.1));
	}

	@Property
	void bigDecimalsDecimalMax() {
		// given
		BigDecimalArbitrary bigDecimalArbitrary = Arbitraries.bigDecimals();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<BigDecimalIntrospectorSpec>() {
			},
			"decimalMax"
		);

		// when
		BigDecimal actual = this.sut.bigDecimals(bigDecimalArbitrary, context).sample();

		// then
		then(actual).isLessThanOrEqualTo(BigDecimal.valueOf(100.1));
	}

	@Property
	void bigDecimalsDecimalMaxExclusive() {
		// given
		BigDecimalArbitrary bigDecimalArbitrary = Arbitraries.bigDecimals();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<BigDecimalIntrospectorSpec>() {
			},
			"decimalMaxExclusive"
		);

		// when
		BigDecimal actual = this.sut.bigDecimals(bigDecimalArbitrary, context).sample();

		// then
		then(actual).isLessThanOrEqualTo(BigDecimal.valueOf(100.1));
	}

	@Property
	void bigDecimalsNegative() {
		// given
		BigDecimalArbitrary bigDecimalArbitrary = Arbitraries.bigDecimals();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<BigDecimalIntrospectorSpec>() {
			},
			"negative"
		);

		// when
		BigDecimal actual = this.sut.bigDecimals(bigDecimalArbitrary, context).sample();

		// then
		then(actual).isLessThan(BigDecimal.valueOf(0));
	}

	@Property
	void bigDecimalsNegativeOrZero() {
		// given
		BigDecimalArbitrary bigDecimalArbitrary = Arbitraries.bigDecimals();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<BigDecimalIntrospectorSpec>() {
			},
			"negativeOrZero"
		);

		// when
		BigDecimal actual = this.sut.bigDecimals(bigDecimalArbitrary, context).sample();

		// then
		then(actual).isLessThanOrEqualTo(BigDecimal.valueOf(0));
	}

	@Property
	void bigDecimalsPositive() {
		// given
		BigDecimalArbitrary bigDecimalArbitrary = Arbitraries.bigDecimals();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<BigDecimalIntrospectorSpec>() {
			},
			"positive"
		);

		// when
		BigDecimal actual = this.sut.bigDecimals(bigDecimalArbitrary, context).sample();

		// then
		then(actual).isGreaterThan(BigDecimal.valueOf(0));
	}

	@Property
	void bigDecimalsPositiveOrZero() {
		// given
		BigDecimalArbitrary bigDecimalArbitrary = Arbitraries.bigDecimals();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<BigDecimalIntrospectorSpec>() {
			},
			"positiveOrZero"
		);

		// when
		BigDecimal actual = this.sut.bigDecimals(bigDecimalArbitrary, context).sample();

		// then
		then(actual).isGreaterThanOrEqualTo(BigDecimal.valueOf(0));
	}

	private <T> ArbitraryGeneratorContext makeContext(TypeReference<T> typeReference, String propertyName) {
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();

		return new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				new ObjectProperty(
					property,
					PropertyNameResolver.IDENTITY,
					0.0D,
					null,
					Collections.emptyList()
				),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null),
			Collections.emptyList()
		);
	}

}
