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

import com.navercorp.fixturemonkey.customizer.DirectivePrecedence;
import com.navercorp.fixturemonkey.customizer.ScopeChain;
import com.navercorp.fixturemonkey.customizer.ScopeSelector;
import com.navercorp.fixturemonkey.property.JvmNodePropertyFactory;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.ContainerSizeResolver;
import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.tree.AncestorAwareResolver;

/**
 * Sizes a container a defined scope reaches: the path from a node the scope selects down to the container matches
 * the path the size was declared at, and the size of the highest {@link DirectivePrecedence} wins.
 * <p>
 * It is looked up after every size the root scope declared, so a container is sized here only when the root scope
 * declared no size reaching it.
 */
final class DefinedScopeContainerSizeResolver implements AncestorAwareResolver<ContainerSizeResolver> {
	private final List<ScopedSize> scopedSizes;
	private final boolean dependsOnAncestors;

	DefinedScopeContainerSizeResolver(
		List<Map.Entry<AnalyzedScope, Map<PathExpression, ContainerSizeResolver>>> resolversLowestPrecedenceFirst
	) {
		this.scopedSizes = new ArrayList<>();
		int sequenceBase = 0;
		for (Map.Entry<AnalyzedScope, Map<PathExpression, ContainerSizeResolver>> entry
			: resolversLowestPrecedenceFirst) {
			AnalyzedScope scope = entry.getKey();
			for (Map.Entry<PathExpression, ContainerSizeResolver> size : entry.getValue().entrySet()) {
				PathExpression path = size.getKey();
				int sequence = sequenceBase + 1 + scope.getContainerSizeSequence(path);
				scopedSizes.add(new ScopedSize(
					scope.getSelector(),
					DirectivePrecedence.definedScope(scope.getPriority(), sequence),
					path,
					size.getValue()
				));
			}
			sequenceBase += 2 + scope.getLatestSequence();
		}
		this.dependsOnAncestors = scopedSizes
			.stream()
			.anyMatch(it -> it.scope.dependsOnAncestors() || it.path.getSegments().size() > 1);
	}

	@Override
	public @Nullable ContainerSizeResolver resolve(JvmNode containerNode, List<JvmNode> ancestors) {
		List<JvmNode> nodes = new ArrayList<>(ancestors);
		nodes.add(containerNode);
		ScopeChain chain = ScopeChain.ofNodes(nodes, JvmNodePropertyFactory.ofChain(nodes));
		ScopedSize best = null;
		int bestDepth = -1;
		for (ScopedSize scopedSize : scopedSizes) {
			int scopeDepth = chain.depth() - scopedSize.path.getSegments().size();
			if (scopeDepth < 0) {
				continue;
			}
			if (best != null && !scopedSize.precedence.outranksSize(scopeDepth, best.precedence, bestDepth)) {
				continue;
			}
			if (chain.matchesBelow(scopeDepth, scopedSize.path) && chain.selects(scopedSize.scope, scopeDepth)) {
				best = scopedSize;
				bestDepth = scopeDepth;
			}
		}
		return best != null ? best.resolver : null;
	}

	@Override
	public boolean dependsOnAncestors() {
		return dependsOnAncestors;
	}

	private static final class ScopedSize {
		private final ScopeSelector scope;
		private final DirectivePrecedence precedence;
		private final PathExpression path;
		private final ContainerSizeResolver resolver;

		private ScopedSize(
			ScopeSelector scope,
			DirectivePrecedence precedence,
			PathExpression path,
			ContainerSizeResolver resolver
		) {
			this.scope = scope;
			this.precedence = precedence;
			this.path = path;
			this.resolver = resolver;
		}
	}
}
