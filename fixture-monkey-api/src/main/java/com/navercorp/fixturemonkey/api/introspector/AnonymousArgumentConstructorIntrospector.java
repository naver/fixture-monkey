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

package com.navercorp.fixturemonkey.api.introspector;

import java.lang.reflect.Constructor;
import java.util.Collections;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.fixturemonkey.api.container.ConcurrentLruCache;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.introspector.ConstructorArbitraryIntrospector.ConstructorWithParameterNames;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;
import com.navercorp.fixturemonkey.api.type.TypeCache;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "1.0.10", status = Status.EXPERIMENTAL)
public final class AnonymousArgumentConstructorIntrospector implements ArbitraryIntrospector {
	public static final AnonymousArgumentConstructorIntrospector INSTANCE =
		new AnonymousArgumentConstructorIntrospector();

	private static final ConcurrentLruCache<Class<?>, ConstructorArbitraryIntrospector> introspectorsByType =
		new ConcurrentLruCache<>(2048);
	private static final Logger LOGGER = LoggerFactory.getLogger(AnonymousArgumentConstructorIntrospector.class);

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		Class<?> type = Types.getActualType(context.getResolvedType());

		LOGGER.info(
			"Type {} is generated. But it cannot be manipulated. "
				+ "If you want to, You have to use a custom ConstructorParameterPropertyGenerator.",
			type
		);
		return getConstructorArbitraryIntrospector(type).introspect(context);
	}

	private static ConstructorArbitraryIntrospector getConstructorArbitraryIntrospector(Class<?> type) {
		return introspectorsByType.computeIfAbsent(
			type,
			clazz -> {
				Constructor<?> constructor = TypeCache.getDeclaredConstructors(clazz).stream()
					.findFirst()
					.orElseThrow(
						() -> new IllegalArgumentException("Given type " + clazz.getTypeName() + " has no constructor.")
					);

				return new ConstructorArbitraryIntrospector(
					new ConstructorWithParameterNames<>(
						constructor,
						Collections.emptyList()
					)
				);
			}
		);
	}

	@Override
	public PropertyGenerator getRequiredPropertyGenerator(Property property) {
		Class<?> type = Types.getActualType(property.getType());
		return getConstructorArbitraryIntrospector(type).getRequiredPropertyGenerator(property);
	}
}
