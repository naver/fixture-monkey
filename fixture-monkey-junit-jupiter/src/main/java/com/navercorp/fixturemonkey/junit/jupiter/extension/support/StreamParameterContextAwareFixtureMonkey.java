/*
 * Fixture Monkey
 *
 * Copyright (c) 2021-present NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.fixturemonkey.junit.jupiter.extension.support;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ParameterContext;

import com.navercorp.fixturemonkey.FixtureMonkey;

final class StreamParameterContextAwareFixtureMonkey implements ParameterContextAwareFixtureMonkey {
	private final ParameterContext parameterContext;
	private final FixtureMonkey fixtureMonkey;

	public StreamParameterContextAwareFixtureMonkey(
		ParameterContext parameterContext,
		FixtureMonkey fixtureMonkey
	) {
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
