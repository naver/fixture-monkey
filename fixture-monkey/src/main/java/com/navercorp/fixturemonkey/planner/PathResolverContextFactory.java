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

package com.navercorp.fixturemonkey.planner;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.customizer.ScopeSelector;
import com.navercorp.fixturemonkey.planner.AnalysisResult;
import com.navercorp.fixturemonkey.tree.ContainerSizeResolverFactory;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.input.ContainerDetector;
import com.navercorp.objectfarm.api.node.ContainerSizeResolver;
import com.navercorp.objectfarm.api.node.GenericTypeResolver;
import com.navercorp.objectfarm.api.node.InterfaceResolver;
import com.navercorp.objectfarm.api.node.SeedSnapshot;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate;
import com.navercorp.objectfarm.api.tree.AncestorAwareResolver;
import com.navercorp.objectfarm.api.tree.PathContainerSizeResolver;
import com.navercorp.objectfarm.api.tree.PathInterfaceResolver;
import com.navercorp.objectfarm.api.tree.PathResolver;
import com.navercorp.objectfarm.api.tree.PathResolverContext;
import com.navercorp.objectfarm.api.tree.ResolutionListener;

/**
 * Builds a {@link PathResolverContext}: the root scope's sizes, interface and generic type resolvers by path, and
 * the defined scopes' sizes by the nodes they select, including the size of a defined scope's container value.
 */
@API(since = "1.2.0", status = Status.EXPERIMENTAL)
final class PathResolverContextFactory {
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
		List<Map.Entry<ScopeSelector, Map<PathExpression, ArbitraryContainerInfo>>> definedScopeContainerSizes,
		ResolutionListener resolutionListener,
		boolean isFixed,
		@Nullable FixtureMonkeyOptions options,
		@Nullable AncestorAwareResolver<List<JvmNodeCandidate>> ancestorAwareChildCandidateResolver,
		SeedSnapshot sampleScope
	) {
		PathResolverContext.Builder builder = PathResolverContext.builder()
			.resolutionListener(resolutionListener)
			.sampleScope(sampleScope);
		if (ancestorAwareChildCandidateResolver != null) {
			builder.ancestorAwareChildCandidateResolver(ancestorAwareChildCandidateResolver);
		}

		Map<PathExpression, Integer> sizeSequenceByPath = analysisResult.getContainerSizeSequenceByPath();

		addRootScopeContainerSizeResolvers(builder, analysisResult);

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

		addDefinedScopeContainerSizeResolver(builder, definedScopeContainerSizes);

		if (isFixed) {
			builder.defaultContainerSizeResolver(
				containerSizeResolverFactory.createFixedContainerSizeResolver(options)
			);
		}

		return builder.build();
	}

	public List<Map.Entry<ScopeSelector, Map<PathExpression, ArbitraryContainerInfo>>> resolveContainerSizes(
		List<AnalyzedScope> analyzedDefinedScopes
	) {
		List<Map.Entry<ScopeSelector, Map<PathExpression, ArbitraryContainerInfo>>> sizes = new ArrayList<>();
		for (AnalyzedScope directives : analyzedDefinedScopes) {
			Map<PathExpression, ArbitraryContainerInfo> containerSizes =
				new HashMap<>(directives.getContainerSizesByPath());
			for (Map.Entry<PathExpression, @Nullable Object> valueEntry : directives.getValuesByPath().entrySet()) {
				OptionalInt containerSize = containerDetector.getContainerSize(valueEntry.getValue());
				if (containerSize.isPresent()) {
					int size = containerSize.getAsInt();
					containerSizes.putIfAbsent(valueEntry.getKey(), new ArbitraryContainerInfo(size, size));
				}
			}
			if (!containerSizes.isEmpty()) {
				sizes.add(new AbstractMap.SimpleImmutableEntry<>(directives.getSelector(), containerSizes));
			}
		}
		return sizes;
	}

	private void addRootScopeContainerSizeResolvers(
		PathResolverContext.Builder builder,
		AnalysisResult analysisResult
	) {
		Map<PathExpression, Integer> sequenceByPath = analysisResult.getContainerSizeSequenceByPath();
		List<Map.Entry<PathExpression, Integer>> wildcardSizeSequences = new ArrayList<>();
		for (Map.Entry<PathExpression, Integer> entry : sequenceByPath.entrySet()) {
			if (entry.getKey().hasWildcard()) {
				wildcardSizeSequences.add(entry);
			}
		}
		for (PathResolver<ContainerSizeResolver> resolver : analysisResult.getContainerSizeResolvers()) {
			PathExpression resolverPath = extractResolverPath(resolver);
			if (resolverPath == null) {
				builder.addContainerSizeResolver(resolver);
				continue;
			}

			// Sequence wins: a wildcard resolver with higher sequence shadows any exact-path
			// resolver at a matching path. Build-time pruning is what enables this — the runtime
			// EXACT-over-WILDCARD precedence in JvmNodeTreeTransformer would otherwise prevent
			// the wildcard from taking effect at the shadowed exact path.
			if (!resolverPath.hasWildcard() && !wildcardSizeSequences.isEmpty()) {
				Integer ownSequence = sequenceByPath.get(resolverPath);
				boolean overriddenByWildcard = false;
				for (Map.Entry<PathExpression, Integer> wildcardEntry : wildcardSizeSequences) {
					if (wildcardEntry.getKey().matches(resolverPath)
						&& (ownSequence == null || wildcardEntry.getValue() > ownSequence)) {
						overriddenByWildcard = true;
						break;
					}
				}
				if (overriddenByWildcard) {
					continue;
				}
			}

			builder.addContainerSizeResolver(resolver);
		}
	}

	private void addDefinedScopeContainerSizeResolver(
		PathResolverContext.Builder builder,
		List<Map.Entry<ScopeSelector, Map<PathExpression, ArbitraryContainerInfo>>> definedScopeContainerSizes
	) {
		if (definedScopeContainerSizes.isEmpty()) {
			return;
		}
		List<Map.Entry<ScopeSelector, Map<PathExpression, ContainerSizeResolver>>> resolversByScope = new ArrayList<>();
		for (Map.Entry<ScopeSelector, Map<PathExpression, ArbitraryContainerInfo>> entry : definedScopeContainerSizes) {
			Map<PathExpression, ContainerSizeResolver> resolversByField = new LinkedHashMap<>();
			entry.getValue().forEach((field, info) ->
				resolversByField.put(field, containerSizeResolverFactory.createContainerSizeResolver(info))
			);
			resolversByScope.add(new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), resolversByField));
		}
		builder.preWildcardContainerSizeResolver(new DefinedScopeContainerSizeResolver(resolversByScope, false));
		builder.postWildcardContainerSizeResolver(
			new DefinedScopeContainerSizeResolver(resolversByScope, true)
		);
	}

	private static @Nullable PathExpression extractResolverPath(PathResolver<ContainerSizeResolver> resolver) {
		if (resolver instanceof PathContainerSizeResolver) {
			PathContainerSizeResolver sizeResolver = (PathContainerSizeResolver) resolver;
			return sizeResolver.getPattern();
		}
		return null;
	}
}
