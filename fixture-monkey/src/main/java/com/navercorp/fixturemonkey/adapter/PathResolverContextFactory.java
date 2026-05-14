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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.adapter.analysis.AnalysisResult;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.input.ContainerDetector;
import com.navercorp.objectfarm.api.node.GenericTypeResolver;
import com.navercorp.objectfarm.api.node.InterfaceResolver;
import com.navercorp.objectfarm.api.tree.PathInterfaceResolver;
import com.navercorp.objectfarm.api.tree.PathResolver;
import com.navercorp.objectfarm.api.tree.PathResolverContext;
import com.navercorp.objectfarm.api.tree.ResolutionListener;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Builds a {@link PathResolverContext} from analysis output and registered builder data.
 * <p>
 * Aggregates analysis-derived container size resolvers (with wildcard-vs-exact precedence applied),
 * type-scoped container size resolvers, interface resolvers, and generic type resolvers. Optionally
 * infers additional type-scoped container sizes from typed values that are themselves containers.
 */
@API(since = "1.1.17", status = Status.EXPERIMENTAL)
public final class PathResolverContextFactory {
	private final ContainerSizeResolverFactory containerSizeResolverFactory;
	private final ContainerDetector containerDetector;

	public PathResolverContextFactory(
		ContainerSizeResolverFactory containerSizeResolverFactory,
		ContainerDetector containerDetector
	) {
		this.containerSizeResolverFactory = containerSizeResolverFactory;
		this.containerDetector = containerDetector;
	}

	/**
	 * Builds a {@link PathResolverContext}.
	 */
	public PathResolverContext build(
		AnalysisResult analysisResult,
		Map<JvmType, Map<String, ArbitraryContainerInfo>> typedContainerSizes,
		ResolutionListener resolutionListener,
		boolean isFixed,
		@Nullable FixtureMonkeyOptions options
	) {
		PathResolverContext.Builder builder = PathResolverContext.builder().resolutionListener(resolutionListener);

		Map<PathExpression, Integer> sizeSequenceByPath = analysisResult.getContainerSizeSequenceByPath();

		List<Map.Entry<PathExpression, Integer>> wildcardSizeSequences = new ArrayList<>();
		for (Map.Entry<PathExpression, Integer> entry : sizeSequenceByPath.entrySet()) {
			if (entry.getKey().hasWildcard()) {
				wildcardSizeSequences.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()));
			}
		}

		containerSizeResolverFactory.addAnalysisContainerSizeResolvers(
			builder,
			analysisResult,
			wildcardSizeSequences
		);

		Map<PathExpression, Integer> valueOrderByPath = analysisResult.getValueOrderByPath();
		for (PathResolver<InterfaceResolver> resolver : analysisResult.getInterfaceResolvers()) {
			// Skip interface resolvers for container paths where a later size() constraint exists.
			// When set("values", singletonList(X)) creates a List → SingletonList resolver,
			// but size("values", 1, 10) comes later, the concrete type should not be locked.
			if (resolver instanceof PathInterfaceResolver) {
				PathExpression resolverPath = ((PathInterfaceResolver)resolver).getPattern();
				Integer sizeSeq = sizeSequenceByPath.get(resolverPath);
				Integer valueSeq = valueOrderByPath.get(resolverPath);
				if (sizeSeq != null && valueSeq != null && sizeSeq > valueSeq) {
					continue;
				}
			}
			builder.addInterfaceResolver(resolver);
		}

		for (PathResolver<GenericTypeResolver> resolver : analysisResult.getGenericTypeResolvers()) {
			builder.addGenericTypeResolver(resolver);
		}

		containerSizeResolverFactory.addTypedContainerSizeResolvers(builder, typedContainerSizes);

		if (isFixed) {
			builder.defaultContainerSizeResolver(
				containerSizeResolverFactory.createFixedContainerSizeResolver(options)
			);
		}

		return builder.build();
	}

	/**
	 * Augments {@code explicitTypedContainerSizes} with sizes inferred from container-typed values
	 * in {@code typedValues}. Values that are containers contribute a fixed size resolver
	 * (size, size) for their {@code (ownerType, fieldName)}.
	 */
	public Map<JvmType, Map<String, ArbitraryContainerInfo>> inferAndMergeTypedContainerSizes(
		Map<JvmType, Map<String, @Nullable Object>> typedValues,
		Map<JvmType, Map<String, ArbitraryContainerInfo>> explicitTypedContainerSizes
	) {
		Map<JvmType, Map<String, ArbitraryContainerInfo>> merged = new HashMap<>(explicitTypedContainerSizes);

		for (Map.Entry<JvmType, Map<String, @Nullable Object>> typeEntry : typedValues.entrySet()) {
			JvmType ownerType = typeEntry.getKey();
			for (Map.Entry<String, @Nullable Object> fieldEntry : typeEntry.getValue().entrySet()) {
				String fieldName = fieldEntry.getKey();
				Object value = fieldEntry.getValue();

				OptionalInt containerSize = containerDetector.getContainerSize(value);
				if (!containerSize.isPresent()) {
					continue;
				}

				int size = containerSize.getAsInt();
				merged.computeIfAbsent(ownerType, k -> new HashMap<>())
					.putIfAbsent(fieldName, new ArbitraryContainerInfo(size, size));
			}
		}
		return merged;
	}
}
