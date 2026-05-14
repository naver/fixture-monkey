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

package com.navercorp.fixturemonkey.resolver;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.property.JvmNodePropertyFactory;
import com.navercorp.fixturemonkey.api.property.CandidateConcretePropertyResolver;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.objectfarm.api.node.SeedState;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Walks the {@link CandidateConcretePropertyResolver} chain to resolve an interface or
 * abstract type into a concrete type.
 * <p>
 * The walk terminates when a concrete type is reached, a cycle is detected, the recursion
 * depth limit is exceeded, or no resolver is configured for the current type.
 */
@API(since = "1.1.17", status = Status.EXPERIMENTAL)
public final class AbstractTypeResolver {
	private final SeedState seedState;

	public AbstractTypeResolver(SeedState seedState) {
		this.seedState = seedState;
	}

	/**
	 * Walks the candidate-resolver chain until a concrete type is reached or the recursion depth runs out.
	 * Non-abstract input types are returned unchanged.
	 *
	 * @param type              the type to resolve
	 * @param resolverLookup    looks up the {@link CandidateConcretePropertyResolver} for a property,
	 *                          returning {@code null} when none is configured
	 * @param maxRecursionDepth maximum number of resolver hops before bailing out
	 */
	public JvmType resolve(
		JvmType type,
		Function<Property, @Nullable CandidateConcretePropertyResolver> resolverLookup,
		int maxRecursionDepth
	) {
		JvmType currentType = type;
		Set<Class<?>> visited = new HashSet<>();

		for (int depth = 0; depth < maxRecursionDepth; depth++) {
			Class<?> rawType = currentType.getRawType();

			if (!visited.add(rawType)) {
				return currentType;
			}

			if (!Modifier.isInterface(rawType.getModifiers()) && !Modifier.isAbstract(rawType.getModifiers())) {
				return currentType;
			}

			Property property = JvmNodePropertyFactory.fromType(currentType);
			CandidateConcretePropertyResolver resolver = resolverLookup.apply(property);

			if (resolver == null) {
				return currentType;
			}

			List<Property> candidates = resolver.resolve(property);
			if (candidates == null || candidates.isEmpty()) {
				return currentType;
			}

			Random random = seedState.snapshot().randomFor(currentType.hashCode());
			Property selected = candidates.get(random.nextInt(candidates.size()));

			currentType = selected.getJvmType();
		}

		return currentType;
	}
}
