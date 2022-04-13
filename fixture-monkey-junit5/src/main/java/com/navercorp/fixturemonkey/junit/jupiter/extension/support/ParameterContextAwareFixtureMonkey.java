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

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ParameterContext;

import com.navercorp.fixturemonkey.FixtureMonkey;

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
}
