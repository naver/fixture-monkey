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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Past;
import javax.validation.constraints.PastOrPresent;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;

@API(since = "0.4.0", status = Status.MAINTAINED)
public class JavaxValidationTimeConstraintGenerator {

	public JavaxValidationDateTimeConstraint generateDateTimeConstraint(ArbitraryGeneratorContext context) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime min = null;
		if (context.findAnnotation(Future.class).isPresent()) {
			min = now.plus(3, ChronoUnit.SECONDS);    // 3000 is buffer for future time
		} else if (context.findAnnotation(FutureOrPresent.class).isPresent()) {
			min = now.plus(2, ChronoUnit.SECONDS);    // 2000 is buffer for future time
		}

		LocalDateTime max = null;
		if (context.findAnnotation(Past.class).isPresent()) {
			max = now.minus(1, ChronoUnit.SECONDS);
		} else if (context.findAnnotation(PastOrPresent.class).isPresent()) {
			max = now;
		}

		return new JavaxValidationDateTimeConstraint(min, max);
	}

	public JavaxValidationDateConstraint generateDateConstraint(ArbitraryGeneratorContext context) {
		LocalDate now = LocalDate.now();
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

	public JavaxValidationTimeConstraint generateTimeConstraint(ArbitraryGeneratorContext context) {
		LocalTime now = LocalTime.now();
		LocalTime min = null;
		if (context.findAnnotation(Future.class).isPresent()) {
			min = now.plusSeconds(3);    // 3000 is buffer for future time
		} else if (context.findAnnotation(FutureOrPresent.class).isPresent()) {
			min = now.plusSeconds(2);    // 2000 is buffer for future time
		}

		LocalTime max = null;
		if (context.findAnnotation(Past.class).isPresent()) {
			max = now.minusNanos(1L);
		} else if (context.findAnnotation(PastOrPresent.class).isPresent()) {
			max = now;
		}

		return new JavaxValidationTimeConstraint(min, max);
	}

	public JavaxValidationYearConstraint generateYearConstraint(ArbitraryGeneratorContext context) {
		Year now = Year.now();
		Year min = null;
		if (context.findAnnotation(Future.class).isPresent()) {
			min = now.plusYears(1);
		} else if (context.findAnnotation(FutureOrPresent.class).isPresent()) {
			min = now;
		}

		Year max = null;
		if (context.findAnnotation(Past.class).isPresent()) {
			max = now.minusYears(1);
		} else if (context.findAnnotation(PastOrPresent.class).isPresent()) {
			max = now;
		}

		return new JavaxValidationYearConstraint(min, max);
	}

	public JavaxValidationYearMonthConstraint generateYearMonthConstraint(ArbitraryGeneratorContext context) {
		YearMonth now = YearMonth.now();
		YearMonth min = null;
		if (context.findAnnotation(Future.class).isPresent()) {
			min = now.plusMonths(1);
		} else if (context.findAnnotation(FutureOrPresent.class).isPresent()) {
			min = now;
		}

		YearMonth max = null;
		if (context.findAnnotation(Past.class).isPresent()) {
			max = now.minusMonths(1);
		} else if (context.findAnnotation(PastOrPresent.class).isPresent()) {
			max = now;
		}

		return new JavaxValidationYearMonthConstraint(min, max);
	}
}
