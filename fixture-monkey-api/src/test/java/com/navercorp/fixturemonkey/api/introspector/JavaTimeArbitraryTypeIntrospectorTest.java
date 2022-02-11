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

package com.navercorp.fixturemonkey.api.introspector;

import static org.assertj.core.api.BDDAssertions.then;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.property.PropertyCache;

class JavaTimeArbitraryTypeIntrospectorTest {
	private final JavaTimeArbitraryTypeIntrospector sut = new JavaTimeArbitraryTypeIntrospector();

	@Property
	void calendarMatch() {
		// given
		String propertyName = "calendar";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaTimeArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		boolean actual = this.sut.match(context);

		then(actual).isTrue();
	}

	@Property
	void calendarIntrospect() {
		// given
		String propertyName = "calendar";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaTimeArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isInstanceOf(Calendar.class);
	}

	@Property
	void dateMatch() {
		// given
		String propertyName = "date";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaTimeArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		boolean actual = this.sut.match(context);

		then(actual).isTrue();
	}

	@Property
	void dateIntrospect() {
		// given
		String propertyName = "date";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaTimeArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isInstanceOf(Date.class);
	}

	@Property
	void instantMatch() {
		// given
		String propertyName = "instant";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaTimeArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		boolean actual = this.sut.match(context);

		then(actual).isTrue();
	}

	@Property
	void instantIntrospect() {
		// given
		String propertyName = "instant";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaTimeArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isInstanceOf(Instant.class);
	}

	@Property
	void localDateMatch() {
		// given
		String propertyName = "localDate";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaTimeArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		boolean actual = this.sut.match(context);

		then(actual).isTrue();
	}

	@Property
	void localDateIntrospect() {
		// given
		String propertyName = "localDate";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaTimeArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isInstanceOf(LocalDate.class);
	}

	@Property
	void localDateTimeMatch() {
		// given
		String propertyName = "localDateTime";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaTimeArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		boolean actual = this.sut.match(context);

		then(actual).isTrue();
	}

	@Property
	void localDateTimeIntrospect() {
		// given
		String propertyName = "localDateTime";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaTimeArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isInstanceOf(LocalDateTime.class);
	}

	@Property
	void localTimeMatch() {
		// given
		String propertyName = "localTime";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaTimeArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		boolean actual = this.sut.match(context);

		then(actual).isTrue();
	}

	@Property
	void localTimeIntrospect() {
		// given
		String propertyName = "localTime";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaTimeArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isInstanceOf(LocalTime.class);
	}

	@Property
	void zonedDateTimeMatch() {
		// given
		String propertyName = "zonedDateTime";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaTimeArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		boolean actual = this.sut.match(context);

		then(actual).isTrue();
	}

	@Property
	void zonedDateTimeIntrospect() {
		// given
		String propertyName = "zonedDateTime";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaTimeArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isInstanceOf(ZonedDateTime.class);
	}

	@Property
	void monthDayMatch() {
		// given
		String propertyName = "monthDay";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaTimeArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		boolean actual = this.sut.match(context);

		then(actual).isTrue();
	}

	@Property
	void monthDayIntrospect() {
		// given
		String propertyName = "monthDay";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaTimeArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isInstanceOf(MonthDay.class);
	}

	@Property
	void offsetDateTimeMatch() {
		// given
		String propertyName = "offsetDateTime";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaTimeArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		boolean actual = this.sut.match(context);

		then(actual).isTrue();
	}

	@Property
	void offsetDateTimeIntrospect() {
		// given
		String propertyName = "offsetDateTime";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaTimeArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isInstanceOf(OffsetDateTime.class);
	}

	@Property
	void offsetTimeMatch() {
		// given
		String propertyName = "offsetTime";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaTimeArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		boolean actual = this.sut.match(context);

		then(actual).isTrue();
	}

	@Property
	void offsetTimeIntrospect() {
		// given
		String propertyName = "offsetTime";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaTimeArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isInstanceOf(OffsetTime.class);
	}

	@Property
	void periodMatch() {
		// given
		String propertyName = "period";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaTimeArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		boolean actual = this.sut.match(context);

		then(actual).isTrue();
	}

	@Property
	void periodIntrospect() {
		// given
		String propertyName = "period";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaTimeArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isInstanceOf(Period.class);
	}

	@Property
	void durationMatch() {
		// given
		String propertyName = "duration";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaTimeArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		boolean actual = this.sut.match(context);

		then(actual).isTrue();
	}

	@Property
	void durationIntrospect() {
		// given
		String propertyName = "duration";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaTimeArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isInstanceOf(Duration.class);
	}

	@Property
	void yearMatch() {
		// given
		String propertyName = "year";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaTimeArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		boolean actual = this.sut.match(context);

		then(actual).isTrue();
	}

	@Property
	void yearIntrospect() {
		// given
		String propertyName = "year";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaTimeArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isInstanceOf(Year.class);
	}

	@Property
	void yearMonthMatch() {
		// given
		String propertyName = "yearMonth";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaTimeArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		boolean actual = this.sut.match(context);

		then(actual).isTrue();
	}

	@Property
	void yearMonthIntrospect() {
		// given
		String propertyName = "yearMonth";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaTimeArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isInstanceOf(YearMonth.class);
	}

	@Property
	void zoneOffsetMatch() {
		// given
		String propertyName = "zoneOffset";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaTimeArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		boolean actual = this.sut.match(context);

		then(actual).isTrue();
	}

	@Property
	void zoneOffsetIntrospect() {
		// given
		String propertyName = "zoneOffset";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaTimeArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property,  null, false, 0.0D),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isInstanceOf(ZoneOffset.class);
	}
}
