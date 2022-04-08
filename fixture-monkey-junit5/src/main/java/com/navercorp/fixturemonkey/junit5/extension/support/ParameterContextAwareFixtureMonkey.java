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

package com.navercorp.fixturemonkey.junit5.extension.support;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ParameterContext;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.junit5.annotation.GiveMe;

public interface ParameterContextAwareFixtureMonkey {

	Object giveMe();

	static ParameterContextAwareFixtureMonkey of(ParameterContext parameterContext, FixtureMonkey fixtureMonkey) {
		Type type = parameterContext.getParameter().getType();
		if (type == List.class) {
			return new ListParameterContextAwareFixtureMonkey(parameterContext, fixtureMonkey);
		} else if (type == Stream.class) {
			return new StreamParameterContextAwareFixtureMonkey(parameterContext, fixtureMonkey);
		} else {
			return new DefaultParameterContextAwareFixtureMonkey(parameterContext, fixtureMonkey);
		}
	}

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
}
