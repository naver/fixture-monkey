package com.navercorp.fixturemonkey.junit.jupiter.extension.support;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ParameterContext;

import com.navercorp.fixturemonkey.FixtureMonkey;

class StreamParameterContextAwareFixtureMonkey implements ParameterContextAwareFixtureMonkey {
	private final ParameterContext parameterContext;
	private final FixtureMonkey fixtureMonkey;

	public StreamParameterContextAwareFixtureMonkey(ParameterContext parameterContext,
		FixtureMonkey fixtureMonkey) {
		if (parameterContext.getParameter().getType() != Stream.class) {
			throw new IllegalArgumentException("Type of parameter must be Stream.");
		}
		this.parameterContext = parameterContext;
		this.fixtureMonkey = fixtureMonkey;
	}

	@Override
	public Stream<?> giveMe() {
		Parameter parameter = parameterContext.getParameter();
		ParameterizedType parameterizedType = (ParameterizedType)parameter.getParameterizedType();
		Class<?> genericType = (Class<?>)parameterizedType.getActualTypeArguments()[0];

		return fixtureMonkey.giveMe(genericType);
	}
}
