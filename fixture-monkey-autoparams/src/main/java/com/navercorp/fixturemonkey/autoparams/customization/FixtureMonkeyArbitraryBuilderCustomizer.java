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

package com.navercorp.fixturemonkey.autoparams.customization;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.javaunit.autoparams.customization.Customizer;
import org.javaunit.autoparams.generator.ObjectContainer;
import org.javaunit.autoparams.generator.ObjectGenerator;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;

final class FixtureMonkeyArbitraryBuilderCustomizer implements Customizer {
	private final FixtureMonkey fixtureMonkey;

	public FixtureMonkeyArbitraryBuilderCustomizer(FixtureMonkey fixtureMonkey) {
		this.fixtureMonkey = fixtureMonkey;
	}

	@Override
	public ObjectGenerator customize(ObjectGenerator generator) {
		return (query, context) ->
			generate(query.getType()).yieldIfEmpty(() -> generator.generate(query, context));
	}

	private ObjectContainer generate(Type type) {
		return type instanceof ParameterizedType
			? generate((ParameterizedType)type)
			: ObjectContainer.EMPTY;
	}

	private ObjectContainer generate(ParameterizedType parameterizedType) {
		Class<?> type = (Class<?>)parameterizedType.getRawType();
		return type == ArbitraryBuilder.class
			? this.generate((Class<?>)parameterizedType.getActualTypeArguments()[0])
			: ObjectContainer.EMPTY;
	}

	private ObjectContainer generate(Class<?> type) {
		return new ObjectContainer(this.fixtureMonkey.giveMeBuilder(type));
	}
}
