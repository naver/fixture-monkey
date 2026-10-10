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

import com.navercorp.fixturemonkey.customizer.Scope;
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
 * A scope the sampled builder selected by name comes first wherever its node sits, the one selected later first, and
 * an inner node of the same scope first. Where it selects the node a sample starts from, it yields to a size the root
 * scope declared through a wildcard path, as it does to one the root scope declared at the container's own path.
 * <p>
 * One instance looks at the selected scopes and at the scope nodes below the node a sample starts from, the other
 * only at that node. The root scope sits at that node too and wins there, so a defined scope not selected that selects
 * it sizes a container only when no size the root scope declared does.
 */
final class DefinedScopeContainerSizeResolver implements AncestorAwareResolver<ContainerSizeResolver> {
	private final List<List<ScopedSize>> selectedHighestPrecedenceFirst;
	private final List<ScopedSize> highestPrecedenceFirst;
	private final List<PathExpression> rootWildcardSizePaths;
	private final int[] pathLengthsShortestFirst;
	private final boolean dependsOnAncestors;
	private final boolean atSampleRoot;

	DefinedScopeContainerSizeResolver(
		List<Map.Entry<Scope, Map<PathExpression, ContainerSizeResolver>>> resolversLowestPrecedenceFirst,
		List<PathExpression> rootWildcardSizePaths,
		boolean atSampleRoot
	) {
		this.rootWildcardSizePaths = rootWildcardSizePaths;
		this.atSampleRoot = atSampleRoot;
		this.selectedHighestPrecedenceFirst = new ArrayList<>();
		this.highestPrecedenceFirst = new ArrayList<>();
		List<ScopedSize> all = new ArrayList<>();
		for (Map.Entry<Scope, Map<PathExpression, ContainerSizeResolver>> entry : resolversLowestPrecedenceFirst) {
			ScopeSelector scope = entry.getKey().getSelector();
			boolean selected = entry.getKey().isSelected();
			if (selected && atSampleRoot) {
				continue;
			}
			List<ScopedSize> sizes = new ArrayList<>();
			entry.getValue().forEach((path, resolver) -> sizes.add(0, new ScopedSize(scope, path, resolver)));
			all.addAll(sizes);
			if (selected) {
				selectedHighestPrecedenceFirst.add(0, sizes);
			} else {
				highestPrecedenceFirst.addAll(0, sizes);
			}
		}
		this.pathLengthsShortestFirst = all
			.stream()
			.mapToInt(it -> it.path.getSegments().size())
			.distinct()
			.sorted()
			.toArray();
		this.dependsOnAncestors = all
			.stream()
			.anyMatch(it -> it.scope.dependsOnAncestors() || it.path.getSegments().size() > 1);
	}

	@Override
	public @Nullable ContainerSizeResolver resolve(JvmNode containerNode, List<JvmNode> ancestors) {
		List<JvmNode> nodes = new ArrayList<>(ancestors);
		nodes.add(containerNode);
		ScopeChain chain = ScopeChain.ofNodes(nodes, JvmNodePropertyFactory.ofChain(nodes));
		if (!atSampleRoot && !selectedHighestPrecedenceFirst.isEmpty()) {
			boolean sizedByRootWildcard = isSizedByRootWildcard(chain);
			for (List<ScopedSize> selected : selectedHighestPrecedenceFirst) {
				ContainerSizeResolver resolver = resolve(chain, selected, 1, Integer.MAX_VALUE);
				if (resolver == null && !sizedByRootWildcard) {
					resolver = resolve(chain, selected, 0, 0);
				}
				if (resolver != null) {
					return resolver;
				}
			}
		}
		return atSampleRoot
			? resolve(chain, highestPrecedenceFirst, 0, 0)
			: resolve(chain, highestPrecedenceFirst, 1, Integer.MAX_VALUE);
	}

	private @Nullable ContainerSizeResolver resolve(
		ScopeChain chain,
		List<ScopedSize> sizes,
		int minScopeDepth,
		int maxScopeDepth
	) {
		for (int pathLength : pathLengthsShortestFirst) {
			int scopeDepth = chain.depth() - pathLength;
			if (scopeDepth < minScopeDepth || scopeDepth > maxScopeDepth) {
				continue;
			}
			for (ScopedSize scopedSize : sizes) {
				if (chain.matchesBelow(scopeDepth, scopedSize.path) && chain.selects(scopedSize.scope, scopeDepth)) {
					return scopedSize.resolver;
				}
			}
		}
		return null;
	}

	private boolean isSizedByRootWildcard(ScopeChain chain) {
		for (PathExpression wildcardPath : rootWildcardSizePaths) {
			if (chain.matchesBelow(0, wildcardPath)) {
				return true;
			}
		}
		return false;
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
