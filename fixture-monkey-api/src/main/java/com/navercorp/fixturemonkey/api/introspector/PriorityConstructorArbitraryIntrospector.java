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
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.container.ConcurrentLruCache;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.introspector.ConstructorArbitraryIntrospector.ConstructorWithParameterNames;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;
import com.navercorp.fixturemonkey.api.type.TypeCache;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "1.0.26", status = Status.EXPERIMENTAL)
public final class PriorityConstructorArbitraryIntrospector implements ArbitraryIntrospector {
	public static final PriorityConstructorArbitraryIntrospector INSTANCE =
		new PriorityConstructorArbitraryIntrospector();

	private static final Map<Property, ConstructorArbitraryIntrospector> CONSTRUCTOR_INTROSPECTORS_BY_PROPERTY =
		new ConcurrentLruCache<>(256);

	private final Predicate<Constructor<?>> constructorFilter;
	private final Comparator<Constructor<?>> sortingCriteria;
	private final Function<Constructor<?>, List<String>> parameterNamesResolver;

	public PriorityConstructorArbitraryIntrospector() {
		this(
			constructor -> !Modifier.isPrivate(constructor.getModifiers()),
			Comparator.comparing(Constructor::getParameterCount),
			constructor -> Collections.emptyList()
		);
	}

	public PriorityConstructorArbitraryIntrospector withConstructorFilter(
		Predicate<Constructor<?>> constructorFilter
	) {
		return new PriorityConstructorArbitraryIntrospector(
			constructorFilter,
			this.sortingCriteria,
			this.parameterNamesResolver
		);
	}

	public PriorityConstructorArbitraryIntrospector withSortingCriteria(
		Comparator<Constructor<?>> sortingCriteria
	) {
		return new PriorityConstructorArbitraryIntrospector(
			this.constructorFilter,
			sortingCriteria,
			this.parameterNamesResolver
		);
	}

	public PriorityConstructorArbitraryIntrospector withParameterNamesResolver(
		Function<Constructor<?>, List<String>> parameterNamesResolver
	) {
		return new PriorityConstructorArbitraryIntrospector(
			this.constructorFilter,
			this.sortingCriteria,
			parameterNamesResolver
		);
	}

	private PriorityConstructorArbitraryIntrospector(
		Predicate<Constructor<?>> constructorFilter,
		Comparator<Constructor<?>> sortingCriteria,
		Function<Constructor<?>, List<String>> parameterNamesResolver
	) {
		this.constructorFilter = constructorFilter;
		this.sortingCriteria = sortingCriteria;
		this.parameterNamesResolver = parameterNamesResolver;
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		return getConstructorArbitraryIntrospector(context.getResolvedProperty()).introspect(context);
	}

	@Override
	public PropertyGenerator getRequiredPropertyGenerator(Property property) {
		return getConstructorArbitraryIntrospector(property).getRequiredPropertyGenerator(property);
	}

	@SuppressWarnings("assignment")
	private ConstructorArbitraryIntrospector getConstructorArbitraryIntrospector(Property property) {
		Class<?> actualType = Types.getActualType(property.getType());

		return CONSTRUCTOR_INTROSPECTORS_BY_PROPERTY.computeIfAbsent(
			property,
			p -> {
				Constructor<?> constructor = TypeCache.getDeclaredConstructors(actualType).stream()
					.filter(constructorFilter)
					.min(sortingCriteria)
					.orElseThrow(() -> new IllegalArgumentException(
						"No matching constructor given type: " + actualType.getTypeName())
					);

				List<@Nullable String> parameterNames = parameterNamesResolver.apply(constructor);

				if (!parameterNames.isEmpty() && parameterNames.size() != constructor.getParameterCount()) {
					throw new IllegalArgumentException(
						"PriorityConstructorArbitraryIntrospector fails to resolve the parameter names "
							+ "with the constructor of " + actualType.getTypeName()
							+ " Please check your parameterNamesResolver."
					);
				}

				return new ConstructorArbitraryIntrospector(
					new ConstructorWithParameterNames<>(
						constructor,
						parameterNames
					)
				);
			}
		);
	}
}
