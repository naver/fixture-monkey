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

package com.navercorp.fixturemonkey.assembly;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.customizer.ScopeChain;
import com.navercorp.fixturemonkey.decompose.DecomposedContainerDetector;
import com.navercorp.fixturemonkey.decompose.PropertyFieldExtractor;
import com.navercorp.fixturemonkey.planner.AssemblyPlan;
import com.navercorp.fixturemonkey.planner.LazyValueHolder;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.input.ContainerDetector;
import com.navercorp.objectfarm.api.input.ObjectValueExtractor;
import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.node.SeedSnapshot;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Mutable per-call state shared across all helpers cooperating during a single
 * {@link ValueProjectionAssembler#assemble} pass.
 *
 * <p>Package-private and split out from {@link ValueProjectionAssembler} so that helpers
 * (path matching, lazy resolution, type metadata, map/interface assembly) can read the same
 * state without duplicating the parameter list.</p>
 */
final class AssemblyState {

	final AssembleContext context;
	final CandidateLookup candidates;
	final LimitCounter limits;
	final SeedSnapshot sampleScope;
	final AtomicInteger interfaceSelectionCounter;

	final AssemblyTree assemblyTree;

	final PathIndex pathIndex;

	final NodePropertyLookup properties;

	final Map<JvmNode, Property> propertyPathPropertyByNode;

	final Map<LazyValueHolder, Object> resolvedLazyCache;

	final ScopeLookup scopes;

	final ValueDecomposer valueDecomposer;

	final ConcurrentHashMap<JvmType, CachedTypeMetadata> typeMetadataCache;

	AssemblyState(AssembleContext context) {
		this.context = context;
		AssemblyPlan plan = context.getPlan();
		this.scopes = ScopeLookup.from(plan.getAnalysisResult(), plan.getAnalyzedDefinedScopes());
		this.candidates = CandidateLookup.from(plan.getValues().getValuesByPath(), scopes);
		this.limits = new LimitCounter(plan.getAnalyzedRootScope(), plan.getAnalyzedDefinedScopes());
		this.sampleScope = context.getSampleScope();
		this.interfaceSelectionCounter = new AtomicInteger(0);

		this.assemblyTree = new AssemblyTree(plan.getNodeTree(), sampleScope);
		this.propertyPathPropertyByNode = new IdentityHashMap<>();
		this.resolvedLazyCache = new IdentityHashMap<>();
		FixtureMonkeyOptions options = context.getOptions();
		ContainerDetector containerDetector =
			new DecomposedContainerDetector(options.getDecomposedContainerValueFactory());
		this.valueDecomposer = new ValueDecomposer(
			candidates,
			limits,
			new ObjectValueExtractor(
				new PropertyFieldExtractor(
					property -> options.getPropertyNameResolver(property).resolve(property),
					context.getPlan().getInlinedValueResolver()
				),
				containerDetector
			),
			containerDetector,
			assemblyTree
		);
		this.pathIndex = new PathIndex(
			new HashSet<>(candidates.getPaths()),
			scopes.getRootContainerSizePaths(),
			scopes.getRootCustomizerFilterNotNullPaths()
		);

		this.properties = NodePropertyLookup.from(assemblyTree);

		@SuppressWarnings("unchecked")
		ConcurrentHashMap<JvmType, CachedTypeMetadata> typeMetadataCache =
			(ConcurrentHashMap<JvmType, CachedTypeMetadata>)context.getPlan().getTypeMetadataCache();
		this.typeMetadataCache = typeMetadataCache;
	}

	/**
	 * Returns the chain of the nodes placed along {@code path}, where the scopes that apply at its end are looked up.
	 */
	ScopeChain chainOf(PathExpression path) {
		return ScopeChain.ofPath(path, assemblyTree::nodeAt, properties::matchingPropertyOf);
	}
}
