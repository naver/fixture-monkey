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

import java.math.BigInteger;
import java.util.Collections;

import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.property.PropertyCache;

class JavaxValidationConstraintGeneratorTest {
	private final JavaxValidationConstraintGenerator sut = new JavaxValidationConstraintGenerator();

	@Property
	void generateStringConstraint() {
		// given
		String propertyName = "str";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(StringIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		JavaxValidationStringConstraint actual = this.sut.generateStringConstraint(context);

		// then
		then(actual.getMinSize()).isNull();
		then(actual.getMaxSize()).isNull();
		then(actual.isDigits()).isFalse();
		then(actual.isNotBlank()).isFalse();
	}

	@Property
	void generateStringConstraintNotBlank() {
		// given
		String propertyName = "notBlank";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(StringIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		JavaxValidationStringConstraint actual = this.sut.generateStringConstraint(context);

		// then
		then(actual.getMinSize()).isEqualTo(BigInteger.ONE);
		then(actual.getMaxSize()).isNull();
		then(actual.isDigits()).isFalse();
		then(actual.isNotBlank()).isTrue();
	}

	@Property
	void generateStringConstraintNotEmpty() {
		// given
		String propertyName = "notEmpty";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(StringIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		JavaxValidationStringConstraint actual = this.sut.generateStringConstraint(context);

		// then
		then(actual.getMinSize()).isEqualTo(BigInteger.ONE);
		then(actual.getMaxSize()).isNull();
		then(actual.isDigits()).isFalse();
		then(actual.isNotBlank()).isFalse();
	}

	@Property
	void generateStringConstraintSize() {
		// given
		String propertyName = "size";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(StringIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		JavaxValidationStringConstraint actual = this.sut.generateStringConstraint(context);

		// then
		then(actual.getMinSize()).isEqualTo(BigInteger.valueOf(5));
		then(actual.getMaxSize()).isEqualTo(BigInteger.valueOf(10));
		then(actual.isDigits()).isFalse();
		then(actual.isNotBlank()).isFalse();
	}

	@Property
	void generateStringConstraintDigits() {
		// given
		String propertyName = "digits";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(StringIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		JavaxValidationStringConstraint actual = this.sut.generateStringConstraint(context);

		// then
		then(actual.getMinSize()).isNull();
		then(actual.getMaxSize()).isEqualTo(10);
		then(actual.isDigits()).isTrue();
		then(actual.isNotBlank()).isTrue();
	}

	@Property
	void generateIntegerConstraint() {
		// given
		String propertyName = "shortValue";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(ShortIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		JavaxValidationIntegerConstraint actual = this.sut.generateIntegerConstraint(context);

		// then
		then(actual.getMin()).isNull();
		then(actual.getMax()).isNull();
	}

	@Property
	void generateIntegerConstraintDigits() {
		// given
		String propertyName = "digitsValue";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(ShortIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		JavaxValidationIntegerConstraint actual = this.sut.generateIntegerConstraint(context);

		// then
		then(actual.getMin()).isEqualTo(BigInteger.valueOf(-999));
		then(actual.getMax()).isEqualTo(BigInteger.valueOf(999));
	}

	@Property
	void generateIntegerConstraintMin() {
		// given
		String propertyName = "minValue";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(ShortIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		JavaxValidationIntegerConstraint actual = this.sut.generateIntegerConstraint(context);

		// then
		then(actual.getMin()).isEqualTo(BigInteger.valueOf(100));
		then(actual.getMax()).isNull();
	}

	@Property
	void generateIntegerConstraintMax() {
		// given
		String propertyName = "maxValue";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(ShortIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		JavaxValidationIntegerConstraint actual = this.sut.generateIntegerConstraint(context);

		// then
		then(actual.getMin()).isNull();
		then(actual.getMax()).isEqualTo(BigInteger.valueOf(100));
	}

	@Property
	void generateIntegerConstraintDecimalMin() {
		// given
		String propertyName = "decimalMin";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(ShortIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		JavaxValidationIntegerConstraint actual = this.sut.generateIntegerConstraint(context);

		// then
		then(actual.getMin()).isEqualTo(BigInteger.valueOf(100));
		then(actual.getMax()).isNull();
	}

	@Property
	void generateIntegerConstraintDecimalMinExclusive() {
		// given
		String propertyName = "decimalMinExclusive";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(ShortIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		JavaxValidationIntegerConstraint actual = this.sut.generateIntegerConstraint(context);

		// then
		then(actual.getMin()).isEqualTo(BigInteger.valueOf(101));
		then(actual.getMax()).isNull();
	}

	@Property
	void generateIntegerConstraintDecimalMax() {
		// given
		String propertyName = "decimalMax";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(ShortIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		JavaxValidationIntegerConstraint actual = this.sut.generateIntegerConstraint(context);

		// then
		then(actual.getMin()).isNull();
		then(actual.getMax()).isEqualTo(BigInteger.valueOf(100));
	}

	@Property
	void generateIntegerConstraintDecimalMaxExclusive() {
		// given
		String propertyName = "decimalMaxExclusive";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(ShortIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		JavaxValidationIntegerConstraint actual = this.sut.generateIntegerConstraint(context);

		// then
		then(actual.getMin()).isNull();
		then(actual.getMax()).isEqualTo(BigInteger.valueOf(99));
	}

	@Property
	void generateIntegerConstraintNegative() {
		// given
		String propertyName = "negative";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(ShortIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		JavaxValidationIntegerConstraint actual = this.sut.generateIntegerConstraint(context);

		// then
		then(actual.getMin()).isNull();
		then(actual.getMax()).isEqualTo(BigInteger.valueOf(-1));
	}

	@Property
	void generateIntegerConstraintNegativeOrZero() {
		// given
		String propertyName = "negativeOrZero";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(ShortIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		JavaxValidationIntegerConstraint actual = this.sut.generateIntegerConstraint(context);

		// then
		then(actual.getMin()).isNull();
		then(actual.getMax()).isEqualTo(BigInteger.ZERO);
	}

	@Property
	void generateIntegerConstraintPositive() {
		// given
		String propertyName = "positive";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(ShortIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		JavaxValidationIntegerConstraint actual = this.sut.generateIntegerConstraint(context);

		// then
		then(actual.getMin()).isEqualTo(BigInteger.ONE);
		then(actual.getMax()).isNull();
	}

	@Property
	void generateIntegerConstraintPositiveOrZero() {
		// given
		String propertyName = "positiveOrZero";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(ShortIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		JavaxValidationIntegerConstraint actual = this.sut.generateIntegerConstraint(context);

		// then
		then(actual.getMin()).isEqualTo(BigInteger.ZERO);
		then(actual.getMax()).isNull();
	}
}
