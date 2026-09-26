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

import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.customizer.ScopeChain;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.JvmNode;

/**
 * Stateless path matching over an {@link AssemblyState}: which value wins at a path among the root scope's exact
 * and wildcard values and the defined scopes' values, and whether values or not-null reach a path.
 */
final class PathMatcher {
	private PathMatcher() {
	}

	static @Nullable WinningValueCandidate findWinningCandidate(PathExpression path, AssemblyState state) {
		ScopedPath bestPath = null;
		ValueCandidate best = null;
		int bestDepth = -1;
		JvmNode bestScopeNode = null;

		boolean notNullRequired = state.scopes.isNotNullRequired(path);
		ScopeChain chain = state.chainOf(path);
		int definedScopeNotNullDepth = state.scopes.definedScopeNotNullDepth(chain);
		boolean anyNotNull = notNullRequired || definedScopeNotNullDepth != Integer.MAX_VALUE;

		ValueCandidate exactCandidate = state.candidates.at(path);
		if (exactCandidate != null && !isDefinedScopeNull(exactCandidate, anyNotNull)) {
			bestPath = ScopedPath.root(path);
			best = exactCandidate;
			bestDepth = valueScopeDepth(path, state);
			bestScopeNode = chain.scopeNodeAt(bestDepth);
		}

		for (Map.Entry<PathExpression, ValueCandidate> entry : state.candidates.getRootWildcardEntries()) {
			PathExpression pattern = entry.getKey();
			ValueCandidate candidate = entry.getValue();
			if (!pattern.matches(path) || isDefinedScopeNull(candidate, anyNotNull)) {
				continue;
			}
			ScopedPath rootPattern = ScopedPath.root(pattern);
			if (state.limits.hasRemaining(rootPattern, chain.scopeNodeAt(0))
				&& outranks(candidate.precedence, 0, best, bestDepth)) {
				bestPath = rootPattern;
				best = candidate;
				bestDepth = 0;
				bestScopeNode = chain.scopeNodeAt(0);
			}
		}

		if (best == null || !best.precedence.isRootScope()) {
			for (Map.Entry<ScopedPath, ValueCandidate> entry : state.scopes.getDefinedScopeValues()) {
				ValueCandidate candidate = entry.getValue();
				ScopedPath scopedPath = entry.getKey();
				int depth = scopedPath.scopeDepthOf(chain);
				if (depth < 0) {
					continue;
				}
				boolean yieldsToNotNull = notNullRequired
					|| state.scopes.yieldsToDefinedScopeNotNull(candidate, depth, definedScopeNotNullDepth, chain);
				if (isDefinedScopeNull(candidate, yieldsToNotNull)) {
					continue;
				}
				if (candidate.value == null && state.scopes.hasDefinedScopeDirectiveBelow(chain, depth - 1)) {
					continue;
				}
				JvmNode scopeNode = chain.scopeNodeAt(depth);
				if (!state.limits.hasRemaining(scopedPath, scopeNode)) {
					continue;
				}
				if (outranks(candidate.precedence, depth, best, bestDepth)) {
					bestPath = scopedPath;
					best = candidate;
					bestDepth = depth;
					bestScopeNode = scopeNode;
				}
			}
		}

		if (bestPath == null || best == null || bestScopeNode == null) {
			return null;
		}
		return new WinningValueCandidate(bestPath, best, bestDepth, bestScopeNode);
	}

	private static boolean isDefinedScopeNull(ValueCandidate candidate, boolean suppressed) {
		return suppressed && candidate.value == null && !candidate.precedence.isRootScope();
	}

	private static boolean outranks(
		DirectivePrecedence precedence,
		int depth,
		@Nullable ValueCandidate best,
		int bestDepth
	) {
		return best == null || precedence.outranks(depth, best.precedence, bestDepth);
	}

	/**
	 * Returns the depth of the scope node a candidate at {@code path} came from: the defined scope whose value was
	 * decomposed into it, or 0 for the root scope.
	 */
	static int valueScopeDepth(PathExpression path, AssemblyState state) {
		return Math.max(state.candidates.decomposedScopeDepthAt(path), 0);
	}

	static boolean isUnderExcludedPath(PathExpression path, Set<PathExpression> excludedPaths) {
		for (PathExpression excluded : excludedPaths) {
			if (path.isChildOf(excluded)) {
				return true;
			}
		}
		return false;
	}
}
