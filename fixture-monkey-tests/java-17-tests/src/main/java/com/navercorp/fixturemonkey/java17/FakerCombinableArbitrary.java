package com.navercorp.fixturemonkey.java17;


import java.lang.reflect.Method;

import net.datafaker.Faker;
import net.datafaker.providers.base.Name;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;

public class FakerCombinableArbitrary implements CombinableArbitrary {

	private final Faker faker = new Faker();

	@Override
	public Object combined() {
		String string = faker.name().fullName();
		return string;
	}

	@Override
	public Object rawValue() {
		String string = faker.name().fullName();
		return string;
	}

	@Override
	public void clear() {
	}

	@Override
	public boolean fixed() {
		return false;
	}
}

