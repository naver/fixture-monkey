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

import com.navercorp.fixturemonkey.customizer.DirectivePrecedence;
import com.navercorp.fixturemonkey.customizer.Scope;
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

		ScopeChain chain = state.chainOf(path);
		boolean anyNotNull = state.scopes.isNotNullRequired(path) || state.scopes.hasDefinedScopeNotNullAt(chain);

		ValueCandidate exactCandidate = state.candidates.at(path);
		if (exactCandidate != null) {
			int exactDepth = valueScopeDepthAt(path, exactCandidate, state);
			if (!yieldsToNotNull(chain, path, exactCandidate, exactDepth, anyNotNull, state)) {
				bestPath = ScopedPath.root(path);
				best = exactCandidate;
				bestDepth = exactDepth;
				bestScopeNode = chain.scopeNodeAt(bestDepth);
			}
		}

		for (Map.Entry<PathExpression, ValueCandidate> entry : state.candidates.getRootWildcardEntries()) {
			PathExpression pattern = entry.getKey();
			ValueCandidate candidate = entry.getValue();
			if (!pattern.matches(path)
				|| yieldsToNotNull(chain, path, candidate, Scope.ROOT_DEPTH, anyNotNull, state)) {
				continue;
			}
			ScopedPath rootPattern = ScopedPath.root(pattern);
			if (state.limits.hasRemaining(rootPattern, chain.scopeNodeAt(Scope.ROOT_DEPTH))
				&& outranks(candidate.precedence, Scope.ROOT_DEPTH, best, bestDepth)) {
				bestPath = rootPattern;
				best = candidate;
				bestDepth = Scope.ROOT_DEPTH;
				bestScopeNode = chain.scopeNodeAt(Scope.ROOT_DEPTH);
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
				if (yieldsToNotNull(chain, path, candidate, depth, anyNotNull, state)) {
					continue;
				}
				if (candidate.value == null
					&& state.scopes.hasOutrankingValueBelow(chain, candidate.precedence, depth)) {
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

	private static boolean yieldsToNotNull(
		ScopeChain chain,
		PathExpression path,
		ValueCandidate candidate,
		int scopeDepth,
		boolean anyNotNull,
		AssemblyState state
	) {
		return candidate.value == null
			&& anyNotNull
			&& state.scopes.hasOutrankingNotNullAt(chain, path, candidate.precedence, scopeDepth);
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
	 * Returns the depth of the scope node {@code candidate} at {@code path} came from: the defined scope whose value
	 * was decomposed into it, or 0 for the root scope.
	 */
	static int valueScopeDepthAt(PathExpression path, @Nullable ValueCandidate candidate, AssemblyState state) {
		if (candidate == null || candidate.precedence.isRootScope()) {
			return Scope.ROOT_DEPTH;
		}
		return Math.max(state.candidates.decomposedScopeDepthAt(path), Scope.ROOT_DEPTH);
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
