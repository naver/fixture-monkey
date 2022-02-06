package com.navercorp.fixturemonkey.javax.validation.introspector;

import java.time.LocalDateTime;

import javax.annotation.Nullable;

public final class JavaxValidationDateTimeConstraint {
	@Nullable
	private final LocalDateTime min;

	@Nullable
	private final LocalDateTime max;

	public JavaxValidationDateTimeConstraint(@Nullable LocalDateTime min, @Nullable LocalDateTime max) {
		this.min = min;
		this.max = max;
	}

	@Nullable
	public LocalDateTime getMin() {
		return this.min;
	}

	@Nullable
	public LocalDateTime getMax() {
		return this.max;
	}
}
