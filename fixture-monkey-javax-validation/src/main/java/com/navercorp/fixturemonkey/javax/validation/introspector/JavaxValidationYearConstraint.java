package com.navercorp.fixturemonkey.javax.validation.introspector;

import java.time.Year;

import javax.annotation.Nullable;

public class JavaxValidationYearConstraint {
	@Nullable
	private final Year min;

	@Nullable
	private final Year max;

	public JavaxValidationYearConstraint(@Nullable Year min, @Nullable Year max) {
		this.min = min;
		this.max = max;
	}

	@Nullable
	public Year getMin() {
		return this.min;
	}

	@Nullable
	public Year getMax() {
		return this.max;
	}
}
