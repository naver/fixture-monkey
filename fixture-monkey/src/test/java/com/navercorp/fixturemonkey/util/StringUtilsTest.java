package com.navercorp.fixturemonkey.util;

import static org.assertj.core.api.BDDAssertions.then;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.NotBlank;

class StringUtilsTest {
	@Property(tries = 1)
	void isBlank(@ForAll @NotBlank String actual) {
		then(StringUtils.isBlank(actual)).isFalse();
	}

	@Property(tries = 1)
	void isNotBlank(@ForAll @NotBlank String actual) {
		then(StringUtils.isNotBlank(actual)).isTrue();
	}
}
