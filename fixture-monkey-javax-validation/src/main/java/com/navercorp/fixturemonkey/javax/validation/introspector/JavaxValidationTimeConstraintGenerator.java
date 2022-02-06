package com.navercorp.fixturemonkey.javax.validation.introspector;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Past;
import javax.validation.constraints.PastOrPresent;

import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorContext;

public class JavaxValidationTimeConstraintGenerator {

	public JavaxValidationDateTimeConstraint generateDateTimeConstraint(
		LocalDateTime now,
		ArbitraryIntrospectorContext context
	) {
		LocalDateTime min = null;
		if (context.findAnnotation(Future.class).isPresent()) {
			min = now.plus(3, ChronoUnit.SECONDS);	// 3000 is buffer for future time
		} else if (context.findAnnotation(FutureOrPresent.class).isPresent()) {
			min = now.plus(2, ChronoUnit.SECONDS);	// 2000 is buffer for future time
		}

		LocalDateTime max = null;
		if (context.findAnnotation(Past.class).isPresent()) {
			max = now.minus(1, ChronoUnit.SECONDS);
		} else if (context.findAnnotation(PastOrPresent.class).isPresent()) {
			max = now;
		}

		return new JavaxValidationDateTimeConstraint(min, max);
	}

	public JavaxValidationDateConstraint generateDateConstraint(
		LocalDate now,
		ArbitraryIntrospectorContext context
	) {
		LocalDate min = null;
		if (context.findAnnotation(Future.class).isPresent()) {
			min = now.plusDays(1);
		} else if (context.findAnnotation(FutureOrPresent.class).isPresent()) {
			min = now;
		}

		LocalDate max = null;
		if (context.findAnnotation(Past.class).isPresent()) {
			max = now.minusDays(1);
		} else if (context.findAnnotation(PastOrPresent.class).isPresent()) {
			max = now;
		}

		return new JavaxValidationDateConstraint(min, max);
	}
}
