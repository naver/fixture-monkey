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

package com.navercorp.fixturemonkey.adapter;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.adapter.analysis.AnalysisResult;
import com.navercorp.fixturemonkey.adapter.property.JvmNodePropertyFactory;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.api.property.CandidateConcretePropertyResolver;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.InterfaceResolver;
import com.navercorp.objectfarm.api.node.SeedState;
import com.navercorp.objectfarm.api.tree.PathResolver;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Resolves the planning-phase root type.
 * <p>
 * If the root type is an interface or abstract class, this resolver follows
 * {@link CandidateConcretePropertyResolver} (configured via {@link FixtureMonkeyOptions}) and any
 * explicit {@code set(concreteValue)} root-path interface resolver in the analysis result to pick
 * a concrete type. Containers (Collection, Map) are left to the default candidate-resolver chain.
 */
@API(since = "1.1.17", status = Status.EXPERIMENTAL)
public final class RootTypeResolver {
	private final SeedState seedState;

	public RootTypeResolver(SeedState seedState) {
		this.seedState = seedState;
	}

	/**
	 * Resolves the root type using {@code $}-path InterfaceResolvers from the analysis result first,
	 * falling back to the candidate-resolver chain.
	 * <p>
	 * Container types (List, Set, Map) bypass the {@code $}-path lookup and use the default
	 * resolution since collection element types are resolved separately by
	 * {@link com.navercorp.objectfarm.api.tree.JvmNodeTreeTransformer}.
	 */
	public JvmType resolve(
		JvmType rootType,
		AnalysisResult analysisResult,
		@Nullable FixtureMonkeyOptions options
	) {
		Class<?> rawType = rootType.getRawType();

		if (!Modifier.isInterface(rawType.getModifiers()) && !Modifier.isAbstract(rawType.getModifiers())) {
			return rootType;
		}

		if (Collection.class.isAssignableFrom(rawType) || Map.class.isAssignableFrom(rawType)) {
			return resolveAbstractType(rootType, options);
		}

		// Check if there's a "$" path InterfaceResolver from explicit set(concreteValue).
		// Iterate from the end so the last set() call wins.
		PathExpression rootPath = PathExpression.of("$");
		List<PathResolver<InterfaceResolver>> resolvers = analysisResult.getInterfaceResolvers();
		for (int i = resolvers.size() - 1; i >= 0; i--) {
			PathResolver<InterfaceResolver> resolver = resolvers.get(i);
			if (resolver.matches(rootPath)) {
				JvmType resolved = resolver.getCustomizer().resolve(rootType);
				if (resolved != null) {
					return resolved;
				}
			}
		}

		return resolveAbstractType(rootType, options);
	}

	/**
	 * Walks the candidate-resolver chain until a concrete type is reached or the recursion depth runs out.
	 */
	public JvmType resolveAbstractType(JvmType type, @Nullable FixtureMonkeyOptions options) {
		if (options == null) {
			return type;
		}

		JvmType currentType = type;
		Set<Class<?>> visited = new HashSet<>();
		int maxDepth = options.getMaxRecursionDepth();

		for (int depth = 0; depth < maxDepth; depth++) {
			Class<?> rawType = currentType.getRawType();

			if (!visited.add(rawType)) {
				return currentType;
			}

			if (!Modifier.isInterface(rawType.getModifiers()) && !Modifier.isAbstract(rawType.getModifiers())) {
				return currentType;
			}

			Property property = JvmNodePropertyFactory.fromType(currentType);
			@SuppressWarnings("deprecation")
			CandidateConcretePropertyResolver resolver = options.getCandidateConcretePropertyResolver(property);

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
