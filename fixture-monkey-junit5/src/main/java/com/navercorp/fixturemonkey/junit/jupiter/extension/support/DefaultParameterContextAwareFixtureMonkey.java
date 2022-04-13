package com.navercorp.fixturemonkey.junit.jupiter.extension.support;

import org.junit.jupiter.api.extension.ParameterContext;

import com.navercorp.fixturemonkey.FixtureMonkey;

class DefaultParameterContextAwareFixtureMonkey implements ParameterContextAwareFixtureMonkey {
	private final ParameterContext parameterContext;
	private final FixtureMonkey fixtureMonkey;

	public DefaultParameterContextAwareFixtureMonkey(ParameterContext parameterContext,
		FixtureMonkey fixtureMonkey) {
		this.parameterContext = parameterContext;
		this.fixtureMonkey = fixtureMonkey;
	}

	@Override
	public Object giveMe() {
		Class<?> type = parameterContext.getParameter().getType();

		return fixtureMonkey.giveMeOne(type);
	}
}
