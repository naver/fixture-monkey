package com.navercorp.fixturemonkey.javax.validation.introspector;

import java.time.YearMonth;

import javax.annotation.Nullable;

public class JavaxValidationYearMonthConstraint {
	@Nullable
	private final YearMonth min;

	@Nullable
	private final YearMonth max;

	public JavaxValidationYearMonthConstraint(@Nullable YearMonth min, @Nullable YearMonth max) {
		this.min = min;
		this.max = max;
	}

	@Nullable
	public YearMonth getMin() {
		return this.min;
	}

	@Nullable
	public YearMonth getMax() {
		return this.max;
	}
}
