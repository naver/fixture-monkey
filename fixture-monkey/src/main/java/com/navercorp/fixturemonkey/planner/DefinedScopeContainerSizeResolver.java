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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.customizer.ScopeChain;
import com.navercorp.fixturemonkey.customizer.ScopeSelector;
import com.navercorp.fixturemonkey.property.JvmNodePropertyFactory;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.ContainerSizeResolver;
import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.tree.AncestorAwareResolver;

/**
 * Sizes a container a defined scope reaches: the path from a node the scope selects down to the container matches
 * the path the size was declared at. Unlike values, an inner scope wins over an outer one; at the same node the
 * scope with the highest precedence wins.
 * <p>
 * One instance looks at the scope nodes below the node a sample starts from, the other only at that node. The root
 * scope sits at that node too and wins there, so a defined scope selecting it sizes a container only when no size
 * the root scope declared does.
 */
final class DefinedScopeContainerSizeResolver implements AncestorAwareResolver<ContainerSizeResolver> {
	private final List<ScopedSize> highestPrecedenceFirst;
	private final int[] pathLengthsShortestFirst;
	private final boolean dependsOnAncestors;
	private final boolean atSampleRoot;

	DefinedScopeContainerSizeResolver(
		List<Map.Entry<ScopeSelector, Map<PathExpression, ContainerSizeResolver>>> resolversLowestPrecedenceFirst,
		boolean atSampleRoot
	) {
		this.atSampleRoot = atSampleRoot;
		this.highestPrecedenceFirst = new ArrayList<>();
		for (Map.Entry<ScopeSelector, Map<PathExpression, ContainerSizeResolver>> entry
			: resolversLowestPrecedenceFirst) {
			entry.getValue().forEach((path, resolver) ->
				highestPrecedenceFirst.add(0, new ScopedSize(entry.getKey(), path, resolver))
			);
		}
		this.pathLengthsShortestFirst = highestPrecedenceFirst
			.stream()
			.mapToInt(it -> it.path.getSegments().size())
			.distinct()
			.sorted()
			.toArray();
		this.dependsOnAncestors = highestPrecedenceFirst
			.stream()
			.anyMatch(it -> it.scope.dependsOnAncestors() || it.path.getSegments().size() > 1);
	}

	@Override
	public @Nullable ContainerSizeResolver resolve(JvmNode containerNode, List<JvmNode> ancestors) {
		List<JvmNode> nodes = new ArrayList<>(ancestors);
		nodes.add(containerNode);
		ScopeChain chain = ScopeChain.ofNodes(nodes, JvmNodePropertyFactory.ofChain(nodes));
		for (int pathLength : pathLengthsShortestFirst) {
			int scopeDepth = chain.depth() - pathLength;
			if (atSampleRoot ? scopeDepth != 0 : scopeDepth < 1) {
				continue;
			}
			for (ScopedSize scopedSize : highestPrecedenceFirst) {
				if (chain.matchesBelow(scopeDepth, scopedSize.path) && chain.selects(scopedSize.scope, scopeDepth)) {
					return scopedSize.resolver;
				}
			}
		}
		return null;
	}

	@Override
	public boolean dependsOnAncestors() {
		return dependsOnAncestors;
	}

	private static final class ScopedSize {
		private final ScopeSelector scope;
		private final PathExpression path;
		private final ContainerSizeResolver resolver;

		private ScopedSize(ScopeSelector scope, PathExpression path, ContainerSizeResolver resolver) {
			this.scope = scope;
			this.path = path;
			this.resolver = resolver;
		}
	}
}
