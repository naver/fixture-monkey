package com.navercorp.fixturemonkey.javax.validation.introspector;

import static org.assertj.core.api.BDDAssertions.then;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Property;
import net.jqwik.time.api.DateTimes;
import net.jqwik.time.api.arbitraries.InstantArbitrary;

import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorContext;

class JavaxValidationTimeArbitraryIntrospectorTest {
	private final JavaxValidationTimeArbitraryIntrospector sut = new JavaxValidationTimeArbitraryIntrospector();

	@Property
	void instant() throws NoSuchFieldException {
		// given
		InstantArbitrary instantArbitrary = DateTimes.instants();
		String propertyName = "instant";
		Field field = TimeIntrospectorSpec.class.getDeclaredField(propertyName);
		ArbitraryIntrospectorContext context = new ArbitraryIntrospectorContext(
			Instant.class,
			propertyName,
			field.getAnnotatedType(),
			Arrays.asList(field.getAnnotations())
		);

		// when
		Arbitrary<Instant> actual = this.sut.instants(instantArbitrary, context);

		// then
		Instant instant = actual.sample();
		then(instant).isNotNull();

		Instant now = Instant.now();
		then(instant).isAfter(now.minus(366, ChronoUnit.DAYS));
		then(instant).isBefore(now.plus(366, ChronoUnit.DAYS));
	}

	@Property
	void instantPast() throws NoSuchFieldException {
		// given
		InstantArbitrary instantArbitrary = DateTimes.instants();
		String propertyName = "instantPast";
		Field field = TimeIntrospectorSpec.class.getDeclaredField(propertyName);
		ArbitraryIntrospectorContext context = new ArbitraryIntrospectorContext(
			Instant.class,
			propertyName,
			field.getAnnotatedType(),
			Arrays.asList(field.getAnnotations())
		);

		// when
		Arbitrary<Instant> actual = this.sut.instants(instantArbitrary, context);

		// then
		Instant instant = actual.sample();
		then(instant).isNotNull();

		Instant now = Instant.now();
		then(instant).isAfter(now.minus(366, ChronoUnit.DAYS));
		then(instant).isBefore(now);
	}

	@Property
	void instantPastOrPresent() throws NoSuchFieldException {
		// given
		InstantArbitrary instantArbitrary = DateTimes.instants();
		String propertyName = "instantPastOrPresent";
		Field field = TimeIntrospectorSpec.class.getDeclaredField(propertyName);
		ArbitraryIntrospectorContext context = new ArbitraryIntrospectorContext(
			Instant.class,
			propertyName,
			field.getAnnotatedType(),
			Arrays.asList(field.getAnnotations())
		);

		// when
		Arbitrary<Instant> actual = this.sut.instants(instantArbitrary, context);

		// then
		Instant instant = actual.sample();
		then(instant).isNotNull();

		Instant now = Instant.now();
		then(instant).isAfter(now.minus(366, ChronoUnit.DAYS));
		then(instant).isBeforeOrEqualTo(now);
	}

	@Property
	void instantFuture() throws NoSuchFieldException {
		// given
		InstantArbitrary instantArbitrary = DateTimes.instants();
		String propertyName = "instantFuture";
		Field field = TimeIntrospectorSpec.class.getDeclaredField(propertyName);
		ArbitraryIntrospectorContext context = new ArbitraryIntrospectorContext(
			Instant.class,
			propertyName,
			field.getAnnotatedType(),
			Arrays.asList(field.getAnnotations())
		);

		// when
		Arbitrary<Instant> actual = this.sut.instants(instantArbitrary, context);

		// then
		Instant instant = actual.sample();
		then(instant).isNotNull();

		Instant now = Instant.now();
		then(instant).isAfter(now);
		then(instant).isBefore(now.plus(366, ChronoUnit.DAYS));
	}

	@Property
	void instantFutureOrPresent() throws NoSuchFieldException {
		// given
		InstantArbitrary instantArbitrary = DateTimes.instants();
		String propertyName = "instantFutureOrPresent";
		Field field = TimeIntrospectorSpec.class.getDeclaredField(propertyName);
		ArbitraryIntrospectorContext context = new ArbitraryIntrospectorContext(
			Instant.class,
			propertyName,
			field.getAnnotatedType(),
			Arrays.asList(field.getAnnotations())
		);

		// when
		Arbitrary<Instant> actual = this.sut.instants(instantArbitrary, context);

		// then
		Instant instant = actual.sample();
		then(instant).isNotNull();

		Instant now = Instant.now();
		then(instant).isAfterOrEqualTo(now);
		then(instant).isBefore(now.plus(366, ChronoUnit.DAYS));
	}
}
