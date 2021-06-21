package com.navercorp.fixturemonkey.validator;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;

public class DefaultArbitraryValidator<T> implements ArbitraryValidator<T> {
	private Validator validator;

	public DefaultArbitraryValidator() {
		try {
			this.validator = Validation.buildDefaultValidatorFactory().getValidator();
		} catch (Exception e) {
			this.validator = null;
		}
	}

	@Override
	public void validate(T arbitrary) {
		if (this.validator != null) {
			Set<ConstraintViolation<T>> violations = this.validator.validate(arbitrary);
			if (!violations.isEmpty()) {
				throw new ConstraintViolationException(
					"DefaultFixtureValidator ConstraintViolations. type: " + arbitrary.getClass(), violations);
			}
		}
	}
}
