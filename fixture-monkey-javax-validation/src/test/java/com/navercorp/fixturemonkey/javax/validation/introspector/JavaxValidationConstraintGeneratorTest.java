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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;

import net.jqwik.api.Example;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.property.PropertyCache;

class JavaxValidationConstraintGeneratorTest {
	private final JavaxValidationConstraintGenerator sut = new JavaxValidationConstraintGenerator();

	@Example
	void generateStringConstraint() {
		// given
		String propertyName = "str";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(StringIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		JavaxValidationStringConstraint actual = this.sut.generateStringConstraint(context);

		// then
		then(actual.getMinSize()).isNull();
		then(actual.getMaxSize()).isNull();
		then(actual.isDigits()).isFalse();
		then(actual.isNotBlank()).isFalse();
	}

	@Example
	void generateStringConstraintNotBlank() {
		// given
		String propertyName = "notBlank";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(StringIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		JavaxValidationStringConstraint actual = this.sut.generateStringConstraint(context);

		// then
		then(actual.getMinSize()).isEqualTo(BigInteger.ONE);
		then(actual.getMaxSize()).isNull();
		then(actual.isDigits()).isFalse();
		then(actual.isNotBlank()).isTrue();
	}

	@Example
	void generateStringConstraintNotEmpty() {
		// given
		String propertyName = "notEmpty";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(StringIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		JavaxValidationStringConstraint actual = this.sut.generateStringConstraint(context);

		// then
		then(actual.getMinSize()).isEqualTo(BigInteger.ONE);
		then(actual.getMaxSize()).isNull();
		then(actual.isDigits()).isFalse();
		then(actual.isNotBlank()).isFalse();
	}

	@Example
	void generateStringConstraintSize() {
		// given
		String propertyName = "size";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(StringIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		JavaxValidationStringConstraint actual = this.sut.generateStringConstraint(context);

		// then
		then(actual.getMinSize()).isEqualTo(BigInteger.valueOf(5));
		then(actual.getMaxSize()).isEqualTo(BigInteger.valueOf(10));
		then(actual.isDigits()).isFalse();
		then(actual.isNotBlank()).isFalse();
	}

	@Example
	void generateStringConstraintDigits() {
		// given
		String propertyName = "digits";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(StringIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		JavaxValidationStringConstraint actual = this.sut.generateStringConstraint(context);

		// then
		then(actual.getMinSize()).isNull();
		then(actual.getMaxSize()).isEqualTo(10);
		then(actual.isDigits()).isTrue();
		then(actual.isNotBlank()).isTrue();
	}

	@Example
	void generateIntegerConstraint() {
		// given
		String propertyName = "shortValue";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(ShortIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		JavaxValidationIntegerConstraint actual = this.sut.generateIntegerConstraint(context);

		// then
		then(actual.getMin()).isNull();
		then(actual.getMax()).isNull();
	}

	@Example
	void generateIntegerConstraintDigits() {
		// given
		String propertyName = "digitsValue";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(ShortIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		JavaxValidationIntegerConstraint actual = this.sut.generateIntegerConstraint(context);

		// then
		then(actual.getMin()).isEqualTo(BigInteger.valueOf(-999));
		then(actual.getMax()).isEqualTo(BigInteger.valueOf(999));
	}

	@Example
	void generateIntegerConstraintMin() {
		// given
		String propertyName = "minValue";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(ShortIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		JavaxValidationIntegerConstraint actual = this.sut.generateIntegerConstraint(context);

		// then
		then(actual.getMin()).isEqualTo(BigInteger.valueOf(100));
		then(actual.getMax()).isNull();
	}

	@Example
	void generateIntegerConstraintMax() {
		// given
		String propertyName = "maxValue";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(ShortIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		JavaxValidationIntegerConstraint actual = this.sut.generateIntegerConstraint(context);

		// then
		then(actual.getMin()).isNull();
		then(actual.getMax()).isEqualTo(BigInteger.valueOf(100));
	}

	@Example
	void generateIntegerConstraintDecimalMin() {
		// given
		String propertyName = "decimalMin";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(ShortIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		JavaxValidationIntegerConstraint actual = this.sut.generateIntegerConstraint(context);

		// then
		then(actual.getMin()).isEqualTo(BigInteger.valueOf(100));
		then(actual.getMax()).isNull();
	}

	@Example
	void generateIntegerConstraintDecimalMinExclusive() {
		// given
		String propertyName = "decimalMinExclusive";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(ShortIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		JavaxValidationIntegerConstraint actual = this.sut.generateIntegerConstraint(context);

		// then
		then(actual.getMin()).isEqualTo(BigInteger.valueOf(101));
		then(actual.getMax()).isNull();
	}

	@Example
	void generateIntegerConstraintDecimalMax() {
		// given
		String propertyName = "decimalMax";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(ShortIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		JavaxValidationIntegerConstraint actual = this.sut.generateIntegerConstraint(context);

		// then
		then(actual.getMin()).isNull();
		then(actual.getMax()).isEqualTo(BigInteger.valueOf(100));
	}

	@Example
	void generateIntegerConstraintDecimalMaxExclusive() {
		// given
		String propertyName = "decimalMaxExclusive";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(ShortIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		JavaxValidationIntegerConstraint actual = this.sut.generateIntegerConstraint(context);

		// then
		then(actual.getMin()).isNull();
		then(actual.getMax()).isEqualTo(BigInteger.valueOf(99));
	}

	@Example
	void generateIntegerConstraintNegative() {
		// given
		String propertyName = "negative";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(ShortIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		JavaxValidationIntegerConstraint actual = this.sut.generateIntegerConstraint(context);

		// then
		then(actual.getMin()).isNull();
		then(actual.getMax()).isEqualTo(BigInteger.valueOf(-1));
	}

	@Example
	void generateIntegerConstraintNegativeOrZero() {
		// given
		String propertyName = "negativeOrZero";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(ShortIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		JavaxValidationIntegerConstraint actual = this.sut.generateIntegerConstraint(context);

		// then
		then(actual.getMin()).isNull();
		then(actual.getMax()).isEqualTo(BigInteger.ZERO);
	}

	@Example
	void generateIntegerConstraintPositive() {
		// given
		String propertyName = "positive";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(ShortIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		JavaxValidationIntegerConstraint actual = this.sut.generateIntegerConstraint(context);

		// then
		then(actual.getMin()).isEqualTo(BigInteger.ONE);
		then(actual.getMax()).isNull();
	}

	@Example
	void generateIntegerConstraintPositiveOrZero() {
		// given
		String propertyName = "positiveOrZero";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(ShortIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		JavaxValidationIntegerConstraint actual = this.sut.generateIntegerConstraint(context);

		// then
		then(actual.getMin()).isEqualTo(BigInteger.ZERO);
		then(actual.getMax()).isNull();
	}

	@Example
	void generateDecimalConstraint() {
		// given
		String propertyName = "doubleValue";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(DoubleIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		JavaxValidationDecimalConstraint actual = this.sut.generateDecimalConstraint(context);

		// then
		then(actual.getMin()).isNull();
		then(actual.getMinInclusive()).isNull();
		then(actual.getMax()).isNull();
		then(actual.getMaxInclusive()).isNull();
	}

	@Example
	void generateDecimalConstraintDigits() {
		// given
		String propertyName = "digitsValue";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(DoubleIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		JavaxValidationDecimalConstraint actual = this.sut.generateDecimalConstraint(context);

		// then
		then(actual.getMin()).isEqualTo(BigDecimal.valueOf(-999));
		then(actual.getMinInclusive()).isTrue();
		then(actual.getMax()).isEqualTo(BigDecimal.valueOf(999));
		then(actual.getMaxInclusive()).isTrue();
	}

	@Example
	void generateDecimalConstraintMin() {
		// given
		String propertyName = "minValue";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(DoubleIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		JavaxValidationDecimalConstraint actual = this.sut.generateDecimalConstraint(context);

		// then
		then(actual.getMin()).isEqualTo(BigDecimal.valueOf(100));
		then(actual.getMinInclusive()).isTrue();
		then(actual.getMax()).isNull();
		then(actual.getMaxInclusive()).isNull();
	}

	@Example
	void generateDecimalConstraintMax() {
		// given
		String propertyName = "maxValue";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(DoubleIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		JavaxValidationDecimalConstraint actual = this.sut.generateDecimalConstraint(context);

		// then
		then(actual.getMin()).isNull();
		then(actual.getMinInclusive()).isNull();
		then(actual.getMax()).isEqualTo(BigDecimal.valueOf(100));
		then(actual.getMaxInclusive()).isTrue();
	}

	@Example
	void generateDecimalConstraintDecimalMin() {
		// given
		String propertyName = "decimalMin";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(DoubleIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		JavaxValidationDecimalConstraint actual = this.sut.generateDecimalConstraint(context);

		// then
		then(actual.getMin()).isEqualTo(BigDecimal.valueOf(100.1));
		then(actual.getMinInclusive()).isTrue();
		then(actual.getMax()).isNull();
		then(actual.getMaxInclusive()).isNull();
	}

	@Example
	void generateDecimalConstraintDecimalMinExclusive() {
		// given
		String propertyName = "decimalMinExclusive";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(DoubleIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		JavaxValidationDecimalConstraint actual = this.sut.generateDecimalConstraint(context);

		// then
		then(actual.getMin()).isEqualTo(BigDecimal.valueOf(100.1));
		then(actual.getMinInclusive()).isFalse();
		then(actual.getMax()).isNull();
		then(actual.getMaxInclusive()).isNull();
	}

	@Example
	void generateDecimalConstraintDecimalMax() {
		// given
		String propertyName = "decimalMax";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(DoubleIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		JavaxValidationDecimalConstraint actual = this.sut.generateDecimalConstraint(context);

		// then
		then(actual.getMin()).isNull();
		then(actual.getMinInclusive()).isNull();
		then(actual.getMax()).isEqualTo(BigDecimal.valueOf(100.1));
		then(actual.getMaxInclusive()).isTrue();
	}

	@Example
	void generateDecimalConstraintDecimalMaxExclusive() {
		// given
		String propertyName = "decimalMaxExclusive";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(DoubleIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		JavaxValidationDecimalConstraint actual = this.sut.generateDecimalConstraint(context);

		// then
		then(actual.getMin()).isNull();
		then(actual.getMinInclusive()).isNull();
		then(actual.getMax()).isEqualTo(BigDecimal.valueOf(100.1));
		then(actual.getMaxInclusive()).isFalse();
	}

	@Example
	void generateDecimalConstraintNegative() {
		// given
		String propertyName = "negative";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(DoubleIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		JavaxValidationDecimalConstraint actual = this.sut.generateDecimalConstraint(context);

		// then
		then(actual.getMin()).isNull();
		then(actual.getMinInclusive()).isNull();
		then(actual.getMax()).isEqualTo(BigDecimal.ZERO);
		then(actual.getMaxInclusive()).isFalse();
	}

	@Example
	void generateDecimalConstraintNegativeOrZero() {
		// given
		String propertyName = "negativeOrZero";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(DoubleIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		JavaxValidationDecimalConstraint actual = this.sut.generateDecimalConstraint(context);

		// then
		then(actual.getMin()).isNull();
		then(actual.getMinInclusive()).isNull();
		then(actual.getMax()).isEqualTo(BigDecimal.ZERO);
		then(actual.getMaxInclusive()).isTrue();
	}

	@Example
	void generateDecimalConstraintPositive() {
		// given
		String propertyName = "positive";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(DoubleIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		JavaxValidationDecimalConstraint actual = this.sut.generateDecimalConstraint(context);

		// then
		then(actual.getMin()).isEqualTo(BigDecimal.ZERO);
		then(actual.getMinInclusive()).isFalse();
		then(actual.getMax()).isNull();
		then(actual.getMaxInclusive()).isNull();
	}

	@Example
	void generateDecimalConstraintPositiveOrZero() {
		// given
		String propertyName = "positiveOrZero";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(DoubleIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		JavaxValidationDecimalConstraint actual = this.sut.generateDecimalConstraint(context);

		// then
		then(actual.getMin()).isEqualTo(BigDecimal.ZERO);
		then(actual.getMinInclusive()).isTrue();
		then(actual.getMax()).isNull();
		then(actual.getMaxInclusive()).isNull();
	}
}
