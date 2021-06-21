package com.navercorp.fixturemonkey.validator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class CompositeArbitraryValidator implements ArbitraryValidator<Object> {
	private final Map<Class<?>, ArbitraryValidator> validators;
	private final ArbitraryValidator defaultValidator;

	public CompositeArbitraryValidator() {
		this(Collections.emptyMap());
	}

	public CompositeArbitraryValidator(Map<Class<?>, ArbitraryValidator> validators) {
		this.validators = new HashMap<>(validators);
		this.defaultValidator = findDefaultValidator();
	}

	private static ArbitraryValidator findDefaultValidator() {
		return new DefaultArbitraryValidator();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void validate(Object arbitrary) {
		ArbitraryValidator validator = this.validators.get(arbitrary.getClass());
		if (validator != null) {
			validator.validate(arbitrary);
		}

		this.defaultValidator.validate(arbitrary);
	}
}
