package com.navercorp.fixturemonkey.tests.java17;

import static org.assertj.core.api.BDDAssertions.then;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.java17.FakerCombinableArbitrary;

public class FakerCombinableArbitraryTest {
	@Test
	void fakerCombinableArbitraryCombined() {
		FakerCombinableArbitrary fakerCombinableArbitrary = new FakerCombinableArbitrary();

		String actual = (String) fakerCombinableArbitrary.combined();

		then(actual).matches("([\\w']+\\.?( )?){2,4}");
	}

	@Test
	void fakerCombinableArbitraryRawValue() {
		FakerCombinableArbitrary fakerCombinableArbitrary = new FakerCombinableArbitrary();

		String actual = (String) fakerCombinableArbitrary.rawValue();

		then(actual).matches("([\\w']+\\.?( )?){2,4}");
	}
}
