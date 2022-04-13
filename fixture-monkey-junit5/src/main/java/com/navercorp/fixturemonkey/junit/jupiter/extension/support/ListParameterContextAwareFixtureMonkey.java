package com.navercorp.fixturemonkey.junit.jupiter.extension.support;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.junit.jupiter.api.extension.ParameterContext;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.junit.jupiter.annotation.GiveMe;

class ListParameterContextAwareFixtureMonkey implements ParameterContextAwareFixtureMonkey {
	private final ParameterContext parameterContext;
	private final FixtureMonkey fixtureMonkey;

	public ListParameterContextAwareFixtureMonkey(ParameterContext parameterContext, FixtureMonkey fixtureMonkey) {
		if (parameterContext.getParameter().getType() != List.class) {
			throw new IllegalArgumentException("Type of parameter must be List.");
		}
		this.parameterContext = parameterContext;
		this.fixtureMonkey = fixtureMonkey;
	}

	@Override
	public List<?> giveMe() {
		Parameter parameter = parameterContext.getParameter();
		ParameterizedType parameterizedType = (ParameterizedType)parameter.getParameterizedType();
		Class<?> genericType = (Class<?>)parameterizedType.getActualTypeArguments()[0];

		int size = parameter.getAnnotation(GiveMe.class).size();
		return fixtureMonkey.giveMe(genericType, size);
	}
}
