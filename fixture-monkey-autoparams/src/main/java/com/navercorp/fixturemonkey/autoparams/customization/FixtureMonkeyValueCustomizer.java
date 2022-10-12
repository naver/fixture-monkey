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
import java.util.Map;
import java.util.stream.Stream;

import autoparams.Builder;
import autoparams.customization.Customizer;
import autoparams.generator.ObjectContainer;
import autoparams.generator.ObjectGenerator;

import com.navercorp.fixturemonkey.FixtureMonkey;

final class FixtureMonkeyValueCustomizer implements Customizer {
	private final FixtureMonkey fixtureMonkey;

	public FixtureMonkeyValueCustomizer(FixtureMonkey fixtureMonkey) {
		this.fixtureMonkey = fixtureMonkey;
	}

	@Override
	public ObjectGenerator customize(ObjectGenerator generator) {
		return (query, context) ->
			generate(query.getType()).yieldIfEmpty(() -> generator.generate(query, context));
	}

	private ObjectContainer generate(Type type) {
		if (type instanceof Class<?>) {
			return this.generateNonGeneric((Class<?>) type);
		} else if (type instanceof ParameterizedType) {
			return this.generateGeneric((ParameterizedType)type);
		} else {
			return ObjectContainer.EMPTY;
		}
	}

	private ObjectContainer generateNonGeneric(Class<?> type) {
		return new ObjectContainer(this.fixtureMonkey.giveMeOne(type));
	}

	private ObjectContainer generateGeneric(ParameterizedType parameterizedType) {
		Class<?> type = (Class<?>) parameterizedType.getRawType();
		if (this.isYieldType(type)) {
			return ObjectContainer.EMPTY;
		}

		return new ObjectContainer(this.fixtureMonkey.giveMeOne(type));
	}

	private boolean isYieldType(Class<?> type) {
		if (Builder.class.isAssignableFrom(type)) {
			return true;
		}

		if (Iterable.class.isAssignableFrom(type)) {
			return true;
		}

		if (Stream.class.isAssignableFrom(type)) {
			return true;
		}

		if (Map.class.isAssignableFrom(type)) {
			return true;
		}

		return false;
	}
}
