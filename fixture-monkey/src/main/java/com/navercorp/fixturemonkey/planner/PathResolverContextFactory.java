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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.customizer.ScopeSet;
import com.navercorp.fixturemonkey.planner.AnalysisResult;
import com.navercorp.fixturemonkey.tree.ContainerSizeResolverFactory;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.ContainerSizeResolver;
import com.navercorp.objectfarm.api.node.GenericTypeResolver;
import com.navercorp.objectfarm.api.node.InterfaceResolver;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate;
import com.navercorp.objectfarm.api.tree.AncestorAwareResolver;
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

	public PathResolverContextFactory(ContainerSizeResolverFactory containerSizeResolverFactory) {
		this.containerSizeResolverFactory = containerSizeResolverFactory;
	}

	/**
	 * Builds a {@link PathResolverContext}.
	 */
	public PathResolverContext build(
		AnalysisResult analysisResult,
		List<Map.Entry<AnalyzedScope, Map<PathExpression, ArbitraryContainerInfo>>> definedScopeContainerSizes,
		ScopeSet scopeSet,
		ResolutionListener resolutionListener,
		boolean isFixed,
		@Nullable FixtureMonkeyOptions options,
		@Nullable AncestorAwareResolver<List<JvmNodeCandidate>> ancestorAwareChildCandidateResolver
	) {
		PathResolverContext.Builder builder = PathResolverContext.builder()
			.resolutionListener(resolutionListener);
		if (ancestorAwareChildCandidateResolver != null) {
			builder.ancestorAwareChildCandidateResolver(ancestorAwareChildCandidateResolver);
		}

		addRootScopeContainerSizeResolvers(builder, analysisResult);

		for (PathResolver<InterfaceResolver> resolver : analysisResult.getInterfaceResolvers()) {
			builder.addInterfaceResolver(resolver);
		}

		for (PathResolver<GenericTypeResolver> resolver : analysisResult.getGenericTypeResolvers()) {
			builder.addGenericTypeResolver(resolver);
		}

		addDefinedScopeContainerSizeResolver(builder, definedScopeContainerSizes, scopeSet);

		if (isFixed) {
			builder.defaultContainerSizeResolver(
				containerSizeResolverFactory.createFixedContainerSizeResolver(options)
			);
		}

		return builder.build();
	}

	public List<Map.Entry<AnalyzedScope, Map<PathExpression, ArbitraryContainerInfo>>> resolveContainerSizes(
		List<AnalyzedScope> analyzedDefinedScopes
	) {
		List<Map.Entry<AnalyzedScope, Map<PathExpression, ArbitraryContainerInfo>>> sizes = new ArrayList<>();
		for (AnalyzedScope directives : analyzedDefinedScopes) {
			Map<PathExpression, ArbitraryContainerInfo> containerSizes = directives.getContainerSizesByPath();
			if (!containerSizes.isEmpty()) {
				sizes.add(new AbstractMap.SimpleImmutableEntry<>(directives, containerSizes));
			}
		}
		return sizes;
	}

	private void addRootScopeContainerSizeResolvers(
		PathResolverContext.Builder builder,
		AnalysisResult analysisResult
	) {
		for (PathResolver<ContainerSizeResolver> resolver : analysisResult.getContainerSizeResolvers()) {
			builder.addContainerSizeResolver(resolver);
		}
	}

	private void addDefinedScopeContainerSizeResolver(
		PathResolverContext.Builder builder,
		List<Map.Entry<AnalyzedScope, Map<PathExpression, ArbitraryContainerInfo>>> definedScopeContainerSizes,
		ScopeSet scopeSet
	) {
		if (definedScopeContainerSizes.isEmpty()) {
			return;
		}
		List<Map.Entry<AnalyzedScope, Map<PathExpression, ContainerSizeResolver>>> resolversByScope = new ArrayList<>();
		for (Map.Entry<AnalyzedScope, Map<PathExpression, ArbitraryContainerInfo>> entry : definedScopeContainerSizes) {
			Map<PathExpression, ContainerSizeResolver> resolversByField = new LinkedHashMap<>();
			entry.getValue().forEach((field, info) ->
				resolversByField.put(field, containerSizeResolverFactory.createContainerSizeResolver(info))
			);
			resolversByScope.add(new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), resolversByField));
		}
		builder.ancestorAwareContainerSizeResolver(new DefinedScopeContainerSizeResolver(resolversByScope, scopeSet));
	}
}
