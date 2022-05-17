package com.navercorp.fixturemonkey.validator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CompositeArbitraryValidatorV4 implements ArbitraryValidatorV4 {
	private final Map<Class<?>, ArbitraryValidatorV4> validators;
	private final ArbitraryValidatorV4 defaultValidator;

	public CompositeArbitraryValidatorV4() {
		this(Collections.emptyMap());
	}

	public CompositeArbitraryValidatorV4(Map<Class<?>, ArbitraryValidatorV4> validators) {
		this.validators = new HashMap<>(validators);
		this.defaultValidator = findDefaultValidator();
	}

	private static ArbitraryValidatorV4 findDefaultValidator() {
		return new DefaultArbitraryValidatorV4();
	}

	@Override
	public <T> void validate(T arbitrary) {
		ArbitraryValidatorV4 validator = this.validators.get(arbitrary.getClass());
		if (validator != null) {
			validator.validate(arbitrary);
		}

		this.defaultValidator.validate(arbitrary);
	}
}
