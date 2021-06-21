package com.navercorp.fixturemonkey.validator;

public interface ArbitraryValidator<T> {
	// if arbitrary is not valid throw exception then re-create arbitrary for valid.
	void validate(T arbitrary);
}
