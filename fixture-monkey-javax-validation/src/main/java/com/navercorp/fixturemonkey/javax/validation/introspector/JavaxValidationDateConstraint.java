package com.navercorp.fixturemonkey.javax.validation.introspector;

import java.time.LocalDate;

import javax.annotation.Nullable;

public final class JavaxValidationDateConstraint {
	@Nullable
	private final LocalDate min;

	@Nullable
	private final LocalDate max;

	public JavaxValidationDateConstraint(@Nullable LocalDate min, @Nullable LocalDate max) {
		this.min = min;
		this.max = max;
	}

	@Nullable
	public LocalDate getMin() {
		return this.min;
	}

	@Nullable
	public LocalDate getMax() {
		return this.max;
	}
}
