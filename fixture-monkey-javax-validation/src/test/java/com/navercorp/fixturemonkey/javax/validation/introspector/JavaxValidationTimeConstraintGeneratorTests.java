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

import java.time.LocalDate;
import java.time.LocalDateTime;

import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorContext;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryTypeIntrospector;
import com.navercorp.fixturemonkey.api.property.PropertyCache;

class JavaxValidationTimeConstraintGeneratorTests {
	private final JavaxValidationTimeConstraintGenerator sut = new JavaxValidationTimeConstraintGenerator();

	@Property
	void generateDateTimeConstraint() {
		// given
		String propertyName = "instant";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryIntrospectorContext context = new ArbitraryIntrospectorContext(
			property,
			ArbitraryTypeIntrospector.INTROSPECTORS
		);
		LocalDateTime baseDateTime = LocalDateTime.now();

		// when
		JavaxValidationDateTimeConstraint actual = this.sut.generateDateTimeConstraint(baseDateTime, context);

		// then
		then(actual.getMin()).isNull();
		then(actual.getMax()).isNull();
	}

	@Property
	void generateDateTimeConstraintPast() {
		// given
		String propertyName = "instantPast";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryIntrospectorContext context = new ArbitraryIntrospectorContext(
			property,
			ArbitraryTypeIntrospector.INTROSPECTORS
		);
		LocalDateTime baseDateTime = LocalDateTime.now();

		// when
		JavaxValidationDateTimeConstraint actual = this.sut.generateDateTimeConstraint(baseDateTime, context);

		// then
		LocalDateTime now = LocalDateTime.now();
		then(actual.getMax()).isBefore(now);
		then(actual.getMin()).isNull();
	}

	@Property
	void generateDateTimeConstraintPastOrPresent() {
		// given
		String propertyName = "instantPastOrPresent";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryIntrospectorContext context = new ArbitraryIntrospectorContext(
			property,
			ArbitraryTypeIntrospector.INTROSPECTORS
		);
		LocalDateTime baseDateTime = LocalDateTime.now();

		// when
		JavaxValidationDateTimeConstraint actual = this.sut.generateDateTimeConstraint(baseDateTime, context);

		// then
		LocalDateTime now = LocalDateTime.now();
		then(actual.getMax()).isBeforeOrEqualTo(now);
		then(actual.getMin()).isNull();

	}

	@Property
	void generateDateTimeConstraintFuture() {
		// given
		String propertyName = "instantFuture";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryIntrospectorContext context = new ArbitraryIntrospectorContext(
			property,
			ArbitraryTypeIntrospector.INTROSPECTORS
		);
		LocalDateTime baseDateTime = LocalDateTime.now();

		// when
		JavaxValidationDateTimeConstraint actual = this.sut.generateDateTimeConstraint(baseDateTime, context);

		// then
		LocalDateTime now = LocalDateTime.now();
		then(actual.getMin()).isAfter(now);
		then(actual.getMax()).isNull();
	}

	@Property
	void generateDateTimeConstraintFutureOrPresent() {
		// given
		String propertyName = "instantFutureOrPresent";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryIntrospectorContext context = new ArbitraryIntrospectorContext(
			property,
			ArbitraryTypeIntrospector.INTROSPECTORS
		);
		LocalDateTime baseDateTime = LocalDateTime.now();

		// when
		JavaxValidationDateTimeConstraint actual = this.sut.generateDateTimeConstraint(baseDateTime, context);

		// then
		LocalDateTime now = LocalDateTime.now();
		then(actual.getMin()).isAfterOrEqualTo(now);
		then(actual.getMax()).isNull();
	}

	@Property
	void generateDateConstraint() {
		// given
		String propertyName = "localDate";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryIntrospectorContext context = new ArbitraryIntrospectorContext(
			property,
			ArbitraryTypeIntrospector.INTROSPECTORS
		);
		LocalDate baseDate = LocalDate.now();

		// when
		JavaxValidationDateConstraint actual = this.sut.generateDateConstraint(baseDate, context);

		// then
		then(actual.getMin()).isNull();
		then(actual.getMax()).isNull();
	}

	@Property
	void generateDateConstraintPast() {
		// given
		String propertyName = "localDatePast";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryIntrospectorContext context = new ArbitraryIntrospectorContext(
			property,
			ArbitraryTypeIntrospector.INTROSPECTORS
		);
		LocalDate baseDate = LocalDate.now();

		// when
		JavaxValidationDateConstraint actual = this.sut.generateDateConstraint(baseDate, context);

		// then
		LocalDate now = LocalDate.now();
		then(actual.getMax()).isBefore(now);
		then(actual.getMin()).isNull();
	}

	@Property
	void generateDateConstraintPastOrPresent() {
		// given
		String propertyName = "localDatePastOrPresent";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryIntrospectorContext context = new ArbitraryIntrospectorContext(
			property,
			ArbitraryTypeIntrospector.INTROSPECTORS
		);
		LocalDate baseDate = LocalDate.now();

		// when
		JavaxValidationDateConstraint actual = this.sut.generateDateConstraint(baseDate, context);

		// then
		LocalDate now = LocalDate.now();
		then(actual.getMax()).isBeforeOrEqualTo(now);
		then(actual.getMin()).isNull();

	}

	@Property
	void generateDateConstraintFuture() {
		// given
		String propertyName = "localDateFuture";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryIntrospectorContext context = new ArbitraryIntrospectorContext(
			property,
			ArbitraryTypeIntrospector.INTROSPECTORS
		);
		LocalDate baseDate = LocalDate.now();

		// when
		JavaxValidationDateConstraint actual = this.sut.generateDateConstraint(baseDate, context);

		// then
		LocalDate now = LocalDate.now();
		then(actual.getMin()).isAfter(now);
		then(actual.getMax()).isNull();
	}

	@Property
	void generateDateConstraintFutureOrPresent() {
		// given
		String propertyName = "localDateFutureOrPresent";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getReadProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryIntrospectorContext context = new ArbitraryIntrospectorContext(
			property,
			ArbitraryTypeIntrospector.INTROSPECTORS
		);
		LocalDate baseDate = LocalDate.now();

		// when
		JavaxValidationDateConstraint actual = this.sut.generateDateConstraint(baseDate, context);

		// then
		LocalDate now = LocalDate.now();
		then(actual.getMin()).isAfterOrEqualTo(now);
		then(actual.getMax()).isNull();
	}
}
